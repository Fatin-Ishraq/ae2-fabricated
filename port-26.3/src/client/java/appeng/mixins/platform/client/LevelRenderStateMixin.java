package appeng.mixins.platform.client;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.state.level.LevelRenderState;

import appeng.platform.neoforge.client.extensions.ILevelRenderStateExtension;

/**
 * FABRIC: Adds {@link ILevelRenderStateExtension} to {@link LevelRenderState} at runtime (declared for compilation in
 * ae2.classtweaker).
 */
@Mixin(LevelRenderState.class)
public abstract class LevelRenderStateMixin implements ILevelRenderStateExtension {
}
