package appeng.platform.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;

/**
 * FABRIC: Renders a custom block outline. Collected by
 * {@link appeng.platform.neoforge.client.event.ExtractBlockOutlineRenderStateEvent} and called from Fabric's
 * {@code LevelRenderEvents.BEFORE_BLOCK_OUTLINE}.
 */
@FunctionalInterface
public interface CustomBlockOutlineRenderer {
    /**
     * @return true to suppress the vanilla outline
     */
    boolean render(BlockOutlineRenderState renderState, SubmitNodeCollector submitNodeCollector, PoseStack poseStack,
            LevelRenderState levelRenderState);
}
