package com.chronodawn.client.renderer.mobs;

import com.chronodawn.client.model.SecondhandArcherModel;
import com.chronodawn.entities.mobs.SecondhandArcherEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for Secondhand Archer entity.
 * Texture location: assets/chronodawn/textures/entity/mobs/secondhand_archer.png
 */
public class SecondhandArcherRenderer extends MobRenderer<SecondhandArcherEntity, SecondhandArcherRenderState, SecondhandArcherModel> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        "chronodawn",
        "textures/entity/mobs/secondhand_archer.png"
    );

    private final ItemModelResolver itemModelResolver;

    public SecondhandArcherRenderer(EntityRendererProvider.Context context) {
        super(context, new SecondhandArcherModel(context.bakeLayer(SecondhandArcherModel.LAYER_LOCATION)), 0.5f);
        this.itemModelResolver = context.getItemModelResolver();
        this.addLayer(new ItemInHandLayer<>(this));
    }

    @Override
    public SecondhandArcherRenderState createRenderState() {
        return new SecondhandArcherRenderState();
    }

    @Override
    public void extractRenderState(SecondhandArcherEntity entity, SecondhandArcherRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isAggressive = entity.isAggressive();
        // Update hand item states for 1.21.4
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, this.itemModelResolver);
    }

    @Override
    public ResourceLocation getTextureLocation(SecondhandArcherRenderState state) {
        return TEXTURE;
    }
}
