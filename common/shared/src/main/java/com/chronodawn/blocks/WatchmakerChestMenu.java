/*
 * Copyright (C) 2025 ksoichiro
 *
 * This file is part of Chrono Dawn.
 *
 * Chrono Dawn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Chrono Dawn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chrono Dawn. If not, see <https://www.gnu.org/licenses/>.
 */
package com.chronodawn.blocks;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Take-only menu over one player's Watchmaker's Chest slots.
 *
 * <p>Uses the vanilla 3-row chest menu type with the same slot layout as the vanilla chest
 * menu, so the client shows the plain chest screen without any client-side code. The server
 * refuses every item put into a chest slot, by click, drag, number-key swap or shift-click.
 */
public class WatchmakerChestMenu extends AbstractContainerMenu {
    private static final int ROWS = 3;
    private static final int COLUMNS = 9;
    private static final int INVENTORY_ROWS = 3;
    private static final int SLOT_SIZE = 18;
    private static final int LEFT = 8;
    private static final int CHEST_TOP = 18;
    // The vanilla chest menu's player inventory rows, for 3 chest rows.
    private static final int INVENTORY_TOP = 85;
    private static final int HOTBAR_TOP = 143;

    private final WatchmakerChestBlockEntity chest;

    public WatchmakerChestMenu(int containerId, Inventory playerInventory, Container slots,
                               WatchmakerChestBlockEntity chest) {
        super(MenuType.GENERIC_9x3, containerId);
        this.chest = chest;
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                addSlot(new TakeOnlySlot(slots, column + row * COLUMNS, LEFT + column * SLOT_SIZE,
                    CHEST_TOP + row * SLOT_SIZE));
            }
        }
        for (int row = 0; row < INVENTORY_ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                addSlot(new Slot(playerInventory, column + row * COLUMNS + COLUMNS, LEFT + column * SLOT_SIZE,
                    INVENTORY_TOP + row * SLOT_SIZE));
            }
        }
        for (int column = 0; column < COLUMNS; column++) {
            addSlot(new Slot(playerInventory, column, LEFT + column * SLOT_SIZE, HOTBAR_TOP));
        }
    }

    /**
     * Moves a shift-clicked chest stack into the player's inventory. Shift-clicking the
     * inventory moves nothing, as the chest takes no items.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index >= WatchmakerChestBlockEntity.SLOTS) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (!moveItemStackTo(stack, WatchmakerChestBlockEntity.SLOTS, slots.size(), true)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(chest, player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        chest.stopOpen(player);
    }

    /** A chest slot that items can be taken from but never put into. */
    private static final class TakeOnlySlot extends Slot {
        private TakeOnlySlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
