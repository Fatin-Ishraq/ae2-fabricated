package appeng.integration.modules.curios;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.EntityCapability;
import appeng.platform.neoforge.transfer.ResourceHandler;
import appeng.platform.neoforge.transfer.item.ItemResource;

public class CuriosIntegration {
    // Create Curios capability ourselves instead of accessing it from the Curios API to avoid loading Curios classes.
    public static final EntityCapability<ResourceHandler<ItemResource>, Void> ITEM_HANDLER = EntityCapability
            .createVoid(
                    Identifier.fromNamespaceAndPath("curios", "item_handler"),
                    ResourceHandler.asClass());
}
