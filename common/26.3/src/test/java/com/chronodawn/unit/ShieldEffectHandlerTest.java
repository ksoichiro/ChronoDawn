package com.chronodawn.unit;

import com.chronodawn.items.shield.ChronoShieldEffectHandler;
import com.chronodawn.items.shield.ChronoShieldMarker;
import com.chronodawn.items.shield.ChronoShieldTier;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Unit tests for {@link ChronoShieldEffectHandler#maybeShortenDebuff}.
 *
 * Validates Effect A behavior: time-themed debuff durations are halved when the
 * target is holding a ChronoDawn shield in either hand (main-hand or off-hand),
 * and untouched otherwise. The trigger is passive — blocking is not required.
 *
 * <p>Uses Mockito (inline) to stub {@link LivingEntity} and {@link ItemStack} (both
 * {@code final}) so the handler can be driven deterministically without needing a
 * registered Item. Bootstraps Minecraft registries in {@link #bootstrap()} so that
 * the handler's static initializer (which resolves {@link MobEffects} holders) works.
 */
public class ShieldEffectHandlerTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void slowness_duration_halved_when_holding_chrono_shield_in_offhand() {
        LivingEntity target = mockEntityWithHands(Items.AIR, mockChronoShieldItem());

        MobEffectInstance incoming = new MobEffectInstance(MobEffects.SLOWNESS, 200, 0);
        MobEffectInstance result = ChronoShieldEffectHandler.maybeShortenDebuff(target, incoming);

        assertEquals(100, result.getDuration(),
            "Slowness duration should be halved when holding a ChronoDawn shield in the off-hand");
    }

    @Test
    void slowness_duration_halved_when_holding_chrono_shield_in_mainhand() {
        LivingEntity target = mockEntityWithHands(mockChronoShieldItem(), Items.AIR);

        MobEffectInstance incoming = new MobEffectInstance(MobEffects.SLOWNESS, 200, 0);
        MobEffectInstance result = ChronoShieldEffectHandler.maybeShortenDebuff(target, incoming);

        assertEquals(100, result.getDuration(),
            "Slowness duration should be halved when holding a ChronoDawn shield in the main hand");
    }

    @Test
    void slowness_duration_unchanged_when_not_holding_chrono_shield() {
        LivingEntity target = mockEntityWithHands(Items.STICK, Items.AIR);

        MobEffectInstance incoming = new MobEffectInstance(MobEffects.SLOWNESS, 200, 0);
        MobEffectInstance result = ChronoShieldEffectHandler.maybeShortenDebuff(target, incoming);

        assertSame(incoming, result,
            "Handler should return the original instance when no ChronoDawn shield is held");
        assertEquals(200, result.getDuration(),
            "Slowness duration should be unchanged when no ChronoDawn shield is held in either hand");
    }

    @Test
    void slowness_duration_unchanged_when_holding_vanilla_shield_in_offhand() {
        // Items.SHIELD is a real registered Item; just mock the ItemStack to return it.
        LivingEntity target = mockEntityWithHands(Items.AIR, Items.SHIELD);

        MobEffectInstance incoming = new MobEffectInstance(MobEffects.SLOWNESS, 200, 0);
        MobEffectInstance result = ChronoShieldEffectHandler.maybeShortenDebuff(target, incoming);

        assertSame(incoming, result,
            "Handler should return the original instance when holding a vanilla shield");
        assertEquals(200, result.getDuration(),
            "Slowness duration should be unchanged when holding a non-Chrono shield");
    }

    @Test
    void regeneration_duration_unchanged_even_when_holding_chrono_shield() {
        LivingEntity target = mockEntityWithHands(Items.AIR, mockChronoShieldItem());

        // Regeneration is a buff and NOT in SHORTENED_EFFECTS — must be passed through untouched.
        MobEffectInstance incoming = new MobEffectInstance(MobEffects.REGENERATION, 200, 0);
        MobEffectInstance result = ChronoShieldEffectHandler.maybeShortenDebuff(target, incoming);

        assertSame(incoming, result,
            "Handler should return the original instance for effects outside SHORTENED_EFFECTS");
        assertEquals(200, result.getDuration(),
            "Regeneration duration should be unchanged — it is not in the shortened-effects set");
    }

    // --- helpers ---

    /**
     * Build a mock {@link Item} that also implements {@link ChronoShieldMarker} without
     * invoking the real {@code Item} constructor (which would try to create an intrusive
     * registry holder and fail on a frozen registry).
     */
    private static Item mockChronoShieldItem() {
        Item item = mock(Item.class, withSettings().extraInterfaces(ChronoShieldMarker.class));
        when(((ChronoShieldMarker) item).getChronoShieldTier()).thenReturn(ChronoShieldTier.T1);
        return item;
    }

    /**
     * Build a mock {@link LivingEntity} whose {@link LivingEntity#getMainHandItem()} and
     * {@link LivingEntity#getOffhandItem()} return ItemStacks wrapping the given items.
     */
    private static LivingEntity mockEntityWithHands(Item mainHand, Item offHand) {
        ItemStack mainStack = Mockito.mock(ItemStack.class);
        when(mainStack.getItem()).thenReturn(mainHand);

        ItemStack offStack = Mockito.mock(ItemStack.class);
        when(offStack.getItem()).thenReturn(offHand);

        LivingEntity entity = mock(LivingEntity.class);
        when(entity.getMainHandItem()).thenReturn(mainStack);
        when(entity.getOffhandItem()).thenReturn(offStack);
        return entity;
    }
}
