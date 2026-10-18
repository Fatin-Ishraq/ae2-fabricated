package appeng.platform.neoforge.capabilities;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * FABRIC: Collects capability providers and registers them with the backing Fabric lookups right away. AE2 creates
 * one instance during common setup (see {@code AppEngBase}).
 */
public class RegisterCapabilitiesEvent {
    private record Registration(BlockCapability<?, ?> capability, Block block) {
    }

    private final Set<Registration> blockRegistrations = new HashSet<>();

    public <T, C> void registerBlock(BlockCapability<T, C> capability, IBlockCapabilityProvider<T, C> provider,
            Block... blocks) {
        capability.registerForBlocks(provider, blocks);
        for (var block : blocks) {
            blockRegistrations.add(new Registration(capability, block));
        }
    }

    public <T, C, BE extends BlockEntity> void registerBlockEntity(BlockCapability<T, C> capability,
            BlockEntityType<BE> blockEntityType, ICapabilityProvider<? super BE, C, T> provider) {
        capability.registerForBlockEntity(blockEntityType, provider);
        for (var block : BuiltInRegistries.BLOCK) {
            if (blockEntityType.isValid(block.defaultBlockState())) {
                blockRegistrations.add(new Registration(capability, block));
            }
        }
    }

    public boolean isBlockRegistered(BlockCapability<?, ?> capability, Block block) {
        return blockRegistrations.contains(new Registration(capability, block));
    }

    /**
     * NeoForge capability proxying has no Fabric equivalent; ignored.
     */
    public void setProxyable(BlockCapability<?, ?> capability) {
    }

    /**
     * NeoForge capability proxying has no Fabric equivalent; ignored.
     */
    public void setNonProxyable(BlockCapability<?, ?> capability) {
    }

    public <T, C> void registerItem(ItemCapability<T, C> capability, ICapabilityProvider<ItemStack, C, T> provider,
            ItemLike... items) {
        capability.register(provider, items);
    }
}
