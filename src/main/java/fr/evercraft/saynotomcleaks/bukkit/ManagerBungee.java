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
package fr.evercraft.saynotomcleaks.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import fr.evercraft.saynotomcleaks.bungee.BungeeSayNoToMcLeaks;

public class ManagerBungee extends Manager implements PluginMessageListener {

	public ManagerBungee(BukkitSayNoToMcLeaks plugin) {
		super(plugin);
		
		this.listener();
	}
	
	public void listener() {
		this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(this.plugin, "BungeeCord");
	    this.plugin.getServer().getMessenger().registerIncomingPluginChannel(this.plugin, "BungeeCord", this);
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals(BungeeSayNoToMcLeaks.CHANNEL)) return;
		
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
	    String subchannel = in.readUTF();
	    if (subchannel.equals(BungeeSayNoToMcLeaks.SUBCHANNEL)) {
	    	final String name = in.readUTF();
	    	final boolean result = in.readBoolean();
	    	
	    	// S'il est déjà connu
	    	if (this.caches.getIfPresent(name) != null) return;
	    	
	    	this.caches.put(name, result);
	    	// Si les 2 IP correspondent
	    	if (result) {
				this.plugin.debug("The player " + name + " doesn't use alt account.");
				
			// Les 2 IP ne correspondent pas
			} else {
				this.executeCommands(name, 100);
			}
	    }
		
	}
}
