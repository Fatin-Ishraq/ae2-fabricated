package appeng.playtest;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.TerminalStyle;
import appeng.api.networking.GridHelper;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.StorageCells;
import appeng.api.util.AEColor;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.parts.FacadeItem;
import appeng.parts.reporting.AbstractTerminalPart;

/**
 * FABRIC: Takes the screenshots for the Modrinth and CurseForge listings: staged scenes, 1920x1080, HUD hidden. Run
 * with {@code ./gradlew runClientGameTest -PplaytestSuite=screenshots} (optionally {@code -PscreenshotScenes=base,cables}).
 * Screenshots end up in build/run/clientGameTest/screenshots. Not part of the mod jar.
 */
public class Ae2Screenshots implements FabricClientGameTest {
    private static final int SPACING = 64;

    @Override
    public void runTest(ClientGameTestContext context) {
        if (!"screenshots".equals(System.getProperty("ae2.playtest.suite"))) {
            return;
        }
        context.getInput().resizeWindow(1920, 1080);
        try (TestSingleplayerContext singleplayer = context.worldBuilder()
                .adjustSettings(s -> s.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE))
                .create()) {
            var server = singleplayer.getServer();
            server.runCommand("time set noon");
            server.runCommand("gamerule advance_time false");
            server.runCommand("weather clear");
            server.runCommand("gamerule advance_weather false");
            context.runOnClient(mc -> {
                // Same as pressing F1
                if (!mc.gui.hud.isHidden()) {
                    mc.gui.hud.toggle();
                }
            });
            context.waitTicks(20);

            if (scene("base")) {
                onServer(server, srv -> buildBase(srv, origin(srv, 0)));
                shot(context, server, origin(server, 0), 0.0, 2.2, -2.4, 0, 8, "1_me_base");
            }
            if (scene("terminal")) {
                var o = scene("base") ? origin(server, 0) : onServer(server, srv -> buildBase(srv, origin(srv, 0)));
                context.runOnClient(mc -> AEConfig.instance().setTerminalStyle(TerminalStyle.TALL));
                onServer(server, srv -> {
                    if (srv.overworld().getBlockEntity(o.offset(1, 1, 3)) instanceof IPartHost host
                            && host.getPart(Direction.NORTH) instanceof AbstractTerminalPart terminal) {
                        terminal.getConfigManager().putSetting(Settings.SORT_BY, SortOrder.AMOUNT);
                        terminal.getConfigManager().putSetting(Settings.SORT_DIRECTION, SortDir.DESCENDING);
                    }
                    return Boolean.TRUE;
                });
                lookFrom(context, server, o, 1.5, 1.6, 0.8, 0, 0);
                context.waitTicks(40);
                rightClick(context, o.offset(1, 1, 3));
                screenshot(context, "2_terminal");
                context.setScreen(() -> null);
                context.waitTicks(5);
            }
            if (scene("autocrafting")) {
                onServer(server, srv -> buildAutocrafting(srv, origin(srv, 1)));
                shot(context, server, origin(server, 1), -4.5, 4.2, -3.5, -30, 28, "3_autocrafting");
            }
            if (scene("cables")) {
                onServer(server, srv -> buildCables(srv, origin(srv, 2)));
                shot(context, server, origin(server, 2), 7.5, 6.5, -5.0, 0, 42, "4_cables");
            }
            if (scene("facades")) {
                onServer(server, srv -> buildFacades(srv, origin(srv, 3)));
                shot(context, server, origin(server, 3), -0.5, 1.5, -2.9, 0, 12, "5_parts_and_facades");
            }
            if (scene("qnb")) {
                onServer(server, srv -> buildQnb(srv, origin(srv, 4)));
                shot(context, server, origin(server, 4), -1.5, 2.6, -2.5, -15, 10, "6_quantum_bridge");
            }
            if (scene("spatial")) {
                onServer(server, srv -> buildSpatial(srv, origin(srv, 5)));
                shot(context, server, origin(server, 5), -2.5, 5.5, -7.0, -20, 25, "7_spatial_storage");
            }
        }
    }

    private static boolean scene(String name) {
        var scenes = System.getProperty("ae2.screenshots.scenes", "").trim();
        return scenes.isEmpty() || Set.of(scenes.split(",")).contains(name);
    }

    private static BlockPos origin(MinecraftServer server, int index) {
        return new BlockPos(index * SPACING, server.overworld().getMinY() + 4, 0);
    }

    private static BlockPos origin(TestServerContext server, int index) {
        return onServer(server, srv -> origin(srv, index));
    }

    // Scenes. Each one is built around its origin, facing north (towards the camera at negative z).

    /**
     * Controllers, a wall of drives with cells, and a panel of terminals.
     */
    private static BlockPos buildBase(MinecraftServer server, BlockPos o) {
        var level = server.overworld();
        platform(level, o, -7, 7, -3, 6);
        // Hidden power source under the controller
        set(level, o.offset(-5, -1, 3), AEBlocks.CREATIVE_ENERGY_CELL.block());
        for (int y = 0; y < 3; y++) {
            for (int x = -5; x <= -4; x++) {
                set(level, o.offset(x, y, 3), AEBlocks.CONTROLLER.block());
            }
        }
        // 4x3 wall of drives
        int cell = 0;
        for (int y = 0; y < 3; y++) {
            for (int x = -3; x <= 0; x++) {
                var pos = o.offset(x, y, 3);
                set(level, pos, AEBlocks.DRIVE.block());
                if (level.getBlockEntity(pos) instanceof DriveBlockEntity drive) {
                    for (int slot = 0; slot < 10; slot++) {
                        drive.getInternalInventory().setItemDirect(slot, filledCell(cell++));
                    }
                }
            }
        }
        // Terminal panel: cable buses in front of a sky stone wall, connected to the drives
        for (int y = 0; y < 3; y++) {
            for (int x = 1; x <= 4; x++) {
                set(level, o.offset(x, y, 4), AEBlocks.SMOOTH_SKY_STONE_BLOCK.block());
            }
        }
        IPartItem<?>[] panel = { AEParts.TERMINAL.get(), AEParts.CRAFTING_TERMINAL.get(),
                AEParts.PATTERN_ENCODING_TERMINAL.get(), AEParts.PATTERN_ACCESS_TERMINAL.get() };
        for (int x = 1; x <= 4; x++) {
            for (int y = 0; y < 3; y++) {
                var pos = o.offset(x, y, 3);
                PartHelper.setPart(level, pos, null, null, AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT));
                if (y == 1) {
                    PartHelper.setPart(level, pos, Direction.NORTH, null, panel[x - 1]);
                } else {
                    IPartItem<?> monitor = y == 2 ? AEParts.DARK_MONITOR.get() : AEParts.STORAGE_MONITOR.get();
                    PartHelper.setPart(level, pos, Direction.NORTH, null, monitor);
                }
            }
        }
        // ME chest, energy cells and a crafting unit stack on the right
        set(level, o.offset(5, 0, 3), AEBlocks.ME_CHEST.block());
        set(level, o.offset(6, 0, 3), AEBlocks.DENSE_ENERGY_CELL.block());
        set(level, o.offset(5, 1, 3), AEBlocks.ENERGY_CELL.block());
        set(level, o.offset(-6, 0, 3), AEBlocks.INSCRIBER.block());
        set(level, o.offset(-6, 0, 2), AEBlocks.CHARGER.block());
        return o;
    }

    /**
     * A formed crafting CPU, pattern providers with molecular assemblers, and a crafting monitor.
     */
    private static BlockPos buildAutocrafting(MinecraftServer server, BlockPos o) {
        var level = server.overworld();
        platform(level, o, -7, 7, -3, 8);
        set(level, o.offset(0, -1, 5), AEBlocks.CREATIVE_ENERGY_CELL.block());
        set(level, o.offset(0, 0, 6), AEBlocks.CONTROLLER.block());
        // 3x3x3 crafting CPU in the middle, monitor facing the camera
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 3; z <= 5; z++) {
                    Block block;
                    if (x == 0 && y == 1 && z == 3) {
                        block = AEBlocks.CRAFTING_MONITOR.block();
                    } else if (y == 1) {
                        block = AEBlocks.CRAFTING_STORAGE_64K.block();
                    } else if ((x + z) % 2 == 0) {
                        block = AEBlocks.CRAFTING_ACCELERATOR.block();
                    } else {
                        block = AEBlocks.CRAFTING_UNIT.block();
                    }
                    set(level, o.offset(x, y, z), block);
                }
            }
        }
        // Pattern providers surrounded by molecular assemblers on both sides, cabled to the CPU
        for (int side : new int[] { -5, 5 }) {
            var provider = o.offset(side, 1, 4);
            var outward = side < 0 ? Direction.WEST : Direction.EAST;
            set(level, provider, AEBlocks.PATTERN_PROVIDER.block());
            for (var dir : new Direction[] { Direction.UP, Direction.DOWN, outward, Direction.NORTH }) {
                set(level, provider.relative(dir), AEBlocks.MOLECULAR_ASSEMBLER.block());
            }
            int inward = side < 0 ? 1 : -1;
            for (int x = side + inward; Math.abs(x) >= 2; x += inward) {
                PartHelper.setPart(level, o.offset(x, 1, 4), null, null, AEParts.SMART_CABLE.item(AEColor.CYAN));
            }
        }
        return o;
    }

    /**
     * A rainbow of smart cables carrying channels from a dense cable trunk to a row of interfaces.
     */
    private static BlockPos buildCables(MinecraftServer server, BlockPos o) {
        var level = server.overworld();
        platform(level, o, -3, 18, -3, 9);
        set(level, o.offset(-1, -1, 0), AEBlocks.CREATIVE_ENERGY_CELL.block());
        set(level, o.offset(-1, 0, 0), AEBlocks.CONTROLLER.block());
        var colors = AEColor.VALID_COLORS;
        for (int x = 0; x < colors.size(); x++) {
            PartHelper.setPart(level, o.offset(x, 0, 0), null, null,
                    AEParts.SMART_DENSE_CABLE.item(AEColor.TRANSPARENT));
            for (int z = 1; z <= 5; z++) {
                PartHelper.setPart(level, o.offset(x, 0, z), null, null, AEParts.SMART_CABLE.item(colors.get(x)));
            }
            set(level, o.offset(x, 0, 6), AEBlocks.INTERFACE.block());
        }
        return o;
    }

    /**
     * Cable types and colors, parts and facades (stone, glass and oak).
     */
    private static BlockPos buildFacades(MinecraftServer server, BlockPos o) {
        var level = server.overworld();
        platform(level, o, -8, 9, -3, 5);
        set(level, o.offset(-7, -1, 0), AEBlocks.CREATIVE_ENERGY_CELL.block());
        set(level, o.offset(-7, 0, 0), AEBlocks.CONTROLLER.block());
        for (int x = -6; x <= 7; x++) {
            IPartItem<?> cable;
            if (x <= -4) {
                cable = AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT);
            } else if (x <= -1) {
                cable = AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT);
            } else if (x <= 2) {
                cable = AEParts.SMART_CABLE.item(AEColor.TRANSPARENT);
            } else if (x <= 5) {
                cable = AEParts.SMART_DENSE_CABLE.item(AEColor.TRANSPARENT);
            } else {
                cable = AEParts.COVERED_DENSE_CABLE.item(AEColor.TRANSPARENT);
            }
            PartHelper.setPart(level, o.offset(x, 0, 0), null, null, cable);
        }
        PartHelper.setPart(level, o.offset(-5, 0, 0), Direction.NORTH, null, AEParts.IMPORT_BUS.get());
        PartHelper.setPart(level, o.offset(-3, 0, 0), Direction.NORTH, null, AEParts.EXPORT_BUS.get());
        PartHelper.setPart(level, o.offset(-2, 0, 0), Direction.NORTH, null, AEParts.STORAGE_BUS.get());
        PartHelper.setPart(level, o.offset(0, 0, 0), Direction.UP, null, AEParts.LEVEL_EMITTER.get());
        PartHelper.setPart(level, o.offset(1, 0, 0), Direction.NORTH, null, AEParts.TERMINAL.get());
        PartHelper.setPart(level, o.offset(2, 0, 0), Direction.NORTH, null, AEParts.ANNIHILATION_PLANE.get());
        PartHelper.setPart(level, o.offset(4, 0, 0), Direction.NORTH, null, AEParts.ME_P2P_TUNNEL.get());
        PartHelper.setPart(level, o.offset(6, 0, 0), Direction.NORTH, null, AEParts.FORMATION_PLANE.get());
        addFacade(level, o.offset(-6, 0, 0), Direction.NORTH, new ItemStack(Blocks.OAK_PLANKS));
        addFacade(level, o.offset(-4, 0, 0), Direction.NORTH, new ItemStack(Blocks.GLASS));
        addFacade(level, o.offset(-1, 0, 0), Direction.NORTH, new ItemStack(Blocks.STONE_BRICKS));
        addFacade(level, o.offset(7, 0, 0), Direction.NORTH, new ItemStack(Blocks.QUARTZ_BLOCK));
        return o;
    }

    /**
     * Two formed quantum network bridges, each on its own small network.
     */
    private static BlockPos buildQnb(MinecraftServer server, BlockPos o) {
        var level = server.overworld();
        platform(level, o, -8, 8, -3, 6);
        for (int side : new int[] { -3, 3 }) {
            var center = o.offset(side, 1, 3);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    set(level, center.offset(dx, dy, 0),
                            dx == 0 && dy == 0 ? AEBlocks.QUANTUM_LINK.block() : AEBlocks.QUANTUM_RING.block());
                }
            }
            set(level, center.offset(0, -2, 1), AEBlocks.CREATIVE_ENERGY_CELL.block());
            set(level, center.offset(0, -1, 1), AEBlocks.CONTROLLER.block());
        }
        return o;
    }

    /**
     * A spatial IO port and pylons framing a small scene that is about to be stored.
     */
    private static BlockPos buildSpatial(MinecraftServer server, BlockPos o) {
        var level = server.overworld();
        platform(level, o, -9, 7, -3, 10);
        // Region to capture: x -3..1, y 0..4, z 3..7. Pylons run along three edges outside it.
        for (int i = -3; i <= 1; i++) {
            set(level, o.offset(i, 0, 2), AEBlocks.SPATIAL_PYLON.block());
            set(level, o.offset(i, 5, 2), AEBlocks.SPATIAL_PYLON.block());
        }
        for (int i = 0; i <= 5; i++) {
            set(level, o.offset(-4, i, 2), AEBlocks.SPATIAL_PYLON.block());
            set(level, o.offset(2, i, 2), AEBlocks.SPATIAL_PYLON.block());
        }
        for (int i = 3; i <= 7; i++) {
            set(level, o.offset(-4, 0, i), AEBlocks.SPATIAL_PYLON.block());
            set(level, o.offset(2, 0, i), AEBlocks.SPATIAL_PYLON.block());
        }
        set(level, o.offset(-5, 0, 2), AEBlocks.SPATIAL_IO_PORT.block());
        set(level, o.offset(-6, 0, 2), AEBlocks.CONTROLLER.block());
        set(level, o.offset(-6, -1, 2), AEBlocks.CREATIVE_ENERGY_CELL.block());
        // A tiny garden inside the region
        for (int x = -3; x <= 1; x++) {
            for (int z = 3; z <= 7; z++) {
                set(level, o.offset(x, -1, z), Blocks.GRASS_BLOCK);
            }
        }
        set(level, o.offset(-1, 0, 5), Blocks.OAK_LOG);
        set(level, o.offset(-1, 1, 5), Blocks.OAK_LOG);
        set(level, o.offset(-1, 2, 5), Blocks.OAK_LOG);
        for (int x = -2; x <= 0; x++) {
            for (int z = 4; z <= 6; z++) {
                for (int y = 3; y <= 4; y++) {
                    if (!(x == -1 && z == 5 && y == 3)) {
                        set(level, o.offset(x, y, z), Blocks.OAK_LEAVES);
                    }
                }
            }
        }
        set(level, o.offset(-1, 3, 5), Blocks.OAK_LOG);
        set(level, o.offset(-3, 0, 3), Blocks.POPPY);
        set(level, o.offset(1, 0, 4), Blocks.DANDELION);
        set(level, o.offset(0, 0, 7), Blocks.BLUE_ORCHID);
        set(level, o.offset(-3, 0, 6), Blocks.OXEYE_DAISY);
        return o;
    }

    // Helpers

    /**
     * A floor of sky stone bricks with a white quartz border, with the air above it cleared.
     */
    private static void platform(ServerLevel level, BlockPos o, int x0, int x1, int z0, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                boolean border = x == x0 || x == x1 || z == z0 || z == z1;
                set(level, o.offset(x, -1, z), border ? Blocks.SMOOTH_QUARTZ
                        : AEBlocks.SKY_STONE_SMALL_BRICK.block());
                for (int y = 0; y < 10; y++) {
                    set(level, o.offset(x, y, z), Blocks.AIR);
                }
            }
        }
    }

    private static void set(ServerLevel level, BlockPos pos, Block block) {
        level.setBlockAndUpdate(pos, block.defaultBlockState());
    }

    private static void addFacade(ServerLevel level, BlockPos pos, Direction side, ItemStack textureItem) {
        var facadeStack = AEItems.FACADE.get().createFacadeForItem(textureItem, false);
        var facade = FacadeItem.createFacade(facadeStack, side);
        if (facade != null && level.getBlockEntity(pos) instanceof IPartHost host) {
            host.getFacadeContainer().addFacade(facade);
            host.markForUpdate();
        }
    }

    private static final List<Item> STOCK = List.of(Items.DIAMOND, Items.IRON_INGOT, Items.GOLD_INGOT,
            Items.COPPER_INGOT, Items.REDSTONE, Items.LAPIS_LAZULI, Items.EMERALD, Items.COAL, Items.QUARTZ,
            Items.NETHERITE_INGOT, Items.AMETHYST_SHARD, Items.OBSIDIAN, Items.GLOWSTONE_DUST, Items.ENDER_PEARL,
            Items.BLAZE_ROD, Items.SLIME_BALL, Items.STRING, Items.BONE, Items.GUNPOWDER, Items.OAK_LOG,
            Items.SPRUCE_LOG, Items.BIRCH_LOG, Items.STONE, Items.COBBLESTONE, Items.DEEPSLATE, Items.GRANITE,
            Items.GLASS, Items.SAND, Items.GRAVEL, Items.CLAY_BALL, Items.WHEAT, Items.BREAD, Items.CARROT,
            Items.POTATO, Items.APPLE, Items.SUGAR_CANE, Items.LEATHER, Items.FEATHER, Items.EGG,
            Items.TORCH, Items.CHEST, Items.HOPPER, Items.PISTON, Items.OBSERVER);

    /**
     * Storage cells with contents, so the drive LEDs show a mix of states (green, orange when the types are full,
     * red when the bytes are full).
     */
    private static ItemStack filledCell(int index) {
        ItemStack cell;
        switch (index % 6) {
            case 0, 3 -> cell = AEItems.ITEM_CELL_64K.stack();
            case 1 -> cell = AEItems.ITEM_CELL_1K.stack();
            case 2 -> cell = AEItems.ITEM_CELL_16K.stack();
            case 4 -> cell = AEItems.FLUID_CELL_4K.stack();
            default -> cell = AEItems.ITEM_CELL_4K.stack();
        }
        var inv = StorageCells.getCellInventory(cell, null);
        if (inv == null) {
            return cell;
        }
        var source = IActionSource.empty();
        if (index % 6 == 4) {
            inv.insert(AEFluidKey.of(index % 12 == 4 ? Fluids.WATER : Fluids.LAVA), 40L * AEFluidKey.AMOUNT_BUCKET,
                    Actionable.MODULATE, source);
        } else if (index % 6 == 1) {
            // Full: red
            inv.insert(AEItemKey.of(Items.COBBLESTONE), 100_000, Actionable.MODULATE, source);
        } else if (index % 6 == 2) {
            // All 63 types used: orange
            for (int i = 0; i < 63; i++) {
                var key = AEItemKey.of(BuiltInRegistries.ITEM.byId(10 + i * 7));
                if (key != null) {
                    inv.insert(key, 16, Actionable.MODULATE, source);
                }
            }
        } else {
            for (int i = 0; i < STOCK.size(); i++) {
                if ((i + index) % 3 == 0) {
                    inv.insert(AEItemKey.of(STOCK.get(i)), 64L * (1 + (i * 37 + index) % 90),
                            Actionable.MODULATE, source);
                }
            }
        }
        return cell;
    }

    private static void shot(ClientGameTestContext context, TestServerContext server, BlockPos o, double x, double y,
            double z, float yaw, float pitch, String name) {
        lookFrom(context, server, o, x, y, z, yaw, pitch);
        context.waitTicks(120);
        screenshot(context, name);
    }

    private static void screenshot(ClientGameTestContext context, String name) {
        context.takeScreenshot(TestScreenshotOptions.of(name).disableCounterPrefix());
    }

    private static void lookFrom(ClientGameTestContext context, TestServerContext server, BlockPos o, double x,
            double y, double z, float yaw, float pitch) {
        onServer(server, srv -> {
            var level = srv.overworld();
            var player = player(srv);
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.teleportTo(level, o.getX() + x, o.getY() + y - 1.62, o.getZ() + z, Set.of(), yaw, pitch, true);
            return Boolean.TRUE;
        });
    }

    private static void rightClick(ClientGameTestContext context, BlockPos pos) {
        context.getInput().lookAt(pos);
        context.waitTicks(5);
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        context.waitTicks(60);
    }

    private static ServerPlayer player(MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }

    private static <T> T onServer(TestServerContext server, Function<MinecraftServer, T> action) {
        var result = new AtomicReference<T>();
        server.waitFor(srv -> {
            result.set(action.apply(srv));
            return true;
        });
        return result.get();
    }
}
