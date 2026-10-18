package appeng.client.api.model.parts;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

/**
 * Subscribe to this event on your mod event bus to register part model types with AE2.
 */
public class RegisterPartModelsEvent {
    /**
     * FABRIC: NeoForge posts this event on the mod bus; on Fabric, register a listener here.
     */
    public static final Event<Consumer<RegisterPartModelsEvent>> EVENT = EventFactory.createArrayBacked(
            Consumer.class,
            listeners -> event -> {
                for (var listener : listeners) {
                    listener.accept(event);
                }
            });

    private final ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends PartModel.Unbaked>> modelIdMapper;

    @ApiStatus.Internal
    public RegisterPartModelsEvent(
            ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends PartModel.Unbaked>> modelIdMapper) {
        this.modelIdMapper = modelIdMapper;
    }

    public void registerModelType(Identifier type, MapCodec<? extends PartModel.Unbaked> codec) {
        this.modelIdMapper.put(type, codec);
    }
}
