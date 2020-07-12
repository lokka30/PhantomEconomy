package io.github.lokka30.phantomeconomy_v2.commands;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import io.github.lokka30.phantomeconomy_v2.api.currencies.Currency;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.InvalidCurrencyException;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BalanceCommand implements TabExecutor {

    private PhantomEconomy instance;

    public BalanceCommand(final PhantomEconomy instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage(instance.getMessageMethods().colorize("&b&lPhantomEconomy &8(v2&8)&b&l: &7This command has not been completed yet."));

        if (sender.hasPermission("phantomeconomy.balance")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    final Player player = (Player) sender;
                    sender.sendMessage("Your balance: ");
                    try {
                        for (Currency currency : instance.getEconomyManager().getEnabledCurrencies()) {
                            final double balance = instance.getAccountManager().getPlayerAccount(player).getBalance(currency);
                            final String currencyName = WordUtils.capitalize(currency.getName().toLowerCase());
                            sender.sendMessage(" -> (%currencyName%): %balance%"
                                    .replace("%currencyName%", currencyName)
                                    .replace("%balance%", currency.formatFinalBalance(balance)));
                        }
                    } catch (InvalidCurrencyException e) {
                        sender.sendMessage("Unable to retrieve your balance, please inform an administrator that the plugin is incorrectly configured.");
                        e.printStackTrace();
                    }
                } else {
                    sender.sendMessage("Usage (console): /balance <currency> <player>");
                }
            } else if (args.length == 1) {
                @SuppressWarnings("deprecation") final OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

                try {
                    if (instance.getAccountManager().hasPlayerAccount(target)) {
                        sender.sendMessage("Balance for %player%:"
                                .replace("%player%", Objects.requireNonNull(target.getName())));
                        for (Currency currency : instance.getEconomyManager().getEnabledCurrencies()) {
                            final double balance = instance.getAccountManager().getPlayerAccount(target).getBalance(currency);
                            final String currencyName = WordUtils.capitalize(currency.getName().toLowerCase());
                            sender.sendMessage(" -> (%currencyName%): %balance%"
                                    .replace("%currencyName%", currencyName)
                                    .replace("%balance%", currency.formatFinalBalance(balance)));
                        }
                    }
                } catch (SQLException | InvalidCurrencyException e) {
                    e.printStackTrace();
                }
            } else {
                sender.sendMessage("Usage: /%label% [player]".replace("%label%", label));
            }
        } else {
            sender.sendMessage("You don't have access to that.");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<String>();
        if (args.length == 1) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    if (player.canSee(onlinePlayer) || player.isOp()) {
                        suggestions.add(onlinePlayer.getName());
                    }
                } else {
                    suggestions.add(onlinePlayer.getName());
                }
            }
        }
        return null;
    }
}
