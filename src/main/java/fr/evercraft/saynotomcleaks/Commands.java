package fr.evercraft.saynotomcleaks;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
	public SayNoToMcLeaks plugin;

	public Commands(SayNoToMcLeaks plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmds, String commandLabel, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			player.sendMessage("Test");
        }
		return false;
	}
}
