package fr.evercraft.saynotomcleaks;

import java.util.Arrays;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;


public final class SayNoToMcLeaks extends JavaPlugin {
	
	private static final Logger LOG = Logger.getLogger("Minecraft");
	
	private ProtocolManager protocolManager;
	private boolean debug;

	public void onLoad() {
		this.protocolManager = ProtocolLibrary.getProtocolManager();
		if (this.protocolManager == null) {
			this.warn("Le plugin ProtocolLib n'est pas activé");
			this.setEnabled(false);
		}
	}
	
	public void onEnable() {
		new Listeners(this);
		
		// Commands
		Commands command = new Commands(this);
		this.getCommand("SayNoToMcLeaks").setExecutor(command);
		this.getCommand("SayNoToMcLeaks").setTabCompleter(command);
		
		// Configs
		this.initConfig();
	}

	public void onDisable() {
	}
	
	public void onReload() {
		// Configs
		this.initConfig();
	}
	
	public void initConfig() {
		this.saveDefaultConfig();
		this.reloadConfig();
		this.getConfig().addDefault("commands", Arrays.asList("kick <player> &cSayNoToMcLeaks"));
		this.getConfig().options().copyDefaults(true);
		this.getConfig().options().copyHeader(true);
		this.saveConfig();
		this.reloadConfig();
		
		this.debug = this.getConfig().getBoolean("debug");
	}
	
	public ProtocolManager getProtocolManager() {
		return this.protocolManager;
	}
	
	public void info(String message) {
		LOG.info("[" + this.getName() + "] " + message);
	}
	
	public void warn(String message) {
		LOG.warning("[" + this.getName() + "] " + message);
	}
	
	public void debug(String message) {
		if (this.debug) LOG.info("[" + this.getName() + "] " + message);
	}
}
