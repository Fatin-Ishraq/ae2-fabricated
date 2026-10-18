package appeng.platform.neoforge.common.extensions;

import net.minecraft.world.level.storage.ValueOutput;

import appeng.platform.neoforge.common.util.ValueIOSerializable;

/**
 * FABRIC: NeoForge's {@code ValueOutput#putChild}. Injected through ae2.classtweaker and
 * {@code appeng.mixins.platform.ValueOutputMixin}.
 */
public interface IValueOutputExtension {
    default void putChild(String key, ValueIOSerializable value) {
        value.serialize(((ValueOutput) this).child(key));
    }
}
