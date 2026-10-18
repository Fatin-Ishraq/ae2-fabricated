/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleGroupRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.PackType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import appeng.platform.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import appeng.platform.neoforge.client.network.ClientPacketDistributor;


import appeng.api.client.StorageCellModels;
import appeng.api.parts.CableRenderMode;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.util.AEColor;
import appeng.client.api.AEKeyRendering;
import appeng.hooks.MouseWheelScrolled;
import appeng.platform.guide.PageAnchor;
import appeng.platform.neoforge.client.event.ExtractLevelRenderStateEvent;
import appeng.platform.neoforge.client.event.RegisterMenuScreensEvent;
import appeng.platform.neoforge.client.event.SubmitCustomGeometryEvent;
import appeng.platform.neoforge.client.extensions.common.IClientBlockExtensions;
import appeng.platform.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import appeng.client.api.model.parts.CompositePartModel;
import appeng.client.api.model.parts.RegisterPartModelsEvent;
import appeng.client.api.model.parts.StaticPartModel;
import appeng.client.api.renderer.parts.RegisterPartRendererEvent;
import appeng.client.areaoverlay.AreaOverlayRenderer;
import appeng.client.block.cablebus.CableBusBlockClientExtensions;
import appeng.client.block.cablebus.CableBusColor;
import appeng.client.commands.ClientCommands;
import appeng.client.gui.me.common.PendingCraftingJobs;
import appeng.client.gui.me.common.PinnedKeys;
import appeng.client.gui.style.StyleManager;
import appeng.client.hooks.BlockAttackHook;
import appeng.client.hooks.RenderBlockOutlineHook;
import appeng.client.integrations.itemlists.FluidBlockPictureInPictureRenderer;
import appeng.client.item.ColorApplicatorItemModel;
import appeng.client.item.EnergyFillLevelProperty;
import appeng.client.item.PortableCellColorTintSource;
import appeng.client.item.StorageCellStateTintSource;
import appeng.client.model.CableAnchorPartModel;
import appeng.client.model.LevelEmitterPartModel;
import appeng.client.model.LockableMonitorPartModel;
import appeng.client.model.P2PFrequencyPartModel;
import appeng.client.model.PaintSplotchesModel;
import appeng.client.model.PartModels;
import appeng.client.model.PlanePartModel;
import appeng.client.model.QnbFormedModel;
import appeng.client.model.SpatialPylonModel;
import appeng.client.model.StatusIndicatorPartModel;
import appeng.client.render.AEColorItemTintSource;
import appeng.client.render.AERenderPipelines;
import appeng.client.render.ColorableBlockEntityBlockColor;
import appeng.client.render.FacadeItemModel;
import appeng.client.render.StaticBlockColor;
import appeng.client.render.StorageCellClientTooltipComponent;
import appeng.client.render.cablebus.CableBusModel;
import appeng.client.render.crafting.CraftingCubeModel;
import appeng.client.render.effects.CraftingParticle;
import appeng.client.render.effects.EnergyFx;
import appeng.client.render.effects.LightningArcFX;
import appeng.client.render.effects.LightningFX;
import appeng.client.render.effects.LightningFXGroup;
import appeng.client.render.effects.MatterCannonFX;
import appeng.client.render.effects.VibrantFX;
import appeng.client.render.model.DriveModel;
import appeng.client.render.model.MemoryCardItemModel;
import appeng.client.render.model.MeteoriteCompassModel;
import appeng.client.render.model.QuartzGlassModel;
import appeng.client.render.model.SingleSpinnableVariant;
import appeng.client.renderer.blockentity.CableBusRenderer;
import appeng.client.renderer.blockentity.ChargerRenderer;
import appeng.client.renderer.blockentity.CraftingMonitorRenderer;
import appeng.client.renderer.blockentity.CrankRenderer;
import appeng.client.renderer.blockentity.DriveRenderer;
import appeng.client.renderer.blockentity.InscriberRenderer;
import appeng.client.renderer.blockentity.MEChestRenderer;
import appeng.client.renderer.blockentity.MolecularAssemblerRenderer;
import appeng.client.renderer.blockentity.SkyStoneChestModel;
import appeng.client.renderer.blockentity.SkyStoneChestRenderer;
import appeng.client.renderer.blockentity.SkyStoneTankRenderer;
import appeng.client.renderer.entity.TinyTNTPrimedRenderer;
import appeng.client.renderer.keytypes.FluidKeyRenderer;
import appeng.client.renderer.keytypes.ItemKeyRenderer;
import appeng.client.renderer.part.MonitorRenderer;
import appeng.client.renderer.parts.PartRendererDispatcher;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.AppEngBase;
import appeng.core.definitions.AEAttachmentTypes;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEEntities;
import appeng.core.definitions.AEItems;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.serverbound.MouseWheelPacket;
import appeng.core.network.serverbound.UpdateHoldingCtrlPacket;
import appeng.core.particles.ParticleTypes;
import appeng.helpers.IMouseWheelItem;
import appeng.items.storage.StorageCellTooltipComponent;
import appeng.parts.reporting.ConversionMonitorPart;
import appeng.parts.reporting.StorageMonitorPart;
import appeng.spatial.SpatialStorageDimensionIds;
import appeng.util.Platform;

/**
 * Client-specific functionality.
 */
/**
 * FABRIC: Created by {@link AppEngClientStartup}; NeoForge creates it through {@code @Mod(dist = Dist.CLIENT)}.
 */
public class AppEngClient extends AppEngBase {
    private static final Logger LOG = LoggerFactory.getLogger(AppEngClient.class);
    public static final Identifier MODEL_CELL_ITEMS_1K = Identifier.parse(
            "ae2:block/drive_1k_item_cell");
    public static final Identifier MODEL_CELL_ITEMS_4K = Identifier.parse(
            "ae2:block/drive_4k_item_cell");
    public static final Identifier MODEL_CELL_ITEMS_16K = Identifier.parse(
            "ae2:block/drive_16k_item_cell");
    public static final Identifier MODEL_CELL_ITEMS_64K = Identifier.parse(
            "ae2:block/drive_64k_item_cell");
    public static final Identifier MODEL_CELL_ITEMS_256K = Identifier.parse(
            "ae2:block/drive_256k_item_cell");
    public static final Identifier MODEL_CELL_FLUIDS_1K = Identifier.parse(
            "ae2:block/drive_1k_fluid_cell");
    public static final Identifier MODEL_CELL_FLUIDS_4K = Identifier.parse(
            "ae2:block/drive_4k_fluid_cell");
    public static final Identifier MODEL_CELL_FLUIDS_16K = Identifier.parse(
            "ae2:block/drive_16k_fluid_cell");
    public static final Identifier MODEL_CELL_FLUIDS_64K = Identifier.parse(
            "ae2:block/drive_64k_fluid_cell");
    public static final Identifier MODEL_CELL_FLUIDS_256K = Identifier.parse(
            "ae2:block/drive_256k_fluid_cell");
    public static final Identifier MODEL_CELL_CREATIVE = Identifier.parse(
            "ae2:block/drive_creative_cell");

    /**
     * This modifier key has to be held to activate mouse wheel items.
     */
    // FABRIC: no NeoForge key conflict context
    private static final KeyMapping MOUSE_WHEEL_ITEM_MODIFIER = new KeyMapping(
            "key.ae2.mouse_wheel_item_modifier", InputConstants.Type.KEYSYM,
            InputConstants.KEY_LSHIFT, Hotkeys.CATEGORY);

    private static final KeyMapping PART_PLACEMENT_OPPOSITE = new KeyMapping(
            "key.ae2.part_placement_opposite", InputConstants.Type.KEYSYM,
            InputConstants.KEY_LCONTROL, Hotkeys.CATEGORY);

    private static AppEngClient INSTANCE;

    /**
     * Last known cable render mode. Used to update all rendered blocks once at the end of the tick when the mode is
     * changed.
     */
    private CableRenderMode prevCableRenderMode = CableRenderMode.STANDARD;

    private final PartRendererDispatcher partRendererDispatcher = new PartRendererDispatcher();

    private PartModels partModels;

    // Recipes synchronized from the server
    private RecipeMap recipeMap = RecipeMap.EMPTY;
    private final Set<RecipeType<?>> knownRecipeTypes = Collections.newSetFromMap(new IdentityHashMap<>());

    // FABRIC: NeoForge registers these through mod-bus and game-bus events; on Fabric they are registered directly
    // with the matching Fabric API callbacks and registries.
    public AppEngClient() {
        super();

        INSTANCE = this;

        this.registerClientCommands();

        registerClientTooltipComponents();
        registerHotkeys();
        InitScreens.init(new RegisterMenuScreensEvent());
        registerReloadListeners();

        BlockAttackHook.install();

        ClientTickEvents.START_CLIENT_TICK.register(client -> updateCableRenderMode());

        clientSetup();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            PendingCraftingJobs.clearPendingJobs();
            PinnedKeys.clearPinnedKeys();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickPinnedKeys(client);
            Hotkeys.checkHotkeys();
            // FABRIC: Fabric has no raw key event; poll the key instead
            ctrlEvent();
        });

        new AEClientboundPacketHandler().register(new RegisterClientPayloadHandlersEvent());
        registerEntityRenderers();
        registerEntityLayerDefinitions();
        registerClientExtensions();
        registerParticleFactories();
        registerParticleGroups();
        registerBlockStateModels();
        registerStandaloneModels();
        registerItemModelProperties();
        registerItemModels();
        registerItemTintSources();
        registerBlockTintSources();
        registerPipRenderers();

        RenderBlockOutlineHook.install();

        var areaOverlayRenderer = new AreaOverlayRenderer();
        ExtractLevelRenderStateEvent.register(areaOverlayRenderer::extractRenderState);
        SubmitCustomGeometryEvent.register(areaOverlayRenderer::submitCustomGeometry);

        RegisterPartRendererEvent.EVENT.register(this::registerPartRenderers);
        RegisterPartModelsEvent.EVENT.register(this::registerPartModelTypes);
        initCustomClientRegistries();
        ClientRecipeSynchronizedEvent.EVENT.register((client, recipes) -> receiveRecipes(recipes));
    }

    private void registerClientCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> builder = net.fabricmc.fabric.api.client.command.v2.ClientCommands
                    .literal("ae2client");
            if (AEConfig.instance().isDebugToolsEnabled()) {
                for (var commandBuilder : ClientCommands.DEBUG_COMMANDS) {
                    commandBuilder.build(builder);
                }
            }
            dispatcher.register(builder);
        });
    }

    private void tickPinnedKeys(Minecraft minecraft) {
        // Only prune pinned keys when no screen is currently open
        if (minecraft.gui.screen() == null) {
            PinnedKeys.prune();
        }
    }

    @Override
    public Level getClientLevel() {
        return Minecraft.getInstance().level;
    }

    @Override
    public void registerHotkey(String id) {
        Hotkeys.registerHotkey(id);
    }

    private void registerHotkeys() {
        KeyMapping.Category.register(Hotkeys.CATEGORY.id());
        KeyMappingHelper.registerKeyMapping(MOUSE_WHEEL_ITEM_MODIFIER);
        KeyMappingHelper.registerKeyMapping(PART_PLACEMENT_OPPOSITE);
        Hotkeys.finalizeRegistration(KeyMappingHelper::registerKeyMapping);
    }

    public static AppEngClient instance() {
        return Objects.requireNonNull(INSTANCE, "AppEngClient is not initialized");
    }

    private void registerClientTooltipComponents() {
        ClientTooltipComponentCallback.EVENT.register(data -> data instanceof StorageCellTooltipComponent component
                ? new StorageCellClientTooltipComponent(component)
                : null);
    }

    private void clientSetup() {
        try {
            AEKeyRendering.register(AEKeyType.items(), AEItemKey.class, new ItemKeyRenderer());
            AEKeyRendering.register(AEKeyType.fluids(), AEFluidKey.class, new FluidKeyRenderer());
        } catch (Throwable e) {
            LOG.error("AE2 failed postClientSetup", e);
            throw new RuntimeException(e);
        }

        MouseWheelScrolled.EVENT.register(this::wheelEvent);
    }

    private void registerReloadListeners() {
        var loader = ResourceLoader.get(PackType.CLIENT_RESOURCES);
        loader.registerReloadListener(PartRendererDispatcher.ID, partRendererDispatcher);
        // The block entity render for parts needs access to the formed PartRendererDispatcher
        loader.addListenerOrdering(PartRendererDispatcher.ID,
                ResourceReloaderKeys.Client.BLOCK_ENTITY_RENDER_DISPATCHER);

        loader.registerReloadListener(AppEng.makeId("styles"), StyleManager.getReloadListener());
    }

    // FABRIC: called from MouseWheelMixin; returns true to cancel the scroll
    private boolean wheelEvent(double scrollDeltaY) {
        if (scrollDeltaY == 0) {
            return false;
        }

        final Minecraft mc = Minecraft.getInstance();
        final Player player = mc.player;
        if (MOUSE_WHEEL_ITEM_MODIFIER.isDown()) {
            var mainHand = player.getItemInHand(InteractionHand.MAIN_HAND)
                    .getItem() instanceof IMouseWheelItem;
            var offHand = player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof IMouseWheelItem;

            if (mainHand || offHand) {
                ServerboundPacket message = new MouseWheelPacket(scrollDeltaY > 0);
                ClientPacketDistributor.sendToServer(message);
                return true;
            }
        }
        return false;
    }

    private void ctrlEvent() {
        var player = Minecraft.getInstance().player;

        if (player != null) {
            var isDown = PART_PLACEMENT_OPPOSITE.isDown();
            var previousIsDown = player.getAttachedOrElse(AEAttachmentTypes.HOLDING_CTRL, false);
            if (previousIsDown != isDown) {
                player.setAttached(AEAttachmentTypes.HOLDING_CTRL, isDown);
                ClientPacketDistributor.sendToServer(new UpdateHoldingCtrlPacket(isDown));
            }
        }
    }

    @Override
    public HitResult getCurrentMouseOver() {
        return Minecraft.getInstance().hitResult;
    }

    private void updateCableRenderMode() {
        var currentMode = getCableRenderMode();

        // Handle changes to the cable-rendering mode
        if (currentMode == this.prevCableRenderMode) {
            return;
        }

        this.prevCableRenderMode = currentMode;

        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        // Invalidate all sections that contain a cable bus within view distance
        // This should asynchronously update the chunk meshes and as part of that use the new facade render mode
        var viewDistance = (int) Math.ceil(mc.levelExtractor.lastViewDistance());
        ChunkPos.rangeClosed(mc.player.chunkPosition(), viewDistance).forEach(chunkPos -> {
            var chunk = mc.level.getChunkSource().getChunkNow(chunkPos.x(), chunkPos.z());
            if (chunk != null) {
                for (var i = 0; i < chunk.getSectionsCount(); i++) {
                    var section = chunk.getSection(i);
                    if (section.maybeHas(state -> state.is(AEBlocks.CABLE_BUS.block()))) {
                        mc.levelExtractor.setSectionDirty(chunkPos.x(), chunk.getSectionYFromSectionIndex(i),
                                chunkPos.z());
                    }
                }
            }
        });
    }

    @Override
    public CableRenderMode getCableRenderMode() {
        if (Platform.isServer()) {
            return super.getCableRenderMode();
        }

        var mc = Minecraft.getInstance();
        if (mc.player == null) {
            return CableRenderMode.STANDARD;
        }

        return this.getCableRenderModeForPlayer(mc.player);
    }

    /**
     * FABRIC: The guidebook (GuideME) is not available in this port.
     */
    @Override
    public void openGuideAtAnchor(PageAnchor anchor) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.sendSystemMessage(Component.translatable("gui.ae2.fabric.guide_unavailable"));
        }
    }

    @Override
    public void sendSystemMessage(Player player, Component text) {
        if (player == Minecraft.getInstance().player) {
            Minecraft.getInstance().gui.hud.getChat().addServerSystemMessage(text);
        }
        super.sendSystemMessage(player, text);
    }

    @Override
    public RecipeMap getRecipeMapForType(Level level, RecipeType<?> recipeType) {
        if (level instanceof ClientLevel) {
            if (!knownRecipeTypes.contains(recipeType)) {
                LOG.warn("Haven't received recipes of type {} from server yet.", recipeType);
                return RecipeMap.EMPTY;
            }

            return recipeMap;
        }

        return super.getRecipeMapForType(level, recipeType);
    }

    private void registerPartModelTypes(RegisterPartModelsEvent event) {
        event.registerModelType(StaticPartModel.Unbaked.ID, StaticPartModel.Unbaked.MAP_CODEC);
        event.registerModelType(CompositePartModel.Unbaked.ID, CompositePartModel.Unbaked.MAP_CODEC);
        event.registerModelType(P2PFrequencyPartModel.Unbaked.ID, P2PFrequencyPartModel.Unbaked.MAP_CODEC);
        event.registerModelType(StatusIndicatorPartModel.Unbaked.ID, StatusIndicatorPartModel.Unbaked.MAP_CODEC);
        event.registerModelType(LevelEmitterPartModel.Unbaked.ID, LevelEmitterPartModel.Unbaked.MAP_CODEC);
        event.registerModelType(CableAnchorPartModel.Unbaked.ID, CableAnchorPartModel.Unbaked.MAP_CODEC);
        event.registerModelType(LockableMonitorPartModel.Unbaked.ID, LockableMonitorPartModel.Unbaked.MAP_CODEC);
        event.registerModelType(PlanePartModel.Unbaked.ID, PlanePartModel.Unbaked.MAP_CODEC);
    }

    private void initCustomClientRegistries() {
        partModels = new PartModels();

        StorageCellModels.registerModel(AEItems.ITEM_CELL_1K, AppEngClient.MODEL_CELL_ITEMS_1K);
        StorageCellModels.registerModel(AEItems.ITEM_CELL_4K, AppEngClient.MODEL_CELL_ITEMS_4K);
        StorageCellModels.registerModel(AEItems.ITEM_CELL_16K, AppEngClient.MODEL_CELL_ITEMS_16K);
        StorageCellModels.registerModel(AEItems.ITEM_CELL_64K, AppEngClient.MODEL_CELL_ITEMS_64K);
        StorageCellModels.registerModel(AEItems.ITEM_CELL_256K, AppEngClient.MODEL_CELL_ITEMS_256K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL_1K, AppEngClient.MODEL_CELL_FLUIDS_1K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL_4K, AppEngClient.MODEL_CELL_FLUIDS_4K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL_16K, AppEngClient.MODEL_CELL_FLUIDS_16K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL_64K, AppEngClient.MODEL_CELL_FLUIDS_64K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL_256K, AppEngClient.MODEL_CELL_FLUIDS_256K);
        StorageCellModels.registerModel(AEItems.CREATIVE_CELL, AppEngClient.MODEL_CELL_CREATIVE);

        StorageCellModels.registerModel(AEItems.PORTABLE_ITEM_CELL1K, AppEngClient.MODEL_CELL_ITEMS_1K);
        StorageCellModels.registerModel(AEItems.PORTABLE_ITEM_CELL4K, AppEngClient.MODEL_CELL_ITEMS_4K);
        StorageCellModels.registerModel(AEItems.PORTABLE_ITEM_CELL16K, AppEngClient.MODEL_CELL_ITEMS_16K);
        StorageCellModels.registerModel(AEItems.PORTABLE_ITEM_CELL64K, AppEngClient.MODEL_CELL_ITEMS_64K);
        StorageCellModels.registerModel(AEItems.PORTABLE_ITEM_CELL256K, AppEngClient.MODEL_CELL_ITEMS_256K);
        StorageCellModels.registerModel(AEItems.PORTABLE_FLUID_CELL1K, AppEngClient.MODEL_CELL_FLUIDS_1K);
        StorageCellModels.registerModel(AEItems.PORTABLE_FLUID_CELL4K, AppEngClient.MODEL_CELL_FLUIDS_4K);
        StorageCellModels.registerModel(AEItems.PORTABLE_FLUID_CELL16K, AppEngClient.MODEL_CELL_FLUIDS_16K);
        StorageCellModels.registerModel(AEItems.PORTABLE_FLUID_CELL64K, AppEngClient.MODEL_CELL_FLUIDS_64K);
        StorageCellModels.registerModel(AEItems.PORTABLE_FLUID_CELL256K, AppEngClient.MODEL_CELL_FLUIDS_256K);
    }

    private void registerPartRenderers(RegisterPartRendererEvent event) {
        event.register(ConversionMonitorPart.class, new MonitorRenderer());
        event.register(StorageMonitorPart.class, new MonitorRenderer());
    }

    public PartModels getPartModels() {
        if (partModels == null) {
            throw new IllegalStateException("Client registries have not been initialized yet");
        }
        return partModels;
    }

    public PartRendererDispatcher getPartRendererDispatcher() {
        return partRendererDispatcher;
    }

    private void registerBlockStateModels() {
        CustomUnbakedBlockStateModel.register(SingleSpinnableVariant.Unbaked.ID, SingleSpinnableVariant.Unbaked.MAP_CODEC);
        CustomUnbakedBlockStateModel.register(CableBusModel.Unbaked.ID, CableBusModel.Unbaked.MAP_CODEC);
        CustomUnbakedBlockStateModel.register(QuartzGlassModel.Unbaked.ID, QuartzGlassModel.Unbaked.MAP_CODEC);
        CustomUnbakedBlockStateModel.register(AppEng.makeId("drive"), DriveModel.Unbaked.MAP_CODEC);
        CustomUnbakedBlockStateModel.register(AppEng.makeId("spatial_pylon"), SpatialPylonModel.Unbaked.MAP_CODEC);
        CustomUnbakedBlockStateModel.register(AppEng.makeId("paint"), PaintSplotchesModel.Unbaked.MAP_CODEC);
        CustomUnbakedBlockStateModel.register(AppEng.makeId("qnb_formed"), QnbFormedModel.Unbaked.MAP_CODEC);
        CustomUnbakedBlockStateModel.register(CraftingCubeModel.Unbaked.ID, CraftingCubeModel.Unbaked.MAP_CODEC);
    }

    private void registerClientExtensions() {
        IClientBlockExtensions.register(new CableBusBlockClientExtensions(AEBlocks.CABLE_BUS.block()),
                AEBlocks.CABLE_BUS.block());
    }

    private void registerEntityRenderers() {
        EntityRendererRegistry.register(AEEntities.TINY_TNT_PRIMED.get(), TinyTNTPrimedRenderer::new);

        BlockEntityRendererRegistry.register(AEBlockEntities.CRANK.get(), CrankRenderer::new);
        BlockEntityRendererRegistry.register(AEBlockEntities.INSCRIBER.get(), InscriberRenderer::new);
        BlockEntityRendererRegistry.register(AEBlockEntities.SKY_CHEST.get(), SkyStoneChestRenderer::new);
        BlockEntityRendererRegistry.register(AEBlockEntities.CHARGER.get(), ChargerRenderer::new);
        BlockEntityRendererRegistry.register(AEBlockEntities.DRIVE.get(), DriveRenderer::new);
        BlockEntityRendererRegistry.register(AEBlockEntities.ME_CHEST.get(), MEChestRenderer::new);
        BlockEntityRendererRegistry.register(AEBlockEntities.CRAFTING_MONITOR.get(), CraftingMonitorRenderer::new);
        BlockEntityRendererRegistry.register(AEBlockEntities.MOLECULAR_ASSEMBLER.get(), MolecularAssemblerRenderer::new);
        BlockEntityRendererRegistry.register(AEBlockEntities.CABLE_BUS.get(), CableBusRenderer::new);
        BlockEntityRendererRegistry.register(AEBlockEntities.SKY_STONE_TANK.get(), SkyStoneTankRenderer::new);
    }

    private void registerEntityLayerDefinitions() {
        ModelLayerRegistry.registerModelLayer(SkyStoneChestRenderer.MODEL_LAYER,
                SkyStoneChestModel::createSingleBodyLayer);
    }

    public void registerParticleFactories() {
        var registry = ParticleProviderRegistry.getInstance();
        registry.register(ParticleTypes.CRAFTING, CraftingParticle.Factory::new);
        registry.register(ParticleTypes.ENERGY, EnergyFx.Factory::new);
        registry.register(ParticleTypes.LIGHTNING_ARC, LightningArcFX.Factory::new);
        registry.register(ParticleTypes.LIGHTNING, LightningFX.Factory::new);
        registry.register(ParticleTypes.MATTER_CANNON, MatterCannonFX.Factory::new);
        registry.register(ParticleTypes.VIBRANT, VibrantFX.Factory::new);
    }

    public void registerParticleGroups() {
        ParticleGroupRegistry.register(LightningFXGroup.GROUP, LightningFXGroup::new);
    }

    private void registerStandaloneModels() {
        ModelLoadingPlugin.register(context -> {
            context.addModel(CrankRenderer.HANDLE_MODEL.fabricKey(),
                    SimpleUnbakedStandaloneModel.simpleModelWrapper(CrankRenderer.HANDLE_MODEL_ID));

            // For rendering the ME chest we require the original storage cell models as standalone models
            for (var cellModelKey : StorageCellModels.standaloneModels().values()) {
                context.addModel(cellModelKey.fabricKey(),
                        SimpleUnbakedStandaloneModel.blockStateModel(Identifier.parse(cellModelKey.getName())));
            }
            var defaultModel = StorageCellModels.getDefaultStandaloneModel();
            context.addModel(defaultModel.fabricKey(),
                    SimpleUnbakedStandaloneModel.blockStateModel(Identifier.parse(defaultModel.getName())));
        });
    }

    private void registerItemModelProperties() {
        RangeSelectItemModelProperties.ID_MAPPER.put(EnergyFillLevelProperty.ID, EnergyFillLevelProperty.CODEC);
    }

    private void registerItemModels() {
        ItemModels.ID_MAPPER.put(ColorApplicatorItemModel.Unbaked.ID, ColorApplicatorItemModel.Unbaked.MAP_CODEC);
        ItemModels.ID_MAPPER.put(MemoryCardItemModel.Unbaked.ID, MemoryCardItemModel.Unbaked.MAP_CODEC);
        ItemModels.ID_MAPPER.put(FacadeItemModel.Unbaked.ID, FacadeItemModel.Unbaked.MAP_CODEC);
        ItemModels.ID_MAPPER.put(MeteoriteCompassModel.Unbaked.ID, MeteoriteCompassModel.Unbaked.MAP_CODEC);
    }

    // FABRIC: The custom sky, cloud and weather renderers of the spatial storage dimension are not registered, since
    // Fabric 26.x has no API for them. The dimension uses vanilla rendering.

    private void registerItemTintSources() {
        ItemTintSources.ID_MAPPER.put(PortableCellColorTintSource.ID, PortableCellColorTintSource.MAP_CODEC);
        ItemTintSources.ID_MAPPER.put(StorageCellStateTintSource.ID, StorageCellStateTintSource.MAP_CODEC);
        ItemTintSources.ID_MAPPER.put(AEColorItemTintSource.ID, AEColorItemTintSource.MAP_CODEC);
    }

    public void registerBlockTintSources() {
        BlockColorRegistry.register(StaticBlockColor.createTintSources(AEColor.TRANSPARENT),
                AEBlocks.WIRELESS_ACCESS_POINT.block());
        BlockColorRegistry.register(CableBusColor.TINT_SOURCES, AEBlocks.CABLE_BUS.block());
        BlockColorRegistry.register(ColorableBlockEntityBlockColor.TINT_SOURCES, AEBlocks.ME_CHEST.block());
    }

    // FABRIC: Fabric synchronizes recipes by serializer (see AppEngBase#registerSynchronizedRecipes)
    private void receiveRecipes(SynchronizedRecipes recipes) {
        var holders = new java.util.ArrayList<net.minecraft.world.item.crafting.RecipeHolder<?>>();
        knownRecipeTypes.clear();
        for (var recipeType : BuiltInRegistries.RECIPE_TYPE) {
            holders.addAll(recipes.getAllOfType(castRecipeType(recipeType)));
            knownRecipeTypes.add(recipeType);
        }
        recipeMap = RecipeMap.create(holders);
    }

    @SuppressWarnings("unchecked")
    private static <I extends net.minecraft.world.item.crafting.RecipeInput, T extends net.minecraft.world.item.crafting.Recipe<I>> RecipeType<T> castRecipeType(
            RecipeType<?> type) {
        return (RecipeType<T>) type;
    }

    private void registerPipRenderers() {
        PictureInPictureRendererRegistry.register(context -> new FluidBlockPictureInPictureRenderer());
    }
}
