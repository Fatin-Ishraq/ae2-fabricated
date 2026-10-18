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

package appeng.menu.implementations;

import java.util.function.Function;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.Nullable;

import io.netty.buffer.Unpooled;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import appeng.core.AppEng;
import appeng.init.InitMenuTypes;
import appeng.menu.AEBaseMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.locator.MenuLocators;

/**
 * Builder that allows creation of menu types which can be opened from multiple types of hosts.
 */
public final class MenuTypeBuilder<M extends AEBaseMenu, I> {

    @Nullable
    private Identifier id;

    private final Class<I> hostInterface;

    private final MenuFactory<M, I> factory;

    private Function<I, Component> menuTitleStrategy = this::getDefaultMenuTitle;

    @Nullable
    private InitialDataSerializer<I> initialDataSerializer;

    @Nullable
    private InitialDataDeserializer<M, I> initialDataDeserializer;

    private MenuType<M> menuType;

    private MenuTypeBuilder(Class<I> hostInterface, TypedMenuFactory<M, I> typedFactory) {
        this.hostInterface = hostInterface;
        this.factory = (containerId, playerInv, accessObj) -> typedFactory.create(menuType, containerId, playerInv,
                accessObj);
    }

    private MenuTypeBuilder(Class<I> hostInterface, MenuFactory<M, I> factory) {
        this.hostInterface = hostInterface;
        this.factory = factory;
    }

    public static <C extends AEBaseMenu, I> MenuTypeBuilder<C, I> create(MenuFactory<C, I> factory,
            Class<I> hostInterface) {
        return new MenuTypeBuilder<>(hostInterface, factory);
    }

    public static <C extends AEBaseMenu, I> MenuTypeBuilder<C, I> create(TypedMenuFactory<C, I> factory,
            Class<I> hostInterface) {
        return new MenuTypeBuilder<>(hostInterface, factory);
    }

    /**
     * Specifies a custom strategy for obtaining a custom menu name.
     * <p>
     * The strategy should return {@link Component#empty()} if there's no custom name.
     */
    public MenuTypeBuilder<M, I> withMenuTitle(Function<I, Component> menuTitleStrategy) {
        this.menuTitleStrategy = menuTitleStrategy;
        return this;
    }

    /**
     * Sets a serializer and deserializer for additional data that should be transmitted from server->client when the
     * menu is being first opened.
     */
    public MenuTypeBuilder<M, I> withInitialData(InitialDataSerializer<I> initialDataSerializer,
            InitialDataDeserializer<M, I> initialDataDeserializer) {
        this.initialDataSerializer = initialDataSerializer;
        this.initialDataDeserializer = initialDataDeserializer;
        return this;
    }

    /**
     * Opens a menu that is based around a single block entity. The block entity's position is encoded in the packet
     * buffer.
     */
    private M fromNetwork(int containerId, Inventory inv, byte[] data) {
        var packetBuf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), inv.player.registryAccess());
        var locator = MenuLocators.readFromPacket(packetBuf);
        I host = locator.locate(inv.player, hostInterface);
        if (host == null) {
            var connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                connection.send(new ServerboundContainerClosePacket(containerId));
            }
            throw new IllegalStateException("Couldn't find menu host at " + locator + " for " + this.id
                    + " on client. Closing menu.");
        }
        M menu = factory.create(containerId, inv, host);
        menu.setReturnedFromSubScreen(packetBuf.readBoolean());
        if (initialDataDeserializer != null) {
            initialDataDeserializer.deserializeInitialData(host, menu, packetBuf);
        }
        return menu;
    }

    private boolean open(ServerPlayer player, MenuHostLocator locator, boolean fromSubMenu) {
        var accessInterface = locator.locate(player, hostInterface);

        if (accessInterface == null) {
            return false;
        }

        Component title = menuTitleStrategy.apply(accessInterface);

        // FABRIC: The initial data is serialized into a byte array on the server and turned back into a
        // RegistryFriendlyByteBuf on the client, since Fabric's extended menu types need a typed stream codec.
        var dataBuffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        try {
            MenuLocators.writeToPacket(dataBuffer, locator);
            dataBuffer.writeBoolean(fromSubMenu);
            if (initialDataSerializer != null) {
                initialDataSerializer.serializeInitialData(accessInterface, dataBuffer);
            }
            var data = new byte[dataBuffer.readableBytes()];
            dataBuffer.readBytes(data);

            boolean closeCurrentScreen = !(player.containerMenu instanceof AEBaseMenu);
            player.openMenu(new AppEngMenuProvider(title, locator, accessInterface, data, closeCurrentScreen));
        } finally {
            dataBuffer.release();
        }

        return true;
    }

    public MenuType<M> build(String id) {
        return build(AppEng.makeId(id));
    }

    /**
     * Creates a menu type that uses this helper as a factory and network deserializer.
     */
    public MenuType<M> buildUnregistered(Identifier id) {
        Preconditions.checkState(menuType == null, "build was already called");
        Preconditions.checkState(this.id == null, "id should not be set");

        this.id = id;
        menuType = new ExtendedMenuType<>(this::fromNetwork, ByteBufCodecs.BYTE_ARRAY);
        MenuOpener.addOpener(menuType, this::open);
        return menuType;
    }

    /**
     * Creates a menu type that uses this helper as a factory and network deserializer, and queues it for registration
     * with Vanilla.
     */
    public MenuType<M> build(Identifier id) {
        var menuType = buildUnregistered(id);
        InitMenuTypes.queueRegistration(this.id, menuType);
        return menuType;
    }

    @FunctionalInterface
    public interface MenuFactory<C, I> {
        C create(int containerId, Inventory playerInv, I menuHost);
    }

    @FunctionalInterface
    public interface TypedMenuFactory<C extends AbstractContainerMenu, I> {
        C create(MenuType<C> type, int containerId, Inventory playerInv, I accessObj);
    }

    /**
     * Strategy used to serialize initial data for opening the menu on the client-side into the packet that is sent to
     * the client.
     */
    @FunctionalInterface
    public interface InitialDataSerializer<I> {
        void serializeInitialData(I host, RegistryFriendlyByteBuf buffer);
    }

    /**
     * Strategy used to deserialize initial data for opening the menu on the client-side from the packet received by the
     * server.
     */
    @FunctionalInterface
    public interface InitialDataDeserializer<C, I> {
        void deserializeInitialData(I host, C menu, RegistryFriendlyByteBuf buffer);
    }

    private Component getDefaultMenuTitle(I accessInterface) {
        if (accessInterface instanceof Nameable nameable) {
            if (nameable.hasCustomName()) {
                return nameable.getCustomName();
            }
        }

        return Component.empty();
    }


    /**
     * FABRIC: Replaces the local MenuProvider class upstream passes to NeoForge's openMenu(provider, buffer writer).
     */
    private class AppEngMenuProvider implements ExtendedMenuProvider<byte[]> {
        private final Component title;
        private final MenuHostLocator locator;
        private final I accessInterface;
        private final byte[] data;
        private final boolean closeCurrentScreen;

        AppEngMenuProvider(Component title, MenuHostLocator locator, I accessInterface, byte[] data,
                boolean closeCurrentScreen) {
            this.title = title;
            this.locator = locator;
            this.accessInterface = accessInterface;
            this.data = data;
            this.closeCurrentScreen = closeCurrentScreen;
        }

        @Override
        public Component getDisplayName() {
            return title;
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int wnd, Inventory p, Player pl) {
            M m = factory.create(wnd, p, accessInterface);
            // Set the original locator on the opened server-side menu for it to more
            // easily remember how to re-open after being closed.
            m.setLocator(locator);
            return m;
        }

        @Override
        public byte[] getScreenOpeningData(ServerPlayer player) {
            return data;
        }

        @Override
        public boolean shouldCloseCurrentScreen() {
            // Do not send close packets when switching between AE menus
            return closeCurrentScreen;
        }
    }

}
