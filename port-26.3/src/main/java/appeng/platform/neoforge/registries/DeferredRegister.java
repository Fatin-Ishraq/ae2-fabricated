/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.neoforge.registries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * FABRIC: Minimal stand-in for NeoForge's DeferredRegister. Entries are collected when the definition classes are
 * initialized and written into the vanilla registry when {@link #register()} is called, which AppEngBase does in
 * vanilla registry order (blocks before items before block entities, ...). This reproduces the lazy construction
 * semantics upstream relies on (e.g. block items being created after all blocks exist).
 */
public class DeferredRegister<T> {
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final String namespace;
    private final List<DeferredHolder<T, ?>> entries = new ArrayList<>();
    private boolean registered;

    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        this.registryKey = registryKey;
        this.namespace = namespace;
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        return new DeferredRegister<>(registryKey, namespace);
    }

    public static <T> DeferredRegister<T> create(Registry<T> registry, String namespace) {
        return new DeferredRegister<>(registry.key(), namespace);
    }

    public static Blocks createBlocks(String namespace) {
        return new Blocks(namespace);
    }

    public static Items createItems(String namespace) {
        return new Items(namespace);
    }

    public ResourceKey<? extends Registry<T>> getRegistryKey() {
        return registryKey;
    }

    public String getNamespace() {
        return namespace;
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> sup) {
        return register(name, key -> sup.get());
    }

    @SuppressWarnings("unchecked")
    public <I extends T> DeferredHolder<T, I> register(String name, Function<Identifier, ? extends I> func) {
        var id = Identifier.fromNamespaceAndPath(namespace, name);
        var holder = (DeferredHolder<T, I>) createHolder(ResourceKey.create(registryKey, id), func);
        addEntry(holder);
        return holder;
    }

    protected DeferredHolder<T, ? extends T> createHolder(ResourceKey<T> key,
            Function<Identifier, ? extends T> factory) {
        return new DeferredHolder<>(key, factory);
    }

    protected final void addEntry(DeferredHolder<T, ?> holder) {
        if (registered) {
            throw new IllegalStateException("Cannot add entries to " + registryKey.identifier() + " after registration");
        }
        entries.add(holder);
    }

    public List<DeferredHolder<T, ?>> getEntries() {
        return List.copyOf(entries);
    }

    @SuppressWarnings("unchecked")
    public Registry<T> getRegistry() {
        var registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(registryKey.identifier());
        return Objects.requireNonNull(registry, () -> "Unknown registry " + registryKey.identifier());
    }

    /**
     * Constructs all collected entries and writes them into the registry.
     */
    public void register() {
        if (registered) {
            throw new IllegalStateException("Already registered " + registryKey.identifier());
        }
        registered = true;
        var registry = getRegistry();
        for (var entry : entries) {
            entry.bind(registry);
        }
    }

    public static class Blocks extends DeferredRegister<Block> {
        protected Blocks(String namespace) {
            super(Registries.BLOCK, namespace);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <B extends Block> DeferredBlock<B> register(String name, Supplier<? extends B> sup) {
            return (DeferredBlock<B>) super.register(name, sup);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <B extends Block> DeferredBlock<B> register(String name, Function<Identifier, ? extends B> func) {
            return (DeferredBlock<B>) super.register(name, func);
        }

        public <B extends Block> DeferredBlock<B> registerBlock(String name,
                Function<BlockBehaviour.Properties, ? extends B> func, BlockBehaviour.Properties props) {
            // Vanilla requires the registry key to be set on the properties before the block is created
            return register(name, id -> func.apply(props.setId(ResourceKey.create(Registries.BLOCK, id))));
        }

        public <B extends Block> DeferredBlock<B> registerBlock(String name,
                Function<BlockBehaviour.Properties, ? extends B> func) {
            return registerBlock(name, func, BlockBehaviour.Properties.of());
        }

        @Override
        protected DeferredHolder<Block, ? extends Block> createHolder(ResourceKey<Block> key,
                Function<Identifier, ? extends Block> factory) {
            return new DeferredBlock<>(key, factory);
        }
    }

    public static class Items extends DeferredRegister<Item> {
        protected Items(String namespace) {
            super(Registries.ITEM, namespace);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <I extends Item> DeferredItem<I> register(String name, Supplier<? extends I> sup) {
            return (DeferredItem<I>) super.register(name, sup);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <I extends Item> DeferredItem<I> register(String name, Function<Identifier, ? extends I> func) {
            return (DeferredItem<I>) super.register(name, func);
        }

        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties, ? extends I> func,
                Item.Properties props) {
            // Vanilla requires the registry key to be set on the properties before the item is created
            return register(name, id -> func.apply(props.setId(ResourceKey.create(Registries.ITEM, id))));
        }

        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties, ? extends I> func) {
            return registerItem(name, func, new Item.Properties());
        }

        @Override
        protected DeferredHolder<Item, ? extends Item> createHolder(ResourceKey<Item> key,
                Function<Identifier, ? extends Item> factory) {
            return new DeferredItem<>(key, factory);
        }
    }
}
