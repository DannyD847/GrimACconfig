package dev.danny.grimconfig;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Discovers newly-loaded Grim checks shortly after a player joins, so the
 * config and GUI always reflect the checks this Grim version actually runs.
 */
public final class JoinListener implements Listener {

    private final GrimConfigPlugin plugin;

    public JoinListener(GrimConfigPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                plugin.discoverFor(player);
            }
        }, 40L);
    }
}
