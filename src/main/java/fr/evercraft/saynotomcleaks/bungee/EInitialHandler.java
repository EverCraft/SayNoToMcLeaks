package fr.evercraft.saynotomcleaks.bungee;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.security.MessageDigest;

import javax.crypto.SecretKey;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.http.HttpClient;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;

public class EInitialHandler extends InitialHandler {
    
    private final BungeeSayNoToMcLeaks plugin;

	public EInitialHandler(BungeeCord bungee, ListenerInfo listener, BungeeSayNoToMcLeaks plugin) {
		super(bungee, listener);
		this.plugin = plugin;
	}

	@Override
    public void handle(final EncryptionResponse encryptResponse) throws Exception {
		super.handle(encryptResponse);
		
		final String name = this.getName();
		final Boolean value = this.plugin.getListener().get(name);
		if (value != null) {
			if (value == true) {
				this.plugin.debug("The player " + name + " is present in cache (No alt account).");
			} else {
				this.plugin.debug("The player " + name + " is present in cache (alt account).");
			}
			this.plugin.getListener().send(name, value);
			return;
		}

        final String serverId = this.getServerId(encryptResponse);
		final InetAddress address = this.getAddress().getAddress();
		String url = "https://sessionserver.mojang.com/session/minecraft/hasJoined?"
				+ "username=" + URLEncoder.encode(name , "UTF-8")
				+ "&serverId=" + URLEncoder.encode(serverId , "UTF-8")
				+ "&ip=" + URLEncoder.encode(address.getHostAddress() , "UTF-8");
		
		Callback<String> handler = new Callback<String>() {
            @Override
            public void done(String result, Throwable error) {
            	EInitialHandler me = EInitialHandler.this;
            	
                if (error == null) {
                	LoginResult obj = BungeeCord.getInstance().gson.fromJson( result, LoginResult.class);
                    if ( obj != null && obj.getId() != null ) {
                    	me.plugin.getListener().put(name, true);
                    	me.plugin.debug("The player " + name + " doesn't use alt account.");
            		} else {
            			me.plugin.getListener().put(name, false);
            			me.plugin.getListener().executeCommandsSync(name);
            		}
                } else if (me.plugin.isDebug()) {
        			error.printStackTrace();
                }
            }
        };
        
        HttpClient.get(url, getChannel().getHandle().eventLoop(), handler);
	}
	
	private String getServerId(final EncryptionResponse encryptResponse) throws Exception {
		SecretKey sharedKey = EncryptionUtil.getSecret( encryptResponse, this.getRequest());
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        for ( byte[] bit : new byte[][] {
        	this.getRequest().getServerId().getBytes("ISO_8859_1"), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded()}) {
            sha.update( bit );
        }
        return URLEncoder.encode(new BigInteger(sha.digest()).toString( 16 ), "UTF-8" );
	}
	
	private EncryptionRequest getRequest() throws Exception {
		Field channelsField = InitialHandler.class.getDeclaredField("request");
		channelsField.setAccessible(true);
		return (EncryptionRequest) channelsField.get(this);
	}
	
	private ChannelWrapper getChannel() throws Exception {
		Field channelsField = InitialHandler.class.getDeclaredField("ch");
		channelsField.setAccessible(true);
		return (ChannelWrapper) channelsField.get(this);
	}
}
