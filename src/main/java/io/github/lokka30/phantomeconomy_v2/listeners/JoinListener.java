package io.github.lokka30.phantomeconomy_v2.listeners;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import io.github.lokka30.phantomeconomy_v2.api.accounts.PlayerAccount;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.AccountAlreadyExistsException;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.InvalidCurrencyException;
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
        final PlayerAccount playerAccount = instance.accountManager.getPlayerAccount(player);

        if (!instance.accountManager.hasPlayerAccount(player)) {
            try {
                instance.accountManager.createPlayerAccount(player);
            } catch (AccountAlreadyExistsException exception) {
                exception.printStackTrace();
            }
        }

        //TODO cache player balance from database
    }
}
