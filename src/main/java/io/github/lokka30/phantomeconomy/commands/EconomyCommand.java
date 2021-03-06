package io.github.lokka30.phantomeconomy.commands;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import io.github.lokka30.phantomeconomy.api.accounts.PlayerAccount;
import io.github.lokka30.phantomeconomy.api.currencies.Currency;
import io.github.lokka30.phantomeconomy.api.exceptions.InvalidCurrencyException;
import io.github.lokka30.phantomeconomy.api.exceptions.NegativeAmountException;
import io.github.lokka30.phantomeconomy.api.exceptions.OversizedWithdrawAmountException;
import io.github.lokka30.phantomlib.enums.LogLevel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

public class EconomyCommand implements CommandExecutor {

    private PhantomEconomy instance;

    public EconomyCommand(final PhantomEconomy instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("phantomeconomy.economy")) {
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "add":
                    case "give":
                    case "deposit":
                        if (sender.hasPermission("phantomeconomy.economy.add")) {
                            if (args.length == 3 || args.length == 4) {
                                Currency currency;
                                if (args.length == 4) {
                                    try {
                                        currency = instance.getCurrencyManager().getCurrency(args[3]);
                                    } catch (InvalidCurrencyException exception) {
                                        sender.sendMessage("Invalid currency %currency%".replace("%currency%", args[3]));
                                        return true;
                                    }
                                } else {
                                    try {
                                        currency = instance.getCurrencyManager().getDefaultCurrency();
                                    } catch (InvalidCurrencyException e) {
                                        sender.sendMessage("PhantomEconomy is incorrectly configured: Unable to access default currency.");
                                        if (sender.isOp()) {
                                            sender.sendMessage("A stack trace has been printed in your server's console.");
                                        } else {
                                            sender.sendMessage("Please inform a server administrator about this issue as soon as possible.");
                                        }
                                        instance.getPhantomLogger().log(LogLevel.SEVERE, instance.PREFIX, "PhantomEconomy is incorrectly configured: unable to access default currency. Stack trace:");
                                        e.printStackTrace();
                                        return true;
                                    }
                                }

                                ArrayList<UUID> playersToActUpon = new ArrayList<>();
                                if (args[1].equals("*")) {
                                    if (sender instanceof Player) {
                                        Player player = (Player) sender;
                                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                            if (player.canSee(onlinePlayer)) {
                                                playersToActUpon.add(onlinePlayer.getUniqueId());
                                            }
                                        }
                                    } else {
                                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                            playersToActUpon.add(onlinePlayer.getUniqueId());
                                        }
                                    }
                                } else {
                                    if (instance.getDatabase().isUsernameCached(args[1])) {
                                        playersToActUpon.add(instance.getDatabase().getUUIDFromUsername(args[1]));
                                    } else {
                                        sender.sendMessage("A player by the name of '%player%' isn't available in the database.".replace("%player%", args[1]));
                                        return true;
                                    }
                                }

                                double amount;
                                try {
                                    amount = Double.parseDouble(args[2]);
                                } catch (NumberFormatException exception) {
                                    sender.sendMessage("Invalid amount '%amount%', was expecting a positive number."
                                            .replace("%amount%", args[2]));
                                    return true;
                                }

                                if (amount <= 0) {
                                    sender.sendMessage("Invalid amount '%amount%', was expecting a positive number."
                                            .replace("%amount%", args[2]));
                                    return true;
                                }

                                int changes = 0;
                                for (UUID uuid : playersToActUpon) {
                                    changes++;
                                    try {
                                        instance.getAccountManager().getPlayerAccount(uuid).deposit(currency, amount);
                                    } catch (NegativeAmountException e) {
                                        sender.sendMessage("An internal error ocurred whilst attempting to perform this command.");
                                        if (sender.isOp()) {
                                            sender.sendMessage("A stack trace has been printed in your server's console.");
                                        } else {
                                            sender.sendMessage("Please inform a server administrator about this issue as soon as possible.");
                                        }
                                        instance.getPhantomLogger().log(LogLevel.SEVERE, instance.PREFIX, "Internal error occured whilst attempting to run 'economy add' command: specified number is negative and has bypassed check meant to prevent this situation. Please report this issue to the plugin support team.");
                                        return true;
                                    }
                                }

                                sender.sendMessage("Added %amount% to balances on %changes% account/s with currency '%currency%'."
                                        .replace("%amount%", currency.formatFinalBalance(amount))
                                        .replace("%changes%", changes + "")
                                        .replace("%currency%", currency.getName()));
                            } else {
                                sender.sendMessage("Usage: /%label% %action% <player/*> <amount> [currency]"
                                        .replace("%label%", label.toLowerCase())
                                        .replace("%action%", args[0].toLowerCase()));
                            }
                        } else {
                            sender.sendMessage("No permission");
                        }
                        break;
                    case "remove":
                    case "take":
                    case "withdraw":
                        if (sender.hasPermission("phantomeconomy.economy.remove")) {
                            if (args.length == 3 || args.length == 4) {
                                Currency currency;
                                if (args.length == 4) {
                                    try {
                                        currency = instance.getCurrencyManager().getCurrency(args[3]);
                                    } catch (InvalidCurrencyException exception) {
                                        sender.sendMessage("Invalid currency %currency%".replace("%currency%", args[3]));
                                        return true;
                                    }
                                } else {
                                    try {
                                        currency = instance.getCurrencyManager().getDefaultCurrency();
                                    } catch (InvalidCurrencyException e) {
                                        sender.sendMessage("PhantomEconomy is incorrectly configured: Unable to access default currency.");
                                        if (sender.isOp()) {
                                            sender.sendMessage("A stack trace has been printed in your server's console.");
                                        } else {
                                            sender.sendMessage("Please inform a server administrator about this issue as soon as possible.");
                                        }
                                        instance.getPhantomLogger().log(LogLevel.SEVERE, instance.PREFIX, "PhantomEconomy is incorrectly configured: unable to access default currency. Stack trace:");
                                        e.printStackTrace();
                                        return true;
                                    }
                                }

                                ArrayList<UUID> playersToActUpon = new ArrayList<>();
                                if (args[1].equals("*")) {
                                    if (sender instanceof Player) {
                                        Player player = (Player) sender;
                                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                            if (player.canSee(onlinePlayer)) {
                                                playersToActUpon.add(onlinePlayer.getUniqueId());
                                            }
                                        }
                                    } else {
                                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                            playersToActUpon.add(onlinePlayer.getUniqueId());
                                        }
                                    }
                                } else {
                                    if (!instance.getDatabase().isUsernameCached(args[1])) {
                                        sender.sendMessage("A player by the name of '%player%' isn't available in the database.".replace("%player%", args[1]));
                                        return true;
                                    }
                                }

                                double amount;
                                try {
                                    amount = Double.parseDouble(args[2]);
                                } catch (NumberFormatException exception) {
                                    sender.sendMessage("Invalid amount '%amount%', was expecting a positive number."
                                            .replace("%amount%", args[2]));
                                    return true;
                                }

                                if (amount <= 0) {
                                    sender.sendMessage("Invalid amount '%amount%', was expecting a positive number."
                                            .replace("%amount%", args[2]));
                                    return true;
                                }

                                int successfulChanges = 0;
                                int unsuccessfulChangesFromOversizedWithdrawal = 0;
                                for (UUID uuid : playersToActUpon) {
                                    try {
                                        final PlayerAccount playerAccount = instance.getAccountManager().getPlayerAccount(uuid);
                                        if (playerAccount.has(currency, amount)) {
                                            playerAccount.withdraw(currency, amount);
                                            successfulChanges++;
                                        } else {
                                            unsuccessfulChangesFromOversizedWithdrawal++;
                                        }
                                    } catch (NegativeAmountException | OversizedWithdrawAmountException e) {
                                        sender.sendMessage("An internal error ocurred whilst attempting to perform this command.");
                                        if (sender.isOp()) {
                                            sender.sendMessage("A stack trace has been printed into the server's console.");
                                        } else {
                                            sender.sendMessage("Please inform a server administrator about this issue as soon as possible.");
                                        }
                                        e.printStackTrace();
                                        return true;
                                    }
                                }

                                if (successfulChanges == 0) {
                                    sender.sendMessage("No balances were modified in this operation.");
                                } else {
                                    sender.sendMessage("Removed %amount% from balances on %changes% account/s with currency '%currency%'."
                                            .replace("%amount%", currency.formatFinalBalance(amount))
                                            .replace("%changes%", successfulChanges + "")
                                            .replace("%currency%", currency.getName()));
                                }

                                if (unsuccessfulChangesFromOversizedWithdrawal != 0) {
                                    sender.sendMessage("(Skipped %changes% account/s as they didn't have enough funds for the operation)"
                                            .replace("%changes%", unsuccessfulChangesFromOversizedWithdrawal + ""));
                                }
                            } else {
                                sender.sendMessage("Usage: /%label% %action% <player/*> <amount> [currency]"
                                        .replace("%label%", label.toLowerCase())
                                        .replace("%action%", args[0].toLowerCase()));
                            }
                        } else {
                            sender.sendMessage("No permission");
                        }
                        break;
                    case "set":
                        if (sender.hasPermission("phantomeconomy.economy.set")) {
                            if (args.length == 3 || args.length == 4) {
                                Currency currency;
                                if (args.length == 4) {
                                    try {
                                        currency = instance.getCurrencyManager().getCurrency(args[3]);
                                    } catch (InvalidCurrencyException exception) {
                                        sender.sendMessage("Invalid currency '%currency%'.".replace("%currency%", args[3]));
                                        return true;
                                    }
                                } else {
                                    try {
                                        currency = instance.getCurrencyManager().getDefaultCurrency();
                                    } catch (InvalidCurrencyException e) {
                                        sender.sendMessage("PhantomEconomy is incorrectly configured: Unable to access default currency.");
                                        if (sender.isOp()) {
                                            sender.sendMessage("Error information has been printed in the server's console.");
                                        } else {
                                            sender.sendMessage("Please inform a server administrator about this issue as soon as possible. You may want to inform them with the exact command you ran.");
                                        }
                                        instance.getPhantomLogger().log(LogLevel.SEVERE, instance.PREFIX, "PhantomEconomy is incorrectly configured: unable to access default currency. Stack trace:");
                                        e.printStackTrace();
                                        return true;
                                    }
                                }

                                ArrayList<UUID> playersToActUpon = new ArrayList<>();
                                if (args[1].equals("*")) {
                                    if (sender instanceof Player) {
                                        Player player = (Player) sender;
                                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                            if (player.canSee(onlinePlayer)) {
                                                playersToActUpon.add(onlinePlayer.getUniqueId());
                                            }
                                        }
                                    } else {
                                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                            playersToActUpon.add(onlinePlayer.getUniqueId());
                                        }
                                    }
                                } else {
                                    if (instance.getDatabase().isUsernameCached(args[1])) {
                                        playersToActUpon.add(instance.getDatabase().getUUIDFromUsername(args[1]));
                                    } else {
                                        sender.sendMessage("A player by the name of '%player%' isn't available in the database.");
                                        return true;
                                    }
                                }

                                double amount;
                                try {
                                    amount = Double.parseDouble(args[2]);
                                } catch (NumberFormatException exception) {
                                    sender.sendMessage("Invalid amount '%amount%', was expecting a positive number."
                                            .replace("%amount%", args[2]));
                                    return true;
                                }

                                if (amount <= 0) {
                                    sender.sendMessage("Invalid amount '%amount%', was expecting a positive number."
                                            .replace("%amount%", args[2]));
                                    return true;
                                }

                                int changes = 0;
                                for (UUID uuid : playersToActUpon) {
                                    changes++;
                                    try {
                                        instance.getAccountManager().getPlayerAccount(uuid).setBalance(currency, amount);
                                    } catch (NegativeAmountException e) {
                                        sender.sendMessage("An internal error ocurred whilst attempting to perform this command.");
                                        if (sender.isOp()) {
                                            sender.sendMessage("Error information has been printed to the server's console.");
                                        } else {
                                            sender.sendMessage("Please inform a server administrator about this issue as soon as possible. You may want to inform them with the exact command you ran.");
                                        }
                                        instance.getPhantomLogger().log(LogLevel.SEVERE, instance.PREFIX, "Internal error occured whilst attempting to run 'economy set' command: specified number is negative and has bypassed check meant to prevent this situation. Please report this issue to the plugin support team.");
                                        return true;
                                    }
                                }

                                sender.sendMessage("Set balances on %changes% account/s with currency '%currency%' to %amount%."
                                        .replace("%amount%", currency.formatFinalBalance(amount))
                                        .replace("%changes%", changes + "")
                                        .replace("%currency%", currency.getName()));
                            } else {
                                sender.sendMessage("Usage: /%label% %action% <player/*> <amount> [currency]"
                                        .replace("%label%", label.toLowerCase())
                                        .replace("%action%", args[0].toLowerCase()));
                            }
                        } else {
                            sender.sendMessage("No permission");
                        }
                        break;
                    case "reset":
                        if (sender.hasPermission("phantomeconomy.economy.remove")) {
                            if (args.length == 2 || args.length == 3) {
                                Currency currency;
                                if (args.length == 3) {
                                    try {
                                        currency = instance.getCurrencyManager().getCurrency(args[2]);
                                    } catch (InvalidCurrencyException exception) {
                                        sender.sendMessage("Invalid currency %currency%".replace("%currency%", args[2]));
                                        return true;
                                    }
                                } else {
                                    try {
                                        currency = instance.getCurrencyManager().getDefaultCurrency();
                                    } catch (InvalidCurrencyException e) {
                                        sender.sendMessage("PhantomEconomy is incorrectly configured: Unable to access default currency.");
                                        if (sender.isOp()) {
                                            sender.sendMessage("A stack trace has been printed in your server's console.");
                                        } else {
                                            sender.sendMessage("Please inform a server administrator about this issue as soon as possible. You may want to inform them with the exact command you ran.");
                                        }
                                        instance.getPhantomLogger().log(LogLevel.SEVERE, instance.PREFIX, "PhantomEconomy is incorrectly configured: unable to access default currency. Stack trace:");
                                        e.printStackTrace();
                                        return true;
                                    }
                                }

                                ArrayList<UUID> playersToActUpon = new ArrayList<>();
                                if (args[1].equals("*")) {
                                    if (sender instanceof Player) {
                                        Player player = (Player) sender;
                                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                            if (player.canSee(onlinePlayer)) {
                                                playersToActUpon.add(onlinePlayer.getUniqueId());
                                            }
                                        }
                                    } else {
                                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                            playersToActUpon.add(onlinePlayer.getUniqueId());
                                        }
                                    }
                                } else {
                                    if (instance.getDatabase().isUsernameCached(args[1])) {
                                        playersToActUpon.add(instance.getDatabase().getUUIDFromUsername(args[1]));
                                    } else {
                                        sender.sendMessage("A player by the name of '%player%' isn't available in the database.".replace("%player%", args[1]));
                                        return true;
                                    }
                                }

                                int changes = 0;
                                for (UUID uuid : playersToActUpon) {
                                    changes++;
                                    try {
                                        instance.getAccountManager().getPlayerAccount(uuid).setBalance(currency, currency.getDefaultBalance());
                                    } catch (NegativeAmountException e) {
                                        sender.sendMessage("An internal error ocurred whilst attempting to perform this command.");
                                        if (sender.isOp()) {
                                            sender.sendMessage("Error information has been provided in the server's console.");
                                        } else {
                                            sender.sendMessage("Please inform a server administrator about this issue as soon as possible.");
                                        }
                                        instance.getPhantomLogger().log(LogLevel.SEVERE, instance.PREFIX, "Internal error occured whilst attempting to run 'economy reset' command: Default balance for currency '" + currency.getName() + "' is negative. Please adjust your PhantomEconomy configuration accordingly to fix this issue.");
                                        return true;
                                    }
                                }

                                sender.sendMessage("Reset balances on %changes% account/s with currency '%currency%'."
                                        .replace("%changes%", changes + "")
                                        .replace("%currency%", currency.getName()));
                            } else {
                                sender.sendMessage("Usage: /%label% %action% <player/*> [currency]"
                                        .replace("%label%", label.toLowerCase())
                                        .replace("%action%", args[0].toLowerCase()));
                            }
                        } else {
                            sender.sendMessage("No permission");
                        }
                        break;
                    default:
                        sender.sendMessage("Usage: /%label% <add/remove/set/reset> <player/*>"
                                .replace("%label%", label.toLowerCase()));
                        break;
                }
            } else {
                sender.sendMessage("Usage: /%label% <add/remove/set/reset> <player/*>"
                        .replace("%label%", label.toLowerCase()));
            }
        } else {
            sender.sendMessage("No permission");
        }
        return true;
    }
}
