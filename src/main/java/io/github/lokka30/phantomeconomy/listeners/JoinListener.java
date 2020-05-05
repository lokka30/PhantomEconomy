package io.github.lokka30.phantomeconomy.listeners;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private PhantomEconomy instance;

    public JoinListener(PhantomEconomy instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        final Player player = e.getPlayer();

        if (!instance.provider.hasAccount(player)) {
            instance.provider.createPlayerAccount(player);
        }

        if (!instance.balanceCache.containsKey(player)) {
            instance.balanceCache.put(player, instance.data.get("players." + player.getUniqueId().toString() + ".balance", instance.getDefaultBalance()));
        }
    }
}
