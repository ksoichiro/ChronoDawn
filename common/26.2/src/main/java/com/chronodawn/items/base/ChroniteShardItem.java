package com.chronodawn.items.base;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/**
 * Chronite Shard - low-tier temporal reagent mined in the Overworld.
 *
 * The Chrono Dawn's temporal field bleeds into Overworld bedrock and
 * crystallises as Chronite. A Time Compass built from the shards senses
 * the place where the two worlds touched, the Ancient Ruins.
 *
 * Deliberately has no armour, tool, or weapon recipes. The shard form
 * signals a reagent rather than a gear material, so its abundance cannot
 * lower the difficulty of the game.
 */
public class ChroniteShardItem extends Item {
    public ChroniteShardItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        return new Properties()
                .stacksTo(64)
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chronite_shard")));
    }
}
