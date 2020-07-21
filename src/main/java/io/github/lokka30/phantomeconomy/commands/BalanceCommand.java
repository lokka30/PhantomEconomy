package io.github.lokka30.phantomeconomy.commands;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import io.github.lokka30.phantomeconomy.api.currencies.Currency;
import io.github.lokka30.phantomeconomy.api.exceptions.AccountAlreadyExistsException;
import io.github.lokka30.phantomeconomy.api.exceptions.InvalidCurrencyException;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        if (sender.hasPermission("phantomeconomy.balance")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    final Player player = (Player) sender;
                    sender.sendMessage("Your balance: ");
                    try {
                        for (Currency currency : instance.getCurrencyManager().getEnabledCurrencies()) {
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
                    sender.sendMessage("Balance for %player%:"
                            .replace("%player%", Objects.requireNonNull(target.getName())));
                    for (Currency currency : instance.getCurrencyManager().getEnabledCurrencies()) {
                        if (!instance.getAccountManager().hasPlayerAccount(target, currency)) {
                            instance.getAccountManager().createPlayerAccount(target, currency);
                        }
                        final double balance = instance.getAccountManager().getPlayerAccount(target).getBalance(currency);
                        final String currencyName = WordUtils.capitalize(currency.getName().toLowerCase());
                        sender.sendMessage(" -> (%currencyName%): %balance%"
                                .replace("%currencyName%", currencyName)
                                .replace("%balance%", currency.formatFinalBalance(balance)));
                    }
                } catch (InvalidCurrencyException | AccountAlreadyExistsException e) {
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
        List<String> suggestions = new ArrayList<>();
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
        return suggestions;
    }
}
