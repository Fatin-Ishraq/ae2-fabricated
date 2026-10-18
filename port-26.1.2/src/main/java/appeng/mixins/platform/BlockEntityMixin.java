package appeng.mixins.platform;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.api.blockgetter.v2.RenderDataBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.platform.neoforge.common.extensions.IBlockEntityExtension;
import appeng.platform.neoforge.model.data.ModelData;

/**
 * FABRIC: Adds {@link IBlockEntityExtension} to {@link BlockEntity} at runtime (declared for compilation in
 * ae2.classtweaker), and exposes NeoForge-style model data as Fabric block entity render data.
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements IBlockEntityExtension, RenderDataBlockEntity {
    @Override
    @Nullable
    public Object getRenderData() {
        var modelData = getModelData();
        return modelData == ModelData.EMPTY ? null : modelData;
    }
}
