package appeng.block.networking;

import appeng.platform.neoforge.model.data.ModelData;

import appeng.api.parts.IPartItem;

public record PartRenderState(IPartItem<?> partItem, ModelData modelData) {
}
