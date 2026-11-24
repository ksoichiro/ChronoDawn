package com.chronodawn.client;

import com.chronodawn.items.TimeCompassItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Needle angle for the Time Compass on Minecraft versions before 1.21.4, where the
 * item model reads the "angle" item property. From 1.21.4 the vanilla
 * minecraft:compass range property does the same job from the item definition.
 *
 * The formula and the wobble mirror vanilla's CompassItemPropertyFunction so the
 * needle behaves like a vanilla compass: it points at the target in the same
 * dimension and drifts smoothly when there is no usable target.
 */
public final class TimeCompassNeedle {
    private static double wobbleRotation;
    private static double wobbleDelta;
    private static long wobbleTick = Long.MIN_VALUE;

    private TimeCompassNeedle() {
    }

    /**
     * @return needle angle in [0, 1), 0 being straight ahead like a vanilla compass
     */
    public static float angle(ItemStack stack, Level level, Entity holder) {
        Optional<GlobalPos> targetPos = TimeCompassItem.getTargetPosition(stack);
        if (level == null) {
            return (float) wobbleRotation;
        }
        if (targetPos.isEmpty() || !level.dimension().equals(targetPos.get().dimension())) {
            return wobble(level.getGameTime());
        }
        if (holder == null) {
            return 0.0f;
        }
        return towards(holder, targetPos.get().pos());
    }

    private static float towards(Entity holder, BlockPos target) {
        Vec3 pos = holder.position();
        return needleAngle(target.getX() + 0.5 - pos.x, target.getZ() + 0.5 - pos.z, holder.getYRot());
    }

    /**
     * @param dx target X minus holder X
     * @param dz target Z minus holder Z
     * @param yawDegrees holder yaw in degrees (0 faces south, 90 faces west)
     * @return needle angle in [0, 1): 0 ahead, 0.25 to the right, 0.5 behind, 0.75 to the left
     */
    static float needleAngle(double dx, double dz, float yawDegrees) {
        double toTarget = Math.atan2(dz, dx) / (Math.PI * 2.0);
        double yaw = Mth.positiveModulo(yawDegrees / 360.0, 1.0);
        // The 0.25 turn is vanilla's: atan2 measures from east while the model starts at north.
        return (float) Mth.positiveModulo(0.5 - (yaw - 0.25 - toTarget), 1.0);
    }

    private static float wobble(long gameTime) {
        // Advance once per game tick, not per rendered frame, so the needle does not blur.
        if (gameTime != wobbleTick) {
            wobbleTick = gameTime;
            double[] next = wobbleStep(wobbleRotation, wobbleDelta, Math.random());
            wobbleRotation = next[0];
            wobbleDelta = next[1];
        }
        return (float) wobbleRotation;
    }

    /**
     * One game tick of vanilla's compass wobble.
     *
     * @return {new rotation, new delta}
     */
    static double[] wobbleStep(double rotation, double delta, double random) {
        double diff = Mth.positiveModulo(random - rotation + 0.5, 1.0) - 0.5;
        double newDelta = (delta + diff * 0.1) * 0.8;
        return new double[] {Mth.positiveModulo(rotation + newDelta, 1.0), newDelta};
    }
}
