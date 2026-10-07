package com.chronodawn.items.equipment;

import com.chronodawn.items.shield.ChronoShieldEffectHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Duration multiplier for time-themed debuffs (Slowness / Weakness / Mining Fatigue) from
 * ChronoDawn shields and armor sets. Sources stack multiplicatively:
 * <ul>
 *   <li>ChronoDawn shield held in either hand: x0.5</li>
 *   <li>Full Enhanced Clockstone Armor: x0.5</li>
 *   <li>Full Clockstone Armor: x0.75, Slowness only</li>
 * </ul>
 * The armor bonuses give the Clockstone sets a role inside Chrono Dawn, where mobs and bosses
 * apply these debuffs often, without raising their raw stats above the matching vanilla tier.
 */
public final class TimeDebuffResistance {
    private TimeDebuffResistance() {}

    static final float SHIELD_MULTIPLIER = 0.5f;
    static final float ENHANCED_CLOCKSTONE_SET_MULTIPLIER = 0.5f;
    static final float CLOCKSTONE_SET_SLOWNESS_MULTIPLIER = 0.75f;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    /**
     * @param target   entity receiving the debuff
     * @param slowness true when the debuff is Slowness (the only effect the Clockstone set reduces)
     * @return multiplier in (0, 1] to apply to the debuff duration
     */
    public static float durationMultiplier(LivingEntity target, boolean slowness) {
        float multiplier = 1.0f;
        if (ChronoShieldEffectHandler.isHoldingChronoShield(target)) {
            multiplier *= SHIELD_MULTIPLIER;
        }
        if (isWearingFullSet(target, EnhancedClockstoneArmorItem.class)) {
            multiplier *= ENHANCED_CLOCKSTONE_SET_MULTIPLIER;
        } else if (slowness && isWearingFullSet(target, ClockstoneArmorItem.class)) {
            multiplier *= CLOCKSTONE_SET_SLOWNESS_MULTIPLIER;
        }
        return multiplier;
    }

    /**
     * Shortens a duration by the given multiplier, keeping at least 1 tick so the effect still
     * applies. Infinite durations are left unchanged.
     */
    public static int shortenDuration(int duration, float multiplier, boolean infinite) {
        if (infinite || multiplier >= 1.0f) return duration;
        return Math.max(1, (int) (duration * multiplier));
    }

    public static boolean isWearingFullSet(LivingEntity entity, Class<?> armorClass) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!armorClass.isInstance(entity.getItemBySlot(slot).getItem())) {
                return false;
            }
        }
        return true;
    }
}
