package com.chronosphere.registry;

import com.chronosphere.Chronosphere;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * Architectury Registry wrapper for custom creative tabs.
 *
 * This class provides a centralized registry for all creative mode tabs in the Chronosphere mod.
 * Using Architectury's DeferredRegister ensures cross-loader compatibility between NeoForge and Fabric.
 *
 * Task: T034a [Phase 2] Create ModCreativeTabs registry
 */
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Chronosphere.MOD_ID, Registries.CREATIVE_MODE_TAB);

    /**
     * Main Chronosphere creative tab.
     *
     * Contains all items and blocks from the Chronosphere mod.
     * Icon: Clockstone item
     *
     * Task: T034b [Phase 2] Register Chronosphere creative tab with icon
     * Task: T034c [Phase 2] Implement item group population
     */
    public static final RegistrySupplier<CreativeModeTab> CHRONOSPHERE_TAB = TABS.register(
        "chronosphere",
        () -> CreativeTabRegistry.create(builder ->
            builder.title(Component.translatable("itemGroup.chronosphere.chronosphere"))
                .icon(() -> new ItemStack(ModItems.CLOCKSTONE.get()))
                .displayItems(ModItems::populateCreativeTab)
        )
    );

    /**
     * Initialize creative tab registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        TABS.register();
        Chronosphere.LOGGER.info("Registered ModCreativeTabs");
    }
}
