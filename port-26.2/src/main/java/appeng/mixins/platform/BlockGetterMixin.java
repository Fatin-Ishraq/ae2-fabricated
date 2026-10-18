package appeng.mixins.platform;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.BlockGetter;

import appeng.platform.neoforge.common.extensions.IBlockGetterExtension;

/**
 * FABRIC: Adds {@link IBlockGetterExtension} to {@link BlockGetter} at runtime (declared for compilation in ae2.classtweaker).
 */
@Mixin(BlockGetter.class)
public interface BlockGetterMixin extends IBlockGetterExtension {
}
