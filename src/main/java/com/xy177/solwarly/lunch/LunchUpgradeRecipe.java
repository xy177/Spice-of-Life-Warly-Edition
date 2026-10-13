// Warly Edition lunch-container adaptation; modifications by xy177 on 2026-10-13.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/tree/f38436cb11c5b8d628e6f20f7b1670dbc811592d/src/main/java/com/tarinoita/solsweetpotato/item/foodcontainer
// Original Apple Pie/Sweet Potato contributors retain copyright.
// Upstream Cyclic attribution: copyright Samson Basset (Lothrazar), MIT.
// See THIRD_PARTY_NOTICES.md, licenses/Cyclic-MIT.txt, and LICENSE.txt.
package com.xy177.solwarly.lunch;

import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public final class LunchUpgradeRecipe extends CustomRecipe {
    public LunchUpgradeRecipe(CraftingBookCategory category) { super(category); }
    private ItemStack source(CraftingInput inventory) {
        if(inventory.width()!=3||inventory.height()!=3)return ItemStack.EMPTY;
        ItemStack source=inventory.getItem(4);Item metal;
        if(source.getItem()==LunchRegistry.BAG.get())metal=Items.IRON_INGOT;
        else if(source.getItem()==LunchRegistry.BOX.get())metal=Items.GOLD_INGOT;else return ItemStack.EMPTY;
        for(int slot=0;slot<9;slot++) {
            if(slot==4)continue;
            if(slot==0||slot==2) { if(!inventory.getItem(slot).isEmpty())return ItemStack.EMPTY; }
            else if(inventory.getItem(slot).getItem()!=metal)return ItemStack.EMPTY;
        }
        return source;
    }
    @Override public boolean matches(CraftingInput inventory,Level level) { return !source(inventory).isEmpty(); }
    @Override public ItemStack assemble(CraftingInput inventory,net.minecraft.core.HolderLookup.Provider access) {
        ItemStack source=source(inventory);if(source.isEmpty())return ItemStack.EMPTY;
        ItemStack result=new ItemStack(source.getItem()==LunchRegistry.BAG.get()?(Item)LunchRegistry.BOX.get():LunchRegistry.GOLD.get());
        result.applyComponents(source.getComponents());return result;
    }
    @Override public boolean canCraftInDimensions(int width,int height) { return width>=3&&height>=3; }
    @Override public RecipeSerializer<?> getSerializer() { return LunchRegistry.UPGRADE.get(); }
}
