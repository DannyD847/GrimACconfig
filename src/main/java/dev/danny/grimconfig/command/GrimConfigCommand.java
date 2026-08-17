package dev.danny.grimconfig.command;

import dev.danny.grimconfig.ChecksService;
import dev.danny.grimconfig.GrimConfigPlugin;
import dev.danny.grimconfig.gui.CheckMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class GrimConfigCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS =
            List.of("gui", "list", "enable", "disable", "toggle", "status", "reload");

    private static final Component PREFIX =
            Component.text("[GrimConfig] ", NamedTextColor.AQUA);

    private final GrimConfigPlugin plugin;

    public GrimConfigCommand(GrimConfigPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String @NotNull [] args) {
        ChecksService checks = plugin.checks();

        if (args.length == 0 || args[0].equalsIgnoreCase("gui")) {
            if (sender instanceof Player player) {
                CheckMenu.open(plugin, player, 0);
            } else {
                sender.sendMessage(PREFIX.append(Component.text(
                        "The GUI is player-only. Use /" + label + " list", NamedTextColor.RED)));
            }
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        String rest = args.length > 1 ? String.join(" ", List.of(args).subList(1, args.length)) : "";

        switch (sub) {
            case "list" -> {
                plugin.discoverOnline();
                List<Component> parts = new ArrayList<>();
                for (ChecksService.CheckEntry entry : checks.all()) {
                    parts.add(Component.text(entry.display(),
                            entry.enabled() ? NamedTextColor.GREEN : NamedTextColor.RED));
                }
                sender.sendMessage(PREFIX.append(Component.text(
                        checks.enabledCount() + "/" + checks.size() + " checks enabled:",
                        NamedTextColor.GRAY)));
                sender.sendMessage(Component.join(
                        JoinConfiguration.separator(Component.text(", ", NamedTextColor.DARK_GRAY)),
                        parts));
            }
            case "enable", "disable" -> {
                boolean enable = sub.equals("enable");
                if (rest.isEmpty()) {
                    usage(sender, label);
                    return true;
                }
                if (rest.equalsIgnoreCase("all")) {
                    int changed = checks.setAll(enable);
                    sender.sendMessage(PREFIX.append(Component.text(
                            (enable ? "Enabled " : "Disabled ") + changed + " checks.",
                            enable ? NamedTextColor.GREEN : NamedTextColor.RED)));
                    return true;
                }
                Optional<ChecksService.CheckEntry> entry = checks.setEnabled(rest, enable);
                if (entry.isEmpty()) {
                    unknown(sender, rest);
                } else {
                    sender.sendMessage(PREFIX.append(state(entry.get())));
                }
            }
            case "toggle" -> {
                if (rest.isEmpty()) {
                    usage(sender, label);
                    return true;
                }
                Optional<ChecksService.CheckEntry> entry = checks.toggle(rest);
                if (entry.isEmpty()) {
                    unknown(sender, rest);
                } else {
                    sender.sendMessage(PREFIX.append(state(entry.get())));
                }
            }
            case "status" -> {
                if (rest.isEmpty()) {
                    usage(sender, label);
                    return true;
                }
                Optional<ChecksService.CheckEntry> entry = checks.find(rest);
                if (entry.isEmpty()) {
                    unknown(sender, rest);
                } else {
                    sender.sendMessage(PREFIX.append(state(entry.get())));
                }
            }
            case "reload" -> {
                plugin.reloadConfig();
                checks.load();
                plugin.discoverOnline();
                sender.sendMessage(PREFIX.append(Component.text(
                        "Reloaded — " + checks.enabledCount() + "/" + checks.size()
                                + " checks enabled.", NamedTextColor.GREEN)));
            }
            default -> usage(sender, label);
        }
        return true;
    }

    private static Component state(ChecksService.CheckEntry entry) {
        return Component.text(entry.display() + " is now ", NamedTextColor.GRAY)
                .append(entry.enabled()
                        ? Component.text("ENABLED", NamedTextColor.GREEN)
                        : Component.text("DISABLED", NamedTextColor.RED));
    }

    private static void unknown(CommandSender sender, String query) {
        sender.sendMessage(PREFIX.append(Component.text(
                "Unknown check '" + query + "'. See /grimconfig list "
                        + "(checks are discovered once a player has joined).",
                NamedTextColor.RED)));
    }

    private static void usage(CommandSender sender, String label) {
        sender.sendMessage(PREFIX.append(Component.text(
                "Usage: /" + label + " [gui|list|enable|disable|toggle|status|reload] [check|all]",
                NamedTextColor.GRAY)));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, String @NotNull [] args) {
        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("enable") || sub.equals("disable")
                    || sub.equals("toggle") || sub.equals("status")) {
                List<String> names = new ArrayList<>();
                if (sub.equals("enable") || sub.equals("disable")) names.add("all");
                for (ChecksService.CheckEntry entry : plugin.checks().all()) {
                    names.add(entry.display());
                }
                return filter(names, args[1]);
            }
        }
        return List.of();
    }

    private static List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                out.add(option);
            }
        }
        return out;
    }
}
