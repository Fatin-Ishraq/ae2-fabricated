package appeng.platform.neoforge.common.extensions;

import net.fabricmc.fabric.api.blockgetter.v2.FabricBlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import appeng.platform.neoforge.model.data.ModelData;

/**
 * FABRIC: NeoForge's {@code getModelData(pos)} on block getters. Model data travels as the block entity render data
 * of Fabric's block getter API, see {@link IBlockEntityExtension}. Injected through ae2.classtweaker and
 * {@code appeng.mixins.platform.BlockGetterMixin}.
 */
public interface IBlockGetterExtension {
    default ModelData getModelData(BlockPos pos) {
        var data = ((FabricBlockGetter) (BlockGetter) this).getBlockEntityRenderData(pos);
        return data instanceof ModelData modelData ? modelData : ModelData.EMPTY;
    }
}
