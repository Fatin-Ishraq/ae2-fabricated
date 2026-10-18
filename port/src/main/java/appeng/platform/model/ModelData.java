/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

/**
 * FABRIC: Immutable property bag equivalent to NeoForge's ModelData. Block entities expose it to baked models through
 * Fabric's {@code RenderDataBlockEntity#getRenderData()}, and models read it back with
 * {@code blockView.getBlockEntityRenderData(pos)} (see {@link #of(Object)}).
 */
public final class ModelData {
    public static final ModelData EMPTY = new ModelData(Map.of());

    private final Map<ModelProperty<?>, Object> properties;

    private ModelData(Map<ModelProperty<?>, Object> properties) {
        this.properties = properties;
    }

    public static Builder builder() {
        return new Builder(null);
    }

    /**
     * Converts the result of {@code getBlockEntityRenderData} into model data, defaulting to {@link #EMPTY}.
     */
    public static ModelData of(@Nullable Object renderData) {
        return renderData instanceof ModelData modelData ? modelData : EMPTY;
    }

    public Set<ModelProperty<?>> getProperties() {
        return properties.keySet();
    }

    public boolean has(ModelProperty<?> property) {
        return properties.containsKey(property);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T get(ModelProperty<T> property) {
        return (T) properties.get(property);
    }

    public Builder derive() {
        return new Builder(this);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof ModelData other && properties.equals(other.properties);
    }

    @Override
    public int hashCode() {
        return properties.hashCode();
    }

    public static final class Builder {
        private final Map<ModelProperty<?>, Object> properties = new HashMap<>();

        private Builder(@Nullable ModelData parent) {
            if (parent != null) {
                properties.putAll(parent.properties);
            }
        }

        public <T> Builder with(ModelProperty<T> property, @Nullable T value) {
            if (value == null) {
                properties.remove(property);
            } else {
                properties.put(property, value);
            }
            return this;
        }

        public ModelData build() {
            return properties.isEmpty() ? EMPTY : new ModelData(new HashMap<>(properties));
        }
    }
}
