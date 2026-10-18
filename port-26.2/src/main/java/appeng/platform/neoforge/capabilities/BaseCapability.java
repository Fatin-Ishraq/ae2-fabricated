package appeng.platform.neoforge.capabilities;

import net.minecraft.resources.Identifier;

/**
 * FABRIC: Common data of block and item capabilities.
 */
public abstract class BaseCapability<T, C> {
    private final Identifier name;
    private final Class<T> typeClass;
    private final Class<C> contextClass;

    protected BaseCapability(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        this.name = name;
        this.typeClass = typeClass;
        this.contextClass = contextClass;
    }

    public final Identifier name() {
        return name;
    }

    public final Class<T> typeClass() {
        return typeClass;
    }

    public final Class<C> contextClass() {
        return contextClass;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + "]";
    }
}
