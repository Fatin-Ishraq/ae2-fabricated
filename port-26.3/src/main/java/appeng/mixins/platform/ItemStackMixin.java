package appeng.mixins.platform;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.ItemStack;

import appeng.platform.neoforge.common.extensions.IItemStackExtension;

/**
 * FABRIC: Adds {@link IItemStackExtension} to {@link ItemStack} at runtime (declared for compilation in ae2.classtweaker).
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements IItemStackExtension {
}
