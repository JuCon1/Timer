package ch.thejucon.timer;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Start extends JavaPlugin {

    private Timer timer;
    private TimerTP timerTP;
    private TimerPlayer timerPlayer;
    private FileConfiguration tpConfig;
    private FileConfiguration colorsConfig;
    private File tpFile;
    private File colorsFile;

    @Override
    public void onEnable() {
        this.getLogger().info("Timer Plugin is loading...");

        // Load or create the tp.yml and colors.yml files
        loadTpConfig();
        loadColorsConfig();

        // Initialize classes
        timerPlayer = new TimerPlayer();
        timerTP = new TimerTP(this);
        timer = new Timer(this);

        // Register commands
        registerCommand("timer", timer); // Register the "timer" command

        this.getLogger().info("Timer Plugin has been activated!");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Timer Plugin has been deactivated!");
    }

    /**
     * Registers a command and sets its executor.
     *
     * @param name     The name of the command (e.g., "timer").
     * @param executor The executor that processes the command.
     */
    private void registerCommand(String name, Object executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor((org.bukkit.command.CommandExecutor) executor);
        } else {
            getLogger().severe("Error: The command '" + name + "' could not be registered!");
        }
    }

    /**
     * Loads the tp.yml configuration file.
     */
    private void loadTpConfig() {
        tpFile = new File(getDataFolder(), "tp.yml");
        if (!tpFile.exists()) {
            tpFile.getParentFile().mkdirs();
            saveResource("tp.yml", false);
        }
        tpConfig = YamlConfiguration.loadConfiguration(tpFile);
    }

    /**
     * Loads the colors.yml configuration file.
     */
    private void loadColorsConfig() {
        colorsFile = new File(getDataFolder(), "colors.yml");
        if (!colorsFile.exists()) {
            colorsFile.getParentFile().mkdirs();
            saveResource("colors.yml", false);
        }
        colorsConfig = YamlConfiguration.loadConfiguration(colorsFile);

        // Check if the configuration was loaded correctly
        if (colorsConfig.get("colors") == null) {
            getLogger().severe("Failed to load colors.yml! Check the file for errors.");
        }
    }

    /**
     * Returns the tp.yml configuration.
     *
     * @return The tp.yml configuration.
     */
    public FileConfiguration getTpConfig() {
        return tpConfig;
    }

    /**
     * Returns the colors.yml configuration.
     *
     * @return The colors.yml configuration.
     */
    public FileConfiguration getColorsConfig() {
        return colorsConfig;
    }

    /**
     * Saves the tp.yml configuration.
     */
    public void saveTpConfig() {
        try {
            tpConfig.save(tpFile);
        } catch (Exception e) {
            getLogger().severe("Could not save tp.yml: " + e.getMessage());
        }
    }

    /**
     * Saves the colors.yml configuration.
     */
    public void saveColorsConfig() {
        try {
            colorsConfig.save(colorsFile);
        } catch (Exception e) {
            getLogger().severe("Could not save colors.yml: " + e.getMessage());
        }
    }

    /**
     * Reloads the tp.yml configuration.
     */
    public void reloadTpConfig() {
        tpConfig = YamlConfiguration.loadConfiguration(tpFile);
    }

    /**
     * Reloads the colors.yml configuration.
     */
    public void reloadColorsConfig() {
        colorsConfig = YamlConfiguration.loadConfiguration(colorsFile);
    }

    /**
     * Returns the Timer instance.
     *
     * @return The Timer instance.
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * Returns the TimerTP instance.
     *
     * @return The TimerTP instance.
     */
    public TimerTP getTimerTP() {
        return timerTP;
    }

    /**
     * Returns the TimerPlayer instance.
     *
     * @return The TimerPlayer instance.
     */
    public TimerPlayer getTimerPlayer() {
        return timerPlayer;
    }
}