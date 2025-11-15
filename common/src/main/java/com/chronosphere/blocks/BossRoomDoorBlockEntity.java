package com.chronosphere.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockEntity for Boss Room Door.
 * Stores the door type to differentiate between entrance and boss room doors.
 *
 * Door Types:
 * - "entrance": Entrance door to Master Clock - requires Key to Master Clock
 * - "boss_room": Boss room door - requires 3x Ancient Gears
 *
 * The door type is stored in NBT and set in structure files.
 */
public class BossRoomDoorBlockEntity extends BlockEntity {
    private String doorType = "entrance"; // Default to entrance door

    public BossRoomDoorBlockEntity(BlockPos pos, BlockState state) {
        super(com.chronosphere.registry.ModBlockEntities.BOSS_ROOM_DOOR.get(), pos, state);
    }

    /**
     * Set the door type.
     * @param doorType "entrance" or "boss_room"
     */
    public void setDoorType(String doorType) {
        this.doorType = doorType;
        setChanged();
    }

    /**
     * Get the door type.
     * @return "entrance" or "boss_room"
     */
    public String getDoorType() {
        return doorType;
    }

    /**
     * Check if this is a boss room door.
     * @return true if door type is "boss_room"
     */
    public boolean isBossRoomDoor() {
        return "boss_room".equals(doorType);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("DoorType", doorType);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("DoorType")) {
            doorType = tag.getString("DoorType");
        }
    }
}
