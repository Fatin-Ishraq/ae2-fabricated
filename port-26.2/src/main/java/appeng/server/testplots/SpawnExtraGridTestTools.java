package appeng.server.testplots;

import net.minecraft.resources.Identifier;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;

/**
 * Triggered to spawn additional testing tools into a container placed next to a spawned AE2 grid.
 */
@TestPlotClass
public class SpawnExtraGridTestTools {
    /**
     * FABRIC: NeoForge posts this on the game event bus; on Fabric listeners register here.
     */
    public static final Event<Consumer<SpawnExtraGridTestTools>> EVENT = EventFactory.createArrayBacked(
            Consumer.class,
            listeners -> event -> {
                for (var listener : listeners) {
                    listener.accept(event);
                }
            });

    static {
        EVENT.register(SpawnTestTools::spawnWirelessTerminals);
    }

    private final Identifier plotId;
    private final InternalInventory inventory;
    private final IGrid grid;

    public SpawnExtraGridTestTools(Identifier plotId, InternalInventory inventory, IGrid grid) {
        this.plotId = plotId;
        this.inventory = inventory;
        this.grid = grid;
    }

    public Identifier getPlotId() {
        return plotId;
    }

    public InternalInventory getInventory() {
        return inventory;
    }

    public IGrid getGrid() {
        return grid;
    }
}
