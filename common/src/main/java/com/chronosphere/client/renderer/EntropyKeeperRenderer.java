package com.chronosphere.client.renderer;

import com.chronosphere.Chronosphere;
import com.chronosphere.client.model.TimeGuardianModel;
import com.chronosphere.entities.bosses.EntropyKeeperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Entropy Keeper entity.
 *
 * Uses TimeGuardianModel (same as Time Guardian) with custom texture.
 *
 * Task: T237 [Phase 2] Implement Entropy Keeper
 */
public class EntropyKeeperRenderer extends MobRenderer<EntropyKeeperEntity, TimeGuardianModel<EntropyKeeperEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Chronosphere.MOD_ID,
        "textures/entity/entropy_keeper.png"
    );

    public EntropyKeeperRenderer(EntityRendererProvider.Context context) {
        super(context, new TimeGuardianModel<>(context.bakeLayer(TimeGuardianRenderer.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntropyKeeperEntity entity) {
        return TEXTURE;
    }
}
