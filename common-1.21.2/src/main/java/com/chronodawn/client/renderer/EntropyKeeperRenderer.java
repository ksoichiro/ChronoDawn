package com.chronodawn.client.renderer;

import com.chronodawn.ChronoDawn;
import com.chronodawn.client.model.EntropyKeeperModel;
import com.chronodawn.entities.bosses.EntropyKeeperEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;

/**
 * Renderer for Entropy Keeper entity.
 *
 * Uses custom EntropyKeeperModel created with Blockbench.
 *
 * Task: T237 [Phase 2] Implement Entropy Keeper
 */
public class EntropyKeeperRenderer extends MobRenderer<EntropyKeeperEntity, EntropyKeeperRenderState, EntropyKeeperModel<EntropyKeeperRenderState>> {
    private static final ResourceLocation TEXTURE = CompatResourceLocation.create(
        ChronoDawn.MOD_ID,
        "textures/entity/entropy_keeper.png"
    );

    // Model layer location for Entropy Keeper
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        CompatResourceLocation.create(ChronoDawn.MOD_ID, "entropy_keeper"),
        "main"
    );

    public EntropyKeeperRenderer(EntityRendererProvider.Context context) {
        super(context, new EntropyKeeperModel<>(context.bakeLayer(LAYER_LOCATION)), 0.5f);
    }

    @Override
    public EntropyKeeperRenderState createRenderState() {
        return new EntropyKeeperRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(EntropyKeeperRenderState state) {
        return TEXTURE;
    }
}

