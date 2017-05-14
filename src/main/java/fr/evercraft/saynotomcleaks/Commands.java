package fr.evercraft.saynotomcleaks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class Commands implements CommandExecutor, TabCompleter {
	
	public static final String PERMISSION_RELOAD = "saynotomcleaks.reload";
	
	private final SayNoToMcLeaks plugin;

	public Commands(SayNoToMcLeaks plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmds, String commandLabel, String[] args) {
		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			if (sender.hasPermission(PERMISSION_RELOAD)) {
				this.plugin.onReload();
				sender.sendMessage(ChatColor.GREEN + "[SayNoToMcLeaks] Reloaded");
			} else {
				sender.sendMessage(ChatColor.RED + "[SayNoToMcLeaks] You don't have permission !");
			}
			return true;
		} else {
			sender.sendMessage(ChatColor.GREEN + "------------ [SayNoToMcLeaks : By rexbut] ------------");
			sender.sendMessage(ChatColor.GREEN + "/saynotomcleaks help : Help plugin");
			if (sender.hasPermission(PERMISSION_RELOAD)) {
				sender.sendMessage(ChatColor.GREEN + "/saynotomcleaks reload : Reload plugin");
			}
			return true;
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> suggests = new ArrayList<String>();
		if(alias.equalsIgnoreCase("saynotomcleaks") && args.length <= 1) {
			suggests.add("help");
			if (sender.hasPermission(PERMISSION_RELOAD)) {
				suggests.add("reload");
			}
		}
		return suggests;
	}
}
