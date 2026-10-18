package appeng.mixins.platform.client;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

import appeng.platform.neoforge.client.extensions.IBlockStateModelExtension;

/**
 * FABRIC: Adds {@link IBlockStateModelExtension} to {@link BlockStateModel} at runtime (declared for compilation in ae2.classtweaker).
 */
@Mixin(BlockStateModel.class)
public interface BlockStateModelMixin extends IBlockStateModelExtension {
}
