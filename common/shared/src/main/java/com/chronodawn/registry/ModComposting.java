package com.chronodawn.registry;

import com.chronodawn.ChronoDawn;
import net.minecraft.world.level.block.ComposterBlock;

/**
 * Registers ChronoDawn plant and food items into the vanilla Composter's
 * {@link ComposterBlock#COMPOSTABLES} table, mirroring the chance tiers of
 * their closest vanilla analogues.
 *
 * Must be called after item registration has resolved (i.e. from the same
 * platform-specific post-registration hook used for spawn egg initialization:
 * immediately on Fabric, from FMLCommonSetupEvent on NeoForge), since
 * {@code RegistrySupplier<Item>.get()} is not safe to call any earlier on
 * NeoForge.
 */
public class ModComposting {
    public static void register() {
        // Saplings
        ComposterBlock.COMPOSTABLES.put(ModItems.TIME_WOOD_SAPLING.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModItems.DARK_TIME_WOOD_SAPLING.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModItems.ANCIENT_TIME_WOOD_SAPLING.get(), 0.3F);

        // Leaves
        ComposterBlock.COMPOSTABLES.put(ModItems.TIME_WOOD_LEAVES.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModItems.DARK_TIME_WOOD_LEAVES.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModItems.ANCIENT_TIME_WOOD_LEAVES.get(), 0.3F);

        // Seeds
        ComposterBlock.COMPOSTABLES.put(ModItems.TIME_WHEAT_SEEDS.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModItems.CHRONO_MELON_SEEDS.get(), 0.3F);

        // Short plants
        ComposterBlock.COMPOSTABLES.put(ModItems.TEMPORAL_TALL_GRASS.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModItems.TEMPORAL_FERN.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModItems.TEMPORAL_GRASS.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModItems.FADED_TEMPORAL_GRASS.get(), 0.3F);

        // Aquatic plants
        ComposterBlock.COMPOSTABLES.put(ModItems.TEMPORAL_KELP.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModItems.DRIED_TEMPORAL_KELP.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModItems.TEMPORAL_SEAGRASS.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModItems.TALL_TEMPORAL_SEAGRASS.get(), 0.3F);

        // Sea Pickle analogue
        ComposterBlock.COMPOSTABLES.put(ModItems.LUMEN_POLYP.get(), 0.65F);

        // Flowers
        ComposterBlock.COMPOSTABLES.put(ModItems.PURPLE_TIME_BLOSSOM.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModItems.ORANGE_TIME_BLOSSOM.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModItems.PINK_TIME_BLOSSOM.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModItems.DAWN_BELL.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModItems.DUSK_BELL.get(), 0.65F);

        // Crops and fungi
        ComposterBlock.COMPOSTABLES.put(ModItems.TIME_WHEAT.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModItems.TEMPORAL_ROOT.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModItems.CHRONO_MELON_SLICE.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModItems.TIMELESS_MUSHROOM.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModItems.UNSTABLE_FUNGUS.get(), 0.65F);

        // Baked foods
        ComposterBlock.COMPOSTABLES.put(ModItems.BAKED_TEMPORAL_ROOT.get(), 0.85F);
        ComposterBlock.COMPOSTABLES.put(ModItems.TIME_WHEAT_COOKIE.get(), 0.85F);

        // Hay bale analogue
        ComposterBlock.COMPOSTABLES.put(ModItems.TIME_WHEAT_BALE.get(), 1.0F);

        ChronoDawn.LOGGER.debug("Registered ModComposting entries");
    }
}
