package appeng.platform.neoforge.client.model.block;

/**
 * FABRIC: A block state model type with its own codec. Same contract as the Fabric interface, which it extends;
 * codecs are registered with {@link net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel#register}.
 */
public interface CustomUnbakedBlockStateModel
        extends net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel {
}
