package com.chronodawn.neoforge.anvil;

import com.chronodawn.ChronoDawn;
import com.chronodawn.anvil.AnvilRepairHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

/**
 * Anvil Repair Event Handler for NeoForge
 *
 * Listens to AnvilUpdateEvent and applies custom Time Crystal repair logic.
 * When Time Crystal is used as repair material, restores 50% durability instead of 25%.
 *
 * Reference: integrate-time-crystal-anvil-repair task
 */
@EventBusSubscriber(modid = ChronoDawn.MOD_ID)
public class AnvilRepairEventHandler {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack leftInput = event.getLeft();
        ItemStack rightInput = event.getRight();

        // Try custom Time Crystal repair
        ItemStack result = AnvilRepairHandler.calculateTimeCrystalRepair(leftInput, rightInput);

        if (result != null) {
            // Time Crystal repair succeeded
            event.setOutput(result);

            // Calculate experience cost
            int cost = AnvilRepairHandler.calculateRepairCost(leftInput, result);
            event.setCost(cost);

            // Update repair cost component for next repair
            int leftWorkCost = leftInput.getOrDefault(DataComponents.REPAIR_COST, 0);
            int newWorkCost = leftWorkCost * 2 + 1;
            result.set(DataComponents.REPAIR_COST, newWorkCost);

            // Set material cost (1 Time Crystal consumed)
            event.setMaterialCost(1);

            // Mark event as handled
            // Note: Don't cancel the event, just set the output
        }
    }
}
