package appeng.platform.neoforge.transfer.energy;

import appeng.platform.neoforge.transfer.TransferPreconditions;
import appeng.platform.neoforge.transfer.transaction.TransactionContext;

/**
 * FABRIC: An energy handler that stores nothing.
 */
public final class EmptyEnergyHandler implements EnergyHandler {
    public static final EmptyEnergyHandler INSTANCE = new EmptyEnergyHandler();

    private EmptyEnergyHandler() {
    }

    @Override
    public long getAmountAsLong() {
        return 0;
    }

    @Override
    public long getCapacityAsLong() {
        return 0;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        return 0;
    }
}
