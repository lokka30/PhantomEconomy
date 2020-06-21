package io.github.lokka30.phantomeconomy_v2;

import de.leonhard.storage.LightningBuilder;
import de.leonhard.storage.internal.FlatFile;
import io.github.lokka30.phantomeconomy_v2.api.EconomyManager;
import io.github.lokka30.phantomeconomy_v2.api.accounts.AccountManager;
import io.github.lokka30.phantomeconomy_v2.api.accounts.PlayerAccount;
import io.github.lokka30.phantomeconomy_v2.api.currencies.Currency;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.AccountAlreadyExistsException;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.InvalidCurrencyException;
import io.github.lokka30.phantomeconomy_v2.cache.FileCache;
import io.github.lokka30.phantomeconomy_v2.commands.*;
import io.github.lokka30.phantomeconomy_v2.databases.Database;
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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;

public class PhantomEconomy extends JavaPlugin {

    /*
    TODO DATABASE LAYOUT

    AccountType, AccountId, CurrencyName, Balance
    ---------------------------------------------
    NonPlayerAccount, TownyNationBal, dollars, 25.23
    PlayerAccount, notch-uuid-eh51-35151, dollars, 23.13
    PlayerAccount, notch-uuid-eh51-35151, coins, 3.19
    BankAccount, lokka30sbank, (vault currency), 2536156.67
     */

    public Utils utils;
    public FileCache fileCache;
    public AccountManager accountManager;
    public EconomyManager economyManager;
    public FlatFile settingsYaml;
    public FlatFile messagesYaml;
    private PluginManager pluginManager;
    private Database database;

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

        utils.log(LogLevel.INFO, "&8--------------------------------");
        utils.log(LogLevel.INFO, " ");
        utils.log(LogLevel.INFO, " ");
        utils.log(LogLevel.INFO, "&b&l&nWARNING!");
        utils.log(LogLevel.INFO, " ");
        utils.log(LogLevel.INFO, " ");
        utils.log(LogLevel.INFO, "&8--------------------------------");
        utils.log(LogLevel.INFO, "&bPhantomEconomy v2 is deep in development, and is not supposed to be loaded onto servers in which wouldn't want to risk harm from an economy plugin malfunction. " +
                "Please use carefully and report all issues to me, make sure to note that you are using 2.0 when reporting them. " +
                "I will not be responsible if a malfunction occurs in the plugin and damages your server. " +
                "Thank you, and be careful!");
        utils.log(LogLevel.INFO, "&8--------------------------------");
        utils.log(LogLevel.INFO, " ");
        utils.log(LogLevel.INFO, " ");
        utils.log(LogLevel.INFO, "&b&l&nWARNING!");
        utils.log(LogLevel.INFO, " ");
        utils.log(LogLevel.INFO, " ");
        utils.log(LogLevel.INFO, "&8--------------------------------");

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
            try {
                if (!accountManager.hasPlayerAccount(player)) {
                    try {
                        accountManager.createPlayerAccount(player);
                    } catch (AccountAlreadyExistsException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            PlayerAccount playerAccount = accountManager.getPlayerAccount(player);
            Currency currency = null;
            HashMap<Currency, Double> balanceMap = new HashMap<>();

            for (String currencyName : fileCache.SETTINGS_CURRENCIES_ENABLED_CURRENCIES) {
                try {
                    currency = economyManager.getCurrency(currencyName);
                } catch (InvalidCurrencyException e) {
                    e.printStackTrace();
                }

                balanceMap.put(currency, playerAccount.getBalance(currency));
            }

            accountManager.cachedPlayerAccountBalances.put(playerAccount, balanceMap);
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
                .addInputStreamFromResource("settings.yml")
                .createYaml();
        messagesYaml = LightningBuilder
                .fromFile(new File(getDataFolder() + "messages"))
                .addInputStreamFromResource("messages.yml")
                .createYaml();

        //Check if they exist
        final File settingsFile = new File(getDataFolder() + "settings.yml");
        final File messagesFile = new File(getDataFolder() + "messages.yml");

        if (!(settingsFile.exists() && !settingsFile.isDirectory())) {
            utils.log(LogLevel.INFO, "File '&bsettings.yml&7' doesn't exist. Creating it now.");
            saveResource("settings.yml", false);
        }

        if (!(messagesFile.exists() && !messagesFile.isDirectory())) {
            utils.log(LogLevel.INFO, "File '&bmessages.yml&7' doesn't exist. Creating it now.");
            saveResource("messages.yml", false);
        }

        //Check their versions
        if (settingsYaml.get("file-version", 0) != utils.getLatestSettingsFileVersion()) {
            utils.log(LogLevel.SEVERE, "File &bsettings.yml&7 is out of date! Errors are likely to occur! Reset it or merge the old values to the new file.");
        }

        if (messagesYaml.get("file-version", 0) != utils.getLatestMessagesFileVersion()) {
            utils.log(LogLevel.SEVERE, "File &bmessages.yml&7 is out of date! Errors are likely to occur! Reset it or merge the old values to the new file.");
        }

        fileCache.loadFromFiles();
    }

    private void loadDatabase() {
        utils.log(LogLevel.INFO, "&8(&33/5&8) &7Connecting to the database...");
        database = new Database(this);
        database.load();
        utils.log(LogLevel.INFO, "... connection completed.");
    }

    private void registerEvents() {
        utils.log(LogLevel.INFO, "&8(&33/5&8) &7Registering events...");

        pluginManager.registerEvents(new JoinListener(this), this);
        pluginManager.registerEvents(new QuitListener(this), this);
    }

    private void hookAvailablePlugins() {
        utils.log(LogLevel.INFO, "&8(&34/6&8) &7Hooking to available plugins...");

        if (pluginManager.getPlugin("Vault") != null) {
            utils.log(LogLevel.INFO, "Plugin '&bVault&7' installed, attempting to hook ...");
            //TODO HOOK
            utils.log(LogLevel.INFO, "... plugin '&bVault&7' hooked.");
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

        if (pluginManager.getPlugin("Vault") != null) {
            utils.log(LogLevel.INFO, "Plugin '&bVault&7' installed, attempting to unhook ...");
            //TODO Unhook
            utils.log(LogLevel.INFO, "... plugin '&bVault&7' unhooked.");
        }
    }

    private void disconnectDatabase() {
        utils.log(LogLevel.INFO, "&8(&31/2&8) &7Disconnecting database ...");
        //TODO close dtabase.
        utils.log(LogLevel.INFO, "&8(&31/2&8) &7... database disconnected.");
    }

    public Database getDatabase() {
        return database;
    }
}
