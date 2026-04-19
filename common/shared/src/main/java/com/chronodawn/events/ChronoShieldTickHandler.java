package com.chronodawn.events;

import com.chronodawn.ChronoDawn;
import com.chronodawn.data.PlayerProgressData;
import com.chronodawn.items.shield.ChronoShieldEffectHandler;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Stage 2 (active drift) FX dispatcher for the Temporal Guardian Shield's Time Echo.
 *
 * <p>Every 10 server ticks, iterates players on each {@link ServerLevel}; for any
 * player whose echo-active window is still open, emits a single drift particle via
 * {@link ChronoShieldEffectHandler#emitEchoActiveDriftFx(ServerPlayer)}.
 *
 * <p>Stages 1 (generation burst) and 3 (consumption burst) are emitted inline from
 * {@link ChronoShieldEffectHandler#tryGenerateEcho} and
 * {@link ChronoShieldEffectHandler#tryConsumeEcho}; only the periodic drift pulse
 * needs a tick hook, which is why this is a dedicated class.
 */
public final class ChronoShieldTickHandler {
    private ChronoShieldTickHandler() {}

    /** Emit one drift particle per active-echo player every this many ticks. */
    private static final int DRIFT_INTERVAL_TICKS = 10;

    /**
     * Register the SERVER_LEVEL_POST tick hook. Called from
     * {@link ChronoDawnEvents#register()} during mod init.
     */
    public static void register() {
        TickEvent.SERVER_LEVEL_POST.register(ChronoShieldTickHandler::onServerLevelTick);
        ChronoDawn.LOGGER.debug("Registered ChronoShieldTickHandler");
    }

    private static void onServerLevelTick(ServerLevel level) {
        long now = level.getGameTime();
        if (now % DRIFT_INTERVAL_TICKS != 0) return;
        if (level.players().isEmpty()) return;

        PlayerProgressData data = PlayerProgressData.get(level);
        for (ServerPlayer sp : level.players()) {
            long activeUntil = data.getShieldEchoActiveUntil(sp.getUUID());
            if (activeUntil > now) {
                ChronoShieldEffectHandler.emitEchoActiveDriftFx(sp);
            }
        }
    }
}
