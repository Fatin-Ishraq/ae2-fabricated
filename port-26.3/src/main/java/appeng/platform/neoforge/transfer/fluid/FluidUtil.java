package appeng.platform.neoforge.transfer.fluid;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.platform.FabricTransfer;
import appeng.platform.neoforge.fluids.FluidStack;
import appeng.platform.neoforge.transfer.ResourceHandler;

/**
 * FABRIC: Fluid helpers (the subset AE2 uses), implemented with the Fabric transfer API.
 */
public final class FluidUtil {
    private FluidUtil() {
    }

    /**
     * @return the first fluid contained in an item (for example a bucket or tank item), or an empty stack.
     */
    public static FluidStack getFirstStackContained(ItemStack stack) {
        if (stack.isEmpty()) {
            return FluidStack.EMPTY;
        }
        var storage = FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        if (storage == null) {
            return FluidStack.EMPTY;
        }
        for (var view : storage.nonEmptyViews()) {
            return FluidStack.of(view.getResource(), (int) Math.min(Integer.MAX_VALUE, view.getAmount()));
        }
        return FluidStack.EMPTY;
    }

    /**
     * Fills or empties the item in the hand of the player using the given handler, like using a bucket on a tank.
     */
    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, @Nullable BlockPos pos,
            ResourceHandler<FluidResource> handler, @Nullable Direction side) {
        return FluidStorageUtil.interactWithFluidStorage(FabricTransfer.toFluidStorage(handler), player, hand);
    }
}
