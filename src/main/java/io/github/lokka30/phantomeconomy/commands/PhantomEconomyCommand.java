package io.github.lokka30.phantomeconomy.commands;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import io.github.lokka30.phantomeconomy.utils.LogLevel;
import io.github.lokka30.phantomeconomy.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PhantomEconomyCommand implements CommandExecutor {

    private PhantomEconomy instance;

    public PhantomEconomyCommand(final PhantomEconomy instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command cmd, @NotNull final String label, @NotNull final String[] args) {
        if (args.length == 0) {
            sender.sendMessage(instance.colorize("&a&lPhantomEconomy: &7Running &aPhantomEconomy v" + instance.getDescription().getVersion() + "&7, developed for server version &a" + Utils.getRecommendedServerVersion() + "&7."));
            if (sender.hasPermission("phantomeconomy.backup")) {
                sender.sendMessage(instance.colorize("&a&lPhantomEconomy: &7To backup the data file, use &a/" + label + " backup&7."));
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("backup")) {
            if (sender.hasPermission("phantomeconomy.backup")) {
                sender.sendMessage(instance.colorize("&a&lPhantomEconomy: &7Started backup ..."));

                backupFile("settings.yml");
                backupFile("messages.yml");
                backupFile("data.json");

                sender.sendMessage(instance.colorize("&a&lPhantomEconomy: &7... Backup complete successfuly."));
            } else {
                sender.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "No permission")));
            }
        } else {
            sender.sendMessage(instance.colorize("&a&lPhantomEconomy: &7Usage: &a/" + label + " [backup]"));
        }
        return true;
    }

    private void backupFile(String fileName) {

        try {
            File backupsFolder = new File(instance.getDataFolder(), File.separator + "backups");
            File source = new File(instance.getDataFolder() + File.separator + fileName);
            File target = new File(instance.getDataFolder() + File.separator + "backups" + File.separator + (fileName + "_backup" + System.currentTimeMillis()));

            if (!backupsFolder.isDirectory() && backupsFolder.mkdir()) {
                instance.log(LogLevel.INFO, "'&bbackups&7' folder didn't exist, created it now.");
            }

            if (!source.exists() && source.createNewFile()) {
                instance.log(LogLevel.INFO, "File '&b" + source.getName() + "&7' didn't exist, created it now.");
            }

            if (!target.exists() && target.createNewFile()) {
                instance.log(LogLevel.INFO, "File '&b" + target.getName() + "&7' didn't exist, created it now.");
            }

            Files.copy(source.toPath(), target.toPath());
        } catch (IOException exception) {
            instance.log(LogLevel.SEVERE, "IOException was thrown whilst attempting to backup file '&b" + fileName + "&7', stack trace:");
            exception.printStackTrace();
        }
    }
}
