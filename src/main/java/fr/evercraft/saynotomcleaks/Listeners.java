package fr.evercraft.saynotomcleaks;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.entity.Player;

import com.comphenix.packetwrapper.WrapperLoginClientEncryptionBegin;
import com.comphenix.packetwrapper.WrapperLoginClientStart;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;

import net.minecraft.server.v1_11_R1.MinecraftEncryption;

public class Listeners {
    private final SayNoToMcLeaks plugin;
    private final LoadingCache<InetSocketAddress, String> names;
    private final LoadingCache<String, Boolean> caches; // True : no McLeaks
    
    public Listeners(SayNoToMcLeaks plugin) {
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
                        me.plugin.warn("Impossible de trouver le pseudo du joueur (IP='" + event.getPlayer().getAddress() + "')");
                        return;
                    }
                    
                    final Boolean value = me.caches.getIfPresent(name);
                    if (value != null) {
                        if (value == true) {
                            me.plugin.info("Le joueur " + name + " est déjà connu");
                            return;
                        } else if (value == false) {
                        	me.sendCommands(name);
                            return;
                        }
                    }
                        
                    final KeyPair server = ((CraftServer) this.plugin.getServer()).getServer().O();
                    final SecretKey secretKey = Listeners.this.getSecretKey(packet.getSharedSecret(), server.getPrivate());
                    final String s = (new BigInteger(MinecraftEncryption.a("", server.getPublic(), secretKey))).toString(16);
                    
                    this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                        try {
                            GameProfile gameProfile = ((CraftServer) this.plugin.getServer()).getServer().az().hasJoinedServer(new GameProfile((UUID)null, name), s, event.getPlayer().getAddress().getAddress());
                            if (gameProfile == null) {
                            	me.caches.put(name, false);
                                me.sendCommands(name);
                            } else {
                            	me.caches.put(name, true);
                                me.plugin.info("Le joueur " + name + " n'utilise pas McLeaks");
                            }
                        } catch (AuthenticationUnavailableException e) {}
                    });
                }
            }
        });
    }
    
    public SecretKey getSecretKey(byte[] secretKeyEncrypted, PrivateKey key) {
        return MinecraftEncryption.a(key, secretKeyEncrypted);
    }
    
    public void sendCommands(String name) {
    	@SuppressWarnings("deprecation")
        Player player = this.plugin.getServer().getPlayer(name);
        if (player == null) {
        	this.plugin.info("Le joueur " + name + " est introuvable");
        }
        
        player.kickPlayer("SayNoToMcLeaks");
        this.plugin.warn("Le joueur " + name + " essaye de venir avec McLeaks");
    }
}
