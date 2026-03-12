package com.chronodawn.unit;

import com.chronodawn.registry.ModBlockEntityId;
import com.chronodawn.registry.ModBlockId;
import com.chronodawn.registry.ModEntityId;
import com.chronodawn.registry.ModItemId;
import com.chronodawn.version.MinecraftVersion;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Mod ID enums ({@link ModBlockId}, {@link ModItemId},
 * {@link ModEntityId}, {@link ModBlockEntityId}).
 * Validates ID uniqueness, version filtering, and render layer assignments.
 */
class ModIdEnumTest {

    // === ModBlockId ===

    @Test
    void modBlockId_allIdsAreUnique() {
        Set<String> ids = new HashSet<>();
        for (ModBlockId block : ModBlockId.values()) {
            assertTrue(ids.add(block.id()),
                "Duplicate block ID: " + block.id());
        }
    }

    @Test
    void modBlockId_allIdsAreNonEmpty() {
        for (ModBlockId block : ModBlockId.values()) {
            assertNotNull(block.id());
            assertFalse(block.id().isEmpty(), block.name() + " has empty ID");
        }
    }

    @Test
    void modBlockId_allHaveMinVersion() {
        for (ModBlockId block : ModBlockId.values()) {
            assertNotNull(block.minVersion(), block.name() + " has null minVersion");
        }
    }

    @Test
    void modBlockId_allHaveRenderLayer() {
        for (ModBlockId block : ModBlockId.values()) {
            assertNotNull(block.renderLayer(), block.name() + " has null renderLayer");
        }
    }

    @Test
    void modBlockId_availableFor_newestIncludesAll() {
        List<ModBlockId> available = ModBlockId.availableFor(MinecraftVersion.newest());
        assertEquals(ModBlockId.values().length, available.size(),
            "All blocks should be available for the newest version");
    }

    @Test
    void modBlockId_availableFor_subsetForOlderVersions() {
        List<ModBlockId> oldest = ModBlockId.availableFor(MinecraftVersion.oldest());
        List<ModBlockId> newest = ModBlockId.availableFor(MinecraftVersion.newest());
        assertTrue(newest.size() >= oldest.size(),
            "Newer versions should have at least as many blocks as older versions");
    }

    @Test
    void modBlockId_availableFor_isConsistentWithIsAvailableFor() {
        for (MinecraftVersion version : MinecraftVersion.values()) {
            List<ModBlockId> available = ModBlockId.availableFor(version);
            for (ModBlockId block : ModBlockId.values()) {
                assertEquals(block.isAvailableFor(version), available.contains(block),
                    block.name() + " availability inconsistent for " + version);
            }
        }
    }

    @Test
    void modBlockId_renderLayer_saplingsCutout() {
        assertEquals(ModBlockId.RenderLayer.CUTOUT, ModBlockId.TIME_WOOD_SAPLING.renderLayer());
        assertEquals(ModBlockId.RenderLayer.CUTOUT, ModBlockId.DARK_TIME_WOOD_SAPLING.renderLayer());
        assertEquals(ModBlockId.RenderLayer.CUTOUT, ModBlockId.ANCIENT_TIME_WOOD_SAPLING.renderLayer());
    }

    @Test
    void modBlockId_renderLayer_leavesCutoutMipped() {
        assertEquals(ModBlockId.RenderLayer.CUTOUT_MIPPED, ModBlockId.TIME_WOOD_LEAVES.renderLayer());
        assertEquals(ModBlockId.RenderLayer.CUTOUT_MIPPED, ModBlockId.DARK_TIME_WOOD_LEAVES.renderLayer());
        assertEquals(ModBlockId.RenderLayer.CUTOUT_MIPPED, ModBlockId.ANCIENT_TIME_WOOD_LEAVES.renderLayer());
    }

    @Test
    void modBlockId_renderLayer_iceTranslucent() {
        assertEquals(ModBlockId.RenderLayer.TRANSLUCENT, ModBlockId.FROZEN_TIME_ICE.renderLayer());
        assertEquals(ModBlockId.RenderLayer.TRANSLUCENT, ModBlockId.TIME_CRYSTAL_BLOCK.renderLayer());
    }

    @Test
    void modBlockId_renderLayer_solidBlocks() {
        assertEquals(ModBlockId.RenderLayer.SOLID, ModBlockId.CLOCKSTONE_BLOCK.renderLayer());
        assertEquals(ModBlockId.RenderLayer.SOLID, ModBlockId.TEMPORAL_BRICKS.renderLayer());
        assertEquals(ModBlockId.RenderLayer.SOLID, ModBlockId.TIME_WOOD_PLANKS.renderLayer());
    }

    @Test
    void modBlockId_renderLayer_doorsCutout() {
        assertEquals(ModBlockId.RenderLayer.CUTOUT, ModBlockId.TIME_WOOD_DOOR.renderLayer());
        assertEquals(ModBlockId.RenderLayer.CUTOUT, ModBlockId.DARK_TIME_WOOD_DOOR.renderLayer());
        assertEquals(ModBlockId.RenderLayer.CUTOUT, ModBlockId.ANCIENT_TIME_WOOD_DOOR.renderLayer());
    }

    @Test
    void modBlockId_renderLayer_trapdoorsCutout() {
        assertEquals(ModBlockId.RenderLayer.CUTOUT, ModBlockId.TIME_WOOD_TRAPDOOR.renderLayer());
        assertEquals(ModBlockId.RenderLayer.CUTOUT, ModBlockId.DARK_TIME_WOOD_TRAPDOOR.renderLayer());
        assertEquals(ModBlockId.RenderLayer.CUTOUT, ModBlockId.ANCIENT_TIME_WOOD_TRAPDOOR.renderLayer());
    }

    // === ModItemId ===

    @Test
    void modItemId_allIdsAreUnique() {
        Set<String> ids = new HashSet<>();
        for (ModItemId item : ModItemId.values()) {
            assertTrue(ids.add(item.id()),
                "Duplicate item ID: " + item.id());
        }
    }

    @Test
    void modItemId_allIdsAreNonEmpty() {
        for (ModItemId item : ModItemId.values()) {
            assertNotNull(item.id());
            assertFalse(item.id().isEmpty(), item.name() + " has empty ID");
        }
    }

    @Test
    void modItemId_availableFor_newestIncludesAll() {
        List<ModItemId> available = ModItemId.availableFor(MinecraftVersion.newest());
        assertEquals(ModItemId.values().length, available.size(),
            "All items should be available for the newest version");
    }

    @Test
    void modItemId_availableFor_isConsistentWithIsAvailableFor() {
        for (MinecraftVersion version : MinecraftVersion.values()) {
            List<ModItemId> available = ModItemId.availableFor(version);
            for (ModItemId item : ModItemId.values()) {
                assertEquals(item.isAvailableFor(version), available.contains(item),
                    item.name() + " availability inconsistent for " + version);
            }
        }
    }

    @Test
    void modItemId_spawnEggs_followNamingConvention() {
        for (ModItemId item : ModItemId.values()) {
            if (item.id().endsWith("_spawn_egg")) {
                String entityPart = item.id().replace("_spawn_egg", "");
                assertFalse(entityPart.isEmpty(),
                    item.name() + " spawn egg has empty entity part");
            }
        }
    }

    // === ModEntityId ===

    @Test
    void modEntityId_allIdsAreUnique() {
        Set<String> ids = new HashSet<>();
        for (ModEntityId entity : ModEntityId.values()) {
            assertTrue(ids.add(entity.id()),
                "Duplicate entity ID: " + entity.id());
        }
    }

    @Test
    void modEntityId_allIdsAreNonEmpty() {
        for (ModEntityId entity : ModEntityId.values()) {
            assertNotNull(entity.id());
            assertFalse(entity.id().isEmpty(), entity.name() + " has empty ID");
        }
    }

    @Test
    void modEntityId_availableFor_newestIncludesAll() {
        List<ModEntityId> available = ModEntityId.availableFor(MinecraftVersion.newest());
        assertEquals(ModEntityId.values().length, available.size(),
            "All entities should be available for the newest version");
    }

    @Test
    void modEntityId_availableFor_isConsistentWithIsAvailableFor() {
        for (MinecraftVersion version : MinecraftVersion.values()) {
            List<ModEntityId> available = ModEntityId.availableFor(version);
            for (ModEntityId entity : ModEntityId.values()) {
                assertEquals(entity.isAvailableFor(version), available.contains(entity),
                    entity.name() + " availability inconsistent for " + version);
            }
        }
    }

    // === ModBlockEntityId ===

    @Test
    void modBlockEntityId_allIdsAreUnique() {
        Set<String> ids = new HashSet<>();
        for (ModBlockEntityId be : ModBlockEntityId.values()) {
            assertTrue(ids.add(be.id()),
                "Duplicate block entity ID: " + be.id());
        }
    }

    @Test
    void modBlockEntityId_allIdsAreNonEmpty() {
        for (ModBlockEntityId be : ModBlockEntityId.values()) {
            assertNotNull(be.id());
            assertFalse(be.id().isEmpty(), be.name() + " has empty ID");
        }
    }

    @Test
    void modBlockEntityId_availableFor_newestIncludesAll() {
        List<ModBlockEntityId> available = ModBlockEntityId.availableFor(MinecraftVersion.newest());
        assertEquals(ModBlockEntityId.values().length, available.size(),
            "All block entities should be available for the newest version");
    }

    // === Cross-enum consistency ===

    @Test
    void spawnEggs_haveMatchingEntities() {
        Set<String> entityIds = new HashSet<>();
        for (ModEntityId entity : ModEntityId.values()) {
            entityIds.add(entity.id());
        }

        for (ModItemId item : ModItemId.values()) {
            if (item.id().endsWith("_spawn_egg")) {
                String entityPart = item.id().replace("_spawn_egg", "");
                assertTrue(entityIds.contains(entityPart),
                    "Spawn egg " + item.id() + " has no matching entity '" + entityPart + "'");
            }
        }
    }
}
