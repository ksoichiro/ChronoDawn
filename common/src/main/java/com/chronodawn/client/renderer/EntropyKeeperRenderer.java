package com.chronodawn.client.renderer;

import com.chronodawn.ChronoDawn;
import com.chronodawn.client.model.EntropyKeeperModel;
import com.chronodawn.entities.bosses.EntropyKeeperEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Entropy Keeper entity.
 *
 * Uses custom EntropyKeeperModel created with Blockbench.
 *
 * Task: T237 [Phase 2] Implement Entropy Keeper
 */
public class EntropyKeeperRenderer extends MobRenderer<EntropyKeeperEntity, EntropyKeeperModel<EntropyKeeperEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        ChronoDawn.MOD_ID,
        "textures/entity/entropy_keeper.png"
    );

    // Model layer location for Entropy Keeper
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "entropy_keeper"),
        "main"
    );

    public EntropyKeeperRenderer(EntityRendererProvider.Context context) {
        super(context, new EntropyKeeperModel<>(context.bakeLayer(LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntropyKeeperEntity entity) {
        return TEXTURE;
    }
}

