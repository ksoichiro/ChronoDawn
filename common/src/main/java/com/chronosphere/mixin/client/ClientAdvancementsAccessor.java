package com.chronosphere.mixin.client;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Accessor mixin for ClientAdvancements to access the private progress field.
 *
 * This accessor allows other mixins to check advancement completion status
 * on the client side without using reflection.
 */
@Mixin(ClientAdvancements.class)
public interface ClientAdvancementsAccessor {

    /**
     * Get the advancement progress map.
     *
     * @return Map of advancement holders to their progress
     */
    @Accessor("progress")
    Map<AdvancementHolder, AdvancementProgress> chronosphere$getProgress();
}
