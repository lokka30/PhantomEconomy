package io.github.lokka30.phantomeconomy.commands;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import io.github.lokka30.phantomeconomy.utils.Utils;
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
import java.util.regex.Matcher;

public class PayCommand implements TabExecutor {

    private PhantomEconomy instance;

    public PayCommand(final PhantomEconomy instance) {
        this.instance = instance;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command cmd, @NotNull final String label, @NotNull final String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            if (player.hasPermission("phantomeconomy.pay")) {
                if (args.length == 2) {
                    if (instance.provider.hasAccount(args[0])) {
                        final OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

                        if (target.getUniqueId().equals(player.getUniqueId())) {
                            player.sendMessage(instance.colorize(instance.messages.get("commands.pay.pay-self", "You can't transfer funds to yourself, silly. :)")));
                            return true;
                        }

                        double amount;

                        try {
                            amount = Double.parseDouble(args[1]);
                        } catch (NumberFormatException exception) {
                            player.sendMessage(instance.colorize(instance.messages.get("invalid-number-double", "Invalid number - %arg% is not a valid number.")).replaceAll("%arg%", args[1]));
                            return true;
                        }

                        amount = Utils.round(amount);

                        if (amount < 0) {
                            player.sendMessage(instance.colorize(instance.messages.get("common.invalid-number-negative", "Invalid number - %number% was specified, but negative values aren't allowed.")).replaceAll("%number%", Double.toString(amount)));
                        } else if (amount == 0) {
                            player.sendMessage(instance.colorize(instance.messages.get("common.invalid-number-zero", "Invalid number - 0.00 was specified, but the value is not allowed.")));
                        } else {
                            if (instance.provider.has(player, amount)) {
                                instance.provider.withdrawPlayer(player, amount);
                                instance.provider.depositPlayer(target, amount);

                                player.sendMessage(instance.colorize(instance.messages.get("commands.pay.success", "Sent %amount% to %player%.")).replaceAll("%amount%", Matcher.quoteReplacement(instance.provider.format(amount))).replaceAll("%player%", args[0]));

                                if (target.isOnline()) {
                                    Objects.requireNonNull(target.getPlayer()).sendMessage(instance.colorize(instance.messages.get("commands.pay.received", "Received %amount% from %player%.")).replaceAll("%amount%", Matcher.quoteReplacement(instance.provider.format(amount))).replaceAll("%player%", player.getName()));
                                }
                            } else {
                                player.sendMessage(instance.colorize(instance.messages.get("commands.pay.lacking-funds", "You lack the funds to make this transaction.")));
                            }
                        }
                    } else {
                        player.sendMessage(instance.colorize(instance.messages.get("common.target-played-before", "%player% hasn't joined the server before.")).replaceAll("%player%", args[0]));
                    }
                } else {
                    player.sendMessage(instance.colorize(instance.messages.get("commands.pay.usage", "Usage: /pay <player> <amount>")));
                }
            } else {
                player.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "You don't have access to that.")));
            }
        } else {
            sender.sendMessage(instance.colorize(instance.messages.get("common.players-only", "Only players may use this command.")));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            Player[] players = new Player[Bukkit.getOnlinePlayers().size()];
            players = Bukkit.getOnlinePlayers().toArray(players);
            for (Player player : players) {
                suggestions.add(player.getName());
            }
        }
        return suggestions;
    }
}
