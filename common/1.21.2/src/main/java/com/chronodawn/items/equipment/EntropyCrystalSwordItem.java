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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

/**
 * Entropy Crystal Sword - Tier 2 specialized weapon.
 *
 * Stats parity with Clockstone Sword (iron-tier total damage), but applies
 * Entropy (DoT) on every successful hit for 5 seconds.
 *
 * 1.21.2 version: extends SwordItem, ctor (Tier, attackDamageBonus, attackSpeed, Properties).
 * Properties must include setId() to avoid runtime NPE.
 */
public class EntropyCrystalSwordItem extends SwordItem {
    private static final int ENTROPY_DURATION_TICKS = 100; // 5 seconds
    private static final int ENTROPY_AMPLIFIER = 0;

    public EntropyCrystalSwordItem(Properties properties) {
        super(EntropyCrystalTier.INSTANCE, 3, -2.4f, properties);
    }

    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(450)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "entropy_crystal_sword")));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.ENTROPY.get());
        target.addEffect(new MobEffectInstance(
            effectHolder,
            ENTROPY_DURATION_TICKS,
            ENTROPY_AMPLIFIER,
            false,
            true
        ));
        return super.hurtEnemy(stack, target, attacker);
    }
}
