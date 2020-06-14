package io.github.lokka30.phantomeconomy.commands;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import io.github.lokka30.phantomeconomy.utils.LogLevel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PhantomEconomyCommand implements CommandExecutor {

    private PhantomEconomy instance;

    public PhantomEconomyCommand(final PhantomEconomy instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command cmd, @NotNull final String label, @NotNull final String[] args) {
        if (args.length == 0) {
            sender.sendMessage(instance.colorize("&a&lPhantomEconomy: &7Running &aPhantomEconomy v" + instance.getDescription().getVersion() + "&7 by &flokka30&7."));
            if (sender.hasPermission("phantomeconomy.backup")) {
                sender.sendMessage(instance.colorize("&a&lPhantomEconomy: &7To backup the data file, use &a/" + label + " backup&7."));
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("backup")) {
            if (sender.hasPermission("phantomeconomy.backup")) {
                sender.sendMessage(instance.colorize("&a&lPhantomEconomy: &7Started backup ..."));

                backupFile("settings", "yml");
                backupFile("messages", "yml");
                backupFile("data", "json");

                sender.sendMessage(instance.colorize("&a&lPhantomEconomy: &7... Backup complete successfuly."));
            } else {
                sender.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "No permission")));
            }
        } else {
            sender.sendMessage(instance.colorize("&a&lPhantomEconomy: &7Usage: &a/" + label + " [backup]"));
        }
        return true;
    }

    private void backupFile(String fileName, String fileType) {
        final String fullFileName = fileName + "." + fileType;

        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");
            LocalDateTime now = LocalDateTime.now();

            final String backupFolderPath = instance.getDataFolder() + File.separator + "backups";
            final String finalBackupFolderPath = backupFolderPath + File.separator + dtf.format(now);
            final String sourceFilePath = instance.getDataFolder() + File.separator + fullFileName;
            final String targetFilePath = finalBackupFolderPath + File.separator + fullFileName;

            File backupsFolder = new File(backupFolderPath);
            File finalBackupsFolder = new File(finalBackupFolderPath);
            File source = new File(sourceFilePath);
            File target = new File(targetFilePath);

            if (!backupsFolder.exists() && !backupsFolder.isDirectory()) {
                instance.log(LogLevel.INFO, "'&b" + backupFolderPath + "&7' folder didn't exist, created it now.");
                if (backupsFolder.mkdir()) {
                    instance.log(LogLevel.INFO, "Folder created successfuly.");
                } else {
                    instance.log(LogLevel.WARNING, "Unable to create folder, cancelling backup of the file.");
                    return;
                }

            }

            if (!finalBackupsFolder.exists() && !finalBackupsFolder.isDirectory()) {
                instance.log(LogLevel.INFO, "'&b" + finalBackupFolderPath + "&7' folder didn't exist, created it now.");
                if (finalBackupsFolder.mkdir()) {
                    instance.log(LogLevel.INFO, "Folder created successfuly.");
                } else {
                    instance.log(LogLevel.WARNING, "Unable to create folder, cancelling backup of the file.");
                    return;
                }

            }

            if (!source.exists()) {
                instance.log(LogLevel.INFO, "File '&b" + fullFileName + "&7' didn't exist, creating it now...");

                if (source.createNewFile()) {
                    instance.log(LogLevel.INFO, "File '&b" + fullFileName + "&7' created successfuly.");
                } else {
                    instance.log(LogLevel.WARNING, "Unable to create file '&b" + fullFileName + "&7', cancelling backup of the file.");
                    return;
                }
            }

            instance.log(LogLevel.INFO, "Creating backup of file '&b" + fullFileName + "&7'...");

            Files.copy(source.toPath(), target.toPath());

            instance.log(LogLevel.INFO, "Backup of file '&b" + fullFileName + "&7'complete.");
        } catch (IOException exception) {
            instance.log(LogLevel.SEVERE, "IOException was thrown whilst attempting to backup file '&b" + fullFileName + "&7', stack trace:");
            exception.printStackTrace();
        }
    }
}
