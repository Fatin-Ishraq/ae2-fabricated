package appeng.platform.neoforge.transfer.energy;

import com.google.common.primitives.Ints;

import appeng.platform.neoforge.transfer.transaction.TransactionContext;

/**
 * FABRIC: An energy container (FE-like units). At the platform boundary it is converted to and from Team Reborn
 * Energy's {@code EnergyStorage} (1 E = 1 FE) by {@link appeng.platform.FabricTransfer}.
 */
public interface EnergyHandler {
    long getAmountAsLong();

    default int getAmountAsInt() {
        return Ints.saturatedCast(getAmountAsLong());
    }

    long getCapacityAsLong();

    default int getCapacityAsInt() {
        return Ints.saturatedCast(getCapacityAsLong());
    }

    int insert(int amount, TransactionContext transaction);

    int extract(int amount, TransactionContext transaction);
}
