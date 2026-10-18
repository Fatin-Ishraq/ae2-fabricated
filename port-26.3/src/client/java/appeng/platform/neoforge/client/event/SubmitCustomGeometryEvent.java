package appeng.platform.neoforge.client.event;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;

/**
 * FABRIC: Lets listeners submit custom geometry for the level, backed by Fabric's {@code COLLECT_SUBMITS}.
 */
public class SubmitCustomGeometryEvent {
    private final LevelRenderState levelRenderState;
    private final SubmitNodeCollector submitNodeCollector;
    private final PoseStack poseStack;

    private SubmitCustomGeometryEvent(LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector,
            PoseStack poseStack) {
        this.levelRenderState = levelRenderState;
        this.submitNodeCollector = submitNodeCollector;
        this.poseStack = poseStack;
    }

    public static void register(Consumer<SubmitCustomGeometryEvent> listener) {
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> listener.accept(
                new SubmitCustomGeometryEvent(context.levelState(), context.submitNodeCollector(),
                        context.poseStack())));
    }

    public LevelRenderState getLevelRenderState() {
        return levelRenderState;
    }

    public SubmitNodeCollector getSubmitNodeCollector() {
        return submitNodeCollector;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }
}
