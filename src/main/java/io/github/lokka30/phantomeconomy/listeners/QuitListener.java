package io.github.lokka30.phantomeconomy.listeners;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    private PhantomEconomy instance;

    public QuitListener(final PhantomEconomy instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        instance.getAccountManager().cachedPlayerAccountBalances.remove(event.getPlayer().getUniqueId());
    }
}