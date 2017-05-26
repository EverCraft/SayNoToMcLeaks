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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;


public final class BungeeSayNoToMcLeaks extends Plugin {
	
	public static final String CHANNEL = "BungeeCord";
	public static final String SUBCHANNEL = "SayNoToMcLeaks";
	
	private Listeners listener;
	
	private boolean debug;
	private boolean isEnable;
	
	private final List<String> commands;
	
	public BungeeSayNoToMcLeaks() {
		this.commands = Collections.synchronizedList(new ArrayList<String>());
	}

	@SuppressWarnings("deprecation")
	public void onLoad() {
		this.isEnable = true;
		if (!this.getProxy().getConfig().isOnlineMode()) {
			this.warn("This server is in offline mode");
			this.isEnable = false;
		}
	}
	
	public void onEnable() {
		if (!this.isEnable) return;
		// Metrics
		this.initMetrics();
		
		try {
			this.listener = new Listeners(this);
			this.getProxy().getPluginManager().registerListener(this, this.listener);
		} catch (Exception e) {
			this.warn("The server version is incompatible");
			this.isEnable = false;
			e.printStackTrace();
		}
		
		// Commands
		Commands command = new Commands(this);
		this.getProxy().getPluginManager().registerCommand(this, command);
		this.getProxy().getPluginManager().registerListener(this, command);
		
		// Configs
		this.initConfig();
	}

	public void onReload() {
		if (!this.isEnable) return;
		
		// Configs
		this.initConfig();
		this.listener.reload();
		this.info("Reloaded");
	}
	
	/*
	 * Initialization
	 */
	
	public void initConfig() {
		if (!this.getDataFolder().exists())
			this.getDataFolder().mkdir();

        File file = new File(this.getDataFolder(), "config.yml");
     
        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config_bungee.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
		try {
			Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
			this.debug = config.getBoolean("debug");
			
			this.commands.clear();
			List<String> commands = config.getStringList("commands");
			if (commands != null) {
				this.commands.addAll(commands);
			}
		} catch (IOException e) {}
	}
	
	public void initMetrics() {
        new Metrics(this);
	}
	
	/*
	 * Log
	 */
	
	public void info(String message) {
		getLogger().info("[SayNoToMcLeaks] " + message);
	}
	
	public void warn(String message) {
		getLogger().warning("[SayNoToMcLeaks] " + message);
	}
	
	public void debug(String message) {
		if (this.debug) getLogger().info("SayNoToMcLeaks] " + message);
	}

	/*
	 * Accesseurs
	 */
	
	public List<String> getCommands() {
		return this.commands;
	}

	public Listeners getListener() {
		return this.listener;
	}

	public boolean isDebug() {
		return this.debug;
	}
	
	public boolean isEnabled() {
		return this.isEnable;
	}
}
