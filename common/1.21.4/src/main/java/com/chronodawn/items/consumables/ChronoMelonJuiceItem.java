package com.chronodawn.items.consumables;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
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
 * Chrono Melon Juice (時のメロンジュース)
 *
 * A drinkable beverage made from Chrono Melon Slices, providing Speed effect.
 *
 * Recipe: 4x Chrono Melon Slices + 1x Glass Bottle
 * Effect: Speed I for 60 seconds
 */
public class ChronoMelonJuiceItem extends Item {

    public ChronoMelonJuiceItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(4)
                .saturationModifier(0.4f)
                .build();

        return new Properties()
                .food(foodProperties)
                .component(DataComponents.CONSUMABLE,
                        Consumable.builder()
                                .onConsume(new ApplyStatusEffectsConsumeEffect(
                                        new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60 * 20, 0),
                                        1.0f))
                                .build())
                .craftRemainder(Items.GLASS_BOTTLE)
                .stacksTo(16)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chrono_melon_juice")));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        super.finishUsingItem(stack, level, entity);

        if (entity instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        }

        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        } else {
            if (entity instanceof Player player && !player.getAbilities().instabuild) {
                ItemStack bottleStack = new ItemStack(Items.GLASS_BOTTLE);
                if (!player.getInventory().add(bottleStack)) {
                    player.drop(bottleStack, false);
                }
            }
            return stack;
        }
    }
}
