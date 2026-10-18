package appeng.platform.neoforge.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/**
 * FABRIC: The common ({@code c:}) convention tags that AE2 uses. NeoForge and Fabric share these tag ids.
 */
public final class Tags {
    private Tags() {
    }

    public static final class Biomes {
        public static final TagKey<Biome> IS_COLD = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_cold"));
        public static final TagKey<Biome> IS_OCEAN = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_ocean"));
        public static final TagKey<Biome> IS_PLAINS = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_plains"));
        public static final TagKey<Biome> IS_SANDY = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_sandy"));
        public static final TagKey<Biome> IS_SNOWY = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_snowy"));

        private Biomes() {
        }
    }

    public static final class Blocks {
        public static final TagKey<Block> BUDDING_BLOCKS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "budding_blocks"));
        public static final TagKey<Block> BUDS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "buds"));
        public static final TagKey<Block> CHESTS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "chests"));
        public static final TagKey<Block> CLUSTERS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "clusters"));
        public static final TagKey<Block> GLASS_BLOCKS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "glass_blocks"));
        public static final TagKey<Block> RELOCATION_NOT_SUPPORTED = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "relocation_not_supported"));
        public static final TagKey<Block> STORAGE_BLOCKS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "storage_blocks"));

        private Blocks() {
        }
    }

    public static final class Items {
        public static final TagKey<Item> BUDDING_BLOCKS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "budding_blocks"));
        public static final TagKey<Item> BUDS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "buds"));
        public static final TagKey<Item> CHESTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "chests"));
        public static final TagKey<Item> CHESTS_WOODEN = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "chests/wooden"));
        public static final TagKey<Item> CLUSTERS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "clusters"));
        public static final TagKey<Item> DUSTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dusts"));
        public static final TagKey<Item> DUSTS_GLOWSTONE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dusts/glowstone"));
        public static final TagKey<Item> DUSTS_REDSTONE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dusts/redstone"));
        public static final TagKey<Item> ENDER_PEARLS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ender_pearls"));
        public static final TagKey<Item> GEMS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "gems"));
        public static final TagKey<Item> GEMS_DIAMOND = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "gems/diamond"));
        public static final TagKey<Item> GEMS_QUARTZ = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "gems/quartz"));
        public static final TagKey<Item> GLASS_BLOCKS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "glass_blocks"));
        public static final TagKey<Item> GLASS_BLOCKS_CHEAP = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "glass_blocks/cheap"));
        public static final TagKey<Item> INGOTS_COPPER = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/copper"));
        public static final TagKey<Item> INGOTS_GOLD = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/gold"));
        public static final TagKey<Item> INGOTS_IRON = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/iron"));
        public static final TagKey<Item> NUGGETS_GOLD = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "nuggets/gold"));
        public static final TagKey<Item> NUGGETS_IRON = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "nuggets/iron"));
        public static final TagKey<Item> RODS_WOODEN = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "rods/wooden"));
        public static final TagKey<Item> STONES = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "stones"));

        private Items() {
        }
    }
}
