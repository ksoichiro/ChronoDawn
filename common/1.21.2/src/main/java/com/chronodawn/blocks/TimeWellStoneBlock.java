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

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;
import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.core.wayside.PlayerCooldownTracker;
import com.chronodawn.core.wayside.TimeWellOutcome;
import com.chronodawn.core.wayside.WellQueue;
import com.chronodawn.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The marked stone in the rim of a Time Well. It watches the water source beside it: an item
 * a player throws in is answered after a short delay, exchanging one item of the stack once
 * per cooldown for each player. Everything not exchanged rises back out.
 */
public class TimeWellStoneBlock extends MossyTemporalCobblestoneBlock implements EntityBlock {
    private static final String EXCHANGE_LOOT_TABLE_PREFIX = "gameplay/time_well/";
    /**
     * Entity tag on a waiting item. Tags are saved with the entity while the queue is not, so
     * after a chunk reload the well can still find and return items it was holding.
     */
    public static final String WAITING_TAG = "chronodawn.time_well_waiting";
    // Thrower of an orphaned waiting item whose player cannot be resolved. No player has this
    // UUID, so the answer finds no thrower and returns the item.
    private static final UUID UNKNOWN_THROWER = new UUID(0L, 0L);
    // Just above the one-block rim, so rising items clear it on their way out.
    private static final double RISE_HEIGHT = 2.2;
    private static final double RISE_HORIZONTAL_SPEED = 0.25;
    private static final double RISE_VERTICAL_SPEED = 0.35;
    private static final double AIM_RANGE = 16.0;
    private static final int GLOW_PARTICLES = 12;
    private static final int END_ROD_PARTICLES = 10;
    private static final double END_ROD_SPEED = 0.05;
    private static final int BUBBLE_PARTICLES = 6;

    /** One answer and the cooldown left when it was refused, so the message can show it. */
    private record Answer(TimeWellOutcome outcome, long remainingTicks) {
    }

    public TimeWellStoneBlock(Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.MOSSY_COBBLESTONE)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_well_stone")));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TimeWellStoneBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.TIME_WELL_STONE.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (tickLevel instanceof ServerLevel serverLevel && blockEntity instanceof TimeWellStoneBlockEntity well) {
                tick(serverLevel, pos, well);
            }
        };
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        // Read the queue before super.onRemove removes the block entity with it.
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel
                && level.getBlockEntity(pos) instanceof TimeWellStoneBlockEntity well) {
            releaseWaiting(serverLevel, well);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * Turns the waiting items back into plain items when the well can no longer answer them,
     * so they can be picked up instead of being stranded until they despawn.
     */
    static void releaseWaiting(ServerLevel level, TimeWellStoneBlockEntity well) {
        // Long.MAX_VALUE makes every entry due, which drains the whole queue.
        for (WellQueue.Entry entry : well.queue().due(Long.MAX_VALUE)) {
            if (level.getEntity(entry.item()) instanceof ItemEntity item && item.isAlive()) {
                item.setDefaultPickUpDelay();
                item.removeTag(WAITING_TAG);
            }
        }
    }

    /**
     * @return the first horizontal neighbour of the stone holding a water source, or null
     */
    public static BlockPos findWater(Level level, BlockPos stonePos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos candidate = stonePos.relative(direction);
            if (isWaterSource(level, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean isWaterSource(Level level, BlockPos pos) {
        FluidState fluid = level.getFluidState(pos);
        return fluid.is(FluidTags.WATER) && fluid.isSource();
    }

    /**
     * Watches the water for new wishes and answers the ones that are due.
     */
    public static void tick(ServerLevel level, BlockPos pos, TimeWellStoneBlockEntity well) {
        BlockPos water = well.cachedWater();
        if (water == null || !isWaterSource(level, water)) {
            water = findWater(level, pos);
            well.cacheWater(water);
        }
        long gameTime = level.getGameTime();
        WellQueue queue = well.queue();
        // Without water no new wish is taken, but items already waiting are still answered, so
        // scooping the water out during the wait does not strand them.
        if (water != null) {
            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(water))) {
                if (queue.contains(item.getUUID())) {
                    continue;
                }
                // Items dropped by mobs, hoppers or dispensers have no player owner and are left alone.
                if (item.getOwner() instanceof Player thrower) {
                    markWaiting(well, item, thrower, gameTime);
                } else if (isWaiting(item)) {
                    // Left over from a wait the well lost on a chunk reload. It cannot be picked up,
                    // so it must be answered again, as returned to a thrower who is gone.
                    markWaiting(well, item, UNKNOWN_THROWER, gameTime);
                }
            }
        }
        List<WellQueue.Entry> due = queue.due(gameTime);
        if (due.isEmpty()) {
            return;
        }
        Map<Player, Answer> answers = new LinkedHashMap<>();
        for (WellQueue.Entry entry : due) {
            // Looked up by UUID, so an item pushed out of the water by the current is still answered.
            if (!(level.getEntity(entry.item()) instanceof ItemEntity item) || !item.isAlive()) {
                continue;
            }
            Player thrower = level.getPlayerByUUID(entry.thrower());
            Answer answer = answerWithRemaining(level, pos, item, thrower);
            if (thrower != null) {
                answers.merge(thrower, answer, TimeWellStoneBlock::moreImportant);
            }
        }
        // One message per player per tick, so several items answered together do not flicker.
        answers.forEach((player, answer) -> sendMessage(player, answer));
    }

    private static Answer moreImportant(Answer a, Answer b) {
        return TimeWellOutcome.mostImportant(a.outcome(), b.outcome()) == a.outcome() ? a : b;
    }

    private static void sendMessage(Player player, Answer answer) {
        String key = answer.outcome().messageKey();
        if (key == null) {
            return;
        }
        Component message = answer.outcome() == TimeWellOutcome.COOLDOWN
            ? Component.translatable(key, PlayerCooldownTracker.toMinutesRoundedUp(answer.remainingTicks()))
            : Component.translatable(key);
        player.displayClientMessage(message, true);
    }

    /**
     * Marks an item as a wish: it cannot be picked up or merged while it waits, and its
     * answer comes after {@link WellQueue#ANSWER_DELAY_TICKS}.
     */
    public static void markWaiting(TimeWellStoneBlockEntity well, ItemEntity item, Player thrower, long gameTime) {
        markWaiting(well, item, thrower.getUUID(), gameTime);
    }

    private static void markWaiting(TimeWellStoneBlockEntity well, ItemEntity item, UUID thrower, long gameTime) {
        // Never-pick-up items do not merge either, so the queued entity keeps its UUID.
        item.setNeverPickUp();
        item.addTag(WAITING_TAG);
        well.queue().offer(item.getUUID(), thrower, gameTime);
    }

    /**
     * @return whether the item carries {@link #WAITING_TAG}, set while it waits for an answer
     */
    public static boolean isWaiting(ItemEntity item) {
        return item.getTags().contains(WAITING_TAG);
    }

    /**
     * Answers one waiting item: the entity is removed and its stack rises back out, minus the
     * one item exchanged when the wish is granted. Sends no message.
     *
     * @param thrower the player who threw the item, or null when they are gone
     */
    public static TimeWellOutcome answer(ServerLevel level, BlockPos stonePos, ItemEntity item, Player thrower) {
        return answerWithRemaining(level, stonePos, item, thrower).outcome();
    }

    private static Answer answerWithRemaining(ServerLevel level, BlockPos stonePos, ItemEntity item, Player thrower) {
        ItemStack stack = item.getItem().copy();
        item.discard();
        BlockPos water = findWater(level, stonePos);
        if (water == null) {
            // The water was removed in the same tick. Return the items where the stone is.
            water = stonePos;
        }
        if (thrower == null) {
            returnAll(level, water, stack, null);
            return new Answer(TimeWellOutcome.THROWER_GONE, 0);
        }
        List<ItemStack> exchange = rollExchange(level, stonePos, stack.getItem(), thrower);
        if (exchange.isEmpty()) {
            returnAll(level, water, stack, thrower);
            return new Answer(TimeWellOutcome.NOT_ACCEPTED, 0);
        }
        long remaining = 0;
        if (level.getBlockEntity(stonePos) instanceof TimeWellStoneBlockEntity well) {
            long cooldown = ChronoDawnConfig.get().gameplay().wayside().timeWellCooldownTicks();
            remaining = well.tryActivate(thrower.getUUID(), level.getGameTime(), cooldown);
        }
        if (remaining > 0) {
            returnAll(level, water, stack, thrower);
            return new Answer(TimeWellOutcome.COOLDOWN, remaining);
        }
        stack.shrink(1);
        for (ItemStack result : exchange) {
            raise(level, water, result, thrower);
        }
        raise(level, water, stack, thrower);
        playExchangeEffects(level, water);
        return new Answer(TimeWellOutcome.EXCHANGED, 0);
    }

    /**
     * Rolls the exchange table for an item. Only chronodawn items have one, and a datapack can
     * remove a table to stop the well taking that item.
     *
     * @param player the wishing player, or null so GameTests can roll without one
     * @return the exchanged items, empty when the item is not accepted
     */
    public static List<ItemStack> rollExchange(ServerLevel level, BlockPos pos, Item item, Player player) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (!ChronoDawn.MOD_ID.equals(id.getNamespace())) {
            return List.of();
        }
        // A missing table resolves to LootTable.EMPTY, which rolls nothing.
        LootTable table = level.getServer().reloadableRegistries().getLootTable(
            ResourceKey.create(Registries.LOOT_TABLE,
                CompatResourceLocation.create(ChronoDawn.MOD_ID, EXCHANGE_LOOT_TABLE_PREFIX + id.getPath())));
        LootParams.Builder builder = new LootParams.Builder(level)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos));
        if (player != null) {
            builder.withParameter(LootContextParams.THIS_ENTITY, player).withLuck(player.getLuck());
        }
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : table.getRandomItems(builder.create(LootContextParamSets.CHEST))) {
            if (!stack.isEmpty()) {
                result.add(stack);
            }
        }
        return result;
    }

    private static void returnAll(ServerLevel level, BlockPos water, ItemStack stack, Player thrower) {
        raise(level, water, stack, thrower);
        playReturnEffects(level, water);
    }

    /**
     * Lifts a stack out of the shaft and tosses it over the rim, toward the thrower when they
     * are near so the items land at their feet rather than back in the water.
     */
    private static void raise(ServerLevel level, BlockPos water, ItemStack stack, Player thrower) {
        if (stack.isEmpty()) {
            return;
        }
        double x = water.getX() + 0.5;
        double y = water.getY() + RISE_HEIGHT;
        double z = water.getZ() + 0.5;
        double dx;
        double dz;
        if (thrower != null && thrower.level() == level
                && thrower.distanceToSqr(x, y, z) <= AIM_RANGE * AIM_RANGE
                && (thrower.getX() != x || thrower.getZ() != z)) {
            dx = thrower.getX() - x;
            dz = thrower.getZ() - z;
        } else {
            double angle = level.getRandom().nextDouble() * 2 * Math.PI;
            dx = Math.cos(angle);
            dz = Math.sin(angle);
        }
        double length = Math.sqrt(dx * dx + dz * dz);
        ItemEntity risen = new ItemEntity(level, x, y, z, stack);
        risen.setDeltaMovement(dx / length * RISE_HORIZONTAL_SPEED, RISE_VERTICAL_SPEED,
            dz / length * RISE_HORIZONTAL_SPEED);
        risen.setDefaultPickUpDelay();
        level.addFreshEntity(risen);
    }

    private static void playExchangeEffects(ServerLevel level, BlockPos water) {
        level.playSound(null, water, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
        double x = water.getX() + 0.5;
        double surface = water.getY() + 0.9;
        double z = water.getZ() + 0.5;
        level.sendParticles(ParticleTypes.GLOW, x, surface, z, GLOW_PARTICLES, 0.3, 0.05, 0.3, 0.0);
        // With a count of 0 the client reads the offsets as a direction scaled by the speed,
        // so each mote rises straight out of the shaft.
        for (int i = 0; i < END_ROD_PARTICLES; i++) {
            double px = x + (level.getRandom().nextDouble() - 0.5) * 0.6;
            double pz = z + (level.getRandom().nextDouble() - 0.5) * 0.6;
            level.sendParticles(ParticleTypes.END_ROD, px, surface, pz, 0, 0.0, 1.0, 0.0, END_ROD_SPEED);
        }
    }

    private static void playReturnEffects(ServerLevel level, BlockPos water) {
        level.playSound(null, water, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.5f, 1.0f);
        level.sendParticles(ParticleTypes.BUBBLE, water.getX() + 0.5, water.getY() + 0.7, water.getZ() + 0.5,
            BUBBLE_PARTICLES, 0.25, 0.1, 0.25, 0.0);
    }
}
