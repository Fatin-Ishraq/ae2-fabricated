package appeng.platform.neoforge.fluids;

import java.util.Objects;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * FABRIC: A mutable fluid amount with data components. Amounts are in Fabric droplets (81000 per bucket, see
 * {@link FluidType#BUCKET_VOLUME}).
 */
public final class FluidStack implements TypedInstance<Fluid>, DataComponentHolder {
    public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY.builtInRegistryHolder(), 0,
            DataComponentPatch.EMPTY);

    private static final Codec<Holder<Fluid>> FLUID_NON_EMPTY_CODEC = BuiltInRegistries.FLUID.holderByNameCodec()
            .validate(holder -> holder.value() == Fluids.EMPTY
                    ? com.mojang.serialization.DataResult.error(() -> "Fluid must not be minecraft:empty")
                    : com.mojang.serialization.DataResult.success(holder));

    public static final Codec<FluidStack> CODEC = Codec.lazyInitialized(
            () -> RecordCodecBuilder.create(i -> i.group(
                    FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidStack::typeHolder),
                    ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(FluidStack::getAmount),
                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                            .forGetter(FluidStack::getComponentsPatch))
                    .apply(i, FluidStack::new)));

    public static final Codec<FluidStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC).xmap(
            optional -> optional.orElse(EMPTY),
            stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public FluidStack decode(RegistryFriendlyByteBuf buf) {
            int amount = buf.readVarInt();
            if (amount <= 0) {
                return EMPTY;
            }
            var fluid = ByteBufCodecs.holderRegistry(Registries.FLUID).decode(buf);
            var patch = DataComponentPatch.STREAM_CODEC.decode(buf);
            return new FluidStack(fluid, amount, patch);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
            if (stack.isEmpty()) {
                buf.writeVarInt(0);
                return;
            }
            buf.writeVarInt(stack.getAmount());
            ByteBufCodecs.holderRegistry(Registries.FLUID).encode(buf, stack.typeHolder());
            DataComponentPatch.STREAM_CODEC.encode(buf, stack.getComponentsPatch());
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> STREAM_CODEC = OPTIONAL_STREAM_CODEC;

    private final Holder<Fluid> fluid;
    private int amount;
    private final PatchedDataComponentMap components;

    public FluidStack(Fluid fluid, int amount) {
        this(fluid.builtInRegistryHolder(), amount, DataComponentPatch.EMPTY);
    }

    public FluidStack(Fluid fluid, int amount, DataComponentPatch patch) {
        this(fluid.builtInRegistryHolder(), amount, patch);
    }

    public FluidStack(Holder<Fluid> fluid, int amount) {
        this(fluid, amount, DataComponentPatch.EMPTY);
    }

    public FluidStack(Holder<Fluid> fluid, int amount, DataComponentPatch patch) {
        this.fluid = fluid;
        this.amount = amount;
        this.components = PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch);
    }

    public static FluidStack of(FluidVariant variant, int amount) {
        if (variant.isBlank() || amount <= 0) {
            return EMPTY;
        }
        return new FluidStack(variant.typeHolder(), amount, variant.getComponentsPatch());
    }

    /**
     * @return the equivalent Fabric fluid variant (without the amount).
     */
    public FluidVariant toVariant() {
        return isEmpty() ? FluidVariant.blank() : FluidVariant.of(getFluid(), getComponentsPatch());
    }

    public boolean isEmpty() {
        return amount <= 0 || fluid.value() == Fluids.EMPTY;
    }

    public Fluid getFluid() {
        return isEmpty() ? Fluids.EMPTY : fluid.value();
    }

    public Holder<Fluid> getFluidHolder() {
        return fluid;
    }

    @Override
    public Holder<Fluid> typeHolder() {
        return isEmpty() ? Fluids.EMPTY.builtInRegistryHolder() : fluid;
    }

    public FluidType getFluidType() {
        return FluidType.of(getFluid());
    }

    public int getAmount() {
        return isEmpty() ? 0 : amount;
    }

    public void setAmount(int amount) {
        if (this == EMPTY) {
            throw new UnsupportedOperationException("Cannot modify the empty fluid stack");
        }
        this.amount = amount;
    }

    public void grow(int amount) {
        setAmount(this.amount + amount);
    }

    public void shrink(int amount) {
        setAmount(this.amount - amount);
    }

    public void limitSize(int max) {
        if (!isEmpty() && amount > max) {
            setAmount(max);
        }
    }

    @Override
    public DataComponentMap getComponents() {
        return components;
    }

    public DataComponentPatch getComponentsPatch() {
        return isEmpty() ? DataComponentPatch.EMPTY : components.asPatch();
    }

    public boolean isComponentsPatchEmpty() {
        return getComponentsPatch().isEmpty();
    }

    public <T> T set(DataComponentType<T> type, T value) {
        return components.set(type, value);
    }

    public <T> T remove(DataComponentType<? extends T> type) {
        return components.remove(type);
    }

    public void applyComponents(DataComponentPatch patch) {
        components.applyPatch(patch);
    }

    public FluidStack copy() {
        if (isEmpty()) {
            return EMPTY;
        }
        return new FluidStack(fluid, amount, components.asPatch());
    }

    public FluidStack copyWithAmount(int amount) {
        if (isEmpty() || amount <= 0) {
            return EMPTY;
        }
        return new FluidStack(fluid, amount, components.asPatch());
    }

    public FluidStack split(int amount) {
        int taken = Math.min(amount, getAmount());
        var result = copyWithAmount(taken);
        if (!isEmpty()) {
            shrink(taken);
        }
        return result;
    }

    public Component getHoverName() {
        return FluidVariantAttributes.getName(toVariant());
    }

    public static boolean isSameFluid(FluidStack first, FluidStack other) {
        return first.getFluid() == other.getFluid();
    }

    public static boolean isSameFluidSameComponents(FluidStack first, FluidStack other) {
        if (first.getFluid() != other.getFluid()) {
            return false;
        }
        return first.isEmpty() && other.isEmpty()
                || Objects.equals(first.getComponentsPatch(), other.getComponentsPatch());
    }

    public static boolean matches(FluidStack first, FluidStack other) {
        return first.getAmount() == other.getAmount() && isSameFluidSameComponents(first, other);
    }

    public static int hashFluidAndComponents(FluidStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return 31 * stack.getFluid().hashCode() + stack.getComponentsPatch().hashCode();
    }

    @Override
    public String toString() {
        return getAmount() + " " + BuiltInRegistries.FLUID.getKey(getFluid());
    }
}
