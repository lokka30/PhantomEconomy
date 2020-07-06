package io.github.lokka30.phantomeconomy_v2.commands;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.InvalidCurrencyException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
                    // Get the sender's balance.
                    final Player player = (Player) sender;
                    try {
                        final double balance = instance.getAccountManager().getPlayerAccount(player).getBalance(instance.getEconomyManager().getDefaultCurrency());
                        // Message: "Your balance is %balance"
                    } catch (InvalidCurrencyException e) {
                        e.printStackTrace();
                        // Message: "Unable to retrieve your balance, please inform an administrator that the plugin is incorrectly configured.
                    }
                } else {
                    // Message: "Usage (console): /balance <currency> <player>"
                }
            } else if (args.length == 1) {
                if (sender instanceof Player) {
                    // Get the sender's balance for the specific currency.
                } else {
                    // Message: "Usage (console): /balance <currency> <player>"
                }
            } else if (args.length == 2) {
                if (sender.hasPermission("phantomeconomy.balance.others")) {
                    // Get the target's balance for the specified currency.
                } else {
                    // No permission.
                }
            } else {
                // Usage.
            }
        } else {
            // No Permission message.
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        //TODO
        return null;
    }
}
