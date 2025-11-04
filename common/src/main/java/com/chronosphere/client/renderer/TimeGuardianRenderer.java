package com.chronosphere.client.renderer;

import com.chronosphere.Chronosphere;
import com.chronosphere.entities.bosses.TimeGuardianEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Time Guardian entity.
 *
 * Currently uses a humanoid model as a temporary placeholder.
 * In future phases, this will be replaced with a custom model and texture.
 *
 * Reference: T110-T115 [US2] Time Guardian implementation
 */
public class TimeGuardianRenderer extends MobRenderer<TimeGuardianEntity, HumanoidModel<TimeGuardianEntity>> {
    // Temporary texture (uses zombie texture as placeholder)
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");

    public TimeGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
        // Add armor layer for better visualization
        this.addLayer(new HumanoidArmorLayer<>(this,
            new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
            new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)),
            context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(TimeGuardianEntity entity) {
        return TEXTURE;
    }
}
