/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.init;

import java.util.function.Function;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;

import team.reborn.energy.api.EnergyStorage;

import appeng.api.AECapabilities;
import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.inventories.PartApiLookup;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.blockentity.misc.GrowthAcceleratorBlockEntity;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.blockentity.powersink.AEBasePoweredBlockEntity;
import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.helpers.externalstorage.GenericStackFluidStorage;
import appeng.helpers.externalstorage.GenericStackItemStorage;
import appeng.items.tools.powered.powersink.PoweredItemCapabilities;
import appeng.parts.crafting.PatternProviderPart;
import appeng.parts.encoding.PatternEncodingTerminalPart;
import appeng.parts.misc.InterfacePart;
import appeng.parts.networking.EnergyAcceptorPart;
import appeng.parts.p2p.FEP2PTunnelPart;
import appeng.parts.p2p.FluidP2PTunnelPart;
import appeng.parts.p2p.ItemP2PTunnelPart;

/**
 * FABRIC: Port of upstream's {@code InitCapabilityProviders}, registering Fabric API lookups instead of NeoForge
 * capabilities. Parts are exposed through {@link PartApiLookup}, which forwards cable bus lookups to the part on the
 * queried side.
 */
public final class InitApiLookup {

    private InitApiLookup() {
    }

    public static void init() {
        // Allow forwarding of API lookups to parts for the cable bus
        var partEvent = new RegisterPartCapabilitiesEvent();
        partEvent.addHostType(AEBlockEntities.CABLE_BUS.get());
        registerPartCapabilities(partEvent);
        RegisterPartCapabilitiesEvent.EVENT.invoker().accept(partEvent);

        initInterface();
        initPatternProvider();
        initCondenser();
        initMEChest();
        initMisc();
        initPoweredItem();
        initCrankable();

        for (var type : AEBlockEntities.getSubclassesOf(AEBaseInvBlockEntity.class)) {
            // The condenser registers its own (side-independent) item storage above. NeoForge uses the first
            // provider returning non-null; Fabric only allows one provider per block entity type.
            if (type == AEBlockEntities.CONDENSER.get()) {
                continue;
            }
            ItemStorage.SIDED.registerForBlockEntity((be, side) -> be.getExposedItemHandler(side), type);
        }
        for (var type : AEBlockEntities.getSubclassesOf(AEBasePoweredBlockEntity.class)) {
            EnergyStorage.SIDED.registerForBlockEntity((be, side) -> be.getEnergyStorage(side), type);
        }
        for (var type : AEBlockEntities.getImplementorsOf(IInWorldGridNodeHost.class)) {
            AECapabilities.IN_WORLD_GRID_NODE_HOST.registerForBlockEntity(
                    (object, context) -> (IInWorldGridNodeHost) object, type);
        }

        registerGenericAdapters();
    }

    /**
     * Expose item and fluid storage for any block that exposes a {@link GenericInternalInventory}. Upstream does this
     * by iterating all blocks at the lowest event priority; Fabric API lookups have fallback providers for this.
     */
    private static void registerGenericAdapters() {
        registerGenericInvAdapter(ItemStorage.SIDED, GenericStackItemStorage::new);
        registerGenericInvAdapter(FluidStorage.SIDED, GenericStackFluidStorage::new);
    }

    private static <T> void registerGenericInvAdapter(BlockApiLookup<T, Direction> lookup,
            Function<GenericInternalInventory, T> adapter) {
        lookup.registerFallback((level, pos, state, blockEntity, context) -> {
            var genericInv = AECapabilities.GENERIC_INTERNAL_INV.find(level, pos, state, blockEntity, context);
            if (genericInv != null) {
                return adapter.apply(genericInv);
            }
            return null;
        });
    }

    private static void initInterface() {
        AECapabilities.GENERIC_INTERNAL_INV.registerForBlockEntity(
                (be, context) -> be.getInterfaceLogic().getStorage(),
                AEBlockEntities.INTERFACE.get());

        AECapabilities.ME_STORAGE.registerForBlockEntity(
                (blockEntity, context) -> blockEntity.getInterfaceLogic().getInventory(),
                AEBlockEntities.INTERFACE.get());
    }

    private static void initPatternProvider() {
        AECapabilities.GENERIC_INTERNAL_INV.registerForBlockEntity(
                (blockEntity, context) -> blockEntity.getLogic().getReturnInv(),
                AEBlockEntities.PATTERN_PROVIDER.get());
    }

    private static void initCondenser() {
        // Condenser will always return its external inventory, even when context is null
        // (unlike the base class it derives from)
        ItemStorage.SIDED.registerForBlockEntity(
                (blockEntity, context) -> blockEntity.getExternalInv().toStorage(),
                AEBlockEntities.CONDENSER.get());
        FluidStorage.SIDED.registerForBlockEntity(
                (blockEntity, context) -> blockEntity.getFluidHandler(),
                AEBlockEntities.CONDENSER.get());
        AECapabilities.ME_STORAGE.registerForBlockEntity(
                (blockEntity, context) -> blockEntity.getMEStorage(),
                AEBlockEntities.CONDENSER.get());
    }

    private static void initMEChest() {
        FluidStorage.SIDED.registerForBlockEntity(MEChestBlockEntity::getFluidHandler,
                AEBlockEntities.ME_CHEST.get());
        AECapabilities.ME_STORAGE.registerForBlockEntity(MEChestBlockEntity::getMEStorage,
                AEBlockEntities.ME_CHEST.get());
    }

    private static void initMisc() {
        AECapabilities.CRAFTING_MACHINE.registerForBlockEntity(
                (object, context) -> object,
                AEBlockEntities.MOLECULAR_ASSEMBLER.get());
        ItemStorage.SIDED.registerForBlockEntity(
                (object, context) -> object.getItemHandler(),
                AEBlockEntities.DEBUG_ITEM_GEN.get());
        EnergyStorage.SIDED.registerForBlockEntity(
                (object, context) -> object,
                AEBlockEntities.DEBUG_ENERGY_GEN.get());
        FluidStorage.SIDED.registerForBlockEntity(
                (object, context) -> object.getStorage(context),
                AEBlockEntities.SKY_STONE_TANK.get());
    }

    private static void initPoweredItem() {
        registerPowerStorageItem(AEItems.ENTROPY_MANIPULATOR);
        registerPowerStorageItem(AEItems.CHARGED_STAFF);
        registerPowerStorageItem(AEItems.COLOR_APPLICATOR);
        registerPowerStorageItem(AEItems.PORTABLE_ITEM_CELL1K);
        registerPowerStorageItem(AEItems.PORTABLE_ITEM_CELL4K);
        registerPowerStorageItem(AEItems.PORTABLE_ITEM_CELL16K);
        registerPowerStorageItem(AEItems.PORTABLE_ITEM_CELL64K);
        registerPowerStorageItem(AEItems.PORTABLE_ITEM_CELL256K);
        registerPowerStorageItem(AEItems.PORTABLE_FLUID_CELL1K);
        registerPowerStorageItem(AEItems.PORTABLE_FLUID_CELL4K);
        registerPowerStorageItem(AEItems.PORTABLE_FLUID_CELL16K);
        registerPowerStorageItem(AEItems.PORTABLE_FLUID_CELL64K);
        registerPowerStorageItem(AEItems.PORTABLE_FLUID_CELL256K);
        registerPowerStorageItem(AEItems.MATTER_CANNON);
        registerPowerStorageItem(AEItems.WIRELESS_TERMINAL);
        registerPowerStorageItem(AEItems.WIRELESS_CRAFTING_TERMINAL);
    }

    private static <T extends Item & IAEItemPowerStorage> void registerPowerStorageItem(
            ItemDefinition<T> definition) {
        EnergyStorage.ITEM.registerForItems(
                (stack, context) -> new PoweredItemCapabilities(context),
                definition);
    }

    private static void initCrankable() {
        AECapabilities.CRANKABLE.registerForBlockEntity(ChargerBlockEntity::getCrankable,
                AEBlockEntities.CHARGER.get());
        AECapabilities.CRANKABLE.registerForBlockEntity(InscriberBlockEntity::getCrankable,
                AEBlockEntities.INSCRIBER.get());
        AECapabilities.CRANKABLE.registerForBlockEntity(GrowthAcceleratorBlockEntity::getCrankable,
                AEBlockEntities.GROWTH_ACCELERATOR.get());
    }

    private static void registerPartCapabilities(RegisterPartCapabilitiesEvent event) {
        event.register(ItemStorage.SIDED,
                (part, direction) -> part.getLogic().getBlankPatternInv().toStorage(),
                PatternEncodingTerminalPart.class);
        event.register(AECapabilities.GENERIC_INTERNAL_INV, (part, context) -> part.getLogic().getReturnInv(),
                PatternProviderPart.class);
        event.register(AECapabilities.GENERIC_INTERNAL_INV,
                (part, context) -> part.getInterfaceLogic().getStorage(),
                InterfacePart.class);
        event.register(AECapabilities.ME_STORAGE,
                (part, context) -> part.getInterfaceLogic().getInventory(), InterfacePart.class);

        event.register(ItemStorage.SIDED, (part, context) -> part.getExposedApi(),
                ItemP2PTunnelPart.class);
        event.register(EnergyStorage.SIDED, (part, context) -> part.getExposedApi(),
                FEP2PTunnelPart.class);
        event.register(FluidStorage.SIDED, (part, context) -> part.getExposedApi(),
                FluidP2PTunnelPart.class);

        event.register(EnergyStorage.SIDED, (part, context) -> part.getEnergyAdapter(),
                EnergyAcceptorPart.class);
    }
}
