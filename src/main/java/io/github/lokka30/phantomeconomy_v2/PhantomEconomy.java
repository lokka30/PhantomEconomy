package io.github.lokka30.phantomeconomy_v2;

import de.leonhard.storage.LightningBuilder;
import de.leonhard.storage.internal.FlatFile;
import io.github.lokka30.phantomeconomy_v2.api.EconomyManager;
import io.github.lokka30.phantomeconomy_v2.api.accounts.AccountManager;
import io.github.lokka30.phantomeconomy_v2.cache.FileCache;
import io.github.lokka30.phantomeconomy_v2.commands.*;
import io.github.lokka30.phantomeconomy_v2.databases.mysql.MySQLDatabase;
import io.github.lokka30.phantomeconomy_v2.databases.sqlite.SQLiteDatabase;
import io.github.lokka30.phantomeconomy_v2.listeners.JoinListener;
import io.github.lokka30.phantomeconomy_v2.listeners.QuitListener;
import io.github.lokka30.phantomeconomy_v2.utils.LogLevel;
import io.github.lokka30.phantomeconomy_v2.utils.UpdateChecker;
import io.github.lokka30.phantomeconomy_v2.utils.Utils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public class PhantomEconomy extends JavaPlugin {

    public Utils utils;
    public FileCache fileCache;
    public AccountManager accountManager;
    public EconomyManager economyManager;
    public FlatFile settingsYaml;
    public FlatFile messagesYaml;
    public FlatFile dataJson;
    public boolean isTownyCompatibilityEnabled;
    private PluginManager pluginManager;
    private MySQLDatabase mysqlDatabase;
    private SQLiteDatabase sqliteDatabase;

    @Override
    public void onLoad() {
        utils = new Utils(this);
        fileCache = new FileCache(this);
        accountManager = new AccountManager(this);
        economyManager = new EconomyManager(this);
        pluginManager = getServer().getPluginManager();
    }

    @Override
    public void onEnable() {
        utils.log(LogLevel.INFO, "&8+---+ &f(Enable Started) &8+---+");
        final long timeStart = System.currentTimeMillis();

        checkCompatibility();
        loadFiles();
        loadDatabase();
        registerEvents();
        hookAvailablePlugins();
        registerCommands();
        registerMetrics();

        final long timeTaken = System.currentTimeMillis() - timeStart;
        utils.log(LogLevel.INFO, "&8+---+ &f(Enable Complete, took &b" + timeTaken + "ms&f) &8+---+");

        for (Player player : Bukkit.getOnlinePlayers()) {
            //TODO update accounts async
        }

        checkForUpdates();
    }

    private void checkCompatibility() {
        utils.log(LogLevel.INFO, "&8(&31/5&8) &7Checking compatibility...");

        //Check server version
        final String currentVersion = getServer().getClass().getPackage().getName().split("\\.")[3];
        if (utils.getSupportedServerVersions().contains(currentVersion)) {
            utils.log(LogLevel.INFO, "Detected server version as '&b" + currentVersion + "&7' (supported).");
        } else {
            utils.log(LogLevel.WARNING, "Server version detected as '&b" + currentVersion + "&7', which this version of the plugin does not support. Use at your own risk, and do not contact support if you have issues.");
        }
    }

    private void loadFiles() {
        utils.log(LogLevel.INFO, "&8(&32/5&8) &7Loading files...");

        settingsYaml = LightningBuilder
                .fromFile(new File(getDataFolder() + "settings"))
                .addInputStreamFromResource("old/settings.yml")
                .createYaml();
        messagesYaml = LightningBuilder
                .fromFile(new File(getDataFolder() + "messages"))
                .addInputStreamFromResource("old/messages.yml")
                .createYaml();
        dataJson = LightningBuilder
                .fromFile(new File(getDataFolder() + "data"))
                .addInputStreamFromResource("old/data.json")
                .createJson();

        //Check if they exist
        final File settingsFile = new File(getDataFolder() + "old/settings.yml");
        final File messagesFile = new File(getDataFolder() + "old/messages.yml");
        final File dataFile = new File(getDataFolder() + "old/data.json");

        if (!(settingsFile.exists() && !settingsFile.isDirectory())) {
            utils.log(LogLevel.INFO, "File '&bsettings.yml&7' doesn't exist. Creating it now.");
            saveResource("old/settings.yml", false);
        }

        if (!(messagesFile.exists() && !messagesFile.isDirectory())) {
            utils.log(LogLevel.INFO, "File '&bmessages.yml&7' doesn't exist. Creating it now.");
            saveResource("old/messages.yml", false);
        }

        if (!(dataFile.exists() && !dataFile.isDirectory())) {
            utils.log(LogLevel.INFO, "File '&bdata.json&7' doesn't exist. Creating it now.");
            saveResource("old/data.json", false);
        }

        //Check their versions
        if (settingsYaml.get("file-version", 0) != utils.getLatestSettingsFileVersion()) {
            utils.log(LogLevel.SEVERE, "File &asettings.yml&7 is out of date! Errors are likely to occur! Reset it or merge the old values to the new file.");
        }

        if (messagesYaml.get("file-version", 0) != utils.getLatestMessagesFileVersion()) {
            utils.log(LogLevel.SEVERE, "File &amessages.yml&7 is out of date! Errors are likely to occur! Reset it or merge the old values to the new file.");
        }

        if (dataJson.get("file-version", 0) != utils.getLatestDataFileVersion()) {
            utils.log(LogLevel.SEVERE, "File &adata.yml&7 is out of date! Errors are likely to occur! Reset it or merge the old values to the new file.");
        }

        fileCache.loadFromFiles();
    }

    private void loadDatabase() {
        utils.log(LogLevel.INFO, "&8(&33/5&8) &7Loading database...");
        switch (fileCache.SETTINGS_DATABASE_TYPE.toLowerCase()) {
            case "sqlite":
                utils.log(LogLevel.INFO, "Using SQLite database");
                if (mysqlDatabase != null) {
                    mysqlDatabase = null;
                }
                sqliteDatabase = new SQLiteDatabase(this);
                sqliteDatabase.load();
                utils.log(LogLevel.INFO, "Loaded database");
                break;
            case "mysql":
                utils.log(LogLevel.INFO, "Using MySQL database");
                if (sqliteDatabase != null) {
                    sqliteDatabase = null;
                }
                mysqlDatabase = new MySQLDatabase(this);
                mysqlDatabase.updateSettings();
                mysqlDatabase.startSQLConnection();
                utils.log(LogLevel.INFO, "Started SQL connection");
                break;
            default:
                utils.log(LogLevel.SEVERE, "Invalid database type in settings!");
                break;
        }
    }

    private void registerEvents() {
        utils.log(LogLevel.INFO, "&8(&33/5&8) &7Registering events...");

        pluginManager.registerEvents(new JoinListener(this), this);
        pluginManager.registerEvents(new QuitListener(this), this);
    }

    private void hookAvailablePlugins() {
        utils.log(LogLevel.INFO, "&8(&34/6&8) &7Hooking to available plugins...");

        if (pluginManager.getPlugin("Towny") == null) {
            utils.log(LogLevel.INFO, "Plugin '&bTowny&7' isn't installed, skipping compatibility");
            isTownyCompatibilityEnabled = false;
        } else {
            utils.log(LogLevel.INFO, "&7Plugin '&bTowny&7' installed, enabling Towny account support...");
            isTownyCompatibilityEnabled = true;
        }

        if (pluginManager.getPlugin("Vault") == null) {
            utils.log(LogLevel.INFO, "Plugin '&bVault&7' isn't installed, skipping hook");
        } else {
            utils.log(LogLevel.INFO, "Plugin '&bVault&7' installed, attempting to hook...");
            //TODO
        }
    }

    private void registerCommands() {
        utils.log(LogLevel.INFO, "&8(&35/6&8) &7Registering commands...");

        Objects.requireNonNull(getCommand("balance")).setExecutor(new BalanceCommand(this));
        Objects.requireNonNull(getCommand("baltop")).setExecutor(new BaltopCommand(this));
        Objects.requireNonNull(getCommand("economy")).setExecutor(new EconomyCommand(this));
        Objects.requireNonNull(getCommand("pay")).setExecutor(new PayCommand(this));
        Objects.requireNonNull(getCommand("phantomeconomy")).setExecutor(new PhantomEconomyCommand(this));
    }

    private void registerMetrics() {
        utils.log(LogLevel.INFO, "&8(&36/6&8) &7Registering bStats...");

        new Metrics(this, 6463);
    }

    private void checkForUpdates() {
        if (fileCache.SETTINGS_OTHER_USE_UPDATE_CHECKER) {
            utils.log(LogLevel.INFO, "&8(&3Update Checker&8) &7Checking for updates...");
            new UpdateChecker(this, 75053).getVersion(version -> {
                final String currentVersion = getDescription().getVersion();

                if (currentVersion.equals(version)) {
                    utils.log(LogLevel.INFO, "&8(&3Update Checker&8) &7You're running the latest version '&b" + currentVersion + "&7'.");
                } else {
                    utils.log(LogLevel.WARNING, "&8(&3Update Checker&8) &7There's a new update available: '&b" + version + "&7'. You're running '&b" + currentVersion + "&7'.");
                }
            });
        }
    }

    @Override
    public void onDisable() {
        utils.log(LogLevel.INFO, "&8+---+ &f(Disable Started) &8+---+");
        final long startTime = System.currentTimeMillis();

        unhookAvailablePlugins();
        disconnectDatabase();

        final long totalTime = System.currentTimeMillis() - startTime;
        utils.log(LogLevel.INFO, "&8+---+ &f(Disable Complete, took &b" + totalTime + "ms&f) &8+---+");
    }

    private void unhookAvailablePlugins() {
        utils.log(LogLevel.INFO, "&8(&31/2&8) &7Unhooking from available plugins...");

        if (pluginManager.getPlugin("Vault") == null) {
            //TODO Not Installed
        } else {
            //TODO Unhook
        }
    }

    private void disconnectDatabase() {
        utils.log(LogLevel.INFO, "&8(&31/2&8) &7Disconnecting database...");
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage("TODO")); //TODO Update Accounts for all online players.
        //TODO close dtabase.
    }
}
