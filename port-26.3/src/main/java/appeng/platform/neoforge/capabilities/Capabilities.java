package appeng.platform.neoforge.capabilities;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

import appeng.platform.FabricTransfer;
import appeng.platform.neoforge.transfer.ResourceHandler;
import appeng.platform.neoforge.transfer.access.ItemAccess;
import appeng.platform.neoforge.transfer.energy.EnergyHandler;
import appeng.platform.neoforge.transfer.fluid.FluidResource;
import appeng.platform.neoforge.transfer.item.ItemResource;

/**
 * FABRIC: The standard capabilities, bridged to the Fabric transfer API ({@link ItemStorage}, {@link FluidStorage})
 * and Team Reborn Energy, so AE2 interacts with every Fabric mod that uses those APIs.
 */
public final class Capabilities {
    private Capabilities() {
    }

    public static final class Energy {
        public static final BlockCapability<EnergyHandler, @Nullable Direction> BLOCK = BlockCapability.bridge(
                id("energy_handler"), EnergyHandler.class, Direction.class,
                team.reborn.energy.api.EnergyStorage.SIDED,
                FabricTransfer::toEnergyStorage, FabricTransfer::fromEnergyStorage);

        public static final ItemCapability<EnergyHandler, ItemAccess> ITEM = ItemCapability.bridge(
                id("energy_handler"), EnergyHandler.class, ItemAccess.class,
                team.reborn.energy.api.EnergyStorage.ITEM,
                FabricTransfer::toEnergyStorage, FabricTransfer::fromEnergyStorage,
                ItemAccess::toContext, ItemAccess::fromContext);

        private Energy() {
        }
    }

    public static final class Fluid {
        public static final BlockCapability<ResourceHandler<FluidResource>, @Nullable Direction> BLOCK = BlockCapability
                .bridge(id("fluid_handler"), ResourceHandler.asClass(), Direction.class,
                        FluidStorage.SIDED,
                        FabricTransfer::toFluidStorage, FabricTransfer::fromFluidStorage);

        public static final ItemCapability<ResourceHandler<FluidResource>, ItemAccess> ITEM = ItemCapability.bridge(
                id("fluid_handler"), ResourceHandler.asClass(), ItemAccess.class,
                FluidStorage.ITEM,
                FabricTransfer::toFluidStorage, FabricTransfer::fromFluidStorage,
                ItemAccess::toContext, ItemAccess::fromContext);

        private Fluid() {
        }
    }

    public static final class Item {
        public static final BlockCapability<ResourceHandler<ItemResource>, @Nullable Direction> BLOCK = BlockCapability
                .bridge(id("item_handler"), ResourceHandler.asClass(), Direction.class,
                        ItemStorage.SIDED,
                        FabricTransfer::toItemStorage, FabricTransfer::fromItemStorage);

        private Item() {
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("neoforge", path);
    }
}
