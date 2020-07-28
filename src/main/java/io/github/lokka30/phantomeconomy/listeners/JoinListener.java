package io.github.lokka30.phantomeconomy.listeners;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import io.github.lokka30.phantomeconomy.api.exceptions.AccountAlreadyExistsException;
import io.github.lokka30.phantomeconomy.api.exceptions.InvalidCurrencyException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private PhantomEconomy instance;

    public JoinListener(final PhantomEconomy instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) throws InvalidCurrencyException {
        final Player player = event.getPlayer();
        if (player.isOp()) {
            player.sendMessage("§8§m+---");
            player.sendMessage("§b§l§nPhantomEconomy Pre-Release Warning:");
            player.sendMessage(" ");
            player.sendMessage("§7PhantomEconomy v2.0.0-PRE-RELEASE is §c§nhighly unstable§7, it is only meant to be used on test servers.");
            player.sendMessage("§cThis pre-release version will most likely contain §nmajor issues§c.");
            player.sendMessage("§7The server owner uses this plugin §cat their own risk§7 (damages will not be compensated for by the plugin authors).");
            player.sendMessage(" ");
            player.sendMessage("§7§oThis message is only displayed to operators.");
            player.sendMessage("§8§m+---");
        }
        instance.getDatabase().assignUsernameToUUID(player.getUniqueId(), player.getName());
        if (!instance.getAccountManager().hasPlayerAccount(player.getUniqueId(), instance.getCurrencyManager().getDefaultCurrency())) {
            try {
                instance.getAccountManager().createPlayerAccount(player.getUniqueId(), instance.getCurrencyManager().getDefaultCurrency());
            } catch (AccountAlreadyExistsException exception) {
                exception.printStackTrace();
            }
        }
    }
}
