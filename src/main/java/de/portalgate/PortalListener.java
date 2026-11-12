package de.portalgate;

import de.portalgate.PortalGate.Dimension;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;

public final class PortalListener implements Listener {

    private final PortalGate plugin;

    public PortalListener(PortalGate plugin) {
        this.plugin = plugin;
    }

    /** Ender-Augen lassen sich nicht in den Rahmen setzen, solange das End zu ist. */
    @EventHandler(ignoreCancelled = true)
    public void onEyePlace(PlayerInteractEvent event) {
        if (plugin.isOpen(Dimension.END)) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.END_PORTAL_FRAME) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.ENDER_EYE) {
            return;
        }
        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("Das End ist noch geschlossen.", NamedTextColor.RED));
    }

    /** Nether-Portal-Rahmen werden nicht aktiv, es bleibt nur das Feuer. */
    @EventHandler(ignoreCancelled = true)
    public void onPortalCreate(PortalCreateEvent event) {
        if (plugin.isOpen(Dimension.NETHER)) {
            return;
        }
        // Nur FIRE blockieren, damit niemand im Nether festsitzt, wenn er zurueck will.
        if (event.getReason() == PortalCreateEvent.CreateReason.FIRE) {
            event.setCancelled(true);
        }
    }

    /** Falls schon ein Portal existiert: Reisen in geschlossene Dimensionen blockieren. */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        World.Environment from = event.getFrom().getWorld().getEnvironment();
        switch (event.getCause()) {
            case NETHER_PORTAL -> {
                if (from != World.Environment.NETHER && !plugin.isOpen(Dimension.NETHER)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(
                            Component.text("Der Nether ist noch geschlossen.", NamedTextColor.RED));
                }
            }
            case END_PORTAL -> {
                if (from != World.Environment.THE_END && !plugin.isOpen(Dimension.END)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(
                            Component.text("Das End ist noch geschlossen.", NamedTextColor.RED));
                }
            }
            default -> {
            }
        }
    }

    /** Dasselbe fuer Tiere, Items und andere Entities. */
    @EventHandler(ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent event) {
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        World.Environment from = event.getFrom().getWorld().getEnvironment();
        World.Environment target = to.getWorld().getEnvironment();
        if (from == target) {
            return;
        }
        if (target == World.Environment.NETHER && !plugin.isOpen(Dimension.NETHER)) {
            event.setCancelled(true);
        } else if (target == World.Environment.THE_END && !plugin.isOpen(Dimension.END)) {
            event.setCancelled(true);
        }
    }
}
