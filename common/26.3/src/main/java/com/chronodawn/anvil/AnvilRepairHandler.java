package com.chronodawn.anvil;

import com.chronodawn.items.artifacts.ChronobladeItem;
import com.chronodawn.items.artifacts.EchoingTimeBootsItem;
import com.chronodawn.items.artifacts.TimeTyrantMailItem;
import com.chronodawn.items.equipment.*;
import com.chronodawn.items.tools.SpatiallyLinkedPickaxeItem;
import com.chronodawn.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Anvil Repair Handler - Custom repair logic for Time Crystal
 *
 * Handles custom anvil repair mechanics for ChronoDawn equipment.
 * When using Time Crystal as repair material, restores 50% durability instead of vanilla's 25%.
 *
 * In NeoForge 1.21.2, custom repair tags cause "Unbound tags in registry" errors during
 * registry freeze. To work around this, we directly check if the item is a ChronoDawn
 * equipment item instead of relying on the REPAIRABLE component's tag.
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
     * Check if the given item is a ChronoDawn equipment that can be repaired with Time Crystal.
     *
     * This method directly checks the item class instead of relying on tags,
     * because custom tags cause "Unbound tags" errors in NeoForge 1.21.2.
     *
     * @param item The item to check
     * @return true if the item is a ChronoDawn equipment repairable with Time Crystal
     */
    public static boolean isChronoDawnRepairable(Item item) {
        // Tier 1 Clockstone Equipment
        if (item instanceof ClockstoneArmorItem) return true;
        if (item instanceof ClockstonePickaxeItem) return true;
        if (item instanceof ClockstoneSwordItem) return true;
        if (item instanceof ClockstoneAxeItem) return true;
        if (item instanceof ClockstoneShovelItem) return true;
        if (item instanceof ClockstoneHoeItem) return true;

        // Tier 2 Enhanced Clockstone Equipment
        if (item instanceof EnhancedClockstoneArmorItem) return true;
        if (item instanceof EnhancedClockstonePickaxeItem) return true;
        if (item instanceof EnhancedClockstoneSwordItem) return true;
        if (item instanceof EnhancedClockstoneAxeItem) return true;
        if (item instanceof EnhancedClockstoneShovelItem) return true;
        if (item instanceof EnhancedClockstoneHoeItem) return true;

        // Artifact Equipment
        if (item instanceof ChronobladeItem) return true;
        if (item instanceof TimeTyrantMailItem) return true;
        if (item instanceof EchoingTimeBootsItem) return true;
        if (item instanceof SpatiallyLinkedPickaxeItem) return true;

        return false;
    }

    /**
     * Calculate custom repair for Time Crystal material.
     * If the repair material is Time Crystal and the item is ChronoDawn equipment,
     * restore 50% durability.
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

        // Check if left item is damageable
        if (!leftInput.isDamageableItem()) {
            return null; // Not repairable
        }

        // Check if the item is a ChronoDawn equipment that can be repaired with Time Crystal
        // This bypasses the REPAIRABLE component check to avoid tag-related issues in NeoForge 1.21.2
        if (!isChronoDawnRepairable(leftInput.getItem())) {
            return null; // Not a ChronoDawn equipment
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
