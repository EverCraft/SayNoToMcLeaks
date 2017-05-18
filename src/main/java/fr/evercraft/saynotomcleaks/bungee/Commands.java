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

import fr.evercraft.saynotomcleaks.bukkit.BukkitSayNoToMcLeaks;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class Commands extends Command  implements Listener {
	
	private final BungeeSayNoToMcLeaks plugin;

	public Commands(BungeeSayNoToMcLeaks plugin) {
		super("bSayNoToMcLeaks", null, "bmcleaks");
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!this.plugin.isEnabled()) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "------------ [SayNoToMcLeaks : By rexbut] ------------"));
			sender.sendMessage(new TextComponent(ChatColor.RED + "Plugin : Disable"));
			if (!this.plugin.getProxy().getConfig().isOnlineMode()) {
				sender.sendMessage(new TextComponent(ChatColor.RED + "Reason : This plugin only run in online mode."));
			}
			return;
		}
		
		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			if (sender.hasPermission(BukkitSayNoToMcLeaks.PERMISSION_RELOAD)) {
				this.plugin.onReload();
				sender.sendMessage(new TextComponent(ChatColor.GREEN + "[SayNoToMcLeaks] Reloaded"));
			} else {
				sender.sendMessage(new TextComponent(ChatColor.RED + "[SayNoToMcLeaks] You don't have permission !"));
			}
		} else {
			sender.sendMessage(new TextComponent(ChatColor.GREEN + "------------ [SayNoToMcLeaks : By rexbut] ------------"));
			sender.sendMessage(new TextComponent(ChatColor.GREEN + "/bmcleaks help : Help plugin"));
			if (sender.hasPermission(BukkitSayNoToMcLeaks.PERMISSION_RELOAD)) {
				sender.sendMessage(new TextComponent(ChatColor.GREEN + "/bmcleaks reload : Reload plugin"));
			}
		}
	}
	
	@EventHandler
	public void onTabComplete(TabCompleteEvent event) {
		String[] args = event.getCursor().split(" ");
		if (args.length == 0 || !args[0].equalsIgnoreCase(this.getName())) return;
		
		event.getSuggestions().clear();
		if(args.length <= 2) {
			event.getSuggestions().add("help");
			event.getSuggestions().add("reload");
		}
	}
}
