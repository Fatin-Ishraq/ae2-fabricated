package appeng.platform.neoforge.common.util;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * FABRIC: An object that saves its state to vanilla value I/O.
 */
public interface ValueIOSerializable {
    void serialize(ValueOutput output);

    void deserialize(ValueInput input);
}
