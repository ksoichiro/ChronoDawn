/*
 * Copyright (C) 2025 ksoichiro
 *
 * This file is part of Chrono Dawn.
 *
 * Chrono Dawn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Chrono Dawn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chrono Dawn. If not, see <https://www.gnu.org/licenses/>.
 */
package com.chronodawn.mixin;

import com.chronodawn.ChronoDawn;
import com.chronodawn.worldgen.runtime.OverlayPackBootstrap;
import com.chronodawn.worldgen.runtime.OverlayPackInjection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.nio.file.Path;

/**
 * Fabric-only mixin (1.20.1 variant) that appends the Chrono Dawn runtime
 * config overlay to the {@link PackRepository} varargs at construction time.
 *
 * <p>1.20.1 lacks {@code PackLocationInfo} and
 * {@code PathPackResources.PathResourcesSupplier} (both added in 1.20.5),
 * so this version uses the older
 * {@code Pack.readMetaAndCreate(name, title, alwaysActive, resources, type, position, source)}
 * overload and constructs {@link PathPackResources} directly with
 * {@code (name, path, isBuiltIn)}.
 */
@Mixin(PackRepository.class)
public abstract class PackRepositoryOverlayMixin {

    @ModifyVariable(
        method = "<init>([Lnet/minecraft/server/packs/repository/RepositorySource;)V",
        at = @At("HEAD"),
        argsOnly = true
    )
    private static RepositorySource[] chronodawn$injectOverlaySource(RepositorySource[] sources) {
        if (!OverlayPackInjection.isServerDataRepository(sources)) {
            return sources;
        }
        Path overlayPath = OverlayPackBootstrap.getOverlayPath();
        if (overlayPath == null) {
            return sources;
        }

        RepositorySource overlaySource = consumer -> {
            Pack.ResourcesSupplier resourcesSupplier = name -> new PathPackResources(
                OverlayPackBootstrap.PACK_ID,
                overlayPath,
                false
            );
            Pack pack = Pack.readMetaAndCreate(
                OverlayPackBootstrap.PACK_ID,
                Component.literal("ChronoDawn config overlay"),
                true,
                resourcesSupplier,
                PackType.SERVER_DATA,
                Pack.Position.TOP,
                PackSource.BUILT_IN
            );
            if (pack != null) {
                consumer.accept(pack);
            } else {
                ChronoDawn.LOGGER.warn(
                    "Runtime overlay pack at {} failed metadata validation; not registering",
                    overlayPath
                );
            }
        };

        RepositorySource[] extended = new RepositorySource[sources.length + 1];
        System.arraycopy(sources, 0, extended, 0, sources.length);
        extended[sources.length] = overlaySource;
        ChronoDawn.LOGGER.info("Registered runtime config overlay datapack with Fabric (PackRepository mixin)");
        return extended;
    }
}
