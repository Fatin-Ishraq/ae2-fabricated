package appeng.mixins.platform;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.storage.ValueOutput;

import appeng.platform.neoforge.common.extensions.IValueOutputExtension;

/**
 * FABRIC: Adds {@link IValueOutputExtension} to {@link ValueOutput} at runtime (declared for compilation in
 * ae2.classtweaker).
 */
@Mixin(ValueOutput.class)
public interface ValueOutputMixin extends IValueOutputExtension {
}
