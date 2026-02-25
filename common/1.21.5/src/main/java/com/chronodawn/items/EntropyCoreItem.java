package com.chronodawn.items;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * Entropy Core - Boss drop material from Entropy Keeper
 *
 * A corrupted core dropped by Entropy Keeper.
 * Used for crafting Chrono Aegis (ultimate shield).
 *
 * Properties:
 * - Rarity: RARE (Aqua color)
 * - Fire resistant
 * - Stack size: 16
 *
 * Crafting Recipe (Chrono Aegis):
 * Guardian Stone (G) + Phantom Essence (P) + Colossus Gear (C) + Entropy Core (E)
 * Pattern:
 *   G P
 *   C E
 *
 * Reference: research.md (Boss 4: Entropy Keeper)
 * Task: T237b [Phase 2] Create EntropyCoreItem
 */
public class EntropyCoreItem extends Item {
    public EntropyCoreItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        return new Properties()
            .rarity(Rarity.RARE)
            .fireResistant()
            .stacksTo(16)
            .setId(ResourceKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "entropy_core")));
    }
}
