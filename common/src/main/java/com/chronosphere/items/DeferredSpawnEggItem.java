package com.chronosphere.items;

import com.chronosphere.Chronosphere;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.SpawnEggItem;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Deferred Spawn Egg Item - Wrapper for SpawnEggItem that accepts RegistrySupplier.
 *
 * This class is necessary for Architectury cross-loader compatibility.
 * Regular SpawnEggItem requires EntityType at construction time, which causes
 * NullPointerException when using DeferredRegister (entities not yet registered).
 *
 * This wrapper accepts a Supplier<EntityType> instead, delaying the entity lookup
 * until the entity is actually registered.
 */
public class DeferredSpawnEggItem extends SpawnEggItem {
    private final Supplier<? extends EntityType<? extends Mob>> entityTypeSupplier;
    private final int backgroundColor;
    private final int highlightColor;

    public DeferredSpawnEggItem(
        Supplier<? extends EntityType<? extends Mob>> entityTypeSupplier,
        int backgroundColor,
        int highlightColor,
        Properties properties
    ) {
        super(null, backgroundColor, highlightColor, properties);
        this.entityTypeSupplier = entityTypeSupplier;
        this.backgroundColor = backgroundColor;
        this.highlightColor = highlightColor;

        // Don't register here - entity types are not available yet
        // Will be registered later via initializeSpawnEgg()
    }

    /**
     * Initialize this spawn egg by registering it to vanilla's static BY_ID map.
     * Must be called after all entities are registered.
     */
    public void initializeSpawnEgg() {
        registerColorsToVanillaMap();
    }

    /**
     * Register this spawn egg's colors to vanilla's static BY_ID map using reflection.
     * This is necessary because vanilla's rendering system uses the static map.
     */
    private void registerColorsToVanillaMap() {
        try {
            // Try to find the BY_ID field with various possible names
            Field byIdField = null;
            String[] possibleFieldNames = {"BY_ID", "f_43221_", "byId"};

            for (String fieldName : possibleFieldNames) {
                try {
                    byIdField = SpawnEggItem.class.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException ignored) {
                    // Try next name
                }
            }

            if (byIdField == null) {
                Chronosphere.LOGGER.warn("Could not find BY_ID field in SpawnEggItem");
                return;
            }

            byIdField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<EntityType<? extends Mob>, SpawnEggItem> byIdMap = (Map<EntityType<? extends Mob>, SpawnEggItem>) byIdField.get(null);

            // Register this spawn egg with the entity type
            EntityType<?> entityType = entityTypeSupplier.get();
            if (entityType != null) {
                byIdMap.put((EntityType<? extends Mob>) entityType, this);
                Chronosphere.LOGGER.info("Registered spawn egg for entity type: {} using field: {}", entityType, byIdField.getName());
            }
        } catch (Exception e) {
            Chronosphere.LOGGER.warn("Failed to register spawn egg colors to vanilla map via reflection", e);
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
    public EntityType<?> getType(net.minecraft.world.item.ItemStack stack) {
        return entityTypeSupplier.get();
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        EntityType<?> entityType = entityTypeSupplier.get();
        return entityType != null ? entityType.requiredFeatures() : FeatureFlagSet.of();
    }

    @Override
    public int getColor(int tintIndex) {
        return tintIndex == 0 ? this.backgroundColor : this.highlightColor;
    }
}
