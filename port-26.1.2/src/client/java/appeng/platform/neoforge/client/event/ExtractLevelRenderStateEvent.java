package appeng.platform.neoforge.client.event;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.LevelRenderState;

/**
 * FABRIC: Called at the end of level render state extraction, backed by Fabric's {@code END_EXTRACTION}.
 */
public class ExtractLevelRenderStateEvent {
    private final ClientLevel level;
    private final LevelRenderState renderState;

    private ExtractLevelRenderStateEvent(ClientLevel level, LevelRenderState renderState) {
        this.level = level;
        this.renderState = renderState;
    }

    public static void register(Consumer<ExtractLevelRenderStateEvent> listener) {
        LevelRenderEvents.END_EXTRACTION.register(context -> {
            if (context.level() != null) {
                listener.accept(new ExtractLevelRenderStateEvent(context.level(), context.levelState()));
            }
        });
    }

    public ClientLevel getLevel() {
        return level;
    }

    public LevelRenderState getRenderState() {
        return renderState;
    }
}
