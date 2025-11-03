package com.chronosphere.items;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Unstable Hourglass item - Material item with crafting risk.
 *
 * Used as crafting material for ultimate artifacts.
 * WARNING: Crafting this item triggers Reversed Resonance effect:
 * - Player receives Slowness IV for 30-60 seconds
 * - Nearby mobs receive Speed II for 30-60 seconds
 *
 * Properties:
 * - Max Stack Size: 1
 * - Crafting trigger is handled by CraftEventHandler (T122)
 *
 * Reference: data-model.md (Items → Materials → Unstable Hourglass)
 */
public class UnstableHourglassItem extends Item {
    public UnstableHourglassItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Unstable Hourglass item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1);
    }

    /**
     * Called when this item is crafted.
     * Triggers Reversed Resonance effect on the crafter.
     *
     * Reversed Resonance:
     * - Player receives Slowness IV for 30-60 seconds
     * - Nearby mobs (within 16 blocks) receive Speed II for 30-60 seconds
     *
     * @param stack The crafted item stack
     * @param level The world level
     * @param player The player who crafted the item
     */
    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);

        // Server-side only
        if (!level.isClientSide) {
            triggerReversedResonance(level, player);
        }
    }

    /**
     * Trigger Reversed Resonance effect.
     *
     * @param level The world level
     * @param player The player at the center of the effect
     */
    private void triggerReversedResonance(Level level, Player player) {
        // Effect duration: 30-60 seconds (600-1200 ticks)
        // Using 45 seconds (900 ticks) as a balanced middle ground
        final int EFFECT_DURATION = 900;
        final double MOB_EFFECT_RADIUS = 16.0;

        // Apply Slowness IV to player
        MobEffectInstance playerSlowness = new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                EFFECT_DURATION,
                3, // Amplifier 3 = Slowness IV
                false, // Not ambient
                true // Show particles (warning to player)
        );
        player.addEffect(playerSlowness);

        // Find all mobs within radius
        Vec3 playerPos = player.position();
        AABB boundingBox = new AABB(
                playerPos.x - MOB_EFFECT_RADIUS,
                playerPos.y - MOB_EFFECT_RADIUS,
                playerPos.z - MOB_EFFECT_RADIUS,
                playerPos.x + MOB_EFFECT_RADIUS,
                playerPos.y + MOB_EFFECT_RADIUS,
                playerPos.z + MOB_EFFECT_RADIUS
        );

        List<Mob> mobs = level.getEntitiesOfClass(
                Mob.class,
                boundingBox,
                mob -> mob.distanceToSqr(playerPos) <= MOB_EFFECT_RADIUS * MOB_EFFECT_RADIUS
        );

        // Apply Speed II to all nearby mobs
        for (Mob mob : mobs) {
            MobEffectInstance mobSpeed = new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    EFFECT_DURATION,
                    1, // Amplifier 1 = Speed II
                    false, // Not ambient
                    false // No particles (subtle effect)
            );
            mob.addEffect(mobSpeed);
        }
    }
}
