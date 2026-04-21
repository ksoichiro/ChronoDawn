package com.chronodawn.network;

import com.chronodawn.ChronoDawn;
import com.chronodawn.client.shield.ClientShieldCooldowns;
import com.chronodawn.compat.CompatResourceLocation;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Single-call S2C network registration + send helpers for ChronoDawn shields (1.20.1 variant).
 *
 * 1.20.1 uses Architectury 9.x's {@code FriendlyByteBuf}-based API rather than the newer
 * {@code CustomPacketPayload} + {@code StreamCodec} path. Semantics match the shared variant:
 * call {@link #register()} once from each loader's {@code onInitialize}. On dedicated servers
 * the receiver lambda is never invoked; on integrated/pure clients it decodes the incoming buf
 * and dispatches to {@link ClientShieldCooldowns#applyFromPayload}.
 *
 * Lambda class-loading of client-only {@code ClientShieldCooldowns} → {@code Minecraft} is
 * deferred until first invocation, so this is safe to call from a path that also runs on a
 * dedicated Fabric server.
 */
public final class ModNetworking {
    private ModNetworking() {}

    public static final ResourceLocation SHIELD_COOLDOWN_ID =
        CompatResourceLocation.create(ChronoDawn.MOD_ID, "s2c_shield_cd");

    public static void register() {
        // Architectury 9.x's registerS2CReceiver is @Environment(EnvType.CLIENT); calling
        // NetworkManager.registerReceiver(Side.S2C, ...) on a dedicated server throws
        // NoSuchMethodError. 1.20.1 Fabric also doesn't require any server-side type
        // pre-registration (sendToPlayer uses ServerPlayNetworking.createS2CPacket(id, buf)
        // directly), so the server path is a no-op.
        if (Platform.getEnvironment() != Env.CLIENT) {
            ChronoDawn.LOGGER.debug("Skipping S2C shield-cooldown receiver registration (server env, 1.20.1)");
            return;
        }
        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            SHIELD_COOLDOWN_ID,
            (buf, context) -> {
                S2CShieldCooldownPayload payload = S2CShieldCooldownPayload.decode(buf);
                context.queue(() -> ClientShieldCooldowns.applyFromPayload(payload));
            }
        );
        ChronoDawn.LOGGER.debug("Registered S2C shield-cooldown receiver (1.20.1 API)");
    }

    public static void sendShieldSpeedCooldown(ServerPlayer player, int durationTicks) {
        // Guarded so unit tests with mocked ServerPlayer don't NPE inside architectury's
        // server packet path. State changes in the caller are authoritative; the overlay
        // packet is a best-effort client hint.
        try {
            FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            S2CShieldCooldownPayload.encode(
                new S2CShieldCooldownPayload(S2CShieldCooldownPayload.KIND_SPEED, durationTicks), buf);
            NetworkManager.sendToPlayer(player, SHIELD_COOLDOWN_ID, buf);
        } catch (Throwable ignored) {
        }
    }

    public static void sendShieldEchoCooldown(ServerPlayer player, int durationTicks) {
        try {
            FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            S2CShieldCooldownPayload.encode(
                new S2CShieldCooldownPayload(S2CShieldCooldownPayload.KIND_ECHO, durationTicks), buf);
            NetworkManager.sendToPlayer(player, SHIELD_COOLDOWN_ID, buf);
        } catch (Throwable ignored) {
        }
    }
}
