package io.github.lokka30.phantomeconomy_v2.commands;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class EconomyCommand implements CommandExecutor {

    private PhantomEconomy instance;

    public EconomyCommand(final PhantomEconomy instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] strings) {
        sender.sendMessage(instance.getMessageMethods().colorize("&b&lPhantomEconomy v2: &7This command has not been completed yet."));
        return true;
    }
}
