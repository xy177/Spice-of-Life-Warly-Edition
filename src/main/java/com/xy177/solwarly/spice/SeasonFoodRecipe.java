package com.xy177.solwarly.spice;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;
import java.util.*;

public final class SeasonFoodRecipe extends CustomRecipe {
    public SeasonFoodRecipe(ResourceLocation id, CraftingBookCategory category) { super(id, category); }
    private List<Integer> occupied(CraftingContainer inventory) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) if (!inventory.getItem(i).isEmpty()) slots.add(i);
        return slots;
    }
    private List<Integer> orderedSlots(CraftingContainer inventory) {
        var slots = occupied(inventory);
        // Powders identify the target uniquely; accompaniment-to-accompaniment recipes keep the top-left rule.
        slots.sort(Comparator.comparingInt(slot -> {
            ItemStack stack = inventory.getItem(slot);
            var kinds = Seasoning.ingredient(stack);
            return kinds.isEmpty() ? 0 : Seasoning.accompaniments(kinds) && (stack.isEdible()) ? 1 : 2;
        }));
        return slots;
    }
    private List<SpiceKind> ingredients(CraftingContainer inventory) {
        var slots = orderedSlots(inventory);
        if (slots.size() < 2 || slots.size() > 9) return List.of();
        ItemStack target = inventory.getItem(slots.get(0));
        if (!target.isEdible()) return List.of();
        var existing = Seasoning.components(target);
        if (Seasoning.seasoned(target) && existing.isEmpty()) return Collections.emptyList();
        List<SpiceKind> kinds = existing;
        for (int i = 1; i < slots.size(); i++) {
            List<SpiceKind> added = Seasoning.ingredient(inventory.getItem(slots.get(i)));
            if (!Seasoning.canAdd(kinds, added)) return Collections.emptyList();
            kinds = Seasoning.combine(kinds, added);
        }
        return kinds;
    }
    public boolean matches(CraftingContainer inventory, Level world) { return !ingredients(inventory).isEmpty(); }
    public ItemStack assemble(CraftingContainer inventory, RegistryAccess access) {
        var kinds = ingredients(inventory);
        if (kinds.isEmpty()) return ItemStack.EMPTY;
        var slots = orderedSlots(inventory);
        ItemStack target = inventory.getItem(slots.get(0));
        List<SpiceKind> before = Seasoning.powders(Seasoning.components(target)), after = Seasoning.powders(kinds);
        int color = Seasoning.color(after.isEmpty() ? kinds : after);
        if (Seasoning.powderUses(before) == Seasoning.powderUses(after) && !before.isEmpty()) color = Seasoning.color(target);
        else if (before.isEmpty() && Seasoning.powderUses(after) == 1) {
            for (int slot : slots.subList(1, slots.size())) {
                ItemStack powder = inventory.getItem(slot);
                if (powder.getItem() == SpiceRegistry.spice) { color = Seasoning.color(powder); break; }
            }
        }
        return Seasoning.apply(Seasoning.plain(inventory.getItem(slots.get(0))), kinds, color);
    }
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inventory) {
        var result = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        if (!ingredients(inventory).isEmpty()) {
            var slots = orderedSlots(inventory);
            for (int i = 1; i < slots.size(); i++) {
                ItemStack material = inventory.getItem(slots.get(i));
                var added = Seasoning.ingredient(material);
                result.set(slots.get(i), added.size() == 1 && added.get(0).accompaniment() && added.get(0) instanceof CustomSpice
                    ? ((CustomSpice) added.get(0)).returned() : net.minecraftforge.common.ForgeHooks.getCraftingRemainingItem(material));
            }
        }
        return result;
    }
    public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }
    public RecipeSerializer<?> getSerializer() { return SpiceRegistry.SEASON.get(); }
}
