package io.github.lokka30.phantomeconomy.listeners;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

@SuppressWarnings("unused")
public class SignPlaceListener implements Listener {

    // In the future, this will do: Converts formatting when a user, with permission, creates an admin shop.

    private PhantomEconomy instance;

    public SignPlaceListener(PhantomEconomy instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onSignPlace(final SignChangeEvent e) {
        /*
        if (!e.isCancelled()) {

        }
         */
    }
}
