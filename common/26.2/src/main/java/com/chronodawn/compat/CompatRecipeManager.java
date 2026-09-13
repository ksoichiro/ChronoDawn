package com.chronodawn.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * Compatibility wrapper for checking whether a recipe is loaded in the
 * server's RecipeManager (Minecraft 26.1.2+).
 *
 * RecipeManager.byKey() requires a ResourceKey&lt;Recipe&lt;?&gt;&gt; built
 * from the RECIPE registry key, and the id type is Identifier (the 26.x
 * rename of ResourceLocation). See the 1.20.1/1.21.1 and 1.21.2+ variants
 * of this class for the earlier shapes.
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
    public static boolean isRecipeLoaded(RecipeManager recipeManager, Identifier id) {
        return recipeManager.byKey(ResourceKey.create(Registries.RECIPE, id)).isPresent();
    }
}
