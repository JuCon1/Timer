package ch.thejucon.timer;

import ch.thejucon.timer.Timer;
import org.bukkit.plugin.java.JavaPlugin;

public class Start extends JavaPlugin {

    private Timer timer;

    @Override
    public void onEnable() {
        this.getLogger().info("Timer_Plugin loaded!");

        // Initialisiere den Timer und registriere den Befehl
        timer = new Timer(this);
        if (getCommand("timer") != null) {
            getCommand("timer").setExecutor(timer);
        } else {
            getLogger().severe("❌ Fehler: Befehl 'timer' konnte nicht registriert werden!");
        }
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Timer_Plugin saved!");
    }
}

