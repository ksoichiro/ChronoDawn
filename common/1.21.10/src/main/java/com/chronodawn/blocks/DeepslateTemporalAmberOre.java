package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DeepslateTemporalAmberOre extends DropExperienceBlock {
    public DeepslateTemporalAmberOre(BlockBehaviour.Properties properties) {
        super(UniformInt.of(2, 5), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
                .strength(4.5f, 3.0f)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "deepslate_temporal_amber_ore")));
    }
}
