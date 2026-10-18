package appeng.api.parts;

import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import appeng.api.inventories.PartApiLookup;

/**
 * Allows registering Fabric API lookups (the equivalent of NeoForge capabilities) for parts. Lookups on a part host
 * (e.g. the cable bus) are forwarded to the part attached on the queried side.
 * <p>
 * FABRIC: On NeoForge this is a mod-bus event. On Fabric, register a listener with {@link #EVENT}; it is invoked once
 * during AE2 initialization. Registrations can also be made at any time via {@link PartApiLookup}.
 */
public class RegisterPartCapabilitiesEvent {

    public static final Event<Consumer<RegisterPartCapabilitiesEvent>> EVENT = EventFactory.createArrayBacked(
            Consumer.class,
            listeners -> event -> {
                for (var listener : listeners) {
                    listener.accept(event);
                }
            });

    /**
     * When using lookups with a context other than {@link Direction}, you need to register a mapping function for AE2
     * to get the side from the context. It cannot determine which part on a part host should handle the lookup
     * otherwise.
     */
    public <T, C> void registerContext(BlockApiLookup<T, C> capability, Function<C, Direction> directionGetter) {
        PartApiLookup.registerCustomContext(capability, directionGetter);
    }

    /**
     * Expose a lookup for a part class.
     * <p>
     * When looking for an API instance, providers are queried starting from the class of the part, and then moving up
     * to its superclass, and so on, until a provider returning a nonnull API is found.
     * <p>
     * If the context of the lookup is not {@link Direction}, you need to register a mapping function for your custom
     * context! That must be done before this function is called.
     */
    public <T, C, P extends IPart> void register(BlockApiLookup<T, C> capability,
            PartApiLookup.PartApiProvider<T, C, P> provider,
            Class<P> partClass) {
        Objects.requireNonNull(capability, "capability");
        Objects.requireNonNull(partClass, "partClass");
        Objects.requireNonNull(provider, "provider");

        if (partClass.isInterface() || Modifier.isAbstract(partClass.getModifiers())) {
            throw new IllegalArgumentException(
                    "Capabilities can only be registered for concrete part classes: " + partClass.getCanonicalName());
        }

        PartApiLookup.register(capability, provider, partClass);
    }

    /**
     * Adds a new type of block entity that will participate in forwarding API lookups to its attached parts.
     */
    public <T extends BlockEntity & IPartHost> void addHostType(BlockEntityType<T> hostType) {
        PartApiLookup.addHostType(hostType);
    }
}
