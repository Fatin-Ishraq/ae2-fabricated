package appeng.mixins.platform.client;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.resources.model.ModelManager;

import appeng.platform.neoforge.client.extensions.IModelManagerExtension;

/**
 * FABRIC: Adds {@link IModelManagerExtension} to {@link ModelManager} at runtime (declared for compilation in ae2.classtweaker).
 */
@Mixin(ModelManager.class)
public abstract class ModelManagerMixin implements IModelManagerExtension {
}
