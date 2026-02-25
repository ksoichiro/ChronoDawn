package com.chronodawn.mixin.client;

import net.minecraft.advancements.Advancement;
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
     * @return Map of advancements to their progress (1.20.1 uses Advancement instead of AdvancementHolder)
     */
    @Accessor("progress")
    Map<Advancement, AdvancementProgress> chronodawn$getProgress();
}
