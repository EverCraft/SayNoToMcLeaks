/*
 * This file is part of SayNoToMcLeaks.
 *
 * SayNoToMcLeaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SayNoToMcLeaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SayNoToMcLeaks.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.evercraft.saynotomcleaks.bukkit;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import com.comphenix.packetwrapper.WrapperLoginClientEncryptionBegin;
import com.comphenix.packetwrapper.WrapperLoginClientStart;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.tinyprotocol.Reflection;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ManagerBukkit extends Manager {
	
	private final LoadingCache<InetSocketAddress, String> names;
	
	private KeyPair serverKey;
	private Object service;

	private final Class<?> classMinecraftSessionService;
	private final Class<?> classHasJoinedMinecraftServerResponse;
	private final Class<?> classYggdrasilMinecraftSessionService;
	private final Class<?> classYggdrasilAuthenticationService;
	
	public ManagerBukkit(BukkitSayNoToMcLeaks plugin) throws IllegalArgumentException, IllegalAccessException {
		super(plugin);
		
		this.names = CacheBuilder.newBuilder()
			.expireAfterAccess(2, TimeUnit.MINUTES)
			.build(new CacheLoader<InetSocketAddress, String>() {
				@Override
				public String load(InetSocketAddress uuid) {
					return null;
				}
			});
		
		Class<?> classCraftServer = Reflection.getCraftBukkitClass("CraftServer");
		Class<?> classMinecraftServer = Reflection.getMinecraftClass("MinecraftServer");
		this.classMinecraftSessionService = Reflection.getClass("com.mojang.authlib.minecraft.MinecraftSessionService");
		this.classHasJoinedMinecraftServerResponse = Reflection.getClass("com.mojang.authlib.yggdrasil.response.HasJoinedMinecraftServerResponse");
		this.classYggdrasilMinecraftSessionService = Reflection.getClass("com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService");
		this.classYggdrasilAuthenticationService = Reflection.getClass("com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService");
		
		Object console = Reflection.getField(classCraftServer, "console", classMinecraftServer).get(this.plugin.getServer());
		for (Field field : classMinecraftServer.getDeclaredFields()) {
			if (field.getType().getName().equals(KeyPair.class.getName())) {
				field.setAccessible(true);
				this.serverKey = (KeyPair) field.get(console);
			} else if (field.getType().getName().equals(this.classMinecraftSessionService.getName())) {
				field.setAccessible(true);
				this.service = field.get(console);
			}
		}
		
		Preconditions.checkNotNull(this.serverKey);
		Preconditions.checkNotNull(this.service);
		
		this.listener();
	}

	/**
	 * Initalise le listener de ProtocolLib
	 */
	public void listener() {
		this.plugin.getProtocolManager().addPacketListener(new PacketAdapter(
				this.plugin,
				ListenerPriority.HIGH, 
				PacketType.Login.Client.START,
				PacketType.Login.Client.ENCRYPTION_BEGIN
				) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				final ManagerBukkit me = ManagerBukkit.this;
				final InetSocketAddress address = event.getPlayer().getAddress();
				
				// Sauvegarde le nom du joueur par rapport à son IP et son Port
				if (event.getPacketType() == PacketType.Login.Client.START) {
					final WrapperLoginClientStart packet = new WrapperLoginClientStart(event.getPacket());
					me.names.put(address, packet.getProfile().getName());
				
				// Requete pour vérifier
				} else if (event.getPacketType() == PacketType.Login.Client.ENCRYPTION_BEGIN) {
					final WrapperLoginClientEncryptionBegin packet = new WrapperLoginClientEncryptionBegin(event.getPacket());
					
					final String name = me.names.getIfPresent(address);
					if (name == null) {
						me.plugin.debug("Error : username not found (IP='" + address.getAddress().getHostName() + "').");
						return;
					}
					
					final Boolean value = me.caches.getIfPresent(name);
					if (value != null) {
						if (value == true) {
							me.plugin.debug("The player " + name + " is present in cache (No alt account).");
						} else {
							me.plugin.debug("The player " + name + " is present in cache (alt account).");
						}
						return;
					}
					
					final SecretKey secretKey = CryptManager.decryptSharedKey(me.serverKey.getPrivate(), packet.getSharedSecret());
					final String serverId = (new BigInteger(CryptManager.getServerIdHash("", me.serverKey.getPublic(), secretKey))).toString(16);
					
					this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, new Runnable() {
					    @Override
					    public void run() {
							me.task(name, serverId, address.getAddress(), true);
					    }
					}, 60);
				}
			}
		});
	}
	
	/**
	 * Réalise la requête au prêt de Mojang, ajoute le resultat dans le cache et applique les commandes si nécessaire
	 * @param name Le pseudo du joueur
	 * @param serverId Le serverId hash
	 * @param address L'adresse IP du joueur
	 * @param again True : on réessaye de faire la requête si il y a une erreur
	 */
	public void task(final String name, final String serverId, final InetAddress address, boolean again) {
		try {
			boolean result = this.isSafe(name, serverId, address); 
			
			// Si il y les 2 IP correspondent
			if (result) {
				this.caches.put(name, true);
				this.plugin.debug("The player " + name + " doesn't use alt account.");
				
			// Les 2 IP ne correspondent pas
			} else {
				this.caches.put(name, false);
				this.executeCommands(name, 100);
			}
		} catch (Exception e) {
			// S'il y a eu un problème lors de la connexion au serveur
			if (e.getClass().getName().equals("com.mojang.authlib.exceptions.AuthenticationException")) {
				// On réessaye dans 1 minutes
				if (again) {
					this.plugin.debug("The client has sent too many requests within a certain amount of time. (name='" + name + "',IP='" + address.getHostName() + "')");
					this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, new Runnable() {
					    @Override
					    public void run() {
					    	ManagerBukkit.this.task(name, serverId, address, false);
					    }
					}, 60 * 20L);
				} else {
					this.plugin.debug("It is not possible to make requests to Mojang. (name='" + name + "',IP='" + address.getHostName() + "')");
				}
			// Autre erreur
			} else if (this.plugin.isDebug()) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Réalise la requête au prêt de Mojang
	 * @param name Le pseudo du joueur
	 * @param serverId Le serverId hash
	 * @param address L'adresse IP du joueur
	 * @return True : Le joueur à la même IP
	 * @throws Exception
	 */
	public boolean isSafe(final String name, final String serverId, final InetAddress address) throws Exception {
		URL url = new URL("https://sessionserver.mojang.com/session/minecraft/hasJoined?"
				+ "username=" + URLEncoder.encode(name , "UTF-8")
				+ "&serverId=" + URLEncoder.encode(serverId , "UTF-8")
				+ "&ip=" + URLEncoder.encode(address.getHostAddress() , "UTF-8"));
		
		Object service = Reflection.getMethod(this.classYggdrasilMinecraftSessionService, "getAuthenticationService").invoke(this.service);
		Object reponse = Reflection.getMethod(this.classYggdrasilAuthenticationService, "makeRequest", java.net.URL.class, java.lang.Object.class, java.lang.Class.class)
				.invoke(service, url, null, this.classHasJoinedMinecraftServerResponse);
		
		if ((reponse != null) && (Reflection.getMethod(this.classHasJoinedMinecraftServerResponse, "getId").invoke(reponse) != null)) {
			return true;
		}
		return false;
	}
}
