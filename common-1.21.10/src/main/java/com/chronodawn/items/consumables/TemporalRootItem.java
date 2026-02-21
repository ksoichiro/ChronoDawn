package com.chronodawn.items.consumables;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;

/**
 * Temporal Root - Raw root vegetable that can be planted or eaten.
 *
 * Provides:
 * - Nutrition: Restores 3 hunger points (1.5 drumsticks)
 * - No special effects in raw form
 *
 * Properties:
 * - Nutrition: 3 hunger points (1.5 drumsticks)
 * - Saturation: 0.6 (total 1.8)
 * - Effect: None (raw state)
 * - Eating Speed: Normal
 * - Plantable: Yes (can be planted on farmland like carrots)
 *
 * Usage:
 * - Can be eaten raw for basic nutrition
 * - Can be planted on farmland to grow Temporal Root crops
 * - Can be cooked into Baked Temporal Root for better nutrition + Regen effect
 *
 * Note:
 * - This item serves both as food and seed (like carrots/potatoes)
 * - Extends BlockItem but uses item.* description prefix since item name differs from crop block
 *
 * Reference: WORK_NOTES.md (Crop 1: Temporal Root)
 * Task: T212 [US1] Create Temporal Root item
 */
public class TemporalRootItem extends BlockItem {
    public TemporalRootItem(Properties properties) {
        super(ModBlocks.TEMPORAL_ROOT.get(), properties);
    }

    /**
     * Create default properties for Temporal Root.
     *
     * @return Item properties with food configuration
     */
    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(3)        // 3 hunger points (1.5 drumsticks)
                .saturationModifier(0.6f)  // Saturation modifier (total: 3 * 0.6 = 1.8)
                .build();

        return new Properties()
                .food(foodProperties)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_root")));
    }
}
