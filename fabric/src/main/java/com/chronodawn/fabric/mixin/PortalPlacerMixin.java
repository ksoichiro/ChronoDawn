package com.chronodawn.fabric.mixin;

import com.chronodawn.ChronoDawn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Fabric-specific Mixin to improve Custom Portal API portal placement by restricting Y search range.
 *
 * This Mixin intercepts the portal placement logic in Custom Portal API's PortalPlacer
 * to ensure portals spawn on the surface instead of underground caves.
 *
 * Implementation:
 * - Modifies the topY and bottomY local variables in createDestinationPortal method
 * - Sets bottomY to 70 (same as Fabric's setPortalSearchYRange lower bound)
 * - Sets topY to 100 (same as Fabric's setPortalSearchYRange upper bound)
 * - This prevents the portal search algorithm from considering deep underground positions
 *
 * Why Y=70-100 Range?
 * - Custom Portal API uses state.isSolid() which detects cave floors too
 * - Y=70 is above most cave systems (typical caves are Y=-64 to Y=64)
 * - Y=100 is high enough for hills/mountains but avoids extreme heights
 * - Matches Fabric's setPortalSearchYRange configuration
 *
 * Why This Approach?
 * - Fabric Custom Portal API v0.0.1-beta66-1.21 has setPortalSearchYRange (also used)
 * - This Mixin provides additional safety by modifying internal search range variables
 *
 * IMPORTANT: Yarn Mappings (Fabric-specific)
 * - Fabric's Custom Portal API JAR uses Yarn mappings (Intermediary names)
 * - Method signature uses class_XXXX names instead of Mojang names
 * - This is required because Custom Portal API is bundled (not remapped at runtime)
 * - class_3218 = ServerLevel (ServerWorld in Yarn)
 * - class_2338 = BlockPos
 * - class_2680 = BlockState
 * - class_2350$class_2351 = Direction$Axis
 *
 * Task: T311 - Fix portal surface generation (issue: portal generated at Y=-48 underground)
 */
@Pseudo
@Mixin(targets = "net.kyrptonaught.customportalapi.portal.PortalPlacer", remap = false)
public class PortalPlacerMixin {
    /**
     * Modify all bottomY variable assignments to set minimum portal search Y-coordinate.
     *
     * From bytecode analysis: `istore 8` stores bottomY local variable.
     * This variable is set multiple times (initial calculation + conditional override).
     * We need to modify ALL assignments to ensure Y=70 minimum is enforced.
     *
     * @param originalBottomY Original bottom Y value
     * @return Modified bottom Y value (minimum 70)
     */
    @ModifyVariable(
        method = "createDestinationPortal(Lnet/minecraft/class_3218;Lnet/minecraft/class_2338;Lnet/minecraft/class_2680;Lnet/minecraft/class_2350$class_2351;)Ljava/util/Optional;",
        at = @At(value = "STORE"),
        index = 8, // Local variable index for bottomY (targets ALL istore 8)
        remap = false
    )
    private static int modifyBottomY(int originalBottomY) {
        int minSafeY = 70;
        if (originalBottomY < minSafeY) {
            ChronoDawn.LOGGER.info("Modified portal search bottom Y from {} to {}", originalBottomY, minSafeY);
            return minSafeY;
        }
        return originalBottomY;
    }

    /**
     * Modify all topY variable assignments to set maximum portal search Y-coordinate.
     *
     * From bytecode analysis: `istore 7` stores topY local variable.
     * This variable is set multiple times (initial calculation + conditional override).
     * We need to modify ALL assignments to ensure Y=100 maximum is enforced.
     *
     * @param originalTopY Original top Y value
     * @return Modified top Y value (maximum 100)
     */
    @ModifyVariable(
        method = "createDestinationPortal(Lnet/minecraft/class_3218;Lnet/minecraft/class_2338;Lnet/minecraft/class_2680;Lnet/minecraft/class_2350$class_2351;)Ljava/util/Optional;",
        at = @At(value = "STORE"),
        index = 7, // Local variable index for topY (targets ALL istore 7)
        remap = false
    )
    private static int modifyTopY(int originalTopY) {
        int maxSafeY = 100;
        if (originalTopY > maxSafeY) {
            ChronoDawn.LOGGER.info("Modified portal search top Y from {} to {}", originalTopY, maxSafeY);
            return maxSafeY;
        }
        return originalTopY;
    }
}
