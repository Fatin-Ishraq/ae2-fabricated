package appeng.platform.neoforge.capabilities;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * FABRIC: A block capability backed by a Fabric {@link BlockApiLookup}. Capabilities created by AE2 use their own
 * lookup with the same id; the standard item/fluid/energy capabilities in {@link Capabilities} are bridged to the
 * Fabric transfer API lookups, converting objects on the way in and out.
 *
 * @param <T> the type AE2 sees
 * @param <C> the context type
 */
public final class BlockCapability<T, C> extends BaseCapability<T, C> {
    private final Bridge<T, C, ?> bridge;

    private BlockCapability(Identifier name, Class<T> typeClass, Class<C> contextClass, Bridge<T, C, ?> bridge) {
        super(name, typeClass, contextClass);
        this.bridge = bridge;
    }

    public static <T, C> BlockCapability<T, C> create(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        var lookup = BlockApiLookup.get(name, typeClass, contextClass);
        return new BlockCapability<>(name, typeClass, contextClass,
                new Bridge<>(lookup, Function.identity(), Function.identity()));
    }

    public static <T> BlockCapability<T, @Nullable Void> createVoid(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, Void.class);
    }

    public static <T> BlockCapability<T, @Nullable Direction> createSided(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, Direction.class);
    }

    /**
     * FABRIC: A capability whose providers and queries go through an existing Fabric lookup.
     *
     * @param toFabric   converts what AE2 provides into the type of the Fabric lookup
     * @param fromFabric converts what the Fabric lookup returns into the type AE2 expects
     */
    public static <T, C, F> BlockCapability<T, C> bridge(Identifier name, Class<T> typeClass, Class<C> contextClass,
            BlockApiLookup<F, C> lookup, Function<T, F> toFabric, Function<F, T> fromFabric) {
        return new BlockCapability<>(name, typeClass, contextClass, new Bridge<>(lookup, toFabric, fromFabric));
    }

    /**
     * @return the Fabric lookup this capability is backed by.
     */
    public BlockApiLookup<?, C> lookup() {
        return bridge.lookup;
    }

    @Nullable
    public T getCapability(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity,
            C context) {
        return bridge.find(level, pos, state, blockEntity, context);
    }

    @Nullable
    T fromFabric(@Nullable Object fabricObject) {
        return bridge.fromFabricUnchecked(fabricObject);
    }

    <BE extends BlockEntity> void registerForBlockEntity(BlockEntityType<BE> type,
            ICapabilityProvider<? super BE, C, T> provider) {
        bridge.registerForBlockEntity(type, provider);
    }

    void registerForBlocks(IBlockCapabilityProvider<T, C> provider, Block... blocks) {
        bridge.registerForBlocks(provider, blocks);
    }

    /**
     * NeoForge allows several providers per block and returns the first non-null result. Fabric lookups only keep
     * one provider per block, so all providers for a block are collected here and a single Fabric provider queries
     * them in registration order.
     */
    private static final class Bridge<T, C, F> {
        private final BlockApiLookup<F, C> lookup;
        private final Function<T, F> toFabric;
        private final Function<F, T> fromFabric;
        private final java.util.Map<Block, java.util.List<IBlockCapabilityProvider<T, C>>> providers = new java.util.IdentityHashMap<>();

        Bridge(BlockApiLookup<F, C> lookup, Function<T, F> toFabric, Function<F, T> fromFabric) {
            this.lookup = lookup;
            this.toFabric = toFabric;
            this.fromFabric = fromFabric;
        }

        @Nullable
        T find(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, C context) {
            var result = lookup.find(level, pos, state, blockEntity, context);
            return result == null ? null : fromFabric.apply(result);
        }

        @SuppressWarnings("unchecked")
        @Nullable
        T fromFabricUnchecked(@Nullable Object value) {
            return value == null ? null : fromFabric.apply((F) value);
        }

        @SuppressWarnings("unchecked")
        <BE extends BlockEntity> void registerForBlockEntity(BlockEntityType<BE> type,
                ICapabilityProvider<? super BE, C, T> provider) {
            IBlockCapabilityProvider<T, C> blockProvider = (level, pos, state, be, context) -> {
                if (be != null && be.getType() == type) {
                    return provider.getCapability((BE) be, context);
                }
                return null;
            };
            for (var block : net.minecraft.core.registries.BuiltInRegistries.BLOCK) {
                if (type.isValid(block.defaultBlockState())) {
                    add(block, blockProvider);
                }
            }
        }

        void registerForBlocks(IBlockCapabilityProvider<T, C> provider, Block... blocks) {
            for (var block : blocks) {
                add(block, provider);
            }
        }

        private void add(Block block, IBlockCapabilityProvider<T, C> provider) {
            var list = providers.get(block);
            if (list == null) {
                list = new java.util.concurrent.CopyOnWriteArrayList<>();
                providers.put(block, list);
                var blockProviders = list;
                lookup.registerForBlocks((level, pos, state, be, context) -> {
                    for (var p : blockProviders) {
                        var result = p.getCapability(level, pos, state, be, context);
                        if (result != null) {
                            return toFabric.apply(result);
                        }
                    }
                    return null;
                }, block);
            }
            list.add(provider);
        }
    }
}
