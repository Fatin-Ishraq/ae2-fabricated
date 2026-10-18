package appeng.platform.neoforge.transfer.item;

import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;

import appeng.platform.neoforge.transfer.resource.Resource;

/**
 * FABRIC: An item with data components, without a count. Backed by a Fabric {@link ItemVariant}, so conversion to the
 * Fabric transfer API is free.
 */
public final class ItemResource implements Resource, TypedInstance<Item>, DataComponentHolder {
    public static final ItemResource EMPTY = new ItemResource(ItemVariant.blank());

    public static final Codec<ItemResource> CODEC = Codec.lazyInitialized(
            () -> RecordCodecBuilder.create(i -> i.group(
                    Item.CODEC.fieldOf("id").forGetter(ItemResource::typeHolder),
                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                            .forGetter(ItemResource::getComponentsPatch))
                    .apply(i, ItemResource::of)));

    public static final Codec<ItemResource> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC).xmap(
            optional -> optional.orElse(EMPTY),
            resource -> resource.isEmpty() ? Optional.empty() : Optional.of(resource));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemResource> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ITEM), ItemResource::typeHolder,
            DataComponentPatch.STREAM_CODEC, ItemResource::getComponentsPatch,
            ItemResource::of);

    private final ItemVariant variant;

    private ItemResource(ItemVariant variant) {
        this.variant = variant;
    }

    public static ItemResource of(ItemVariant variant) {
        return variant.isBlank() ? EMPTY : new ItemResource(variant);
    }

    public static ItemResource of(ItemStack stack) {
        return stack.isEmpty() ? EMPTY : of(ItemVariant.of(stack));
    }

    public static ItemResource of(@Nullable ItemStackTemplate template) {
        if (template == null) {
            return EMPTY;
        }
        return of(template.item(), template.components());
    }

    public static ItemResource of(ItemLike item) {
        return of(ItemVariant.of(item));
    }

    public static ItemResource of(ItemLike item, DataComponentPatch patch) {
        return of(ItemVariant.of(item, patch));
    }

    public static ItemResource of(Holder<Item> holder) {
        return of(holder.value());
    }

    public static ItemResource of(Holder<Item> holder, DataComponentPatch patch) {
        return of(holder.value(), patch);
    }

    /**
     * @return the equivalent Fabric item variant.
     */
    public ItemVariant toVariant() {
        return variant;
    }

    public Item value() {
        return variant.getItem();
    }

    public Item getItem() {
        return variant.getItem();
    }

    @Override
    public Holder<Item> typeHolder() {
        return variant.typeHolder();
    }

    @Override
    public boolean isEmpty() {
        return variant.isBlank();
    }

    public boolean matches(ItemStack stack) {
        return isEmpty() ? stack.isEmpty() : variant.matches(stack);
    }

    public boolean matches(@Nullable ItemStackTemplate template) {
        if (template == null) {
            return isEmpty();
        }
        return getItem() == template.item().value() && getComponentsPatch().equals(template.components());
    }

    public boolean is(ItemLike item) {
        return getItem() == item.asItem();
    }

    public boolean is(Predicate<Holder<Item>> predicate) {
        return predicate.test(typeHolder());
    }

    public boolean test(Predicate<ItemStack> predicate) {
        return predicate.test(toStack());
    }

    public boolean isComponentsPatchEmpty() {
        return variant.getComponentsPatch().isEmpty();
    }

    public ItemResource withMergedPatch(DataComponentPatch patch) {
        if (isEmpty() || patch.isEmpty()) {
            return this;
        }
        var stack = toStack();
        stack.applyComponents(patch);
        return of(stack);
    }

    public <D> ItemResource with(DataComponentType<D> type, @Nullable D data) {
        if (isEmpty()) {
            return this;
        }
        var stack = toStack();
        stack.set(type, data);
        return of(stack);
    }

    public ItemResource without(DataComponentType<?> type) {
        if (isEmpty()) {
            return this;
        }
        var stack = toStack();
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

    public ItemStack toStack(int count) {
        return isEmpty() ? ItemStack.EMPTY : variant.toStack(count);
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public int getMaxStackSize() {
        return getComponents().getOrDefault(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE, 1);
    }

    public Component getHoverName() {
        return toStack().getHoverName();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof ItemResource other && variant.equals(other.variant);
    }

    @Override
    public int hashCode() {
        return variant.hashCode();
    }

    @Override
    public String toString() {
        return isEmpty() ? "ItemResource[EMPTY]" : "ItemResource[" + variant + "]";
    }
}
