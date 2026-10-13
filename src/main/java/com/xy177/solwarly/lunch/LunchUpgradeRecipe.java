// Warly Edition lunch-container adaptation; modifications by xy177 on 2026-10-13.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/tree/f38436cb11c5b8d628e6f20f7b1670dbc811592d/src/main/java/com/tarinoita/solsweetpotato/item/foodcontainer
// Original Apple Pie/Sweet Potato contributors retain copyright.
// Upstream Cyclic attribution: copyright Samson Basset (Lothrazar), MIT.
// See THIRD_PARTY_NOTICES.md, licenses/Cyclic-MIT.txt, and LICENSE.txt.
package com.xy177.solwarly.lunch;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.NonNullList;

/** Preserve contents and custom item data when upgrading a lunch container. */
public final class LunchUpgradeRecipe extends ShapedRecipes {
    private final ItemLunchContainer source;

    public LunchUpgradeRecipe(ItemLunchContainer source, Item metal, ItemLunchContainer target) {
        super("solwarly:lunch", 3, 3, ingredients(source, metal), new ItemStack(target));
        this.source = source;
    }
    private static NonNullList<Ingredient> ingredients(Item source, Item metal) {
        Ingredient ingot = Ingredient.fromItem(metal);
        return NonNullList.from(Ingredient.EMPTY, Ingredient.EMPTY, ingot, Ingredient.EMPTY,
            ingot, Ingredient.fromItem(source), ingot, ingot, ingot, ingot);
    }
    @Override public ItemStack getCraftingResult(InventoryCrafting inventory) {
        ItemStack result = getRecipeOutput().copy();
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack input = inventory.getStackInSlot(slot);
            if (input.getItem() == source && input.hasTagCompound()) result.setTagCompound(input.getTagCompound().copy());
        }
        return result;
    }
}
