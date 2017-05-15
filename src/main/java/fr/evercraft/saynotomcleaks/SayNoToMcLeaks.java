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

import java.util.Arrays;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;


public final class SayNoToMcLeaks extends JavaPlugin {
	
	private static final Logger LOG = Logger.getLogger("Minecraft");
	
	private ProtocolManager protocolManager;
	private Listeners listener;
	private boolean debug;

	public void onLoad() {
		this.protocolManager = ProtocolLibrary.getProtocolManager();
		if (this.protocolManager == null) {
			this.warn("ProtocolLib is not enabled");
			this.setEnabled(false);
		}
		if (!this.getServer().getOnlineMode()) {
			this.warn("This server is in offline mode");
			this.setEnabled(false);
		}
	}
	
	public void onEnable() {
		// Metrics
		this.initMetrics();
		
		try {
			this.listener = new Listeners(this);
			this.getServer().getPluginManager().registerEvents(this.listener, this);
		} catch (Exception e) {
			this.warn("The server version is incompatible");
			this.setEnabled(false);
			e.printStackTrace();
		}
		
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
		this.listener.reload();
		this.info("Reloaded");
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
	
	public void initMetrics() {
        new Metrics(this);
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

	public boolean isDebug() {
		return this.debug;
	}
}
