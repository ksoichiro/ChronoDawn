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
import com.chronodawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The plate in front of a Lost Adventurer Memorial. Stepping onto it is a prayer at
 * the grave: light falls on the player and leaves a journal page and a few supplies,
 * once per cooldown for each player.
 */
public class MemorialPlateBlock extends TemporalStonePressurePlate implements EntityBlock {
    private static final String OFFERING_LOOT_TABLE = "gameplay/lost_adventurer_memorial";
    private static final String BLESSED_OFFERING_LOOT_TABLE = "gameplay/lost_adventurer_memorial_blessed";
    private static final double PILLAR_HEIGHT = 8.0;
    private static final int PILLAR_PARTICLES = 40;
    private static final double PILLAR_SPREAD = 0.4;
    private static final double FALL_SPEED = 0.3;
    private static final int GLOW_RING_PARTICLES = 16;
    private static final double GLOW_RING_RADIUS = 0.6;
    // Above the praying player's head, so the items fall onto them with the light.
    // The plate cannot tell which way the structure was rotated, so it does not aim at the headstone.
    private static final double OFFERING_DROP_HEIGHT = 1.5;

    public MemorialPlateBlock(Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .noCollision()
                .strength(0.5f)
                .sound(SoundType.STONE)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "memorial_plate")));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MemorialPlateBlockEntity(pos, state);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean colliding) {
        super.entityInside(state, level, pos, entity, effectApplier, colliding);
        if (!level.isClientSide() && entity instanceof Player player && level instanceof ServerLevel serverLevel) {
            onPlayerInside(serverLevel, pos, player);
        }
    }

    /**
     * Handles a player inside the plate's block cell. Vanilla calls this every tick while the
     * player stays there. The whole cell counts, not only the pressed area, so brushing the
     * plate's edge also prays, which the design accepts.
     */
    public static void onPlayerInside(ServerLevel level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof MemorialPlateBlockEntity plate
                && plate.markStep(player.getUUID(), level.getGameTime())) {
            pray(level, pos, player, plate);
        }
    }

    private static void pray(ServerLevel level, BlockPos pos, Player player, MemorialPlateBlockEntity plate) {
        long cooldown = ChronoDawnConfig.get().gameplay().wayside().memorialCooldownTicks();
        long remaining = plate.tryActivate(player.getUUID(), level.getGameTime(), cooldown);
        if (remaining > 0) {
            player.displayClientMessage(
                Component.translatable("message.chronodawn.memorial_plate.cooldown",
                    PlayerCooldownTracker.toMinutesRoundedUp(remaining)),
                true
            );
            return;
        }
        spawnLightPillar(level, pos);
        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.2f);
        dropOffering(level, pos, player);
        player.displayClientMessage(Component.translatable("message.chronodawn.memorial_plate.answered"), true);
    }

    public static List<ItemStack> rollOffering(ServerLevel level, BlockPos pos) {
        return rollOffering(level, pos, OFFERING_LOOT_TABLE, null);
    }

    /**
     * Rolls a chronodawn loot table at the plate. A missing table yields an empty list.
     */
    public static List<ItemStack> rollOffering(ServerLevel level, BlockPos pos, String lootTablePath) {
        return rollOffering(level, pos, lootTablePath, null);
    }

    /**
     * Holding the journal in either hand blesses the prayer: a small offering replaces the page,
     * so a player who finished the journal stops collecting duplicates. Loot tables cannot see
     * the held item, so the choice is made here.
     */
    public static String offeringTableFor(Player player) {
        Item journal = ModItems.LOST_ADVENTURER_JOURNAL.get();
        return player.getMainHandItem().is(journal) || player.getOffhandItem().is(journal)
            ? BLESSED_OFFERING_LOOT_TABLE
            : OFFERING_LOOT_TABLE;
    }

    // The player is optional so GameTests can roll without one. When present, datapacks can
    // condition on the praying player and luck applies, as with vanilla chest loot.
    private static List<ItemStack> rollOffering(ServerLevel level, BlockPos pos, String lootTablePath, Player player) {
        LootTable table = level.getServer().reloadableRegistries().getLootTable(
            ResourceKey.create(Registries.LOOT_TABLE, CompatResourceLocation.create(ChronoDawn.MOD_ID, lootTablePath)));
        LootParams.Builder builder = new LootParams.Builder(level)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos));
        if (player != null) {
            builder.withParameter(LootContextParams.THIS_ENTITY, player).withLuck(player.getLuck());
        }
        return table.getRandomItems(builder.create(LootContextParamSets.CHEST));
    }

    private static void dropOffering(ServerLevel level, BlockPos pos, Player player) {
        for (ItemStack stack : rollOffering(level, pos, offeringTableFor(player), player)) {
            if (stack.isEmpty()) {
                continue;
            }
            // Above the praying player rather than the plate center, so two players sharing
            // the plate each catch their own offering.
            ItemEntity item = new ItemEntity(level,
                player.getX(), pos.getY() + OFFERING_DROP_HEIGHT, player.getZ(), stack);
            item.setDeltaMovement(Vec3.ZERO);
            item.setDefaultPickUpDelay();
            level.addFreshEntity(item);
        }
    }

    private static void spawnLightPillar(ServerLevel level, BlockPos pos) {
        double centerX = pos.getX() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        for (int i = 0; i < PILLAR_PARTICLES; i++) {
            double y = pos.getY() + 1.0 + PILLAR_HEIGHT * i / PILLAR_PARTICLES;
            double x = centerX + (level.getRandom().nextDouble() - 0.5) * PILLAR_SPREAD;
            double z = centerZ + (level.getRandom().nextDouble() - 0.5) * PILLAR_SPREAD;
            // With a count of 0 the client reads the offsets as a direction scaled by the speed,
            // so each mote falls toward the plate instead of scattering.
            level.sendParticles(ParticleTypes.END_ROD, x, y, z, 0, 0.0, -1.0, 0.0, FALL_SPEED);
        }
        for (int i = 0; i < GLOW_RING_PARTICLES; i++) {
            double angle = 2 * Math.PI * i / GLOW_RING_PARTICLES;
            level.sendParticles(ParticleTypes.GLOW,
                centerX + Math.cos(angle) * GLOW_RING_RADIUS, pos.getY() + 0.1,
                centerZ + Math.sin(angle) * GLOW_RING_RADIUS,
                1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
