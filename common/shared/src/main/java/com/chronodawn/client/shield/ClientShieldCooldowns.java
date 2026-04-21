package com.chronodawn.client.shield;

import com.chronodawn.network.S2CShieldCooldownPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

/**
 * Client-side store of ChronoDawn shield cooldown endpoints for the local player.
 *
 * The server pushes a {@code S2CShieldCooldownPayload} each time a CD starts. The
 * receiver stamps {@link #updateSpeed}/{@link #updateEcho} with the client's current
 * game-tick at arrival plus the server-provided duration; the render-thread Mixin
 * reads {@link #getSpeedProgress}/{@link #getEchoProgress} to compute a 0–1 overlay
 * ratio.
 *
 * All access is single-threaded (client main thread); no synchronization needed.
 *
 * Note: this class references {@link Minecraft}, which is client-only. It is only
 * loaded when its methods are actually invoked — on dedicated servers the receiver
 * lambda path never fires, so this class stays unloaded and does not force
 * resolution of missing client classes.
 */
public final class ClientShieldCooldowns {
    private ClientShieldCooldowns() {}

    private static long speedStartTick = 0L;
    private static long speedEndTick = 0L;
    private static long echoStartTick = 0L;
    private static long echoEndTick = 0L;

    public static void updateSpeed(long nowTick, int durationTicks) {
        speedStartTick = nowTick;
        speedEndTick = nowTick + durationTicks;
    }

    public static void updateEcho(long nowTick, int durationTicks) {
        echoStartTick = nowTick;
        echoEndTick = nowTick + durationTicks;
    }

    /**
     * Packet-receiver entry point. Called from the S2C handler on the client main thread
     * (architectury queues the call via {@code PacketContext#queue}).
     */
    public static void applyFromPayload(S2CShieldCooldownPayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        long now = level == null ? 0L : level.getGameTime();
        if (payload.kind() == S2CShieldCooldownPayload.KIND_SPEED) {
            updateSpeed(now, payload.durationTicks());
        } else if (payload.kind() == S2CShieldCooldownPayload.KIND_ECHO) {
            updateEcho(now, payload.durationTicks());
        }
    }

    /** 0.0 when expired, approaching 1.0 when just started. */
    public static float getSpeedProgress(long nowTick, float partialTick) {
        return compute(speedStartTick, speedEndTick, nowTick, partialTick);
    }

    public static float getEchoProgress(long nowTick, float partialTick) {
        return compute(echoStartTick, echoEndTick, nowTick, partialTick);
    }

    private static float compute(long startTick, long endTick, long nowTick, float partialTick) {
        long total = endTick - startTick;
        if (total <= 0L) return 0f;
        float elapsed = (float)(nowTick - startTick) + partialTick;
        float remaining = (float)total - elapsed;
        if (remaining <= 0f) return 0f;
        float ratio = remaining / (float)total;
        return Math.min(1f, Math.max(0f, ratio));
    }
}
