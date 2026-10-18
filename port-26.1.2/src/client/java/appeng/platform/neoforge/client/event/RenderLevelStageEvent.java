package appeng.platform.neoforge.client.event;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.renderer.state.level.LevelRenderState;

/**
 * FABRIC: The render level stages AE2 uses, backed by Fabric's {@link LevelRenderEvents}.
 */
public abstract class RenderLevelStageEvent {
    private final LevelRenderState levelRenderState;
    private final PoseStack poseStack;

    private RenderLevelStageEvent(LevelRenderState levelRenderState, PoseStack poseStack) {
        this.levelRenderState = levelRenderState;
        this.poseStack = poseStack;
    }

    public LevelRenderState getLevelRenderState() {
        return levelRenderState;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    /**
     * Fired after the weather has been rendered. Fabric's closest stage is the end of the main level pass.
     */
    public static final class AfterWeather extends RenderLevelStageEvent {
        private AfterWeather(LevelRenderState levelRenderState, PoseStack poseStack) {
            super(levelRenderState, poseStack);
        }

        public static void register(Consumer<AfterWeather> listener) {
            LevelRenderEvents.END_MAIN.register(
                    context -> listener.accept(new AfterWeather(context.levelState(), context.poseStack())));
        }
    }
}
