package io.github.lokka30.phantomeconomy.listeners;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

@SuppressWarnings("unused")
public class SignUseListener implements Listener {

    private PhantomEconomy instance;

    public SignUseListener(PhantomEconomy instance) {
        this.instance = instance;
    }

    // In the future, this will do the following: Checks if players are interacting with admin shop signs. Also checks for the balance sign.

    @EventHandler
    public void onSignInteract(final PlayerInteractEvent e) {
        /*
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block clickedBlock = e.getClickedBlock();

            if(clickedBlock instanceof Sign) {
                Sign sign = (Sign) clickedBlock.getState();
                if(sign.getLine(0).equalsIgnoreCase("[Balance]")) {
                    //...
                }
            }
        }
         */
    }
}
