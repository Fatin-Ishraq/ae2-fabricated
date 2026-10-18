package appeng.platform.neoforge.capabilities;

import org.jetbrains.annotations.Nullable;

/**
 * FABRIC: Provides a capability for an object (block entity, item stack, entity).
 */
@FunctionalInterface
public interface ICapabilityProvider<O, C, T> {
    @Nullable
    T getCapability(O object, C context);
}
