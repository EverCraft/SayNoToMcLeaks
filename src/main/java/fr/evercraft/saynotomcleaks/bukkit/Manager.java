package fr.evercraft.saynotomcleaks.bukkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public abstract class Manager implements Listener {

	protected final BukkitSayNoToMcLeaks plugin;
	
	protected final LoadingCache<String, Boolean> caches; // True == no McLeaks
	protected final List<String> commands;

	public Manager(BukkitSayNoToMcLeaks plugin) {
		this.plugin = plugin;
		this.commands = Collections.synchronizedList(new ArrayList<String>());
		this.caches = CacheBuilder.newBuilder()
			.expireAfterAccess(1, TimeUnit.HOURS)
			.build(new CacheLoader<String, Boolean>() {
				@Override
				public Boolean load(String uuid) {
					return null;
				}
			});
		this.reload();
	}
	
	public void reload() {
		this.commands.clear();
		this.caches.invalidateAll();
		
		final List<String> commands = this.plugin.getConfig().getStringList("commands");
		if (commands != null) {
			this.commands.addAll(commands);
		}
	}
	
	/**
	 * Event exécuté quand le joueur charge le monde
	 * @param event
	 */
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Boolean value = this.caches.getIfPresent(event.getPlayer().getName());
		if (value != null && !value) {
			this.executeCommands(event.getPlayer().getName(), 100);
		}
	}
	
	/**
	 * Execute la liste de commande dans un certains temps
	 * @param name Le pseudo du joueur
	 * @param later Le temps avant l'exécution
	 */
	public void executeCommands(final String name, long later) {
		this.plugin.getServer().getScheduler().runTaskLater(this.plugin, new Runnable() {
		    @Override
		    public void run() {
		    	Manager.this.executeCommandsSync(name);
		    }
		}, later);
	}
	
	/**
	 * Execute la liste des commandes immédiatement. A faire uniquement en synchrone !
	 * @param name Le pseudo du joueur
	 */
	public void executeCommandsSync(final String name) {
		@SuppressWarnings("deprecation")
		final Player player = this.plugin.getServer().getPlayerExact(name);
		if (player == null) {
			this.plugin.debug( name + " not found.");
			return;
		}
		
		if (player.hasPermission(BukkitSayNoToMcLeaks.PERMISSION_BYPASS)) {
			this.plugin.debug("The player " + name + " is an alt account but he has bypass permission.");
			return;
		}
		
		final String uuid = player.getUniqueId().toString();
		final String displayname = player.getDisplayName();
		final String ip = player.getAddress().getAddress().getHostAddress();
		
		for (String command : this.commands) {
			this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), command
					.replaceAll("<player>", name)
					.replaceAll("<uuid>", uuid)
					.replaceAll("<displayname>", displayname)
					.replaceAll("<ip>", ip));
		}
		this.plugin.debug("The player " + name + " is an alt account.");
	}
}
