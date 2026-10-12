package com.xy177.solwarly.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import java.util.*;
import java.util.function.Function;

/** Flavor calculation hook shared by built-in seasoning and optional integrations. */
public final class FlavorAPI {
    private static final Map<ResourceLocation, Function<ItemStack, Flavor>> providers = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Function<ItemStack, Collection<ItemStack>>> variants = new LinkedHashMap<>();
    public static final Flavor PLAIN = new Flavor("plain", 1);

    public static void register(ResourceLocation id, Function<ItemStack, Flavor> provider) {
        if (id == null || provider == null) throw new IllegalArgumentException("Flavor provider is required");
        providers.put(id, provider);
    }

    public static Flavor resolve(ItemStack stack) {
        for (Function<ItemStack, Flavor> provider : providers.values()) {
            Flavor flavor = provider.apply(stack);
            if (flavor != null) return flavor;
        }
        return PLAIN;
    }
    /** Register on both sides alongside the resolver; return attainable versions of this plain food. */
    public static void registerVariants(ResourceLocation id, Function<ItemStack, Collection<ItemStack>> provider) {
        if (id == null || provider == null) throw new IllegalArgumentException("Flavor variant provider is required");
        variants.put(id, provider);
    }
    public static List<ItemStack> variants(ItemStack plainFood) {
        Map<String, ItemStack> result = new LinkedHashMap<>(); result.put(PLAIN.id, plainFood.copy());
        for (Function<ItemStack, Collection<ItemStack>> provider : variants.values())
            for (ItemStack variant : provider.apply(plainFood.copy()))
                if (!variant.isEmpty()) result.putIfAbsent(resolve(variant).id, variant.copy());
        return new ArrayList<>(result.values());
    }

    public static final class Flavor {
        public final String id;
        public final double diversityMultiplier;
        public Flavor(String id, double diversityMultiplier) {
            if (id == null || id.isEmpty() || !Double.isFinite(diversityMultiplier) || diversityMultiplier < 0)
                throw new IllegalArgumentException("Invalid flavor");
            this.id = id;
            this.diversityMultiplier = diversityMultiplier;
        }
    }
    private FlavorAPI() {}
}
