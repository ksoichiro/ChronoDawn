package com.chronodawn.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor Mixin for Item to set the crafting remainder after construction.
 *
 * The Clockwork Block acts as the Clockwork Cookie's mold and must remain in the
 * crafting grid, i.e. its remainder is the item itself. Item.Properties#craftRemainder
 * needs the item instance before it exists, and the vanilla getter is final, so the
 * field is set once the item is registered (see ModItems#register).
 */
@Mixin(Item.class)
public interface ItemCraftRemainderAccessor {
    @Mutable
    @Accessor("craftingRemainingItem")
    void chronodawn$setCraftingRemainingItem(ItemStackTemplate remainder);
}
