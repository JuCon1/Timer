package ch.thejucon.timer;

import org.bukkit.plugin.java.JavaPlugin;

public class Start extends JavaPlugin {


    // Plugin load engine
    @Override
    public void onEnable() {
        this.getServer().getPluginManager();
        this.getLogger().info("Timer_Plugin loaded!");
    }

    // Plugin unload engine
    @Override
    public void onDisable() {
        this.getLogger().info("Timer_Plugin saved!");
    }
}