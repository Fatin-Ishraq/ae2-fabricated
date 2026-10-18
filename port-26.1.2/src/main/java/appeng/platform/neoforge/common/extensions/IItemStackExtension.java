package appeng.platform.neoforge.common.extensions;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.FuelValues;

import appeng.platform.neoforge.capabilities.ItemCapability;

/**
 * FABRIC: NeoForge's extra methods on {@link ItemStack}. Injected through ae2.classtweaker and
 * {@code appeng.mixins.platform.ItemStackMixin}.
 */
public interface IItemStackExtension {
    private ItemStack self() {
        return (ItemStack) (Object) this;
    }

    @Nullable
    default <T> T getCapability(ItemCapability<T, Void> capability) {
        return capability.getCapability(self(), null);
    }

    @Nullable
    default <T, C> T getCapability(ItemCapability<T, C> capability, C context) {
        return capability.getCapability(self(), context);
    }

    default boolean isComponentsPatchEmpty() {
        return self().getComponentsPatch().isEmpty();
    }

    default int getBurnTime(@Nullable RecipeType<?> recipeType, FuelValues fuelValues) {
        return fuelValues.burnDuration(self());
    }
}
