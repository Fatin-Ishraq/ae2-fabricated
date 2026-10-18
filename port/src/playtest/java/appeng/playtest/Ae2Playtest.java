package appeng.playtest;

import java.io.File;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.mojang.blaze3d.platform.InputConstants;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.Vec3;

import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.client.render.overlay.IOverlayDataSource;
import appeng.client.render.overlay.OverlayManager;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.parts.FacadeItem;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.server.testworld.TestWorldGenerator;

/**
 * FABRIC: Scripted client playtest of the 1.21.1 port, the same script as the 26.x ports' client game test. Fabric API
 * for 1.21.1 has no client game test API, so this mod drives the client itself from a script thread. Creates a
 * creative flat world, builds a showcase network and some of AE2's own test plots, takes screenshots and opens a few
 * GUIs, then quits.
 * <p>
 * Run with {@code ./gradlew runPlaytest}; screenshots end up in build/playtest/screenshots. Not part of the mod jar.
 */
public class Ae2Playtest implements ClientModInitializer {
    private static final Logger LOG = LoggerFactory.getLogger("AE2 playtest");

    private static final String[] PLOTS = {
            "fe_controller",
            "subnet",
            "channel_assignment_test",
            "p2p_items",
            "simple_qnb_link",
            "crafting_terminal_fill_grid_moves_misplaced_items",
            "insert_item_into_mechest",
            "energy_overlay",
            "spatial_entity_storage",
    };

    private static final AtomicInteger TICKS = new AtomicInteger();
    private static int screenshotIndex;

    @Override
    public void onInitializeClient() {
        if (!Boolean.getBoolean("ae2.playtest")) {
            return;
        }
        ClientTickEvents.END_CLIENT_TICK.register(mc -> TICKS.incrementAndGet());
        var thread = new Thread(() -> {
            try {
                run();
                LOG.info("Playtest finished");
            } catch (Throwable t) {
                LOG.error("Playtest failed", t);
            } finally {
                onClient(mc -> {
                    mc.stop();
                    return null;
                });
            }
        }, "AE2 playtest");
        thread.setDaemon(true);
        thread.start();
    }

    private static void run() {
        // Wait for the game to finish loading
        waitFor(() -> onClient(mc -> mc.getOverlay() == null && mc.screen != null));
        onClient(mc -> {
            mc.options.pauseOnLostFocus = false;
            var settings = new LevelSettings("playtest", GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                    new GameRules(), WorldDataConfiguration.DEFAULT);
            mc.createWorldOpenFlows().createFreshLevel("playtest-" + System.currentTimeMillis(), settings,
                    new WorldOptions(0, false, false),
                    registries -> registries.registryOrThrow(Registries.WORLD_PRESET)
                            .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                    new TitleScreen());
            return null;
        });
        waitFor(() -> onClient(mc -> mc.player != null && mc.level != null && mc.screen == null
                && mc.getSingleplayerServer() != null
                && !mc.getSingleplayerServer().getPlayerList().getPlayers().isEmpty()));
        command("time set day");
        command("weather clear");
        waitTicks(40);

        // Hand-built showcase: all cable types, parts, facades, a crafting CPU and a drive with cells
        onServer(Ae2Playtest::buildShowcase);
        waitTicks(100);
        if (step("showcase")) {
            showcaseScreenshots();
        }
        if (step("area_overlay")) {
            areaOverlay();
        }
        if (step("terminal")) {
            terminal();
        }
        if (step("drive")) {
            drive();
        }
        for (var plot : PLOTS) {
            if (step("plots") || step(plot)) {
                plot(plot);
            }
        }
    }

    private static void showcaseScreenshots() {
        screenshot("showcase_front");
        onServer(srv -> lookFrom(srv, 0.5, 4.5, -3.5, 0, 45));
        waitTicks(40);
        screenshot("showcase_above");
        onServer(srv -> lookFrom(srv, 4.5, 1.6, -2.2, 20, 10));
        waitTicks(40);
        screenshot("showcase_closeup_parts");
        onServer(srv -> lookFrom(srv, -3.5, 2.0, -2.0, 0, 30));
        waitTicks(40);
        screenshot("showcase_closeup_facades");
    }

    /**
     * Area overlay (as shown by the spatial anchor), drawn around the showcase chunks.
     */
    private static void areaOverlay() {
        onServer(srv -> lookFrom(srv, 0.5, 20, -40, 0, 25));
        waitTicks(40);
        var overlay = onClient(Ae2Playtest::showAreaOverlay);
        waitTicks(10);
        screenshot("area_overlay");
        onClient(mc -> {
            OverlayManager.getInstance().removeHandlers(overlay);
            return null;
        });
        waitTicks(10);
        screenshot("area_overlay_removed");
    }

    /**
     * Fills the network and looks at it through the ME terminal.
     */
    private static void terminal() {
        onServer(Ae2Playtest::fillShowcaseNetwork);
        var terminalPos = onServer(srv -> new BlockPos(0, srv.overworld().getMinBuildHeight() + 4, 0));
        onServer(srv -> lookFrom(srv, 0.5, 0.1, -1.6, 0, 0));
        waitTicks(20);
        rightClick(terminalPos);
        screenshot("gui_terminal_with_items");
        closeScreen();
    }

    /**
     * The drive on top of the controller (upstream 1.21.1 has no drive test plot).
     */
    private static void drive() {
        var drivePos = onServer(srv -> new BlockPos(-6, srv.overworld().getMinBuildHeight() + 5, 0));
        onServer(srv -> lookFrom(srv, -5.5, 1.1, -1.6, 0, 0));
        waitTicks(20);
        rightClick(drivePos);
        screenshot("gui_drive");
        closeScreen();
    }

    private static void plot(String plot) {
        var startPos = onServer(srv -> buildPlot(srv, plot));
        waitTicks(100);
        screenshot("plot_" + plot);

        if (plot.startsWith("crafting_terminal")) {
            openGuiAt(startPos, be -> be instanceof CableBusBlockEntity cb && hasCraftingTerminal(cb),
                    "gui_crafting_terminal");
        } else if (plot.equals("insert_item_into_mechest")) {
            openGuiAt(startPos, be -> be instanceof MEChestBlockEntity, "gui_me_chest");
        }
    }

    /**
     * Steps to run, from {@code -PplaytestSteps=showcase,area_overlay,terminal,drive,plots,<plot id>}. All by default.
     */
    private static boolean step(String name) {
        var steps = System.getProperty("ae2.playtest.steps", "").trim();
        return steps.isEmpty() || Set.of(steps.split(",")).contains(name);
    }

    private static IOverlayDataSource showAreaOverlay(Minecraft minecraft) {
        var level = minecraft.level;
        var pos = new BlockPos(0, level.getMinBuildHeight() + 4, 0);
        var source = new IOverlayDataSource() {
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
        OverlayManager.getInstance().showArea(source);
        return source;
    }

    private static Object lookFrom(MinecraftServer server, double x, double dy, double z, float yaw, float pitch) {
        var level = server.overworld();
        var player = player(server);
        player.teleportTo(level, x, level.getMinBuildHeight() + 4 + dy, z, Set.of(), yaw, pitch);
        return Boolean.TRUE;
    }

    private static Object buildShowcase(MinecraftServer server) {
        var level = server.overworld();
        int y = level.getMinBuildHeight() + 4;
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
        var node = appeng.api.networking.GridHelper.getNodeHost(level,
                new BlockPos(0, level.getMinBuildHeight() + 4, 0));
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
        for (var side : Direction.values()) {
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
        var origin = new BlockPos(0, level.getMinBuildHeight() + 4, 0);
        var generator = new TestWorldGenerator(level, player, origin, AppEng.makeId(plot));
        generator.generate();
        var start = generator.getSuitableStartPos();
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.teleportTo(level, start.getX() + 0.5, start.getY() + 5, start.getZ() - 3, Set.of(), 0, 35);
        return origin;
    }

    /**
     * Finds a block entity near the plot, moves the player next to it, right-clicks it and screenshots the GUI.
     */
    private static void openGuiAt(BlockPos origin, Predicate<BlockEntity> filter, String screenshot) {
        var target = onServer(srv -> {
            var level = srv.overworld();
            for (var pos : BlockPos.betweenClosed(origin.offset(-2, -2, -2), origin.offset(40, 8, 40))) {
                var be = level.getBlockEntity(pos);
                if (be != null && filter.test(be)) {
                    var player = player(srv);
                    player.teleportTo(level, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() - 1.6, Set.of(), 0, 0);
                    return pos.immutable();
                }
            }
            return null;
        });
        if (target == null) {
            screenshot(screenshot + "_not_found");
            return;
        }
        waitTicks(20);
        rightClick(target);
        screenshot(screenshot);
        closeScreen();
    }

    /**
     * Turns the client player to face the block and presses the use key, like a player would.
     */
    private static void rightClick(BlockPos pos) {
        onClient(mc -> {
            mc.player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(pos));
            return null;
        });
        waitTicks(5);
        onClient(mc -> {
            KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_RIGHT));
            return null;
        });
        waitTicks(40);
    }

    private static void closeScreen() {
        onClient(mc -> {
            if (mc.player != null && mc.screen != null) {
                mc.player.closeContainer();
            }
            mc.setScreen(null);
            return null;
        });
        waitTicks(5);
    }

    private static void command(String command) {
        onServer(srv -> {
            srv.getCommands().performPrefixedCommand(srv.createCommandSourceStack(), command);
            return null;
        });
    }

    private static void screenshot(String name) {
        var fileName = String.format("%04d_%s.png", screenshotIndex++, name);
        onClient(mc -> {
            Screenshot.grab(new File(mc.gameDirectory.getAbsolutePath()), fileName, mc.getMainRenderTarget(),
                    message -> LOG.info("{}", message.getString()));
            return null;
        });
        waitTicks(2);
    }

    private static ServerPlayer player(MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }

    private static void waitTicks(int ticks) {
        int target = TICKS.get() + ticks;
        waitFor(() -> TICKS.get() >= target);
    }

    private static void waitFor(Supplier<Boolean> condition) {
        long deadline = System.currentTimeMillis() + 300_000;
        while (!condition.get()) {
            if (System.currentTimeMillis() > deadline) {
                throw new IllegalStateException("Timed out waiting for the game");
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static <T> T onClient(Function<Minecraft, T> action) {
        var mc = Minecraft.getInstance();
        return mc.submit(() -> action.apply(mc)).join();
    }

    private static <T> T onServer(Function<MinecraftServer, T> action) {
        var server = onClient(Minecraft::getSingleplayerServer);
        return server.submit(() -> action.apply(server)).join();
    }
}
