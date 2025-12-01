package com.chronosphere.entities.boats;

import com.chronosphere.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Custom boat entity for Chronosphere mod wood types.
 * Extends vanilla Boat to support Time Wood variants.
 *
 * Task: T268-T270 [US1] Create Time Wood Boat variants
 */
public class ChronosphereBoat extends Boat {

    private static final EntityDataAccessor<Integer> DATA_CHRONOSPHERE_TYPE = SynchedEntityData.defineId(
            ChronosphereBoat.class, EntityDataSerializers.INT);

    public ChronosphereBoat(EntityType<? extends Boat> entityType, Level level) {
        super(entityType, level);
    }

    public ChronosphereBoat(Level level, double x, double y, double z) {
        this(ModEntities.CHRONOSPHERE_BOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CHRONOSPHERE_TYPE, ChronosphereBoatType.TIME_WOOD.ordinal());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("ChronosphereType", getChronosphereBoatType().getName());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("ChronosphereType")) {
            setChronosphereBoatType(ChronosphereBoatType.byName(compound.getString("ChronosphereType")));
        }
    }

    public void setChronosphereBoatType(ChronosphereBoatType type) {
        this.entityData.set(DATA_CHRONOSPHERE_TYPE, type.ordinal());
    }

    public ChronosphereBoatType getChronosphereBoatType() {
        return ChronosphereBoatType.byId(this.entityData.get(DATA_CHRONOSPHERE_TYPE));
    }

    @Override
    public Item getDropItem() {
        return getChronosphereBoatType().getBoatItem();
    }

    /**
     * Variant enum for vanilla compatibility - always returns OAK since we handle drops ourselves.
     */
    @Override
    public Type getVariant() {
        return Type.OAK;
    }

    /**
     * Set variant - no-op since we use ChronosphereBoatType instead.
     */
    @Override
    public void setVariant(Type type) {
        // Ignore vanilla type - we use ChronosphereBoatType
    }
}
