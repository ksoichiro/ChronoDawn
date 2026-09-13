package com.chronodawn.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * Compatibility wrapper for checking whether a recipe is loaded in the
 * server's RecipeManager (Minecraft 1.21.2+).
 *
 * Starting in 1.21.2, RecipeManager.byKey() requires a
 * ResourceKey&lt;Recipe&lt;?&gt;&gt; instead of a plain ResourceLocation.
 * The key is built from the RECIPE registry key. See the 1.20.1/1.21.1
 * variant of this class for the earlier, simpler shape.
 */
public final class CompatRecipeManager {

    private CompatRecipeManager() {
        // Utility class
    }

    /**
     * Check whether a recipe with the given id is loaded.
     *
     * @param recipeManager the server's RecipeManager
     * @param id the recipe id
     * @return true if the recipe is present
     */
    public static boolean isRecipeLoaded(RecipeManager recipeManager, ResourceLocation id) {
        return recipeManager.byKey(ResourceKey.create(Registries.RECIPE, id)).isPresent();
    }
}
