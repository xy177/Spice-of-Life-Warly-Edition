package com.xy177.solwarly.lunch;

import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;
import squeek.applecore.api.AppleCoreAPI;

/** Contents live in the item's normal NBT, so copies, upgrades and inventory packets preserve them. */
public final class LunchInventory extends ItemStackHandler {
    public static final String NBT_KEY = "LunchInventory";
    private final ItemStack container;

    public LunchInventory(ItemStack container) {
        super(((ItemLunchContainer) container.getItem()).slots);
        this.container = container;
        if (container.hasTagCompound()) {
            NBTTagCompound contents = container.getTagCompound().getCompoundTag(NBT_KEY).copy();
            contents.setInteger("Size", getSlots());
            deserializeNBT(contents);
        }
    }

    public static boolean canHold(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof ItemLunchContainer) return false;
        EnumAction action = stack.getItemUseAction();
        return (action == EnumAction.EAT || action == EnumAction.DRINK) && AppleCoreAPI.accessor.isFood(stack);
    }

    @Override public boolean isItemValid(int slot, ItemStack stack) { return canHold(stack); }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return canHold(stack) ? super.insertItem(slot, stack, simulate) : stack;
    }
    @Override protected void onContentsChanged(int slot) {
        if (!container.hasTagCompound()) container.setTagCompound(new NBTTagCompound());
        container.getTagCompound().setTag(NBT_KEY, serializeNBT());
    }
}
