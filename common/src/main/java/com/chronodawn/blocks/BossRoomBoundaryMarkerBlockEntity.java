package com.chronodawn.blocks;

import com.chronodawn.compat.CompatBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockEntity for Boss Room Boundary Marker.
 *
 * Stores:
 * - markerType: "boss_room_min" or "boss_room_max"
 * - replaceWith: ResourceLocation of block to replace with (defaults to "minecraft:air")
 *
 * The marker type determines which corner of the bounding box this marker represents:
 * - "boss_room_min": Southwest floor corner (min X, min Y, min Z)
 * - "boss_room_max": Northeast ceiling corner (max X, max Y, max Z)
 *
 * Implementation: T224 - Boss room protection with marker blocks
 */
public class BossRoomBoundaryMarkerBlockEntity extends CompatBlockEntity {
    private String markerType = "boss_room_min"; // Default to min
    private ResourceLocation replaceWith = BuiltInRegistries.BLOCK.getKey(Blocks.AIR); // Default to air

    public BossRoomBoundaryMarkerBlockEntity(BlockPos pos, BlockState state) {
        super(com.chronodawn.registry.ModBlockEntities.BOSS_ROOM_BOUNDARY_MARKER.get(), pos, state);
    }

    /**
     * Set the marker type.
     * @param markerType "boss_room_min" or "boss_room_max"
     */
    public void setMarkerType(String markerType) {
        this.markerType = markerType;
        setChanged();
    }

    /**
     * Get the marker type.
     * @return "boss_room_min" or "boss_room_max"
     */
    public String getMarkerType() {
        return markerType;
    }

    /**
     * Check if this is a minimum boundary marker.
     * @return true if marker type is "boss_room_min"
     */
    public boolean isMinMarker() {
        return "boss_room_min".equals(markerType);
    }

    /**
     * Check if this is a maximum boundary marker.
     * @return true if marker type is "boss_room_max"
     */
    public boolean isMaxMarker() {
        return "boss_room_max".equals(markerType);
    }

    /**
     * Set the block to replace this marker with during structure generation.
     * @param blockId ResourceLocation of the replacement block
     */
    public void setReplaceWith(ResourceLocation blockId) {
        this.replaceWith = blockId;
        setChanged();
    }

    /**
     * Get the block to replace this marker with.
     * @return ResourceLocation of the replacement block
     */
    public ResourceLocation getReplaceWith() {
        return replaceWith;
    }

    /**
     * Get the replacement block state.
     * @return BlockState to replace this marker with
     */
    public BlockState getReplacementState() {
        var block = BuiltInRegistries.BLOCK.get(replaceWith);
        return block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState();
    }

    @Override
    public void saveData(CompoundTag tag) {
        tag.putString("MarkerType", markerType);
        tag.putString("ReplaceWith", replaceWith.toString());
    }

    @Override
    public void loadData(CompoundTag tag) {
        if (tag.contains("MarkerType")) {
            markerType = tag.getString("MarkerType");
        }
        if (tag.contains("ReplaceWith")) {
            replaceWith = ResourceLocation.parse(tag.getString("ReplaceWith"));
        }
    }
}
