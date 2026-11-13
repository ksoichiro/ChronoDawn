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
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Holds the per-player slots and cooldowns of one Watchmaker's Chest, and animates its lid.
 *
 * <p>Deliberately not a {@link Container}: hoppers and other automation look for one, and
 * per-player slots have no meaning for them. Each player sees their own slots through
 * {@link #slotsFor(UUID)}.
 *
 * <p>1.20.1: items are saved without registries and Container has no per-stack size limit.
 */
public class WatchmakerChestBlockEntity extends CooldownActivatorBlockEntity implements LidBlockEntity {
    public static final int SLOTS = 27;
    private static final String PLAYER_SLOTS_KEY = "PlayerSlots";
    private static final String PLAYER_KEY = "Player";
    // Same block event id as the vanilla chest, carrying the number of open screens.
    private static final int OPEN_COUNT_EVENT = 1;
    // As the vanilla chest: how far from the chest a player with its screen open is looked for,
    // and how many ticks pass between looks.
    private static final int RECHECK_RANGE = 5;
    private static final int RECHECK_DELAY_TICKS = 5;

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
     * @return the number of screens counted as open on this chest
     */
    public int getOpenCount() {
        return openCount;
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
        scheduleRecheck(level);
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
            pruneEmptySlots();
        }
        signalOpenCount(level);
    }

    /**
     * Counts the screens that are really open on this chest and corrects the open count. A screen
     * can close without {@link WatchmakerChestMenu#removed}, for example when its player is
     * removed or teleported, which would leave the lid open for good. Like the vanilla chest, a
     * scheduled tick calls this while the lid is open.
     */
    public void recheckOpen() {
        if (level == null) {
            return;
        }
        int actual = countOpenScreens(level);
        if (actual != openCount) {
            boolean closing = actual == 0;
            openCount = actual;
            if (closing) {
                playSound(level, SoundEvents.CHEST_CLOSE);
                level.gameEvent(null, GameEvent.CONTAINER_CLOSE, worldPosition);
                pruneEmptySlots();
            }
            signalOpenCount(level);
        }
        if (openCount > 0) {
            scheduleRecheck(level);
        }
    }

    private int countOpenScreens(Level level) {
        // As the vanilla chest, only players near the chest are looked at: a screen stays valid
        // only within reach of its block.
        AABB area = new AABB(worldPosition).inflate(RECHECK_RANGE);
        return level.getEntities(EntityTypeTest.forClass(Player.class), area,
            player -> !player.isSpectator() && player.containerMenu instanceof WatchmakerChestMenu menu
                && menu.isFor(this)).size();
    }

    private void scheduleRecheck(Level level) {
        level.scheduleTick(worldPosition, getBlockState().getBlock(), RECHECK_DELAY_TICKS);
    }

    /**
     * Drops the lists of players who hold nothing, so the map does not grow with every player who
     * ever opened the chest. The cooldown is kept separately. Only done while no screen is open,
     * because an open screen holds its player's list.
     */
    private void pruneEmptySlots() {
        if (openCount == 0) {
            playerSlots.values().removeIf(items -> items.stream().allMatch(ItemStack::isEmpty));
        }
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
    protected void saveAdditional(CompoundTag tag) {
        // super saves the cooldowns through saveData.
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (Map.Entry<UUID, NonNullList<ItemStack>> entry : playerSlots.entrySet()) {
            // An emptied entry holds nothing to keep. It is skipped, not removed, so that saving
            // has no side effect. The cooldown is kept separately.
            if (entry.getValue().stream().allMatch(ItemStack::isEmpty)) {
                continue;
            }
            CompoundTag playerTag = new CompoundTag();
            playerTag.putString(PLAYER_KEY, entry.getKey().toString());
            ContainerHelper.saveAllItems(playerTag, entry.getValue());
            list.add(playerTag);
        }
        tag.put(PLAYER_SLOTS_KEY, list);
    }

    @Override
    public void load(CompoundTag tag) {
        // super loads the cooldowns through loadData.
        super.load(tag);
        Map<UUID, NonNullList<ItemStack>> loaded = new HashMap<>();
        ListTag list = tag.getList(PLAYER_SLOTS_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag playerTag = list.getCompound(i);
            UUID player;
            try {
                player = UUID.fromString(playerTag.getString(PLAYER_KEY));
            } catch (IllegalArgumentException e) {
                continue;
            }
            NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(playerTag, items);
            loaded.put(player, items);
        }
        applyLoaded(loaded);
    }

    /**
     * Replaces the contents with the loaded ones. Each existing list is updated in place and not
     * replaced: an open screen holds its player's list, so a replaced list would leave the screen
     * on items the chest no longer has, and an item taken from it would be duplicated. This runs
     * for a chunk load and also for an operator's /data merge on a chest with screens open.
     */
    private void applyLoaded(Map<UUID, NonNullList<ItemStack>> loaded) {
        for (Map.Entry<UUID, NonNullList<ItemStack>> entry : playerSlots.entrySet()) {
            NonNullList<ItemStack> source = loaded.remove(entry.getKey());
            NonNullList<ItemStack> target = entry.getValue();
            for (int i = 0; i < SLOTS; i++) {
                target.set(i, source == null ? ItemStack.EMPTY : source.get(i));
            }
        }
        playerSlots.putAll(loaded);
        pruneEmptySlots();
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
            if (stack.getCount() > getMaxStackSize()) {
                stack.setCount(getMaxStackSize());
            }
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
