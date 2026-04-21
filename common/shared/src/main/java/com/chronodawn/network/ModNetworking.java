package com.chronodawn.network;

import com.chronodawn.ChronoDawn;
import com.chronodawn.client.shield.ClientShieldCooldowns;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.server.level.ServerPlayer;

/**
 * Env-gated S2C network registration + send helpers for ChronoDawn shields.
 *
 * <p>Architectury's adaptor methods change their {@code @Environment} annotations across
 * versions. In architectury 13.x–18.x (MC 1.21.1–1.21.10) the Fabric adaptor's
 * {@code registerS2C} is annotated {@code @Environment(EnvType.CLIENT)}, so calling
 * {@link NetworkManager#registerReceiver(NetworkManager.Side, net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type,
 * net.minecraft.network.codec.StreamCodec, NetworkManager.NetworkReceiver) NetworkManager.registerReceiver(Side.S2C, ...)}
 * from a dedicated-server init path throws {@code AbstractMethodError}. In 19.x (MC 1.21.11)
 * that annotation was dropped and the call is safe on both sides. The conservative portable
 * pattern is therefore:
 * <ul>
 *   <li>CLIENT env: call {@code registerReceiver(Side.S2C, ...)} — registers type AND receiver.</li>
 *   <li>SERVER env: call {@code registerS2CPayloadType(...)} — registers only the type (needed
 *       so the server can encode outgoing packets on 1.21.1+ Fabric payload APIs).</li>
 * </ul>
 *
 * <p>Calling both in the same JVM would double-register on Fabric's
 * {@code PayloadTypeRegistry.playS2C()} and throw "already registered", so the branches must
 * be mutually exclusive.
 *
 * <p>Class-loading note: the receiver lambda references {@link ClientShieldCooldowns} (which
 * imports {@code net.minecraft.client.Minecraft}). {@link Platform#getEnvironment()} gates the
 * lambda construction to CLIENT envs, and {@code LambdaMetafactory} resolves lambda body types
 * lazily, so dedicated servers never load the client class.
 */
public final class ModNetworking {
    private ModNetworking() {}

    public static void register() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                S2CShieldCooldownPayload.TYPE,
                S2CShieldCooldownPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> ClientShieldCooldowns.applyFromPayload(payload))
            );
        } else {
            NetworkManager.registerS2CPayloadType(
                S2CShieldCooldownPayload.TYPE,
                S2CShieldCooldownPayload.STREAM_CODEC
            );
        }
        ChronoDawn.LOGGER.debug("Registered S2C shield-cooldown payload (env={})", Platform.getEnvironment());
    }

    public static void sendShieldSpeedCooldown(ServerPlayer player, int durationTicks) {
        // Guarded so unit tests with mocked ServerPlayer (no real server network handler) don't
        // NPE inside architectury's ServerPlayNetworking.createS2CPacket. State changes in the
        // caller are authoritative; the overlay packet is a best-effort client hint.
        try {
            NetworkManager.sendToPlayer(player,
                new S2CShieldCooldownPayload(S2CShieldCooldownPayload.KIND_SPEED, durationTicks));
        } catch (Throwable ignored) {
        }
    }

    public static void sendShieldEchoCooldown(ServerPlayer player, int durationTicks) {
        try {
            NetworkManager.sendToPlayer(player,
                new S2CShieldCooldownPayload(S2CShieldCooldownPayload.KIND_ECHO, durationTicks));
        } catch (Throwable ignored) {
        }
    }
}
