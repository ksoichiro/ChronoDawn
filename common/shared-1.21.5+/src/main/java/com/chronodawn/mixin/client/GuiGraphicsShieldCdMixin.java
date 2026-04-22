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
 * <p>This version uses the 6-param {@code fill(x, y, x2, y2, z, color)} with z=200
 * to ensure the overlay renders on top of the item icon.
 * In 1.21.5–1.21.10 the 5-param {@code fill} defaults to z=0 which renders behind items.
 * Vanilla's own {@code renderItemCooldown} also uses z=200.
 *
 * <p>1.21.11 overhauled the rendering pipeline (RenderPipeline replaced RenderType)
 * and the 5-param fill renders correctly there, so this override is not needed.
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
        // z=200 matches vanilla renderItemCooldown; ensures overlay renders on top of items
        self.fill(x, top, x + 16, bot, 200, Integer.MAX_VALUE);
    }
}
