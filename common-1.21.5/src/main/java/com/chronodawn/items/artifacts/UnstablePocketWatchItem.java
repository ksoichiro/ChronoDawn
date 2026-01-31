package com.chronodawn.items.artifacts;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Unstable Pocket Watch - Speed Effect Swapping Utility
 *
 * Properties:
 * - Stack Size: 1
 * - Rarity: Epic
 *
 * Special Ability - Temporal Flux:
 * - Swaps speed effects between player and nearby mobs
 * - 30 second cooldown
 *
 * Reference: T167-171
 */
public class UnstablePocketWatchItem extends Item {
    public UnstablePocketWatchItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .rarity(net.minecraft.world.item.Rarity.EPIC)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "unstable_pocket_watch")));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            // Effect swapping logic handled by UnstablePocketWatchSwapHandler
            UnstablePocketWatchSwapHandler.swapEffects(player, level);
        }

        // In 1.21.2, InteractionResult no longer takes ItemStack parameter
        return InteractionResult.SUCCESS;
    }
}
