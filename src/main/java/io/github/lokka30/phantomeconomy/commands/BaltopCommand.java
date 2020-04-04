package io.github.lokka30.phantomeconomy.commands;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;

public class BaltopCommand implements CommandExecutor {

    private PhantomEconomy instance;

    public BaltopCommand(final PhantomEconomy instance) {
        this.instance = instance;
    }

    /*
    Credit: Big thanks to Dkbay for providing the baltop code.
     */

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command cmd, @NotNull final String label, @NotNull final String[] args) {
        final Map<String, Double> baltop = instance.baltopUpdater.getBaltop();

        List<String> keyList = new ArrayList<>(baltop.keySet());
        List<Double> valueList = new ArrayList<>(baltop.values());

        int length;

        if (args.length == 0) {
            length = 10;
        } else if (args.length == 1) {
            try {
                length = Integer.parseInt(args[0]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(instance.colorize(instance.messages.get("common.invalid-number-integer", "Invalid number - %number% is not an integer.")).replaceAll("%number%", args[0]));
                return true;
            }

            if (length >= 1) {
                length = length * 10;
            } else {
                length = 10;
            }
        } else {
            sender.sendMessage(instance.colorize(instance.messages.get("commands.baltop.usage", "Usage: /baltop [position]")));
            return true;
        }

        sender.sendMessage(instance.colorize(instance.messages.get("commands.baltop.header", "+----------+ Baltop +----------+")));
        for (int i = length - 10; i < length; i++) {
            if (baltop.size() <= i) {
                if (!(i > length - 10)) {
                    sender.sendMessage(instance.colorize(instance.messages.get("commands.baltop.no-players-left", "There are no players left to display.")));
                }
                break;
            }
            int counter = i + 1;
            UUID uuid = UUID.fromString(keyList.get(i));
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            BigDecimal balance = BigDecimal.valueOf(valueList.get(i)).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();

            sender.sendMessage(instance.colorize(instance.messages.get("commands.baltop.counter", "#%position% - %player% - %amount%"))
                    .replaceAll("%position%", Integer.toString(counter))
                    .replaceAll("%player%", Objects.requireNonNull(offlinePlayer.getName()))
                    .replaceAll("%amount%", Matcher.quoteReplacement(instance.provider.format(balance.doubleValue()))));
        }
        return true;
    }
}
