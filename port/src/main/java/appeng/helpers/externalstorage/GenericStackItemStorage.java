package appeng.helpers.externalstorage;

import com.google.common.primitives.Ints;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.stacks.AEKeyType;
import appeng.util.IVariantConversion;

/**
 * Exposes a {@link GenericStackInv} as the platforms external item storage interface.
 */
public class GenericStackItemStorage extends GenericStackInvStorage<ItemVariant> {
    public GenericStackItemStorage(GenericInternalInventory inv) {
        super(IVariantConversion.ITEM, AEKeyType.items(), inv);
    }
}
