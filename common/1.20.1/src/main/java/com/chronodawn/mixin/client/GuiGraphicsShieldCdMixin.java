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
 * Era B/C variant of the cooldown overlay mixin (MC 1.20.1, 1.21.1, 1.21.2, 1.21.3, 1.21.4).
 *
 * <p>Pre-1.21.6 vanilla {@link GuiGraphics#fill(int, int, int, int, int)} (5-param)
 * defaults internally to {@code z=0}, which renders the swipe behind the item icon
 * (item is drawn at z=200 by vanilla {@code renderItemBar} / {@code renderItemCooldown}).
 * This override uses the 6-param {@code fill(x, y, x2, y2, z, color)} with z=200 to
 * match vanilla's own cooldown overlay.</p>
 *
 * <p>1.21.5 has the same z=0 issue and uses an analogous override at
 * {@code common/shared-1.21.5+/.../GuiGraphicsShieldCdMixin.java}. 1.21.6+ replaced
 * the 6-param fill with the RenderPipeline API, and the 5-param fill there delegates
 * to {@code fill(RenderPipelines.GUI, ...)} which renders correctly without z, so the
 * {@code common/shared/} version (5-param) works for 1.21.6+.</p>
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
