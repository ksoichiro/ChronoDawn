package com.chronodawn.gametest;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.compat.CompatResourceLocation;
import java.util.List;

public final class StructureTests {
    private StructureTests() {}

    public static <T> List<T> generateStructureTests(MobBehaviorTests.TestFactory<T> factory) {
        return List.of(factory.create("structure_templates_load", helper -> helper.runAfterDelay(1, () -> {
            var manager = helper.getLevel().getStructureManager();
            for (var spec : StructureSpecs.getStructureSpecs()) {
                if (manager.get(CompatResourceLocation.create(ChronoDawn.MOD_ID, spec.id())).isEmpty()) {
                    CompatGameTestHelper.fail(helper, "Structure template '" + spec.id() + "' could not be loaded");
                    return;
                }
            }
            helper.succeed();
        })));
    }
}
