package appeng.playtest;

import appeng.api.util.DimensionalBlockPos;
import appeng.core.areaoverlay.AreaOverlayManager;
import appeng.core.areaoverlay.IAreaOverlayDataSource;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.api.util.AEColor;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.parts.FacadeItem;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.core.AppEng;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.server.testworld.TestWorldGenerator;

/**
 * FABRIC: Scripted client playtest of the port. Builds some of AE2's own test plots in a real client world, takes
 * screenshots of them and opens a few GUIs. Run with {@code ./gradlew runClientGameTest}; screenshots end up in
 * build/run/clientGameTest/screenshots. Not part of the mod jar.
 */
public class Ae2ClientPlaytest implements FabricClientGameTest {
    private static final String[] PLOTS = {
            "fe_controller",
            "subnet",
            "channel_assignment_test",
            "p2p_items",
            "simple_qnb_link",
            "crafting_terminal_fill_grid_moves_misplaced_items",
            "insert_item_into_mechest",
            "drive_deferred_side_effects",
            "energy_overlay",
            "spatial_entity_storage",
    };

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder()
                .adjustSettings(s -> s.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE))
                .create()) {
            var server = singleplayer.getServer();
            server.runCommand("time set day");
            server.runCommand("weather clear");
            context.waitTicks(40);

            // Hand-built showcase: all cable types, parts, facades, a crafting CPU and a drive with cells
            onServer(server, Ae2ClientPlaytest::buildShowcase);
            context.waitTicks(100);
            context.takeScreenshot("showcase_front");
            onServer(server, srv -> lookFrom(srv, 0.5, 4.5, -3.5, 0, 45));
            context.waitTicks(40);
            context.takeScreenshot("showcase_above");
            onServer(server, srv -> lookFrom(srv, 4.5, 1.6, -2.2, 20, 10));
            context.waitTicks(40);
            context.takeScreenshot("showcase_closeup_parts");
            onServer(server, srv -> lookFrom(srv, -3.5, 2.0, -2.0, 0, 30));
            context.waitTicks(40);
            context.takeScreenshot("showcase_closeup_facades");

            // Area overlay (as shown by the spatial anchor), drawn around the showcase chunks
            onServer(server, srv -> lookFrom(srv, 0.5, 20, -40, 0, 25));
            context.waitTicks(40);
            var overlay = context.computeOnClient(Ae2ClientPlaytest::showAreaOverlay);
            context.waitTicks(10);
            context.takeScreenshot("area_overlay");
            context.runOnClient(mc -> AreaOverlayManager.getInstance().removeArea(overlay));

            // Fill the network and look at it through the ME terminal
            onServer(server, Ae2ClientPlaytest::fillShowcaseNetwork);
            var terminalPos = onServer(server, srv -> new BlockPos(0, srv.overworld().getMinY() + 4, 0));
            onServer(server, srv -> lookFrom(srv, 0.5, 0.1, -1.6, 0, 0));
            context.waitTicks(20);
            context.getInput().lookAt(terminalPos);
            context.waitTicks(5);
            context.getInput().pressMouse(com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_RIGHT);
            context.waitTicks(40);
            context.takeScreenshot("gui_terminal_with_items");
            context.setScreen(() -> null);
            context.waitTicks(5);

            for (var plot : PLOTS) {
                var startPos = onServer(server, srv -> buildPlot(srv, plot));
                context.waitTicks(100);
                context.takeScreenshot("plot_" + plot);

                if (plot.startsWith("crafting_terminal")) {
                    openGuiAt(context, server, startPos, be -> be instanceof CableBusBlockEntity cb
                            && hasCraftingTerminal(cb), "gui_crafting_terminal");
                } else if (plot.equals("insert_item_into_mechest")) {
                    openGuiAt(context, server, startPos, be -> be instanceof MEChestBlockEntity, "gui_me_chest");
                } else if (plot.equals("drive_deferred_side_effects")) {
                    openGuiAt(context, server, startPos, be -> be instanceof DriveBlockEntity, "gui_drive");
                }
            }
        }
    }

    private static IAreaOverlayDataSource showAreaOverlay(Minecraft minecraft) {
        var level = minecraft.level;
        var pos = new BlockPos(0, level.getMinY() + 4, 0);
        var source = new IAreaOverlayDataSource() {
            @Override
            public Set<ChunkPos> getOverlayChunks() {
                return Set.of(new ChunkPos(0, 0), new ChunkPos(-1, 0), new ChunkPos(0, -1), new ChunkPos(-1, -1),
                        new ChunkPos(1, 0));
            }

            @Override
            public BlockEntity getOverlayBlockEntity() {
                return level.getBlockEntity(pos);
            }

            @Override
            public DimensionalBlockPos getOverlaySourceLocation() {
                return new DimensionalBlockPos(level, pos);
            }

            @Override
            public int getOverlayColor() {
                return 0x80FF6020;
            }
        };
        AreaOverlayManager.getInstance().showArea(source);
        return source;
    }

    private static Object lookFrom(MinecraftServer server, double x, double dy, double z, float yaw, float pitch) {
        var level = server.overworld();
        var player = player(server);
        player.teleportTo(level, x, level.getMinY() + 4 + dy, z, Set.of(), yaw, pitch, true);
        return Boolean.TRUE;
    }

    private static Object buildShowcase(MinecraftServer server) {
        var level = server.overworld();
        int y = level.getMinY() + 4;
        for (int x = -10; x <= 10; x++) {
            for (int z = -8; z <= 8; z++) {
                level.setBlockAndUpdate(new BlockPos(x, y - 1, z), Blocks.SMOOTH_STONE.defaultBlockState());
                for (int dy = 0; dy < 6; dy++) {
                    level.setBlockAndUpdate(new BlockPos(x, y + dy, z), Blocks.AIR.defaultBlockState());
                }
            }
        }
        level.setBlockAndUpdate(new BlockPos(-7, y, 0), AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(-6, y, 0), AEBlocks.CONTROLLER.block().defaultBlockState());

        // Cables: glass (fluix), covered (red), smart (blue), dense smart (fluix), dense covered (green)
        for (int x = -5; x <= 7; x++) {
            var pos = new BlockPos(x, y, 0);
            IPartItem<?> cable;
            if (x <= -3 || x == -1) {
                // Fluix (uncolored) cables connect to every color; differently colored cables do not connect
                cable = AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT);
            } else if (x <= -2) {
                cable = AEParts.COVERED_CABLE.item(AEColor.RED);
            } else if (x <= 2) {
                cable = AEParts.SMART_CABLE.item(AEColor.BLUE);
            } else if (x <= 5) {
                cable = AEParts.SMART_DENSE_CABLE.item(AEColor.TRANSPARENT);
            } else {
                cable = AEParts.COVERED_DENSE_CABLE.item(AEColor.GREEN);
            }
            PartHelper.setPart(level, pos, null, null, cable);
        }
        PartHelper.setPart(level, new BlockPos(0, y, 0), Direction.NORTH, null, AEParts.TERMINAL.get());
        PartHelper.setPart(level, new BlockPos(1, y, 0), Direction.NORTH, null, AEParts.CRAFTING_TERMINAL.get());
        PartHelper.setPart(level, new BlockPos(2, y, 0), Direction.UP, null, AEParts.LEVEL_EMITTER.get());
        PartHelper.setPart(level, new BlockPos(-1, y, 0), Direction.NORTH, null, AEParts.IMPORT_BUS.get());
        PartHelper.setPart(level, new BlockPos(-2, y, 0), Direction.NORTH, null, AEParts.STORAGE_BUS.get());
        PartHelper.setPart(level, new BlockPos(4, y, 0), Direction.NORTH, null, AEParts.ANNIHILATION_PLANE.get());
        PartHelper.setPart(level, new BlockPos(3, y, 0), Direction.NORTH, null, AEParts.ME_P2P_TUNNEL.get());

        // Facades
        addFacade(level, new BlockPos(-4, y, 0), Direction.UP, new ItemStack(Blocks.STONE_BRICKS));
        addFacade(level, new BlockPos(-3, y, 0), Direction.NORTH, new ItemStack(Blocks.GLASS));
        addFacade(level, new BlockPos(-5, y, 0), Direction.NORTH, new ItemStack(Blocks.OAK_PLANKS));

        // Drive with cells on top of the controller
        level.setBlockAndUpdate(new BlockPos(-6, y + 1, 0), AEBlocks.DRIVE.block().defaultBlockState());
        if (level.getBlockEntity(new BlockPos(-6, y + 1, 0)) instanceof DriveBlockEntity drive) {
            drive.getInternalInventory().setItemDirect(0, AEItems.ITEM_CELL_1K.stack());
            drive.getInternalInventory().setItemDirect(1, AEItems.FLUID_CELL_4K.stack());
        }

        // 2x2x2 crafting CPU behind the dense cable
        for (int dx = 0; dx < 2; dx++) {
            for (int dy = 0; dy < 2; dy++) {
                for (int dz = 1; dz < 3; dz++) {
                    var block = dx == 0 && dy == 1 ? AEBlocks.CRAFTING_MONITOR.block()
                            : dx == 1 && dy == 0 ? AEBlocks.CRAFTING_STORAGE_1K.block()
                                    : AEBlocks.CRAFTING_UNIT.block();
                    level.setBlockAndUpdate(new BlockPos(4 + dx, y + dy, dz), block.defaultBlockState());
                }
            }
        }
        lookFrom(server, 0.5, 1.6, -4.5, 0, 15);
        var player = player(server);
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        return Boolean.TRUE;
    }

    private static Object fillShowcaseNetwork(MinecraftServer server) {
        var level = server.overworld();
        var node = appeng.api.networking.GridHelper.getNodeHost(level, new BlockPos(0, level.getMinY() + 4, 0));
        if (node == null) {
            return Boolean.FALSE;
        }
        var gridNode = node.getGridNode(null);
        if (gridNode == null) {
            return Boolean.FALSE;
        }
        var storage = gridNode.getGrid().getStorageService().getInventory();
        var source = new appeng.me.helpers.BaseActionSource();
        storage.insert(appeng.api.stacks.AEItemKey.of(net.minecraft.world.item.Items.DIAMOND), 640,
                appeng.api.config.Actionable.MODULATE, source);
        storage.insert(appeng.api.stacks.AEItemKey.of(net.minecraft.world.item.Items.IRON_INGOT), 1234,
                appeng.api.config.Actionable.MODULATE, source);
        storage.insert(appeng.api.stacks.AEItemKey.of(net.minecraft.world.item.Items.REDSTONE), 77,
                appeng.api.config.Actionable.MODULATE, source);
        storage.insert(appeng.api.stacks.AEItemKey.of(AEItems.CERTUS_QUARTZ_CRYSTAL.asItem()), 12,
                appeng.api.config.Actionable.MODULATE, source);
        storage.insert(appeng.api.stacks.AEFluidKey.of(net.minecraft.world.level.material.Fluids.WATER),
                5L * appeng.api.stacks.AEFluidKey.AMOUNT_BUCKET, appeng.api.config.Actionable.MODULATE, source);
        storage.insert(appeng.api.stacks.AEFluidKey.of(net.minecraft.world.level.material.Fluids.LAVA),
                2L * appeng.api.stacks.AEFluidKey.AMOUNT_BUCKET, appeng.api.config.Actionable.MODULATE, source);
        return Boolean.TRUE;
    }

    private static void addFacade(ServerLevel level, BlockPos pos, Direction side, ItemStack textureItem) {
        var facadeItem = AEItems.FACADE.get();
        var facadeStack = facadeItem.createFacadeForItem(textureItem, false);
        var facade = FacadeItem.createFacade(facadeStack, side);
        if (facade != null && level.getBlockEntity(pos) instanceof IPartHost host) {
            host.getFacadeContainer().addFacade(facade);
            host.markForUpdate();
        }
    }

    private static boolean hasCraftingTerminal(CableBusBlockEntity cableBus) {
        for (var side : net.minecraft.core.Direction.values()) {
            if (cableBus.getPart(side) instanceof CraftingTerminalPart) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds a plot around the world origin and moves the player to look at it from above.
     */
    private static BlockPos buildPlot(MinecraftServer server, String plot) {
        var level = server.overworld();
        var player = player(server);
        var origin = new BlockPos(0, level.getMinY() + 4, 0);
        var generator = new TestWorldGenerator(level, player, origin, AppEng.makeId(plot));
        generator.generate();
        var start = generator.getSuitableStartPos();
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.teleportTo(level, start.getX() + 0.5, start.getY() + 5, start.getZ() - 3, Set.of(), 0, 35, true);
        return origin;
    }

    /**
     * Finds a block entity near the plot, moves the player next to it, right-clicks it and screenshots the GUI.
     */
    private static void openGuiAt(ClientGameTestContext context, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
            BlockPos origin, Predicate<BlockEntity> filter, String screenshot) {
        var target = onServer(server, srv -> {
            var level = srv.overworld();
            for (var pos : BlockPos.betweenClosed(origin.offset(-2, -2, -2), origin.offset(40, 8, 40))) {
                var be = level.getBlockEntity(pos);
                if (be != null && filter.test(be)) {
                    var player = player(srv);
                    player.teleportTo(level, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() - 1.6, Set.of(), 0, 0,
                            true);
                    return pos.immutable();
                }
            }
            return null;
        });
        if (target == null) {
            context.takeScreenshot(screenshot + "_not_found");
            return;
        }
        context.waitTicks(20);
        context.getInput().lookAt(target);
        context.waitTicks(5);
        context.getInput().pressMouse(com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_RIGHT);
        context.waitTicks(40);
        context.takeScreenshot(screenshot);
        context.setScreen(() -> null);
        context.waitTicks(5);
    }

    private static ServerPlayer player(MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }

    @Nullable
    private static <T> T onServer(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
            java.util.function.Function<MinecraftServer, T> action) {
        var result = new AtomicReference<T>();
        server.waitFor(srv -> {
            result.set(action.apply(srv));
            return true;
        });
        return result.get();
    }
}
