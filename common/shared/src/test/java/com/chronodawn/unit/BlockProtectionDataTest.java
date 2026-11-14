package com.chronodawn.unit;

import com.chronodawn.data.BlockProtectionData;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * State-transition tests for boss-room block protection.
 *
 * Each test builds its own BlockProtectionData, so no global reset is needed
 * and parallel test batches cannot interfere with each other.
 * Actually preventing a block break needs a live world and is not covered here.
 */
class BlockProtectionDataTest {
    private static final BlockPos CENTER = new BlockPos(100, 64, 100);

    @BeforeAll
    static void initialize() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static BoundingBox around(BlockPos center, int radius) {
        return new BoundingBox(
            center.getX() - radius, center.getY() - radius, center.getZ() - radius,
            center.getX() + radius, center.getY() + radius, center.getZ() + radius);
    }

    @Test
    void registeredAreaIsProtected() {
        BlockProtectionData data = new BlockProtectionData();
        data.register("area", around(CENTER, 5));

        assertTrue(data.isProtected(CENTER));
    }

    @Test
    void positionOutsideAreaIsNotProtected() {
        BlockProtectionData data = new BlockProtectionData();
        data.register("area", around(CENTER, 2));

        assertFalse(data.isProtected(CENTER.offset(10, 10, 10)));
    }

    @Test
    void areaBoundsAreInclusive() {
        BlockProtectionData data = new BlockProtectionData();
        data.register("area", around(CENTER, 2));

        assertTrue(data.isProtected(CENTER.offset(2, 2, 2)));
        assertFalse(data.isProtected(CENTER.offset(3, 0, 0)));
    }

    @Test
    void markDefeatedUnprotectsArea() {
        BlockProtectionData data = new BlockProtectionData();
        data.register("area", around(CENTER, 5));
        assertTrue(data.isProtected(CENTER));

        data.markDefeated("area");

        assertFalse(data.isProtected(CENTER));
    }

    @Test
    void findAreaAtReturnsKeyForPositionInsideArea() {
        BlockProtectionData data = new BlockProtectionData();
        data.register("area", around(CENTER, 5));

        assertEquals("area", data.findAreaAt(CENTER));
        assertNull(data.findAreaAt(CENTER.offset(50, 0, 0)));
    }

    @Test
    void defeatingAtPositionUnprotectsFoundArea() {
        BlockProtectionData data = new BlockProtectionData();
        data.register("area", around(CENTER, 5));

        data.markDefeated(data.findAreaAt(CENTER));

        assertFalse(data.isProtected(CENTER));
    }

    @Test
    void multipleAreasAreIndependent() {
        BlockProtectionData data = new BlockProtectionData();
        BlockPos center1 = new BlockPos(3, 5, 3);
        BlockPos center2 = new BlockPos(10, 5, 10);
        data.register("one", around(center1, 2));
        data.register("two", around(center2, 2));
        assertTrue(data.isProtected(center1));
        assertTrue(data.isProtected(center2));

        data.markDefeated("one");

        assertFalse(data.isProtected(center1));
        assertTrue(data.isProtected(center2));
    }

    @Test
    void registerAndDefeatMarkDataDirty() {
        BlockProtectionData data = new BlockProtectionData();
        assertFalse(data.isDirty());

        data.register("area", around(CENTER, 5));
        assertTrue(data.isDirty());

        data.setDirty(false);
        data.markDefeated("area");
        assertTrue(data.isDirty());
    }

    @Test
    void savedStateRoundTripsThroughNbt() {
        BlockProtectionData data = new BlockProtectionData();
        BlockPos other = new BlockPos(-40, 70, 25);
        data.register("active", around(CENTER, 5));
        data.register("defeated", around(other, 5));
        data.markDefeated("defeated");

        BlockProtectionData restored = new BlockProtectionData();
        restored.loadData(data.saveData(new CompoundTag()));

        assertTrue(restored.isProtected(CENTER));
        assertFalse(restored.isProtected(other));
        assertEquals("defeated", restored.findAreaAt(other));
    }

    @Test
    void newInstanceStartsEmpty() {
        BlockProtectionData data = new BlockProtectionData();

        assertFalse(data.isProtected(CENTER));
        assertNull(data.findAreaAt(CENTER));
    }
}
