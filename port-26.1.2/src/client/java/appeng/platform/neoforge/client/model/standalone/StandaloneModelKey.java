package appeng.platform.neoforge.client.model.standalone;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.minecraft.client.resources.model.ModelDebugName;

/**
 * FABRIC: Key of an extra model that is baked with the other models. Backed by a Fabric {@link ExtraModelKey}.
 */
public final class StandaloneModelKey<T> {
    private final ModelDebugName name;
    private final ExtraModelKey<T> fabricKey;

    public StandaloneModelKey(ModelDebugName name) {
        this.name = name;
        this.fabricKey = ExtraModelKey.create(name::debugName);
    }

    public String getName() {
        return name.debugName();
    }

    /**
     * FABRIC: the key for the Fabric model loading API.
     */
    public ExtraModelKey<T> fabricKey() {
        return fabricKey;
    }

    @Override
    public String toString() {
        return "StandaloneModelKey[name=" + getName() + "]";
    }
}
