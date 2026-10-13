package com.xy177.solwarly.spice;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import java.util.*;

public final class MixedSpiceRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    private List<SpiceKind> ingredients(InventoryCrafting inventory) {
        List<SpiceKind> kinds = new ArrayList<>(); int catalysts = 0;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i); if (stack.isEmpty()) continue;
            if (SpiceConfig.matchesMixedCatalyst(stack)) { catalysts++; continue; }
            List<SpiceKind> components = Seasoning.components(stack);
            if (stack.getItem() != SpiceRegistry.spice || Seasoning.mixed(stack) || components.size() != 1 || components.stream().anyMatch(SpiceKind::accompaniment) || components.contains(SpiceKind.CREATIVE)) return Collections.emptyList();
            kinds.add(components.get(0));
        }
        return catalysts == 1 && kinds.size() == SpiceConfig.mixedIngredients ? kinds : Collections.emptyList();
    }
    public boolean matches(InventoryCrafting inventory, World world) { return !ingredients(inventory).isEmpty(); }
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        List<SpiceKind> kinds = ingredients(inventory); return kinds.isEmpty() ? ItemStack.EMPTY : Seasoning.mixedPowder(kinds, SpiceConfig.mixedCount);
    }
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inventory) {
        NonNullList<ItemStack> result = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
        if (matches(inventory, null)) for (int i = 0; i < result.size(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (SpiceConfig.matchesMixedCatalyst(stack)) {
                if (SpiceConfig.consumeMixedCatalyst) result.set(i, net.minecraftforge.common.ForgeHooks.getContainerItem(stack));
                else { ItemStack returned = stack.copy(); returned.setCount(1); result.set(i, returned); }
            }
        }
        return result;
    }
    public boolean canFit(int width, int height) { return width * height >= SpiceConfig.mixedIngredients + 1; }
    public ItemStack getRecipeOutput() { return ItemStack.EMPTY; }
    public boolean isDynamic() { return true; }
}
