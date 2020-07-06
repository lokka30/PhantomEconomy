package io.github.lokka30.phantomeconomy_v2.commands;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class PhantomEconomyCommand implements CommandExecutor {

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
                    " "
            );

            for (String message : messages) {
                sender.sendMessage(instance.getMessageMethods().colorize(message));
            }
        } else {
            sender.sendMessage(instance.getMessageMethods().prefix("&b&lPhantomEconomy: &7", "Usage: &b/" + label));
        }
        return true;
    }
}
