package com.xy177.solwarly.spice;

import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Registration point for future spice materials and their processing ratios. */
public final class SpiceRecipes {
    private static final List<SpiceRecipe> RECIPES = new ArrayList<>();

    public static void register(SpiceRecipe recipe) { RECIPES.add(recipe); }
    static void remove(SpiceRecipe recipe) { RECIPES.remove(recipe); }
    public static SpiceRecipe find(ItemStack input) {
        for (SpiceRecipe recipe : RECIPES) if (recipe.matches(input)) return recipe;
        return null;
    }
    public static List<SpiceRecipe> all() { return Collections.unmodifiableList(RECIPES); }
    private SpiceRecipes() {}
}
