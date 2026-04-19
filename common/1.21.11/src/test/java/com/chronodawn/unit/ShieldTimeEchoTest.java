package com.chronodawn.unit;

import com.chronodawn.data.PlayerProgressData;
import com.chronodawn.items.shield.ChronoShieldEffectHandler;
import com.chronodawn.items.shield.ChronoShieldMarker;
import com.chronodawn.items.shield.ChronoShieldTier;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ChronoShieldEffectHandler#tryGenerateEcho} and
 * {@link ChronoShieldEffectHandler#tryConsumeEcho}.
 *
 * Validates Effect #12 (Time Echo) handler behavior:
 * - T1/T2: tryGenerateEcho is a no-op (tier flag gates it)
 * - T3 with no CD and no active echo: schedules activeUntil = now + 100
 * - T3 with active CD: no-op
 * - T3 with already-active echo: no refresh
 * - tryConsumeEcho without active echo: returns false, no state changes
 * - tryConsumeEcho with active echo: clears activeUntil, sets CD, deducts 1 durability from shield
 */
public class ShieldTimeEchoTest {

    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void t3_generates_echo_when_no_cd_and_no_active_echo() {
        long now = 1000L;
        ServerPlayer player = mockPlayer(now);
        PlayerProgressData data = mock(PlayerProgressData.class);
        when(data.getShieldEchoCooldownEnd(PLAYER_ID)).thenReturn(0L);
        when(data.getShieldEchoActiveUntil(PLAYER_ID)).thenReturn(0L);
        ServerLevel level = (ServerLevel) player.level();

        try (MockedStatic<PlayerProgressData> mocked = Mockito.mockStatic(PlayerProgressData.class)) {
            mocked.when(() -> PlayerProgressData.get(level)).thenReturn(data);

            ChronoShieldEffectHandler.tryGenerateEcho(player, ChronoShieldTier.T3);

            verify(data).setShieldEchoActiveUntil(PLAYER_ID, now + 100L);
            verify(data, never()).setShieldEchoCooldownEnd(any(UUID.class), Mockito.anyLong());
        }
    }

    @Test
    void t1_does_not_generate_echo() {
        long now = 1000L;
        ServerPlayer player = mockPlayer(now);
        PlayerProgressData data = mock(PlayerProgressData.class);
        ServerLevel level = (ServerLevel) player.level();

        try (MockedStatic<PlayerProgressData> mocked = Mockito.mockStatic(PlayerProgressData.class)) {
            mocked.when(() -> PlayerProgressData.get(level)).thenReturn(data);

            ChronoShieldEffectHandler.tryGenerateEcho(player, ChronoShieldTier.T1);

            verify(data, never()).setShieldEchoActiveUntil(any(UUID.class), Mockito.anyLong());
            verify(data, never()).setShieldEchoCooldownEnd(any(UUID.class), Mockito.anyLong());
        }
    }

    @Test
    void t2_does_not_generate_echo() {
        long now = 1000L;
        ServerPlayer player = mockPlayer(now);
        PlayerProgressData data = mock(PlayerProgressData.class);
        ServerLevel level = (ServerLevel) player.level();

        try (MockedStatic<PlayerProgressData> mocked = Mockito.mockStatic(PlayerProgressData.class)) {
            mocked.when(() -> PlayerProgressData.get(level)).thenReturn(data);

            ChronoShieldEffectHandler.tryGenerateEcho(player, ChronoShieldTier.T2);

            verify(data, never()).setShieldEchoActiveUntil(any(UUID.class), Mockito.anyLong());
            verify(data, never()).setShieldEchoCooldownEnd(any(UUID.class), Mockito.anyLong());
        }
    }

    @Test
    void t3_skips_generation_when_cd_active() {
        long now = 1000L;
        ServerPlayer player = mockPlayer(now);
        PlayerProgressData data = mock(PlayerProgressData.class);
        // CD ends in the future — trigger suppressed
        when(data.getShieldEchoCooldownEnd(PLAYER_ID)).thenReturn(now + 50L);
        ServerLevel level = (ServerLevel) player.level();

        try (MockedStatic<PlayerProgressData> mocked = Mockito.mockStatic(PlayerProgressData.class)) {
            mocked.when(() -> PlayerProgressData.get(level)).thenReturn(data);

            ChronoShieldEffectHandler.tryGenerateEcho(player, ChronoShieldTier.T3);

            verify(data, never()).setShieldEchoActiveUntil(any(UUID.class), Mockito.anyLong());
            verify(data, never()).setShieldEchoCooldownEnd(any(UUID.class), Mockito.anyLong());
        }
    }

    @Test
    void t3_skips_generation_when_echo_already_active() {
        long now = 1000L;
        ServerPlayer player = mockPlayer(now);
        PlayerProgressData data = mock(PlayerProgressData.class);
        when(data.getShieldEchoCooldownEnd(PLAYER_ID)).thenReturn(0L);
        // Echo still active (ends in the future) — do not refresh
        when(data.getShieldEchoActiveUntil(PLAYER_ID)).thenReturn(now + 40L);
        ServerLevel level = (ServerLevel) player.level();

        try (MockedStatic<PlayerProgressData> mocked = Mockito.mockStatic(PlayerProgressData.class)) {
            mocked.when(() -> PlayerProgressData.get(level)).thenReturn(data);

            ChronoShieldEffectHandler.tryGenerateEcho(player, ChronoShieldTier.T3);

            verify(data, never()).setShieldEchoActiveUntil(any(UUID.class), Mockito.anyLong());
            verify(data, never()).setShieldEchoCooldownEnd(any(UUID.class), Mockito.anyLong());
        }
    }

    @Test
    void tryConsumeEcho_returns_false_when_no_echo_active() {
        long now = 1000L;
        ServerPlayer player = mockPlayer(now);
        PlayerProgressData data = mock(PlayerProgressData.class);
        // No active echo (activeUntil <= now)
        when(data.getShieldEchoActiveUntil(PLAYER_ID)).thenReturn(0L);
        ServerLevel level = (ServerLevel) player.level();

        try (MockedStatic<PlayerProgressData> mocked = Mockito.mockStatic(PlayerProgressData.class)) {
            mocked.when(() -> PlayerProgressData.get(level)).thenReturn(data);

            boolean consumed = ChronoShieldEffectHandler.tryConsumeEcho(player);

            assertFalse(consumed, "Expected tryConsumeEcho to return false with no active echo");
            verify(data, never()).setShieldEchoActiveUntil(any(UUID.class), Mockito.anyLong());
            verify(data, never()).setShieldEchoCooldownEnd(any(UUID.class), Mockito.anyLong());
            verify(player, never()).getItemInHand(any(InteractionHand.class));
        }
    }

    @Test
    void tryConsumeEcho_consumes_and_sets_cd_when_echo_active() {
        long now = 1000L;
        ServerPlayer player = mockPlayer(now);
        PlayerProgressData data = mock(PlayerProgressData.class);
        // Echo currently active (ends in the future)
        when(data.getShieldEchoActiveUntil(PLAYER_ID)).thenReturn(now + 30L);
        ServerLevel level = (ServerLevel) player.level();

        // Off-hand has a ChronoDawn shield stack; main-hand empty
        ItemStack offhandShield = mock(ItemStack.class);
        Item shieldItem = mock(Item.class, Mockito.withSettings().extraInterfaces(ChronoShieldMarker.class));
        when(offhandShield.getItem()).thenReturn(shieldItem);
        ItemStack mainhandEmpty = mock(ItemStack.class);
        when(mainhandEmpty.getItem()).thenReturn(Items.AIR);
        when(player.getItemInHand(InteractionHand.MAIN_HAND)).thenReturn(mainhandEmpty);
        when(player.getItemInHand(InteractionHand.OFF_HAND)).thenReturn(offhandShield);

        try (MockedStatic<PlayerProgressData> mocked = Mockito.mockStatic(PlayerProgressData.class)) {
            mocked.when(() -> PlayerProgressData.get(level)).thenReturn(data);

            boolean consumed = ChronoShieldEffectHandler.tryConsumeEcho(player);

            assertTrue(consumed, "Expected tryConsumeEcho to return true when echo active");
            verify(data).setShieldEchoActiveUntil(PLAYER_ID, 0L);
            verify(data).setShieldEchoCooldownEnd(PLAYER_ID, now + 600L);
            // 1 durability drained from the offhand ChronoDawn shield
            verify(offhandShield).hurtAndBreak(1, player, EquipmentSlot.OFFHAND);
            // Main-hand (empty) must not have been hurt
            verify(mainhandEmpty, never()).hurtAndBreak(anyInt(), any(), any(EquipmentSlot.class));
        }
    }

    @Test
    void tryConsumeEcho_durability_from_mainhand_if_offhand_empty() {
        long now = 1000L;
        ServerPlayer player = mockPlayer(now);
        PlayerProgressData data = mock(PlayerProgressData.class);
        when(data.getShieldEchoActiveUntil(PLAYER_ID)).thenReturn(now + 30L);
        ServerLevel level = (ServerLevel) player.level();

        // Main-hand holds the ChronoDawn shield; off-hand empty
        ItemStack mainhandShield = mock(ItemStack.class);
        Item shieldItem = mock(Item.class, Mockito.withSettings().extraInterfaces(ChronoShieldMarker.class));
        when(mainhandShield.getItem()).thenReturn(shieldItem);
        ItemStack offhandEmpty = mock(ItemStack.class);
        when(offhandEmpty.getItem()).thenReturn(Items.AIR);
        when(player.getItemInHand(InteractionHand.MAIN_HAND)).thenReturn(mainhandShield);
        when(player.getItemInHand(InteractionHand.OFF_HAND)).thenReturn(offhandEmpty);

        try (MockedStatic<PlayerProgressData> mocked = Mockito.mockStatic(PlayerProgressData.class)) {
            mocked.when(() -> PlayerProgressData.get(level)).thenReturn(data);

            boolean consumed = ChronoShieldEffectHandler.tryConsumeEcho(player);

            assertTrue(consumed, "Expected tryConsumeEcho to return true when echo active");
            verify(data).setShieldEchoActiveUntil(PLAYER_ID, 0L);
            verify(data).setShieldEchoCooldownEnd(PLAYER_ID, now + 600L);
            verify(mainhandShield).hurtAndBreak(1, player, EquipmentSlot.OFFHAND);
            verify(offhandEmpty, never()).hurtAndBreak(anyInt(), any(), any(EquipmentSlot.class));
        }
    }

    // --- helpers ---

    private static ServerPlayer mockPlayer(long gameTime) {
        ServerLevel level = mock(ServerLevel.class);
        when(level.getGameTime()).thenReturn(gameTime);

        ServerPlayer player = mock(ServerPlayer.class);
        when(player.level()).thenReturn(level);
        when(player.getUUID()).thenReturn(PLAYER_ID);
        return player;
    }
}
