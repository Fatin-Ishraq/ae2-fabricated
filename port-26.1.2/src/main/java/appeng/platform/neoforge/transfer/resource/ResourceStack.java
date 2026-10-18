package appeng.platform.neoforge.transfer.resource;

/**
 * FABRIC: A resource with an amount.
 */
public record ResourceStack<T extends Resource>(T resource, int amount) {
    public boolean isEmpty() {
        return resource.isEmpty() || amount <= 0;
    }
}
