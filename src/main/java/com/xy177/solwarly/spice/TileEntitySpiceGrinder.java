package com.xy177.solwarly.spice;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import javax.annotation.Nullable;

/** All processing and item movement happen on the server; the client receives only animation state. */
public class TileEntitySpiceGrinder extends TileEntity implements ITickable {
    public static final int BATCH_TICKS = 60;
    private int progress, idleTicks;
    private boolean working;
    private long lastWorkingSoundTick = Long.MIN_VALUE;
    private long animationStart;
    private ItemStack processingInput = ItemStack.EMPTY;
    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override protected void onContentsChanged(int slot) {
            if (slot == 0 && world != null && !world.isRemote) {
                SpiceRecipe recipe = SpiceRecipes.find(getStackInSlot(0));
                if (recipe == null || getStackInSlot(0).getCount() < recipe.inputCount) {
                    progress = 0; processingInput = ItemStack.EMPTY; setWorking(false);
                }
            }
            markDirty();
        }
    };
    private final IItemHandler input = new SidedHandler(0, true);
    private final IItemHandler output = new SidedHandler(1, false);

    public boolean isWorking() { return working; }
    public long animationStart() { return animationStart; }
    public ItemStack getInput() { return inventory.getStackInSlot(0); }
    public ItemStack getOutput() { return inventory.getStackInSlot(1); }
    public int getProgress() { return progress; }

    @Override public void update() {
        if (world == null || world.isRemote) return;
        SpiceRecipe recipe = SpiceRecipes.find(getInput());
        boolean canWork = recipe != null && getInput().getCount() >= recipe.inputCount;
        if (canWork) {
            idleTicks = 0;
            if (!same(recipe.input, processingInput)) { processingInput = recipe.input.copy(); progress = 0; }
            if (!working) setWorking(true);
            else if (lastWorkingSoundTick == Long.MIN_VALUE) playWorkingSound();
            if (++progress >= BATCH_TICKS) {
                finish(recipe); progress = 0;
                world.playSound(null, pos, SpiceRegistry.grinderComplete, SoundCategory.BLOCKS, 0.5F, 1.0F);
                SpiceRecipe next = SpiceRecipes.find(getInput());
                if (next == null || getInput().getCount() < next.inputCount) setWorking(false);
                else playWorkingSound();
            }
        } else {
            progress = 0; processingInput = ItemStack.EMPTY; setWorking(false);
            if (idleTicks < BATCH_TICKS + 1) idleTicks++;
            if (idleTicks > BATCH_TICKS && !getOutput().isEmpty() && !hasCollectorBelow()) ejectOutput();
        }
        if (canWork || !getOutput().isEmpty()) markDirty();
    }

    private void finish(SpiceRecipe recipe) {
        ItemStack result = recipe.result();
        ItemStack current = getOutput();
        if (!current.isEmpty() && (!same(current, result)
            || current.getCount() + result.getCount() > Math.min(64, current.getMaxStackSize()))) ejectOutput();
        inventory.extractItem(0, recipe.inputCount, false);
        ItemStack containers = recipe.returnedContainers();
        while (!containers.isEmpty()) returnContainer(containers.splitStack(containers.getMaxStackSize()));
        ItemStack remaining = inventory.insertItem(1, result, false);
        if (!remaining.isEmpty()) drop(remaining);
    }
    private void returnContainer(ItemStack stack) {
        if (hasCollectorBelow()) {
            TileEntity collector = world.getTileEntity(pos.down());
            if (collector != null) {
                IItemHandler handler = collector.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                if (handler != null) stack = net.minecraftforge.items.ItemHandlerHelper.insertItemStacked(handler, stack, false);
            }
        }
        if (!stack.isEmpty()) drop(stack);
    }
    protected boolean hasCollectorBelow() {
        return GrinderConfig.isCollector(world.getBlockState(pos.down()).getBlock());
    }
    public void ejectOutput() {
        ItemStack stack = inventory.extractItem(1, 64, false);
        if (!stack.isEmpty()) drop(stack);
    }
    protected void drop(ItemStack stack) {
        if (world == null || world.isRemote || stack.isEmpty()) return;
        EntityItem item = new EntityItem(world, pos.getX() + .5, pos.getY() + .8, pos.getZ() + .5, stack.copy());
        item.motionX = (world.rand.nextDouble() - .5) * .16;
        item.motionY = .18;
        item.motionZ = (world.rand.nextDouble() - .5) * .16;
        item.setDefaultPickupDelay(); world.spawnEntity(item);
    }
    public void dropContents() {
        for (int slot = 0; slot < 2; slot++) drop(inventory.extractItem(slot, 64, false));
    }
    public boolean insertHeld(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (held.isEmpty()) {
            ItemStack retrieved = inventory.extractItem(0, 64, false);
            if (retrieved.isEmpty()) return false;
            net.minecraftforge.items.ItemHandlerHelper.giveItemToPlayer(player, retrieved);
            progress = 0; processingInput = ItemStack.EMPTY; setWorking(false);
            return true;
        }
        SpiceRecipe recipe = SpiceRecipes.find(held);
        if (recipe == null) return false;
        ItemStack remainder = input.insertItem(0, held, true);
        int amount = (held.getCount() - remainder.getCount()) / recipe.inputCount * recipe.inputCount;
        if (amount == 0) return false;
        ItemStack offered = held.copy(); offered.setCount(amount);
        input.insertItem(0, offered, false);
        if (!player.capabilities.isCreativeMode) held.shrink(amount);
        return true;
    }
    private void setWorking(boolean value) {
        if (working == value) return;
        working = value;
        if (!value) lastWorkingSoundTick = Long.MIN_VALUE;
        else if (lastWorkingSoundTick == Long.MIN_VALUE) playWorkingSound();
        if (value) animationStart = world.getTotalWorldTime();
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        markDirty();
    }
    private void playWorkingSound() {
        if (world == null || world.isRemote) return;
        world.playSound(null, pos, SpiceRegistry.grinderWorking, SoundCategory.BLOCKS, 0.5F, 1.0F);
        lastWorkingSoundTick = world.getTotalWorldTime();
    }
    private static boolean same(ItemStack first, ItemStack second) {
        return ItemStack.areItemsEqual(first, second) && ItemStack.areItemStackTagsEqual(first, second);
    }

    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag); tag.setTag("Inventory", inventory.serializeNBT());
        tag.setInteger("Progress", progress); tag.setInteger("IdleTicks", idleTicks);
        tag.setTag("ProcessingInput", processingInput.serializeNBT()); tag.setBoolean("Working", working);
        tag.setLong("AnimationStart", animationStart); return tag;
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag); inventory.deserializeNBT(tag.getCompoundTag("Inventory"));
        progress = Math.max(0, Math.min(BATCH_TICKS - 1, tag.getInteger("Progress")));
        idleTicks = Math.max(0, Math.min(BATCH_TICKS + 1, tag.getInteger("IdleTicks")));
        processingInput = new ItemStack(tag.getCompoundTag("ProcessingInput")); working = tag.getBoolean("Working");
        animationStart = tag.getLong("AnimationStart");
    }
    @Override public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.writeToNBT(new NBTTagCompound());
        tag.setBoolean("Working", working); tag.setLong("AnimationStart", animationStart); return tag;
    }
    @Override public void handleUpdateTag(NBTTagCompound tag) {
        working = tag.getBoolean("Working"); animationStart = tag.getLong("AnimationStart");
    }
    @Override public SPacketUpdateTileEntity getUpdatePacket() { return new SPacketUpdateTileEntity(pos, 0, getUpdateTag()); }
    @Override public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) { handleUpdateTag(packet.getNbtCompound()); }
    @Override public AxisAlignedBB getRenderBoundingBox() { return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1.5, pos.getZ() + 1); }
    @Override public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing side) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, side);
    }
    @Override @Nullable public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(side == EnumFacing.DOWN ? output : input);
        return super.getCapability(capability, side);
    }
    private final class SidedHandler implements IItemHandler {
        private final int slot;
        private final boolean insertion;
        SidedHandler(int slot, boolean insertion) { this.slot = slot; this.insertion = insertion; }
        public int getSlots() { return 1; }
        public ItemStack getStackInSlot(int index) { return index == 0 ? inventory.getStackInSlot(slot) : ItemStack.EMPTY; }
        public ItemStack insertItem(int index, ItemStack stack, boolean simulate) {
            if (index != 0 || !insertion || SpiceRecipes.find(stack) == null) return stack;
            return inventory.insertItem(slot, stack, simulate);
        }
        public ItemStack extractItem(int index, int amount, boolean simulate) {
            return index == 0 ? inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }
        public int getSlotLimit(int index) { return index == 0 ? 64 : 0; }
    }
}
