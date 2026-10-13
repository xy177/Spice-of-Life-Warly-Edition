// Warly Edition lunch-container adaptation; modifications by xy177 on 2026-10-13.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/tree/f38436cb11c5b8d628e6f20f7b1670dbc811592d/src/main/java/com/tarinoita/solsweetpotato/item/foodcontainer
// Original Apple Pie/Sweet Potato contributors retain copyright.
// Upstream Cyclic attribution: copyright Samson Basset (Lothrazar), MIT.
// See THIRD_PARTY_NOTICES.md, licenses/Cyclic-MIT.txt, and LICENSE.txt.
package com.xy177.solwarly.lunch;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public final class ContainerLunch extends AbstractContainerMenu {
    public final ItemStack containerItem;
    public final int size;
    private final int boundSlot;

    public ContainerLunch(int id,Player player, int boundSlot) {
        super(LunchRegistry.MENU.get(),id);
        this.boundSlot = boundSlot;
        containerItem = player.getInventory().getItem(boundSlot);
        LunchInventory contents = new LunchInventory(containerItem);
        size = contents.getSlots();
        int columns = size > 9 ? size / 2 : size;
        int left = (176 - columns * 18) / 2;
        int top = size > 9 ? 29 : 35;
        for (int i = 0; i < size; i++) {
            addSlot(new SlotItemHandler(contents, i, left + i % columns * 18, top + i / columns * 18) {
                @Override public void setChanged() {
                    // Vanilla merging changes stack counts directly, bypassing ItemStackHandler's callbacks.
                    contents.setStackInSlot(getSlotIndex(), getItem());
                }
            });
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addPlayerSlot(player, 9 + row * 9 + col, 8 + col * 18, 84 + row * 18);
        for (int col = 0; col < 9; col++) addPlayerSlot(player, col, 8 + col * 18, 142);
    }
    private void addPlayerSlot(Player player, int index, int x, int y) {
        addSlot(new Slot(player.getInventory(), index, x, y) {
            @Override public boolean mayPickup(Player player) { return index != boundSlot; }
            @Override public boolean mayPlace(ItemStack stack) { return index != boundSlot; }
        });
    }
    public static boolean validSlot(Player player, int slot) {
        return (slot >= 0 && slot < 9 || slot == 40)
            && player.getInventory().getItem(slot).getItem() instanceof ItemLunchContainer;
    }
    @Override public boolean stillValid(Player player) {
        return player.getInventory().getItem(boundSlot) == containerItem;
    }
    @Override public void clicked(int slot,int button,ClickType type,Player player) {
        if(type==ClickType.SWAP&&button==boundSlot)return;
        if(slot>=0&&slot<this.slots.size()) {
            Slot clicked=this.slots.get(slot);if(clicked.container==player.getInventory()&&clicked.getSlotIndex()==boundSlot)return;
        }
        super.clicked(slot,button,type,player);
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= this.slots.size()) return ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (index < size) {
            if (!moveItemStackTo(stack, size, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (!LunchInventory.canHold(stack) || !moveItemStackTo(stack, 0, size, false)) return ItemStack.EMPTY;
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }
}
