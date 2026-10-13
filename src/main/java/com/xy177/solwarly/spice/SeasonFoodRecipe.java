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
    private List<Integer> occupied(InventoryCrafting inventory) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < inventory.getSizeInventory(); i++) if (!inventory.getStackInSlot(i).isEmpty()) slots.add(i);
        return slots;
    }
    private List<Integer> orderedSlots(InventoryCrafting inventory) {
        List<Integer> slots = occupied(inventory);
        // Powders identify the target uniquely; accompaniment-to-accompaniment recipes keep the top-left rule.
        slots.sort(Comparator.comparingInt(slot -> {
            ItemStack stack = inventory.getStackInSlot(slot);
            List<SpiceKind> kinds = Seasoning.ingredient(stack);
            return kinds.isEmpty() ? 0 : Seasoning.accompaniments(kinds) && (AppleCoreAPI.accessor.isFood(stack)) ? 1 : 2;
        }));
        return slots;
    }
    private List<SpiceKind> ingredients(InventoryCrafting inventory) {
        List<Integer> slots = orderedSlots(inventory);
        if (slots.size() < 2 || slots.size() > 9) return Collections.emptyList();
        ItemStack target = inventory.getStackInSlot(slots.get(0));
        if (!AppleCoreAPI.accessor.isFood(target)) return Collections.emptyList();
        List<SpiceKind> existing = Seasoning.components(target);
        if (Seasoning.seasoned(target) && existing.isEmpty()) return Collections.emptyList();
        List<SpiceKind> kinds = existing;
        for (int i = 1; i < slots.size(); i++) {
            List<SpiceKind> added = Seasoning.ingredient(inventory.getStackInSlot(slots.get(i)));
            if (!Seasoning.canAdd(kinds, added)) return Collections.emptyList();
            kinds = Seasoning.combine(kinds, added);
        }
        return kinds;
    }
    public boolean matches(InventoryCrafting inventory, World world) { return !ingredients(inventory).isEmpty(); }
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        List<SpiceKind> kinds = ingredients(inventory);
        if (kinds.isEmpty()) return ItemStack.EMPTY;
        List<Integer> slots = orderedSlots(inventory);
        ItemStack target = inventory.getStackInSlot(slots.get(0));
        List<SpiceKind> before = Seasoning.powders(Seasoning.components(target)), after = Seasoning.powders(kinds);
        int color = Seasoning.color(after.isEmpty() ? kinds : after);
        if (Seasoning.powderUses(before) == Seasoning.powderUses(after) && !before.isEmpty()) color = Seasoning.color(target);
        else if (before.isEmpty() && Seasoning.powderUses(after) == 1) {
            for (int slot : slots.subList(1, slots.size())) {
                ItemStack powder = inventory.getStackInSlot(slot);
                if (powder.getItem() == SpiceRegistry.spice) { color = Seasoning.color(powder); break; }
            }
        }
        return Seasoning.apply(Seasoning.plain(inventory.getStackInSlot(slots.get(0))), kinds, color);
    }
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inventory) {
        NonNullList<ItemStack> result = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
        if (!ingredients(inventory).isEmpty()) {
            List<Integer> slots = orderedSlots(inventory);
            for (int i = 1; i < slots.size(); i++) {
                ItemStack ingredient = inventory.getStackInSlot(slots.get(i));
                List<SpiceKind> added = Seasoning.ingredient(ingredient);
                result.set(slots.get(i), added.size() == 1 && added.get(0).accompaniment() && added.get(0) instanceof CustomSpice
                    ? ((CustomSpice) added.get(0)).returned() : Seasoning.rice(ingredient) ? new ItemStack(net.minecraft.init.Items.BOWL)
                    : net.minecraftforge.common.ForgeHooks.getContainerItem(ingredient));
            }
        }
        return result;
    }
    public boolean canFit(int width, int height) { return width * height >= 2; }
    public ItemStack getRecipeOutput() { return ItemStack.EMPTY; }
    public boolean isDynamic() { return true; }
}
