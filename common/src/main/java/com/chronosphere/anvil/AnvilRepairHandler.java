package com.chronosphere.anvil;

import com.chronosphere.registry.ModItems;
import net.minecraft.world.item.ItemStack;

/**
 * Anvil Repair Handler - Custom repair logic for Time Crystal
 *
 * Handles custom anvil repair mechanics for Chronosphere equipment.
 * When using Time Crystal as repair material, restores 50% durability instead of vanilla's 25%.
 *
 * Usage:
 * - Call calculateTimeCrystalRepair() from anvil event handlers
 * - Returns repaired ItemStack if Time Crystal is used, null otherwise
 *
 * Reference: integrate-time-crystal-anvil-repair task
 */
public class AnvilRepairHandler {
    /**
     * Time Crystal repair percentage (50% of max durability)
     */
    public static final float TIME_CRYSTAL_REPAIR_PERCENTAGE = 0.5f;

    /**
     * Calculate custom repair for Time Crystal material.
     * If the repair material is Time Crystal, restore 50% durability.
     *
     * @param leftInput The item being repaired (left slot)
     * @param rightInput The repair material (right slot)
     * @return Repaired ItemStack if Time Crystal is used, null otherwise
     */
    public static ItemStack calculateTimeCrystalRepair(ItemStack leftInput, ItemStack rightInput) {
        // Check if repair material is Time Crystal
        if (!rightInput.is(ModItems.TIME_CRYSTAL.get())) {
            return null; // Not Time Crystal, use vanilla logic
        }

        // Check if left item is damageable and has a valid repair ingredient
        if (!leftInput.isDamageableItem()) {
            return null; // Not repairable
        }

        // Check if Time Crystal is valid repair material for this item
        if (!leftInput.getItem().isValidRepairItem(leftInput, rightInput)) {
            return null; // Time Crystal is not valid for this item
        }

        // Calculate repair amount (50% of max durability)
        int maxDamage = leftInput.getMaxDamage();
        int repairAmount = Math.round(maxDamage * TIME_CRYSTAL_REPAIR_PERCENTAGE);

        // Create repaired item
        ItemStack result = leftInput.copy();
        int currentDamage = result.getDamageValue();
        int newDamage = Math.max(0, currentDamage - repairAmount);
        result.setDamageValue(newDamage);

        return result;
    }

    /**
     * Calculate experience cost for Time Crystal repair.
     * Based on the amount of durability restored.
     *
     * @param leftInput The item being repaired
     * @param result The repaired item
     * @return Experience cost in levels
     */
    public static int calculateRepairCost(ItemStack leftInput, ItemStack result) {
        int durabilityRestored = leftInput.getDamageValue() - result.getDamageValue();
        int maxDamage = leftInput.getMaxDamage();

        // Base cost: 1 level per 10% durability restored, minimum 1
        int cost = Math.max(1, (durabilityRestored * 10) / maxDamage);

        // Add prior work penalty
        int leftWorkCost = leftInput.getOrDefault(net.minecraft.core.component.DataComponents.REPAIR_COST, 0);
        int totalCost = cost + leftWorkCost;

        return totalCost;
    }
}
