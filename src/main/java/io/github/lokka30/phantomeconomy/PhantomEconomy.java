package io.github.lokka30.phantomeconomy;

import de.leonhard.storage.LightningBuilder;
import de.leonhard.storage.internal.FlatFile;
import io.github.lokka30.phantomeconomy.api.AccountManager;
import io.github.lokka30.phantomeconomy.api.CurrencyManager;
import io.github.lokka30.phantomeconomy.api.accounts.PlayerAccount;
import io.github.lokka30.phantomeconomy.api.currencies.Currency;
import io.github.lokka30.phantomeconomy.api.exceptions.AccountAlreadyExistsException;
import io.github.lokka30.phantomeconomy.api.exceptions.InvalidCurrencyException;
import io.github.lokka30.phantomeconomy.cache.FileCache;
import io.github.lokka30.phantomeconomy.commands.*;
import io.github.lokka30.phantomeconomy.databases.Database;
import io.github.lokka30.phantomeconomy.hooks.VaultProvider;
import io.github.lokka30.phantomeconomy.listeners.JoinListener;
import io.github.lokka30.phantomeconomy.listeners.QuitListener;
import io.github.lokka30.phantomeconomy.utils.UpdateChecker;
import io.github.lokka30.phantomeconomy.utils.Utils;
import io.github.lokka30.phantomlib.PhantomLib;
import io.github.lokka30.phantomlib.classes.CommandRegister;
import io.github.lokka30.phantomlib.classes.MessageMethods;
import io.github.lokka30.phantomlib.classes.PhantomLogger;
import io.github.lokka30.phantomlib.enums.LogLevel;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;

public class PhantomEconomy extends JavaPlugin {

    public String PREFIX = "&b&lPhantomEconomy: &7";
    private Utils utils;
    private FileCache fileCache;
    private AccountManager accountManager;
    private CurrencyManager currencyManager;
    private FlatFile settings;
    private FlatFile messages;
    private PluginManager pluginManager;
    private Database database;
    private Economy vaultProvider;
    private PhantomLogger phantomLogger;
    private MessageMethods messageMethods;
    private CommandRegister commandRegister;

    @Override
    public void onLoad() {
        pluginManager = getServer().getPluginManager();
        if (pluginManager.getPlugin("PhantomLib") == null) {
            getLogger().severe("=--------------------------------=");
            getLogger().severe("(!) MISSING DEPENDENCY WARNING (!)");
            getLogger().severe("=--------------------------------=");
            getLogger().severe(" ");
            getLogger().severe("> PhantomEconomy v2 requires PhantomLib to be installed in your plugins folder.");
            getLogger().severe(" ");
            getLogger().severe("> You can download PhantomLib here: https://www.spigotmc.org/resources/%E2%99%A6-phantomlib-%E2%99%A6-1-7-10-1-15-2.78556/");
            getLogger().severe(" ");
            getLogger().severe("> The plugin will now disable itself as PhantomLib is required for it to function.");
            getLogger().severe(" ");
            getLogger().severe("=--------------------------------=");
            pluginManager.disablePlugin(this);
        } else {
            PhantomLib phantomLib = PhantomLib.getInstance();
            phantomLogger = phantomLib.getPhantomLogger();
            messageMethods = phantomLib.getMessageMethods();
            commandRegister = phantomLib.getCommandRegister();
            utils = new Utils();
            fileCache = new FileCache(this);
            accountManager = new AccountManager(this);
            currencyManager = new CurrencyManager(this);
        }
    }

    @Override
    public void onEnable() {
        phantomLogger.log(LogLevel.INFO, PREFIX, "&8+-----+ &f(Enable Started) &8+-----+");
        final long timeStart = System.currentTimeMillis();

        phantomLogger.log(LogLevel.SEVERE, PREFIX, "&8--------------------------------");
        phantomLogger.log(LogLevel.SEVERE, PREFIX, "&4&lWARNING! &7PhantomEconomy v2.0.0 is &chighly unstable&7 and is likely " +
                "to completely not function on your server. Unintentional effects of running this pre-release may cause damage to your server. " +
                "&cYou are running this at your own risk. &7Please wait until the actual release on SpigotMC if you wish to use PhantomEconomy v2. " +
                "If you are testing this resource, please do so on a server where there is nothing too good to lose. " +
                "Thank you, and please be careful!");
        phantomLogger.log(LogLevel.SEVERE, PREFIX, "&8--------------------------------");

        checkCompatibility();
        loadFiles();
        try {
            loadDatabase();
        } catch (SQLException | InvalidCurrencyException e) {
            e.printStackTrace();
        }
        registerEvents();
        hookAvailablePlugins();
        registerCommands();
        registerMetrics();

        final long timeTaken = System.currentTimeMillis() - timeStart;
        phantomLogger.log(LogLevel.INFO, PREFIX, "&8+-----+ &f(Enable Complete, took &b" + timeTaken + "ms&f) &8+-----+");

        try {
            ensureOnlinePlayersHaveAccounts();
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
        }

        startRepeatingTasks();

        checkForUpdates();
    }

    private void checkCompatibility() {
        phantomLogger.log(LogLevel.INFO, PREFIX, "&8(&3Startup &8- &31&8/&37&8) &7Checking compatibility...");

        //Check server version
        final String currentVersion = getServer().getVersion();
        boolean isSupported = false;
        for (String supportedBaseVersion : getUtils().getSupportedServerVersions()) {
            if (currentVersion.contains(supportedBaseVersion)) {
                isSupported = true;
                break;
            }
        }
        if (!isSupported) {
            phantomLogger.log(LogLevel.WARNING, PREFIX, "Server version detected as '&b" + currentVersion + "&7', which this version of the plugin does not provide support for. Use at your own risk, and do not contact support if you have issues.");
        }

        phantomLogger.log(LogLevel.INFO, PREFIX, "&7... compatibility check completed with no severities.");
    }

    private void loadFiles() {
        phantomLogger.log(LogLevel.INFO, PREFIX, "&8(&3Startup &8- &32&8/&37&8) &7Loading files...");

        if (getDataFolder().mkdir()) {
            phantomLogger.log(LogLevel.INFO, PREFIX, "&7File &bdataFolder &7didn't exist, it has been created.");
        }

        settings = LightningBuilder
                .fromFile(new File(getDataFolder() + File.separator + "settings"))
                .addInputStreamFromResource("settings.yml")
                .createYaml();
        messages = LightningBuilder
                .fromFile(new File(getDataFolder() + File.separator + "messages"))
                .addInputStreamFromResource("messages.yml")
                .createYaml();

        //Check if they exist
        final File settingsFile = new File(getDataFolder() + File.separator + "settings.yml");
        final File messagesFile = new File(getDataFolder() + File.separator + "messages.yml");

        if (!(settingsFile.exists() && !settingsFile.isDirectory())) {
            phantomLogger.log(LogLevel.INFO, PREFIX, "File '&bsettings.yml&7' doesn't exist. Creating it now.");
            saveResource("settings.yml", false);
        }

        if (!(messagesFile.exists() && !messagesFile.isDirectory())) {
            phantomLogger.log(LogLevel.INFO, PREFIX, "File '&bmessages.yml&7' doesn't exist. Creating it now.");
            saveResource("messages.yml", false);
        }

        //Check their versions
        if (settings.get("other-options.file-version", 0) != utils.getLatestSettingsFileVersion()) {
            phantomLogger.log(LogLevel.SEVERE, PREFIX, "File &bsettings.yml&7 is out of date! Errors are likely to occur! Reset it or merge the old values to the new file.");
        }

        if (messages.get("other-options.file-version", 0) != utils.getLatestMessagesFileVersion()) {
            phantomLogger.log(LogLevel.SEVERE, PREFIX, "File &bmessages.yml&7 is out of date! Errors are likely to occur! Reset it or merge the old values to the new file.");
        }

        fileCache.loadFromFiles();

        phantomLogger.log(LogLevel.INFO, PREFIX, "&7... files loaded.");
    }

    private void loadDatabase() throws SQLException, InvalidCurrencyException {
        phantomLogger.log(LogLevel.INFO, PREFIX, "&8(&3Startup &8- &33&8/&37&8) &7Connecting to the database...");
        database = new Database(this);
        database.load();
        phantomLogger.log(LogLevel.INFO, PREFIX, "&7... connection completed.");
    }

    private void registerEvents() {
        phantomLogger.log(LogLevel.INFO, PREFIX, "&8(&3Startup &8- &34&8/&37&8) &7Registering events...");

        pluginManager.registerEvents(new JoinListener(this), this);
        pluginManager.registerEvents(new QuitListener(this), this);
        phantomLogger.log(LogLevel.INFO, PREFIX, "&7... events completed.");
    }

    private void hookAvailablePlugins() {
        phantomLogger.log(LogLevel.INFO, PREFIX, "&8(&3Startup &8- &35&8/&37&8) &7Hooking to available plugins...");

        if (pluginManager.getPlugin("Vault") != null) {
            phantomLogger.log(LogLevel.INFO, PREFIX, "&7Plugin '&bVault&7' installed, attempting to hook ...");
            vaultProvider = new VaultProvider(this);
            Bukkit.getServicesManager().register(Economy.class, vaultProvider, this, ServicePriority.Highest);
            phantomLogger.log(LogLevel.INFO, PREFIX, "&7... plugin '&bVault&7' hooked.");
        }
    }

    private void registerCommands() {
        phantomLogger.log(LogLevel.INFO, PREFIX, "&8(&3Startup &8- &36&8/&37&8) &7Registering commands...");

        getCommandRegister().registerCommand(this, "balance", new BalanceCommand(this));
        getCommandRegister().registerCommand(this, "baltop", new BaltopCommand(this));
        getCommandRegister().registerCommand(this, "economy", new EconomyCommand(this));
        getCommandRegister().registerCommand(this, "pay", new PayCommand(this));
        getCommandRegister().registerCommand(this, "paytoggle", new PayToggleCommand(this));
        getCommandRegister().registerCommand(this, "phantomeconomy", new PhantomEconomyCommand(this));

        phantomLogger.log(LogLevel.INFO, PREFIX, "&7... commands registered.");
    }

    private void registerMetrics() {
        phantomLogger.log(LogLevel.INFO, PREFIX, "&8(&3Startup &8- &37&8/&37&8) &7Starting bStats...");

        new Metrics(this, 6463);

        phantomLogger.log(LogLevel.INFO, PREFIX, "&7... bStats started.");
    }

    private void startRepeatingTasks() {
        final long fifteenMinutes = 20 * 60 * 15;
        final long fourtyFiveMinutes = 20 * 60 * 45;

        if (getFileCache().SETTINGS_STARTUP_TASKS_CLEAR_BALTOP_CACHE) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    getDatabase().clearBaltopCacheAndServerTotal();
                }
            }.runTaskTimer(this, fifteenMinutes, fifteenMinutes);
        }

        if (getFileCache().SETTINGS_STARTUP_TASKS_CLEAR_PLAYER_ACCOUNT_CACHE) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    getAccountManager().cachedPlayerAccountBalances.clear();
                }
            }.runTaskTimer(this, fourtyFiveMinutes, fourtyFiveMinutes);
        }

        if (getFileCache().SETTINGS_STARTUP_TASKS_CLEAR_NON_PLAYER_ACCOUNT_CACHE) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    getAccountManager().cachedNonPlayerAccountBalances.clear();
                }
            }.runTaskTimer(this, fourtyFiveMinutes, fourtyFiveMinutes);
        }

        if (getFileCache().SETTINGS_STARTUP_TASKS_CLEAR_BANK_ACCOUNT_CACHE) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    getAccountManager().cachedBankAccountBalances.clear();
                }
            }.runTaskTimer(this, fourtyFiveMinutes, fourtyFiveMinutes);
        }
    }

    private void ensureOnlinePlayersHaveAccounts() throws InvalidCurrencyException {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!accountManager.hasPlayerAccount(player.getUniqueId(), getCurrencyManager().getDefaultCurrency())) {
                try {
                    accountManager.createPlayerAccount(player.getUniqueId(), getCurrencyManager().getDefaultCurrency());
                } catch (AccountAlreadyExistsException | InvalidCurrencyException e) {
                    e.printStackTrace();
                }
            }

            PlayerAccount playerAccount = accountManager.getPlayerAccount(player.getUniqueId());
            Currency currency = null;
            HashMap<String, Double> balanceMap = new HashMap<>();

            for (String currencyName : fileCache.SETTINGS_CURRENCIES_ENABLED_CURRENCIES) {
                try {
                    currency = currencyManager.getCurrency(currencyName);
                } catch (InvalidCurrencyException e) {
                    e.printStackTrace();
                }

                assert currency != null;
                balanceMap.put(currency.getName(), playerAccount.getBalance(currency));
            }

            accountManager.cachedPlayerAccountBalances.put(player.getUniqueId(), balanceMap);
        }
    }

    private void checkForUpdates() {
        if (fileCache.SETTINGS_OTHER_USE_UPDATE_CHECKER) {
            phantomLogger.log(LogLevel.INFO, PREFIX, "&8(&3Update Checker&8) &7Checking for updates...");
            new UpdateChecker(this, 75053).getVersion(version -> {
                final String currentVersion = getDescription().getVersion();

                if (currentVersion.equals(version)) {
                    phantomLogger.log(LogLevel.INFO, PREFIX, "&8(&3Update Checker&8) &7You're running the latest version '&b" + currentVersion + "&7'.");
                } else {
                    phantomLogger.log(LogLevel.WARNING, PREFIX, "&8(&3Update Checker&8) &7There's a new update available: '&b" + version + "&7'. You're running '&b" + currentVersion + "&7'.");
                }
            });
        }
    }

    @Override
    public void onDisable() {
        phantomLogger.log(LogLevel.INFO, PREFIX, "&8+-----+ &f(Disable Started) &8+-----+");
        final long startTime = System.currentTimeMillis();

        unhookAvailablePlugins();
        disconnectDatabase();

        final long totalTime = System.currentTimeMillis() - startTime;
        phantomLogger.log(LogLevel.INFO, PREFIX, "&8+-----+ &f(Disable Complete, took &b" + totalTime + "ms&f) &8+-----+");
    }

    private void unhookAvailablePlugins() {
        phantomLogger.log(LogLevel.INFO, PREFIX, "&8(&3Shutdown &8- &31&8/&32&8) &7Unhooking from available plugins...");

        if (pluginManager.getPlugin("Vault") != null) {
            phantomLogger.log(LogLevel.INFO, PREFIX, "&7Plugin '&bVault&7' installed, attempting to unhook ...");
            Bukkit.getServicesManager().unregister(Economy.class, vaultProvider);

            phantomLogger.log(LogLevel.INFO, PREFIX, "&7... plugin '&bVault&7' unhooked.");
        }
    }

    private void disconnectDatabase() {
        phantomLogger.log(LogLevel.INFO, PREFIX, "&8(&3Shutdown &8- &32&8/&32&8) &7 &7Disconnecting database ...");
        database.close();
        phantomLogger.log(LogLevel.INFO, PREFIX, "&7... database disconnected.");
    }

    public Database getDatabase() {
        return database;
    }

    public FlatFile getSettings() {
        return settings;
    }

    public FlatFile getMessages() {
        return messages;
    }

    public Utils getUtils() {
        return utils;
    }

    public FileCache getFileCache() {
        return fileCache;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    public PhantomLogger getPhantomLogger() {
        return phantomLogger;
    }

    public CommandRegister getCommandRegister() {
        return commandRegister;
    }

    public MessageMethods getMessageMethods() {
        return messageMethods;
    }
}
