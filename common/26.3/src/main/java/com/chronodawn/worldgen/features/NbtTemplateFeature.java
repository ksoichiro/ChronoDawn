package com.chronodawn.worldgen.features;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Generic feature that loads an NBT structure template and places it at the
 * configured location. Used by Chrono Dawn's small ambient features (wells,
 * cairns, sundials, etc.).
 *
 * <p>26.3 override: {@code Feature} is no longer generic over a separate
 * {@code FeatureConfiguration} type; a concrete feature is a record that
 * folds its configuration fields in directly and implements {@code Feature}.
 * {@code place(...)} takes the level/generator/random/origin directly instead
 * of a {@code FeaturePlaceContext} wrapper.</p>
 *
 * <p>Placement uses the MOTION_BLOCKING_NO_LEAVES heightmap so the template
 * sits on solid terrain rather than on top of leaves or grass. A configurable
 * Y offset lets templates "settle" partially into the ground.</p>
 *
 * <p>This feature does not run any structure processors. Block-state replacement
 * (e.g. mossy variants, biome-aware swaps) should be authored directly into the
 * NBT.</p>
 *
 * @param template     The structure template to load (e.g. "chronodawn:time_cairn").
 * @param randomRotate If true, the template is placed with a random 90-degree rotation.
 * @param yOffset      Vertical offset applied after heightmap snapping. Negative values
 *                     bury the structure into terrain (useful for "settled" footprints).
 * @param minGroundContactRatio Minimum fraction of the rotated template footprint that
 *                              must have sturdy terrain directly below it.
 * @param groundContactYOffset Template-relative Y offset used for ground contact checks.
 * @param clearReplaceableBlocks If true, replaceable blocks such as grass and snow layers
 *                               are cleared from the footprint before placement.
 */
public record NbtTemplateFeature(
        Identifier template,
        boolean randomRotate,
        int yOffset,
        double minGroundContactRatio,
        int groundContactYOffset,
        boolean clearReplaceableBlocks
) implements Feature {

    public static final MapCodec<NbtTemplateFeature> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("template").forGetter(NbtTemplateFeature::template),
                    Codec.BOOL.optionalFieldOf("random_rotate", Boolean.TRUE).forGetter(NbtTemplateFeature::randomRotate),
                    Codec.INT.optionalFieldOf("y_offset", 0).forGetter(NbtTemplateFeature::yOffset),
                    Codec.doubleRange(0.0, 1.0).optionalFieldOf("min_ground_contact_ratio", 0.0)
                            .forGetter(NbtTemplateFeature::minGroundContactRatio),
                    Codec.INT.optionalFieldOf("ground_contact_y_offset", -1)
                            .forGetter(NbtTemplateFeature::groundContactYOffset),
                    Codec.BOOL.optionalFieldOf("clear_replaceable_blocks", Boolean.TRUE)
                            .forGetter(NbtTemplateFeature::clearReplaceableBlocks)
            ).apply(instance, NbtTemplateFeature::new)
    );

    @Override
    public MapCodec<? extends Feature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        ServerLevel serverLevel = level.getLevel();
        StructureTemplateManager manager = serverLevel.getStructureTemplateManager();
        StructureTemplate structureTemplate = manager.get(template).orElse(null);
        if (structureTemplate == null) {
            ChronoDawn.LOGGER.warn("NbtTemplateFeature: template not found: {}", template);
            return false;
        }

        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ());
        BlockPos placePos = new BlockPos(origin.getX(), surfaceY + yOffset, origin.getZ());

        Rotation rotation = randomRotate ? Rotation.getRandom(random) : Rotation.NONE;

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(false)
                .setRandom(random);

        Vec3i size = structureTemplate.getSize();
        BlockPos pivot = new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
        settings.setRotationPivot(pivot);

        if (!hasEnoughGroundContact(
                level,
                placePos,
                size,
                settings,
                minGroundContactRatio,
                groundContactYOffset
        )) {
            return false;
        }

        if (clearReplaceableBlocks) {
            clearReplaceableBlocks(level, placePos, size, settings);
        }

        return structureTemplate.placeInWorld(level, placePos, placePos, settings, random, 2);
    }

    private static boolean hasEnoughGroundContact(
            WorldGenLevel level,
            BlockPos placePos,
            Vec3i templateSize,
            StructurePlaceSettings settings,
            double minGroundContactRatio,
            int groundContactYOffset
    ) {
        if (minGroundContactRatio <= 0.0) {
            return true;
        }

        int checked = 0;
        int supported = 0;

        for (int x = 0; x < templateSize.getX(); x++) {
            for (int z = 0; z < templateSize.getZ(); z++) {
                BlockPos groundPos = toWorldPos(placePos, settings, x, groundContactYOffset, z);
                checked++;

                BlockState groundState = level.getBlockState(groundPos);
                if (!groundState.canBeReplaced() && groundState.isFaceSturdy(level, groundPos, Direction.UP)) {
                    supported++;
                }
            }
        }

        return checked > 0 && (double) supported / checked >= minGroundContactRatio;
    }

    private static void clearReplaceableBlocks(
            WorldGenLevel level,
            BlockPos placePos,
            Vec3i templateSize,
            StructurePlaceSettings settings
    ) {
        for (int x = 0; x < templateSize.getX(); x++) {
            for (int z = 0; z < templateSize.getZ(); z++) {
                BlockPos pos = toWorldPos(placePos, settings, x, 0, z);
                BlockState state = level.getBlockState(pos);
                if (!state.isAir() && state.canBeReplaced() && state.getFluidState().isEmpty()) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    private static BlockPos toWorldPos(
            BlockPos placePos,
            StructurePlaceSettings settings,
            int x,
            int y,
            int z
    ) {
        return placePos.offset(StructureTemplate.calculateRelativePosition(settings, new BlockPos(x, y, z)));
    }
}
