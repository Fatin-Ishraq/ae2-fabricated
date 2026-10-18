package appeng.platform.neoforge.capabilities;

/**
 * FABRIC: Kept for API compatibility. Fabric lookups are queried on every access, so there is no invalidation and
 * these listeners are never called.
 */
@FunctionalInterface
public interface ICapabilityInvalidationListener {
    boolean onInvalidate();
}
