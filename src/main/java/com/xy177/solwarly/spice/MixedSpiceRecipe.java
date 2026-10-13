package com.xy177.solwarly.spice;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;

import java.util.*;

public final class MixedSpiceRecipe extends CustomRecipe {
    public MixedSpiceRecipe(ResourceLocation id,CraftingBookCategory category) { super(id,category); }
    private List<SpiceKind> ingredients(CraftingContainer inventory) {
        List<SpiceKind> kinds = new ArrayList<>(); int catalysts = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i); if (stack.isEmpty()) continue;
            if (SpiceConfig.matchesMixedCatalyst(stack)) { catalysts++; continue; }
            List<SpiceKind> components = Seasoning.ingredient(stack);
            if (Seasoning.mixed(stack) || components.size() != 1 || (components.get(0).accompaniment() || components.get(0) == SpiceKind.CREATIVE)) return Collections.emptyList();
            kinds.add(components.get(0));
        }
        return catalysts == 1 && kinds.size() == SpiceConfig.mixedIngredients ? kinds : Collections.emptyList();
    }
    public boolean matches(CraftingContainer inventory, Level world) { return !ingredients(inventory).isEmpty(); }
    public ItemStack assemble(CraftingContainer inventory,RegistryAccess access) {
        List<SpiceKind> kinds = ingredients(inventory); return kinds.isEmpty() ? ItemStack.EMPTY : Seasoning.mixedPowder(kinds, SpiceConfig.mixedCount);
    }
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inventory) {
        NonNullList<ItemStack> result = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        if (matches(inventory, null)) for (int i = 0; i < result.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (SpiceConfig.matchesMixedCatalyst(stack)) {
                if (SpiceConfig.consumeMixedCatalyst) result.set(i, net.minecraftforge.common.ForgeHooks.getCraftingRemainingItem(stack));
                else { ItemStack returned = stack.copy(); returned.setCount(1); result.set(i, returned); }
            }
        }
        return result;
    }
    public boolean canCraftInDimensions(int width, int height) { return width * height >= SpiceConfig.mixedIngredients + 1; }
        public RecipeSerializer<?> getSerializer() { return SpiceRegistry.MIXED.get(); }
}
