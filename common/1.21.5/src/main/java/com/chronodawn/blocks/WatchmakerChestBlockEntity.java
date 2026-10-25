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

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Holds the per-player slots and cooldowns of one Watchmaker's Chest, and animates its lid.
 *
 * <p>Deliberately not a {@link Container}: hoppers and other automation look for one, and
 * per-player slots have no meaning for them. Each player sees their own slots through
 * {@link #slotsFor(UUID)}.
 *
 * <p>1.21.5: CompoundTag getters return Optional, so loading uses the OrEmpty/Or getters.
 */
public class WatchmakerChestBlockEntity extends CooldownActivatorBlockEntity implements LidBlockEntity {
    public static final int SLOTS = 27;
    private static final String PLAYER_SLOTS_KEY = "PlayerSlots";
    private static final String PLAYER_KEY = "Player";
    // Same block event id as the vanilla chest, carrying the number of open screens.
    private static final int OPEN_COUNT_EVENT = 1;

    private final Map<UUID, NonNullList<ItemStack>> playerSlots = new HashMap<>();
    private final ChestLidController lidController = new ChestLidController();
    // Not saved: open screens close when the chunk unloads.
    private int openCount;

    public WatchmakerChestBlockEntity(BlockPos pos, BlockState state) {
        super(com.chronodawn.registry.ModBlockEntities.WATCHMAKER_CHEST.get(), pos, state);
    }

    /**
     * @return a live 27-slot view of the player's slots, created empty when the player has none.
     *         Changes write through to this block entity.
     */
    public Container slotsFor(UUID player) {
        return new PlayerSlots(playerSlots.computeIfAbsent(player, id -> NonNullList.withSize(SLOTS, ItemStack.EMPTY)));
    }

    /**
     * @return whether the player has no items in this chest
     */
    public boolean isEmptyFor(UUID player) {
        NonNullList<ItemStack> items = playerSlots.get(player);
        return items == null || items.stream().allMatch(ItemStack::isEmpty);
    }

    /**
     * Counts a screen opened on this chest. Every player sees the lid open, so the sound plays
     * for everyone nearby, and sculk sensors detect it. As with the vanilla chest, a spectator
     * neither moves the lid nor makes a sound.
     */
    public void startOpen(Player player) {
        if (level == null || player.isSpectator()) {
            return;
        }
        openCount++;
        if (openCount == 1) {
            playSound(level, SoundEvents.CHEST_OPEN);
            level.gameEvent(player, GameEvent.CONTAINER_OPEN, worldPosition);
        }
        signalOpenCount(level);
    }

    /**
     * Counts a screen on this chest closing.
     */
    public void stopOpen(Player player) {
        // A screen opened before a chunk reload is not counted, so do not go below zero.
        if (level == null || player.isSpectator() || openCount == 0) {
            return;
        }
        openCount--;
        if (openCount == 0) {
            playSound(level, SoundEvents.CHEST_CLOSE);
            level.gameEvent(player, GameEvent.CONTAINER_CLOSE, worldPosition);
        }
        signalOpenCount(level);
    }

    private void signalOpenCount(Level level) {
        level.blockEvent(worldPosition, getBlockState().getBlock(), OPEN_COUNT_EVENT, openCount);
    }

    private void playSound(Level level, SoundEvent sound) {
        level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
            sound, SoundSource.BLOCKS, 0.5f, level.random.nextFloat() * 0.1f + 0.9f);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == OPEN_COUNT_EVENT) {
            lidController.shouldBeOpen(param > 0);
            return true;
        }
        return super.triggerEvent(id, param);
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return lidController.getOpenness(partialTicks);
    }

    /**
     * Client ticker that moves the lid toward its open or closed position.
     */
    public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, WatchmakerChestBlockEntity chest) {
        chest.lidController.tickLid();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        // super saves the cooldowns through saveData.
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        Iterator<Map.Entry<UUID, NonNullList<ItemStack>>> entries = playerSlots.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<UUID, NonNullList<ItemStack>> entry = entries.next();
            // An emptied entry holds nothing to keep, so it is dropped to stop the map growing
            // with every player who ever opened the chest. The cooldown is kept separately.
            if (entry.getValue().stream().allMatch(ItemStack::isEmpty)) {
                entries.remove();
                continue;
            }
            CompoundTag playerTag = new CompoundTag();
            playerTag.putString(PLAYER_KEY, entry.getKey().toString());
            ContainerHelper.saveAllItems(playerTag, entry.getValue(), registries);
            list.add(playerTag);
        }
        tag.put(PLAYER_SLOTS_KEY, list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        // super loads the cooldowns through loadData.
        super.loadAdditional(tag, registries);
        playerSlots.clear();
        ListTag list = tag.getListOrEmpty(PLAYER_SLOTS_KEY);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag playerTag = list.getCompoundOrEmpty(i);
            UUID player;
            try {
                player = UUID.fromString(playerTag.getStringOr(PLAYER_KEY, ""));
            } catch (IllegalArgumentException e) {
                continue;
            }
            NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(playerTag, items, registries);
            playerSlots.put(player, items);
        }
    }

    /**
     * One player's slots as a container, for the menu and for filling from a loot table.
     */
    private final class PlayerSlots implements Container {
        private final NonNullList<ItemStack> items;

        private PlayerSlots(NonNullList<ItemStack> items) {
            this.items = items;
        }

        @Override
        public int getContainerSize() {
            return SLOTS;
        }

        @Override
        public boolean isEmpty() {
            return items.stream().allMatch(ItemStack::isEmpty);
        }

        @Override
        public ItemStack getItem(int slot) {
            return items.get(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
            if (!removed.isEmpty()) {
                setChanged();
            }
            return removed;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ContainerHelper.takeItem(items, slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            items.set(slot, stack);
            stack.limitSize(getMaxStackSize(stack));
            setChanged();
        }

        @Override
        public void setChanged() {
            WatchmakerChestBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return Container.stillValidBlockEntity(WatchmakerChestBlockEntity.this, player);
        }

        @Override
        public void clearContent() {
            items.clear();
            setChanged();
        }
    }
}
