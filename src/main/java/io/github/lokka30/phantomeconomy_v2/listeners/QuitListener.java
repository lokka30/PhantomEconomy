package io.github.lokka30.phantomeconomy_v2.listeners;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
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
        //TODO remove cached balance
    }
}