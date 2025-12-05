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

    // Temporal Wraith expected values
    public static final float TEMPORAL_WRAITH_HEALTH = 20.0f;
    public static final double TEMPORAL_WRAITH_ATTACK_DAMAGE = 4.0;

    // Clockwork Sentinel expected values
    public static final float CLOCKWORK_SENTINEL_HEALTH = 30.0f;
    public static final double CLOCKWORK_SENTINEL_ATTACK_DAMAGE = 6.0;
    public static final double CLOCKWORK_SENTINEL_ARMOR = 5.0;

    // Chronos Warden expected values (mini-boss)
    public static final float CHRONOS_WARDEN_HEALTH = 180.0f;
    public static final double CHRONOS_WARDEN_ARMOR = 12.0;

    // Clockwork Colossus expected values (mini-boss)
    public static final float CLOCKWORK_COLOSSUS_HEALTH = 200.0f;
    public static final double CLOCKWORK_COLOSSUS_KNOCKBACK_RESISTANCE = 1.0;

    // Temporal Phantom expected values (mini-boss)
    public static final float TEMPORAL_PHANTOM_HEALTH = 150.0f;
    public static final double TEMPORAL_PHANTOM_ATTACK_DAMAGE = 8.0;

    // Entropy Keeper expected values (mini-boss)
    public static final float ENTROPY_KEEPER_HEALTH = 160.0f;
    public static final double ENTROPY_KEEPER_ARMOR = 6.0;

    // Floq expected values (slime-like mob)
    public static final float FLOQ_HEALTH = 16.0f;
    public static final double FLOQ_ATTACK_DAMAGE = 3.0;

    // Time Keeper expected values (neutral trader)
    public static final float TIME_KEEPER_HEALTH = 20.0f;

    // Chronoblade expected values
    public static final int CHRONOBLADE_DURABILITY = 2000;

    // Clockstone tool expected values
    public static final int CLOCKSTONE_TOOL_DURABILITY = 450;

    // Enhanced Clockstone tool expected values
    public static final int ENHANCED_CLOCKSTONE_TOOL_DURABILITY = 1200;

    // Time Guardian attack damage (missing from previous tests)
    public static final double TIME_GUARDIAN_ATTACK_DAMAGE = 10.0;

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

    // ============== Temporal Wraith Attribute Tests ==============

    /**
     * Test that Temporal Wraith has correct initial health (20 HP = 10 hearts).
     */
    public static final Consumer<GameTestHelper> TEST_TEMPORAL_WRAITH_INITIAL_HEALTH = helper -> {
        var entity = helper.spawn(ModEntities.TEMPORAL_WRAITH.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            float actualHealth = entity.getHealth();

            if (Math.abs(actualHealth - TEMPORAL_WRAITH_HEALTH) < 0.1f) {
                helper.succeed();
            } else {
                helper.fail("Temporal Wraith health was " + actualHealth + ", expected " + TEMPORAL_WRAITH_HEALTH);
            }
        });
    };

    /**
     * Test that Temporal Wraith has correct attack damage (4.0 = 2 hearts).
     */
    public static final Consumer<GameTestHelper> TEST_TEMPORAL_WRAITH_ATTACK_DAMAGE = helper -> {
        var entity = helper.spawn(ModEntities.TEMPORAL_WRAITH.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            double actualDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);

            if (Math.abs(actualDamage - TEMPORAL_WRAITH_ATTACK_DAMAGE) < 0.1) {
                helper.succeed();
            } else {
                helper.fail("Temporal Wraith attack damage was " + actualDamage +
                    ", expected " + TEMPORAL_WRAITH_ATTACK_DAMAGE);
            }
        });
    };

    // ============== Clockwork Sentinel Attribute Tests ==============

    /**
     * Test that Clockwork Sentinel has correct initial health (30 HP = 15 hearts).
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKWORK_SENTINEL_INITIAL_HEALTH = helper -> {
        var entity = helper.spawn(ModEntities.CLOCKWORK_SENTINEL.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            float actualHealth = entity.getHealth();

            if (Math.abs(actualHealth - CLOCKWORK_SENTINEL_HEALTH) < 0.1f) {
                helper.succeed();
            } else {
                helper.fail("Clockwork Sentinel health was " + actualHealth + ", expected " + CLOCKWORK_SENTINEL_HEALTH);
            }
        });
    };

    /**
     * Test that Clockwork Sentinel has correct attack damage (6.0 = 3 hearts).
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKWORK_SENTINEL_ATTACK_DAMAGE = helper -> {
        var entity = helper.spawn(ModEntities.CLOCKWORK_SENTINEL.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            double actualDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);

            if (Math.abs(actualDamage - CLOCKWORK_SENTINEL_ATTACK_DAMAGE) < 0.1) {
                helper.succeed();
            } else {
                helper.fail("Clockwork Sentinel attack damage was " + actualDamage +
                    ", expected " + CLOCKWORK_SENTINEL_ATTACK_DAMAGE);
            }
        });
    };

    /**
     * Test that Clockwork Sentinel has correct armor value (5.0).
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKWORK_SENTINEL_ARMOR = helper -> {
        var entity = helper.spawn(ModEntities.CLOCKWORK_SENTINEL.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            double actualArmor = entity.getAttributeValue(Attributes.ARMOR);

            if (Math.abs(actualArmor - CLOCKWORK_SENTINEL_ARMOR) < 0.1) {
                helper.succeed();
            } else {
                helper.fail("Clockwork Sentinel armor was " + actualArmor + ", expected " + CLOCKWORK_SENTINEL_ARMOR);
            }
        });
    };

    // ============== Mini-Boss Attribute Tests ==============

    /**
     * Test that Chronos Warden has correct initial health (180 HP = 90 hearts).
     */
    public static final Consumer<GameTestHelper> TEST_CHRONOS_WARDEN_INITIAL_HEALTH = helper -> {
        var entity = helper.spawn(ModEntities.CHRONOS_WARDEN.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            float actualHealth = entity.getHealth();

            if (Math.abs(actualHealth - CHRONOS_WARDEN_HEALTH) < 0.1f) {
                helper.succeed();
            } else {
                helper.fail("Chronos Warden health was " + actualHealth + ", expected " + CHRONOS_WARDEN_HEALTH);
            }
        });
    };

    /**
     * Test that Chronos Warden has correct armor value (12.0).
     */
    public static final Consumer<GameTestHelper> TEST_CHRONOS_WARDEN_ARMOR = helper -> {
        var entity = helper.spawn(ModEntities.CHRONOS_WARDEN.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            double actualArmor = entity.getAttributeValue(Attributes.ARMOR);

            if (Math.abs(actualArmor - CHRONOS_WARDEN_ARMOR) < 0.1) {
                helper.succeed();
            } else {
                helper.fail("Chronos Warden armor was " + actualArmor + ", expected " + CHRONOS_WARDEN_ARMOR);
            }
        });
    };

    /**
     * Test that Clockwork Colossus has correct initial health (200 HP = 100 hearts).
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKWORK_COLOSSUS_INITIAL_HEALTH = helper -> {
        var entity = helper.spawn(ModEntities.CLOCKWORK_COLOSSUS.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            float actualHealth = entity.getHealth();

            if (Math.abs(actualHealth - CLOCKWORK_COLOSSUS_HEALTH) < 0.1f) {
                helper.succeed();
            } else {
                helper.fail("Clockwork Colossus health was " + actualHealth + ", expected " + CLOCKWORK_COLOSSUS_HEALTH);
            }
        });
    };

    /**
     * Test that Clockwork Colossus has complete knockback immunity (1.0).
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKWORK_COLOSSUS_KNOCKBACK_RESISTANCE = helper -> {
        var entity = helper.spawn(ModEntities.CLOCKWORK_COLOSSUS.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            double actualResistance = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);

            if (Math.abs(actualResistance - CLOCKWORK_COLOSSUS_KNOCKBACK_RESISTANCE) < 0.1) {
                helper.succeed();
            } else {
                helper.fail("Clockwork Colossus knockback resistance was " + actualResistance +
                    ", expected " + CLOCKWORK_COLOSSUS_KNOCKBACK_RESISTANCE);
            }
        });
    };

    /**
     * Test that Temporal Phantom has correct initial health (150 HP = 75 hearts).
     */
    public static final Consumer<GameTestHelper> TEST_TEMPORAL_PHANTOM_INITIAL_HEALTH = helper -> {
        var entity = helper.spawn(ModEntities.TEMPORAL_PHANTOM.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            float actualHealth = entity.getHealth();

            if (Math.abs(actualHealth - TEMPORAL_PHANTOM_HEALTH) < 0.1f) {
                helper.succeed();
            } else {
                helper.fail("Temporal Phantom health was " + actualHealth + ", expected " + TEMPORAL_PHANTOM_HEALTH);
            }
        });
    };

    /**
     * Test that Temporal Phantom has correct attack damage (8.0 = 4 hearts).
     */
    public static final Consumer<GameTestHelper> TEST_TEMPORAL_PHANTOM_ATTACK_DAMAGE = helper -> {
        var entity = helper.spawn(ModEntities.TEMPORAL_PHANTOM.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            double actualDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);

            if (Math.abs(actualDamage - TEMPORAL_PHANTOM_ATTACK_DAMAGE) < 0.1) {
                helper.succeed();
            } else {
                helper.fail("Temporal Phantom attack damage was " + actualDamage +
                    ", expected " + TEMPORAL_PHANTOM_ATTACK_DAMAGE);
            }
        });
    };

    /**
     * Test that Entropy Keeper has correct initial health (160 HP = 80 hearts).
     */
    public static final Consumer<GameTestHelper> TEST_ENTROPY_KEEPER_INITIAL_HEALTH = helper -> {
        var entity = helper.spawn(ModEntities.ENTROPY_KEEPER.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            float actualHealth = entity.getHealth();

            if (Math.abs(actualHealth - ENTROPY_KEEPER_HEALTH) < 0.1f) {
                helper.succeed();
            } else {
                helper.fail("Entropy Keeper health was " + actualHealth + ", expected " + ENTROPY_KEEPER_HEALTH);
            }
        });
    };

    /**
     * Test that Entropy Keeper has correct armor value (6.0).
     */
    public static final Consumer<GameTestHelper> TEST_ENTROPY_KEEPER_ARMOR = helper -> {
        var entity = helper.spawn(ModEntities.ENTROPY_KEEPER.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            double actualArmor = entity.getAttributeValue(Attributes.ARMOR);

            if (Math.abs(actualArmor - ENTROPY_KEEPER_ARMOR) < 0.1) {
                helper.succeed();
            } else {
                helper.fail("Entropy Keeper armor was " + actualArmor + ", expected " + ENTROPY_KEEPER_ARMOR);
            }
        });
    };

    // ============== Other Mob Attribute Tests ==============

    /**
     * Test that Floq has correct initial health (16 HP = 8 hearts).
     */
    public static final Consumer<GameTestHelper> TEST_FLOQ_INITIAL_HEALTH = helper -> {
        var entity = helper.spawn(ModEntities.FLOQ.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            float actualHealth = entity.getHealth();

            if (Math.abs(actualHealth - FLOQ_HEALTH) < 0.1f) {
                helper.succeed();
            } else {
                helper.fail("Floq health was " + actualHealth + ", expected " + FLOQ_HEALTH);
            }
        });
    };

    /**
     * Test that Floq has correct attack damage (3.0 = 1.5 hearts).
     */
    public static final Consumer<GameTestHelper> TEST_FLOQ_ATTACK_DAMAGE = helper -> {
        var entity = helper.spawn(ModEntities.FLOQ.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            double actualDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);

            if (Math.abs(actualDamage - FLOQ_ATTACK_DAMAGE) < 0.1) {
                helper.succeed();
            } else {
                helper.fail("Floq attack damage was " + actualDamage + ", expected " + FLOQ_ATTACK_DAMAGE);
            }
        });
    };

    /**
     * Test that Time Keeper has correct initial health (20 HP = 10 hearts).
     */
    public static final Consumer<GameTestHelper> TEST_TIME_KEEPER_INITIAL_HEALTH = helper -> {
        var entity = helper.spawn(ModEntities.TIME_KEEPER.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            float actualHealth = entity.getHealth();

            if (Math.abs(actualHealth - TIME_KEEPER_HEALTH) < 0.1f) {
                helper.succeed();
            } else {
                helper.fail("Time Keeper health was " + actualHealth + ", expected " + TIME_KEEPER_HEALTH);
            }
        });
    };

    // ============== Tool Durability Tests ==============

    /**
     * Test that Clockstone Sword has correct durability (450).
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKSTONE_SWORD_DURABILITY = helper -> {
        ItemStack sword = new ItemStack(ModItems.CLOCKSTONE_SWORD.get());

        helper.runAfterDelay(1, () -> {
            int actualDurability = sword.getMaxDamage();

            if (actualDurability == CLOCKSTONE_TOOL_DURABILITY) {
                helper.succeed();
            } else {
                helper.fail("Clockstone Sword durability was " + actualDurability + ", expected " + CLOCKSTONE_TOOL_DURABILITY);
            }
        });
    };

    /**
     * Test that Enhanced Clockstone Sword has correct durability (1200).
     */
    public static final Consumer<GameTestHelper> TEST_ENHANCED_CLOCKSTONE_SWORD_DURABILITY = helper -> {
        ItemStack sword = new ItemStack(ModItems.ENHANCED_CLOCKSTONE_SWORD.get());

        helper.runAfterDelay(1, () -> {
            int actualDurability = sword.getMaxDamage();

            if (actualDurability == ENHANCED_CLOCKSTONE_TOOL_DURABILITY) {
                helper.succeed();
            } else {
                helper.fail("Enhanced Clockstone Sword durability was " + actualDurability + ", expected " + ENHANCED_CLOCKSTONE_TOOL_DURABILITY);
            }
        });
    };

    /**
     * Test that Time Guardian has correct attack damage (10.0 = 5 hearts).
     */
    public static final Consumer<GameTestHelper> TEST_TIME_GUARDIAN_ATTACK_DAMAGE = helper -> {
        var entity = helper.spawn(ModEntities.TIME_GUARDIAN.get(), TEST_POS);

        helper.runAfterDelay(1, () -> {
            double actualDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);

            if (Math.abs(actualDamage - TIME_GUARDIAN_ATTACK_DAMAGE) < 0.1) {
                helper.succeed();
            } else {
                helper.fail("Time Guardian attack damage was " + actualDamage +
                    ", expected " + TIME_GUARDIAN_ATTACK_DAMAGE);
            }
        });
    };
}
