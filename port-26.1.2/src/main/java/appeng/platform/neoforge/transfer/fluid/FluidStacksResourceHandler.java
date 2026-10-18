package appeng.platform.neoforge.transfer.fluid;

import java.util.Objects;

import net.minecraft.core.NonNullList;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import appeng.platform.neoforge.common.util.ValueIOSerializable;
import appeng.platform.neoforge.fluids.FluidStack;
import appeng.platform.neoforge.transfer.ResourceHandler;
import appeng.platform.neoforge.transfer.TransferPreconditions;
import appeng.platform.neoforge.transfer.transaction.SnapshotJournal;
import appeng.platform.neoforge.transfer.transaction.TransactionContext;

/**
 * FABRIC: Fixed-capacity fluid tanks stored as fluid stacks, with transaction support. Amounts are in droplets.
 */
public class FluidStacksResourceHandler implements ResourceHandler<FluidResource>, ValueIOSerializable {
    private static final String VALUE_IO_KEY = "stacks";

    private final NonNullList<FluidStack> stacks;
    private final int capacity;
    private final IndexJournal[] journals;

    public FluidStacksResourceHandler(int size, int capacity) {
        this.stacks = NonNullList.withSize(size, FluidStack.EMPTY);
        this.capacity = capacity;
        this.journals = new IndexJournal[size];
        for (int i = 0; i < size; i++) {
            journals[i] = new IndexJournal(i);
        }
    }

    /**
     * Called after the contents of an index changed and the change was committed.
     */
    protected void onContentsChanged(int index, FluidStack previousContents) {
    }

    protected int getCapacity(int index, FluidResource resource) {
        return capacity;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return true;
    }

    public void set(int index, FluidResource resource, int amount) {
        stacks.set(index, resource.toStack(amount));
    }

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    public FluidResource getResource(int index) {
        return FluidResource.of(stacks.get(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return stacks.get(index).getAmount();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return getCapacity(index, resource);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        var current = stacks.get(index);
        if (!current.isEmpty() && !resource.matches(current) || !isValid(index, resource)) {
            return 0;
        }
        int inserted = Math.min(amount, getCapacity(index, resource) - current.getAmount());
        if (inserted <= 0) {
            return 0;
        }
        journals[index].updateSnapshots(transaction);
        stacks.set(index, resource.toStack(current.getAmount() + inserted));
        return inserted;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        var current = stacks.get(index);
        if (current.isEmpty() || !resource.matches(current)) {
            return 0;
        }
        int extracted = Math.min(amount, current.getAmount());
        if (extracted <= 0) {
            return 0;
        }
        journals[index].updateSnapshots(transaction);
        stacks.set(index, current.copyWithAmount(current.getAmount() - extracted));
        return extracted;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.store(VALUE_IO_KEY, FluidStack.OPTIONAL_CODEC.listOf(), stacks);
    }

    @Override
    public void deserialize(ValueInput input) {
        var loaded = input.read(VALUE_IO_KEY, FluidStack.OPTIONAL_CODEC.listOf()).orElse(java.util.List.of());
        for (int i = 0; i < stacks.size(); i++) {
            stacks.set(i, i < loaded.size() ? Objects.requireNonNull(loaded.get(i)) : FluidStack.EMPTY);
        }
    }

    private final class IndexJournal extends SnapshotJournal<FluidStack> {
        private final int index;

        IndexJournal(int index) {
            this.index = index;
        }

        @Override
        protected FluidStack createSnapshot() {
            return stacks.get(index).copy();
        }

        @Override
        protected void revertToSnapshot(FluidStack snapshot) {
            stacks.set(index, snapshot);
        }

        @Override
        protected void onRootCommit(FluidStack originalState) {
            if (!FluidStack.matches(originalState, stacks.get(index))) {
                onContentsChanged(index, originalState);
            }
        }
    }
}
