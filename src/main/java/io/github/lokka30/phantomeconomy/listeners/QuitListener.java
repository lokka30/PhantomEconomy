package io.github.lokka30.phantomeconomy.listeners;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    private PhantomEconomy instance;

    public QuitListener(PhantomEconomy instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        final Player player = e.getPlayer();
        instance.balanceCache.remove(player);
    }
}
