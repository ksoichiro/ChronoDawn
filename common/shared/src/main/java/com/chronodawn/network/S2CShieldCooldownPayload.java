package com.chronodawn.network;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * S2C payload notifying the client that a ChronoDawn shield cooldown has just started.
 *
 * Field {@code kind} is renamed (not {@code type}) so the record accessor does not
 * collide with {@link CustomPacketPayload#type()}.
 * {@code durationTicks} is the full cooldown length; the client records its own now-tick
 * on receipt and derives {@code endTick = receiveTick + durationTicks}, sidestepping
 * server/client game-time skew.
 */
public record S2CShieldCooldownPayload(byte kind, int durationTicks) implements CustomPacketPayload {
    public static final byte KIND_SPEED = 0;
    public static final byte KIND_ECHO = 1;

    public static final CustomPacketPayload.Type<S2CShieldCooldownPayload> TYPE =
        new CustomPacketPayload.Type<>(
            CompatResourceLocation.create(ChronoDawn.MOD_ID, "s2c_shield_cd"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CShieldCooldownPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BYTE, S2CShieldCooldownPayload::kind,
            ByteBufCodecs.VAR_INT, S2CShieldCooldownPayload::durationTicks,
            S2CShieldCooldownPayload::new
        );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
