package com.xy177.solwarly.spice;

import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtils;
import java.util.*;

public final class BuiltinSpiceRecipes {
    private static final List<SpiceRecipe> registered = new ArrayList<>();
    public static void reload() {
        if (SpiceConfig.mixedCatalyst().isEmpty()) com.xy177.solwarly.SOLWarly.LOGGER.warn(
            "Invalid mixed spice catalyst {}@{}; mixing is disabled.", SpiceConfig.mixedCatalystItem, SpiceConfig.mixedCatalystMetadata);
        registered.forEach(SpiceRecipes::remove); registered.clear();
        add("sugar", new ItemStack(Items.SUGAR), 3, SpiceKind.SUGAR, 1, ItemStack.EMPTY);
        add("reeds", new ItemStack(Items.REEDS), 3, SpiceKind.SUGAR, 1, ItemStack.EMPTY);
        add("beetroot", new ItemStack(Items.BEETROOT), 3, SpiceKind.SUGAR, 1, ItemStack.EMPTY);
        add("honey", optional("farmers_future_delight:honey_bottle"), 3, SpiceKind.SUGAR, 1, new ItemStack(Items.GLASS_BOTTLE));
        add("water_bottle", PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER), 1, SpiceKind.SALT, 1, new ItemStack(Items.GLASS_BOTTLE));
        add("water_bucket", new ItemStack(Items.WATER_BUCKET), 1, SpiceKind.SALT, 3, new ItemStack(Items.BUCKET));
        add("tomato", optional("farmersdelight:tomato"), 3, SpiceKind.TOMATO, 1, ItemStack.EMPTY);
        add("onion", optional("farmersdelight:onion"), 3, SpiceKind.ONION, 1, ItemStack.EMPTY);
        add("ham", optional("farmersdelight:smoked_ham"), 1, SpiceKind.HAM, 2, ItemStack.EMPTY);
        add("beef", new ItemStack(Items.COOKED_BEEF), 2, SpiceKind.BEEF, 3, ItemStack.EMPTY);
        add("spicy_blaze", new ItemStack(Items.BLAZE_POWDER), 3, SpiceKind.SPICY, 1, ItemStack.EMPTY);
        add("spicy_propeller", optional("nethers_delight_legacy:propelpearl"), 3, SpiceKind.SPICY, 1, ItemStack.EMPTY);
        add("chorus_fruit", optional("minecraft:chorus_fruit"), 3, SpiceKind.CHORUS, 1, ItemStack.EMPTY);
        add("chorus_grain", optional("ends_delight:chorus_fruit_grain"), 3, SpiceKind.CHORUS, 1, ItemStack.EMPTY);
        add("dragon_raw", optional("ends_delight:raw_dragon_meat"), 1, SpiceKind.DRAGON, 2, ItemStack.EMPTY);
        add("dragon_roasted", optional("ends_delight:roasted_dragon_meat"), 1, SpiceKind.DRAGON, 2, ItemStack.EMPTY);
        add("dragon_raw_cuts", optional("ends_delight:raw_dragon_meat_cuts"), 1, SpiceKind.DRAGON, 2, ItemStack.EMPTY);
        add("dragon_roasted_cuts", optional("ends_delight:roasted_dragon_meat_cuts"), 1, SpiceKind.DRAGON, 2, ItemStack.EMPTY);
        for (SpiceKind kind : SpiceKind.values()) if (kind.fruit())
            add("fruit_" + kind.fruitId(), optional("fruits_delight_legacy:" + kind.fruitId() + "_jelly"), 1, kind, 1, new ItemStack(Items.GLASS_BOTTLE));
        add("pickle_kimchi", optional("brewinandchewinlegacy:kimchi"), 2, SpiceKind.PICKLE, 1, ItemStack.EMPTY);
        add("pickle_pickled", optional("brewinandchewinlegacy:pickled_pickles"), 2, SpiceKind.PICKLE, 1, ItemStack.EMPTY);
        add("cheese_flan", optional("brewinandchewinlegacy:flaxen_cheese_wedge"), 1, SpiceKind.CHEESE, 2, ItemStack.EMPTY);
        add("cheese_scarlet", optional("brewinandchewinlegacy:scarlet_cheese_wedge"), 1, SpiceKind.CHEESE, 2, ItemStack.EMPTY);
        add("cave_carrot", optional("miners_delight:cave_carrot"), 2, SpiceKind.CARROT, 1, ItemStack.EMPTY);
        add("searing", optional("twilightforest:fiery_ingot"), 1, SpiceKind.SEARING, 3, ItemStack.EMPTY);
        add("toxic", optional("twilightforest:steeleaf_ingot"), 1, SpiceKind.TOXIC, 1, ItemStack.EMPTY);
        add("frost", optional("twilightforest:ice_bomb"), 1, SpiceKind.FROST, 1, ItemStack.EMPTY);
        add("mushroom", optional("twilight_spark_delight:labyrinth_mushroom"), 2, SpiceKind.MUSHROOM, 1, ItemStack.EMPTY);
        add("experiment", optional("twilight_spark_delight:experiment_000"), 1, SpiceKind.EXPERIMENT, 1, ItemStack.EMPTY);
        add("mystery", optional("twilight_spark_delight:quest_ram_cheese"), 3, SpiceKind.MYSTERY, 1, ItemStack.EMPTY);
        add("deep_tear", optional("twilight_spark_delight:experiment_234"), 1, SpiceKind.DEEP_TEAR, 3, ItemStack.EMPTY);
        SpiceConfig.save();
    }
    private static ItemStack optional(String id) { Item item = Item.getByNameOrId(id); return item == null ? ItemStack.EMPTY : new ItemStack(item); }
    private static void add(String name, ItemStack input, int x, SpiceKind kind, int y, ItemStack container) {
        if (!kind.available()) return;
        int inputs = SpiceConfig.recipeCount(name, true, x, input.isEmpty() ? 64 : input.getMaxStackSize());
        int outputs = SpiceConfig.recipeCount(name, false, y, 64);
        if (input.isEmpty()) return;
        SpiceRecipe recipe = new SpiceRecipe(input, inputs, Seasoning.powder(Collections.singletonList(kind), 1), outputs, container);
        registered.add(recipe); SpiceRecipes.register(recipe);
    }
    private BuiltinSpiceRecipes() {}
}
