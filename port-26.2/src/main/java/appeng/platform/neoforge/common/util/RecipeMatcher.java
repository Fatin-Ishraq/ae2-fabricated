/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.neoforge.common.util;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

/**
 * FABRIC: Replacement for NeoForge's RecipeMatcher. Finds an assignment of inputs to ingredients such that every
 * input matches exactly one ingredient. Uses simple backtracking, which is fine for crafting-grid sized inputs.
 */
public final class RecipeMatcher {
    private RecipeMatcher() {
    }

    /**
     * @return For each input, the index of the ingredient it was matched to, or null if no complete match exists.
     */
    @Nullable
    public static <T> int[] findMatches(List<T> inputs, List<? extends Predicate<T>> tests) {
        if (inputs.size() != tests.size()) {
            return null;
        }
        var result = new int[inputs.size()];
        var used = new boolean[tests.size()];
        return match(inputs, tests, 0, used, result) ? result : null;
    }

    private static <T> boolean match(List<T> inputs, List<? extends Predicate<T>> tests, int inputIndex,
            boolean[] used, int[] result) {
        if (inputIndex == inputs.size()) {
            return true;
        }
        var input = inputs.get(inputIndex);
        for (int i = 0; i < tests.size(); i++) {
            if (!used[i] && tests.get(i).test(input)) {
                used[i] = true;
                result[inputIndex] = i;
                if (match(inputs, tests, inputIndex + 1, used, result)) {
                    return true;
                }
                used[i] = false;
            }
        }
        return false;
    }
}
