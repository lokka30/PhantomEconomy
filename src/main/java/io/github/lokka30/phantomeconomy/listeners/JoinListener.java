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
            player.sendMessage(" ");
            player.sendMessage("§4§l--- PHANTOMECONOMY WARNING ---");
            player.sendMessage("§cYou are using a §nhighly unstable§c build of PhantomEconomy, v2.0.0 PRE-RELEASE.");
            player.sendMessage("§cTHE PLUGIN WILL MOST LIKELY HAVE MINOR AND MAJOR ISSUES.");
            player.sendMessage("§7YOU RUN THIS VERSION AT YOUR OWN RISK.");
            player.sendMessage("§7Thanks.  §b§o~ lokka30");
            player.sendMessage(" ");
        }
        if (!instance.getAccountManager().hasPlayerAccount(player)) {
            try {
                instance.getAccountManager().createPlayerAccount(player);
            } catch (AccountAlreadyExistsException exception) {
                exception.printStackTrace();
            }
        }
    }
}
