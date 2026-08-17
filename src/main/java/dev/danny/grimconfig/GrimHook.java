package dev.danny.grimconfig;

import ac.grim.grimac.api.GrimAbstractAPI;
import ac.grim.grimac.api.event.EventBus;
import ac.grim.grimac.api.event.events.CommandExecuteEvent;
import ac.grim.grimac.api.event.events.CompletePredictionEvent;
import ac.grim.grimac.api.event.events.FlagEvent;
import ac.grim.grimac.api.plugin.GrimPlugin;

/**
 * Subscribes to Grim's event bus and suppresses flags, prediction offsets and
 * punishment commands for checks the admin has disabled. Handlers return the
 * new cancelled state; a high priority gives us the final say.
 */
public final class GrimHook {

    private static final int PRIORITY = 1000;

    private final GrimConfigPlugin plugin;
    private final GrimAbstractAPI api;

    public GrimHook(GrimConfigPlugin plugin, GrimAbstractAPI api) {
        this.plugin = plugin;
        this.api = api;
    }

    public void register() {
        ChecksService checks = plugin.checks();
        EventBus bus = api.getEventBus();
        GrimPlugin grimPlugin = api.getGrimPlugin(plugin);

        bus.get(FlagEvent.class).onFlagSupplier(grimPlugin,
                (user, check, verbose, cancelled) -> checks.shouldSuppress(check) || cancelled,
                PRIORITY, false);

        bus.get(CompletePredictionEvent.class).onCompletePrediction(grimPlugin,
                (user, check, offset, cancelled) -> checks.shouldSuppress(check) || cancelled,
                PRIORITY, false);

        bus.get(CommandExecuteEvent.class).onCommandExecuteSupplier(grimPlugin,
                (user, check, verbose, command, cancelled) -> checks.shouldSuppress(check) || cancelled,
                PRIORITY, false);
    }
}
