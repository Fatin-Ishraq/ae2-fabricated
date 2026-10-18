package appeng.platform.neoforge.registries.holdersets;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Either;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;

/**
 * FABRIC: The union of several holder sets, as a snapshot taken at construction time.
 */
public final class OrHolderSet<T> implements HolderSet<T> {
    private final HolderSet<T> union;

    public OrHolderSet(List<HolderSet<T>> values) {
        var holders = new LinkedHashSet<Holder<T>>();
        for (var set : values) {
            set.forEach(holders::add);
        }
        this.union = HolderSet.direct(List.copyOf(holders));
    }

    @Override
    public Stream<Holder<T>> stream() {
        return union.stream();
    }

    @Override
    public int size() {
        return union.size();
    }

    @Override
    public boolean isBound() {
        return true;
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return union.unwrap();
    }

    @Override
    public Optional<Holder<T>> getRandomElement(RandomSource random) {
        return union.getRandomElement(random);
    }

    @Override
    public Holder<T> get(int index) {
        return union.get(index);
    }

    @Override
    public boolean contains(Holder<T> holder) {
        return union.contains(holder);
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return union.canSerializeIn(owner);
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return Optional.empty();
    }

    @Override
    public Iterator<Holder<T>> iterator() {
        return union.iterator();
    }
}
