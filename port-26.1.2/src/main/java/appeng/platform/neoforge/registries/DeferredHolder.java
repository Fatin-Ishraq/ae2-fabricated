/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.neoforge.registries;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

/**
 * FABRIC: Minimal stand-in for NeoForge's DeferredHolder. Unlike NeoForge's version this is not itself a
 * {@link Holder}; use {@link #holder()} to get the bound registry holder.
 */
public class DeferredHolder<R, T extends R> implements Supplier<T> {
    private final ResourceKey<R> key;
    @Nullable
    private Function<Identifier, ? extends R> factory;
    @Nullable
    private T value;
    @Nullable
    private Holder<R> holder;

    protected DeferredHolder(ResourceKey<R> key, Function<Identifier, ? extends R> factory) {
        this.key = key;
        this.factory = factory;
    }

    @SuppressWarnings("unchecked")
    void bind(Registry<R> registry) {
        if (factory == null) {
            throw new IllegalStateException("Already bound: " + key);
        }
        R created = Objects.requireNonNull(factory.apply(key.identifier()), () -> "Factory returned null: " + key);
        factory = null;
        value = (T) created;
        holder = Registry.registerForHolder(registry, key, created);
    }

    @Override
    public T get() {
        if (value == null) {
            throw new IllegalStateException("Registry object not yet registered: " + key.identifier());
        }
        return value;
    }

    public T value() {
        return get();
    }

    public boolean isBound() {
        return value != null;
    }

    public Identifier getId() {
        return key.identifier();
    }

    public ResourceKey<R> getKey() {
        return key;
    }

    public Holder<R> holder() {
        if (holder == null) {
            throw new IllegalStateException("Registry object not yet registered: " + key.identifier());
        }
        return holder;
    }

    @Override
    public String toString() {
        return "DeferredHolder{" + key + "}";
    }
}
