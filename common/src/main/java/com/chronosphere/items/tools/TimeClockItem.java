package com.chronosphere.items.tools;

import com.chronosphere.core.time.MobAICanceller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Time Clock item - Utility item for cancelling mob AI attacks.
 *
 * When used, forcibly cancels the next attack AI routine of all mobs within radius.
 *
 * Properties:
 * - Max Stack Size: 1
 * - Cooldown: 10 seconds (200 ticks)
 * - Effect Radius: 8 blocks
 *
 * Crafting:
 * - Requires Enhanced Clockstone (obtained from Desert Clock Tower)
 *
 * Reference: data-model.md (Items → Tools → Time Clock)
 */
public class TimeClockItem extends Item {
    /**
     * Cooldown duration in ticks (10 seconds = 200 ticks).
     */
    public static final int COOLDOWN_TICKS = 200;

    /**
     * Effect radius in blocks.
     */
    public static final double EFFECT_RADIUS = 8.0;

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
     * Cancels the next attack AI routine of all mobs within EFFECT_RADIUS.
     * Applies cooldown to prevent spam usage.
     *
     * @param level The world level
     * @param player The player using the item
     * @param hand The hand holding the item
     * @return InteractionResultHolder indicating success or failure
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Check if player is on cooldown
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemStack);
        }

        // Server-side logic only
        if (!level.isClientSide) {
            // Cancel attack AI for all mobs within radius
            MobAICanceller.cancelAttackAI(level, player.position(), EFFECT_RADIUS);
        }

        // Apply cooldown
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemStack);
    }
}
