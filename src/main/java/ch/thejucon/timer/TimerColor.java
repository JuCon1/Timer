package ch.thejucon.timer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TimerColor implements CommandExecutor {

    private final Start plugin;

    public TimerColor(Start plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!sender.hasPermission("timer.color")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /timer color <color>");
            return true;
        }

        String colorName = args[0].toUpperCase();
        String colorCode = plugin.getColorsConfig().getString("colors." + colorName);

        if (colorCode == null) {
            sender.sendMessage("§cInvalid color. Available colors: " + getAvailableColors());
            return true;
        }

        plugin.getTimer().setTimerColor(colorCode);
        sender.sendMessage("§aTimer color set to " + colorName + " (" + colorCode + "Timer§r).");

        return true;
    }

    private String getAvailableColors() {
        List<String> colors = new ArrayList<>();
        if (plugin.getColorsConfig().contains("colors")) {
            colors.addAll(plugin.getColorsConfig().getConfigurationSection("colors").getKeys(false));
        }
        return String.join(", ", colors);
    }
}