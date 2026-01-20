package com.chronodawn.items;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Chronicle Book item - Opens the Chronicle guidebook GUI when used.
 * Replaces Patchouli book with custom implementation.
 *
 * Client-side GUI opening is handled in platform-specific code.
 */
public class ChronicleBookItem extends Item {
    public ChronicleBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // On client side, GUI opening is handled via platform-specific events
        // See: ChronoDawnClientFabric and ChronoDawnClientNeoForge

        return InteractionResult.SUCCESS;
    }

    public static Properties createProperties() {
        return new Item.Properties()
            .stacksTo(1) // Chronicle book is unique, only 1 per stack
            .setId(ResourceKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chronicle_book")));
    }
}
