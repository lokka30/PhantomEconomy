package io.github.lokka30.phantomeconomy;

import de.leonhard.storage.LightningBuilder;
import de.leonhard.storage.internal.FlatFile;
import io.github.lokka30.phantomeconomy.commands.*;
import io.github.lokka30.phantomeconomy.listeners.JoinListener;
import io.github.lokka30.phantomeconomy.listeners.QuitListener;
import io.github.lokka30.phantomeconomy.listeners.SignPlaceListener;
import io.github.lokka30.phantomeconomy.listeners.SignUseListener;
import io.github.lokka30.phantomeconomy.utils.LogLevel;
import io.github.lokka30.phantomeconomy.utils.UpdateChecker;
import io.github.lokka30.phantomeconomy.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;

public class PhantomEconomy extends JavaPlugin {

    public BaltopUpdater baltopUpdater;

    public FlatFile settings;
    public FlatFile messages;
    public FlatFile data;

    public Economy provider;
    public PluginManager pluginManager;

    public HashMap<OfflinePlayer, Double> balanceCache;

    public boolean hasTownyCompatibility = false;

    @Override
    public void onLoad() {
        baltopUpdater = new BaltopUpdater(this);
        pluginManager = getServer().getPluginManager();
        balanceCache = new HashMap<>();
    }

    @Override
    public void onEnable() {
        log(LogLevel.INFO, "--+-- Enabling Began --+--");

        log(LogLevel.INFO, "Checking for incompatibilities...");

        if (checkCompatibility()) {
            log(LogLevel.INFO, "Loading files...");
            loadFiles();

            log(LogLevel.INFO, "Registering events...");
            registerEvents();

            log(LogLevel.INFO, "Registering commands...");
            registerCommands();

            log(LogLevel.INFO, "Hooking to Vault...");
            hookVault();

            log(LogLevel.INFO, "Starting bStats metrics...");
            new Metrics(this, 6463);

            if (settings.get("use-baltop-update-task", true)) {
                log(LogLevel.INFO, "Starting baltop update task...");

                new BukkitRunnable() {
                    public void run() {
                        baltopUpdater.update();
                    }
                }.runTaskTimer(this, 0L, 20 * 60 * 10L); //20 ticks per second. 60 seconds per minute. 10 of these. = 10 minutes per baltop update.
            }

            if (settings.get("use-balance-update-task", false)) {
                log(LogLevel.INFO, "Starting balance update task...");

                new BukkitRunnable() {
                    public void run() {
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            balanceCache.put(onlinePlayer, Utils.round(provider.getBalance(onlinePlayer)));
                        }
                    }
                }.runTaskTimer(this, 0L, 20 * 60 * 10L); //20 ticks per second. 60 seconds per minute. 10 of these. = 10 minutes per baltop update.
            }

            log(LogLevel.INFO, "--+-- Enabling Complete --+--");
        } else {
            pluginManager.disablePlugin(this);
        }

        checkUpdates();

        startBalanceUpdateTask();
    }

    @Override
    public void onDisable() {
        log(LogLevel.INFO, "--+-- Disabling Began --+--");

        log(LogLevel.INFO, "Unhooking from Vault...");
        unhookVault();

        log(LogLevel.INFO, "--+-- Disabling Began --+--");
    }

    private boolean checkCompatibility() {
        // --- Check if the server version is compatible. ---
        // Note: This doesn't stop the loading of the plugin. It just informs the server owners that they won't get support.
        final String currentVersion = getServer().getVersion();
        boolean isSupportedVersion = false;
        for (String supportedVersion : Utils.getSupportedServerVersions()) {
            if (currentVersion.contains(supportedVersion)) {
                isSupportedVersion = true;
                break;
            }
        }
        if (!isSupportedVersion) {
            log(LogLevel.WARNING, "Possible incompatibility found: &cServer is not running a supported version!");
            log(LogLevel.WARNING, "Your server version doesn't match with the recommended version above.");
            log(LogLevel.WARNING, "Support will not be provided from the author if encounter issues if you don't run the recommended server version.");
        }

        // --- Check if the server has Vault loaded. ---
        // Note: If Vault isn't found, the plugin will stop loading.
        if (pluginManager.getPlugin("Vault") == null) {
            log(LogLevel.SEVERE, "Incompatibility found: &cVault is not installed!");
            log(LogLevel.SEVERE, "This plugin depends on Vault to interact with other plugins.");
            log(LogLevel.SEVERE, "Link to dependency: https://www.spigotmc.org/resources/vault.34315/");
            log(LogLevel.SEVERE, "The plugin will now disable itself.");
            return false;
        }

        // --- Check if the server has Towny loaded. ---
        // Note: If Towny isn't found, then the plugin will keep loading, but disable Towny compatibility.
        if (pluginManager.getPlugin("Towny") == null) {
            hasTownyCompatibility = false;
        } else {
            hasTownyCompatibility = true;
            log(LogLevel.INFO, "Towny found -- towny compatibility enabled.");
        }

        // --- No incompatibilities found, continue loading. ---
        return true;
    }

    private void loadFiles() {
        //Tell LightningStorage to start its business.
        final String path = "plugins/PhantomEconomy/";
        settings = LightningBuilder
                .fromFile(new File(path + "settings"))
                .addInputStreamFromResource("settings.yml")
                .createYaml();
        messages = LightningBuilder
                .fromFile(new File(path + "messages"))
                .addInputStreamFromResource("messages.yml")
                .createYaml();
        data = LightningBuilder
                .fromFile(new File(path + "data"))
                .addInputStreamFromResource("data.json")
                .createJson();

        //Check if they exist
        final File settingsFile = new File(path + "settings.yml");
        final File messagesFile = new File(path + "messages.yml");
        final File dataFile = new File(path + "data.json");

        if (!(settingsFile.exists() && !settingsFile.isDirectory())) {
            log(LogLevel.INFO, "File &asettings.yml&7 doesn't exist. Creating it now.");
            saveResource("settings.yml", false);
        }

        if (!(messagesFile.exists() && !messagesFile.isDirectory())) {
            log(LogLevel.INFO, "File &amessages.yml&7 doesn't exist. Creating it now.");
            saveResource("messages.yml", false);
        }

        if (!(dataFile.exists() && !dataFile.isDirectory())) {
            log(LogLevel.INFO, "File &adata.json&7 doesn't exist. Creating it now.");
            saveResource("data.json", false);
        }

        //Check their versions
        if (settings.get("file-version", 0) != Utils.getRecommendedSettingsVersion()) {
            log(LogLevel.SEVERE, "File &asettings.yml&7 is out of date! Errors are likely to occur! Reset it or merge the old values to the new file.");
        }

        if (messages.get("file-version", 0) != Utils.getRecommendedMessagesVersion()) {
            log(LogLevel.SEVERE, "File &amessages.yml&7 is out of date! Errors are likely to occur! Reset it or merge the old values to the new file.");
        }

        if (data.get("file-version", 0) != Utils.getRecommendedDataVersion()) {
            log(LogLevel.SEVERE, "File &adata.yml&7 is out of date! Errors are likely to occur! Reset it or merge the old values to the new file.");
        }
    }

    private void registerEvents() {
        pluginManager.registerEvents(new JoinListener(this), this);
        pluginManager.registerEvents(new SignPlaceListener(this), this);
        pluginManager.registerEvents(new SignUseListener(this), this);
        pluginManager.registerEvents(new QuitListener(this), this);
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("balance")).setExecutor(new BalanceCommand(this));
        Objects.requireNonNull(getCommand("baltop")).setExecutor(new BaltopCommand(this));
        Objects.requireNonNull(getCommand("economy")).setExecutor(new EconomyCommand(this));
        Objects.requireNonNull(getCommand("pay")).setExecutor(new PayCommand(this));
        Objects.requireNonNull(getCommand("phantomeconomy")).setExecutor(new PhantomEconomyCommand(this));
    }

    public void hookVault() {
        if (pluginManager.getPlugin("Vault") == null) {
            log(LogLevel.INFO, "Vault isn't installed - this somehow got past the compatibility check - hook task aborted.");
        } else {
            provider = new VaultImplementer(this);
            Bukkit.getServicesManager().register(Economy.class, provider, this, ServicePriority.Highest);
            log(LogLevel.INFO, "Hooked to Vault successfuly.");
        }
    }

    public void unhookVault() {
        if (pluginManager.getPlugin("Vault") == null) {
            log(LogLevel.INFO, "Vault isn't installed - unhook task aborted.");
        } else {
            Bukkit.getServicesManager().unregister(Economy.class, provider);
            log(LogLevel.INFO, "Unhooked from Vault successfully.");
        }
    }

    private void checkUpdates() {
        if (settings.get("updater", true)) {
            log(LogLevel.INFO, "&8[&7Update Checker&8] &7Starting version comparison...");
            new UpdateChecker(this, 75053).getVersion(version -> {
                if (getDescription().getVersion().equalsIgnoreCase(version)) {
                    log(LogLevel.INFO, "&8[&7Update Checker&8] &7You're running the latest version.");
                } else {
                    log(LogLevel.WARNING, "&8[&7Update Checker&8] &7There's a new update available: &a" + version + "&7. You're running &a" + getDescription().getVersion() + "&7.");
                }
            });
        }
    }

    private void startBalanceUpdateTask() {
        if (settings.get("use-balance-update-task", false)) {
            new BukkitRunnable() {

                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        double balance = provider.getBalance(player);
                        balanceCache.put(player, balance);
                        data.set("players." + player.getUniqueId().toString() + ".balance", balance);
                    }
                }
            }.runTaskTimer(this, 0L, 20L * 60 * 10);
            /*
            20 as there are 20 ticks per second
            60 as there are 60 seconds per minute
            10 as it should run every 10 minutes
             */
        }
    }

    public String colorize(final String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg
                .replaceAll("%arrow%", "►"));
    }

    public void log(final LogLevel level, String msg) {
        final Logger logger = getLogger();
        msg = "&7" + msg;
        switch (level) {
            case INFO:
                logger.info(colorize(msg));
                break;
            case WARNING:
                logger.warning(colorize(msg));
                break;
            case SEVERE:
                logger.severe(colorize(msg));
                break;
            default:
                logger.severe(colorize("Unexpected LogLevel specified. message: " + msg));
        }
    }

    public double getDefaultBalance() {
        double defaultBalance;

        if (settings.get("default-balance.enabled", true)) {
            try {
                defaultBalance = settings.get("default-balance.amount", 50.00);
            } catch (NumberFormatException e) {
                log(LogLevel.SEVERE, "Invalid default balance! Please set it to a valid number. Using default $50.00.");
                defaultBalance = 50.00;
            }
        } else {
            defaultBalance = 0.00;
        }

        return defaultBalance;
    }
}
