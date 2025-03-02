package ch.thejucon.timer;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Timer implements CommandExecutor, TabCompleter {

    private final Start plugin;
    private BukkitRunnable timerTask;
    private int totalTime = 0;
    private int remainingTime = 0;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private String timerColor = "§f"; // Standardfarbe: Weiß

    public Timer(Start plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("timer")) {
            return false;
        }

        if (args.length < 1) {
            // Wenn keine Argumente angegeben sind, zeige die Hilfe an
            sendHelpMessage(sender);
            return true;
        }

        // Redirect "tp" subcommand to TimerTP
        if (args[0].equalsIgnoreCase("tp")) {
            return plugin.getTimerTP().onCommand(sender, cmd, label, args);
        }

        // Redirect "player" subcommand to TimerPlayer
        if (args[0].equalsIgnoreCase("player")) {
            return plugin.getTimerPlayer().onCommand(sender, cmd, label, args);
        }

        // Handle "color" subcommand
        if (args[0].equalsIgnoreCase("color")) {
            if (!sender.hasPermission("timer.color")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage("Usage: /timer color <color>");
                return true;
            }

            String colorName = args[1].toUpperCase();
            if (plugin.getColorsConfig().contains("colors." + colorName)) {
                // Einfache Farbe
                setTimerColor(plugin.getColorsConfig().getString("colors." + colorName));
                sender.sendMessage("Timer color set to " + colorName + ".");
            } else if (plugin.getColorsConfig().contains("gradients." + colorName)) {
                // Statischer Farbverlauf
                setTimerColor(colorName);
                sender.sendMessage("Timer gradient set to " + colorName + ".");
            } else if (plugin.getColorsConfig().contains("animations." + colorName)) {
                // Animierter Farbverlauf
                setTimerColor(colorName);
                sender.sendMessage("Timer animation set to " + colorName + ".");
            } else {
                sender.sendMessage("Invalid color. Available colors: " + getAvailableColors());
                return true;
            }
            return true;
        }

        // Handle other commands in the Timer class
        switch (args[0].toLowerCase()) {
            case "start":
                if (!sender.hasPermission("timer.time")) {
                    sender.sendMessage("§cYou do not have permission to use this command.");
                    return true;
                }
                startTimer(sender);
                break;
            case "stop":
                if (!sender.hasPermission("timer.time")) {
                    sender.sendMessage("§cYou do not have permission to use this command.");
                    return true;
                }
                stopTimer(true);
                break;
            case "pause":
                if (!sender.hasPermission("timer.time")) {
                    sender.sendMessage("§cYou do not have permission to use this command.");
                    return true;
                }
                pauseTimer();
                break;
            case "continue":
                if (!sender.hasPermission("timer.time")) {
                    sender.sendMessage("§cYou do not have permission to use this command.");
                    return true;
                }
                continueTimer();
                break;
            case "settime":
                if (!sender.hasPermission("timer.settime")) {
                    sender.sendMessage("§cYou do not have permission to use this command.");
                    return true;
                }
                if (args.length < 2) {
                    sendHelpMessage(sender); // Weiterleitung zu /timer help
                    return true;
                }
                setTime(sender, args[1]);
                break;
            case "reload":
                if (!sender.hasPermission("timer.*")) {
                    sender.sendMessage("§cYou do not have permission to use this command.");
                    return true;
                }
                plugin.reloadTpConfig();
                plugin.reloadColorsConfig();
                sender.sendMessage("Configuration reloaded!");
                break;
            case "help":
                sendHelpMessage(sender);
                break;
            default:
                // Bei unbekanntem Befehl zeige die Hilfe an
                sender.sendMessage("Unknown command. Type \"/timer help\" for help.");
                return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            // Vorschläge für den Hauptbefehl (inklusive "help" und "color")
            return Arrays.asList("start", "stop", "pause", "continue", "settime", "reload", "player", "tp", "help", "color");
        } else if (args.length == 2) {
            // Vorschläge für Unterbefehle
            switch (args[0].toLowerCase()) {
                case "player":
                    return Arrays.asList("exclude", "add");
                case "tp":
                    return Arrays.asList("enable", "disable", "set", "reset");
                case "color":
                    // Gib die verfügbaren Farben, Farbverläufe und Animationen zur Tab-Vervollständigung zurück
                    List<String> colorOptions = new ArrayList<>();
                    if (plugin.getColorsConfig().contains("colors")) {
                        colorOptions.addAll(plugin.getColorsConfig().getConfigurationSection("colors").getKeys(false));
                    }
                    if (plugin.getColorsConfig().contains("gradients")) {
                        colorOptions.addAll(plugin.getColorsConfig().getConfigurationSection("gradients").getKeys(false));
                    }
                    if (plugin.getColorsConfig().contains("animations")) {
                        colorOptions.addAll(plugin.getColorsConfig().getConfigurationSection("animations").getKeys(false));
                    }
                    return colorOptions;
            }
        } else if (args.length == 3) {
            // Vorschläge für Spielernamen bei "exclude" oder "add"
            if (args[0].equalsIgnoreCase("player") && (args[1].equalsIgnoreCase("exclude") || args[1].equalsIgnoreCase("add"))) {
                List<String> playerNames = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    playerNames.add(player.getName());
                }
                return playerNames;
            }
        }
        return new ArrayList<>(); // Keine Vorschläge
    }

    /**
     * Setzt die Farbe des Timers.
     *
     * @param color Die Farbe als Minecraft-Farbcode (z. B. "§c" für Rot) oder der Name eines Farbverlaufs/Animation.
     */
    public void setTimerColor(String color) {
        this.timerColor = color;
    }

    /**
     * Gibt eine Liste der verfügbaren Farben zurück.
     *
     * @return Eine Zeichenkette mit den verfügbaren Farben.
     */
    private String getAvailableColors() {
        List<String> colors = new ArrayList<>();
        if (plugin.getColorsConfig().contains("colors")) {
            colors.addAll(plugin.getColorsConfig().getConfigurationSection("colors").getKeys(false));
        }
        if (plugin.getColorsConfig().contains("gradients")) {
            colors.addAll(plugin.getColorsConfig().getConfigurationSection("gradients").getKeys(false));
        }
        if (plugin.getColorsConfig().contains("animations")) {
            colors.addAll(plugin.getColorsConfig().getConfigurationSection("animations").getKeys(false));
        }
        return String.join(", ", colors);
    }

    /**
     * Zeigt die Hilfenachricht im Standard-Design an.
     *
     * @param sender Der CommandSender, der die Hilfe erhalten soll.
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§6--- Timer Plugin Help ---");
        sender.sendMessage("§7/timer start §f- Startet den Timer.");
        sender.sendMessage("§7/timer stop §f- Stoppt den Timer.");
        sender.sendMessage("§7/timer pause §f- Pausiert den Timer.");
        sender.sendMessage("§7/timer continue §f- Setzt den Timer fort.");
        sender.sendMessage("§7/timer settime <Hours:Minutes:Seconds> §f- Setzt die Timer-Zeit.");
        sender.sendMessage("§7/timer reload §f- Lädt die Konfiguration neu.");
        sender.sendMessage("§7/timer player exclude <Name> §f- Schließt einen Spieler von der Teleportation aus.");
        sender.sendMessage("§7/timer player add <Name> §f- Fügt einen Spieler wieder zur Teleportation hinzu.");
        sender.sendMessage("§7/timer tp enable §f- Aktiviert die Teleportation.");
        sender.sendMessage("§7/timer tp disable §f- Deaktiviert die Teleportation.");
        sender.sendMessage("§7/timer tp set §f- Setzt einen Teleportationspunkt.");
        sender.sendMessage("§7/timer tp reset §f- Setzt alle Teleportationspunkte zurück.");
        sender.sendMessage("§7/timer color <color> §f- Ändert die Farbe des Timers.");
        sender.sendMessage("§6-----------------------------");
    }

    private void startTimer(CommandSender sender) {
        if (isRunning || timerTask != null) {
            sender.sendMessage("The timer is already running.");
            return;
        }

        if (totalTime == 0) {
            sender.sendMessage("Please set a time first using /timer settime <Hours:Minutes:Seconds>.");
            return;
        }

        isRunning = true;
        isPaused = false;
        remainingTime = totalTime;

        Bukkit.broadcastMessage("Timer started for " + formatTime(totalTime) + ".");
        runTimerTask();
    }

    private void stopTimer(boolean sendMessage) {
        if (!isRunning) return;

        isRunning = false;
        isPaused = false;

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }

        clearActionBar();

        if (sendMessage) {
            Bukkit.broadcastMessage("Timer has been stopped.");
        }
    }

    private void pauseTimer() {
        if (!isRunning || isPaused) return;
        isPaused = true;
        Bukkit.broadcastMessage("Timer paused.");
    }

    private void continueTimer() {
        if (!isRunning || !isPaused) return;
        isPaused = false;
        Bukkit.broadcastMessage("Timer continued.");
        runTimerTask();
    }

    private void setTime(CommandSender sender, String timeString) {
        try {
            String[] parts = timeString.split(":");
            if (parts.length != 3) {
                sender.sendMessage("Invalid time format. Type \"/timer help\" for help.");
                return;
            }

            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);

            if (hours < 0 || minutes < 0 || seconds < 0 || minutes >= 60 || seconds >= 60) {
                sender.sendMessage("Invalid time. Minutes and seconds must be between 0 and 59. Type \"/timer help\" for help.");
                return;
            }

            totalTime = hours * 3600 + minutes * 60 + seconds;
            remainingTime = totalTime;

            sender.sendMessage("Timer set to " + formatTime(totalTime) + ".");
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid number. Type \"/timer help\" for help.");
        }
    }

    private void runTimerTask() {
        if (timerTask != null) return;

        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isRunning || isPaused) return;

                if (remainingTime > 0) {
                    remainingTime--;
                    updateActionBar();
                } else {
                    stopTimer(false);
                    Bukkit.broadcastMessage("Time's up!");

                    TimerTP timerTP = plugin.getTimerTP();
                    if (timerTP.isTpEnabled() && timerTP.getTpLocations().size() >= timerTP.getRequiredTpPoints()) {
                        timerTP.teleportPlayers();
                    }
                }
            }
        };

        timerTask.runTaskTimer(plugin, 0, 20);
    }

    private void updateActionBar() {
        String time = formatTime(remainingTime);

        // Überprüfe, ob eine Animation oder ein Farbverlauf aktiv ist
        if (plugin.getColorsConfig().contains("animations." + timerColor)) {
            // Animation verwenden
            List<String> frames = plugin.getColorsConfig().getStringList("animations." + timerColor + ".frames");
            double delay = plugin.getColorsConfig().getDouble("animations." + timerColor + ".delay", 1.0);
            int frameIndex = (int) ((System.currentTimeMillis() / (delay * 1000)) % frames.size());
            time = frames.get(frameIndex) + time;
        } else if (plugin.getColorsConfig().contains("gradients." + timerColor)) {
            // Statischen Farbverlauf verwenden
            List<String> colors = plugin.getColorsConfig().getStringList("gradients." + timerColor + ".colors");
            time = applyGradient(time, colors);
        } else {
            // Einfache Farbe verwenden
            time = timerColor + time;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(time));
        }
    }

    /**
     * Wendet einen statischen Farbverlauf auf den Text an.
     *
     * @param text  Der Text, auf den der Farbverlauf angewendet wird.
     * @param colors Die Liste der Farben im Farbverlauf.
     * @return Der Text mit dem angewendeten Farbverlauf.
     */
    private String applyGradient(String text, List<String> colors) {
        StringBuilder gradientText = new StringBuilder();
        int length = text.length();
        int colorCount = colors.size();

        for (int i = 0; i < length; i++) {
            int colorIndex = (i * colorCount) / length;
            gradientText.append(colors.get(colorIndex)).append(text.charAt(i));
        }

        return gradientText.toString();
    }

    private void clearActionBar() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
        }
    }

    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}