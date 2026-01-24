package com.chronodawn.mixin.client;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Client-side mixin to modify sky color in ChronoDawn dimension.
 *
 * This mixin changes the sky color from grey (0x909090) to bright blue (0x5588DD)
 * when the Time Tyrant has been defeated, as indicated by the advancement
 * chronodawn:story/us3/time_tyrant_defeat.
 *
 * The color change is applied by intercepting the Biome.getSkyColor method
 * and returning a modified color value based on advancement status.
 *
 * Design Note (Advancement as World State Proxy):
 * Ideally, this would check the server-side world state (ChronoDawnGlobalState.isTyrantDefeated()).
 * However, sky color rendering is client-side and cannot directly access server SavedData.
 *
 * Solution: Use advancement as a proxy for world state:
 * - Server grants advancement to all players when Time Tyrant is defeated
 * - Minecraft automatically syncs advancements to clients
 * - Client checks advancement status (which reflects world state)
 *
 * This approach avoids custom server→client packets while achieving the same result.
 * All players (including those who log in after defeat) receive the advancement automatically.
 *
 * Future improvement: Implement custom packet to directly sync ChronoDawnGlobalState to client.
 */
@Mixin(Biome.class)
public class SkyColorMixin {

    // Default grey sky color (before Time Tyrant defeat)
    @Unique
    private static final int DEFAULT_SKY_COLOR = 0x909090;

    // Bright blue sky color (after Time Tyrant defeat)
    // RGB(85, 136, 221) - Clear blue sky
    @Unique
    private static final int BRIGHT_SKY_COLOR = 0x5588DD;

    // Advancement ID for Time Tyrant defeat
    @Unique
    private static final ResourceLocation TIME_TYRANT_DEFEATED_ADVANCEMENT =
        CompatResourceLocation.create("chronodawn", "story/us3/time_tyrant_defeat");

    /**
     * Inject into getSkyColor to modify the sky color in ChronoDawn.
     *
     * @param cir Callback info returnable containing the original sky color
     */
    @Inject(
        method = "getSkyColor",
        at = @At("RETURN"),
        cancellable = true
    )
    private void modifyChronoDawnSkyColor(CallbackInfoReturnable<Integer> cir) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        // Only apply to ChronoDawn dimension
        if (!minecraft.level.dimension().equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
            return;
        }

        // Get original sky color
        int originalColor = cir.getReturnValue();

        // Only modify if it's the default grey color (0x909090 = 9474192)
        if (originalColor != DEFAULT_SKY_COLOR) {
            return;
        }

        // Check if Time Tyrant has been defeated
        boolean timeTyrantDefeated = hasTimeTyrantBeenDefeated();

        // If defeated, replace with bright blue sky color
        if (timeTyrantDefeated) {
            cir.setReturnValue(BRIGHT_SKY_COLOR);
        }
    }

    /**
     * Check if the player has completed the Time Tyrant defeat advancement.
     *
     * This method uses Mixin's accessor pattern to access the private progress
     * field in ClientAdvancements and check advancement completion status on
     * the client side.
     *
     * @return true if the advancement has been completed
     */
    @Unique
    private static boolean hasTimeTyrantBeenDefeated() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null) {
            return false;
        }

        ClientPacketListener connection = minecraft.getConnection();
        if (connection == null) {
            return false;
        }

        // Get advancement manager
        var advancementManager = connection.getAdvancements();

        // Access the progress map using the accessor
        var progressAccessor = (ClientAdvancementsAccessor) advancementManager;
        var progressMap = progressAccessor.chronodawn$getProgress();

        if (progressMap == null) {
            return false;
        }

        // Note: In 1.20.1, ClientAdvancements.get() doesn't exist
        // Find the advancement by ResourceLocation from the progress map
        net.minecraft.advancements.Advancement advancement = null;
        for (net.minecraft.advancements.Advancement adv : progressMap.keySet()) {
            if (adv.getId().equals(TIME_TYRANT_DEFEATED_ADVANCEMENT)) {
                advancement = adv;
                break;
            }
        }

        if (advancement == null) {
            return false;
        }

        // Get progress for this specific advancement
        var progress = progressMap.get(advancement);
        if (progress == null) {
            return false;
        }

        // Check if advancement is completed
        return progress.isDone();
    }
}
