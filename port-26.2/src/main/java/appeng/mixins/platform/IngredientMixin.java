package appeng.mixins.platform;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.platform.neoforge.common.extensions.IIngredientExtension;

/**
 * FABRIC: Adds {@link IIngredientExtension} to {@link Ingredient} at runtime (declared for compilation in
 * ae2.classtweaker).
 */
@Mixin(Ingredient.class)
public abstract class IngredientMixin implements IIngredientExtension {
    @Shadow
    @Final
    private HolderSet<Item> values;

    @Override
    public HolderSet<Item> getValues() {
        return values;
    }
}
