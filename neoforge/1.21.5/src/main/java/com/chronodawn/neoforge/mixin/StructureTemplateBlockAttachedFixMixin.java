package com.chronodawn.neoforge.mixin;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

/**
 * Vanilla bug fix (NeoForge variant): {@code StructureTemplate#addEntitiesToWorld}
 * rewrites the entity's {@code Pos} in the per-entity NBT before construction so
 * the entity is placed at the correct new world location, but does <b>not</b>
 * rewrite {@code block_pos}, the attachment-block coordinates used by
 * {@link net.minecraft.world.entity.decoration.BlockAttachedEntity}
 * (Item Frame, Painting, etc.). The reconstructed entity therefore reads its
 * <i>saved-time</i> attachment, fails the {@code closerThan(16.0)} sanity check
 * in {@code readAdditionalSaveData}, logs:
 *
 * <pre>{@code Block-attached entity at invalid position: BlockPos{...} }</pre>
 *
 * leaves its {@code pos} field unset, fails its periodic {@code survives()}
 * tick check, and is silently discarded — losing the contents of any Item Frame
 * placed by Chrono Dawn small features (petrified_adventurer, time_well,
 * watchmaker_camp).
 *
 * <p>This mixin rewrites {@code block_pos} in the entity NBT to the structure's
 * already-transformed attachment block ({@code blockpos} local) just before
 * the entity is constructed.
 *
 * <p><b>Era:</b> NeoForge 1.21.5 only. NeoForge renames vanilla {@code
 * placeEntities} → {@code addEntitiesToWorld} and folds Mirror/Rotation/Pivot/
 * BoundingBox into a single {@link StructurePlaceSettings} parameter. 1.21.5
 * has no {@code ProblemReporter} parameter; 1.21.6+ adds it and uses a separate
 * variant of this mixin. Pre-1.21.5 versions still use {@code TileX/TileY/TileZ}
 * and need a separate mixin (Phase 2).
 */
@Mixin(StructureTemplate.class)
public class StructureTemplateBlockAttachedFixMixin {

    @Inject(
        method = "addEntitiesToWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;createEntityIgnoreException(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/nbt/CompoundTag;)Ljava/util/Optional;"
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void chronodawn$rewriteBlockPosForAttachedEntity(
            ServerLevelAccessor level,
            BlockPos origin,
            StructurePlaceSettings settings,
            CallbackInfo ci,
            Iterator<StructureTemplate.StructureEntityInfo> iter,
            StructureTemplate.StructureEntityInfo info,
            BlockPos blockpos,
            CompoundTag compoundtag,
            Vec3 worldPos,
            ListTag posListTag
    ) {
        if (compoundtag.contains("block_pos")) {
            BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, blockpos)
                    .resultOrPartial(err -> ChronoDawn.LOGGER.warn(
                            "Failed to encode block_pos for structure entity: {}", err))
                    .ifPresent(encoded -> compoundtag.put("block_pos", encoded));
        }
    }
}
