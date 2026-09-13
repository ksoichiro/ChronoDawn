package com.chronodawn.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * Compatibility wrapper for checking whether a recipe is loaded in the
 * server's RecipeManager (Minecraft 1.20.1 / 1.21.1).
 *
 * In this version, RecipeManager.byKey() takes a plain ResourceLocation.
 * From 1.21.2 onward it requires a ResourceKey&lt;Recipe&lt;?&gt;&gt; built
 * from the RECIPE registry key instead (see the 1.21.2+ variant of this
 * class).
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
        return recipeManager.byKey(id).isPresent();
    }
}
