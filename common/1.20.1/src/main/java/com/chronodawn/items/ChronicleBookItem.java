package com.chronodawn.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // On client side, GUI opening is handled via platform-specific events
        // See: ChronoDawnClientFabric and ChronoDawnClientNeoForge

        return InteractionResultHolder.success(stack);
    }

    public static Properties createProperties() {
        return new Item.Properties()
            .stacksTo(1); // Chronicle book is unique, only 1 per stack
    }
}
