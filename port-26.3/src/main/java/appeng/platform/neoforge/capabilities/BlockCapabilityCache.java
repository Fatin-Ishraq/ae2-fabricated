package appeng.platform.neoforge.capabilities;

import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * FABRIC: Cached capability lookup at a fixed position, backed by Fabric's {@link BlockApiCache}. Fabric's cache
 * tracks block entity changes itself, so every {@link #getCapability()} call returns the current object. The
 * invalidation listener of NeoForge is not supported (it is never called).
 */
public final class BlockCapabilityCache<T, C> {
    private final BlockCapability<T, C> capability;
    private final ServerLevel level;
    private final BlockPos pos;
    private final C context;
    private final BlockApiCache<?, C> cache;

    private BlockCapabilityCache(BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context) {
        this.capability = capability;
        this.level = level;
        this.pos = pos;
        this.context = context;
        this.cache = BlockApiCache.create(capability.lookup(), level, pos);
    }

    public static <T, C> BlockCapabilityCache<T, C> create(BlockCapability<T, C> capability, ServerLevel level,
            BlockPos pos, C context) {
        return new BlockCapabilityCache<>(capability, level, pos.immutable(), context);
    }

    public static <T, C> BlockCapabilityCache<T, C> create(BlockCapability<T, C> capability, ServerLevel level,
            BlockPos pos, C context, BooleanSupplier isValid, Runnable invalidationListener) {
        return create(capability, level, pos, context);
    }

    public ServerLevel level() {
        return level;
    }

    public BlockPos pos() {
        return pos;
    }

    public C context() {
        return context;
    }

    @Nullable
    public T getCapability() {
        if (!level.isLoaded(pos)) {
            return null;
        }
        return capability.fromFabric(cache.find(context));
    }
}
