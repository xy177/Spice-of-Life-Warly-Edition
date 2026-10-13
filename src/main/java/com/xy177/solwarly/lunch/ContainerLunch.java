// Warly Edition lunch-container adaptation; modifications by xy177 on 2026-10-13.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/tree/f38436cb11c5b8d628e6f20f7b1670dbc811592d/src/main/java/com/tarinoita/solsweetpotato/item/foodcontainer
// Original Apple Pie/Sweet Potato contributors retain copyright.
// Upstream Cyclic attribution: copyright Samson Basset (Lothrazar), MIT.
// See THIRD_PARTY_NOTICES.md, licenses/Cyclic-MIT.txt, and LICENSE.txt.
package com.xy177.solwarly.lunch;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public final class ContainerLunch extends Container {
    public final ItemStack containerItem;
    public final int slots;
    private final int boundSlot;

    public ContainerLunch(EntityPlayer player, int boundSlot) {
        this.boundSlot = boundSlot;
        containerItem = player.inventory.getStackInSlot(boundSlot);
        LunchInventory contents = new LunchInventory(containerItem);
        slots = contents.getSlots();
        int columns = slots > 9 ? slots / 2 : slots;
        int left = (176 - columns * 18) / 2;
        int top = slots > 9 ? 29 : 35;
        for (int i = 0; i < slots; i++) {
            addSlotToContainer(new SlotItemHandler(contents, i, left + i % columns * 18, top + i / columns * 18) {
                @Override public void onSlotChanged() {
                    // Vanilla merging changes stack counts directly, bypassing ItemStackHandler's callbacks.
                    contents.setStackInSlot(getSlotIndex(), getStack());
                }
            });
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addPlayerSlot(player, 9 + row * 9 + col, 8 + col * 18, 84 + row * 18);
        for (int col = 0; col < 9; col++) addPlayerSlot(player, col, 8 + col * 18, 142);
    }
    private void addPlayerSlot(EntityPlayer player, int index, int x, int y) {
        addSlotToContainer(new Slot(player.inventory, index, x, y) {
            @Override public boolean canTakeStack(EntityPlayer player) { return index != boundSlot; }
            @Override public boolean isItemValid(ItemStack stack) { return index != boundSlot; }
        });
    }
    public static boolean validSlot(EntityPlayer player, int slot) {
        return (slot >= 0 && slot < 9 || slot == 40)
            && player.inventory.getStackInSlot(slot).getItem() instanceof ItemLunchContainer;
    }
    @Override public boolean canInteractWith(EntityPlayer player) {
        return player.inventory.getStackInSlot(boundSlot) == containerItem;
    }
    @Override public ItemStack slotClick(int slot, int button, ClickType type, EntityPlayer player) {
        if (type == ClickType.SWAP && button == boundSlot) return ItemStack.EMPTY;
        if (slot >= 0 && slot < inventorySlots.size()) {
            Slot clicked = inventorySlots.get(slot);
            if (clicked.inventory == player.inventory && clicked.getSlotIndex() == boundSlot) return ItemStack.EMPTY;
        }
        return super.slotClick(slot, button, type, player);
    }
    @Override public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index < 0 || index >= inventorySlots.size()) return ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (!slot.getHasStack() || !slot.canTakeStack(player)) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack(), original = stack.copy();
        if (index < slots) {
            if (!mergeItemStack(stack, slots, inventorySlots.size(), true)) return ItemStack.EMPTY;
        } else if (!LunchInventory.canHold(stack) || !mergeItemStack(stack, 0, slots, false)) return ItemStack.EMPTY;
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY); else slot.onSlotChanged();
        slot.onTake(player, stack);
        return original;
    }
}
