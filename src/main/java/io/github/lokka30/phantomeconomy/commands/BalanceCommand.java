package io.github.lokka30.phantomeconomy.commands;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class BalanceCommand implements TabExecutor {

    private PhantomEconomy instance;

    public BalanceCommand(final PhantomEconomy instance) {
        this.instance = instance;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command cmd, @NotNull final String label, @NotNull final String[] args) {
        if (sender.hasPermission("phantomeconomy.balance")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    final Player player = (Player) sender;
                    player.sendMessage(instance.colorize(instance.messages.get("commands.balance.self", "Your balance is %amount%.")).replaceAll("%amount%", Matcher.quoteReplacement(instance.provider.format(instance.provider.getBalance(player)))));
                } else {
                    sender.sendMessage(instance.colorize(instance.messages.get("commands.balance.usage-console", "Usage (console): /balance <player>")));
                }
            } else if (args.length == 1) {
                if (sender.hasPermission("phantomeconomy.balance.others")) {
                    if (instance.provider.hasAccount(args[0])) {
                        sender.sendMessage(instance.colorize(instance.messages.get("commands.balance.others", "%player%'s balance is %amount%.")).replaceAll("%player%", args[0]).replaceAll("%amount%", Matcher.quoteReplacement(instance.provider.format(instance.provider.getBalance(args[0])))));
                    } else {
                        sender.sendMessage(instance.colorize(instance.messages.get("common.target-never-played-before", "%target% hasn't joined the server before.")).replaceAll("%player%", args[0]));
                    }
                } else {
                    sender.sendMessage(instance.colorize(instance.messages.get("commands.balance.no-permission-others", "You don't have access to viewing the balance of other players.")));
                }
            } else {
                sender.sendMessage(instance.colorize(instance.messages.get("commands.balance.usage", "Usage: /balance [player]")));
            }
        } else {
            sender.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "You don't have access to that.")));
            return true;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission("phantomeconomy.balance.others")) {
            Player[] players = new Player[Bukkit.getOnlinePlayers().size()];
            players = Bukkit.getOnlinePlayers().toArray(players);
            for (Player player : players) {
                suggestions.add(player.getName());
            }
        }
        return suggestions;
    }
}
