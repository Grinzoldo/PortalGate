package de.portalgate;

import de.portalgate.PortalGate.Dimension;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;
import java.util.Locale;

public final class PortalCommand implements TabExecutor {

    private final PortalGate plugin;

    public PortalCommand(PortalGate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("portalgate.admin")) {
            send(sender, "Dafuer hast du keine Berechtigung.", NamedTextColor.RED);
            return true;
        }

        String name = command.getName().toLowerCase(Locale.ROOT);
        String arg = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "";

        switch (name) {
            case "end" -> {
                switch (arg) {
                    case "open" -> change(sender, Dimension.END, true);
                    case "close" -> change(sender, Dimension.END, false);
                    case "status" -> status(sender);
                    default -> send(sender, "Benutzung: /end <open|close|status>", NamedTextColor.YELLOW);
                }
            }
            case "open", "close" -> {
                boolean open = name.equals("open");
                Dimension dimension = switch (arg) {
                    case "nether" -> Dimension.NETHER;
                    case "end" -> Dimension.END;
                    default -> null;
                };
                if (dimension == null) {
                    send(sender, "Benutzung: /" + name + " <nether|end>", NamedTextColor.YELLOW);
                } else {
                    change(sender, dimension, open);
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private void change(CommandSender sender, Dimension dimension, boolean open) {
        if (plugin.isOpen(dimension) == open) {
            send(sender, dimension.displayName() + " ist bereits " + (open ? "geoeffnet." : "geschlossen."),
                    NamedTextColor.YELLOW);
            return;
        }
        plugin.setOpen(dimension, open);
    }

    private void status(CommandSender sender) {
        for (Dimension dimension : Dimension.values()) {
            boolean open = plugin.isOpen(dimension);
            send(sender, dimension.displayName() + ": " + (open ? "geoeffnet" : "geschlossen"),
                    open ? NamedTextColor.GREEN : NamedTextColor.RED);
        }
    }

    private void send(CommandSender sender, String text, NamedTextColor color) {
        sender.sendMessage(Component.text(text, color));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("portalgate.admin") || args.length != 1) {
            return List.of();
        }
        List<String> options = command.getName().equalsIgnoreCase("end")
                ? List.of("open", "close", "status")
                : List.of("nether", "end");
        String prefix = args[0].toLowerCase(Locale.ROOT);
        return options.stream().filter(option -> option.startsWith(prefix)).toList();
    }
}
