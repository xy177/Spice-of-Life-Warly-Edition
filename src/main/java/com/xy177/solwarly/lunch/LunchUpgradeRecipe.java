package com.xy177.solwarly.lunch;

import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public final class LunchUpgradeRecipe extends CustomRecipe {
    public LunchUpgradeRecipe(ResourceLocation id,CraftingBookCategory category) { super(id,category); }
    private ItemStack source(CraftingContainer inventory) {
        if(inventory.getWidth()!=3||inventory.getHeight()!=3)return ItemStack.EMPTY;
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
    @Override public boolean matches(CraftingContainer inventory,Level level) { return !source(inventory).isEmpty(); }
    @Override public ItemStack assemble(CraftingContainer inventory,RegistryAccess access) {
        ItemStack source=source(inventory);if(source.isEmpty())return ItemStack.EMPTY;
        ItemStack result=new ItemStack(source.getItem()==LunchRegistry.BAG.get()?LunchRegistry.BOX.get():LunchRegistry.GOLD.get());
        if(source.hasTag())result.setTag(source.getTag().copy());return result;
    }
    @Override public boolean canCraftInDimensions(int width,int height) { return width>=3&&height>=3; }
    @Override public RecipeSerializer<?> getSerializer() { return LunchRegistry.UPGRADE.get(); }
}
