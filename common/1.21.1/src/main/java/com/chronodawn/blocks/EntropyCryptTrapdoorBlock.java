package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.bosses.EntropyKeeperEntity;
import com.chronodawn.registry.ModEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockSetType.PressurePlateSensitivity;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom trapdoor block for Entropy Crypt structure.
 *
 * This trapdoor leads to the Vault (treasure room) below the Boss Chamber.
 * When a player tries to open it, Entropy Keeper spawns.
 * After the boss is defeated, the trapdoor can be opened normally.
 *
 * Features:
 * - First interaction: Spawns Entropy Keeper boss
 * - ACTIVATED property: Tracks if boss has been spawned
 * - After activation: Functions as normal trapdoor
 *
 * Usage:
 * - Place in Boss Chamber floor (above Vault)
 * - Set ACTIVATED=false in NBT
 *
 * Thread Safety (T429):
 * - Uses ConcurrentHashMap.newKeySet() for spawn position tracking
 *
 * Task: T429 [Thread Safety] Fix non-thread-safe collection usage
 */
public class EntropyCryptTrapdoorBlock extends TrapDoorBlock {
    /**
     * Property to track if the boss has been spawned.
     * false = boss not yet spawned, will spawn on interaction
     * true = boss already spawned, trapdoor functions normally
     */
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");

    // Track positions where boss has been spawned (per world session)
    // T429: Use ConcurrentHashMap.newKeySet() for thread-safe Set
    private static final Set<BlockPos> spawnedPositions = ConcurrentHashMap.newKeySet();

    /**
     * Custom BlockSetType that looks/sounds like iron but allows hand interaction.
     * This enables our custom use logic to work properly.
     * Note: In 1.21.1, we use IRON directly as BlockSetType.register() is private
     */
    private static final BlockSetType ENTROPY_CRYPT_TYPE = BlockSetType.IRON;

    public EntropyCryptTrapdoorBlock(Properties properties) {
        super(ENTROPY_CRYPT_TYPE, properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(OPEN, false)
            .setValue(HALF, net.minecraft.world.level.block.state.properties.Half.BOTTOM)
            .setValue(POWERED, false)
            .setValue(WATERLOGGED, false)
            .setValue(ACTIVATED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVATED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // Check if boss has already been spawned
        if (!state.getValue(ACTIVATED)) {
            // First interaction - spawn the boss
            if (!level.isClientSide) {
                // Check if already spawned at this position (session tracking)
                if (spawnedPositions.contains(pos)) {
                    // Already spawned this session, just activate and allow opening
                    level.setBlock(pos, state.setValue(ACTIVATED, true), 3);
                    return super.useWithoutItem(state.setValue(ACTIVATED, true), level, pos, player, hitResult);
                }

                // Spawn Entropy Keeper
                boolean spawned = spawnEntropyKeeper((ServerLevel) level, pos, player);

                if (spawned) {
                    // Mark as activated
                    level.setBlock(pos, state.setValue(ACTIVATED, true), 3);
                    spawnedPositions.add(pos.immutable());

                    // Send message to player
                    player.displayClientMessage(
                        Component.translatable("message.chronodawn.entropy_keeper_awakens"),
                        true
                    );

                    ChronoDawn.LOGGER.debug(
                        "Entropy Keeper spawned from trapdoor at {} by player {}",
                        pos,
                        player.getName().getString()
                    );

                    // Don't open the trapdoor yet - player must defeat boss first
                    return InteractionResult.SUCCESS;
                } else {
                    // Spawn failed - still don't open trapdoor
                    ChronoDawn.LOGGER.error("Failed to spawn Entropy Keeper at {}", pos);
                    return InteractionResult.FAIL;
                }
            }
            // Client side - don't open trapdoor
            return InteractionResult.SUCCESS;
        }

        // Already activated - function as normal trapdoor (manually toggle since IRON type blocks hand interaction)
        if (!level.isClientSide) {
            state = state.cycle(OPEN);
            level.setBlock(pos, state, 10);
            // Play sound
            this.playToggleSound(player, level, pos, state.getValue(OPEN));
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Play trapdoor open/close sound.
     */
    private void playToggleSound(Player player, Level level, BlockPos pos, boolean open) {
        level.playSound(
            player,
            pos,
            open ? ENTROPY_CRYPT_TYPE.trapdoorOpen() : ENTROPY_CRYPT_TYPE.trapdoorClose(),
            net.minecraft.sounds.SoundSource.BLOCKS,
            1.0F,
            level.getRandom().nextFloat() * 0.1F + 0.9F
        );
    }

    /**
     * Spawn Entropy Keeper near the trapdoor.
     *
     * @param level ServerLevel
     * @param trapdoorPos Position of the trapdoor
     * @param player Player who triggered the spawn
     * @return true if spawn was successful
     */
    private boolean spawnEntropyKeeper(ServerLevel level, BlockPos trapdoorPos, Player player) {
        // Find spawn position (a few blocks away from trapdoor, at floor level)
        BlockPos spawnPos = findSpawnPosition(level, trapdoorPos);

        if (spawnPos == null) {
            ChronoDawn.LOGGER.warn("Could not find valid spawn position for Entropy Keeper near {}", trapdoorPos);
            return false;
        }

        // Create and spawn Entropy Keeper
        EntropyKeeperEntity keeper = ModEntities.ENTROPY_KEEPER.get().create(level);
        if (keeper != null) {
            keeper.moveTo(
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                0.0f,
                0.0f
            );

            // Face the player
            double dx = player.getX() - spawnPos.getX();
            double dz = player.getZ() - spawnPos.getZ();
            float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
            keeper.setYRot(yaw);
            keeper.setYHeadRot(yaw);

            keeper.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.TRIGGERED, null);
            return level.addFreshEntity(keeper);
        }

        ChronoDawn.LOGGER.error("Failed to create Entropy Keeper entity");
        return false;
    }

    /**
     * Find a valid spawn position for the boss.
     * Looks for a position with solid ground and enough air space.
     *
     * @param level ServerLevel
     * @param trapdoorPos Position of the trapdoor
     * @return Valid spawn position, or null if not found
     */
    private BlockPos findSpawnPosition(ServerLevel level, BlockPos trapdoorPos) {
        // Try positions around the trapdoor (3-5 blocks away)
        int[] offsets = {3, -3, 4, -4, 5, -5};

        for (int xOff : offsets) {
            for (int zOff : offsets) {
                BlockPos checkPos = trapdoorPos.offset(xOff, 0, zOff);

                // Find ground level
                for (int y = 2; y >= -2; y--) {
                    BlockPos testPos = checkPos.above(y);
                    BlockPos belowPos = testPos.below();

                    // Check if valid spawn position (air with solid ground)
                    if (level.getBlockState(testPos).isAir() &&
                        level.getBlockState(testPos.above()).isAir() &&
                        !level.getBlockState(belowPos).isAir()) {
                        return testPos;
                    }
                }
            }
        }

        // Fallback: spawn above trapdoor
        return trapdoorPos.above(2);
    }

    /**
     * Reset spawn tracking (for testing or world reload).
     */
    public static void resetSpawnTracking() {
        spawnedPositions.clear();
    }
}
