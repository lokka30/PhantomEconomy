package io.github.lokka30.phantomeconomy_v2.utils;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;

public class Utils {

    private PhantomEconomy instance;

    public Utils(final PhantomEconomy instance) {
        this.instance = instance;
    }

    public String colorize(final String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public void log(final LogLevel logLevel, String msg) {
        msg = colorize("&b&lPhantomEconomy: &7" + msg);
        switch (logLevel) {
            case INFO:
                Bukkit.getLogger().info(msg);
                break;
            case WARNING:
                Bukkit.getLogger().warning(msg);
                break;
            case SEVERE:
                Bukkit.getLogger().severe(msg);
                break;
            default:
                throw new IllegalStateException("Illegal LogLevel state: " + logLevel.toString());
        }
    }

    public List<String> getSupportedServerVersions() {
        return Arrays.asList(
                "1.15.2",
                "1.15.1",
                "1.15",
                "1.14.4",
                "1.14.3",
                "1.14.2",
                "1.14.1",
                "1.14",
                "1.13.2",
                "1.13.1",
                "1.13",
                "1.12.2",
                "1.12.1",
                "1.12",
                "1.11.2",
                "1.11.1",
                "1.11",
                "1.10.2",
                "1.10.1",
                "1.10",
                "1.9.4",
                "1.9.3",
                "1.9.2",
                "1.9.1",
                "1.9",
                "1.8.9",
                "1.8.8",
                "1.8.7",
                "1.8.6",
                "1.8.5",
                "1.8.4",
                "1.8.3",
                "1.8.2",
                "1.8.1",
                "1.8",
                "1.7.10"
        );
    }

    public int getLatestSettingsFileVersion() {
        return 6;
    }

    public int getLatestMessagesFileVersion() {
        return 5;
    }

    public int getLatestDataFileVersion() {
        return 1;
    }
}
