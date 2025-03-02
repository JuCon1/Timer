package ch.thejucon.timer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimerTP implements CommandExecutor {

    private boolean tpEnabled = false;
    private final List<Location> tpLocations = new ArrayList<>(); // List of teleport points
    private final Start plugin;

    public TimerTP(Start plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2 || !args[0].equalsIgnoreCase("tp")) {
            player.sendMessage("§cUsage: /timer tp <enable|disable|set|reset>");
            return true;
        }

        if (!player.hasPermission("timer.tp")) {
            player.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "enable":
                if (tpLocations.size() < getRequiredTpPoints()) {
                    player.sendMessage("§cYou need to set at least " + getRequiredTpPoints() + " teleport points!");
                    return true;
                }
                tpEnabled = true;
                player.sendMessage("§aTeleport enabled!");
                break;

            case "disable":
                tpEnabled = false;
                player.sendMessage("§cTeleport disabled.");
                break;

            case "set":
                if (tpLocations.size() >= getRequiredTpPoints()) {
                    player.sendMessage("§cYou cannot set more than " + getRequiredTpPoints() + " teleport points!");
                    return true;
                }

                tpLocations.add(player.getLocation());
                player.sendMessage("§aTeleport point saved! Current: " + tpLocations.size() + "/" + getRequiredTpPoints());
                break;

            case "reset":
                tpLocations.clear();
                player.sendMessage("§aAll teleport points have been reset.");
                break;

            default:
                player.sendMessage("§cInvalid command! Usage: /timer tp <enable|disable|set|reset>");
                break;
        }

        return true;
    }

    public boolean isTpEnabled() {
        return tpEnabled;
    }

    public List<Location> getTpLocations() {
        return tpLocations;
    }

    public int getRequiredTpPoints() {
        // Calculate required teleport points: online players (excluding excluded players) + additional TP points from tp.yml
        TimerPlayer timerPlayer = plugin.getTimerPlayer();
        int onlinePlayers = timerPlayer.getOnlinePlayerCount();
        int additionalPoints = plugin.getTpConfig().getInt("additional-tp-points", 5); // Default to 5 if not set
        return onlinePlayers + additionalPoints;
    }

    public void teleportPlayers() {
        if (!tpEnabled) {
            Bukkit.broadcastMessage("§cTeleport failed: Teleportation is not enabled!");
            return;
        }

        int requiredPoints = getRequiredTpPoints();
        int currentPoints = tpLocations.size();

        if (currentPoints < requiredPoints) {
            Bukkit.broadcastMessage("§cTeleport failed: Not enough teleport points set! You need " + requiredPoints + " but only have " + currentPoints + ".");
            return;
        }

        List<Location> shuffledLocations = new ArrayList<>(tpLocations);
        Collections.shuffle(shuffledLocations);

        int index = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Check if the player is excluded
            if (plugin.getTimerPlayer().getExcludedPlayers().contains(player.getName())) {
                continue; // Skip this player
            }

            if (index >= shuffledLocations.size()) {
                index = 0; // If there are more players than TP points, start from the beginning
            }
            player.teleport(shuffledLocations.get(index));
            index++;
        }

        Bukkit.broadcastMessage("§aPlayers have been teleported successfully!");
    }
}