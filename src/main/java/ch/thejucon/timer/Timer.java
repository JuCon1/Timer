package ch.thejucon.timer;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Timer implements CommandExecutor {

    private final Start plugin;
    private BukkitRunnable timerTask;
    private int totalTime = 0;
    private int remainingTime = 0;
    private boolean isRunning = false;
    private boolean isPaused = false;

    public Timer(Start plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // **Fix: Nur "/timer" wird verarbeitet, alle anderen Befehle wie "/time" bleiben erhalten**
        if (!cmd.getName().equalsIgnoreCase("timer")) {
            return false; // Minecraft verarbeitet den Befehl normal weiter
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("Nutzung: /timer <start|stop|pause|continue|settime>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                startTimer();
                break;
            case "stop":
                stopTimer(true);
                break;
            case "pause":
                pauseTimer();
                break;
            case "continue":
                continueTimer();
                break;
            case "settime":
                if (args.length < 2) {
                    sender.sendMessage("Nutzung: /timer settime <Minuten>");
                    return true;
                }
                setTime(sender, args[1]);
                break;
            default:
                sender.sendMessage("Unbekannter Befehl: " + args[0]);
                return true;
        }
        return true;
    }

    private void startTimer() {
        if (isRunning || timerTask != null) return;
        if (totalTime == 0) return;

        isRunning = true;
        isPaused = false;
        remainingTime = totalTime;

        Bukkit.broadcastMessage("Timer gestartet für " + formatTime(totalTime) + "!");
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
            Bukkit.broadcastMessage("Timer wurde gestoppt!");
        }
    }

    private void pauseTimer() {
        if (!isRunning || isPaused) return;
        isPaused = true;
    }

    private void continueTimer() {
        if (!isRunning || !isPaused) return;
        isPaused = false;
        runTimerTask();
    }

    private void setTime(CommandSender sender, String minutesString) {
        try {
            int minutes = Integer.parseInt(minutesString);
            if (minutes <= 0) {
                sender.sendMessage("Die Zeit muss größer als 0 sein!");
                return;
            }

            totalTime = minutes * 60;
            remainingTime = totalTime;
            sender.sendMessage("Timer auf " + formatTime(totalTime) + " gesetzt.");
        } catch (NumberFormatException e) {
            sender.sendMessage("Ungültige Zahl! Nutzung: /timer settime <Minuten>");
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
                    Bukkit.broadcastMessage("Zeit abgelaufen!");
                }
            }
        };

        timerTask.runTaskTimer(plugin, 0, 20);
    }

    private void updateActionBar() {
        String time = formatTime(remainingTime);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(time));
        }
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
