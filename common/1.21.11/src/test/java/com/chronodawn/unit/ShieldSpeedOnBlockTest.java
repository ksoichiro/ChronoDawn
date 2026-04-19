package com.chronodawn.unit;

import com.chronodawn.data.PlayerProgressData;
import com.chronodawn.items.shield.ChronoShieldEffectHandler;
import com.chronodawn.items.shield.ChronoShieldTier;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ChronoShieldEffectHandler#onBlockSuccess(ServerPlayer, ChronoShieldTier)}.
 *
 * Validates Effect #7 (Speed on block) behavior:
 * - T1: no-op (tier flag hasSpeedOnBlock = false)
 * - T2/T3 with no active cooldown: Speed I applied, CD stored for 3 seconds
 * - T2/T3 with active cooldown: no-op (prevents shield-swap exploits)
 *
 * <p>Mocks the static {@link PlayerProgressData#get(ServerLevel)} via
 * {@link Mockito#mockStatic(Class)} so no ServerLevel data storage is required.
 */
public class ShieldSpeedOnBlockTest {

    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void t1_shield_is_noop_no_effect_applied() {
        ServerPlayer player = mockPlayer(1000L);
        PlayerProgressData data = mock(PlayerProgressData.class);
        ServerLevel level = (ServerLevel) player.level();

        try (MockedStatic<PlayerProgressData> mocked = Mockito.mockStatic(PlayerProgressData.class)) {
            mocked.when(() -> PlayerProgressData.get(level)).thenReturn(data);

            ChronoShieldEffectHandler.onBlockSuccess(player, ChronoShieldTier.T1);

            verify(player, never()).addEffect(any());
            verify(data, never()).setShieldSpeedCooldownEnd(any(UUID.class), Mockito.anyLong());
        }
    }

    @Test
    void t2_shield_applies_speed_when_no_cooldown_active() {
        long now = 1000L;
        ServerPlayer player = mockPlayer(now);
        PlayerProgressData data = mock(PlayerProgressData.class);
        // Past cooldown (ended long ago)
        when(data.getShieldSpeedCooldownEnd(PLAYER_ID)).thenReturn(0L);
        ServerLevel level = (ServerLevel) player.level();

        try (MockedStatic<PlayerProgressData> mocked = Mockito.mockStatic(PlayerProgressData.class)) {
            mocked.when(() -> PlayerProgressData.get(level)).thenReturn(data);

            ChronoShieldEffectHandler.onBlockSuccess(player, ChronoShieldTier.T2);

            // Speed effect applied
            verify(player).addEffect(any(MobEffectInstance.class));
            // Cooldown stored for now + 60 ticks (3 seconds)
            verify(data).setShieldSpeedCooldownEnd(PLAYER_ID, now + 60L);
        }
    }

    @Test
    void t2_shield_is_noop_when_cooldown_active() {
        long now = 1000L;
        ServerPlayer player = mockPlayer(now);
        PlayerProgressData data = mock(PlayerProgressData.class);
        // Cooldown ends in the future — trigger should be suppressed
        when(data.getShieldSpeedCooldownEnd(PLAYER_ID)).thenReturn(now + 30L);
        ServerLevel level = (ServerLevel) player.level();

        try (MockedStatic<PlayerProgressData> mocked = Mockito.mockStatic(PlayerProgressData.class)) {
            mocked.when(() -> PlayerProgressData.get(level)).thenReturn(data);

            ChronoShieldEffectHandler.onBlockSuccess(player, ChronoShieldTier.T2);

            verify(player, never()).addEffect(any());
            verify(data, never()).setShieldSpeedCooldownEnd(any(UUID.class), Mockito.anyLong());
        }
    }

    @Test
    void t3_shield_applies_speed_when_no_cooldown_active() {
        long now = 5000L;
        ServerPlayer player = mockPlayer(now);
        PlayerProgressData data = mock(PlayerProgressData.class);
        when(data.getShieldSpeedCooldownEnd(PLAYER_ID)).thenReturn(0L);
        ServerLevel level = (ServerLevel) player.level();

        try (MockedStatic<PlayerProgressData> mocked = Mockito.mockStatic(PlayerProgressData.class)) {
            mocked.when(() -> PlayerProgressData.get(level)).thenReturn(data);

            ChronoShieldEffectHandler.onBlockSuccess(player, ChronoShieldTier.T3);

            verify(player).addEffect(any(MobEffectInstance.class));
            verify(data).setShieldSpeedCooldownEnd(PLAYER_ID, now + 60L);
        }
    }

    // --- helpers ---

    /**
     * Build a mock {@link ServerPlayer} whose level() returns a mocked {@link ServerLevel}
     * with the given game time, and whose getUUID() returns {@link #PLAYER_ID}.
     */
    private static ServerPlayer mockPlayer(long gameTime) {
        ServerLevel level = mock(ServerLevel.class);
        when(level.getGameTime()).thenReturn(gameTime);

        ServerPlayer player = mock(ServerPlayer.class);
        when(player.level()).thenReturn(level);
        when(player.getUUID()).thenReturn(PLAYER_ID);
        return player;
    }
}
