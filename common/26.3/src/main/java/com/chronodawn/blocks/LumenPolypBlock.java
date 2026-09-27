package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LumenPolypBlock extends SeaPickleBlock {

    public LumenPolypBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties(String id) {
        return BlockBehaviour.Properties.of()
            .lightLevel(state -> {
                int pickles = state.getValue(SeaPickleBlock.PICKLES);
                return state.getValue(SeaPickleBlock.WATERLOGGED) ? 3 + 3 * pickles : 0;
            })
            .sound(SoundType.SLIME_BLOCK)
            .noOcclusion()
            .pushReaction(net.minecraft.world.level.material.PushReaction.POPPED)
            .setId(ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, id)));
    }
}
