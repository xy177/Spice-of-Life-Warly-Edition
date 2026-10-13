package com.xy177.solwarly.lunch;

import net.minecraft.world.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.items.ItemStackHandler;


/** Contents live in the item's normal NBT, so copies, upgrades and inventory packets preserve them. */
public final class LunchInventory extends ItemStackHandler {
    public static final String NBT_KEY = "LunchInventory";
    private final ItemStack container;

    public LunchInventory(ItemStack container) {
        super(((ItemLunchContainer) container.getItem()).slots);
        this.container = container;
        if (container.hasTag()) {
            CompoundTag contents = container.getTag().getCompound(NBT_KEY).copy();
            contents.putInt("Size", getSlots());
            deserializeNBT(contents);
        }
    }

    public static boolean canHold(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof ItemLunchContainer) return false;
        UseAnim action = stack.getUseAnimation();
        return (action == UseAnim.EAT || action == UseAnim.DRINK) && stack.isEdible();
    }

    @Override public boolean isItemValid(int slot, ItemStack stack) { return canHold(stack); }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return canHold(stack) ? super.insertItem(slot, stack, simulate) : stack;
    }
    @Override protected void onContentsChanged(int slot) {
        if (!container.hasTag()) container.setTag(new CompoundTag());
        container.getTag().put(NBT_KEY, serializeNBT());
    }
}
