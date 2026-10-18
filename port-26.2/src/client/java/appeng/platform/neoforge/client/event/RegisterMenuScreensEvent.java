package appeng.platform.neoforge.client.event;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * FABRIC: Registers menu screens with vanilla {@link MenuScreens} (made accessible in ae2.classtweaker).
 */
public class RegisterMenuScreensEvent {
    public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(
            MenuType<? extends M> menuType, MenuScreens.ScreenConstructor<M, U> screenConstructor) {
        MenuScreens.register(menuType, screenConstructor);
    }
}
