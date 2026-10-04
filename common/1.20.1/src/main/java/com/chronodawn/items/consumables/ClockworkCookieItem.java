package com.chronodawn.items.consumables;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.List;

/**
 * Clockwork Cookie (歯車クッキー)
 *
 * A time-themed cookie stamped out with a Clockwork Block used as a mold.
 * Eating one "winds the spring": it extends the remaining Resistance I duration
 * instead of resetting it, so several cookies eaten before a fight stack up.
 *
 * Recipe: 2x Time Wheat + 1x Time Jam + 1x Clockwork Block → 4x Clockwork Cookie
 * (the Clockwork Block is a mold and stays in the crafting grid)
 *
 * Properties:
 * - Nutrition: 2 hunger points (1 drumstick)
 * - Saturation: 0.4 (total 0.8)
 * - Effect: +30 seconds of Resistance I per cookie, up to 3 minutes (see {@link ClockworkWindUp})
 * - Eating Speed: Fast (1.6 seconds)
 *
 * Acquisition:
 * - Crafted (Clockwork Block mold)
 * - Watchmaker Camp chests
 * - Time Keeper trade
 */
public class ClockworkCookieItem extends Item {

    public ClockworkCookieItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide()) {
            windUpResistance(entity);
        }
        return result;
    }

    /**
     * Extend the remaining Resistance I time by one wind instead of resetting it.
     * Only Resistance I is wound. While a stronger Resistance is active, vanilla addEffect
     * keeps it, so the cookie may have no visible effect until that one runs out.
     */
    private static void windUpResistance(LivingEntity entity) {
        MobEffectInstance current = entity.getEffect(MobEffects.DAMAGE_RESISTANCE);
        int remaining = current != null && current.getAmplifier() == 0 ? current.getDuration() : 0;
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ClockworkWindUp.nextDuration(remaining), 0));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.chronodawn.clockwork_cookie.tooltip"));
        tooltipComponents.add(Component.translatable("item.chronodawn.clockwork_cookie.tooltip.cap"));
    }

    /**
     * Create default properties for Clockwork Cookie.
     *
     * @return Item properties with food configuration
     */
    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(2)              // 2 hunger points (1 drumstick)
                .saturationMod(0.4f)  // Saturation modifier (total: 2 * 0.4 = 0.8)
                .fast()  // Fast eating speed (1.6 seconds)
                .build();

        return new Properties()
                .food(foodProperties);
    }
}
