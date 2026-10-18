package appeng.platform.neoforge.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import appeng.platform.neoforge.client.model.quad.QuadExtras;

/**
 * FABRIC: A block state model whose parts depend on the level (usually on model data, see
 * {@link appeng.platform.neoforge.model.data.ModelData}). The Fabric renderer calls
 * {@link #emitQuads}, which collects the parts for the position and emits their quads, including the vertex colors and
 * ambient occlusion flags that AE2 attaches to quads ({@link QuadExtras}).
 */
public interface DynamicBlockStateModel extends BlockStateModel {
    @Override
    @Deprecated
    default void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), random, parts);
    }

    @Override
    void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
            List<BlockStateModelPart> parts);

    @Override
    default void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
            RandomSource random, Predicate<@Nullable Direction> cullTest) {
        var parts = new ArrayList<BlockStateModelPart>();
        collectParts(level, pos, state, random, parts);
        for (var part : parts) {
            emitPart(part, emitter, cullTest);
        }
    }

    /**
     * Emits the quads of a model part, applying {@link QuadExtras}.
     */
    static void emitPart(BlockStateModelPart part, QuadEmitter emitter, Predicate<@Nullable Direction> cullTest) {
        boolean partAo = part.useAmbientOcclusion();
        for (var cullFace : Direction.values()) {
            if (cullTest.test(cullFace)) {
                continue;
            }
            for (var quad : part.getQuads(cullFace)) {
                emitQuad(quad, cullFace, partAo, emitter);
            }
        }
        for (var quad : part.getQuads(null)) {
            emitQuad(quad, null, partAo, emitter);
        }
    }

    private static void emitQuad(BakedQuad quad, @Nullable Direction cullFace, boolean partAo, QuadEmitter emitter) {
        emitter.fromBakedQuad(quad);
        emitter.cullFace(cullFace);
        var colors = QuadExtras.colors(quad);
        if (colors != null) {
            emitter.color(colors[0], colors[1], colors[2], colors[3]);
        }
        emitter.ambientOcclusion(partAo && QuadExtras.ambientOcclusion(quad) ? TriState.DEFAULT : TriState.FALSE);
        emitter.emit();
    }
}
