package io.github.lokka30.phantomeconomy.listeners;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class SignUseListener implements Listener {

    private PhantomEconomy instance;

    public SignUseListener(PhantomEconomy instance) {
        this.instance = instance;
    }

    //TODO - Checks if players are interacting with admin shop signs. Also checks for the balance sign.

    @EventHandler
    public void onSignInteract(final PlayerInteractEvent e) {
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            switch (Objects.requireNonNull(e.getClickedBlock()).getType()) {
                case ACACIA_SIGN:
                case ACACIA_WALL_SIGN:
                case OAK_SIGN:
                case OAK_WALL_SIGN:
                case DARK_OAK_SIGN:
                case DARK_OAK_WALL_SIGN:
                case SPRUCE_SIGN:
                case SPRUCE_WALL_SIGN:
                case BIRCH_SIGN:
                case BIRCH_WALL_SIGN:
                case JUNGLE_SIGN:
                case JUNGLE_WALL_SIGN:
                    //...
                    return;
            }
        }
    }
}
