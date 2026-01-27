package com.chronodawn.items.consumables;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

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
 * Timeless Mushroom Soup (時知らずのキノコスープ)
 *
 * A simple soup made from Timeless Mushrooms, providing Night Vision effect.
 *
 * Recipe: 2x Timeless Mushrooms + 1x Bowl
 * Effect: Night Vision for 60 seconds
 */
public class TimelessMushroomSoupItem extends Item {

    public TimelessMushroomSoupItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(6)
                .saturationModifier(0.6f)
                .build();

        return new Properties()
                .food(foodProperties)
                .component(DataComponents.CONSUMABLE,
                        Consumable.builder()
                                .onConsume(new ApplyStatusEffectsConsumeEffect(
                                        new MobEffectInstance(MobEffects.NIGHT_VISION, 60 * 20, 0),
                                        1.0f))
                                .build())
                .craftRemainder(Items.BOWL)
                .stacksTo(1)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "timeless_mushroom_soup")));
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
