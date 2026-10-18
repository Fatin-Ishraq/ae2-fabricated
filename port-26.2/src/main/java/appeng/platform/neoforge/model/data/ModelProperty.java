/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.neoforge.model.data;

import java.util.function.Predicate;

/**
 * FABRIC: Key for a value in {@link ModelData}. Same identity semantics as NeoForge's ModelProperty.
 */
public final class ModelProperty<T> implements Predicate<T> {
    private final Predicate<T> predicate;

    public ModelProperty() {
        this(value -> true);
    }

    public ModelProperty(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(T value) {
        return predicate.test(value);
    }
}
