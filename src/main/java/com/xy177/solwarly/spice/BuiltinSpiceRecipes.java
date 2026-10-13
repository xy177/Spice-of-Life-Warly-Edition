package com.xy177.solwarly.spice;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import java.util.*;

public final class BuiltinSpiceRecipes {
    private static final List<SpiceRecipe> registered = new ArrayList<>();
    public static void reload() {
        if (SpiceConfig.mixedCatalyst().isEmpty()) com.xy177.solwarly.SOLWarly.LOGGER.warn(
            "Invalid mixed spice catalyst {}@{}; mixing is disabled.", SpiceConfig.mixedCatalystItem, SpiceConfig.mixedCatalystMetadata);
        registered.forEach(SpiceRecipes::remove); registered.clear();
        add("sugar", new ItemStack(Items.SUGAR), 3, SpiceKind.SUGAR, 1, ItemStack.EMPTY);
        add("reeds", new ItemStack(Items.SUGAR_CANE), 3, SpiceKind.SUGAR, 1, ItemStack.EMPTY);
        add("beetroot", new ItemStack(Items.BEETROOT), 3, SpiceKind.SUGAR, 1, ItemStack.EMPTY);
        add("honey", optional("minecraft:honey_bottle"), 3, SpiceKind.SUGAR, 1, new ItemStack(Items.GLASS_BOTTLE));
        add("water_bottle", PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), 1, SpiceKind.SALT, 1, new ItemStack(Items.GLASS_BOTTLE));
        add("water_bucket", new ItemStack(Items.WATER_BUCKET), 1, SpiceKind.SALT, 3, new ItemStack(Items.BUCKET));
        add("tomato", optional("farmersdelight:tomato"), 3, SpiceKind.TOMATO, 1, ItemStack.EMPTY);
        add("onion", optional("farmersdelight:onion"), 3, SpiceKind.ONION, 1, ItemStack.EMPTY);
        add("ham", optional("farmersdelight:smoked_ham"), 1, SpiceKind.HAM, 2, ItemStack.EMPTY);
        add("beef", new ItemStack(Items.COOKED_BEEF), 2, SpiceKind.BEEF, 3, ItemStack.EMPTY);
        add("spicy_blaze", new ItemStack(Items.BLAZE_POWDER), 3, SpiceKind.SPICY, 1, ItemStack.EMPTY);
        add("spicy_propeller", optional(SpiceKind.spicyIngredientId()), 3, SpiceKind.SPICY, 1, ItemStack.EMPTY);
        add("chorus_fruit", optional("minecraft:chorus_fruit"), 3, SpiceKind.CHORUS, 1, ItemStack.EMPTY);
        add("chorus_grain", optional("ends_delight:chorus_fruit_grain"), 3, SpiceKind.CHORUS, 1, ItemStack.EMPTY);
        add("dragon_raw", optional("ends_delight:raw_dragon_meat"), 1, SpiceKind.DRAGON, 2, ItemStack.EMPTY);
        add("dragon_roasted", optional("ends_delight:roasted_dragon_meat"), 1, SpiceKind.DRAGON, 2, ItemStack.EMPTY);
        add("dragon_raw_cuts", optional("ends_delight:raw_dragon_meat_cuts"), 1, SpiceKind.DRAGON, 2, ItemStack.EMPTY);
        add("dragon_roasted_cuts", optional("ends_delight:roasted_dragon_meat_cuts"), 1, SpiceKind.DRAGON, 2, ItemStack.EMPTY);
        for (SpiceKind kind : SpiceKind.values()) if (kind.fruit())
            add("fruit_" + kind.fruitId(), optional("fruitsdelight:" + kind.fruitId() + "_jelly"), 1, kind, 1, new ItemStack(Items.GLASS_BOTTLE));
        add("pickle_kimchi", optional("brewinandchewin:kimchi"), 2, SpiceKind.PICKLE, 1, ItemStack.EMPTY);
        add("pickle_pickled", optional("brewinandchewin:pickled_pickles"), 2, SpiceKind.PICKLE, 1, ItemStack.EMPTY);
        add("cheese_flan", optional("brewinandchewin:flaxen_cheese_wedge"), 1, SpiceKind.CHEESE, 2, ItemStack.EMPTY);
        add("cheese_scarlet", optional("brewinandchewin:scarlet_cheese_wedge"), 1, SpiceKind.CHEESE, 2, ItemStack.EMPTY);
        add("cave_carrot", optional("miners_delight:cave_carrot"), 2, SpiceKind.CARROT, 1, ItemStack.EMPTY);
        add("searing", optional("twilightforest:fiery_ingot"), 1, SpiceKind.SEARING, 3, ItemStack.EMPTY);
        add("toxic", optional("twilightforest:steeleaf_ingot"), 1, SpiceKind.TOXIC, 1, ItemStack.EMPTY);
        add("frost", optional("twilightforest:ice_bomb"), 1, SpiceKind.FROST, 1, ItemStack.EMPTY);
        add("mushroom", optional("twilight_spark_delight:labyrinth_mushroom"), 2, SpiceKind.MUSHROOM, 1, ItemStack.EMPTY);
        add("experiment", optional("twilight_spark_delight:experiment_000"), 1, SpiceKind.EXPERIMENT, 1, ItemStack.EMPTY);
        add("mystery", optional("twilight_spark_delight:quest_ram_cheese"), 3, SpiceKind.MYSTERY, 1, ItemStack.EMPTY);
        add("deep_tear", optional("twilight_spark_delight:experiment_234"), 1, SpiceKind.DEEP_TEAR, 3, ItemStack.EMPTY);
        add("cinema", optional("immortalers_delight:popolutcorn"), 1, SpiceKind.CINEMA, 3, container(optional("immortalers_delight:popolutcorn")));
        add("pod_husk", optional("immortalers_delight:pitcher_pod_petal"), 2, SpiceKind.POD_HUSK, 3, container(optional("immortalers_delight:pitcher_pod_petal")));
        add("alfalfa", optional("immortalers_delight:alfalfa"), 3, SpiceKind.ALFALFA, 2, container(optional("immortalers_delight:alfalfa")));
        add("evolutcorn", optional("immortalers_delight:evolutcorn"), 3, SpiceKind.EVOLUTCORN, 2, container(optional("immortalers_delight:evolutcorn")));
        add("himekaido", optional("immortalers_delight:himekaido"), 2, SpiceKind.HIMEKAIDO, 1, container(optional("immortalers_delight:himekaido")));
        add("pearlipearl", optional("immortalers_delight:pearlipearl"), 1, SpiceKind.PEARLIPEARL, 1, container(optional("immortalers_delight:pearlipearl")));
        add("leisamboo", optional("immortalers_delight:leisamboo_tea"), 1, SpiceKind.LEISAMBOO, 2, container(optional("immortalers_delight:leisamboo_tea")));
        add("powdered_soul", optional("immortalers_delight:warped_laurel"), 1, SpiceKind.POWDERED_SOUL, 1, container(optional("immortalers_delight:warped_laurel")));
        add("conqueror", optional("minecraft:pitcher_plant"), 2, SpiceKind.CONQUEROR, 3, container(optional("minecraft:pitcher_plant")));
        add("gelpitaya_flesh", optional("immortalers_delight:gelpitaya_flesh"), 1, SpiceKind.GELPITAYA, 3, container(optional("immortalers_delight:gelpitaya_flesh")));
        add("gelpitaya_fruit", optional("immortalers_delight:gelpitaya"), 1, SpiceKind.GELPITAYA, 8, container(optional("immortalers_delight:gelpitaya")));
        add("moonlit", optional("immortalers_delight:sextlotus_root"), 2, SpiceKind.MOONLIT, 3, container(optional("immortalers_delight:sextlotus_root")));
        add("walnut", optional("immortalers_delight:obsidian_walnut_kernel"), 1, SpiceKind.WALNUT, 2, container(optional("immortalers_delight:obsidian_walnut_kernel")));
        addDual("poisonous_potato", optional("minecraft:poisonous_potato"), optional("immortalers_delight:himekaido"), SpiceKind.POISONOUS_POTATO);
        addDual("spider_eye", optional("minecraft:spider_eye"), optional("immortalers_delight:himekaido"), SpiceKind.SPIDER_EYE);
        addDual("pufferfish", optional("minecraft:pufferfish"), optional("immortalers_delight:himekaido"), SpiceKind.PUFFERFISH);
        addDual("miracle", optional("minecraft:gold_ingot"), optional("immortalers_delight:himekaido"), SpiceKind.MIRACLE);
        SpiceConfig.save();
    }
    private static ItemStack optional(String id) { Item item = com.xy177.solwarly.registry.RegistryLookup.item(id); return item == null ? ItemStack.EMPTY : new ItemStack(item); }
    private static void add(String name, ItemStack input, int x, SpiceKind kind, int y, ItemStack container) {
        if (!kind.available()) return;
        int inputs = SpiceConfig.recipeCount(name, true, x, input.isEmpty() ? 64 : input.getMaxStackSize());
        int outputs = SpiceConfig.recipeCount(name, false, y, 64);
        if (input.isEmpty()) return;
        SpiceRecipe recipe = new SpiceRecipe(input, inputs, Seasoning.powder(Collections.singletonList(kind), 1), outputs, container);
        registered.add(recipe); SpiceRecipes.register(recipe);
    }
    private static ItemStack container(ItemStack stack) { return stack.isEmpty() ? ItemStack.EMPTY : net.minecraftforge.common.ForgeHooks.getCraftingRemainingItem(stack); }
    private static void addDual(String name, ItemStack first, ItemStack second, SpiceKind kind) {
        if (first.isEmpty() || second.isEmpty() || !kind.available()) return;
        int x = SpiceConfig.recipeCount(name, true, 1, first.getMaxStackSize());
        int z = SpiceConfig.recipeCount(name + "_second", true, 1, second.getMaxStackSize());
        int y = SpiceConfig.recipeCount(name, false, 1, 64);
        var recipe = new SpiceRecipe(first, x, second, z, Seasoning.powder(List.of(kind), 1), y, container(first), container(second));
        registered.add(recipe); SpiceRecipes.register(recipe);
    }
    private BuiltinSpiceRecipes() {}
}
