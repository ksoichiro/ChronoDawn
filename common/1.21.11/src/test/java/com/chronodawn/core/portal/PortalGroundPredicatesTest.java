package com.chronodawn.core.portal;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the static position predicates in {@link PortalTeleportHandler}.
 * Verifies that fluids are rejected by both "clear space" and "ground" checks,
 * preventing the destination portal from being placed underwater or in lava.
 */
class PortalGroundPredicatesTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void clearSpace_air_isClear() {
        BlockState air = Blocks.AIR.defaultBlockState();
        assertTrue(PortalTeleportHandler.isClearForPortalSpace(air));
    }

    @Test
    void clearSpace_water_isNotClear() {
        BlockState water = Blocks.WATER.defaultBlockState();
        assertFalse(PortalTeleportHandler.isClearForPortalSpace(water),
            "Water must not count as clear space; otherwise portals generate underwater.");
    }

    @Test
    void clearSpace_lava_isNotClear() {
        BlockState lava = Blocks.LAVA.defaultBlockState();
        assertFalse(PortalTeleportHandler.isClearForPortalSpace(lava),
            "Lava must not count as clear space.");
    }

    @Test
    void clearSpace_snowLayer_isClear() {
        BlockState snow = Blocks.SNOW.defaultBlockState();
        assertTrue(PortalTeleportHandler.isClearForPortalSpace(snow),
            "Replaceable non-fluid blocks (snow layer) should still count as clear (vanilla parity).");
    }

    @Test
    void ground_stone_isSuitable() {
        BlockState stone = Blocks.STONE.defaultBlockState();
        assertTrue(PortalTeleportHandler.isSuitablePortalGround(stone));
    }

    @Test
    void ground_water_isNotSuitable() {
        BlockState water = Blocks.WATER.defaultBlockState();
        assertFalse(PortalTeleportHandler.isSuitablePortalGround(water),
            "Water must not count as solid ground; otherwise portals settle on the seabed.");
    }

    @Test
    void ground_lava_isNotSuitable() {
        BlockState lava = Blocks.LAVA.defaultBlockState();
        assertFalse(PortalTeleportHandler.isSuitablePortalGround(lava));
    }
}
