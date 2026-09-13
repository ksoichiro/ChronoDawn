package com.chronodawn.fabric.worldgen;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Injects Chronite Ore into every Overworld biome.
 *
 * NeoForge does this from data via data/neoforge/biome_modifier/add_chronite_ore.json.
 * Fabric has no data-driven equivalent, so the same injection is expressed here.
 * Keep the two in sync: same placed feature, same generation step, same biome set.
 */
public final class ChroniteBiomeModifications {
    private ChroniteBiomeModifications() {
    }

    public static void register() {
        ResourceKey<PlacedFeature> oreChronite = ResourceKey.create(
            Registries.PLACED_FEATURE,
            CompatResourceLocation.create(ChronoDawn.MOD_ID, "ore_chronite")
        );

        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            oreChronite
        );
    }
}
