package fr.evercraft.saynotomcleaks;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;


public final class SayNoToMcLeaks extends JavaPlugin {
	
	private static final Logger LOG = Logger.getLogger("Minecraft");
	
	private ProtocolManager protocolManager;

	public void onLoad() {
		this.protocolManager = ProtocolLibrary.getProtocolManager();
		if (this.protocolManager == null) {
			this.warn("Le plugin ProtocolLib n'est pas activé");
			this.setEnabled(false);
		}
	}
	
	public void onEnable() {
		new Listeners(this);
		
		this.getCommand("SayNoToMcLeaks").setExecutor(new Commands(this));
	}

	public void onDisable() {
	}
	
	public void onReload() {
		
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
}
