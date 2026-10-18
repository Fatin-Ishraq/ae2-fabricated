package appeng.parts.p2p;

import appeng.platform.neoforge.capabilities.Capabilities;
import appeng.platform.neoforge.transfer.item.ItemResource;

import appeng.api.parts.IPartItem;
import appeng.api.stacks.AEKeyType;

public class ItemP2PTunnelPart extends ResourceHandlerP2PTunnelPart<ItemP2PTunnelPart, ItemResource> {
    public ItemP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem, Capabilities.Item.BLOCK, ItemResource.EMPTY, AEKeyType.items());
    }
}
