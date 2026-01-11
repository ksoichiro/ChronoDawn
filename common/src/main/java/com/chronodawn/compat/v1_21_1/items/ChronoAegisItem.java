package com.chronodawn.items;

import com.chronodawn.registry.ModEffects;
import com.chronodawn.registry.ModSounds;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Chrono Aegis - Ultimate preparation item for Time Tyrant boss fight
 *
 * Crafted from 4 boss drop materials:
 * - Guardian Stone (Chronos Warden drop)
 * - Phantom Essence (Temporal Phantom drop)
 * - Colossus Gear (Clockwork Colossus drop)
 * - Entropy Core (Entropy Keeper drop)
 *
 * Usage: Right-click to activate 10-minute buff
 * Effects:
 * - Time Stop Resistance: Reduces Time Tyrant's Time Stop (Slowness V → Slowness II)
 * - Dimensional Anchor: Prevents Time Tyrant teleportation for 3s after teleport
 * - Temporal Shield: Reduces Time Tyrant's AoE damage by 50%
 * - Time Reversal Disruption: Reduces Time Tyrant's HP recovery (10% → 5%)
 * - Clarity: Auto-cleanses Slowness/Weakness/Mining Fatigue periodically
 *
 * Task: T238 [US3] Implement Chrono Aegis item
 */
public class ChronoAegisItem extends Item {
    public ChronoAegisItem(Properties properties) {
        super(properties
            .stacksTo(1)
            .rarity(Rarity.EPIC)
            .fireResistant()
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // Get Chrono Aegis effect as Holder
            MobEffect effect = ModEffects.CHRONO_AEGIS_BUFF.get();
            Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);

            // Check if player already has Chrono Aegis buff
            if (player.hasEffect(effectHolder)) {
                player.displayClientMessage(
                    Component.translatable("item.chronodawn.chrono_aegis.already_active"),
                    true
                );
                return InteractionResultHolder.fail(stack);
            }

            // Apply Chrono Aegis buff to all nearby players (32 block radius)
            final double BUFF_RANGE = 32.0;
            int playersAffected = 0;

            for (Player nearbyPlayer : level.players()) {
                // Check if player is in same dimension and within range
                if (nearbyPlayer.level().dimension() == player.level().dimension() &&
                    nearbyPlayer.distanceTo(player) <= BUFF_RANGE) {

                    // Apply buff
                    nearbyPlayer.addEffect(new MobEffectInstance(
                        effectHolder,
                        12000, // 10 minutes
                        0, // Level 0
                        false, // Not ambient
                        true, // Show particles
                        true // Show icon
                    ));

                    // Notify each player
                    if (nearbyPlayer == player) {
                        nearbyPlayer.displayClientMessage(
                            Component.translatable("item.chronodawn.chrono_aegis.activated"),
                            true
                        );
                    } else {
                        nearbyPlayer.displayClientMessage(
                            Component.translatable("item.chronodawn.chrono_aegis.received_from_ally", player.getName()),
                            true
                        );
                    }

                    playersAffected++;
                }
            }

            // Play activation sound at user's location (audible to all nearby players)
            level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                ModSounds.CHRONO_AEGIS_ACTIVATE.get(),
                SoundSource.PLAYERS,
                1.0f, 1.0f
            );

            // Consume item (single-use)
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.chronodawn.chrono_aegis.tooltip"));
        tooltipComponents.add(Component.translatable("item.chronodawn.chrono_aegis.tooltip.duration"));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Always show enchantment glint
    }
}
