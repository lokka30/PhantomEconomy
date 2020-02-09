package io.github.lokka30.phantomeconomy.commands;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class CEconomy implements CommandExecutor {

    private PhantomEconomy instance = PhantomEconomy.getInstance();

    //ARGS GUIDE
    // args.length: /eco0 task1 player2 amount3
    // args[i]: /eco task0 player1 amount2
    @Override
    public boolean onCommand(final CommandSender cs, final Command cmd, final String label, final String[] args) {
        if (checkPermission(cs, "phantomeconomy.economy")) {
            if (args.length == 0) {
                for (String s : instance.messages.get("economy.help", Arrays.asList("&a&lPhantomEconomy: &7/eco <add/remove/set/reset> <target> [...]"))) {
                    cs.sendMessage(instance.colorize(s));
                }
                return true;
            } else {
                switch (args[0].toLowerCase()) {
                    case "add":
                        if (checkPermission(cs, "phantomeconomy.economy.add")) {
                            if (args.length == 3) {
                                final OfflinePlayer target = getPlayer(args[1]);

                                if (target == null) {
                                    cs.sendMessage(getMessage("common.target-offline", "&a&lPhantomEconomy: &r%target%&7 is offline.").replaceAll("%target%", args[1]));
                                } else {
                                    final UUID uuid = target.getUniqueId();
                                    final double amount;

                                    //Check the amount - is it a number?
                                    try {
                                        amount = Double.parseDouble(args[2]);
                                    } catch (NumberFormatException ex) {
                                        cs.sendMessage(getMessage("common.invalid-number", "&a&lPhantomEconomy: &7Invalid number &a%amount%&7.").replaceAll("%amount%", args[2]));
                                        return true;
                                    }

                                    //Check the amount - is it too low?
                                    if (amount <= 0) {
                                        cs.sendMessage(getMessage("economy.common.amount-greater-than-0", "&a&lPhantomEconomy: &7Invalid amount &a%amount%&7, it must be greater than &a0&7.").replaceAll("%amount%", "" + amount));
                                        return true;
                                    }

                                    instance.getEconomyManager().addBalance(uuid, amount);
                                    cs.sendMessage(getMessage("economy.add.success", "&a&lPhantomCombat: &7Added &a$%amount%&7 to &a%target%&7's balance.")
                                            .replaceAll("%target%", args[1])
                                            .replaceAll("%amount%", amount + ""));
                                }
                                return true;
                            } else {
                                cs.sendMessage(getMessage("economy.add.usage", "&a&lPhantomEconomy: &7Usage: /eco add <uuid> <amount>"));
                                return true;
                            }
                        }
                    case "remove":
                        if (checkPermission(cs, "phantomeconomy.economy.remove")) {
                            if (args.length == 3) {
                                final OfflinePlayer target = getPlayer(args[1]);

                                if (target == null) {
                                    cs.sendMessage(getMessage("common.target-offline", "&a&lPhantomEconomy: &r%target%&7 is offline.")
                                            .replaceAll("%target%", args[1]));
                                } else {
                                    final UUID uuid = target.getUniqueId();
                                    final double amount;

                                    //Check the amount - is it a number?
                                    try {
                                        amount = Double.parseDouble(args[2]);
                                    } catch (NumberFormatException ex) {
                                        cs.sendMessage(getMessage("common.invalid-number", "&a&lPhantomEconomy: &7Invalid number &a%amount%&7.")
                                                .replaceAll("%amount%", args[2]));
                                        return true;
                                    }

                                    //Check the amount - is it too low?
                                    if (amount <= 0) {
                                        cs.sendMessage(getMessage("economy.common.amount-greater-than-0", "&a&lPhantomEconomy: &7Invalid amount &a%amount%&7, it must be greater than &a0&7.")
                                                .replaceAll("%amount%", "" + amount));
                                        return true;
                                    }

                                    instance.getEconomyManager().removeBalance(uuid, amount);
                                    cs.sendMessage(getMessage("economy.remove.success", "&a&lPhantomCombat: &7Removed &a$%amount%&7 from &a%target%&7's balance.")
                                            .replaceAll("%target%", args[1])
                                            .replaceAll("%amount%", amount + ""));
                                }
                            } else {
                                cs.sendMessage(getMessage("economy.add.usage", "&a&lPhantomEconomy: &7Usage: /eco add <uuid> <amount>"));
                            }
                        }
                        return true;
                    case "set":
                        cs.sendMessage("todo set");
                        return true;
                    case "reset":
                        cs.sendMessage("todo reset");
                        return true;
                    default:
                        for (String s : instance.messages.get("economy.help", Collections.singletonList("&a&lPhantomEconomy: &7/eco <add/remove/set/reset> [...]"))) {
                            cs.sendMessage(instance.colorize(s));
                        }
                        return true;
                }
            }
        }
        return true;
    }

    private OfflinePlayer getPlayer(final String name) {
        final OfflinePlayer target;
        final Player player = Bukkit.getPlayer(name);
        if (player == null) {
            if (name.length() == 36) {
                target = Bukkit.getOfflinePlayer(UUID.fromString(name));
            } else {
                return null;
            }
        } else {
            target = player;
        }
        return target;
    }

    private String getMessage(final String path, final String def) {
        return instance.colorize(instance.messages.get(path, def));
    }

    private boolean checkPermission(final CommandSender cs, final String permission) {
        if (cs instanceof Player && !cs.hasPermission(permission)) {
            cs.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "&a&lPhantomEconomy: &7You don't have access to that.")));
            return false;
        }
        return true;
    }
}
