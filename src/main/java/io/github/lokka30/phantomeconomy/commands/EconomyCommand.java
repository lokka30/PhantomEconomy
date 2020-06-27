package io.github.lokka30.phantomeconomy.commands;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

public class EconomyCommand implements TabExecutor {

    private PhantomEconomy instance;

    public EconomyCommand(final PhantomEconomy instance) {
        this.instance = instance;
    }

    /*
    Reference: args.length and args[i]

    args.length:
    /economy(0) add(1) Notch(2) 23(3)

    args[i]:
    /economy(null) add(0) Notch(1) 23(2)
     */

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command cmd, @NotNull final String label, @NotNull final String[] args) {
        if (sender.hasPermission("phantomeconomy.economy")) {
            if (args.length > 0) {
                switch (args[0]) {
                    case "add":
                        if (sender.hasPermission("phantomeconomy.economy.add")) {
                            if (args.length == 3) {
                                if (instance.provider.hasAccount(args[1])) {
                                    final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);

                                    double amount;

                                    try {
                                        amount = Double.parseDouble(args[2]);
                                    } catch (NumberFormatException exception) {
                                        sender.sendMessage(instance.colorize(instance.messages.get("common.invalid-number-double", "Invalid number - %arg% is not a valid number.")).replaceAll("%arg%", args[2]));
                                        return true;
                                    }

                                    if (amount < 0) {
                                        sender.sendMessage(instance.colorize(instance.messages.get("common.invalid-number-negative", "Invalid number - %number% is negative.")).replaceAll("%number%", args[2]));
                                        return true;
                                    } else if (amount == 0) {
                                        sender.sendMessage(instance.colorize(instance.messages.get("common.invalid-number-zero", "Invalid number - 0 is not allowed.")));
                                        return true;
                                    } else {
                                        instance.provider.depositPlayer(offlinePlayer, amount);
                                        sender.sendMessage(instance.colorize(instance.messages.get("commands.economy.add.success", "Deposited %amount% to %player%'s balance.")).replaceAll("%amount%", Matcher.quoteReplacement(instance.provider.format(amount))).replaceAll("%player%", args[1]));

                                        if (offlinePlayer.isOnline()) {
                                            Objects.requireNonNull(offlinePlayer.getPlayer()).sendMessage(instance.colorize(instance.messages.get("commands.economy.add.by", "%amount% was deposited by %sender%."))
                                                    .replaceAll("%amount%", Matcher.quoteReplacement(instance.provider.format(amount)))
                                                    .replaceAll("%sender%", sender.getName()));
                                        }

                                        return true;
                                    }
                                } else {
                                    sender.sendMessage(instance.colorize(instance.messages.get("common.target-never-played-before", "%player% hasn't joined the server before."))
                                            .replaceAll("%player%", args[1]));
                                    return true;
                                }
                            } else {
                                sender.sendMessage(instance.colorize(instance.messages.get("commands.economy.add.usage", "/economy add <player> <amount>")));
                                return true;
                            }
                        } else {
                            sender.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "You don't have access to that.")));
                            return true;
                        }
                    case "remove":
                        if (sender.hasPermission("phantomeconomy.economy.remove")) {
                            if (args.length == 3) {
                                if (instance.provider.hasAccount(args[1])) {
                                    final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);

                                    double amount;

                                    try {
                                        amount = Double.parseDouble(args[2]);
                                    } catch (NumberFormatException exception) {
                                        sender.sendMessage(instance.colorize(instance.messages.get("common.invalid-number-double", "Invalid number - %arg% is not a valid number."))
                                                .replaceAll("%arg%", args[2]));
                                        return true;
                                    }

                                    if (amount < 0) {
                                        sender.sendMessage(instance.colorize(instance.messages.get("common.invalid-number-negative", "Invalid number - %number% is negative."))
                                                .replaceAll("%number%", args[2]));
                                        return true;
                                    } else if (amount == 0) {
                                        sender.sendMessage(instance.colorize(instance.messages.get("common.invalid-number-zero", "Invalid number - 0 is not allowed.")));
                                        return true;
                                    } else {
                                        if (instance.provider.has(offlinePlayer, amount)) {
                                            instance.provider.withdrawPlayer(offlinePlayer, amount);
                                            sender.sendMessage(instance.colorize(instance.messages.get("commands.economy.remove.success", "Withdrew %amount% from %player%'s balance."))
                                                    .replaceAll("%amount%", Matcher.quoteReplacement(instance.provider.format(amount)))
                                                    .replaceAll("%player%", args[1]));

                                            if (offlinePlayer.isOnline()) {
                                                Objects.requireNonNull(offlinePlayer.getPlayer()).sendMessage(instance.colorize(instance.messages.get("commands.economy.remove.by", "%amount% was withdrawn by %sender%."))
                                                        .replaceAll("%amount%", Matcher.quoteReplacement(instance.provider.format(amount)))
                                                        .replaceAll("%sender%", sender.getName()));
                                            }
                                        } else {
                                            sender.sendMessage(instance.colorize(instance.messages.get("commands.economy.remove.not-enough-funds", "%player% doesn't have a balance equal to or greater than %amount%.")).replaceAll("%player%", args[1]).replaceAll("%amount%", Matcher.quoteReplacement(instance.provider.format(amount))));
                                        }
                                        return true;
                                    }
                                } else {
                                    sender.sendMessage(instance.colorize(instance.messages.get("common.target-never-played-before", "%player% hasn't joined the server before.")).replaceAll("%player%", args[1]));
                                    return true;
                                }
                            } else {
                                sender.sendMessage(instance.colorize(instance.messages.get("commands.economy.remove.usage", "/economy remove <player> <amount>")));
                                return true;
                            }
                        } else {
                            sender.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "You don't have access to that.")));
                            return true;
                        }
                    case "set":
                        if (sender.hasPermission("phantomeconomy.economy.set")) {
                            if (args.length == 3) {
                                if (instance.provider.hasAccount(args[1])) {
                                    final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);

                                    double amount;

                                    try {
                                        amount = Double.parseDouble(args[2]);
                                    } catch (NumberFormatException exception) {
                                        sender.sendMessage(instance.colorize(instance.messages.get("common.invalid-number-double", "Invalid number - %arg% is not a valid number.")).replaceAll("%arg%", args[2]));
                                        return true;
                                    }

                                    if (amount < 0) {
                                        sender.sendMessage(instance.colorize(instance.messages.get("common.invalid-number-negative", "Invalid number - %number% is negative.")).replaceAll("%number%", args[2]));
                                    } else {
                                        instance.provider.withdrawPlayer(offlinePlayer, instance.provider.getBalance(offlinePlayer));
                                        instance.provider.depositPlayer(offlinePlayer, amount);

                                        if (offlinePlayer.isOnline()) {
                                            Objects.requireNonNull(offlinePlayer.getPlayer()).sendMessage(instance.colorize(instance.messages.get("commands.economy.set.by", "Your balance was set to %amount% by %sender%."))
                                                    .replaceAll("%amount%", Matcher.quoteReplacement(instance.provider.format(amount)))
                                                    .replaceAll("%sender%", sender.getName()));
                                        }

                                        sender.sendMessage(instance.colorize(instance.messages.get("commands.economy.set.success", "Set %player%'s balance to %amount%.")).replaceAll("%amount%", Matcher.quoteReplacement(instance.provider.format(amount))).replaceAll("%player%", args[1]));
                                    }
                                } else {
                                    sender.sendMessage(instance.colorize(instance.messages.get("common.target-never-played-before", "%player% hasn't joined the server before.")).replaceAll("%player%", args[1]));
                                }
                            } else {
                                sender.sendMessage(instance.colorize(instance.messages.get("commands.economy.set.usage", "/economy set <player> <amount>")));
                            }
                        } else {
                            sender.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "You don't have access to that.")));
                        }
                        return true;
                    case "reset":
                        if (sender.hasPermission("phantomeconomy.economy.reset")) {
                            if (args.length == 2) {
                                if (instance.provider.hasAccount(args[1])) {
                                    final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                                    instance.provider.withdrawPlayer(offlinePlayer, instance.provider.getBalance(offlinePlayer));
                                    instance.provider.depositPlayer(offlinePlayer, instance.getDefaultBalance());

                                    if (offlinePlayer.isOnline()) {
                                        Objects.requireNonNull(offlinePlayer.getPlayer()).sendMessage(instance.colorize(instance.messages.get("commands.economy.reset.by", "%sender% reset your balance."))
                                                .replaceAll("%sender%", sender.getName()));
                                    }

                                    sender.sendMessage(instance.colorize(instance.messages.get("commands.economy.reset.success", "Set %player%'s balance to the default balance.")).replaceAll("%player%", args[1]));
                                } else {
                                    sender.sendMessage(instance.colorize(instance.messages.get("common.target-never-played-before", "%player% hasn't joined the server before.")).replaceAll("%player%", args[1]));
                                }
                            } else {
                                sender.sendMessage(instance.colorize(instance.messages.get("commands.economy.reset.usage", "/economy reset <player>")));
                            }
                        } else {
                            sender.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "You don't have access to that.")));
                            return true;
                        }
                        return true;
                    default:
                        for (String msg : instance.messages.get("commands.economy.usage", Arrays.asList("/economy add <player> <amount>", "/economy remove <player> <amount>", "/economy set <player> <amount>", "/economy reset <player>"))) {
                            sender.sendMessage(instance.colorize(msg));
                        }
                        return true;
                }
            } else {
                for (String msg : instance.messages.get("commands.economy.usage", Arrays.asList("/economy add <player> <amount>", "/economy remove <player> <amount>", "/economy set <player> <amount>", "/economy reset <player>"))) {
                    sender.sendMessage(instance.colorize(msg));
                }
                return true;
            }
        } else {
            sender.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "You don't have access to that.")));
            return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("phantomeconomy.economy.add"))
                suggestions.add("add");
            if (sender.hasPermission("phantomeconomy.economy.remove"))
                suggestions.add("remove");
            if (sender.hasPermission("phantomeconomy.economy.set"))
                suggestions.add("set");
            if (sender.hasPermission("phantomeconomy.economy.reset"))
                suggestions.add("reset");
        }
        if (args.length == 2) {
            if (args[1] == null) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    suggestions.add(onlinePlayer.getName());
                }
            } else {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getName().startsWith(args[1])) {
                        suggestions.add(onlinePlayer.getName());
                    }
                }
            }
        }
        return suggestions;
    }
}
