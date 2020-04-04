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
        final String balancePath = "players." + player.getUniqueId().toString() + ",balance";
        if (instance.data.get(balancePath, null) == null) {
            if (instance.settings.get("default-balance.enabled", true)) {
                instance.data.set(balancePath, instance.settings.get("default-balance.amount", 50.0));
            } else {
                instance.data.set(balancePath, 0.0);
            }

        }

        instance.balanceCache.put(player, instance.data.get(balancePath, 0.0));
    }
}
