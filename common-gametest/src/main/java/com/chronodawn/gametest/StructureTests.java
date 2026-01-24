package com.chronodawn.gametest;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Shared structure template test generator used across all Minecraft versions.
 *
 * Generates tests for:
 * - Template loading and minimum size validation
 * - Required block presence and minimum count verification
 */
public final class StructureTests {

    private StructureTests() {
        // Utility class
    }

    /**
     * Block requirement: name + block supplier + minimum count.
     * Uses Supplier to defer registry resolution until test execution time.
     */
    public record BlockRequirement(String name, Supplier<Block> blockSupplier, int minCount) {}

    /**
     * Structure template specification: template ID + minimum dimensions + block requirements.
     */
    public record StructureSpec(
        String id,
        int minWidth,
        int minHeight,
        int minDepth,
        List<BlockRequirement> blockRequirements
    ) {}

    /**
     * Shared structure specs for all versions.
     */
    public static List<StructureSpec> getStructureSpecs() {
        return List.of(
            new StructureSpec("ancient_ruins", 5, 3, 5, List.of(
                new BlockRequirement("chest", () -> Blocks.CHEST, 2),
                new BlockRequirement("temporal_particle_emitter", ModBlocks.TEMPORAL_PARTICLE_EMITTER, 20),
                new BlockRequirement("clockstone_ore", ModBlocks.CLOCKSTONE_ORE, 30)
            )),
            new StructureSpec("desert_clock_tower", 21, 48, 21, List.of(
                new BlockRequirement("chest", () -> Blocks.CHEST, 3),
                new BlockRequirement("clock_tower_teleporter", ModBlocks.CLOCK_TOWER_TELEPORTER, 1)
            )),
            new StructureSpec("time_keeper_village", 11, 8, 11, List.of(
                new BlockRequirement("chest", () -> Blocks.CHEST, 2),
                new BlockRequirement("barrel", () -> Blocks.BARREL, 1),
                new BlockRequirement("time_crystal_block", ModBlocks.TIME_CRYSTAL_BLOCK, 1)
            )),
            new StructureSpec("forgotten_library", 35, 15, 35, List.of(
                new BlockRequirement("chest", () -> Blocks.CHEST, 7),
                new BlockRequirement("barrel", () -> Blocks.BARREL, 1),
                new BlockRequirement("enchanting_table", () -> Blocks.ENCHANTING_TABLE, 1)
            )),
            new StructureSpec("master_clock_surface", 15, 10, 15, List.of(
                new BlockRequirement("boss_room_boundary_marker", ModBlocks.BOSS_ROOM_BOUNDARY_MARKER, 2),
                new BlockRequirement("boss_room_door", ModBlocks.BOSS_ROOM_DOOR, 1),
                new BlockRequirement("dropper", () -> Blocks.DROPPER, 1)
            )),
            new StructureSpec("master_clock_stairs", 15, 8, 15, List.of(
                new BlockRequirement("boss_room_boundary_marker", ModBlocks.BOSS_ROOM_BOUNDARY_MARKER, 2),
                new BlockRequirement("jigsaw", () -> Blocks.JIGSAW, 2)
            )),
            new StructureSpec("master_clock_stairs_bottom", 15, 12, 15, List.of(
                new BlockRequirement("boss_room_boundary_marker", ModBlocks.BOSS_ROOM_BOUNDARY_MARKER, 2),
                new BlockRequirement("jigsaw", () -> Blocks.JIGSAW, 2)
            )),
            new StructureSpec("master_clock_corridor", 15, 15, 15, List.of(
                new BlockRequirement("boss_room_boundary_marker", ModBlocks.BOSS_ROOM_BOUNDARY_MARKER, 2),
                new BlockRequirement("jigsaw", () -> Blocks.JIGSAW, 2)
            )),
            new StructureSpec("master_clock_boss_room", 35, 20, 35, List.of(
                new BlockRequirement("boss_room_door", ModBlocks.BOSS_ROOM_DOOR, 1),
                new BlockRequirement("boss_room_boundary_marker", ModBlocks.BOSS_ROOM_BOUNDARY_MARKER, 2),
                new BlockRequirement("jigsaw", () -> Blocks.JIGSAW, 1)
            )),
            new StructureSpec("guardian_vault_entrance", 7, 6, 7, List.of(
                new BlockRequirement("jigsaw", () -> Blocks.JIGSAW, 1)
            )),
            new StructureSpec("guardian_vault_main", 30, 20, 25, List.of(
                new BlockRequirement("boss_room_boundary_marker", ModBlocks.BOSS_ROOM_BOUNDARY_MARKER, 2),
                new BlockRequirement("boss_room_door", ModBlocks.BOSS_ROOM_DOOR, 1),
                new BlockRequirement("chest", () -> Blocks.CHEST, 1),
                new BlockRequirement("barrel", () -> Blocks.BARREL, 9),
                new BlockRequirement("jigsaw", () -> Blocks.JIGSAW, 1)
            )),
            new StructureSpec("entropy_crypt_entrance", 7, 6, 7, List.of(
                new BlockRequirement("jigsaw", () -> Blocks.JIGSAW, 1)
            ))
        );
    }

    /**
     * Generates tests verifying structure template loading, minimum size, and required blocks.
     */
    public static <T> List<T> generateStructureTests(
            List<StructureSpec> specs,
            MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        for (var spec : specs) {
            ResourceLocation templateId = CompatResourceLocation.create(ChronoDawn.MOD_ID, spec.id());

            // Template load + size test
            tests.add(factory.create("structure_load_" + spec.id(), helper -> {
                helper.runAfterDelay(1, () -> {
                    var templateManager = helper.getLevel().getStructureManager();
                    var templateOpt = templateManager.get(templateId);
                    if (templateOpt.isEmpty()) {
                        helper.fail("Structure template '" + spec.id() + "' could not be loaded");
                        return;
                    }
                    StructureTemplate template = templateOpt.get();
                    var size = template.getSize();
                    if (size.getX() < spec.minWidth() || size.getY() < spec.minHeight() || size.getZ() < spec.minDepth()) {
                        helper.fail("Structure '" + spec.id() + "' size " +
                            size.getX() + "x" + size.getY() + "x" + size.getZ() +
                            " is smaller than minimum " +
                            spec.minWidth() + "x" + spec.minHeight() + "x" + spec.minDepth());
                        return;
                    }
                    helper.succeed();
                });
            }));

            // Block requirement tests
            for (var req : spec.blockRequirements()) {
                String testName = req.minCount() > 1
                    ? "structure_contains_" + req.minCount() + "_" + req.name() + "_" + spec.id()
                    : "structure_contains_" + req.name() + "_" + spec.id();
                tests.add(factory.create(testName, helper -> {
                    helper.runAfterDelay(1, () -> {
                        Block block = req.blockSupplier().get();
                        var templateManager = helper.getLevel().getStructureManager();
                        var templateOpt = templateManager.get(templateId);
                        if (templateOpt.isEmpty()) {
                            helper.fail("Structure template '" + spec.id() + "' could not be loaded");
                            return;
                        }
                        StructureTemplate template = templateOpt.get();
                        var blocks = template.filterBlocks(
                            BlockPos.ZERO,
                            new StructurePlaceSettings(),
                            block
                        );
                        if (blocks.size() < req.minCount()) {
                            helper.fail("Structure '" + spec.id() + "' contains " +
                                blocks.size() + " " + req.name() + ", expected at least " + req.minCount());
                            return;
                        }
                        helper.succeed();
                    });
                }));
            }
        }
        return tests;
    }
}
