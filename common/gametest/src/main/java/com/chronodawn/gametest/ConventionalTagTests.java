package com.chronodawn.gametest;

import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.compat.CompatResourceLocation;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Shared runtime verification that the conventional (c:) tags this mod ships
 * actually resolve in game and carry the members they were written for.
 *
 * The build-time validateConventionalTags task only inspects the JSON files:
 * it cannot tell whether the data pack was loaded, whether the c: namespace
 * resolved, or whether our entries merged into a tag the platform defines.
 * That last case is the one that matters most — an ore reaches c:ores only
 * through the platform's own reference to c:ores/coal, so asserting it here
 * proves the cross-mod contract other mods rely on.
 *
 * Only umbrella tags are asserted, because they exist under both tag
 * conventions: on 1.20.1 they list members directly (conventional tags v1),
 * and from 1.21.1 they reach the same members transitively through the
 * per-material subtags (v2). The per-era member lists still differ, so the
 * caller passes the spec list for its own era.
 */
public final class ConventionalTagTests {

    private ConventionalTagTests() {
        // Utility class
    }

    @FunctionalInterface
    public interface TestFactory<T> {
        T create(String name, Consumer<GameTestHelper> test);
    }

    /** One umbrella tag and the mod IDs that must be reachable from it. */
    public record TagSpec(String tagPath, List<String> memberIds) {
    }

    private static final List<String> ORE_IDS = List.of(
            "temporal_coal_ore",
            "temporal_iron_ore",
            "temporal_gold_ore",
            "deepslate_temporal_gold_ore",
            "temporal_redstone_ore",
            "deepslate_temporal_redstone_ore",
            "clockstone_ore",
            "deepslate_clockstone_ore",
            "time_crystal_ore",
            "entropy_crystal_ore",
            "temporal_amber_ore",
            "deepslate_temporal_amber_ore");

    private static final List<String> FOOD_IDS = List.of(
            "time_bread",
            "enhanced_time_bread",
            "time_wheat_cookie",
            "clockwork_cookie",
            "time_fruit_pie",
            "fruit_of_time",
            "chrono_melon_slice",
            "temporal_root",
            "baked_temporal_root",
            "temporal_root_stew",
            "timeless_mushroom_soup",
            "chrono_bovine_meat",
            "cooked_chrono_bovine_meat",
            "glide_fish",
            "cooked_glide_fish",
            "golden_time_wheat",
            "glistening_chrono_melon");

    /** Item-tag expectations for conventional tags v1 (Minecraft 1.20.1). */
    public static List<TagSpec> v1ItemSpecs() {
        return List.of(
                new TagSpec("ores", ORE_IDS),
                new TagSpec("ingots", List.of("clockstone", "enhanced_clockstone")),
                new TagSpec("gems", List.of("time_crystal", "entropy_crystal")),
                new TagSpec("raw_ores", List.of("raw_temporal_amber")),
                new TagSpec("dusts", List.of("temporal_amber_dust")),
                new TagSpec("foods", FOOD_IDS));
    }

    /** Block-tag expectations for conventional tags v1 (Minecraft 1.20.1). */
    public static List<TagSpec> v1BlockSpecs() {
        return List.of(new TagSpec("ores", ORE_IDS));
    }

    /** Item-tag expectations for conventional tags v2 (Minecraft 1.21.1+). */
    public static List<TagSpec> v2ItemSpecs() {
        return List.of(
                new TagSpec("ores", ORE_IDS),
                new TagSpec("ingots", List.of("clockstone", "enhanced_clockstone")),
                new TagSpec("gems", List.of("time_crystal", "entropy_crystal")),
                new TagSpec("raw_materials", List.of("raw_temporal_amber")),
                new TagSpec("dusts", List.of("temporal_amber_dust")),
                new TagSpec("storage_blocks", List.of("clockstone_block", "time_crystal_block")),
                new TagSpec("foods", FOOD_IDS));
    }

    /** Block-tag expectations for conventional tags v2 (Minecraft 1.21.1+). */
    public static List<TagSpec> v2BlockSpecs() {
        return List.of(
                new TagSpec("ores", ORE_IDS),
                new TagSpec("storage_blocks", List.of("clockstone_block", "time_crystal_block")));
    }

    /**
     * Generates one test per (tag, member) pair.
     *
     * @param itemsClass  registry class holding RegistrySupplier&lt;Item&gt; fields
     * @param blocksClass registry class holding RegistrySupplier&lt;Block&gt; fields
     * @param itemSpecs   item-tag expectations for this version's tag convention
     * @param blockSpecs  block-tag expectations for this version's tag convention
     * @param factory     factory to create test instances
     */
    public static <T> List<T> generate(
            Class<?> itemsClass,
            Class<?> blocksClass,
            List<TagSpec> itemSpecs,
            List<TagSpec> blockSpecs,
            TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();

        for (TagSpec spec : itemSpecs) {
            for (String memberId : spec.memberIds()) {
                String testName = "conventional_tag_item_" + spec.tagPath().replace('/', '_') + "_" + memberId;
                tests.add(factory.create(testName, helper -> helper.runAfterDelay(1, () -> {
                    Item item = lookup(itemsClass, Item.class, memberId);
                    if (item == null) {
                        CompatGameTestHelper.fail(helper, "Item not registered: " + memberId);
                        return;
                    }
                    TagKey<Item> tag = TagKey.create(Registries.ITEM,
                            CompatResourceLocation.create("c", spec.tagPath()));
                    if (!new ItemStack(item).is(tag)) {
                        CompatGameTestHelper.fail(helper, memberId + " is not in item tag c:" + spec.tagPath());
                        return;
                    }
                    helper.succeed();
                })));
            }
        }

        for (TagSpec spec : blockSpecs) {
            for (String memberId : spec.memberIds()) {
                String testName = "conventional_tag_block_" + spec.tagPath().replace('/', '_') + "_" + memberId;
                tests.add(factory.create(testName, helper -> helper.runAfterDelay(1, () -> {
                    Block block = lookup(blocksClass, Block.class, memberId);
                    if (block == null) {
                        CompatGameTestHelper.fail(helper, "Block not registered: " + memberId);
                        return;
                    }
                    TagKey<Block> tag = TagKey.create(Registries.BLOCK,
                            CompatResourceLocation.create("c", spec.tagPath()));
                    if (!block.defaultBlockState().is(tag)) {
                        CompatGameTestHelper.fail(helper, memberId + " is not in block tag c:" + spec.tagPath());
                        return;
                    }
                    helper.succeed();
                })));
            }
        }

        return tests;
    }

    /**
     * Resolves a registry entry by its registered ID, reading the mod's own
     * RegistrySupplier fields rather than the game registries: the supplier
     * API is stable across every supported version, while the registry lookup
     * methods are not.
     */
    private static <R> R lookup(Class<?> registryClass, Class<R> type, String id) {
        Map<String, Object> byId = registryCache.computeIfAbsent(registryClass, ConventionalTagTests::indexSuppliers);
        Object value = byId.get(id);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    private static final Map<Class<?>, Map<String, Object>> registryCache = new HashMap<>();

    private static Map<String, Object> indexSuppliers(Class<?> registryClass) {
        Map<String, Object> byId = new HashMap<>();
        for (Field field : registryClass.getDeclaredFields()) {
            if (!isRegistrySupplierField(field)) continue;
            try {
                RegistrySupplier<?> supplier = (RegistrySupplier<?>) field.get(null);
                byId.put(supplier.getId().getPath(), supplier.get());
            } catch (Exception e) {
                // A supplier that cannot be read is reported by the test that needs it.
            }
        }
        return byId;
    }

    private static boolean isRegistrySupplierField(Field field) {
        return Modifier.isPublic(field.getModifiers())
            && Modifier.isStatic(field.getModifiers())
            && Modifier.isFinal(field.getModifiers())
            && RegistrySupplier.class.isAssignableFrom(field.getType());
    }
}
