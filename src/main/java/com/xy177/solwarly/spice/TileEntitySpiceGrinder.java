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
    public static final int BATCH_TICKS = 60, WAIT_TICKS = 100;
    private int progress, idleTicks, orphanTicks, waitTicks;
    private boolean working;
    private long lastWorkingSoundTick = Long.MIN_VALUE, animationStart;
    private String binding = "";
    private SpiceRecipe processingRecipe;
    private ItemStack waitingInput = ItemStack.EMPTY;
    private java.util.List<ItemStack> clientPreview = java.util.Collections.emptyList();
    private int clientPreviewTicks = 30;
    private final ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override protected void onContentsChanged(int slot) {
            if (slot == 0 && getStackInSlot(0).isEmpty()) { waitingInput = ItemStack.EMPTY; waitTicks = 0; }
            markDirty();
        }
    };
    private final IItemHandler input = new SidedHandler(true), output = new SidedHandler(false);
    public boolean isWorking() { return working; }
    public long animationStart() { return animationStart; }
    public ItemStack getInput() { return inventory.getStackInSlot(0); }
    public ItemStack getSecondInput() { return inventory.getStackInSlot(1); }
    public ItemStack getOutput() { return inventory.getStackInSlot(2); }
    public int getProgress() { return progress; }
    public String binding() { return binding; }
    private boolean singleOnly() { return binding.isEmpty() && world != null && world.isBlockPowered(pos); }
    private SpiceRecipe recipe() { return SpiceRecipes.find(getInput(), getSecondInput(), binding, singleOnly()); }
    @Override public void update() {
        if (world == null || world.isRemote) return;
        if (singleOnly() && !getSecondInput().isEmpty()) eject(1);
        if (getInput().isEmpty() && !getSecondInput().isEmpty()) {
            if (++orphanTicks >= BATCH_TICKS) { eject(1); orphanTicks = 0; }
            markDirty();
        } else orphanTicks = 0;
        if (!same(getInput(), waitingInput)) { waitingInput = getInput().copy(); waitTicks = 0; }
        SpiceRecipe recipe = recipe();
        boolean delay = binding.isEmpty() && !singleOnly() && (recipe == null || !recipe.dual())
            && SpiceRecipes.overlaps(getInput()) && waitTicks < WAIT_TICKS;
        if (delay) {
            idleTicks = 0; setWorking(true); waitTicks++;
            progress = 0; processingRecipe = null; markDirty(); return;
        }
        if (recipe != null) {
            idleTicks = 0;
            if (!working) setWorking(true);
            else if (lastWorkingSoundTick == Long.MIN_VALUE) playWorkingSound();
            waitTicks = WAIT_TICKS;
            if (processingRecipe != recipe) { processingRecipe = recipe; progress = 0; }
            if (++progress >= BATCH_TICKS) {
                finish(recipe); progress = 0;
                world.playSound(null, pos, SpiceRegistry.grinderComplete, SoundCategory.BLOCKS, 0.5F, 1.0F);
                if (recipe() == null) setWorking(false); else playWorkingSound();
            }
        } else {
            progress = 0; processingRecipe = null; setWorking(false);
            if (idleTicks <= BATCH_TICKS) idleTicks++;
            if (idleTicks > BATCH_TICKS && !getOutput().isEmpty() && !hasCollectorBelow()) ejectOutput();
        }
        if (recipe != null || !getOutput().isEmpty()) markDirty();
    }
    private void finish(SpiceRecipe recipe) {
        ItemStack result = recipe.result(), current = getOutput();
        if (!current.isEmpty() && (!same(current, result) || current.getCount() + result.getCount() > Math.min(64, current.getMaxStackSize()))) ejectOutput();
        int firstCount = recipe.countFor(getInput());
        int secondCount = recipe.dual() ? recipe.countFor(getSecondInput()) : 0;
        inventory.extractItem(0, firstCount, false);
        if (secondCount > 0) inventory.extractItem(1, secondCount, false);
        returnStack(recipe.returnedContainers()); returnStack(recipe.secondReturnedContainers());
        returnStack(inventory.insertItem(2, result, false));
    }
    /** Every ejection first offers the stack to a configured collector; breaking still drops contents. */
    private void returnStack(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (hasCollectorBelow()) {
            IItemHandler handler = world.getTileEntity(pos.down()) == null ? null : world.getTileEntity(pos.down()).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
            if (handler != null) stack = net.minecraftforge.items.ItemHandlerHelper.insertItemStacked(handler, stack, false);
        }
        while (!stack.isEmpty()) drop(stack.splitStack(stack.getMaxStackSize()));
    }
    protected boolean hasCollectorBelow() { return GrinderConfig.isCollector(world.getBlockState(pos.down()).getBlock()); }
    private void eject(int slot) { returnStack(inventory.extractItem(slot, 64, false)); }
    public void ejectOutput() { eject(2); }
    protected void drop(ItemStack stack) {
        if (world == null || world.isRemote || stack.isEmpty()) return;
        EntityItem item = new EntityItem(world, pos.getX() + .5, pos.getY() + .8, pos.getZ() + .5, stack.copy());
        item.motionX = (world.rand.nextDouble() - .5) * .16; item.motionY = .18; item.motionZ = (world.rand.nextDouble() - .5) * .16;
        item.setDefaultPickupDelay(); world.spawnEntity(item);
    }
    public void dropContents() { for (int slot = 0; slot < 3; slot++) drop(inventory.extractItem(slot, 64, false)); }
    public boolean insertHeld(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (player.isSneaking()) {
            if (held.isEmpty()) { binding = ""; changedBinding(); return true; }
            java.util.List<SpiceKind> kinds = Seasoning.ingredient(held);
            if (Seasoning.mixed(held) || kinds.size() != 1 || kinds.get(0).accompaniment()) return false;
            String selected = Seasoning.identity(kinds);
            if (SpiceRecipes.all().stream().noneMatch(r -> r.boundTo(selected))) return false;
            binding = selected;
            // Filter against all routes of the selected product, not just one ingredient list.
            for (int slot = 0; slot < 2; slot++) if (!inventory.getStackInSlot(slot).isEmpty()
                && SpiceRecipes.ingredient(inventory.getStackInSlot(slot), ItemStack.EMPTY, binding, false) == null) eject(slot);
            changedBinding(); return true;
        }
        if (held.isEmpty()) {
            boolean retrieved = false;
            for (int slot = 0; slot < 2; slot++) {
                ItemStack stack = inventory.extractItem(slot, 64, false);
                if (!stack.isEmpty()) { net.minecraftforge.items.ItemHandlerHelper.giveItemToPlayer(player, stack); retrieved = true; }
            }
            progress = 0; processingRecipe = null; setWorking(false); return retrieved;
        }
        for (int slot = 0; slot < 2; slot++) {
            if (!input.isItemValid(slot, held)) continue;
            SpiceRecipe recipe = SpiceRecipes.ingredient(held, slot == 1 ? getInput() : getSecondInput(), binding, singleOnly());
            if (recipe == null) continue;
            ItemStack remainder = input.insertItem(slot, held, true);
            int batch = recipe.countFor(held);
            int amount = (held.getCount() - remainder.getCount()) / batch * batch;
            if (amount == 0) continue;
            input.insertItem(slot, SpiceRecipe.copy(held, amount), false);
            if (!player.capabilities.isCreativeMode) held.shrink(amount);
            return true;
        }
        return false;
    }
    private void changedBinding() {
        progress = 0; processingRecipe = null;
        markDirty();
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }
    private void setWorking(boolean value) {
        if (working == value) return;
        working = value;
        if (!value) lastWorkingSoundTick = Long.MIN_VALUE;
        else { animationStart = world.getTotalWorldTime(); playWorkingSound(); }
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3); markDirty();
    }
    private void playWorkingSound() {
        if (world == null || world.isRemote) return;
        world.playSound(null, pos, SpiceRegistry.grinderWorking, SoundCategory.BLOCKS, 0.5F, 1.0F);
        lastWorkingSoundTick = world.getTotalWorldTime();
    }
    private static boolean same(ItemStack first, ItemStack second) { return first.isEmpty() && second.isEmpty() || SpiceRecipe.same(first, second); }
    private java.util.List<ItemStack> preview() {
        java.util.List<ItemStack> result = new java.util.ArrayList<ItemStack>();
        if (!binding.isEmpty()) for (SpiceRecipe recipe : SpiceRecipes.all()) if (recipe.boundTo(binding)) {
            result.add(recipe.result()); result.add(SpiceRecipe.copy(recipe.input, recipe.inputCount));
            if (recipe.dual()) result.add(SpiceRecipe.copy(recipe.secondInput, recipe.secondCount));
        }
        return result;
    }
    public ItemStack displayStack(float partial) {
        java.util.List<ItemStack> items = world.isRemote ? clientPreview : preview();
        int interval = world.isRemote ? clientPreviewTicks : GrinderConfig.previewTicks;
        return items.isEmpty() ? ItemStack.EMPTY : items.get((int) ((world.getTotalWorldTime() / Math.max(1, interval)) % items.size()));
    }
    private final class SidedHandler implements IItemHandler {
        private final boolean insertion;
        SidedHandler(boolean insertion) { this.insertion = insertion; }
        public int getSlots() { return insertion ? 2 : 1; }
        private int slot(int index) { return insertion ? index : 2; }
        public ItemStack getStackInSlot(int index) { return index >= 0 && index < getSlots() ? inventory.getStackInSlot(slot(index)) : ItemStack.EMPTY; }
        public boolean isItemValid(int index, ItemStack stack) {
            if (!insertion || index < 0 || index > 1 || stack.isEmpty()) return false;
            if (index == 1 && (singleOnly() || getInput().isEmpty() || ItemStack.areItemsEqual(getInput(), stack))) return false;
            if (index == 0 && !getSecondInput().isEmpty() && ItemStack.areItemsEqual(getSecondInput(), stack)) return false;
            return SpiceRecipes.ingredient(stack, index == 1 ? getInput() : getSecondInput(), binding, singleOnly()) != null;
        }
        public ItemStack insertItem(int index, ItemStack stack, boolean simulate) {
            return isItemValid(index, stack) ? inventory.insertItem(slot(index), stack, simulate) : stack;
        }
        public ItemStack extractItem(int index, int amount, boolean simulate) {
            return index >= 0 && index < getSlots() ? inventory.extractItem(slot(index), amount, simulate) : ItemStack.EMPTY;
        }
        public int getSlotLimit(int index) { return index >= 0 && index < getSlots() ? 64 : 0; }
    }
    private static NBTTagCompound migratedInventory(NBTTagCompound old) {
        NBTTagCompound data = old.copy();
        if (data.getInteger("Size") == 2) {
            net.minecraft.nbt.NBTTagList entries = data.getTagList("Items", 10);
            for (int i = 0; i < entries.tagCount(); i++) if (entries.getCompoundTagAt(i).getInteger("Slot") == 1) entries.getCompoundTagAt(i).setInteger("Slot", 2);
        }
        data.setInteger("Size", 3); return data;
    }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag); tag.setTag("Inventory", inventory.serializeNBT());
        tag.setInteger("Progress", progress); tag.setInteger("IdleTicks", idleTicks);
        tag.setInteger("OrphanTicks", orphanTicks); tag.setInteger("WaitTicks", waitTicks);
        tag.setTag("WaitingInput", waitingInput.serializeNBT()); tag.setString("Binding", binding);
        tag.setBoolean("Working", working); tag.setLong("AnimationStart", animationStart); return tag;
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag); inventory.deserializeNBT(migratedInventory(tag.getCompoundTag("Inventory")));
        idleTicks = Math.max(0, Math.min(61, tag.getInteger("IdleTicks")));
        orphanTicks = Math.max(0, Math.min(59, tag.getInteger("OrphanTicks")));
        waitTicks = Math.max(0, Math.min(WAIT_TICKS, tag.getInteger("WaitTicks")));
        waitingInput = new ItemStack(tag.getCompoundTag("WaitingInput")); binding = tag.getString("Binding");
        working = tag.getBoolean("Working"); animationStart = tag.getLong("AnimationStart");
        processingRecipe = recipe();
        progress = processingRecipe == null ? 0 : Math.max(0, Math.min(BATCH_TICKS - 1, tag.getInteger("Progress")));
    }
    @Override public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.writeToNBT(new NBTTagCompound());
        tag.setBoolean("Working", working); tag.setLong("AnimationStart", animationStart);
        tag.setString("Binding", binding); tag.setInteger("PreviewTicks", GrinderConfig.previewTicks);
        net.minecraft.nbt.NBTTagList items = new net.minecraft.nbt.NBTTagList();
        for (ItemStack item : preview()) items.appendTag(item.serializeNBT()); tag.setTag("Preview", items); return tag;
    }
    @Override public void handleUpdateTag(NBTTagCompound tag) {
        working = tag.getBoolean("Working"); animationStart = tag.getLong("AnimationStart"); binding = tag.getString("Binding");
        clientPreviewTicks = Math.max(1, tag.getInteger("PreviewTicks"));
        net.minecraft.nbt.NBTTagList items = tag.getTagList("Preview", 10);
        java.util.List<ItemStack> stacks = new java.util.ArrayList<>();
        for (int i = 0; i < items.tagCount(); i++) stacks.add(new ItemStack(items.getCompoundTagAt(i))); clientPreview = stacks;
    }
    @Override public SPacketUpdateTileEntity getUpdatePacket() { return new SPacketUpdateTileEntity(pos, 0, getUpdateTag()); }
    @Override public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) { handleUpdateTag(packet.getNbtCompound()); }
    @Override public AxisAlignedBB getRenderBoundingBox() { return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2.3, pos.getZ() + 1); }
    @Override public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing side) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, side);
    }
    @Override @Nullable public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(side == EnumFacing.DOWN ? output : input);
        return super.getCapability(capability, side);
    }
}
