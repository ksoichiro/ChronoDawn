package com.chronodawn.gametest;

import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModEntities;
import com.chronodawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

import java.util.function.Consumer;

/**
 * Common GameTest logic shared between Fabric and NeoForge implementations.
 *
 * This class provides the test logic as Consumer<GameTestHelper> functions
 * that can be used by both platform-specific GameTest implementations.
 *
 * Reference: T172f/T173d - Common GameTest Logic
 */
public final class ChronoDawnGameTestLogic {

    private ChronoDawnGameTestLogic() {
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

    // Clockstone Armor expected defense values
    public static final int CLOCKSTONE_HELMET_DEFENSE = 2;
    public static final int CLOCKSTONE_CHESTPLATE_DEFENSE = 6;
    public static final int CLOCKSTONE_LEGGINGS_DEFENSE = 5;
    public static final int CLOCKSTONE_BOOTS_DEFENSE = 2;

    // Enhanced Clockstone Armor expected defense values
    public static final int ENHANCED_CLOCKSTONE_HELMET_DEFENSE = 3;
    public static final int ENHANCED_CLOCKSTONE_CHESTPLATE_DEFENSE = 7;
    public static final int ENHANCED_CLOCKSTONE_LEGGINGS_DEFENSE = 6;
    public static final int ENHANCED_CLOCKSTONE_BOOTS_DEFENSE = 3;

    // Spatially Linked Pickaxe expected values
    public static final int SPATIALLY_LINKED_PICKAXE_DURABILITY = 1561;

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

    /**
     * Test that ChronoDawn Boat entity can be spawned.
     */
    public static final Consumer<GameTestHelper> TEST_CHRONO_DAWN_BOAT_CAN_SPAWN = helper -> {
        helper.spawn(ModEntities.CHRONO_DAWN_BOAT.get(), TEST_POS);
        helper.succeedWhenEntityPresent(ModEntities.CHRONO_DAWN_BOAT.get(), TEST_POS);
    };

    /**
     * Test that ChronoDawn Chest Boat entity can be spawned.
     */
    public static final Consumer<GameTestHelper> TEST_CHRONO_DAWN_CHEST_BOAT_CAN_SPAWN = helper -> {
        helper.spawn(ModEntities.CHRONO_DAWN_CHEST_BOAT.get(), TEST_POS);
        helper.succeedWhenEntityPresent(ModEntities.CHRONO_DAWN_CHEST_BOAT.get(), TEST_POS);
    };

    /**
     * Test that Chronos Warden entity can be spawned.
     */
    public static final Consumer<GameTestHelper> TEST_CHRONOS_WARDEN_CAN_SPAWN = helper -> {
        helper.spawn(ModEntities.CHRONOS_WARDEN.get(), TEST_POS);
        helper.succeedWhenEntityPresent(ModEntities.CHRONOS_WARDEN.get(), TEST_POS);
    };

    /**
     * Test that Clockwork Colossus entity can be spawned.
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKWORK_COLOSSUS_CAN_SPAWN = helper -> {
        helper.spawn(ModEntities.CLOCKWORK_COLOSSUS.get(), TEST_POS);
        helper.succeedWhenEntityPresent(ModEntities.CLOCKWORK_COLOSSUS.get(), TEST_POS);
    };

    /**
     * Test that Time Keeper entity can be spawned.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_KEEPER_CAN_SPAWN = helper -> {
        helper.spawn(ModEntities.TIME_KEEPER.get(), TEST_POS);
        helper.succeedWhenEntityPresent(ModEntities.TIME_KEEPER.get(), TEST_POS);
    };

    /**
     * Test that Floq entity can be spawned.
     */
    public static final Consumer<GameTestHelper> TEST_FLOQ_CAN_SPAWN = helper -> {
        helper.spawn(ModEntities.FLOQ.get(), TEST_POS);
        helper.succeedWhenEntityPresent(ModEntities.FLOQ.get(), TEST_POS);
    };

    /**
     * Test that Temporal Phantom entity can be spawned.
     */
    public static final Consumer<GameTestHelper> TEST_TEMPORAL_PHANTOM_CAN_SPAWN = helper -> {
        helper.spawn(ModEntities.TEMPORAL_PHANTOM.get(), TEST_POS);
        helper.succeedWhenEntityPresent(ModEntities.TEMPORAL_PHANTOM.get(), TEST_POS);
    };

    /**
     * Test that Entropy Keeper entity can be spawned.
     */
    public static final Consumer<GameTestHelper> TEST_ENTROPY_KEEPER_CAN_SPAWN = helper -> {
        helper.spawn(ModEntities.ENTROPY_KEEPER.get(), TEST_POS);
        helper.succeedWhenEntityPresent(ModEntities.ENTROPY_KEEPER.get(), TEST_POS);
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

    /**
     * Test that Time Crystal Block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_CRYSTAL_BLOCK_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TIME_CRYSTAL_BLOCK.get());
        helper.succeedWhenBlockPresent(ModBlocks.TIME_CRYSTAL_BLOCK.get(), TEST_POS);
    };

    /**
     * Test that Temporal Bricks can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TEMPORAL_BRICKS_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TEMPORAL_BRICKS.get());
        helper.succeedWhenBlockPresent(ModBlocks.TEMPORAL_BRICKS.get(), TEST_POS);
    };

    /**
     * Test that Clockstone Ore block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKSTONE_ORE_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.CLOCKSTONE_ORE.get());
        helper.succeedWhenBlockPresent(ModBlocks.CLOCKSTONE_ORE.get(), TEST_POS);
    };

    /**
     * Test that Time Crystal Ore block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_CRYSTAL_ORE_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TIME_CRYSTAL_ORE.get());
        helper.succeedWhenBlockPresent(ModBlocks.TIME_CRYSTAL_ORE.get(), TEST_POS);
    };

    /**
     * Test that Clockwork Block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKWORK_BLOCK_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.CLOCKWORK_BLOCK.get());
        helper.succeedWhenBlockPresent(ModBlocks.CLOCKWORK_BLOCK.get(), TEST_POS);
    };

    /**
     * Test that Dark Time Wood Log block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_DARK_TIME_WOOD_LOG_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.DARK_TIME_WOOD_LOG.get());
        helper.succeedWhenBlockPresent(ModBlocks.DARK_TIME_WOOD_LOG.get(), TEST_POS);
    };

    /**
     * Test that Dark Time Wood Planks block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_DARK_TIME_WOOD_PLANKS_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.DARK_TIME_WOOD_PLANKS.get());
        helper.succeedWhenBlockPresent(ModBlocks.DARK_TIME_WOOD_PLANKS.get(), TEST_POS);
    };

    /**
     * Test that Ancient Time Wood Log block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_ANCIENT_TIME_WOOD_LOG_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.ANCIENT_TIME_WOOD_LOG.get());
        helper.succeedWhenBlockPresent(ModBlocks.ANCIENT_TIME_WOOD_LOG.get(), TEST_POS);
    };

    /**
     * Test that Time Wood Stairs block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_WOOD_STAIRS_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TIME_WOOD_STAIRS.get());
        helper.succeedWhenBlockPresent(ModBlocks.TIME_WOOD_STAIRS.get(), TEST_POS);
    };

    /**
     * Test that Time Wood Slab block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_WOOD_SLAB_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TIME_WOOD_SLAB.get());
        helper.succeedWhenBlockPresent(ModBlocks.TIME_WOOD_SLAB.get(), TEST_POS);
    };

    /**
     * Test that Time Wood Fence block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_WOOD_FENCE_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TIME_WOOD_FENCE.get());
        helper.succeedWhenBlockPresent(ModBlocks.TIME_WOOD_FENCE.get(), TEST_POS);
    };

    /**
     * Test that Time Wood Door block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_WOOD_DOOR_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TIME_WOOD_DOOR.get());
        helper.succeedWhenBlockPresent(ModBlocks.TIME_WOOD_DOOR.get(), TEST_POS);
    };

    /**
     * Test that Time Wood Trapdoor block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_WOOD_TRAPDOOR_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TIME_WOOD_TRAPDOOR.get());
        helper.succeedWhenBlockPresent(ModBlocks.TIME_WOOD_TRAPDOOR.get(), TEST_POS);
    };

    /**
     * Test that Clockstone Stairs block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKSTONE_STAIRS_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.CLOCKSTONE_STAIRS.get());
        helper.succeedWhenBlockPresent(ModBlocks.CLOCKSTONE_STAIRS.get(), TEST_POS);
    };

    /**
     * Test that Clockstone Slab block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKSTONE_SLAB_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.CLOCKSTONE_SLAB.get());
        helper.succeedWhenBlockPresent(ModBlocks.CLOCKSTONE_SLAB.get(), TEST_POS);
    };

    /**
     * Test that Clockstone Wall block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKSTONE_WALL_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.CLOCKSTONE_WALL.get());
        helper.succeedWhenBlockPresent(ModBlocks.CLOCKSTONE_WALL.get(), TEST_POS);
    };

    /**
     * Test that Temporal Bricks Stairs block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TEMPORAL_BRICKS_STAIRS_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TEMPORAL_BRICKS_STAIRS.get());
        helper.succeedWhenBlockPresent(ModBlocks.TEMPORAL_BRICKS_STAIRS.get(), TEST_POS);
    };

    /**
     * Test that Temporal Bricks Slab block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TEMPORAL_BRICKS_SLAB_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TEMPORAL_BRICKS_SLAB.get());
        helper.succeedWhenBlockPresent(ModBlocks.TEMPORAL_BRICKS_SLAB.get(), TEST_POS);
    };

    /**
     * Test that Temporal Bricks Wall block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TEMPORAL_BRICKS_WALL_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TEMPORAL_BRICKS_WALL.get());
        helper.succeedWhenBlockPresent(ModBlocks.TEMPORAL_BRICKS_WALL.get(), TEST_POS);
    };

    /**
     * Test that Reversing Time Sandstone block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_REVERSING_TIME_SANDSTONE_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.REVERSING_TIME_SANDSTONE.get());
        helper.succeedWhenBlockPresent(ModBlocks.REVERSING_TIME_SANDSTONE.get(), TEST_POS);
    };

    /**
     * Test that Frozen Time Ice block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_FROZEN_TIME_ICE_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.FROZEN_TIME_ICE.get());
        helper.succeedWhenBlockPresent(ModBlocks.FROZEN_TIME_ICE.get(), TEST_POS);
    };

    /**
     * Test that Temporal Moss block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TEMPORAL_MOSS_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TEMPORAL_MOSS.get());
        helper.succeedWhenBlockPresent(ModBlocks.TEMPORAL_MOSS.get(), TEST_POS);
    };

    /**
     * Test that Time Wheat Bale block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_WHEAT_BALE_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TIME_WHEAT_BALE.get());
        helper.succeedWhenBlockPresent(ModBlocks.TIME_WHEAT_BALE.get(), TEST_POS);
    };

    /**
     * Test that Chrono Melon block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_CHRONO_MELON_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.CHRONO_MELON.get());
        helper.succeedWhenBlockPresent(ModBlocks.CHRONO_MELON.get(), TEST_POS);
    };

    /**
     * Test that Ancient Time Wood Planks block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_ANCIENT_TIME_WOOD_PLANKS_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.ANCIENT_TIME_WOOD_PLANKS.get());
        helper.succeedWhenBlockPresent(ModBlocks.ANCIENT_TIME_WOOD_PLANKS.get(), TEST_POS);
    };

    /**
     * Test that Time Wood Leaves block can be placed.
     */
    public static final Consumer<GameTestHelper> TEST_TIME_WOOD_LEAVES_CAN_BE_PLACED = helper -> {
        helper.setBlock(TEST_POS, ModBlocks.TIME_WOOD_LEAVES.get());
        helper.succeedWhenBlockPresent(ModBlocks.TIME_WOOD_LEAVES.get(), TEST_POS);
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
     * Test that Clockstone Pickaxe has correct durability (450).
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKSTONE_PICKAXE_DURABILITY = helper -> {
        ItemStack pickaxe = new ItemStack(ModItems.CLOCKSTONE_PICKAXE.get());

        helper.runAfterDelay(1, () -> {
            int actualDurability = pickaxe.getMaxDamage();

            if (actualDurability == CLOCKSTONE_TOOL_DURABILITY) {
                helper.succeed();
            } else {
                helper.fail("Clockstone Pickaxe durability was " + actualDurability + ", expected " + CLOCKSTONE_TOOL_DURABILITY);
            }
        });
    };

    /**
     * Test that Enhanced Clockstone Pickaxe has correct durability (1200).
     */
    public static final Consumer<GameTestHelper> TEST_ENHANCED_CLOCKSTONE_PICKAXE_DURABILITY = helper -> {
        ItemStack pickaxe = new ItemStack(ModItems.ENHANCED_CLOCKSTONE_PICKAXE.get());

        helper.runAfterDelay(1, () -> {
            int actualDurability = pickaxe.getMaxDamage();

            if (actualDurability == ENHANCED_CLOCKSTONE_TOOL_DURABILITY) {
                helper.succeed();
            } else {
                helper.fail("Enhanced Clockstone Pickaxe durability was " + actualDurability + ", expected " + ENHANCED_CLOCKSTONE_TOOL_DURABILITY);
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

    // ============== Armor Defense Tests ==============

    /**
     * Test that Clockstone Chestplate has correct defense value (6).
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKSTONE_CHESTPLATE_DEFENSE = helper -> {
        ItemStack chestplate = new ItemStack(ModItems.CLOCKSTONE_CHESTPLATE.get());

        helper.runAfterDelay(1, () -> {
            if (chestplate.getItem() instanceof net.minecraft.world.item.ArmorItem armorItem) {
                int actualDefense = armorItem.getDefense();
                if (actualDefense == CLOCKSTONE_CHESTPLATE_DEFENSE) {
                    helper.succeed();
                } else {
                    helper.fail("Clockstone Chestplate defense was " + actualDefense +
                        ", expected " + CLOCKSTONE_CHESTPLATE_DEFENSE);
                }
            } else {
                helper.fail("Clockstone Chestplate is not an ArmorItem");
            }
        });
    };

    /**
     * Test that Enhanced Clockstone Chestplate has correct defense value (7).
     */
    public static final Consumer<GameTestHelper> TEST_ENHANCED_CLOCKSTONE_CHESTPLATE_DEFENSE = helper -> {
        ItemStack chestplate = new ItemStack(ModItems.ENHANCED_CLOCKSTONE_CHESTPLATE.get());

        helper.runAfterDelay(1, () -> {
            if (chestplate.getItem() instanceof net.minecraft.world.item.ArmorItem armorItem) {
                int actualDefense = armorItem.getDefense();
                if (actualDefense == ENHANCED_CLOCKSTONE_CHESTPLATE_DEFENSE) {
                    helper.succeed();
                } else {
                    helper.fail("Enhanced Clockstone Chestplate defense was " + actualDefense +
                        ", expected " + ENHANCED_CLOCKSTONE_CHESTPLATE_DEFENSE);
                }
            } else {
                helper.fail("Enhanced Clockstone Chestplate is not an ArmorItem");
            }
        });
    };

    /**
     * Test that Spatially Linked Pickaxe has correct durability (1561 - diamond tier).
     */
    public static final Consumer<GameTestHelper> TEST_SPATIALLY_LINKED_PICKAXE_DURABILITY = helper -> {
        ItemStack pickaxe = new ItemStack(ModItems.SPATIALLY_LINKED_PICKAXE.get());

        helper.runAfterDelay(1, () -> {
            int actualDurability = pickaxe.getMaxDamage();

            if (actualDurability == SPATIALLY_LINKED_PICKAXE_DURABILITY) {
                helper.succeed();
            } else {
                helper.fail("Spatially Linked Pickaxe durability was " + actualDurability +
                    ", expected " + SPATIALLY_LINKED_PICKAXE_DURABILITY);
            }
        });
    };

    // ============== Additional Armor Defense Tests ==============

    /**
     * Test that Clockstone Helmet has correct defense value (2).
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKSTONE_HELMET_DEFENSE = helper -> {
        ItemStack helmet = new ItemStack(ModItems.CLOCKSTONE_HELMET.get());

        helper.runAfterDelay(1, () -> {
            if (helmet.getItem() instanceof net.minecraft.world.item.ArmorItem armorItem) {
                int actualDefense = armorItem.getDefense();
                if (actualDefense == CLOCKSTONE_HELMET_DEFENSE) {
                    helper.succeed();
                } else {
                    helper.fail("Clockstone Helmet defense was " + actualDefense +
                        ", expected " + CLOCKSTONE_HELMET_DEFENSE);
                }
            } else {
                helper.fail("Clockstone Helmet is not an ArmorItem");
            }
        });
    };

    /**
     * Test that Clockstone Leggings has correct defense value (5).
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKSTONE_LEGGINGS_DEFENSE = helper -> {
        ItemStack leggings = new ItemStack(ModItems.CLOCKSTONE_LEGGINGS.get());

        helper.runAfterDelay(1, () -> {
            if (leggings.getItem() instanceof net.minecraft.world.item.ArmorItem armorItem) {
                int actualDefense = armorItem.getDefense();
                if (actualDefense == CLOCKSTONE_LEGGINGS_DEFENSE) {
                    helper.succeed();
                } else {
                    helper.fail("Clockstone Leggings defense was " + actualDefense +
                        ", expected " + CLOCKSTONE_LEGGINGS_DEFENSE);
                }
            } else {
                helper.fail("Clockstone Leggings is not an ArmorItem");
            }
        });
    };

    /**
     * Test that Clockstone Boots has correct defense value (2).
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKSTONE_BOOTS_DEFENSE = helper -> {
        ItemStack boots = new ItemStack(ModItems.CLOCKSTONE_BOOTS.get());

        helper.runAfterDelay(1, () -> {
            if (boots.getItem() instanceof net.minecraft.world.item.ArmorItem armorItem) {
                int actualDefense = armorItem.getDefense();
                if (actualDefense == CLOCKSTONE_BOOTS_DEFENSE) {
                    helper.succeed();
                } else {
                    helper.fail("Clockstone Boots defense was " + actualDefense +
                        ", expected " + CLOCKSTONE_BOOTS_DEFENSE);
                }
            } else {
                helper.fail("Clockstone Boots is not an ArmorItem");
            }
        });
    };

    /**
     * Test that Enhanced Clockstone Helmet has correct defense value (3).
     */
    public static final Consumer<GameTestHelper> TEST_ENHANCED_CLOCKSTONE_HELMET_DEFENSE = helper -> {
        ItemStack helmet = new ItemStack(ModItems.ENHANCED_CLOCKSTONE_HELMET.get());

        helper.runAfterDelay(1, () -> {
            if (helmet.getItem() instanceof net.minecraft.world.item.ArmorItem armorItem) {
                int actualDefense = armorItem.getDefense();
                if (actualDefense == ENHANCED_CLOCKSTONE_HELMET_DEFENSE) {
                    helper.succeed();
                } else {
                    helper.fail("Enhanced Clockstone Helmet defense was " + actualDefense +
                        ", expected " + ENHANCED_CLOCKSTONE_HELMET_DEFENSE);
                }
            } else {
                helper.fail("Enhanced Clockstone Helmet is not an ArmorItem");
            }
        });
    };

    /**
     * Test that Enhanced Clockstone Leggings has correct defense value (6).
     */
    public static final Consumer<GameTestHelper> TEST_ENHANCED_CLOCKSTONE_LEGGINGS_DEFENSE = helper -> {
        ItemStack leggings = new ItemStack(ModItems.ENHANCED_CLOCKSTONE_LEGGINGS.get());

        helper.runAfterDelay(1, () -> {
            if (leggings.getItem() instanceof net.minecraft.world.item.ArmorItem armorItem) {
                int actualDefense = armorItem.getDefense();
                if (actualDefense == ENHANCED_CLOCKSTONE_LEGGINGS_DEFENSE) {
                    helper.succeed();
                } else {
                    helper.fail("Enhanced Clockstone Leggings defense was " + actualDefense +
                        ", expected " + ENHANCED_CLOCKSTONE_LEGGINGS_DEFENSE);
                }
            } else {
                helper.fail("Enhanced Clockstone Leggings is not an ArmorItem");
            }
        });
    };

    /**
     * Test that Enhanced Clockstone Boots has correct defense value (3).
     */
    public static final Consumer<GameTestHelper> TEST_ENHANCED_CLOCKSTONE_BOOTS_DEFENSE = helper -> {
        ItemStack boots = new ItemStack(ModItems.ENHANCED_CLOCKSTONE_BOOTS.get());

        helper.runAfterDelay(1, () -> {
            if (boots.getItem() instanceof net.minecraft.world.item.ArmorItem armorItem) {
                int actualDefense = armorItem.getDefense();
                if (actualDefense == ENHANCED_CLOCKSTONE_BOOTS_DEFENSE) {
                    helper.succeed();
                } else {
                    helper.fail("Enhanced Clockstone Boots defense was " + actualDefense +
                        ", expected " + ENHANCED_CLOCKSTONE_BOOTS_DEFENSE);
                }
            } else {
                helper.fail("Enhanced Clockstone Boots is not an ArmorItem");
            }
        });
    };

    // ============== Additional Tool Durability Tests ==============

    /**
     * Test that Clockstone Axe has correct durability (450).
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKSTONE_AXE_DURABILITY = helper -> {
        ItemStack axe = new ItemStack(ModItems.CLOCKSTONE_AXE.get());

        helper.runAfterDelay(1, () -> {
            int actualDurability = axe.getMaxDamage();

            if (actualDurability == CLOCKSTONE_TOOL_DURABILITY) {
                helper.succeed();
            } else {
                helper.fail("Clockstone Axe durability was " + actualDurability + ", expected " + CLOCKSTONE_TOOL_DURABILITY);
            }
        });
    };

    /**
     * Test that Clockstone Shovel has correct durability (450).
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKSTONE_SHOVEL_DURABILITY = helper -> {
        ItemStack shovel = new ItemStack(ModItems.CLOCKSTONE_SHOVEL.get());

        helper.runAfterDelay(1, () -> {
            int actualDurability = shovel.getMaxDamage();

            if (actualDurability == CLOCKSTONE_TOOL_DURABILITY) {
                helper.succeed();
            } else {
                helper.fail("Clockstone Shovel durability was " + actualDurability + ", expected " + CLOCKSTONE_TOOL_DURABILITY);
            }
        });
    };

    /**
     * Test that Clockstone Hoe has correct durability (450).
     */
    public static final Consumer<GameTestHelper> TEST_CLOCKSTONE_HOE_DURABILITY = helper -> {
        ItemStack hoe = new ItemStack(ModItems.CLOCKSTONE_HOE.get());

        helper.runAfterDelay(1, () -> {
            int actualDurability = hoe.getMaxDamage();

            if (actualDurability == CLOCKSTONE_TOOL_DURABILITY) {
                helper.succeed();
            } else {
                helper.fail("Clockstone Hoe durability was " + actualDurability + ", expected " + CLOCKSTONE_TOOL_DURABILITY);
            }
        });
    };

    /**
     * Test that Enhanced Clockstone Axe has correct durability (1200).
     */
    public static final Consumer<GameTestHelper> TEST_ENHANCED_CLOCKSTONE_AXE_DURABILITY = helper -> {
        ItemStack axe = new ItemStack(ModItems.ENHANCED_CLOCKSTONE_AXE.get());

        helper.runAfterDelay(1, () -> {
            int actualDurability = axe.getMaxDamage();

            if (actualDurability == ENHANCED_CLOCKSTONE_TOOL_DURABILITY) {
                helper.succeed();
            } else {
                helper.fail("Enhanced Clockstone Axe durability was " + actualDurability + ", expected " + ENHANCED_CLOCKSTONE_TOOL_DURABILITY);
            }
        });
    };

    /**
     * Test that Enhanced Clockstone Shovel has correct durability (1200).
     */
    public static final Consumer<GameTestHelper> TEST_ENHANCED_CLOCKSTONE_SHOVEL_DURABILITY = helper -> {
        ItemStack shovel = new ItemStack(ModItems.ENHANCED_CLOCKSTONE_SHOVEL.get());

        helper.runAfterDelay(1, () -> {
            int actualDurability = shovel.getMaxDamage();

            if (actualDurability == ENHANCED_CLOCKSTONE_TOOL_DURABILITY) {
                helper.succeed();
            } else {
                helper.fail("Enhanced Clockstone Shovel durability was " + actualDurability + ", expected " + ENHANCED_CLOCKSTONE_TOOL_DURABILITY);
            }
        });
    };

    /**
     * Test that Enhanced Clockstone Hoe has correct durability (1200).
     */
    public static final Consumer<GameTestHelper> TEST_ENHANCED_CLOCKSTONE_HOE_DURABILITY = helper -> {
        ItemStack hoe = new ItemStack(ModItems.ENHANCED_CLOCKSTONE_HOE.get());

        helper.runAfterDelay(1, () -> {
            int actualDurability = hoe.getMaxDamage();

            if (actualDurability == ENHANCED_CLOCKSTONE_TOOL_DURABILITY) {
                helper.succeed();
            } else {
                helper.fail("Enhanced Clockstone Hoe durability was " + actualDurability + ", expected " + ENHANCED_CLOCKSTONE_TOOL_DURABILITY);
            }
        });
    };

    // ============== Player Input Tests ==============

    /**
     * Test that a mock player can be created in the game test environment.
     * This verifies the basic player simulation capability.
     */
    public static final Consumer<GameTestHelper> TEST_MOCK_PLAYER_CAN_BE_CREATED = helper -> {
        helper.runAfterDelay(1, () -> {
            try {
                Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
                if (player != null) {
                    helper.succeed();
                } else {
                    helper.fail("Mock player was null");
                }
            } catch (Exception e) {
                helper.fail("Failed to create mock player: " + e.getMessage());
            }
        });
    };

    /**
     * Test that a mock player can be equipped with armor in the chest slot.
     * Verifies basic armor equipment functionality.
     */
    public static final Consumer<GameTestHelper> TEST_PLAYER_CAN_EQUIP_CHESTPLATE = helper -> {
        helper.runAfterDelay(1, () -> {
            try {
                Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
                ItemStack chestplate = new ItemStack(ModItems.CLOCKSTONE_CHESTPLATE.get());

                // Equip chestplate to player
                player.setItemSlot(EquipmentSlot.CHEST, chestplate);

                // Verify armor is equipped
                ItemStack equipped = player.getItemBySlot(EquipmentSlot.CHEST);
                if (!equipped.isEmpty() && equipped.getItem() == ModItems.CLOCKSTONE_CHESTPLATE.get()) {
                    helper.succeed();
                } else {
                    helper.fail("Chestplate was not properly equipped");
                }
            } catch (Exception e) {
                helper.fail("Failed to equip chestplate: " + e.getMessage());
            }
        });
    };

    /**
     * Test that Time Tyrant's Mail can be equipped in the chest slot.
     * Migrated from: TimeGuardianMailTest.testTimeGuardianMailMustBeEquippedInChestSlot (partial)
     */
    public static final Consumer<GameTestHelper> TEST_TIME_TYRANT_MAIL_CAN_BE_EQUIPPED = helper -> {
        helper.runAfterDelay(1, () -> {
            try {
                Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
                ItemStack mail = new ItemStack(ModItems.TIME_TYRANT_MAIL.get());

                // Equip Time Tyrant's Mail to chest slot
                player.setItemSlot(EquipmentSlot.CHEST, mail);

                // Verify armor is equipped
                ItemStack equipped = player.getItemBySlot(EquipmentSlot.CHEST);
                if (!equipped.isEmpty() && equipped.getItem() == ModItems.TIME_TYRANT_MAIL.get()) {
                    helper.succeed();
                } else {
                    helper.fail("Time Tyrant's Mail was not properly equipped in chest slot");
                }
            } catch (Exception e) {
                helper.fail("Failed to equip Time Tyrant's Mail: " + e.getMessage());
            }
        });
    };

    /**
     * Test that player can hold Chronoblade in main hand.
     * Verifies weapon equipment functionality.
     */
    public static final Consumer<GameTestHelper> TEST_PLAYER_CAN_HOLD_CHRONOBLADE = helper -> {
        helper.runAfterDelay(1, () -> {
            try {
                Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
                ItemStack chronoblade = new ItemStack(ModItems.CHRONOBLADE.get());

                // Equip Chronoblade to main hand
                player.setItemSlot(EquipmentSlot.MAINHAND, chronoblade);

                // Verify weapon is equipped
                ItemStack equipped = player.getItemBySlot(EquipmentSlot.MAINHAND);
                if (!equipped.isEmpty() && equipped.getItem() == ModItems.CHRONOBLADE.get()) {
                    helper.succeed();
                } else {
                    helper.fail("Chronoblade was not properly equipped in main hand");
                }
            } catch (Exception e) {
                helper.fail("Failed to equip Chronoblade: " + e.getMessage());
            }
        });
    };

    /**
     * Test that player can be equipped with full Clockstone armor set.
     * Verifies all armor slots can be equipped simultaneously.
     */
    public static final Consumer<GameTestHelper> TEST_PLAYER_CAN_EQUIP_FULL_ARMOR_SET = helper -> {
        helper.runAfterDelay(1, () -> {
            try {
                Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);

                // Create full armor set
                ItemStack helmet = new ItemStack(ModItems.CLOCKSTONE_HELMET.get());
                ItemStack chestplate = new ItemStack(ModItems.CLOCKSTONE_CHESTPLATE.get());
                ItemStack leggings = new ItemStack(ModItems.CLOCKSTONE_LEGGINGS.get());
                ItemStack boots = new ItemStack(ModItems.CLOCKSTONE_BOOTS.get());

                // Equip all armor pieces
                player.setItemSlot(EquipmentSlot.HEAD, helmet);
                player.setItemSlot(EquipmentSlot.CHEST, chestplate);
                player.setItemSlot(EquipmentSlot.LEGS, leggings);
                player.setItemSlot(EquipmentSlot.FEET, boots);

                // Verify all armor is equipped
                boolean helmetEquipped = player.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.CLOCKSTONE_HELMET.get();
                boolean chestplateEquipped = player.getItemBySlot(EquipmentSlot.CHEST).getItem() == ModItems.CLOCKSTONE_CHESTPLATE.get();
                boolean leggingsEquipped = player.getItemBySlot(EquipmentSlot.LEGS).getItem() == ModItems.CLOCKSTONE_LEGGINGS.get();
                boolean bootsEquipped = player.getItemBySlot(EquipmentSlot.FEET).getItem() == ModItems.CLOCKSTONE_BOOTS.get();

                if (helmetEquipped && chestplateEquipped && leggingsEquipped && bootsEquipped) {
                    helper.succeed();
                } else {
                    helper.fail("Not all armor pieces were properly equipped: helmet=" + helmetEquipped +
                        ", chestplate=" + chestplateEquipped + ", leggings=" + leggingsEquipped +
                        ", boots=" + bootsEquipped);
                }
            } catch (Exception e) {
                helper.fail("Failed to equip full armor set: " + e.getMessage());
            }
        });
    };

    /**
     * Test that player inventory can receive items.
     * Verifies basic inventory manipulation.
     */
    public static final Consumer<GameTestHelper> TEST_PLAYER_INVENTORY_CAN_RECEIVE_ITEMS = helper -> {
        helper.runAfterDelay(1, () -> {
            try {
                Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
                ItemStack timeCrystal = new ItemStack(ModItems.TIME_CRYSTAL.get(), 10);

                // Add items to player inventory
                boolean added = player.getInventory().add(timeCrystal);

                if (added) {
                    // Verify items are in inventory
                    boolean hasItems = player.getInventory().contains(new ItemStack(ModItems.TIME_CRYSTAL.get()));
                    if (hasItems) {
                        helper.succeed();
                    } else {
                        helper.fail("Items were added but not found in inventory");
                    }
                } else {
                    helper.fail("Failed to add items to player inventory");
                }
            } catch (Exception e) {
                helper.fail("Failed to manipulate player inventory: " + e.getMessage());
            }
        });
    };
}
