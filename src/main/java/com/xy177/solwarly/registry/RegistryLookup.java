package com.xy177.solwarly.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.effect.MobEffect;

/** Registry lookups which do not silently turn missing optional ingredients into air. */
public final class RegistryLookup {
    public static Item item(String id) {
        ResourceLocation name=ResourceLocation.tryParse(id);
        return name!=null&&net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(name)?net.minecraft.core.registries.BuiltInRegistries.ITEM.get(name):null;
    }
    public static ResourceLocation id(Item item) { return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item); }
    public static ResourceLocation id(Block block) { return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block); }
    public static ResourceLocation id(net.minecraft.core.Holder<MobEffect> effect) { return net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.value()); }
    public static net.minecraft.core.Holder<MobEffect> effect(ResourceLocation id) { return net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getHolder(id).orElse(null); }
    public static net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute(ResourceLocation id) { return net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.getHolder(id).orElse(null); }
    private RegistryLookup() {}
}
