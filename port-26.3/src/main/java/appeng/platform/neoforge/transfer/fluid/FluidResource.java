package appeng.platform.neoforge.transfer.fluid;

import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;

import appeng.platform.neoforge.fluids.FluidStack;
import appeng.platform.neoforge.fluids.FluidType;
import appeng.platform.neoforge.transfer.resource.Resource;

/**
 * FABRIC: A fluid with data components, without an amount. Backed by a Fabric {@link FluidVariant}.
 */
public final class FluidResource implements Resource, TypedInstance<Fluid>, DataComponentHolder {
    public static final FluidResource EMPTY = new FluidResource(FluidVariant.blank());

    public static final Codec<FluidResource> CODEC = Codec.lazyInitialized(
            () -> RecordCodecBuilder.create(i -> i.group(
                    BuiltInRegistries.FLUID.holderByNameCodec().fieldOf("id").forGetter(FluidResource::typeHolder),
                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                            .forGetter(FluidResource::getComponentsPatch))
                    .apply(i, FluidResource::of)));

    public static final Codec<FluidResource> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC).xmap(
            optional -> optional.orElse(EMPTY),
            resource -> resource.isEmpty() ? Optional.empty() : Optional.of(resource));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidResource> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.FLUID), FluidResource::typeHolder,
            DataComponentPatch.STREAM_CODEC, FluidResource::getComponentsPatch,
            FluidResource::of);

    private final FluidVariant variant;

    private FluidResource(FluidVariant variant) {
        this.variant = variant;
    }

    public static FluidResource of(FluidVariant variant) {
        return variant.isBlank() ? EMPTY : new FluidResource(variant);
    }

    public static FluidResource of(FluidStack stack) {
        return stack.isEmpty() ? EMPTY : of(FluidVariant.of(stack.getFluid(), stack.getComponentsPatch()));
    }

    public static FluidResource of(Fluid fluid) {
        return of(FluidVariant.of(fluid));
    }

    public static FluidResource of(Fluid fluid, DataComponentPatch patch) {
        return of(FluidVariant.of(fluid, patch));
    }

    public static FluidResource of(Holder<Fluid> fluid) {
        return of(fluid.value());
    }

    public static FluidResource of(Holder<Fluid> holder, DataComponentPatch patch) {
        return of(holder.value(), patch);
    }

    /**
     * @return the equivalent Fabric fluid variant.
     */
    public FluidVariant toVariant() {
        return variant;
    }

    public Fluid value() {
        return variant.getFluid();
    }

    public Fluid getFluid() {
        return variant.getFluid();
    }

    @Override
    public Holder<Fluid> typeHolder() {
        return variant.typeHolder();
    }

    public FluidType getFluidType() {
        return FluidType.of(getFluid());
    }

    @Override
    public boolean isEmpty() {
        return variant.isBlank();
    }

    public boolean is(Predicate<Holder<Fluid>> predicate) {
        return predicate.test(typeHolder());
    }

    public FluidResource withMergedPatch(DataComponentPatch patch) {
        if (isEmpty() || patch.isEmpty()) {
            return this;
        }
        var stack = toStack(1);
        stack.applyComponents(patch);
        return of(stack);
    }

    public <D> FluidResource with(DataComponentType<D> type, @Nullable D data) {
        if (isEmpty()) {
            return this;
        }
        var stack = toStack(1);
        stack.set(type, data);
        return of(stack);
    }

    public FluidResource without(DataComponentType<?> type) {
        if (isEmpty()) {
            return this;
        }
        var stack = toStack(1);
        stack.remove(type);
        return of(stack);
    }

    @Override
    public DataComponentMap getComponents() {
        return variant.getComponents();
    }

    public DataComponentPatch getComponentsPatch() {
        return variant.getComponentsPatch();
    }

    public FluidStack toStack(int amount) {
        return FluidStack.of(variant, amount);
    }

    public boolean isComponentsPatchEmpty() {
        return getComponentsPatch().isEmpty();
    }

    public boolean matches(FluidStack stack) {
        if (isEmpty()) {
            return stack.isEmpty();
        }
        return stack.getFluid() == getFluid() && stack.getComponentsPatch().equals(getComponentsPatch());
    }

    public boolean test(Predicate<FluidStack> predicate) {
        return predicate.test(toStack(FluidType.BUCKET_VOLUME));
    }

    public Component getHoverName() {
        return FluidVariantAttributes.getName(variant);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof FluidResource other && variant.equals(other.variant);
    }

    @Override
    public int hashCode() {
        return variant.hashCode();
    }

    @Override
    public String toString() {
        return isEmpty() ? "FluidResource[EMPTY]" : "FluidResource[" + variant + "]";
    }
}
