package com.chronodawn.mixin;

import com.chronodawn.anvil.AnvilRepairHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for AnvilMenu to implement custom Time Crystal repair logic.
 *
 * Injects into createResult() method to handle Time Crystal repairs with 50% durability restoration.
 *
 * Reference: integrate-time-crystal-anvil-repair task
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends net.minecraft.world.inventory.ItemCombinerMenu {

    public AnvilMenuMixin(net.minecraft.world.inventory.MenuType<?> p_39773_, int p_39774_, net.minecraft.world.entity.player.Inventory p_39775_, net.minecraft.world.inventory.ContainerLevelAccess p_39776_, ItemCombinerMenuSlotDefinition slotDefinition) {
        // 1.21.2: ItemCombinerMenu constructor now requires ItemCombinerMenuSlotDefinition parameter
        super(p_39773_, p_39774_, p_39775_, p_39776_, slotDefinition);
    }

    /**
     * Inject into createResult() to handle custom Time Crystal repair.
     * Runs at the HEAD to check for Time Crystal repair before vanilla logic.
     */
    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void onCreateResult(CallbackInfo ci) {
        // Get input items from parent class fields
        ItemStack leftInput = this.inputSlots.getItem(0);
        ItemStack rightInput = this.inputSlots.getItem(1);

        // Try custom Time Crystal repair
        ItemStack result = AnvilRepairHandler.calculateTimeCrystalRepair(leftInput, rightInput);

        if (result != null) {
            // Time Crystal repair succeeded
            // Calculate experience cost
            int experienceCost = AnvilRepairHandler.calculateRepairCost(leftInput, result);

            // Update repair cost component for next repair
            int leftWorkCost = leftInput.getOrDefault(DataComponents.REPAIR_COST, 0);
            int newWorkCost = leftWorkCost * 2 + 1;
            result.set(DataComponents.REPAIR_COST, newWorkCost);

            // Set result and cost
            this.resultSlots.setItem(0, result);
            ((AnvilMenuAccessor)this).getCost().set(experienceCost);

            // Set material consumption count to 1 (only consume 1 Time Crystal)
            ((AnvilMenuAccessor)this).setRepairItemCountCost(1);

            // Broadcast changes
            this.broadcastChanges();

            // Cancel vanilla logic
            ci.cancel();
        }
    }
}
