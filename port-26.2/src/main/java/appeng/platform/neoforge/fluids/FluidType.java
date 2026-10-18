package appeng.platform.neoforge.fluids;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import appeng.platform.neoforge.common.SoundAction;
import appeng.platform.neoforge.common.SoundActions;

/**
 * FABRIC: Fluid properties, answered by Fabric's {@link FluidVariantAttributes}. Fabric measures fluids in droplets,
 * 81000 per bucket, instead of NeoForge's 1000 millibuckets.
 */
public final class FluidType {
    public static final int BUCKET_VOLUME = (int) FluidConstants.BUCKET;

    private final Fluid fluid;

    private FluidType(Fluid fluid) {
        this.fluid = fluid;
    }

    public static FluidType of(Fluid fluid) {
        return new FluidType(fluid);
    }

    private FluidVariant variant() {
        return FluidVariant.of(fluid);
    }

    public boolean isLighterThanAir() {
        return fluid != Fluids.EMPTY && FluidVariantAttributes.isLighterThanAir(variant());
    }

    public Component getDescription() {
        return FluidVariantAttributes.getName(variant());
    }

    @Nullable
    public SoundEvent getSound(SoundAction action) {
        if (fluid == Fluids.EMPTY) {
            return null;
        }
        if (action == SoundActions.BUCKET_FILL) {
            return FluidVariantAttributes.getFillSound(variant());
        } else if (action == SoundActions.BUCKET_EMPTY) {
            return FluidVariantAttributes.getEmptySound(variant());
        }
        return null;
    }

    @Nullable
    public SoundEvent getSound(@Nullable Player player, SoundAction action) {
        return getSound(action);
    }
}
