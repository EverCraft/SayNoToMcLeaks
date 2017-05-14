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
package fr.evercraft.saynotomcleaks;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import org.bukkit.entity.Player;

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

public class Listeners {
	
	private final SayNoToMcLeaks plugin;
	
	private final LoadingCache<InetSocketAddress, String> names;
	private final LoadingCache<String, Boolean> caches; // True : no McLeaks
	
	private KeyPair serverKey;
	private Object service;

	private final Class<?> classGameProfile;
	private final Class<?> classMinecraftSessionService;
	
	public Listeners(SayNoToMcLeaks plugin) throws IllegalArgumentException, IllegalAccessException {
		this.plugin = plugin;
		this.names = CacheBuilder.newBuilder()
			.expireAfterAccess(2, TimeUnit.MINUTES)
			.build(new CacheLoader<InetSocketAddress, String>() {
				@Override
				public String load(InetSocketAddress uuid) {
					return null;
				}
			});
		this.caches = CacheBuilder.newBuilder()
			.expireAfterAccess(2, TimeUnit.HOURS)
			.build(new CacheLoader<String, Boolean>() {
				@Override
				public Boolean load(String uuid) {
					return null;
				}
			});
		
		Class<?> classCraftServer = Reflection.getCraftBukkitClass("CraftServer");
		Class<?> classMinecraftServer = Reflection.getMinecraftClass("MinecraftServer");
		this.classGameProfile = Reflection.getClass("com.mojang.authlib.GameProfile");
		this.classMinecraftSessionService = Reflection.getClass("com.mojang.authlib.minecraft.MinecraftSessionService");
		
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
		
		this.listerner();
	}

	public void listerner() {
		this.plugin.getProtocolManager().addPacketListener(new PacketAdapter(
				this.plugin,
				ListenerPriority.HIGH, 
				PacketType.Login.Client.START,
				PacketType.Login.Client.ENCRYPTION_BEGIN
				) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				final Listeners me = Listeners.this;
				
				// Sauvegarde le nom du joueur par rapport à son IP et son Port
				if (event.getPacketType() == PacketType.Login.Client.START) {
					
					final WrapperLoginClientStart packet = new WrapperLoginClientStart(event.getPacket());
					me.names.put(event.getPlayer().getAddress(), packet.getProfile().getName());
				
				// Requete pour vérifier
				} else if (event.getPacketType() == PacketType.Login.Client.ENCRYPTION_BEGIN) {
					final WrapperLoginClientEncryptionBegin packet = new WrapperLoginClientEncryptionBegin(event.getPacket());
					
					final String name = me.names.getIfPresent(event.getPlayer().getAddress());
					if (name == null) {
						me.plugin.debug("Error : username not found (IP='" + event.getPlayer().getAddress() + "').");
						return;
					}
					
					final Boolean value = me.caches.getIfPresent(name);
					if (value != null) {
						if (value == true) {
							me.plugin.debug("The player " + name + " is present in cache.");
							return;
						} else if (value == false) {
							me.executeCommands(name);
							return;
						}
					}
					
					final SecretKey secretKey = me.getSecretKey(packet.getSharedSecret(), me.serverKey.getPrivate());
					final String serverId = (new BigInteger(CryptManager.getServerIdHash("", me.serverKey.getPublic(), secretKey))).toString(16);
					
					this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
					    @Override
					    public void run() {
							try {
								Object profile = Reflection.getConstructor(classGameProfile, UUID.class, String.class).invoke(null, name);
								Object result = Reflection.getMethod(classMinecraftSessionService, "hasJoinedServer", classGameProfile, String.class, InetAddress.class)
										.invoke(me.service, profile, serverId, event.getPlayer().getAddress().getAddress());
								if (result == null) {
									me.caches.put(name, false);
									me.executeCommands(name);
								} else {
									me.caches.put(name, true);
									me.plugin.debug("The player " + name + " doesn't use alt account.");
								}
							} catch (Exception e) {}
					    }
					});
				}
			}
		});
	}
	
	public SecretKey getSecretKey(byte[] secretKeyEncrypted, PrivateKey key) {
		return CryptManager.decryptSharedKey(key, secretKeyEncrypted);
	}
	
	public void executeCommands(String name) {
		this.plugin.getServer().getScheduler().runTaskLater(this.plugin, new Runnable() {
		    @Override
		    public void run() {
		    	Listeners.this.executeCommandsSync(name);
		    }
		}, 150);
	}
	
	public void executeCommandsSync(String name) {
		@SuppressWarnings("deprecation")
		Player player = this.plugin.getServer().getPlayerExact(name);
		if (player == null || player.getName() == null || player.getUniqueId() == null) {
			this.plugin.debug( name + " not found.");
		}
		
		List<?> commands = this.plugin.getConfig().getList("commands");
		if (commands == null) {
			this.plugin.warn("Error : Commands list.");
			return;
		}
		
		for (Object command : commands) {
			this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), command.toString()
					.replaceAll("<player>", player.getName())
					.replaceAll("<uuid>", player.getUniqueId().toString())
					.replaceAll("<displayname>", player.getDisplayName())
					.replaceAll("<ip>", player.getAddress().getAddress().getHostAddress()));
		}
		this.plugin.debug("The player " + name + " is an alt account.");
	}
}
