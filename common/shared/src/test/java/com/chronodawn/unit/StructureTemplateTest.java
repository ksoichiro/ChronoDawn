package com.chronodawn.unit;

import com.chronodawn.gametest.StructureSpecs;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class StructureTemplateTest {
    private static final Set<String> DISALLOWED = Set.of("minecraft:dirt", "minecraft:grass_block",
        "minecraft:coarse_dirt", "minecraft:smooth_stone", "minecraft:cracked_stone_bricks",
        "minecraft:chiseled_stone_bricks");
    /**
     * Rigid surface structures placed with terrain adaptation. Explicit air above a column's
     * topmost block would cut the terrain on the high side of a slope away in the shape of the
     * template's box, so that air must be left out (see scripts/strip_exterior_air.py).
     */
    private static final Set<String> TERRAIN_PRESERVING = Set.of("ancient_ruins", "desert_clock_tower",
        "forgotten_library", "clockwork_depths_tower", "entropy_crypt_entrance", "master_clock_surface",
        "phantom_catacombs_entrance", "time_keeper_village");

    @Test
    void validateTemplates() throws Exception {
        String directory = getClass().getClassLoader().getResource("data/chronodawn/structure/empty_test.nbt") != null
            ? "data/chronodawn/structure/" : "data/chronodawn/structures/";
        URL url = getClass().getClassLoader().getResource(directory);
        assertNotNull(url, "Structure resource directory is missing");
        Set<String> specs = StructureSpecs.getStructureSpecs().stream().map(StructureSpecs.StructureSpec::id)
            .collect(Collectors.toSet());
        Path root = Path.of(url.toURI());
        try (var files = Files.walk(root)) {
            Set<String> actual = files.filter(Files::isRegularFile).map(p -> root.relativize(p).toString())
                .filter(name -> name.endsWith(".nbt")).map(name -> name.substring(0, name.length() - 4))
                .collect(Collectors.toSet());
            actual.remove("empty_test");
            assertEquals(specs, actual, "Structure specifications must cover all templates");
        }
        for (var spec : StructureSpecs.getStructureSpecs()) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(directory + spec.id() + ".nbt")) {
                assertNotNull(input, spec.id());
                var nbt = readRoot(input);
                var size = list(nbt.get("size"));
                assertTrue((int) size.get(0) >= spec.minWidth() && (int) size.get(1) >= spec.minHeight()
                    && (int) size.get(2) >= spec.minDepth(), spec.id() + " has insufficient dimensions: " + size);
                var palette = list(nbt.get("palette"));
                var blocks = list(nbt.get("blocks"));
                Map<String, Integer> counts = new HashMap<>();
                Map<List<?>, String> positions = new HashMap<>();
                Set<List<?>> replacedWithAir = new HashSet<>();
                for (Object entry : blocks) {
                    var block = compound(entry);
                    String name = (String) compound(palette.get((int) block.get("state"))).get("Name");
                    counts.merge(name, 1, Integer::sum);
                    positions.put(list(block.get("pos")), name);
                    if (block.get("nbt") != null && "minecraft:air".equals(compound(block.get("nbt")).get("ReplaceWith"))) {
                        replacedWithAir.add(list(block.get("pos")));
                    }
                }
                for (var requirement : spec.blockRequirements()) {
                    int count = counts.getOrDefault(requirement.name(), 0);
                    assertTrue(count >= requirement.minCount(), spec.id() + " contains " + count + " "
                        + requirement.name() + ", expected at least " + requirement.minCount());
                }
                if (!spec.id().equals("ancient_ruins")) {
                    for (String name : DISALLOWED) {
                        assertEquals(0, counts.getOrDefault(name, 0), spec.id() + " contains " + name);
                    }
                }
                if (TERRAIN_PRESERVING.contains(spec.id())) {
                    assertNoAirAboveTopBlock(spec.id(), positions, replacedWithAir);
                }
                if (spec.id().equals("watchmaker_camp")) {
                    Set<String> required = spec.blockRequirements().stream().map(StructureSpecs.BlockRequirement::name)
                        .filter(name -> !name.equals("minecraft:chest")).collect(Collectors.toSet());
                    positions.forEach((pos, name) -> {
                        if (name.equals("minecraft:chest")) {
                            String above = positions.get(List.of(pos.get(0), (int) pos.get(1) + 1, pos.get(2)));
                            assertTrue(above == null || !required.contains(above), "Camp chest has " + above + " above it");
                        }
                    });
                }
            }
        }
    }

    /**
     * Uses the same covering blocks as scripts/strip_exterior_air.py: vines, leaves and blocks that
     * a processor replaces with air do not cover the air below them.
     */
    private static void assertNoAirAboveTopBlock(String id, Map<List<?>, String> positions,
                                                 Set<List<?>> replacedWithAir) {
        Map<List<?>, Integer> top = new HashMap<>();
        positions.forEach((pos, name) -> {
            if (!name.equals("minecraft:air") && !name.contains("vine") && !name.endsWith("_leaves")
                    && !replacedWithAir.contains(pos)) {
                top.merge(List.of(pos.get(0), pos.get(2)), (int) pos.get(1), Math::max);
            }
        });
        long exposed = positions.entrySet().stream().filter(e -> e.getValue().equals("minecraft:air"))
            .filter(e -> (int) e.getKey().get(1) > top.getOrDefault(List.of(e.getKey().get(0), e.getKey().get(2)), -1))
            .count();
        assertEquals(0, exposed, id + " has air above the topmost block of a column, which cuts the terrain away");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> compound(Object value) { return (Map<String, Object>) value; }
    @SuppressWarnings("unchecked")
    private static List<Object> list(Object value) { return (List<Object>) value; }

    private static Map<String, Object> readRoot(InputStream input) throws IOException {
        try (var in = new DataInputStream(new GZIPInputStream(input))) {
            if (in.readUnsignedByte() != 10) throw new IOException("Expected root compound");
            in.readUTF();
            return compound(readTag(in, 10));
        }
    }

    private static Object readTag(DataInputStream in, int type) throws IOException {
        switch (type) {
            case 1: return in.readByte();
            case 2: return in.readShort();
            case 3: return in.readInt();
            case 4: return in.readLong();
            case 5: return in.readFloat();
            case 6: return in.readDouble();
            case 7: return in.readNBytes(in.readInt());
            case 8: return in.readUTF();
            case 9: {
                int elementType = in.readUnsignedByte();
                int size = in.readInt();
                List<Object> result = new ArrayList<>(size);
                for (int i = 0; i < size; i++) result.add(readTag(in, elementType));
                return result;
            }
            case 10: {
                Map<String, Object> result = new HashMap<>();
                int childType;
                while ((childType = in.readUnsignedByte()) != 0) {
                    String name = in.readUTF();
                    result.put(name, readTag(in, childType));
                }
                return result;
            }
            case 11: {
                int[] result = new int[in.readInt()];
                for (int i = 0; i < result.length; i++) result[i] = in.readInt();
                return result;
            }
            case 12: {
                long[] result = new long[in.readInt()];
                for (int i = 0; i < result.length; i++) result[i] = in.readLong();
                return result;
            }
            default: throw new IOException("Unknown NBT tag " + type);
        }
    }
}
