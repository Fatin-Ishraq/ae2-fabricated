package appeng.platform.neoforge.client.model.quad;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.resources.model.geometry.BakedQuad;

/**
 * FABRIC: Per-vertex colors and the ambient occlusion flag of a quad. Vanilla 26.x quads have neither (NeoForge patches
 * them in), so AE2's {@link MutableQuad} attaches them to the {@link BakedQuad} through
 * {@code appeng.mixins.platform.client.BakedQuadMixin}. They are applied when AE2 models emit quads to the Fabric
 * renderer (see {@code appeng.platform.neoforge.client.model.DynamicBlockStateModel}).
 */
public interface QuadExtras {
    @Nullable
    int[] ae2$getColors();

    void ae2$setColors(@Nullable int[] colors);

    boolean ae2$hasAmbientOcclusion();

    void ae2$setAmbientOcclusion(boolean ambientOcclusion);

    /**
     * @return the ARGB colors of the 4 vertices, or null if the quad is white
     */
    @Nullable
    static int[] colors(BakedQuad quad) {
        return ((QuadExtras) (Object) quad).ae2$getColors();
    }

    static boolean ambientOcclusion(BakedQuad quad) {
        return ((QuadExtras) (Object) quad).ae2$hasAmbientOcclusion();
    }

    static BakedQuad with(BakedQuad quad, @Nullable int[] colors, boolean ambientOcclusion) {
        var extras = (QuadExtras) (Object) quad;
        extras.ae2$setColors(colors);
        extras.ae2$setAmbientOcclusion(ambientOcclusion);
        return quad;
    }
}
