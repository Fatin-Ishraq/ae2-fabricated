package appeng.platform.neoforge.common.extensions;

import net.minecraft.world.level.material.Fluid;

import appeng.platform.neoforge.fluids.FluidType;

/**
 * FABRIC: NeoForge's {@code Fluid#getFluidType()}. Injected through ae2.classtweaker and
 * {@code appeng.mixins.platform.FluidMixin}.
 */
public interface IFluidExtension {
    default FluidType getFluidType() {
        return FluidType.of((Fluid) this);
    }
}
