package com.chronodawn.blocks;

import com.chronodawn.compat.CompatBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockEntity for Boss Room Door.
 * Stores the door type to differentiate between entrance and boss room doors.
 *
 * Door Types:
 * - "entrance": Entrance door to Master Clock - requires Key to Master Clock
 * - "boss_room": Boss room door (Master Clock) - requires 3x Ancient Gears
 * - "guardian_vault": Guardian Vault boss door (Chronos Warden) - spawns Chronos Warden
 * - "clockwork_depths": Clockwork Depths boss door (Clockwork Colossus)
 * - "phantom_tower": Phantom Tower boss door (Temporal Phantom)
 * - "entropy_crypt": Entropy Crypt boss door (Entropy Keeper)
 *
 * The door type is stored in NBT and set in structure files.
 */
public class BossRoomDoorBlockEntity extends CompatBlockEntity {
    private String doorType = "entrance"; // Default to entrance door

    public BossRoomDoorBlockEntity(BlockPos pos, BlockState state) {
        super(com.chronodawn.registry.ModBlockEntities.BOSS_ROOM_DOOR.get(), pos, state);
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
     * Check if this is a boss room door (Master Clock - Time Tyrant).
     * @return true if door type is "boss_room"
     */
    public boolean isBossRoomDoor() {
        return "boss_room".equals(doorType);
    }

    /**
     * Check if this is a Guardian Vault door (Chronos Warden).
     * @return true if door type is "guardian_vault"
     */
    public boolean isGuardianVaultDoor() {
        return "guardian_vault".equals(doorType);
    }

    /**
     * Check if this is a Clockwork Depths door (Clockwork Colossus).
     * @return true if door type is "clockwork_depths"
     */
    public boolean isClockworkDepthsDoor() {
        return "clockwork_depths".equals(doorType);
    }

    /**
     * Check if this is a Phantom Tower door (Temporal Phantom).
     * @return true if door type is "phantom_tower"
     */
    public boolean isPhantomTowerDoor() {
        return "phantom_tower".equals(doorType);
    }

    /**
     * Check if this is an Entropy Crypt door (Entropy Keeper).
     * @return true if door type is "entropy_crypt"
     */
    public boolean isEntropyCryptDoor() {
        return "entropy_crypt".equals(doorType);
    }

    @Override
    public void saveData(CompoundTag tag) {
        tag.putString("DoorType", doorType);
    }

    @Override
    public void loadData(CompoundTag tag) {
        if (tag.contains("DoorType")) {
            doorType = tag.getString("DoorType");
        }
    }
}
