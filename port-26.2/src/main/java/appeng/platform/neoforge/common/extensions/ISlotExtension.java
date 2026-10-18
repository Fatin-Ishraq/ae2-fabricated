package appeng.platform.neoforge.common.extensions;

import net.minecraft.world.inventory.Slot;

/**
 * FABRIC: NeoForge's {@code Slot#getSlotIndex()}. Injected through ae2.classtweaker and
 * {@code appeng.mixins.platform.SlotMixin}.
 */
public interface ISlotExtension {
    /**
     * @return the index of this slot in its container.
     */
    default int getSlotIndex() {
        return ((Slot) this).getContainerSlot();
    }
}
