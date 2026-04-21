package com.chronodawn.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * S2C payload notifying the client that a ChronoDawn shield cooldown has just started.
 *
 * 1.20.1 predates Minecraft's {@code CustomPacketPayload} (MC 1.20.5+) and Architectury's
 * {@code registerS2CPayloadType} API. This file is the 1.20.1-specific replacement for the
 * shared record under {@code common/shared}; it's a plain data class with explicit
 * {@code encode}/{@code decode} helpers that sit on top of Architectury 9.x's
 * {@code FriendlyByteBuf}-based {@code NetworkManager.registerReceiver}.
 *
 * Field {@code kind} matches the shared record's accessor so
 * {@link com.chronodawn.client.shield.ClientShieldCooldowns#applyFromPayload} can stay
 * version-agnostic.
 */
public final class S2CShieldCooldownPayload {
    public static final byte KIND_SPEED = 0;
    public static final byte KIND_ECHO = 1;

    private final byte kind;
    private final int durationTicks;

    public S2CShieldCooldownPayload(byte kind, int durationTicks) {
        this.kind = kind;
        this.durationTicks = durationTicks;
    }

    public byte kind() { return kind; }
    public int durationTicks() { return durationTicks; }

    public static void encode(S2CShieldCooldownPayload payload, FriendlyByteBuf buf) {
        buf.writeByte(payload.kind);
        buf.writeVarInt(payload.durationTicks);
    }

    public static S2CShieldCooldownPayload decode(FriendlyByteBuf buf) {
        byte kind = buf.readByte();
        int durationTicks = buf.readVarInt();
        return new S2CShieldCooldownPayload(kind, durationTicks);
    }
}
