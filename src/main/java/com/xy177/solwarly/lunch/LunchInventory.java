// Warly Edition lunch-container adaptation; modifications by xy177 on 2026-10-13.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/tree/f38436cb11c5b8d628e6f20f7b1670dbc811592d/src/main/java/com/tarinoita/solsweetpotato/item/foodcontainer
// Original Apple Pie/Sweet Potato contributors retain copyright.
// Upstream Cyclic attribution: copyright Samson Basset (Lothrazar), MIT.
// See THIRD_PARTY_NOTICES.md, licenses/Cyclic-MIT.txt, and LICENSE.txt.
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
