package com.chronodawn.mixin;

import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor Mixin for FolderRepositorySource to read which pack source a folder belongs to.
 *
 * Used by the Fabric PackRepositoryOverlayMixin to inject the runtime overlay only into
 * repositories that Fabric API also adds mod data packs to.
 */
@Mixin(FolderRepositorySource.class)
public interface FolderRepositorySourceAccessor {
    @Accessor("packSource")
    PackSource chronodawn$getPackSource();
}
