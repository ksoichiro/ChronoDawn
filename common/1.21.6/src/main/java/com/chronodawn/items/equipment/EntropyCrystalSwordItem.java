package com.chronodawn.items.equipment;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Entropy Crystal Sword - Tier 2 specialized weapon.
 *
 * Stats parity with Clockstone Sword (iron-tier total damage), but applies
 * Entropy (DoT) on every successful hit for 5 seconds.
 *
 * 1.21.5+ version: extends Item (SwordItem removed), uses Properties#sword()
 * data-component helper. hurtEnemy returns void.
 */
public class EntropyCrystalSwordItem extends Item {
    // Sword damage param; total in-game: 1.0 (player) + 2.5 (tier) + 3.0 = 6.5, parity with ClockstoneSword
    private static final float ATTACK_DAMAGE_BONUS = 3.0f;
    private static final float ATTACK_SPEED_MODIFIER = -2.4f;
    private static final int ENTROPY_DURATION_TICKS = 100; // 5 seconds
    private static final int ENTROPY_AMPLIFIER = 0;

    public EntropyCrystalSwordItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "entropy_crystal_sword");
        return new Properties()
                .stacksTo(1)
                .setId(ResourceKey.create(Registries.ITEM, itemId))
                .sword(EntropyCrystalTier.INSTANCE, ATTACK_DAMAGE_BONUS, ATTACK_SPEED_MODIFIER);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.ENTROPY.get());
        target.addEffect(new MobEffectInstance(
            effectHolder,
            ENTROPY_DURATION_TICKS,
            ENTROPY_AMPLIFIER,
            false,
            true
        ));
        super.hurtEnemy(stack, target, attacker);
    }
}
