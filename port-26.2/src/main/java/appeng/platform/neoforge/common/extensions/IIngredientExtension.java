package appeng.platform.neoforge.common.extensions;

import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;

/**
 * FABRIC: NeoForge's extra methods on {@code Ingredient}, answered with Fabric custom ingredients. Injected through
 * ae2.classtweaker; {@link #getValues()} is implemented by {@code appeng.mixins.platform.IngredientMixin}.
 */
public interface IIngredientExtension {
    /**
     * @return true if the ingredient only matches by item (no component checks)
     */
    default boolean isSimple() {
        return !((FabricIngredient) this).requiresTesting();
    }

    /**
     * @return true for custom (non-vanilla) ingredients
     */
    default boolean isCustom() {
        return ((FabricIngredient) this).getCustomIngredient() != null;
    }

    /**
     * @return the items of a vanilla ingredient
     */
    default HolderSet<Item> getValues() {
        throw new UnsupportedOperationException("Implemented by IngredientMixin");
    }
}
