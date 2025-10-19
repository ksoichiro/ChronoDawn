package com.chronosphere.registry;

import com.chronosphere.Chronosphere;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

/**
 * Architectury Registry wrapper for custom blocks.
 *
 * This class provides a centralized registry for all custom blocks in the Chronosphere mod.
 * Using Architectury's DeferredRegister ensures cross-loader compatibility between NeoForge and Fabric.
 */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Chronosphere.MOD_ID, Registries.BLOCK);

    // TODO: Register custom blocks here in future phases
    // Example:
    // public static final RegistrySupplier<Block> CLOCKSTONE_ORE = BLOCKS.register(
    //     "clockstone_ore",
    //     () -> new ClockstoneOre(BlockBehaviour.Properties.of()
    //         .strength(3.0f)
    //         .requiresCorrectToolForDrops())
    // );

    /**
     * Initialize block registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        BLOCKS.register();
        Chronosphere.LOGGER.info("Registered ModBlocks");
    }
}
