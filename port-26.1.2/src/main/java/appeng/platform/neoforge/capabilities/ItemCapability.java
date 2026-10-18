package appeng.platform.neoforge.capabilities;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * FABRIC: An item capability backed by a Fabric {@link ItemApiLookup}. The context is converted as well, since the
 * standard Fabric item lookups take a {@code ContainerItemContext} where NeoForge passes an {@code ItemAccess}.
 *
 * @param <T> the type AE2 sees
 * @param <C> the context type AE2 sees
 */
public final class ItemCapability<T, C> extends BaseCapability<T, C> {
    private final Bridge<T, C, ?, ?> bridge;

    private ItemCapability(Identifier name, Class<T> typeClass, Class<C> contextClass, Bridge<T, C, ?, ?> bridge) {
        super(name, typeClass, contextClass);
        this.bridge = bridge;
    }

    public static <T, C> ItemCapability<T, C> create(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        var lookup = ItemApiLookup.get(name, typeClass, contextClass);
        return new ItemCapability<>(name, typeClass, contextClass, new Bridge<>(lookup,
                Function.identity(), Function.identity(), Function.identity(), Function.identity()));
    }

    public static <T> ItemCapability<T, @Nullable Void> createVoid(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, Void.class);
    }

    /**
     * FABRIC: A capability whose providers and queries go through an existing Fabric lookup.
     */
    public static <T, C, F, FC> ItemCapability<T, C> bridge(Identifier name, Class<T> typeClass, Class<C> contextClass,
            ItemApiLookup<F, FC> lookup, Function<T, F> toFabric, Function<F, T> fromFabric,
            Function<C, FC> contextToFabric, Function<FC, C> contextFromFabric) {
        return new ItemCapability<>(name, typeClass, contextClass,
                new Bridge<>(lookup, toFabric, fromFabric, contextToFabric, contextFromFabric));
    }

    @Nullable
    public T getCapability(ItemStack stack, C context) {
        if (stack.isEmpty()) {
            return null;
        }
        return bridge.find(stack, context);
    }

    void register(ICapabilityProvider<ItemStack, C, T> provider, ItemLike... items) {
        bridge.register(provider, items);
    }

    private record Bridge<T, C, F, FC>(ItemApiLookup<F, FC> lookup, Function<T, F> toFabric,
            Function<F, T> fromFabric, Function<C, FC> contextToFabric, Function<FC, C> contextFromFabric) {
        @Nullable
        T find(ItemStack stack, C context) {
            var result = lookup.find(stack, contextToFabric.apply(context));
            return result == null ? null : fromFabric.apply(result);
        }

        void register(ICapabilityProvider<ItemStack, C, T> provider, ItemLike... items) {
            lookup.registerForItems((stack, context) -> {
                var result = provider.getCapability(stack, contextFromFabric.apply(context));
                return result == null ? null : toFabric.apply(result);
            }, items);
        }
    }
}
