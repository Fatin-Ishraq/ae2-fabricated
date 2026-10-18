package appeng.mixins.platform;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.block.Block;

import appeng.platform.neoforge.common.extensions.IBlockExtension;

/**
 * FABRIC: Adds {@link IBlockExtension} to {@link Block} at runtime (declared for compilation in ae2.classtweaker).
 */
@Mixin(Block.class)
public abstract class BlockMixin implements IBlockExtension {
}
