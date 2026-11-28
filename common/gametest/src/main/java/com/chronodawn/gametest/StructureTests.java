package com.chronodawn.gametest;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.compat.CompatResourceLocation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class StructureTests {
    /**
     * The Sentinel Outpost steps between the first and second floor that the weathering script
     * turns into Andesite Stairs. They were Tuff Stairs, which 1.20.1 does not have, so 1.20.1
     * placed air there and the staircase had a gap.
     */
    private static final Set<BlockPos> SENTINEL_WEATHERED_STEPS = Set.of(new BlockPos(17, 5, 15),
        new BlockPos(17, 6, 14));

    private StructureTests() {}

    public static <T> List<T> generateStructureTests(MobBehaviorTests.TestFactory<T> factory) {
        return List.of(factory.create("structure_templates_load", helper -> helper.runAfterDelay(1, () -> {
            var manager = helper.getLevel().getStructureManager();
            for (var spec : StructureSpecs.getStructureSpecs()) {
                if (manager.get(CompatResourceLocation.create(ChronoDawn.MOD_ID, spec.id())).isEmpty()) {
                    CompatGameTestHelper.fail(helper, "Structure template '" + spec.id() + "' could not be loaded");
                    return;
                }
            }
            helper.succeed();
        })), factory.create("structure_template_blocks_exist_in_version", helper -> helper.runAfterDelay(1, () -> {
            // A template block that this Minecraft version does not know under its saved ID loads as
            // air without an error. Check blocks that were affected on 1.20.1 through the game's own
            // template loading.
            var manager = helper.getLevel().getStructureManager();
            StructureTemplate sentinel = manager.get(CompatResourceLocation.create(ChronoDawn.MOD_ID,
                "sentinel_outpost")).orElse(null);
            StructureTemplate ruins = manager.get(CompatResourceLocation.create(ChronoDawn.MOD_ID,
                "ancient_ruins")).orElse(null);
            if (sentinel == null || ruins == null) {
                CompatGameTestHelper.fail(helper, "sentinel_outpost or ancient_ruins could not be loaded");
                return;
            }
            Set<BlockPos> steps = sentinel.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(),
                Blocks.ANDESITE_STAIRS).stream().map(StructureTemplate.StructureBlockInfo::pos)
                .collect(Collectors.toSet());
            if (!steps.containsAll(SENTINEL_WEATHERED_STEPS)) {
                CompatGameTestHelper.fail(helper, "Sentinel Outpost staircase is missing Andesite Stairs at "
                    + SENTINEL_WEATHERED_STEPS + ", found " + steps);
                return;
            }
            // Short grass is minecraft:grass before 1.20.3 and minecraft:short_grass after it. Both
            // are a TallGrassBlock other than the fern, so look it up by class.
            int grass = 0;
            for (Block block : BuiltInRegistries.BLOCK) {
                if (block instanceof TallGrassBlock && block != Blocks.FERN) {
                    grass += ruins.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), block).size();
                }
            }
            if (grass == 0) {
                CompatGameTestHelper.fail(helper, "Ancient Ruins has no short grass");
                return;
            }
            helper.succeed();
        })));
    }
}
