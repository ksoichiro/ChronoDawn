package com.chronodawn.items.consumables;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

/**
 * Baked Temporal Root - Cooked root vegetable with healing properties.
 *
 * Provides:
 * - Nutrition: Restores 6 hunger points (3 drumsticks)
 * - Regeneration I effect: Healing over time
 *
 * Properties:
 * - Nutrition: 6 hunger points (3 drumsticks)
 * - Saturation: 0.6 (total 3.6)
 * - Effect: Regeneration I for 5 seconds (100% chance)
 * - Eating Speed: Normal
 *
 * Acquisition:
 * - Smelt Temporal Root in furnace or smoker
 * - Recipe: Temporal Root → (smelting) → Baked Temporal Root
 *
 * Usage:
 * - Better than raw Temporal Root (double nutrition)
 * - Provides healing effect for combat/exploration
 * - Can be used in Temporal Root Stew recipe
 *
 * Note:
 * - This is the cooked variant of Temporal Root
 * - Significantly better than raw version
 * - Similar to baked potato but with healing
 *
 * Reference: WORK_NOTES.md (Crop 1: Temporal Root - Baked variant)
 * Task: T212 [US1] Create Baked Temporal Root item
 */
public class BakedTemporalRootItem extends Item {
    public BakedTemporalRootItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Baked Temporal Root.
     *
     * @return Item properties with food configuration
     */
    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(6)        // 6 hunger points (3 drumsticks)
                .saturationModifier(0.6f)  // Saturation modifier (total: 6 * 0.6 = 3.6)
                .build();

        return new Properties()
                .food(foodProperties)
                .component(DataComponents.CONSUMABLE,
                        Consumable.builder()
                                .onConsume(new ApplyStatusEffectsConsumeEffect(
                                    new MobEffectInstance(
                                            MobEffects.REGENERATION,  // Regeneration I
                                            5 * 20,                   // 5 seconds (20 ticks/second)
                                            0),                       // Amplifier 0 = Regeneration I
                                    1.0f))  // 100% chance to apply effect
                                .build())
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "baked_temporal_root")));
    }
}
