package appeng.mixins.platform;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;

import appeng.platform.neoforge.common.extensions.IRecipeManagerExtension;

/**
 * FABRIC: Implements NeoForge's {@code RecipeManager#recipeMap()} (declared for compilation in ae2.classtweaker).
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin implements IRecipeManagerExtension {
    @Shadow
    private RecipeMap recipes;

    @Override
    public RecipeMap recipeMap() {
        return recipes;
    }
}
