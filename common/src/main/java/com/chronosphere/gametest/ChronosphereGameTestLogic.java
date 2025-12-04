package com.chronosphere.gametest;

import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModEntities;
import com.chronosphere.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * Common GameTest logic shared between Fabric and NeoForge implementations.
 *
 * This class provides the test logic as Consumer<GameTestHelper> functions
 * that can be used by both platform-specific GameTest implementations.
 *
 * Reference: T172f/T173d - Common GameTest Logic
 */
public final class ChronosphereGameTestLogic {

    private ChronosphereGameTestLogic() {
        // Utility class
    }

    // Standard test position
    public static final BlockPos TEST_POS = new BlockPos(1, 2, 1);

    // Expected health values
    public static final float TIME_GUARDIAN_HEALTH = 200.0f;
    public static final float TIME_TYRANT_HEALTH = 500.0f;

    // Expected armor values
    public static final double TIME_GUARDIAN_ARMOR = 10.0;
    public static final double TIME_TYRANT_ARMOR = 15.0;

    // Expected knockback resistance values
    public static final double TIME_GUARDIAN_KNOCKBACK_RESISTANCE = 0.8;
    public static final double TIME_TYRANT_KNOCKBACK_RESISTANCE = 1.0; // Complete immunity

    // Expected attack damage values
    public static final double TIME_TYRANT_ATTACK_DAMAGE = 18.0;

    // Chronoblade expected values
    public static final int CHRONOBLADE_DURABILITY = 2000;

    // ============== Entity Spawn Tests ==============

    /**
     * Test that Time Guardian entity can be spawned.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_GUARDIAN_CAN_SPAWN = helper -> {
        helper.spawn(ModEntities.TIME_GUARDIAN.get(), TEST_POS);
        helper.succeedWhenEntityPresent(ModEntities.TIME_GUARDIAN.get(), TEST_POS);
    };

    /**
     * Test that Time Tyrant entity can be spawned.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_TYRANT_CAN_SPAWN = helper -> {
        helper.spawn(ModEntities.TIME_TYRANT.get(), TEST_POS);
        helper.succeedWhenEntityPresent(ModEntities.TIME_TYRANT.get(), TEST_POS);
    };

    /**
     * Test that Temporal Wraith entity can be spawned.
     */
    public static final Consumer<GameTestHelper> TEST_TEMPORAL_WRAITH_CAN_SPAWN = helper -> {
        helper.spawn(ModEntities.TEMPORAL_WRAITH.get(), TEST_POS);
        helper.succeedWhenEntityPresent(ModEntities.TEMPORAL_WRAITH.get(), TEST_POS);
    };

    /**
     * Test that Clockwork Sentinel entity can be spawned.
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKWORK_SENTINEL_CAN_SPAWN = helper -> {
        helper.spawn(ModEntities.CLOCKWORK_SENTINEL.get(), TEST_POS);
        helper.succeedWhenEntityPresent(ModEntities.CLOCKWORK_SENTINEL.get(), TEST_POS);
    };

    // ============== Block Placement Tests ==============

    /**
     * Test that Time Wood Log block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_WOOD_LOG_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TIME_WOOD_LOG.get());
        helper.succeedWhenBlockPresent(ModBlocks.TIME_WOOD_LOG.get(), TEST_POS);
    };

    /**
     * Test that Time Wood Planks block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_WOOD_PLANKS_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TIME_WOOD_PLANKS.get());
        helper.succeedWhenBlockPresent(ModBlocks.TIME_WOOD_PLANKS.get(), TEST_POS);
    };

    /**
     * Test that Clockstone Block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKSTONE_BLOCK_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.CLOCKSTONE_BLOCK.get());
        helper.succeedWhenBlockPresent(ModBlocks.CLOCKSTONE_BLOCK.get(), TEST_POS);
    };

    // ============== Entity Health Tests ==============

    /**
     * Test that Time Guardian has correct initial health (200 HP).
     */
    public static final Consumer<GameTestHelper> TEST_TIME_GUARDIAN_INITIAL_HEALTH = helper -> {
        var entity = helper.spawn(ModEntities.TIME_GUARDIAN.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            float actualHealth = entity.getHealth();

            if (Math.abs(actualHealth - TIME_GUARDIAN_HEALTH) < 0.1f) {
                helper.succeed();
            } else {
                helper.fail("Time Guardian health was " + actualHealth + ", expected " + TIME_GUARDIAN_HEALTH);
            }
        });
    };

    /**
     * Test that Time Tyrant has correct initial health (500 HP).
     */
    public static final Consumer<GameTestHelper> TEST_TIME_TYRANT_INITIAL_HEALTH = helper -> {
        var entity = helper.spawn(ModEntities.TIME_TYRANT.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            float actualHealth = entity.getHealth();

            if (Math.abs(actualHealth - TIME_TYRANT_HEALTH) < 0.1f) {
                helper.succeed();
            } else {
                helper.fail("Time Tyrant health was " + actualHealth + ", expected " + TIME_TYRANT_HEALTH);
            }
        });
    };

    // ============== Entity Attribute Tests (Migrated from @Disabled) ==============

    /**
     * Test that Time Guardian has correct armor value (10.0).
     * Migrated from: TimeGuardianFightTest.testTimeGuardianHealthAndArmorValues
     */
    public static final Consumer<GameTestHelper> TEST_TIME_GUARDIAN_ARMOR = helper -> {
        var entity = helper.spawn(ModEntities.TIME_GUARDIAN.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            double actualArmor = entity.getAttributeValue(Attributes.ARMOR);

            if (Math.abs(actualArmor - TIME_GUARDIAN_ARMOR) < 0.1) {
                helper.succeed();
            } else {
                helper.fail("Time Guardian armor was " + actualArmor + ", expected " + TIME_GUARDIAN_ARMOR);
            }
        });
    };

    /**
     * Test that Time Guardian has high knockback resistance (0.8).
     * Migrated from: TimeGuardianFightTest.testTimeGuardianCannotBeKnockedBack
     */
    public static final Consumer<GameTestHelper> TEST_TIME_GUARDIAN_KNOCKBACK_RESISTANCE = helper -> {
        var entity = helper.spawn(ModEntities.TIME_GUARDIAN.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            double actualResistance = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);

            if (Math.abs(actualResistance - TIME_GUARDIAN_KNOCKBACK_RESISTANCE) < 0.1) {
                helper.succeed();
            } else {
                helper.fail("Time Guardian knockback resistance was " + actualResistance +
                    ", expected " + TIME_GUARDIAN_KNOCKBACK_RESISTANCE);
            }
        });
    };

    // ============== Item Attribute Tests (Migrated from @Disabled) ==============

    /**
     * Test that Chronoblade has correct durability (2000).
     * Migrated from: ChronobladeTest.testChronobladeHighDurability
     */
    public static final Consumer<GameTestHelper> TEST_CHRONOBLADE_DURABILITY = helper -> {
        ItemStack chronoblade = new ItemStack(ModItems.CHRONOBLADE.get());

        helper.runAfterDelay(1, () -> {
            int actualDurability = chronoblade.getMaxDamage();

            if (actualDurability == CHRONOBLADE_DURABILITY) {
                helper.succeed();
            } else {
                helper.fail("Chronoblade durability was " + actualDurability + ", expected " + CHRONOBLADE_DURABILITY);
            }
        });
    };

    // ============== Time Tyrant Attribute Tests ==============

    /**
     * Test that Time Tyrant has correct armor value (15.0).
     * Migrated from: TimeTyrantFightTest (implicit in health/armor tests)
     */
    public static final Consumer<GameTestHelper> TEST_TIME_TYRANT_ARMOR = helper -> {
        var entity = helper.spawn(ModEntities.TIME_TYRANT.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            double actualArmor = entity.getAttributeValue(Attributes.ARMOR);

            if (Math.abs(actualArmor - TIME_TYRANT_ARMOR) < 0.1) {
                helper.succeed();
            } else {
                helper.fail("Time Tyrant armor was " + actualArmor + ", expected " + TIME_TYRANT_ARMOR);
            }
        });
    };

    /**
     * Test that Time Tyrant has complete knockback immunity (1.0).
     * Migrated from: TimeTyrantFightTest.testTimeTyrantCannotBeKnockedBackOrPushed
     */
    public static final Consumer<GameTestHelper> TEST_TIME_TYRANT_KNOCKBACK_RESISTANCE = helper -> {
        var entity = helper.spawn(ModEntities.TIME_TYRANT.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            double actualResistance = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);

            if (Math.abs(actualResistance - TIME_TYRANT_KNOCKBACK_RESISTANCE) < 0.1) {
                helper.succeed();
            } else {
                helper.fail("Time Tyrant knockback resistance was " + actualResistance +
                    ", expected " + TIME_TYRANT_KNOCKBACK_RESISTANCE);
            }
        });
    };

    /**
     * Test that Time Tyrant has correct attack damage (18.0 = 9 hearts).
     * Migrated from: TimeTyrantFightTest (implicit in combat tests)
     */
    public static final Consumer<GameTestHelper> TEST_TIME_TYRANT_ATTACK_DAMAGE = helper -> {
        var entity = helper.spawn(ModEntities.TIME_TYRANT.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            double actualDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);

            if (Math.abs(actualDamage - TIME_TYRANT_ATTACK_DAMAGE) < 0.1) {
                helper.succeed();
            } else {
                helper.fail("Time Tyrant attack damage was " + actualDamage +
                    ", expected " + TIME_TYRANT_ATTACK_DAMAGE);
            }
        });
    };
}
