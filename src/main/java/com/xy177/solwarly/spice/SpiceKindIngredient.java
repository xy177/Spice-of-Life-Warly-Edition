package com.xy177.solwarly.spice;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.*;
import net.neoforged.neoforge.registries.*;
import java.util.stream.Stream;

/** Match a seasoning identity, independently of configurable tint and unrelated item data. */
public record SpiceKindIngredient(String flavor) implements ICustomIngredient {
    public static final MapCodec<SpiceKindIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        com.mojang.serialization.Codec.STRING.fieldOf("flavor").forGetter(SpiceKindIngredient::flavor)).apply(instance, SpiceKindIngredient::new));
    private static final DeferredRegister<IngredientType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, "solwarly");
    private static final DeferredHolder<IngredientType<?>, IngredientType<SpiceKindIngredient>> TYPE = TYPES.register("spice_kind", () -> new IngredientType<>(CODEC));
    public static void register(net.neoforged.bus.api.IEventBus bus) { TYPES.register(bus); }
    @Override public boolean test(ItemStack stack) {
        var kind = SpiceKind.from(flavor);
        return kind != null && stack.getItem() == SpiceRegistry.spice && Seasoning.components(stack).equals(java.util.List.of(kind));
    }
    @Override public Stream<ItemStack> getItems() {
        var kind = SpiceKind.from(flavor);
        return kind == null ? Stream.empty() : Stream.of(Seasoning.material(kind));
    }
    @Override public boolean isSimple() { return false; }
    @Override public IngredientType<?> getType() { return TYPE.get(); }
}
