package io.github.lokka30.phantomeconomy.commands;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import io.github.lokka30.phantomeconomy.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class PhantomEconomyCommand implements CommandExecutor {

    private PhantomEconomy instance;

    public PhantomEconomyCommand(final PhantomEconomy instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command cmd, @NotNull final String label, @NotNull final String[] args) {
        sender.sendMessage(instance.colorize("&a&lPhantomEconomy: &7This server is running &aPhantomEconomy v" + instance.getDescription().getVersion() + "&7, developed for server version &a" + Utils.getRecommendedServerVersion() + "&7."));
        sender.sendMessage(instance.colorize("&a&lPhantomEconomy: &7Credits:"));
        for (String author : instance.getDescription().getAuthors()) {
            sender.sendMessage(instance.colorize("&8 %arrow% &7" + author));
        }
        return true;
    }
}
