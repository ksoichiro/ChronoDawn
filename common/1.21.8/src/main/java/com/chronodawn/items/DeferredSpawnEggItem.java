package com.chronodawn.items;

import com.chronodawn.ChronoDawn;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Supplier;

/**
 * Deferred Spawn Egg Item for Minecraft 1.21.4.
 *
 * In 1.21.4, SpawnEggItem constructor changed significantly:
 * - Color parameters removed (use Client Items system or custom textures)
 * - Constructor now takes (EntityType, Properties)
 *
 * This wrapper stores colors for client-side rendering via getColor() method,
 * which is used by the tint system in the new Client Items model definition.
 */
public class DeferredSpawnEggItem extends SpawnEggItem {
    private final Supplier<? extends EntityType<? extends Mob>> entityTypeSupplier;
    private final int backgroundColor;
    private final int highlightColor;
    // Lazily resolved entity type, cached after first access
    private EntityType<? extends Mob> cachedEntityType;

    public DeferredSpawnEggItem(
        Supplier<? extends EntityType<? extends Mob>> entityTypeSupplier,
        int backgroundColor,
        int highlightColor,
        Properties properties
    ) {
        // 1.21.4: SpawnEggItem constructor takes (EntityType, Properties)
        // We need to pass a valid EntityType, but it may not be available yet
        // Use a placeholder and override getDefaultInstance() to resolve later
        super(getInitialEntityType(entityTypeSupplier), properties);
        this.entityTypeSupplier = entityTypeSupplier;
        this.backgroundColor = backgroundColor;
        this.highlightColor = highlightColor;
    }

    /**
     * Get the initial entity type for the super constructor.
     * Returns the entity type if available, or null if not yet registered.
     */
    @SuppressWarnings("unchecked")
    private static EntityType<? extends Mob> getInitialEntityType(
            Supplier<? extends EntityType<? extends Mob>> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            // Entity not yet registered - this is expected during mod loading
            return null;
        }
    }

    /**
     * Initialize this spawn egg after all entities are registered.
     * In 1.21.4, this is mostly a no-op as registration happens differently.
     */
    public void initializeSpawnEgg() {
        // Cache the entity type now that entities are registered
        if (cachedEntityType == null) {
            cachedEntityType = entityTypeSupplier.get();
        }
    }

    /**
     * Convenience constructor for RegistrySupplier.
     */
    public DeferredSpawnEggItem(
        RegistrySupplier<? extends EntityType<? extends Mob>> entityTypeSupplier,
        int backgroundColor,
        int highlightColor,
        Properties properties
    ) {
        this((Supplier<? extends EntityType<? extends Mob>>) entityTypeSupplier, backgroundColor, highlightColor, properties);
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        EntityType<?> entityType = getResolvedEntityType();
        return entityType != null ? entityType.requiredFeatures() : FeatureFlagSet.of();
    }

    /**
     * Get the resolved entity type, caching it for performance.
     */
    @SuppressWarnings("unchecked")
    private EntityType<? extends Mob> getResolvedEntityType() {
        if (cachedEntityType == null) {
            try {
                cachedEntityType = entityTypeSupplier.get();
            } catch (Exception e) {
                ChronoDawn.LOGGER.debug("Entity type not yet available for spawn egg");
            }
        }
        return cachedEntityType;
    }

    /**
     * Get the background color for this spawn egg.
     * Used by the Client Items tint system in 1.21.4.
     */
    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Get the highlight color for this spawn egg.
     * Used by the Client Items tint system in 1.21.4.
     */
    public int getHighlightColor() {
        return highlightColor;
    }

    /**
     * Get the color for the given tint index.
     * Index 0 = background color, Index 1 = highlight color.
     * This is used by color providers for rendering.
     */
    public int getColor(int tintIndex) {
        return tintIndex == 0 ? this.backgroundColor : this.highlightColor;
    }

    /**
     * Get the entity type for this spawn egg (simplified version for testing).
     * In 1.21.4, the parent getType() requires Provider and ItemStack,
     * but for our deferred implementation, we can resolve it directly.
     *
     * @param stack The item stack (unused but kept for API compatibility)
     * @return The resolved entity type
     */
    @SuppressWarnings("unchecked")
    public EntityType<?> getType(ItemStack stack) {
        return getResolvedEntityType();
    }

    /**
     * Get the entity type for this spawn egg (1.21.4 signature).
     * This is the method signature used by 1.21.4's SpawnEggItem.
     *
     * @param registries The registry lookup provider
     * @param stack The item stack
     * @return The resolved entity type
     */
    @Override
    public EntityType<?> getType(HolderLookup.Provider registries, ItemStack stack) {
        return getResolvedEntityType();
    }

    /**
     * Override useOn to use the deferred entity type.
     * This is necessary because the parent SpawnEggItem stores the EntityType
     * passed to the constructor in a field, which would be null for deferred eggs.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack itemStack = context.getItemInHand();
        BlockPos blockPos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockState blockState = level.getBlockState(blockPos);

        // Get the deferred entity type
        EntityType<?> entityType = getResolvedEntityType();
        if (entityType == null) {
            ChronoDawn.LOGGER.error("Spawn egg entity type not resolved");
            return InteractionResult.FAIL;
        }

        // Handle spawner blocks
        if (blockState.is(Blocks.SPAWNER)) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof SpawnerBlockEntity spawnerBlockEntity) {
                spawnerBlockEntity.setEntityId(entityType, level.getRandom());
                blockEntity.setChanged();
                level.sendBlockUpdated(blockPos, blockState, blockState, 3);
                level.gameEvent(context.getPlayer(), GameEvent.BLOCK_CHANGE, blockPos);
                itemStack.shrink(1);
                return InteractionResult.SUCCESS;
            }
        }

        // Calculate spawn position
        BlockPos spawnPos;
        if (blockState.getCollisionShape(level, blockPos).isEmpty()) {
            spawnPos = blockPos;
        } else {
            spawnPos = blockPos.relative(direction);
        }

        // Spawn the entity
        @SuppressWarnings("unchecked")
        EntityType<? extends Mob> mobType = (EntityType<? extends Mob>) entityType;
        if (mobType.spawn(
                serverLevel,
                itemStack,
                context.getPlayer(),
                spawnPos,
                EntitySpawnReason.SPAWN_ITEM_USE,
                true,
                !blockPos.equals(spawnPos) && direction == Direction.UP
        ) != null) {
            itemStack.shrink(1);
            level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
        }

        return InteractionResult.SUCCESS;
    }
}
