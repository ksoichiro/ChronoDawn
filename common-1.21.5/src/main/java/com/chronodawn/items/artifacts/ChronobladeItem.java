package com.chronodawn.items.artifacts;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;

/**
 * Chronoblade - Ultimate Time-Manipulating Weapon
 *
 * The ultimate artifact crafted from the fragments of the defeated Time Tyrant.
 * Wields the power of time itself to disrupt enemy attack patterns.
 *
 * Properties:
 * - Attack Damage: 8.0 (base 1.0 + 7.0 bonus, stronger than netherite)
 * - Attack Speed: -2.4 (standard sword speed)
 * - Durability: 2000 uses (superior to diamond/netherite)
 * - Enchantability: 18
 *
 * Special Ability - AI Skip:
 * - 25% chance to skip enemy's next attack AI on successful hit
 * - When triggered, enemy's next attack action is cancelled completely
 * - Provides a one-sided attack opportunity during the AI skip window
 * - Extremely powerful in combat, allowing players to chain attacks safely
 *
 * Crafting Recipe:
 * - Fragment of Stasis Core x3
 * - Eye of Chronos x1
 * - Enhanced Clockstone x2
 * - Unstable Hourglass x1
 *
 * Lore:
 * Forged from the shattered core of Time Tyrant, this blade cuts through not just
 * flesh and bone, but the very fabric of temporal causality. Those struck by it
 * find their next actions stolen from the timeline itself.
 *
 * Reference: T148-T152 - Create Chronoblade with AI skip ability
 *
 * Note: In 1.21.5, SwordItem has been removed. Items now use data components
 * and Item.Properties helper methods instead of inheritance.
 */
public class ChronobladeItem extends Item {
    /**
     * AI skip chance (25% = 0.25). Public for testing.
     */
    public static final float AI_SKIP_CHANCE = 0.25f;

    // Attack damage: 8.0 total (1.0 base + 7.0 bonus)
    // Netherite sword: 8.0
    private static final float ATTACK_DAMAGE_BONUS = 7.0f;

    // Attack speed: 1.6 (base 4.0 - 2.4 modifier)
    private static final float ATTACK_SPEED_MODIFIER = -2.4f;

    // Durability cost when hitting entities
    private static final int DURABILITY_COST_PER_HIT = 1;

    // Shield disable duration in seconds (longer than normal sword)
    private static final float SHIELD_DISABLE_SECONDS = 6.0f;

    public ChronobladeItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Chronoblade item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chronoblade");

        return new Properties()
                .stacksTo(1)
                .durability(2000)
                .rarity(Rarity.EPIC)
                .setId(ResourceKey.create(Registries.ITEM, itemId))
                // Mark as sword-type item
                .sword()
                // Add weapon component for attack behavior
                .component(DataComponents.WEAPON, new Weapon(
                    DURABILITY_COST_PER_HIT,
                    SHIELD_DISABLE_SECONDS
                ))
                // Add attribute modifiers for attack damage and speed
                .component(DataComponents.ATTRIBUTE_MODIFIERS, createAttributeModifiers(itemId));
    }

    /**
     * Create attribute modifiers for attack damage and speed.
     */
    private static ItemAttributeModifiers createAttributeModifiers(ResourceLocation itemId) {
        return ItemAttributeModifiers.builder()
                .add(
                    Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(
                        itemId,
                        ATTACK_DAMAGE_BONUS,
                        AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
                )
                .add(
                    Attributes.ATTACK_SPEED,
                    new AttributeModifier(
                        itemId,
                        ATTACK_SPEED_MODIFIER,
                        AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    /**
     * Called when the player successfully attacks an entity with this sword.
     * Applies AI skip effect with 25% chance.
     *
     * @param stack The item stack
     * @param target The entity being attacked
     * @param attacker The entity attacking (player)
     * @return true to allow the attack
     */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Apply AI skip effect with 25% chance
        if (attacker.level().getRandom().nextFloat() < AI_SKIP_CHANCE) {
            ChronobladeAISkipHandler.applyAISkip(target);
        }

        return super.hurtEnemy(stack, target, attacker);
    }
}
