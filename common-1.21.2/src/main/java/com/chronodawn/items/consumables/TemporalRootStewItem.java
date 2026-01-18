package com.chronodawn.items.consumables;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;

/**
 * Temporal Root Stew (時の根菜シチュー)
 *
 * A hearty stew combining Baked Temporal Root and Timeless Mushroom.
 * Provides high nutrition and strong regeneration effect.
 *
 * Recipe: 1x Baked Temporal Root + 1x Timeless Mushroom + 1x Bowl
 * Effect: Regeneration II for 10 seconds
 */
public class TemporalRootStewItem extends Item {

    public TemporalRootStewItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(8)
                .saturationModifier(0.6f)
                .build();

        return new Properties()
                .food(foodProperties)
                .component(DataComponents.CONSUMABLE,
                        Consumable.builder()
                                .onConsume(new ApplyStatusEffectsConsumeEffect(
                                        new MobEffectInstance(MobEffects.REGENERATION, 10 * 20, 1),
                                        1.0f))
                                .build())
                .craftRemainder(Items.BOWL)
                .stacksTo(1);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            return new ItemStack(Items.BOWL);
        }

        return result.isEmpty() ? new ItemStack(Items.BOWL) : result;
    }
}
