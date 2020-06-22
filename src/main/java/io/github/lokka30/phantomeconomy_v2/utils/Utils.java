package io.github.lokka30.phantomeconomy_v2.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Utils {

    public Utils() {
    }

    public String colorize(final String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public void log(final LogLevel logLevel, String msg) {
        msg = colorize("&b&lPhantomEconomy: &7" + msg);
        Logger logger = Bukkit.getLogger();
        switch (logLevel) {
            case INFO:
                logger.info(msg);
                break;
            case WARNING:
                logger.warning(msg);
                break;
            case SEVERE:
                logger.severe(msg);
                break;
            default:
                throw new IllegalStateException("Illegal LogLevel state: " + logLevel.toString());
        }
    }

    public List<String> getSupportedServerVersions() {
        return Arrays.asList(
                "1.16",
                "1.15",
                "1.14,",
                "1.13",
                "1.12",
                "1.11",
                "1.10",
                "1.9",
                "1.8",
                "1.7"
        );
    }

    public int getLatestSettingsFileVersion() {
        return 6;
    }

    public int getLatestMessagesFileVersion() {
        return 5;
    }
}
