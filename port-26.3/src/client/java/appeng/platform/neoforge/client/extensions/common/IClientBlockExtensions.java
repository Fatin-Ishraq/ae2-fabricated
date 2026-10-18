package appeng.platform.neoforge.client.extensions.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * FABRIC: Client-side block behavior (the particle effects AE2 customizes). Registered per block with
 * {@link #register}; consulted by {@code appeng.mixins.platform.client.ClientLevelParticlesMixin}.
 */
public interface IClientBlockExtensions {
    IClientBlockExtensions DEFAULT = new IClientBlockExtensions() {
    };

    Map<Block, IClientBlockExtensions> REGISTERED = new ConcurrentHashMap<>();

    static void register(IClientBlockExtensions extensions, Block... blocks) {
        for (var block : blocks) {
            REGISTERED.put(block, extensions);
        }
    }

    static IClientBlockExtensions of(BlockState state) {
        return of(state.getBlock());
    }

    static IClientBlockExtensions of(Block block) {
        return REGISTERED.getOrDefault(block, DEFAULT);
    }

    /**
     * @return true to suppress the vanilla hit particles
     */
    default boolean addHitEffects(BlockState state, Level level, BlockPos pos, Direction face, ParticleEngine manager) {
        return false;
    }

    /**
     * @return true to suppress the vanilla destroy particles
     */
    default boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        return false;
    }
}
