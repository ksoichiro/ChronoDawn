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
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.component.Consumable;

/**
 * Glistening Chrono Melon (きらめく時のメロン)
 *
 * A premium melon slice infused with gold, providing high saturation and absorption.
 *
 * Recipe: 1x Chrono Melon Slice + 8x Gold Nuggets
 * Effect: Absorption I for 30 seconds
 */
public class GlisteningChronoMelonItem extends Item {

    public GlisteningChronoMelonItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(2)
                .saturationModifier(1.2f)
                .build();

        return new Properties()
                .food(foodProperties)
                .component(DataComponents.CONSUMABLE,
                        Consumable.builder()
                                .onConsume(new ApplyStatusEffectsConsumeEffect(
                                        new MobEffectInstance(MobEffects.ABSORPTION, 30 * 20, 0),
                                        1.0f))
                                .build())
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "glistening_chrono_melon")));
    }
}
