package dev.danny.grimconfig;

import ac.grim.grimac.api.GrimAbstractAPI;
import ac.grim.grimac.api.GrimAPIProvider;
import ac.grim.grimac.api.GrimUser;
import dev.danny.grimconfig.command.GrimConfigCommand;
import dev.danny.grimconfig.gui.MenuListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class GrimConfigPlugin extends JavaPlugin {

    private ChecksService checks;
    private volatile GrimAbstractAPI grimApi;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        checks = new ChecksService(this);
        checks.load();

        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);

        GrimConfigCommand command = new GrimConfigCommand(this);
        PluginCommand pluginCommand = getCommand("grimconfig");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        // We depend on GrimAC so its API is normally ready, but getAsync also
        // covers the case where Grim finishes initializing after us.
        GrimAPIProvider.getAsync().thenAccept(api -> {
            if (!isEnabled()) return;
            getServer().getScheduler().runTask(this, () -> hook(api));
        });
    }

    private void hook(GrimAbstractAPI api) {
        this.grimApi = api;
        new GrimHook(this, api).register();
        for (Player player : getServer().getOnlinePlayers()) {
            discoverFor(player);
        }
        getLogger().info("Hooked into GrimAC " + api.getGrimVersion() + " — "
                + checks.size() + " checks tracked, " + checks.enabledCount() + " enabled.");
    }

    /** Pulls the live check list from a player's GrimUser into the service. */
    public void discoverFor(Player player) {
        GrimAbstractAPI api = grimApi;
        if (api == null) return;
        GrimUser user = api.getGrimUser(player.getUniqueId());
        if (user != null) {
            checks.discover(user.getChecks());
        }
    }

    /** Refreshes the check list from everyone online (main thread). */
    public void discoverOnline() {
        for (Player player : getServer().getOnlinePlayers()) {
            discoverFor(player);
        }
    }

    public ChecksService checks() {
        return checks;
    }

    public boolean isHooked() {
        return grimApi != null;
    }
}
