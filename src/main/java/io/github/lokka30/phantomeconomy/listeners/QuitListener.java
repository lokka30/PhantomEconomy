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

    /**
     * This event checks when a player quits the server.
     *
     * @param e the PlayerQuitEvent.
     */
    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        final Player player = e.getPlayer();

        // Remove their balance from the balance cache, otherwise larger servers would have their memory taken up by storing balances of offline players.
        instance.balanceCache.remove(player);
    }
}
