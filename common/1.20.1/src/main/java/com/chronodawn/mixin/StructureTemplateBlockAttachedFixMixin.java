package com.chronodawn.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

/**
 * Vanilla bug fix: {@code StructureTemplate#placeEntities} rewrites the
 * entity's {@code Pos} in the per-entity NBT before construction so the entity
 * is placed at the correct new world location, but does <b>not</b> rewrite the
 * attachment-block coordinates ({@code TileX/TileY/TileZ}) used by
 * {@link net.minecraft.world.entity.decoration.BlockAttachedEntity}
 * (Item Frame, Painting, etc.). The reconstructed entity therefore reads its
 * <i>saved-time</i> attachment, fails the {@code closerThan(16.0)} sanity check
 * in {@code readAdditionalSaveData}, logs:
 *
 * <pre>{@code Block-attached entity at invalid position: BlockPos{...} }</pre>
 *
 * leaves its {@code pos} field unset, fails its periodic {@code survives()}
 * tick check, and is silently discarded — losing the contents of any Item Frame
 * placed by Chrono Dawn small features (lost_adventurer_memorial, time_well,
 * watchmaker_camp).
 *
 * <p>This mixin rewrites {@code TileX/TileY/TileZ} in the entity NBT to the
 * structure's already-transformed attachment block ({@code blockpos} local)
 * just before the entity is constructed.
 *
 * <p><b>Era:</b> Fabric 1.20.1 / 1.21.1 / 1.21.2 / 1.21.4 (and 1.21.3 via the
 * 1.21.2 module). All share {@code placeEntities} with 7 args and
 * {@code createEntityIgnoreException} with 2 args, and persist the attachment
 * as flat int keys ({@code TileX}, {@code TileY}, {@code TileZ}).
 * 1.21.5+ replaces the int triple with a single {@code block_pos} codec under
 * the same key and uses its own variant of this mixin.
 */
@Mixin(StructureTemplate.class)
public class StructureTemplateBlockAttachedFixMixin {

    @Inject(
        method = "placeEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;createEntityIgnoreException(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/nbt/CompoundTag;)Ljava/util/Optional;"
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void chronodawn$rewriteTilePosForAttachedEntity(
            ServerLevelAccessor level,
            BlockPos origin,
            Mirror mirror,
            Rotation rotation,
            BlockPos pivot,
            BoundingBox boundingBox,
            boolean placeIgnoreEntities,
            CallbackInfo ci,
            Iterator<StructureTemplate.StructureEntityInfo> iter,
            StructureTemplate.StructureEntityInfo info,
            BlockPos blockpos,
            CompoundTag compoundtag,
            Vec3 relativePos,
            Vec3 worldPos,
            ListTag posListTag
    ) {
        if (compoundtag.contains("TileX")) {
            compoundtag.putInt("TileX", blockpos.getX());
            compoundtag.putInt("TileY", blockpos.getY());
            compoundtag.putInt("TileZ", blockpos.getZ());
        }
    }
}
