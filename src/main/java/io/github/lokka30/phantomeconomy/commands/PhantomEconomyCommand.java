package io.github.lokka30.phantomeconomy.commands;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import io.github.lokka30.phantomeconomy.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        Path source = Paths.get(instance.getDataFolder() + File.separator + fileName);
        Path target = Paths.get(instance.getDataFolder() + File.separator + "backups" + File.separator + (fileName + "_backup" + System.currentTimeMillis()));

        try {
            Files.copy(source, target);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
