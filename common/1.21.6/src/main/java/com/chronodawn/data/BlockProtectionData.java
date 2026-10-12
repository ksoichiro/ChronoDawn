package com.chronodawn.data;

import com.chronodawn.compat.CompatSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Persists temporary boss-room protection for a dimension. */
public final class BlockProtectionData extends CompatSavedData {
    private static final String DATA_NAME = "chronodawn_block_protection";
    private static final Set<BlockProtectionData> ACTIVE_INSTANCES = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<String, Bounds> protectedAreas = new ConcurrentHashMap<>();
    private final Set<String> defeatedBosses = ConcurrentHashMap.newKeySet();

    public static BlockProtectionData get(ServerLevel level) {
        BlockProtectionData data = CompatSavedData.computeIfAbsent(
            level.getDataStorage(), BlockProtectionData::new, BlockProtectionData::load, DATA_NAME
        );
        ACTIVE_INSTANCES.add(data);
        return data;
    }

    public static BlockProtectionData load(CompoundTag tag) {
        BlockProtectionData data = new BlockProtectionData();
        data.loadData(tag);
        return data;
    }

    @Override
    public void loadData(CompoundTag tag) {
        ListTag areas = tag.getListOrEmpty("Areas");
        for (int i = 0; i < areas.size(); i++) {
            CompoundTag area = areas.getCompound(i).orElse(new CompoundTag());
            protectedAreas.put(area.getStringOr("Key", ""), Bounds.load(area));
        }
        ListTag defeated = tag.getListOrEmpty("Defeated");
        for (int i = 0; i < defeated.size(); i++) {
            defeatedBosses.add(defeated.getCompound(i).orElse(new CompoundTag()).getStringOr("Key", ""));
        }
    }

    @Override
    public CompoundTag saveData(CompoundTag tag) {
        ListTag areas = new ListTag();
        protectedAreas.forEach((key, bounds) -> areas.add(bounds.save(key)));
        tag.put("Areas", areas);
        ListTag defeated = new ListTag();
        defeatedBosses.forEach(key -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("Key", key);
            defeated.add(entry);
        });
        tag.put("Defeated", defeated);
        return tag;
    }

    public void register(String key, BoundingBox area) {
        Bounds bounds = Bounds.from(area);
        if (!bounds.equals(protectedAreas.put(key, bounds))) {
            setDirty();
        }
    }

    public void markDefeated(String key) {
        if (defeatedBosses.add(key)) {
            setDirty();
        }
    }

    public boolean isProtected(BlockPos pos) {
        return protectedAreas.entrySet().stream()
            .anyMatch(entry -> !defeatedBosses.contains(entry.getKey()) && entry.getValue().isInside(pos));
    }

    public String findAreaAt(BlockPos pos) {
        return protectedAreas.entrySet().stream()
            .filter(entry -> entry.getValue().isInside(pos))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    /** Clears loaded data for GameTests and explicit world-reset tooling. */
    public static void resetLoadedData() {
        ACTIVE_INSTANCES.forEach(data -> {
            data.protectedAreas.clear();
            data.defeatedBosses.clear();
            data.setDirty();
        });
    }

    private record Bounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        static Bounds from(BoundingBox box) {
            return new Bounds(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
        }

        static Bounds load(CompoundTag tag) {
            return new Bounds(tag.getIntOr("MinX", 0), tag.getIntOr("MinY", 0), tag.getIntOr("MinZ", 0),
                tag.getIntOr("MaxX", 0), tag.getIntOr("MaxY", 0), tag.getIntOr("MaxZ", 0));
        }

        boolean isInside(BlockPos pos) {
            return pos.getX() >= minX && pos.getX() <= maxX && pos.getY() >= minY && pos.getY() <= maxY
                && pos.getZ() >= minZ && pos.getZ() <= maxZ;
        }

        CompoundTag save(String key) {
            CompoundTag tag = new CompoundTag();
            tag.putString("Key", key);
            tag.putInt("MinX", minX); tag.putInt("MinY", minY); tag.putInt("MinZ", minZ);
            tag.putInt("MaxX", maxX); tag.putInt("MaxY", maxY); tag.putInt("MaxZ", maxZ);
            return tag;
        }
    }
}
