package ch.thejucon.timer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TimerPlayer implements CommandExecutor {

    private final List<String> excludedPlayers = new ArrayList<>(); // List of excluded players

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("timer")) {
            return false;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /timer player <exclude <Name>|add <Name>>");
            return true;
        }

        if (args[0].equalsIgnoreCase("player")) {
            if (!sender.hasPermission("timer.player")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage("§cUsage: /timer player <exclude <Name>|add <Name>>");
                return true;
            }

            if (args[1].equalsIgnoreCase("exclude")) {
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /timer player exclude <Name>");
                    return true;
                }

                String playerName = args[2];
                Player player = Bukkit.getPlayer(playerName);

                if (player == null || !player.isOnline()) {
                    sender.sendMessage("§cPlayer " + playerName + " is not online or does not exist.");
                    return true;
                }

                if (excludedPlayers.contains(playerName)) {
                    sender.sendMessage("§cPlayer " + playerName + " is already excluded.");
                    return true;
                }

                excludedPlayers.add(playerName);
                sender.sendMessage("§aPlayer " + playerName + " has been excluded from teleportation.");
            } else if (args[1].equalsIgnoreCase("add")) {
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /timer player add <Name>");
                    return true;
                }

                String playerName = args[2];
                Player player = Bukkit.getPlayer(playerName);

                if (player == null || !player.isOnline()) {
                    sender.sendMessage("§cPlayer " + playerName + " is not online or does not exist.");
                    return true;
                }

                if (!excludedPlayers.contains(playerName)) {
                    sender.sendMessage("§cPlayer " + playerName + " is not excluded.");
                    return true;
                }

                excludedPlayers.remove(playerName);
                sender.sendMessage("§aPlayer " + playerName + " has been added back to teleportation.");
            } else {
                sender.sendMessage("§cUnknown subcommand. Usage: /timer player <exclude <Name>|add <Name>>");
            }
        }

        return true;
    }

    public List<String> getExcludedPlayers() {
        return excludedPlayers;
    }

    public int getOnlinePlayerCount() {
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!excludedPlayers.contains(player.getName())) {
                count++;
            }
        }
        return count;
    }
}