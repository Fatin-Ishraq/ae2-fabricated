package appeng.mixins.platform;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.Level;

import appeng.platform.neoforge.common.extensions.ILevelExtension;

/**
 * FABRIC: Adds {@link ILevelExtension} to {@link Level} at runtime (declared for compilation in ae2.classtweaker).
 */
@Mixin(Level.class)
public abstract class LevelMixin implements ILevelExtension {
}
