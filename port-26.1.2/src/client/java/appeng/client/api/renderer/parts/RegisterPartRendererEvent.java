package appeng.client.api.renderer.parts;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.ApiStatus;


import appeng.api.parts.IPart;

public class RegisterPartRendererEvent {
    /**
     * FABRIC: NeoForge posts this event on the mod bus; on Fabric, register a listener here.
     */
    public static final Event<Consumer<RegisterPartRendererEvent>> EVENT = EventFactory.createArrayBacked(
            Consumer.class,
            listeners -> event -> {
                for (var listener : listeners) {
                    listener.accept(event);
                }
            });

    private final PartRegistrationSink delegate;

    @ApiStatus.Internal
    public RegisterPartRendererEvent(PartRegistrationSink delegate) {
        this.delegate = delegate;
    }

    public <T extends IPart> void register(Class<T> partClass, PartRenderer<? super T, ?> renderer) {
        delegate.register(partClass, renderer);
    }

    @FunctionalInterface
    @ApiStatus.Internal
    public interface PartRegistrationSink {
        <T extends IPart> void register(Class<T> partClass, PartRenderer<? super T, ?> factory);
    }
}
