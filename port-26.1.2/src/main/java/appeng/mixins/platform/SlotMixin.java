package appeng.mixins.platform;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.inventory.Slot;

import appeng.platform.neoforge.common.extensions.ISlotExtension;

/**
 * FABRIC: Adds {@link ISlotExtension} to {@link Slot} at runtime (declared for compilation in ae2.classtweaker).
 */
@Mixin(Slot.class)
public abstract class SlotMixin implements ISlotExtension {
}
