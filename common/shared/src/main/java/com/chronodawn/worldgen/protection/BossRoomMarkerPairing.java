package com.chronodawn.worldgen.protection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure pairing rules for Boss Room Boundary Markers.
 *
 * Split out of BossRoomProtectionProcessor so the rules can be unit tested without a
 * ServerLevel, a registry bootstrap or the processor's static marker maps.
 */
public final class BossRoomMarkerPairing {
    private BossRoomMarkerPairing() {
    }

    /** What the pairing rules need to know about one marker. */
    public interface Marker {
        BlockPos pos();

        /** Position relative to the structure piece, stable across placements. */
        BlockPos structureRelativePos();

        /** When the marker was recorded; the newer one wins among markers at one position. */
        long addedTime();
    }

    public record Pair<M extends Marker>(M min, M max) {
    }

    /**
     * @param pairs        min/max pairs in pairing order
     * @param unpairedMins min markers left over when there are more mins than maxs
     * @param unpairedMaxs max markers left over when there are more maxs than mins
     */
    public record Result<M extends Marker>(List<Pair<M>> pairs, List<M> unpairedMins, List<M> unpairedMaxs) {
    }

    /**
     * Pair the markers of one placement.
     *
     * Markers at the same position are reduced to the newest one (a later template block, such
     * as stairs_bottom over a stairs max marker, can overwrite a marker). Both sides are then sorted by structure-relative Y, highest first,
     * and paired by index. The order comes from the template and not from the iteration order
     * of the concurrent map the markers were collected from, so the result is deterministic.
     * The sort is stable and has no tie-breaker beyond Y, so markers that share a Y keep the
     * order of the input list.
     */
    public static <M extends Marker> Result<M> pair(Collection<M> mins, Collection<M> maxs) {
        List<M> sortedMins = dedupeAndSort(mins);
        List<M> sortedMaxs = dedupeAndSort(maxs);

        int pairCount = Math.min(sortedMins.size(), sortedMaxs.size());
        List<Pair<M>> pairs = new ArrayList<>(pairCount);
        for (int i = 0; i < pairCount; i++) {
            pairs.add(new Pair<>(sortedMins.get(i), sortedMaxs.get(i)));
        }
        return new Result<>(
            pairs,
            new ArrayList<>(sortedMins.subList(pairCount, sortedMins.size())),
            new ArrayList<>(sortedMaxs.subList(pairCount, sortedMaxs.size()))
        );
    }

    private static <M extends Marker> List<M> dedupeAndSort(Collection<M> markers) {
        Map<BlockPos, M> newestByPos = new LinkedHashMap<>();
        for (M marker : markers) {
            M existing = newestByPos.get(marker.pos());
            if (existing == null || marker.addedTime() > existing.addedTime()) {
                newestByPos.put(marker.pos(), marker);
            }
        }
        List<M> sorted = new ArrayList<>(newestByPos.values());
        sorted.sort(Comparator.comparingInt((M m) -> m.structureRelativePos().getY()).reversed());
        return sorted;
    }

    /** The protected area spanned by a min and a max marker, whichever corner each one is. */
    public static BoundingBox areaOf(BlockPos a, BlockPos b) {
        return new BoundingBox(
            Math.min(a.getX(), b.getX()),
            Math.min(a.getY(), b.getY()),
            Math.min(a.getZ(), b.getZ()),
            Math.max(a.getX(), b.getX()),
            Math.max(a.getY(), b.getY()),
            Math.max(a.getZ(), b.getZ())
        );
    }
}
