package appeng.mixins.platform.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.resources.model.geometry.BakedQuad;

import appeng.platform.neoforge.client.model.quad.QuadExtras;

/**
 * FABRIC: Stores AE2's per-vertex colors and ambient occlusion flag on baked quads, see {@link QuadExtras}.
 */
@Mixin(BakedQuad.class)
public class BakedQuadMixin implements QuadExtras {
    @Unique
    @Nullable
    private int[] ae2$colors;

    @Unique
    private boolean ae2$noAmbientOcclusion;

    @Override
    @Nullable
    public int[] ae2$getColors() {
        return ae2$colors;
    }

    @Override
    public void ae2$setColors(@Nullable int[] colors) {
        this.ae2$colors = colors;
    }

    @Override
    public boolean ae2$hasAmbientOcclusion() {
        return !ae2$noAmbientOcclusion;
    }

    @Override
    public void ae2$setAmbientOcclusion(boolean ambientOcclusion) {
        this.ae2$noAmbientOcclusion = !ambientOcclusion;
    }
}
