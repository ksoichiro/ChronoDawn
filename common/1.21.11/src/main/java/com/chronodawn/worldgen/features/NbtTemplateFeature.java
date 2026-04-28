package com.chronodawn.worldgen.features;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Generic feature that loads an NBT structure template and places it at the
 * configured location. Used by Chrono Dawn's small ambient features (wells,
 * cairns, sundials, etc.).
 *
 * <p>1.21.11 override: {@code StructureTemplateManager.get()} takes
 * {@code Identifier} (renamed from {@code ResourceLocation} in 1.21.11).</p>
 *
 * <p>Placement uses the MOTION_BLOCKING_NO_LEAVES heightmap so the template
 * sits on solid terrain rather than on top of leaves or grass. A configurable
 * Y offset lets templates "settle" partially into the ground.</p>
 *
 * <p>This feature does not run any structure processors. Block-state replacement
 * (e.g. mossy variants, biome-aware swaps) should be authored directly into the
 * NBT.</p>
 */
public class NbtTemplateFeature extends Feature<NbtTemplateConfiguration> {

    public NbtTemplateFeature() {
        super(NbtTemplateConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NbtTemplateConfiguration> context) {
        NbtTemplateConfiguration config = context.config();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        ServerLevel serverLevel = level.getLevel();
        StructureTemplateManager manager = serverLevel.getStructureManager();
        // In 1.21.11 ResourceLocation was renamed to Identifier; config.template() returns Identifier.
        StructureTemplate template = manager.get(config.template()).orElse(null);
        if (template == null) {
            ChronoDawn.LOGGER.warn("NbtTemplateFeature: template not found: {}", config.template());
            return false;
        }

        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ());
        BlockPos placePos = new BlockPos(origin.getX(), surfaceY + config.yOffset(), origin.getZ());

        Rotation rotation = config.randomRotate() ? Rotation.getRandom(random) : Rotation.NONE;

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(false)
                .setRandom(random);

        Vec3i size = template.getSize();
        BlockPos pivot = new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
        settings.setRotationPivot(pivot);

        return template.placeInWorld(level, placePos, placePos, settings, random, 2);
    }
}
