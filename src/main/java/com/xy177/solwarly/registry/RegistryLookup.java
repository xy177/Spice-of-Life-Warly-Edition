package com.xy177.solwarly.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

/** Registry lookups which do not silently turn missing optional ingredients into air. */
public final class RegistryLookup {
    public static Item item(String id) {
        ResourceLocation name=ResourceLocation.tryParse(id);
        return name!=null&&ForgeRegistries.ITEMS.containsKey(name)?ForgeRegistries.ITEMS.getValue(name):null;
    }
    public static ResourceLocation id(Item item) { return ForgeRegistries.ITEMS.getKey(item); }
    public static ResourceLocation id(Block block) { return ForgeRegistries.BLOCKS.getKey(block); }
    public static ResourceLocation id(MobEffect effect) { return ForgeRegistries.MOB_EFFECTS.getKey(effect); }
    public static MobEffect effect(ResourceLocation id) { return ForgeRegistries.MOB_EFFECTS.getValue(id); }
    private RegistryLookup() {}
}
