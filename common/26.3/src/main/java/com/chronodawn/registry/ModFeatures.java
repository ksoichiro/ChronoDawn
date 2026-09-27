package com.chronodawn.registry;

import com.chronodawn.ChronoDawn;
import com.chronodawn.worldgen.features.NbtTemplateFeature;
import com.mojang.serialization.MapCodec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Architectury Registry wrapper for Chrono Dawn custom feature types.
 *
 * <p>Currently registers a single generic {@link NbtTemplateFeature} which is
 * reused for all small-decoration features (wells, cairns, sundials, etc.).
 * Per-feature configuration lives in the corresponding configured_feature JSON.</p>
 *
 * <p>26.3 override: {@code Feature} is no longer generic; the registry is
 * keyed by {@code MapCodec<? extends Feature>} under {@code Registries.FEATURE_TYPE}
 * instead of by {@code Feature<?>} instances under {@code Registries.FEATURE}.</p>
 */
public class ModFeatures {
    public static final DeferredRegister<MapCodec<? extends Feature>> FEATURES =
        DeferredRegister.create(ChronoDawn.MOD_ID, Registries.FEATURE_TYPE);

    public static final RegistrySupplier<MapCodec<? extends Feature>> NBT_TEMPLATE =
        FEATURES.register("nbt_template", () -> NbtTemplateFeature.CODEC);

    public static void register() {
        FEATURES.register();
        ChronoDawn.LOGGER.debug("Registered ModFeatures");
    }

    private ModFeatures() {
        throw new UnsupportedOperationException("Utility class");
    }
}
