package com.chronodawn.entities.boats;

import com.chronodawn.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/**
 * Custom chest boat entity for ChronoDawn mod wood types.
 * Extends vanilla ChestBoat to support Time Wood variants with storage.
 *
 * Task: T268-T270 [US1] Create Time Wood Chest Boat variants
 */
public class ChronoDawnChestBoat extends ChestBoat {

    private static final EntityDataAccessor<Integer> DATA_CHRONO_DAWN_TYPE = SynchedEntityData.defineId(
            ChronoDawnChestBoat.class, EntityDataSerializers.INT);

    public ChronoDawnChestBoat(EntityType<? extends ChestBoat> entityType, Level level, Supplier<Item> supplier) {
        super(entityType, level, supplier);
    }

    public ChronoDawnChestBoat(Level level, Supplier<Item> supplier, double x, double y, double z) {
        this(ModEntities.CHRONO_DAWN_CHEST_BOAT.get(), level, supplier);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CHRONO_DAWN_TYPE, ChronoDawnBoatType.TIME_WOOD.ordinal());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("ChronoDawnType", getChronoDawnBoatType().getName());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("ChronoDawnType")) {
            setChronoDawnBoatType(ChronoDawnBoatType.byName(compound.getStringOr("ChronoDawnType", ChronoDawnBoatType.TIME_WOOD.getName())));
        }
    }

    public void setChronoDawnBoatType(ChronoDawnBoatType type) {
        this.entityData.set(DATA_CHRONO_DAWN_TYPE, type.ordinal());
    }

    public ChronoDawnBoatType getChronoDawnBoatType() {
        return ChronoDawnBoatType.byId(this.entityData.get(DATA_CHRONO_DAWN_TYPE));
    }

    /**
     * Note: In Minecraft 1.21.2, getDropItem() is no longer overridable.
     * The drop item is now determined by the Supplier<Item> passed to the constructor.
     */
}
