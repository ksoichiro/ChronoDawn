package com.chronodawn.blocks;

import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * Ancient Time Wood Button block.
 *
 * Wooden button that emits redstone signal when pressed.
 * Stays active for 1.5 seconds (30 ticks).
 *
 * Properties:
 * - Hardness: 0.5 (same as Oak Button)
 * - Blast Resistance: 0.5 (same as Oak Button)
 * - Tool Required: None
 * - Activation Duration: 1.5 seconds (30 ticks)
 *
 * Crafting:
 * - 1x Ancient Time Wood Planks → 1x Ancient Time Wood Button
 */
public class AncientTimeWoodButton extends ButtonBlock {
    public AncientTimeWoodButton(BlockBehaviour.Properties properties) {
        super(properties, BlockSetType.OAK, 30, true); // 30 ticks = 1.5 seconds (wooden button duration)
    }

    /**
     * Create default properties for Ancient Time Wood Button.
     *
     * @return Block properties similar to vanilla wooden buttons
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .noCollission()
                .strength(0.5f) // hardness and blast resistance (same as Oak Button)
                .sound(SoundType.WOOD)
                .pushReaction(PushReaction.DESTROY);
    }
}
