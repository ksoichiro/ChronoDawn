package com.chronodawn.gametest;

import com.chronodawn.registry.ModBlockId;
import java.util.List;

public final class StructureSpecs {
    private StructureSpecs() {}

    public record BlockRequirement(String name, int minCount) {}
    public record StructureSpec(String id, int minWidth, int minHeight, int minDepth,
                                List<BlockRequirement> blockRequirements) {}

    public static List<StructureSpec> getStructureSpecs() {
        return List.of(
            new StructureSpec("ancient_ruins", 5, 3, 5, List.of(
                new BlockRequirement("minecraft:chest", 2),
                new BlockRequirement("chronodawn:" + ModBlockId.TEMPORAL_PARTICLE_EMITTER.id(), 20),
                new BlockRequirement("chronodawn:" + ModBlockId.CLOCKSTONE_ORE.id(), 30)
            )),
            new StructureSpec("desert_clock_tower", 21, 48, 21, List.of(
                new BlockRequirement("minecraft:chest", 3),
                new BlockRequirement("chronodawn:" + ModBlockId.CLOCK_TOWER_TELEPORTER.id(), 1)
            )),
            new StructureSpec("time_keeper_village", 11, 8, 11, List.of(
                new BlockRequirement("minecraft:chest", 2),
                new BlockRequirement("minecraft:barrel", 1),
                new BlockRequirement("chronodawn:" + ModBlockId.TIME_CRYSTAL_BLOCK.id(), 1)
            )),
            new StructureSpec("time_well", 3, 5, 3, List.of(
                new BlockRequirement("chronodawn:" + ModBlockId.TEMPORAL_COBBLESTONE.id(), 8),
                new BlockRequirement("chronodawn:" + ModBlockId.TEMPORAL_AMBER_ORE.id(), 1),
                new BlockRequirement("chronodawn:" + ModBlockId.TIME_WELL_STONE.id(), 1),
                new BlockRequirement("minecraft:water", 1)
            )),
            new StructureSpec("lost_adventurer_memorial", 3, 3, 3, List.of(
                new BlockRequirement("chronodawn:" + ModBlockId.MOSSY_TEMPORAL_COBBLESTONE_SLAB.id(), 2),
                new BlockRequirement("chronodawn:" + ModBlockId.MEMORIAL_PLATE.id(), 1)
            )),
            new StructureSpec("lost_adventurer_memorial_snowy", 3, 3, 3, List.of(
                new BlockRequirement("minecraft:snow", 4),
                new BlockRequirement("chronodawn:" + ModBlockId.MEMORIAL_PLATE.id(), 1)
            )),
            new StructureSpec("time_cairn", 3, 3, 3, List.of(
                new BlockRequirement("chronodawn:" + ModBlockId.TIME_CAIRN_DIAL.id(), 1)
            )),
            new StructureSpec("watchmaker_camp", 5, 3, 5, List.of(
                new BlockRequirement("chronodawn:" + ModBlockId.TIME_WOOD_FENCE.id(), 12),
                new BlockRequirement("minecraft:white_wool", 15),
                new BlockRequirement("minecraft:chest", 1),
                new BlockRequirement("minecraft:campfire", 1)
            )),
            new StructureSpec("old_sundial", 3, 3, 3, List.of(
                new BlockRequirement("chronodawn:" + ModBlockId.SMOOTH_TEMPORAL_STONE.id(), 9),
                new BlockRequirement("chronodawn:" + ModBlockId.SUNDIAL_GNOMON.id(), 1)
            )),
            new StructureSpec("hourglass_monolith", 3, 5, 3, List.of(
                new BlockRequirement("chronodawn:" + ModBlockId.TEMPORAL_SANDSTONE.id(), 18),
                new BlockRequirement("chronodawn:" + ModBlockId.TEMPORAL_SANDSTONE_WALL.id(), 16),
                new BlockRequirement("minecraft:sand", 1)
            )),
            new StructureSpec("upside_down_tree", 4, 6, 4, List.of(
                new BlockRequirement("chronodawn:" + ModBlockId.ANCIENT_TIME_WOOD_LOG.id(), 8),
                new BlockRequirement("chronodawn:" + ModBlockId.ANCIENT_TIME_WOOD_LEAVES.id(), 7)
            )),
            new StructureSpec("forgotten_library", 35, 15, 35, List.of(
                new BlockRequirement("minecraft:chest", 7),
                new BlockRequirement("minecraft:barrel", 1),
                new BlockRequirement("minecraft:enchanting_table", 1)
            )),
            new StructureSpec("master_clock_surface", 15, 10, 15, List.of(
                new BlockRequirement("chronodawn:" + ModBlockId.BOSS_ROOM_BOUNDARY_MARKER.id(), 2),
                new BlockRequirement("chronodawn:" + ModBlockId.BOSS_ROOM_DOOR.id(), 1),
                new BlockRequirement("minecraft:dropper", 1)
            )),
            new StructureSpec("master_clock_stairs", 15, 8, 15, List.of(
                new BlockRequirement("chronodawn:" + ModBlockId.BOSS_ROOM_BOUNDARY_MARKER.id(), 2),
                new BlockRequirement("minecraft:jigsaw", 2)
            )),
            new StructureSpec("master_clock_stairs_bottom", 15, 12, 15, List.of(
                new BlockRequirement("chronodawn:" + ModBlockId.BOSS_ROOM_BOUNDARY_MARKER.id(), 2),
                new BlockRequirement("minecraft:jigsaw", 2)
            )),
            new StructureSpec("master_clock_corridor", 15, 15, 15, List.of(
                new BlockRequirement("chronodawn:" + ModBlockId.BOSS_ROOM_BOUNDARY_MARKER.id(), 2),
                new BlockRequirement("minecraft:jigsaw", 2)
            )),
            new StructureSpec("master_clock_boss_room", 35, 20, 35, List.of(
                new BlockRequirement("chronodawn:" + ModBlockId.BOSS_ROOM_DOOR.id(), 1),
                new BlockRequirement("chronodawn:" + ModBlockId.BOSS_ROOM_BOUNDARY_MARKER.id(), 2),
                new BlockRequirement("minecraft:jigsaw", 1)
            )),
            new StructureSpec("guardian_vault_entrance", 7, 6, 7, List.of(
                new BlockRequirement("minecraft:jigsaw", 1)
            )),
            new StructureSpec("guardian_vault_main", 30, 20, 25, List.of(
                new BlockRequirement("chronodawn:" + ModBlockId.BOSS_ROOM_BOUNDARY_MARKER.id(), 2),
                new BlockRequirement("chronodawn:" + ModBlockId.BOSS_ROOM_DOOR.id(), 1),
                new BlockRequirement("minecraft:chest", 1),
                new BlockRequirement("minecraft:barrel", 9),
                new BlockRequirement("minecraft:jigsaw", 1),
                new BlockRequirement("chronodawn:" + ModBlockId.CRACKED_TEMPORAL_STONE_BRICKS.id(), 6)
            )),
            new StructureSpec("entropy_crypt_entrance", 7, 6, 7, List.of(
                new BlockRequirement("minecraft:jigsaw", 1)
            )),
            new StructureSpec("entropy_crypt_main", 26, 20, 25, List.of(
                new BlockRequirement("chronodawn:" + ModBlockId.BOSS_ROOM_BOUNDARY_MARKER.id(), 2),
                new BlockRequirement("chronodawn:" + ModBlockId.ENTROPY_CRYPT_TRAPDOOR.id(), 1),
                new BlockRequirement("minecraft:chest", 3),
                new BlockRequirement("minecraft:jigsaw", 1)
            )),
            new StructureSpec("entropy_crypt_stairs", 5, 20, 5, List.of(
                new BlockRequirement("minecraft:jigsaw", 2)
            )),
            new StructureSpec("clockwork_depths_tower", 11, 15, 11, List.of(
                new BlockRequirement("minecraft:campfire", 4),
                new BlockRequirement("minecraft:jigsaw", 1)
            )),
            new StructureSpec("clockwork_depths_gearshaft", 13, 21, 13, List.of(
                new BlockRequirement("minecraft:jigsaw", 2)
            )),
            new StructureSpec("clockwork_depths_engine_room", 30, 17, 30, List.of(
                new BlockRequirement("chronodawn:" + ModBlockId.BOSS_ROOM_BOUNDARY_MARKER.id(), 2),
                new BlockRequirement("minecraft:jigsaw", 2),
                new BlockRequirement("minecraft:crimson_wall_sign", 4)
            )),
            new StructureSpec("clockwork_depths_archive_vault", 7, 9, 7, List.of(
                new BlockRequirement("minecraft:chest", 1),
                new BlockRequirement("minecraft:barrel", 4),
                new BlockRequirement("minecraft:anvil", 1),
                new BlockRequirement("minecraft:jigsaw", 1)
            )),
            new StructureSpec("phantom_catacombs_entrance", 9, 7, 9, List.of(
                new BlockRequirement("minecraft:jigsaw", 1)
            )),
            new StructureSpec("phantom_catacombs_corridor", 5, 40, 5, List.of(
                new BlockRequirement("minecraft:jigsaw", 2)
            )),
            new StructureSpec("phantom_catacombs_boss_room", 21, 9, 21, List.of(
                new BlockRequirement("chronodawn:" + ModBlockId.BOSS_ROOM_BOUNDARY_MARKER.id(), 2),
                new BlockRequirement("minecraft:amethyst_block", 1)
            )),
            new StructureSpec("phantom_catacombs_room_dead_end", 7, 9, 7, List.of(
                new BlockRequirement("minecraft:jigsaw", 1),
                new BlockRequirement("minecraft:crying_obsidian", 1)
            )),
            new StructureSpec("phantom_catacombs_room_1", 7, 9, 7, List.of(
                new BlockRequirement("minecraft:jigsaw", 2)
            )),
            new StructureSpec("phantom_catacombs_room_2", 7, 9, 7, List.of(
                new BlockRequirement("minecraft:jigsaw", 4)
            )),
            new StructureSpec("phantom_catacombs_room_3", 7, 9, 7, List.of(
                new BlockRequirement("minecraft:jigsaw", 4)
            )),
            new StructureSpec("phantom_catacombs_room_4", 7, 9, 7, List.of(
                new BlockRequirement("minecraft:jigsaw", 3)
            )),
            new StructureSpec("phantom_catacombs_room_5", 7, 9, 7, List.of(
                new BlockRequirement("minecraft:jigsaw", 3)
            )),
            new StructureSpec("phantom_catacombs_room_6", 7, 9, 7, List.of(
                new BlockRequirement("minecraft:jigsaw", 2)
            )),
            new StructureSpec("phantom_catacombs_room_7", 7, 9, 7, List.of(
                new BlockRequirement("minecraft:crying_obsidian", 1)
            ))
        );
    }

}
