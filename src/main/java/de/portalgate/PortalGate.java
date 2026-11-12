package de.portalgate;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class PortalGate extends JavaPlugin {

    public enum Dimension {
        NETHER("Der Nether"),
        END("Das End");

        private final String displayName;

        Dimension(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    private boolean endOpen;
    private boolean netherOpen;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        endOpen = getConfig().getBoolean("end-open", false);
        netherOpen = getConfig().getBoolean("nether-open", false);

        getServer().getPluginManager().registerEvents(new PortalListener(this), this);

        PortalCommand handler = new PortalCommand(this);
        for (String name : new String[] {"end", "open", "close"}) {
            PluginCommand command = getCommand(name);
            if (command != null) {
                command.setExecutor(handler);
                command.setTabCompleter(handler);
            }
        }
    }

    public boolean isOpen(Dimension dimension) {
        return dimension == Dimension.END ? endOpen : netherOpen;
    }

    public void setOpen(Dimension dimension, boolean open) {
        if (dimension == Dimension.END) {
            endOpen = open;
            getConfig().set("end-open", open);
        } else {
            netherOpen = open;
            getConfig().set("nether-open", open);
        }
        saveConfig();

        Component message = Component.text(
                dimension.displayName() + (open ? " wurde geoeffnet!" : " wurde geschlossen."),
                open ? NamedTextColor.GREEN : NamedTextColor.RED);
        Bukkit.broadcast(message);
    }
}
