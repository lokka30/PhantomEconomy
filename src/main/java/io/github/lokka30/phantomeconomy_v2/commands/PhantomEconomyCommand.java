package io.github.lokka30.phantomeconomy_v2.commands;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhantomEconomyCommand implements TabExecutor {

    private PhantomEconomy instance;

    public PhantomEconomyCommand(final PhantomEconomy instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            List<String> messages = Arrays.asList(
                    " ",
                    "&b&lPhantomEconomy &3v" + instance.getDescription().getVersion(),
                    "&fDeveloper &8&m->&3&o lokka30",
                    " ",
                    "&f&nSpigotMC Resource Page:",
                    "&8https://www.spigotmc.org/resources/%E2%99%A6-phantomeconomy-%E2%99%A6-for-1-7-1-15.75053/",
                    " ",
                    "&f&nAvailable commands:",
                    "&8 &m->&b /balance [player]",
                    "&8 &m->&b /pay <player> <amount>",
                    "&8 &m->&b /eco [(add/give/deposit)/(remove/take/withdraw)/set/reset <player> <amount>",
                    "&8 &m->&b /baltop [page]",
                    "&8 &m->&b /%label% [reload/backup/compatibility]",
                    " "
            );

            for (String message : messages) {
                sender.sendMessage(instance.getMessageMethods().colorize(message));
            }
        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "reload":
                    if (sender.hasPermission("phantomeconomy.reload")) {
                        sender.sendMessage("Reloading configuration...");
                        instance.getFileCache().loadFromFiles();
                        sender.sendMessage("...configuration reloaded successfuly.");
                    } else {
                        sender.sendMessage("You don't have access to that.");
                    }
                    break;
                case "backup":
                    sender.sendMessage("(Backup) This command is currently unavailable.");
                    //TODO
                    break;
                case "compatibility":
                    sender.sendMessage("(Compatibility) This command is currently unavailable.");
                    //TODO
                    break;
                default:
                    sender.sendMessage("Usage: /%label% [reload/backup/compatibility]".replace("%label%", label));
                    break;
            }
        } else {
            sender.sendMessage("Usage: /%label% [reload/backup/compatibility]".replace("%label%", label));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("reload");
            suggestions.add("backup");
            suggestions.add("compatibility");
        }
        return suggestions;
    }
}
