package com.chronodawn.registry;

import com.chronodawn.ChronoDawn;
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
 * This class provides a centralized registry for all creative mode tabs in the ChronoDawn mod.
 * Using Architectury's DeferredRegister ensures cross-loader compatibility between NeoForge and Fabric.
 *
 * Task: T034a [Phase 2] Create ModCreativeTabs registry
 */
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(ChronoDawn.MOD_ID, Registries.CREATIVE_MODE_TAB);

    /**
     * Main ChronoDawn creative tab.
     *
     * Contains all items and blocks from the ChronoDawn mod.
     * Icon: Clockstone item
     *
     * Task: T034b [Phase 2] Register ChronoDawn creative tab with icon
     * Task: T034c [Phase 2] Implement item group population
     */
    public static final RegistrySupplier<CreativeModeTab> CHRONO_DAWN_TAB = TABS.register(
        "chronodawn",
        () -> CreativeTabRegistry.create(builder ->
            builder.title(Component.translatable("itemGroup.chronodawn.chronodawn"))
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
        ChronoDawn.LOGGER.debug("Registered ModCreativeTabs");
    }
}
