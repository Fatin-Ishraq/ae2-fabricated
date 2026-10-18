package appeng.platform.neoforge.common.extensions;

import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.platform.neoforge.model.data.ModelData;

/**
 * FABRIC: NeoForge's extra methods on {@link BlockEntity}. Injected through ae2.classtweaker and
 * {@code appeng.mixins.platform.BlockEntityMixin}, which also exposes {@link #getModelData()} as Fabric block entity
 * render data.
 */
public interface IBlockEntityExtension {
    /**
     * @return the data for rendering the block model of this block entity.
     */
    default ModelData getModelData() {
        return ModelData.EMPTY;
    }

    /**
     * Requests that the block is re-rendered with new model data.
     */
    default void requestModelDataUpdate() {
        var be = (BlockEntity) this;
        var level = be.getLevel();
        if (level != null && level.isClientSide()) {
            var state = be.getBlockState();
            level.sendBlockUpdated(be.getBlockPos(), state, state, 8);
        }
    }

    /**
     * Notifies capability listeners for this block entity's position (Fabric lookups themselves are not cached).
     */
    default void invalidateCapabilities() {
        var be = (BlockEntity) this;
        if (be.getLevel() != null) {
            appeng.platform.neoforge.capabilities.CapabilityListeners.invalidate(be.getLevel(), be.getBlockPos());
        }
    }

    /**
     * Called when the chunk containing this block entity is unloaded (see {@code appeng.hooks.BlockEntityUnloadHook}).
     */
    default void onChunkUnloaded() {
    }
}
