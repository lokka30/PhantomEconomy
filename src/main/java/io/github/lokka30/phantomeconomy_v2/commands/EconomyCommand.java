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
        sender.sendMessage(instance.getUtils().colorize("&b&lPhantomEconomy &8(v2&8)&b&l: &7This command has not been completed yet."));
        return true;
    }
}
