package com.xy177.solwarly.spice;

import net.minecraft.world.item.ItemStack;
import java.util.*;

public final class SpiceRecipes {
    private static final List<SpiceRecipe> RECIPES = new ArrayList<>();
    public static void register(SpiceRecipe recipe) { RECIPES.add(recipe); }
    static void remove(SpiceRecipe recipe) { RECIPES.remove(recipe); }
    /** Single-ingredient lookup retained for custom recipe conflict checks. */
    public static SpiceRecipe find(ItemStack input) {
        for (var recipe : RECIPES) if (!recipe.dual() && recipe.matches(input)) return recipe;
        return null;
    }
    public static SpiceRecipe find(ItemStack first, ItemStack second, String binding, boolean singleOnly) {
        if (!singleOnly) for (var recipe : RECIPES) if (recipe.boundTo(binding) && recipe.dual() && recipe.ready(first, second)) return recipe;
        for (var recipe : RECIPES) if (recipe.boundTo(binding) && !recipe.dual() && recipe.ready(first, second)) return recipe;
        return null;
    }
    public static SpiceRecipe ingredient(ItemStack stack, ItemStack other, String binding, boolean singleOnly) {
        if (!singleOnly && !other.isEmpty()) for (var recipe : RECIPES)
            if (recipe.boundTo(binding) && recipe.matchesPair(stack, other)) return recipe;
        for (var recipe : RECIPES) if (recipe.boundTo(binding) && (!singleOnly || !recipe.dual()) && recipe.accepts(stack)) return recipe;
        return null;
    }
    public static boolean overlaps(ItemStack first) {
        return find(first) != null && RECIPES.stream().anyMatch(r -> r.dual() && r.accepts(first));
    }
    public static List<SpiceRecipe> all() { return Collections.unmodifiableList(RECIPES); }
    private SpiceRecipes() {}
}
