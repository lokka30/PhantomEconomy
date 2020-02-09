package io.github.lokka30.phantomeconomy.commands;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CBalance implements CommandExecutor {

    private PhantomEconomy instance = PhantomEconomy.getInstance();

    @Override
    public boolean onCommand(final CommandSender cs, final Command cmd, final String label, final String[] args) {
        if (cs instanceof Player && !cs.hasPermission("phantomeconomy.balance")) {
            cs.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "&a&lPhantomEconomy: &7You don't have access to that.")));
        } else {
            if (args.length == 0) {
                if (cs instanceof Player) {
                    cs.sendMessage(instance.colorize(instance.messages.get("balance.your-balance", "&a&lPhantomEconomy: &7Your balance is &a$%amount%&7.")
                            .replaceAll("%amount%", "" + instance.getEconomyManager().getBalance(((Player) cs).getUniqueId()))));
                } else {
                    cs.sendMessage(instance.colorize(instance.messages.get("balance.usage-console", "&a&lPhantomEconomy: &7Usage (console): &2/balance <target/uuid>")));
                }
                return true;
            } else if (args.length == 1) {
                final OfflinePlayer target = getPlayer(args[0]);
                if (target == null) {
                    cs.sendMessage(instance.colorize(instance.messages.get("common.target-offline", "&a&lPhantomEconomy: &r%target%&7 is offline.")
                            .replaceAll("%target%", args[0])));
                } else {
                    cs.sendMessage(instance.colorize(instance.messages.get("balance.targets-balance", "&a&lPhantomEconomy: &r%target%&7's balance is &a$%amount%&7.")
                            .replaceAll("%amount%", "" + instance.getEconomyManager().getBalance(target.getUniqueId())))
                            .replaceAll("%target%", args[0]));
                }
                return true;
            } else {
                cs.sendMessage(instance.colorize(instance.messages.get("balance.usage", "&a&lPhantomEconomy: &7Usage: &2/balance [player]")));
                return true;
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
}
