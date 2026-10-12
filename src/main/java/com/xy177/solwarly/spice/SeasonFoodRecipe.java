package com.xy177.solwarly.spice;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import squeek.applecore.api.AppleCoreAPI;
import java.util.*;

public final class SeasonFoodRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    private int[] slots(InventoryCrafting inventory) {
        List<Integer> occupied = new ArrayList<>();
        for (int i = 0; i < inventory.getSizeInventory(); i++) if (!inventory.getStackInSlot(i).isEmpty()) occupied.add(i);
        if (occupied.size() != 2) return null;
        for (int material : occupied) {
            ItemStack ingredient = inventory.getStackInSlot(material);
            boolean powder = ingredient.getItem() == SpiceRegistry.spice && !Seasoning.components(ingredient).isEmpty();
            if (!powder && !Seasoning.rice(ingredient)) continue;
            int food = occupied.get(0) == material ? occupied.get(1) : occupied.get(0);
            ItemStack target = inventory.getStackInSlot(food);
            if (!Seasoning.seasoned(target) && AppleCoreAPI.accessor.isFood(target)) return new int[] {food, material};
        }
        return null;
    }
    public boolean matches(InventoryCrafting inventory, World world) { return slots(inventory) != null; }
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        int[] slots = slots(inventory); if (slots == null) return ItemStack.EMPTY;
        ItemStack material = inventory.getStackInSlot(slots[1]);
        boolean rice = Seasoning.rice(material);
        return Seasoning.apply(inventory.getStackInSlot(slots[0]), rice
            ? Collections.singletonList(SpiceKind.RICE) : Seasoning.components(material),
            rice ? SpiceConfig.colors.get(SpiceKind.RICE) : ItemSpice.color(material, 0));
    }
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inventory) {
        NonNullList<ItemStack> result = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
        int[] slots = slots(inventory);
        if (slots != null && Seasoning.rice(inventory.getStackInSlot(slots[1]))) result.set(slots[1], new ItemStack(net.minecraft.init.Items.BOWL));
        return result;
    }
    public boolean canFit(int width, int height) { return width * height >= 2; }
    public ItemStack getRecipeOutput() { return ItemStack.EMPTY; }
    public boolean isDynamic() { return true; }
}
