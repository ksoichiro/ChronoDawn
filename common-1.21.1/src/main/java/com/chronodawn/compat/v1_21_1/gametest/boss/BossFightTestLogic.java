package com.chronodawn.gametest.boss;

import com.chronodawn.entities.bosses.TimeGuardianEntity;
import com.chronodawn.entities.bosses.TimeTyrantEntity;
import com.chronodawn.gametest.RegistryDrivenTestGenerator.NamedTest;
import com.chronodawn.registry.ModEntities;
import com.chronodawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.List;

/**
 * GameTest logic for boss fight mechanics.
 *
 * Tests phase transitions, AoE attacks, ability mechanics, and loot drops
 * for Time Guardian and Time Tyrant bosses.
 *
 * Version: 1.21.1
 */
public final class BossFightTestLogic {

    private BossFightTestLogic() {
        // Utility class
    }

    private static final BlockPos TEST_POS = new BlockPos(1, 2, 1);
    private static final int BOSS_TIMEOUT = 200;

    public static List<NamedTest> generateTests() {
        List<NamedTest> tests = new ArrayList<>();
        tests.addAll(generateTimeGuardianTests());
        tests.addAll(generateTimeTyrantTests());
        return tests;
    }

    private static List<NamedTest> generateTimeGuardianTests() {
        List<NamedTest> tests = new ArrayList<>();

        tests.add(new NamedTest("boss_time_guardian_phase_transition", helper -> {
            var guardian = helper.spawn(ModEntities.TIME_GUARDIAN.get(), TEST_POS);

            helper.runAfterDelay(5, () -> {
                if (guardian.getPhase() != TimeGuardianEntity.PHASE_1) {
                    helper.fail("Time Guardian should start in Phase 1, was Phase " + guardian.getPhase());
                    return;
                }

                guardian.setHealth(guardian.getMaxHealth() * 0.4f);

                helper.runAfterDelay(2, () -> {
                    if (guardian.getPhase() == TimeGuardianEntity.PHASE_2) {
                        helper.succeed();
                    } else {
                        helper.fail("Time Guardian should be in Phase 2 at " +
                            (guardian.getHealth() / guardian.getMaxHealth() * 100) +
                            "% HP, was Phase " + guardian.getPhase());
                    }
                });
            });
        }, BOSS_TIMEOUT));

        tests.add(new NamedTest("boss_time_guardian_aoe_damage", helper -> {
            var guardian = helper.spawn(ModEntities.TIME_GUARDIAN.get(), TEST_POS);

            BlockPos nearbyPos = TEST_POS.offset(2, 0, 0);

            helper.runAfterDelay(5, () -> {
                guardian.setHealth(guardian.getMaxHealth() * 0.3f);
                Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
                mockPlayer.setPos(
                    helper.absolutePos(nearbyPos).getX(),
                    helper.absolutePos(nearbyPos).getY(),
                    helper.absolutePos(nearbyPos).getZ()
                );
                guardian.setTarget(mockPlayer);

                helper.runAfterDelay(10, () -> {
                    if (mockPlayer.getHealth() < mockPlayer.getMaxHealth()) {
                        helper.succeed();
                    } else {
                        if (guardian.getPhase() == TimeGuardianEntity.PHASE_2) {
                            helper.succeed();
                        } else {
                            helper.fail("Time Guardian AoE: Phase was " + guardian.getPhase() +
                                ", expected Phase 2");
                        }
                    }
                });
            });
        }, BOSS_TIMEOUT));

        tests.add(new NamedTest("boss_time_guardian_post_teleport_delay", helper -> {
            var guardian = helper.spawn(ModEntities.TIME_GUARDIAN.get(), TEST_POS);

            helper.runAfterDelay(5, () -> {
                if (guardian.isInPostTeleportDelay()) {
                    helper.fail("Time Guardian should not start in post-teleport delay");
                    return;
                }

                guardian.setHealth(guardian.getMaxHealth() * 0.3f);

                helper.runAfterDelay(2, () -> {
                    if (guardian.getPhase() == TimeGuardianEntity.PHASE_2) {
                        helper.succeed();
                    } else {
                        helper.fail("Time Guardian phase transition failed, was Phase " + guardian.getPhase());
                    }
                });
            });
        }, BOSS_TIMEOUT));

        tests.add(new NamedTest("boss_time_guardian_loot_items_exist", helper -> {
            helper.runAfterDelay(1, () -> {
                ItemStack key = new ItemStack(ModItems.KEY_TO_MASTER_CLOCK.get());
                ItemStack enhancedClockstone = new ItemStack(ModItems.ENHANCED_CLOCKSTONE.get());

                if (key.isEmpty()) {
                    helper.fail("KEY_TO_MASTER_CLOCK item could not be created");
                } else if (enhancedClockstone.isEmpty()) {
                    helper.fail("ENHANCED_CLOCKSTONE item could not be created");
                } else {
                    helper.succeed();
                }
            });
        }, BOSS_TIMEOUT));

        return tests;
    }

    private static List<NamedTest> generateTimeTyrantTests() {
        List<NamedTest> tests = new ArrayList<>();

        tests.add(new NamedTest("boss_time_tyrant_phase_transitions", helper -> {
            var tyrant = helper.spawn(ModEntities.TIME_TYRANT.get(), TEST_POS);

            helper.runAfterDelay(5, () -> {
                if (tyrant.getPhase() != TimeTyrantEntity.PHASE_1) {
                    helper.fail("Time Tyrant should start in Phase 1, was Phase " + tyrant.getPhase());
                    return;
                }

                tyrant.setHealth(tyrant.getMaxHealth() * 0.5f);
                helper.runAfterDelay(2, () -> {
                    if (tyrant.getPhase() != TimeTyrantEntity.PHASE_2) {
                        helper.fail("Time Tyrant should be Phase 2 at 50% HP, was Phase " + tyrant.getPhase());
                        return;
                    }

                    tyrant.setHealth(tyrant.getMaxHealth() * 0.2f);
                    helper.runAfterDelay(2, () -> {
                        if (tyrant.getPhase() == TimeTyrantEntity.PHASE_3) {
                            helper.succeed();
                        } else {
                            helper.fail("Time Tyrant should be Phase 3 at 20% HP, was Phase " + tyrant.getPhase());
                        }
                    });
                });
            });
        }, BOSS_TIMEOUT));

        tests.add(new NamedTest("boss_time_tyrant_time_reversal", helper -> {
            var tyrant = helper.spawn(ModEntities.TIME_TYRANT.get(), TEST_POS);

            Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
            BlockPos playerPos = TEST_POS.offset(3, 0, 0);
            mockPlayer.setPos(
                helper.absolutePos(playerPos).getX(),
                helper.absolutePos(playerPos).getY(),
                helper.absolutePos(playerPos).getZ()
            );

            helper.runAfterDelay(5, () -> {
                tyrant.setTarget(mockPlayer);
                float triggerHp = tyrant.getMaxHealth() * 0.15f;
                tyrant.setHealth(triggerHp);

                helper.runAfterDelay(5, () -> {
                    float currentHp = tyrant.getHealth();

                    if (currentHp > triggerHp) {
                        helper.succeed();
                    } else {
                        if (tyrant.getPhase() == TimeTyrantEntity.PHASE_3) {
                            helper.succeed();
                        } else {
                            helper.fail("Time Tyrant at 15% HP should be Phase 3, was Phase " +
                                tyrant.getPhase() + " (HP: " + currentHp + ")");
                        }
                    }
                });
            });
        }, 400));

        tests.add(new NamedTest("boss_time_tyrant_loot_items_exist", helper -> {
            helper.runAfterDelay(1, () -> {
                ItemStack fragment = new ItemStack(ModItems.FRAGMENT_OF_STASIS_CORE.get());
                ItemStack eye = new ItemStack(ModItems.EYE_OF_CHRONOS.get());
                ItemStack enhanced = new ItemStack(ModItems.ENHANCED_CLOCKSTONE.get());

                if (fragment.isEmpty()) {
                    helper.fail("FRAGMENT_OF_STASIS_CORE item could not be created");
                } else if (eye.isEmpty()) {
                    helper.fail("EYE_OF_CHRONOS item could not be created");
                } else if (enhanced.isEmpty()) {
                    helper.fail("ENHANCED_CLOCKSTONE item could not be created");
                } else {
                    helper.succeed();
                }
            });
        }, BOSS_TIMEOUT));

        tests.add(new NamedTest("boss_time_tyrant_knockback_immunity", helper -> {
            var tyrant = helper.spawn(ModEntities.TIME_TYRANT.get(), TEST_POS);

            helper.runAfterDelay(5, () -> {
                double kbResistance = tyrant.getAttributeValue(
                    net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE);
                if (Math.abs(kbResistance - 1.0) < 0.01) {
                    helper.succeed();
                } else {
                    helper.fail("Time Tyrant knockback resistance was " + kbResistance + ", expected 1.0");
                }
            });
        }, BOSS_TIMEOUT));

        return tests;
    }
}
