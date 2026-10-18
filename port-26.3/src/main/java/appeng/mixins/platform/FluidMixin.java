package appeng.mixins.platform;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.material.Fluid;

import appeng.platform.neoforge.common.extensions.IFluidExtension;

/**
 * FABRIC: Adds {@link IFluidExtension} to {@link Fluid} at runtime (declared for compilation in ae2.classtweaker).
 */
@Mixin(Fluid.class)
public abstract class FluidMixin implements IFluidExtension {
}
