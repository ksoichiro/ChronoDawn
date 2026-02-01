package com.chronodawn.neoforge.mixin;

import com.chronodawn.ChronoDawn;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to debug and prevent crash when entity renderer is null.
 * This happens during dimension transitions when an entity type
 * doesn't have a registered renderer.
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void onShouldRender(E entity, Frustum frustum, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        EntityRenderDispatcher self = (EntityRenderDispatcher) (Object) this;
        EntityRenderer<? super E, ?> renderer = self.getRenderer(entity);

        if (renderer == null) {
            ChronoDawn.LOGGER.error("Entity renderer is NULL for entity type: {} (class: {})",
                entity.getType().getDescriptionId(),
                entity.getClass().getName());
            // Return false to skip rendering this entity instead of crashing
            cir.setReturnValue(false);
        }
    }
}
