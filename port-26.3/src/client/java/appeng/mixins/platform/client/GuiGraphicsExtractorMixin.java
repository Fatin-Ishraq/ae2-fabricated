package appeng.mixins.platform.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

import appeng.platform.neoforge.client.extensions.IGuiGraphicsExtension;

/**
 * FABRIC: Implements {@link IGuiGraphicsExtension} (declared for compilation in ae2.classtweaker).
 */
@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin implements IGuiGraphicsExtension {
    @Shadow
    @Final
    private GuiGraphicsExtractor.ScissorStack scissorStack;

    @Shadow
    @Final
    private GuiRenderState guiRenderState;

    @Override
    @Nullable
    public ScreenRectangle peekScissorStack() {
        return scissorStack.peek();
    }

    @Override
    public void submitGuiElementRenderState(GuiElementRenderState renderState) {
        guiRenderState.addGuiElement(renderState);
    }

    @Override
    public void submitPictureInPictureRenderState(PictureInPictureRenderState renderState) {
        guiRenderState.addPicturesInPictureState(renderState);
    }
}
