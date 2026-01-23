package com.chronodawn.gametest;

import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModEntities;
import com.chronodawn.registry.ModItems;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Centralized test data definitions for registry-driven GameTest generation.
 *
 * Adding new blocks/entities/tools/armor to this class automatically generates
 * corresponding tests without needing to update test code elsewhere.
 */
public final class RegistryDrivenTestData {

    private RegistryDrivenTestData() {
        // Utility class
    }

    /**
     * All blocks that can be tested with simple setBlock placement.
     * Excludes: portals, crops, potted blocks, stems, saplings (need special substrate).
     */
    public static List<RegistrySupplier<Block>> getPlaceableBlocks() {
        return List.of(
            // Ores
            ModBlocks.CLOCKSTONE_ORE,
            ModBlocks.TIME_CRYSTAL_ORE,
            // Clockstone variants
            ModBlocks.CLOCKSTONE_BLOCK,
            ModBlocks.CLOCKSTONE_STAIRS,
            ModBlocks.CLOCKSTONE_SLAB,
            ModBlocks.CLOCKSTONE_WALL,
            // Temporal Bricks variants
            ModBlocks.TEMPORAL_BRICKS,
            ModBlocks.TEMPORAL_BRICKS_STAIRS,
            ModBlocks.TEMPORAL_BRICKS_SLAB,
            ModBlocks.TEMPORAL_BRICKS_WALL,
            // Decorative blocks
            ModBlocks.CLOCKWORK_BLOCK,
            ModBlocks.TIME_CRYSTAL_BLOCK,
            ModBlocks.REVERSING_TIME_SANDSTONE,
            ModBlocks.TEMPORAL_MOSS,
            ModBlocks.FROZEN_TIME_ICE,
            ModBlocks.TIME_WHEAT_BALE,
            ModBlocks.CHRONO_MELON,
            // Time Wood
            ModBlocks.TIME_WOOD_LOG,
            ModBlocks.STRIPPED_TIME_WOOD_LOG,
            ModBlocks.TIME_WOOD,
            ModBlocks.STRIPPED_TIME_WOOD,
            ModBlocks.TIME_WOOD_LEAVES,
            ModBlocks.TIME_WOOD_PLANKS,
            ModBlocks.TIME_WOOD_STAIRS,
            ModBlocks.TIME_WOOD_SLAB,
            ModBlocks.TIME_WOOD_FENCE,
            ModBlocks.TIME_WOOD_DOOR,
            ModBlocks.TIME_WOOD_TRAPDOOR,
            ModBlocks.TIME_WOOD_FENCE_GATE,
            ModBlocks.TIME_WOOD_BUTTON,
            ModBlocks.TIME_WOOD_PRESSURE_PLATE,
            // Dark Time Wood
            ModBlocks.DARK_TIME_WOOD_LOG,
            ModBlocks.STRIPPED_DARK_TIME_WOOD_LOG,
            ModBlocks.DARK_TIME_WOOD,
            ModBlocks.STRIPPED_DARK_TIME_WOOD,
            ModBlocks.DARK_TIME_WOOD_LEAVES,
            ModBlocks.DARK_TIME_WOOD_PLANKS,
            ModBlocks.DARK_TIME_WOOD_STAIRS,
            ModBlocks.DARK_TIME_WOOD_SLAB,
            ModBlocks.DARK_TIME_WOOD_FENCE,
            ModBlocks.DARK_TIME_WOOD_DOOR,
            ModBlocks.DARK_TIME_WOOD_TRAPDOOR,
            ModBlocks.DARK_TIME_WOOD_FENCE_GATE,
            ModBlocks.DARK_TIME_WOOD_BUTTON,
            ModBlocks.DARK_TIME_WOOD_PRESSURE_PLATE,
            // Ancient Time Wood
            ModBlocks.ANCIENT_TIME_WOOD_LOG,
            ModBlocks.STRIPPED_ANCIENT_TIME_WOOD_LOG,
            ModBlocks.ANCIENT_TIME_WOOD,
            ModBlocks.STRIPPED_ANCIENT_TIME_WOOD,
            ModBlocks.ANCIENT_TIME_WOOD_LEAVES,
            ModBlocks.ANCIENT_TIME_WOOD_PLANKS,
            ModBlocks.ANCIENT_TIME_WOOD_STAIRS,
            ModBlocks.ANCIENT_TIME_WOOD_SLAB,
            ModBlocks.ANCIENT_TIME_WOOD_FENCE,
            ModBlocks.ANCIENT_TIME_WOOD_DOOR,
            ModBlocks.ANCIENT_TIME_WOOD_TRAPDOOR,
            ModBlocks.ANCIENT_TIME_WOOD_FENCE_GATE,
            ModBlocks.ANCIENT_TIME_WOOD_BUTTON,
            ModBlocks.ANCIENT_TIME_WOOD_PRESSURE_PLATE,
            // Flowers and mushrooms
            ModBlocks.TIMELESS_MUSHROOM,
            ModBlocks.PURPLE_TIME_BLOSSOM,
            ModBlocks.ORANGE_TIME_BLOSSOM,
            ModBlocks.PINK_TIME_BLOSSOM,
            ModBlocks.DAWN_BELL,
            ModBlocks.DUSK_BELL,
            ModBlocks.UNSTABLE_FUNGUS,
            // Special blocks
            ModBlocks.BOSS_ROOM_DOOR,
            ModBlocks.ENTROPY_CRYPT_TRAPDOOR,
            ModBlocks.TEMPORAL_PARTICLE_EMITTER,
            ModBlocks.DECORATIVE_WATER,
            ModBlocks.BOSS_ROOM_BOUNDARY_MARKER,
            ModBlocks.CLOCK_TOWER_TELEPORTER
        );
    }

    /**
     * All entities that can be tested with simple spawn.
     * Excludes: projectiles (TIME_ARROW, TIME_BLAST, GEAR_PROJECTILE).
     */
    @SuppressWarnings("unchecked")
    public static List<RegistrySupplier<EntityType<?>>> getSpawnableEntities() {
        return List.of(
            // Bosses
            (RegistrySupplier<EntityType<?>>) (RegistrySupplier<?>) ModEntities.TIME_GUARDIAN,
            (RegistrySupplier<EntityType<?>>) (RegistrySupplier<?>) ModEntities.TIME_TYRANT,
            // Mini-bosses
            (RegistrySupplier<EntityType<?>>) (RegistrySupplier<?>) ModEntities.CHRONOS_WARDEN,
            (RegistrySupplier<EntityType<?>>) (RegistrySupplier<?>) ModEntities.CLOCKWORK_COLOSSUS,
            (RegistrySupplier<EntityType<?>>) (RegistrySupplier<?>) ModEntities.TEMPORAL_PHANTOM,
            (RegistrySupplier<EntityType<?>>) (RegistrySupplier<?>) ModEntities.ENTROPY_KEEPER,
            // Regular mobs
            (RegistrySupplier<EntityType<?>>) (RegistrySupplier<?>) ModEntities.TEMPORAL_WRAITH,
            (RegistrySupplier<EntityType<?>>) (RegistrySupplier<?>) ModEntities.CLOCKWORK_SENTINEL,
            (RegistrySupplier<EntityType<?>>) (RegistrySupplier<?>) ModEntities.FLOQ,
            (RegistrySupplier<EntityType<?>>) (RegistrySupplier<?>) ModEntities.TIME_KEEPER,
            (RegistrySupplier<EntityType<?>>) (RegistrySupplier<?>) ModEntities.EPOCH_HUSK,
            (RegistrySupplier<EntityType<?>>) (RegistrySupplier<?>) ModEntities.FORGOTTEN_MINUTE,
            (RegistrySupplier<EntityType<?>>) (RegistrySupplier<?>) ModEntities.CHRONAL_LEECH,
            (RegistrySupplier<EntityType<?>>) (RegistrySupplier<?>) ModEntities.MOMENT_CREEPER,
            // Boats
            (RegistrySupplier<EntityType<?>>) (RegistrySupplier<?>) ModEntities.CHRONO_DAWN_BOAT,
            (RegistrySupplier<EntityType<?>>) (RegistrySupplier<?>) ModEntities.CHRONO_DAWN_CHEST_BOAT
        );
    }

    /**
     * Tool specifications: item supplier + expected durability.
     */
    public record ToolSpec(RegistrySupplier<Item> item, int expectedDurability) {}

    public static List<ToolSpec> getToolSpecs() {
        return List.of(
            // Clockstone tools (450 durability)
            new ToolSpec(ModItems.CLOCKSTONE_SWORD, 450),
            new ToolSpec(ModItems.CLOCKSTONE_PICKAXE, 450),
            new ToolSpec(ModItems.CLOCKSTONE_AXE, 450),
            new ToolSpec(ModItems.CLOCKSTONE_SHOVEL, 450),
            new ToolSpec(ModItems.CLOCKSTONE_HOE, 450),
            // Enhanced Clockstone tools (1200 durability)
            new ToolSpec(ModItems.ENHANCED_CLOCKSTONE_SWORD, 1200),
            new ToolSpec(ModItems.ENHANCED_CLOCKSTONE_PICKAXE, 1200),
            new ToolSpec(ModItems.ENHANCED_CLOCKSTONE_AXE, 1200),
            new ToolSpec(ModItems.ENHANCED_CLOCKSTONE_SHOVEL, 1200),
            new ToolSpec(ModItems.ENHANCED_CLOCKSTONE_HOE, 1200),
            // Special weapons
            new ToolSpec(ModItems.CHRONOBLADE, 2000),
            new ToolSpec(ModItems.SPATIALLY_LINKED_PICKAXE, 1561)
        );
    }

    /**
     * Armor specifications: item supplier + equipment slot + expected defense.
     */
    public record ArmorSpec(RegistrySupplier<Item> item, EquipmentSlot slot, int expectedDefense) {}

    public static List<ArmorSpec> getArmorSpecs() {
        return List.of(
            // Clockstone armor
            new ArmorSpec(ModItems.CLOCKSTONE_HELMET, EquipmentSlot.HEAD, 2),
            new ArmorSpec(ModItems.CLOCKSTONE_CHESTPLATE, EquipmentSlot.CHEST, 6),
            new ArmorSpec(ModItems.CLOCKSTONE_LEGGINGS, EquipmentSlot.LEGS, 5),
            new ArmorSpec(ModItems.CLOCKSTONE_BOOTS, EquipmentSlot.FEET, 2),
            // Enhanced Clockstone armor
            new ArmorSpec(ModItems.ENHANCED_CLOCKSTONE_HELMET, EquipmentSlot.HEAD, 3),
            new ArmorSpec(ModItems.ENHANCED_CLOCKSTONE_CHESTPLATE, EquipmentSlot.CHEST, 7),
            new ArmorSpec(ModItems.ENHANCED_CLOCKSTONE_LEGGINGS, EquipmentSlot.LEGS, 6),
            new ArmorSpec(ModItems.ENHANCED_CLOCKSTONE_BOOTS, EquipmentSlot.FEET, 3)
        );
    }

    /**
     * Entity attribute specifications: entity type + attribute + expected value.
     */
    public record EntityAttributeSpec(
        RegistrySupplier<? extends EntityType<?>> entity,
        String attributeName,
        double expectedValue
    ) {}

    public static List<EntityAttributeSpec> getEntityAttributeSpecs() {
        return List.of(
            // Time Guardian
            new EntityAttributeSpec(ModEntities.TIME_GUARDIAN, "max_health", 200.0),
            new EntityAttributeSpec(ModEntities.TIME_GUARDIAN, "armor", 10.0),
            new EntityAttributeSpec(ModEntities.TIME_GUARDIAN, "attack_damage", 10.0),
            new EntityAttributeSpec(ModEntities.TIME_GUARDIAN, "knockback_resistance", 0.8),
            // Time Tyrant
            new EntityAttributeSpec(ModEntities.TIME_TYRANT, "max_health", 500.0),
            new EntityAttributeSpec(ModEntities.TIME_TYRANT, "armor", 15.0),
            new EntityAttributeSpec(ModEntities.TIME_TYRANT, "attack_damage", 18.0),
            new EntityAttributeSpec(ModEntities.TIME_TYRANT, "knockback_resistance", 1.0),
            // Chronos Warden
            new EntityAttributeSpec(ModEntities.CHRONOS_WARDEN, "max_health", 180.0),
            new EntityAttributeSpec(ModEntities.CHRONOS_WARDEN, "armor", 12.0),
            // Clockwork Colossus
            new EntityAttributeSpec(ModEntities.CLOCKWORK_COLOSSUS, "max_health", 200.0),
            new EntityAttributeSpec(ModEntities.CLOCKWORK_COLOSSUS, "knockback_resistance", 1.0),
            // Temporal Phantom
            new EntityAttributeSpec(ModEntities.TEMPORAL_PHANTOM, "max_health", 150.0),
            new EntityAttributeSpec(ModEntities.TEMPORAL_PHANTOM, "attack_damage", 8.0),
            // Entropy Keeper
            new EntityAttributeSpec(ModEntities.ENTROPY_KEEPER, "max_health", 160.0),
            new EntityAttributeSpec(ModEntities.ENTROPY_KEEPER, "armor", 6.0),
            // Temporal Wraith
            new EntityAttributeSpec(ModEntities.TEMPORAL_WRAITH, "max_health", 20.0),
            new EntityAttributeSpec(ModEntities.TEMPORAL_WRAITH, "attack_damage", 4.0),
            // Clockwork Sentinel
            new EntityAttributeSpec(ModEntities.CLOCKWORK_SENTINEL, "max_health", 30.0),
            new EntityAttributeSpec(ModEntities.CLOCKWORK_SENTINEL, "attack_damage", 6.0),
            new EntityAttributeSpec(ModEntities.CLOCKWORK_SENTINEL, "armor", 5.0),
            // Floq
            new EntityAttributeSpec(ModEntities.FLOQ, "max_health", 16.0),
            new EntityAttributeSpec(ModEntities.FLOQ, "attack_damage", 3.0),
            // Time Keeper
            new EntityAttributeSpec(ModEntities.TIME_KEEPER, "max_health", 20.0),
            // Epoch Husk
            new EntityAttributeSpec(ModEntities.EPOCH_HUSK, "max_health", 24.0),
            new EntityAttributeSpec(ModEntities.EPOCH_HUSK, "attack_damage", 5.0),
            // Forgotten Minute
            new EntityAttributeSpec(ModEntities.FORGOTTEN_MINUTE, "max_health", 14.0),
            new EntityAttributeSpec(ModEntities.FORGOTTEN_MINUTE, "attack_damage", 4.0),
            // Chronal Leech
            new EntityAttributeSpec(ModEntities.CHRONAL_LEECH, "max_health", 10.0),
            new EntityAttributeSpec(ModEntities.CHRONAL_LEECH, "attack_damage", 2.0),
            // Moment Creeper
            new EntityAttributeSpec(ModEntities.MOMENT_CREEPER, "max_health", 22.0)
        );
    }
}
