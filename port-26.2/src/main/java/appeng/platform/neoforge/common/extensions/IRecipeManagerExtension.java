package appeng.platform.neoforge.common.extensions;

import net.minecraft.world.item.crafting.RecipeMap;

/**
 * FABRIC: NeoForge's {@code RecipeManager#recipeMap()}. Injected through ae2.classtweaker; implemented by
 * {@code appeng.mixins.platform.RecipeManagerMixin}.
 */
public interface IRecipeManagerExtension {
    default RecipeMap recipeMap() {
        throw new UnsupportedOperationException("Implemented by RecipeManagerMixin");
    }
}
