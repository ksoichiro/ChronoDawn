package com.chronodawn.items.tools;

import com.chronodawn.core.time.MobAICanceller;
import com.chronodawn.entities.bosses.TimeTyrantEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Time Clock item - Utility item for cancelling mob AI attacks.
 *
 * When used, forcibly cancels the next attack AI routine of all mobs within radius.
 *
 * Special Effect vs Time Tyrant:
 * - When used on Time Tyrant, applies powerful weakening effect
 * - 10 seconds: Defense 15→5, Speed 50% reduction
 * - Limited to 1 use per boss phase (3 times total)
 * - Cooldown: 30 seconds (600 ticks) for boss effect
 *
 * Properties:
 * - Max Stack Size: 1
 * - Normal Cooldown: 10 seconds (200 ticks)
 * - Boss Cooldown: 30 seconds (600 ticks)
 * - Effect Radius: 8 blocks (normal use)
 * - Target Range: 32 blocks (boss use)
 *
 * Crafting:
 * - Requires Enhanced Clockstone (obtained from Desert Clock Tower)
 *
 * Reference: data-model.md (Items → Tools → Time Clock)
 * Reference: T171f - Boss Battle Balance Enhancement
 */
public class TimeClockItem extends Item {
    /**
     * Normal cooldown duration in ticks (10 seconds = 200 ticks).
     */
    public static final int COOLDOWN_TICKS = 200;

    /**
     * Boss effect cooldown duration in ticks (30 seconds = 600 ticks).
     */
    public static final int BOSS_COOLDOWN_TICKS = 600;

    /**
     * Effect radius in blocks for normal use.
     */
    public static final double EFFECT_RADIUS = 8.0;

    /**
     * Maximum range for targeting Time Tyrant.
     */
    public static final double TARGET_RANGE = 32.0;

    public TimeClockItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Time Clock item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1);
    }

    /**
     * Called when the player right-clicks with the Time Clock.
     *
     * Priority 1: Check if player is targeting Time Tyrant within range
     *   - If yes: Apply special boss weakening effect
     *   - Limited to 1 use per phase, 30s cooldown
     *
     * Priority 2: Normal use - cancel attack AI for all mobs within radius
     *   - 10s cooldown
     *
     * @param level The world level
     * @param player The player using the item
     * @param hand The hand holding the item
     * @return InteractionResult indicating success or failure
     */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Check if player is on cooldown
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResult.FAIL;
        }

        // Server-side logic only
        if (!level.isClientSide) {
            // Priority 1: Check for Time Tyrant targeting
            TimeTyrantEntity targetedTyrant = getTargetedTimeTyrant(level, player);

            if (targetedTyrant != null) {
                // Try to apply boss weakening effect
                boolean success = targetedTyrant.applyTimeClockWeakening();

                if (success) {
                    // Inform player of success
                    player.displayClientMessage(
                        Component.translatable("message.chronodawn.time_clock_boss_success"),
                        true
                    );

                    // Apply longer cooldown for boss effect
                    player.getCooldowns().addCooldown(this, BOSS_COOLDOWN_TICKS);
                    return InteractionResult.SUCCESS;
                } else {
                    // Already used in current phase
                    player.displayClientMessage(
                        Component.translatable("message.chronodawn.time_clock_boss_limit"),
                        true
                    );
                    return InteractionResult.FAIL;
                }
            }

            // Priority 2: Normal use - cancel attack AI for all mobs within radius
            MobAICanceller.cancelAttackAI(level, player.position(), EFFECT_RADIUS);

            // Apply normal cooldown
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        } else {
            // Client-side: apply normal cooldown
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Get the Time Tyrant entity that the player is targeting.
     *
     * Uses raycasting from player's eye position to find Time Tyrant within range.
     *
     * @param level The world level
     * @param player The player
     * @return TimeTyrantEntity if found, null otherwise
     */
    private TimeTyrantEntity getTargetedTimeTyrant(Level level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(TARGET_RANGE));

        // Create AABB for entity search
        AABB searchBox = new AABB(eyePos, endPos).inflate(2.0);

        // Find all Time Tyrant entities in range
        List<TimeTyrantEntity> tyrants = level.getEntitiesOfClass(
            TimeTyrantEntity.class,
            searchBox,
            entity -> entity.distanceToSqr(player) <= TARGET_RANGE * TARGET_RANGE
        );

        if (tyrants.isEmpty()) {
            return null;
        }

        // Find the closest Time Tyrant to the player's look direction
        TimeTyrantEntity closestTyrant = null;
        double closestDistance = Double.MAX_VALUE;

        for (TimeTyrantEntity tyrant : tyrants) {
            Vec3 tyrantPos = tyrant.position().add(0, tyrant.getBbHeight() / 2, 0);
            Vec3 toTyrant = tyrantPos.subtract(eyePos).normalize();
            double dotProduct = lookVec.dot(toTyrant);

            // Check if tyrant is roughly in front of player (dot product > 0.9 = ~25 degree cone)
            if (dotProduct > 0.9) {
                double distance = eyePos.distanceToSqr(tyrantPos);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestTyrant = tyrant;
                }
            }
        }

        return closestTyrant;
    }
}
