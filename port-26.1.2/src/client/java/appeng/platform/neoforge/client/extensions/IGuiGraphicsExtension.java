package appeng.platform.neoforge.client.extensions;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

/**
 * FABRIC: NeoForge's extra methods on {@code GuiGraphicsExtractor}. Injected through ae2.classtweaker; implemented by
 * {@code appeng.mixins.platform.client.GuiGraphicsExtractorMixin}.
 */
public interface IGuiGraphicsExtension {
    @Nullable
    default ScreenRectangle peekScissorStack() {
        throw new UnsupportedOperationException("Implemented by GuiGraphicsExtractorMixin");
    }

    default void submitGuiElementRenderState(GuiElementRenderState renderState) {
        throw new UnsupportedOperationException("Implemented by GuiGraphicsExtractorMixin");
    }

    default void submitPictureInPictureRenderState(PictureInPictureRenderState renderState) {
        throw new UnsupportedOperationException("Implemented by GuiGraphicsExtractorMixin");
    }
}
