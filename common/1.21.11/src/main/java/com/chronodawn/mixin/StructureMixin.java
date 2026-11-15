package com.chronodawn.mixin;

import com.chronodawn.worldgen.StructureFoundationFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Keeps the surface structures of this mod off steep ground.
 *
 * <p>findValidGenerationPoint is where both generation and the structure check behind
 * {@code /locate} decide whether a structure exists, so changing the result here keeps them in
 * agreement. See {@link StructureFoundationFiller#placeOnGround}.
 */
@Mixin(Structure.class)
public abstract class StructureMixin {

    @Inject(method = "findValidGenerationPoint", at = @At("RETURN"), cancellable = true)
    private void chronodawn$placeOnGround(
        Structure.GenerationContext context,
        CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir
    ) {
        Optional<Structure.GenerationStub> stub = cir.getReturnValue();
        if (stub.isEmpty()) {
            return;
        }
        Structure self = (Structure) (Object) this;
        net.minecraft.resources.Identifier key = context.registryAccess()
            .lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
            .getKey(self);
        if (key == null || !StructureFoundationFiller.appliesTo(key.getNamespace(), key.getPath())) {
            return;
        }
        ChunkPos chunkPos = context.chunkPos();
        cir.setReturnValue(StructureFoundationFiller.placeOnGround(context, stub.get(),
            (dx, dz) -> self.findValidGenerationPoint(
                withChunk(context, new ChunkPos(chunkPos.x + dx, chunkPos.z + dz)))));
    }

    private static Structure.GenerationContext withChunk(Structure.GenerationContext context, ChunkPos chunkPos) {
        return new Structure.GenerationContext(
            context.registryAccess(),
            context.chunkGenerator(),
            context.biomeSource(),
            context.randomState(),
            context.structureTemplateManager(),
            context.seed(),
            chunkPos,
            context.heightAccessor(),
            context.validBiome()
        );
    }
}
