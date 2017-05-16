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
package fr.evercraft.saynotomcleaks.bungee;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PipelineUtils;

public class Listeners implements Listener {
	
	private final SayNoToMcLeaks plugin;
	
	public ChannelInitializer<Channel> channels;
	public Method channelsMethod;
	
	private final LoadingCache<String, Boolean> caches; // True == no McLeaks
	
	public Listeners(SayNoToMcLeaks plugin) throws Exception {
		this.plugin = plugin;
		this.caches = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.HOURS)
			.build(new CacheLoader<String, Boolean>() {
				@Override
				public Boolean load(String uuid) {
					return null;
				}
			});

		this.listener();
	}
	
	public LoadingCache<String, Boolean> getCaches() {
		return this.caches;
	}

	/**
	 * Initalise le listener de ProtocolLib
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public void listener() throws Exception {
		System.out.println("Listeners.listener()");
		
		Class<?> classPipelineUtils = Class.forName("net.md_5.bungee.netty.PipelineUtils");
		Class<?> classChannelInitializer = Class.forName("io.netty.channel.ChannelInitializer");
		Class<?> classChannel = Class.forName("io.netty.channel.Channel");
		
		Field channelsField = classPipelineUtils.getDeclaredField("SERVER_CHILD");
		channelsField.setAccessible(true);
		
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(channelsField, channelsField.getModifiers() & ~Modifier.FINAL);
		
		this.channels = (ChannelInitializer<Channel>) channelsField.get(null);
		this.channelsMethod = classChannelInitializer.getDeclaredMethod("initChannel", classChannel);
		this.channelsMethod.setAccessible(true);
		
		channelsField.set(null, new ChannelInitializer<Channel>() {
	        @Override
	        protected void initChannel(Channel ch) throws Exception {
	        	final Listeners me = Listeners.this;
	        	
	        	me.channelsMethod.invoke(me.channels, ch);
	            ch.pipeline().get(HandlerBoss.class).setHandler(new EInitialHandler(BungeeCord.getInstance(), ch.attr(PipelineUtils.LISTENER).get(), me.plugin));
	        }
	    });
	}
	
	/**
	 * Event exécuté quand le joueur charge le monde
	 * @param event
	 */
	@EventHandler
	public void onPlayerJoin(final PostLoginEvent event) {
		final Boolean value = this.caches.getIfPresent(event.getPlayer().getName());
		if (value != null && !value) {
			this.executeCommandsSync(event.getPlayer());
		}
	}
	
	public void executeCommandsSync(final String name) {
		final ProxiedPlayer player = this.plugin.getProxy().getPlayer(name);
		if (player == null) {
			this.plugin.debug( name + " not found.");
			return;
		}
		
		this.executeCommandsSync(player);
	}

	public void executeCommandsSync(final ProxiedPlayer player) {		
		final String name = player.getName();
		final String uuid = player.getUniqueId().toString();
		final String displayname = player.getDisplayName();
		final String ip = player.getAddress().getAddress().getHostAddress();

		for (String command : this.plugin.getCommands()) {
			this.plugin.getProxy().getPluginManager().dispatchCommand(this.plugin.getProxy().getConsole(), command
					.replaceAll("<player>", name)
					.replaceAll("<uuid>", uuid)
					.replaceAll("<displayname>", displayname)
					.replaceAll("<ip>", ip));
		}
		this.plugin.debug("The player " + name + " is an alt account.");
	}
}
