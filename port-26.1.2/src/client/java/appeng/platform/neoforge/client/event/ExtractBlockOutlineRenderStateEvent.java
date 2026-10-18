package appeng.platform.neoforge.client.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

import appeng.platform.neoforge.client.CustomBlockOutlineRenderer;

/**
 * FABRIC: Lets a listener add custom block outline renderers for the block the player is looking at. Built from
 * Fabric's {@code AFTER_BLOCK_OUTLINE_EXTRACTION}; the renderers run in {@code BEFORE_BLOCK_OUTLINE}.
 */
public class ExtractBlockOutlineRenderStateEvent {
    private static final List<CustomBlockOutlineRenderer> pendingRenderers = new ArrayList<>();

    private final ClientLevel level;
    private final Camera camera;
    private final BlockHitResult hitResult;

    private ExtractBlockOutlineRenderStateEvent(ClientLevel level, Camera camera, BlockHitResult hitResult) {
        this.level = level;
        this.camera = camera;
        this.hitResult = hitResult;
    }

    /**
     * Registers the listener with the Fabric level rendering events.
     */
    public static void register(Consumer<ExtractBlockOutlineRenderStateEvent> listener) {
        LevelRenderEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.register((context, hit) -> {
            pendingRenderers.clear();
            if (hit instanceof BlockHitResult blockHit && context.level() != null) {
                listener.accept(new ExtractBlockOutlineRenderStateEvent(context.level(), context.camera(), blockHit));
            }
        });
        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, outlineRenderState) -> {
            boolean suppressVanilla = false;
            for (var renderer : pendingRenderers) {
                suppressVanilla |= renderer.render(outlineRenderState, context.bufferSource(),
                        context.poseStack(), outlineRenderState.isTranslucent(), context.levelState());
            }
            return !suppressVanilla;
        });
    }

    public ClientLevel getLevel() {
        return level;
    }

    public Camera getCamera() {
        return camera;
    }

    public BlockHitResult getHitResult() {
        return hitResult;
    }

    public BlockPos getBlockPos() {
        return hitResult.getBlockPos();
    }

    public boolean isHighContrast() {
        return Minecraft.getInstance().options.highContrastBlockOutline().get();
    }

    public void addCustomRenderer(CustomBlockOutlineRenderer renderer) {
        pendingRenderers.add(renderer);
    }
}
