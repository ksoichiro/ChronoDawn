package com.chronodawn.events;

import com.chronodawn.ChronoDawn;
import com.chronodawn.data.PlayerProgressData;
import com.chronodawn.items.shield.ChronoShieldEffectHandler;
import com.chronodawn.registry.ModSounds;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

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
     * Play one active-pulse sound per active-echo player every this many ticks.
     * Must be a multiple of {@link #DRIFT_INTERVAL_TICKS} so the pulse tick is a
     * subset of drift ticks and the single `now % DRIFT` gate catches both.
     */
    private static final int PULSE_SOUND_INTERVAL_TICKS = 20;

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

        boolean pulseTick = (now % PULSE_SOUND_INTERVAL_TICKS == 0);
        PlayerProgressData data = PlayerProgressData.get(level);
        for (ServerPlayer sp : level.players()) {
            long activeUntil = data.getShieldEchoActiveUntil(sp.getUUID());
            if (activeUntil > now) {
                ChronoShieldEffectHandler.emitEchoActiveDriftFx(sp);
                if (pulseTick) {
                    level.playSound(
                        null, sp.blockPosition(),
                        ModSounds.SHIELD_ECHO_ACTIVE_PULSE.get(),
                        SoundSource.PLAYERS, 0.5f, 1.0f
                    );
                }
            }
        }
    }
}
