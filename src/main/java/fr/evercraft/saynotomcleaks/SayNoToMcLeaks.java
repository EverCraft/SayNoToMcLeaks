package fr.evercraft.saynotomcleaks;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;


public final class SayNoToMcLeaks extends JavaPlugin {
	
	private static final Logger LOG = Logger.getLogger("Minecraft");
	
	private ProtocolManager protocolManager;

	public void onEnable() {
		this.protocolManager = setupProtocolManager();
		if (this.protocolManager == null) return; // Erreur
		
		this.getCommand("evertest").setExecutor(new Commands(this));
	}

	public void onDisable() {
	}
	
	/**
	 * Rechargement du plugin
	 */
	public void onReload() {
		
	}
	
	public ProtocolManager getProtocolManager() {
		return this.protocolManager;
	}
	
	private ProtocolManager setupProtocolManager() {
		ProtocolManager protocolManager = null;
		Plugin plugin = getServer().getPluginManager().getPlugin("ProtocolLib");
		if ((plugin == null) || (!(plugin instanceof ProtocolLibrary))) {
			this.warn("Le plugin ProtocolLib n'est pas activé");
			this.setEnabled(false);
		} else {
			protocolManager = ProtocolLibrary.getProtocolManager();
		}
		return protocolManager;
	}
	
	public void info(String message) {
		LOG.info("[" + this.getName() + "] " + message);
	}
	
	public void warn(String message) {
		LOG.warning("[" + this.getName() + "] " + message);
	}
}
