package com.chronodawn.forge.anvil;

import com.chronodawn.ChronoDawn;
import com.chronodawn.anvil.AnvilRepairHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Anvil Repair Event Handler for Forge
 *
 * Listens to AnvilUpdateEvent and applies custom Time Crystal repair logic.
 * When Time Crystal is used as repair material, restores 50% durability instead of 25%.
 *
 * Reference: integrate-time-crystal-anvil-repair task
 */
@Mod.EventBusSubscriber(modid = ChronoDawn.MOD_ID)
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

            // Update repair cost NBT for next repair (1.20.1 uses NBT instead of DataComponents)
            CompoundTag leftTag = leftInput.getTag();
            int leftWorkCost = (leftTag != null && leftTag.contains("RepairCost")) ? leftTag.getInt("RepairCost") : 0;
            int newWorkCost = leftWorkCost * 2 + 1;
            CompoundTag resultTag = result.getOrCreateTag();
            resultTag.putInt("RepairCost", newWorkCost);

            // Set material cost (1 Time Crystal consumed)
            event.setMaterialCost(1);

            // Mark event as handled
            // Note: Don't cancel the event, just set the output
        }
    }
}
