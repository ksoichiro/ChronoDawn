package com.chronodawn.mixin.client;

import com.chronodawn.client.shield.ClientShieldCooldowns;
import com.chronodawn.items.shield.ChronoShieldMarker;
import com.chronodawn.items.shield.ChronoShieldTier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws a vanilla-style cooldown swipe overlay on ChronoDawn shield items whose
 * Speed-on-block (T2+) or Time-Echo (T3) cooldown is currently active.
 *
 * <p>Using a custom overlay rather than {@code ItemCooldowns.addCooldown} intentionally
 * avoids the side effect of blocking the shield's use during the cooldown — our CDs
 * gate *bonus effects*, not the core shield-blocking function.
 *
 * <p>Echo CD takes display priority over Speed CD when both are active on the same
 * stack (Echo is rarer and longer, so its ending-soon feedback is more informative).
 */
@Mixin(GuiGraphics.class)
public class GuiGraphicsShieldCdMixin {

    @Inject(
        method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
        at = @At("TAIL")
    )
    private void chronodawn$renderShieldCooldownOverlay(
        Font font, ItemStack stack, int x, int y, CallbackInfo ci
    ) {
        if (!(stack.getItem() instanceof ChronoShieldMarker marker)) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;

        long now = level.getGameTime();
        // Partial-tick smoothing intentionally omitted (Minecraft#getTimer was renamed/removed
        // in 1.21.11; the overlay quantizes to whole ticks, which is visually negligible over
        // the 60-tick / 600-tick CDs we render here).
        float partial = 0f;

        ChronoShieldTier tier = marker.getChronoShieldTier();
        float progress = 0f;
        if (tier.hasTimeEcho) {
            progress = ClientShieldCooldowns.getEchoProgress(now, partial);
        }
        if (progress <= 0f && tier.hasSpeedOnBlock) {
            progress = ClientShieldCooldowns.getSpeedProgress(now, partial);
        }
        if (progress <= 0f) return;

        GuiGraphics self = (GuiGraphics)(Object)this;
        int top = y + Mth.floor(16.0F * (1.0F - progress));
        int bot = top + Mth.ceil(16.0F * progress);
        self.fill(x, top, x + 16, bot, Integer.MAX_VALUE);
    }
}
