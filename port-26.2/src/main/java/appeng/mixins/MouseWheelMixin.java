package appeng.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

import appeng.hooks.MouseWheelScrolled;

/**
 * FABRIC: Emulates NeoForge's InputEvent.MouseScrollingEvent for scrolling outside of screens (used by AE2's mouse
 * wheel items). Fires with the sensitivity-adjusted vertical delta, like NeoForge.
 */
@Mixin(MouseHandler.class)
public class MouseWheelMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void ae2$onScrollWithoutScreen(long windowPointer, double xOffset, double yOffset, CallbackInfo ci) {
        if (windowPointer != minecraft.getWindow().handle() || minecraft.gui.screen() != null
                || minecraft.gui.overlay() != null || minecraft.player == null) {
            return;
        }
        boolean discrete = minecraft.options.discreteMouseScroll().get();
        double sensitivity = minecraft.options.mouseWheelSensitivity().get();
        double delta = (discrete ? Math.signum(yOffset) : yOffset) * sensitivity;
        if (MouseWheelScrolled.EVENT.invoker().onWheelScrolled(delta)) {
            ci.cancel();
        }
    }
}
