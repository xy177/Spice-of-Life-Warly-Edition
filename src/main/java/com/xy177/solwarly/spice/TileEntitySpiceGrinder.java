package com.xy177.solwarly.spice;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;

import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import javax.annotation.Nullable;

/** All processing and item movement happen on the server; the client receives only animation state. */
public class TileEntitySpiceGrinder extends BlockEntity {
    public TileEntitySpiceGrinder(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state) { super(SpiceRegistry.GRINDER_ENTITY.get(),pos,state); }
    public static final int BATCH_TICKS = 60, WAIT_TICKS = 100;
    private int progress, idleTicks, orphanTicks, waitTicks;
    private boolean working;
    private long lastWorkingSoundTick = Long.MIN_VALUE, animationStart;
    private String binding = "";
    private SpiceRecipe processingRecipe;
    private ItemStack waitingInput = ItemStack.EMPTY;
    private java.util.List<ItemStack> clientPreview = java.util.List.of();
    private int clientPreviewTicks = 30;
    private final ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override protected void onContentsChanged(int slot) {
            if (slot == 0 && getStackInSlot(0).isEmpty()) { waitingInput = ItemStack.EMPTY; waitTicks = 0; }
            setChanged();
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
    private boolean singleOnly() { return binding.isEmpty() && level != null && level.hasNeighborSignal(worldPosition); }
    private SpiceRecipe recipe() { return SpiceRecipes.find(getInput(), getSecondInput(), binding, singleOnly()); }
    public void update() {
        if (level == null || level.isClientSide) return;
        if (singleOnly() && !getSecondInput().isEmpty()) eject(1);
        if (getInput().isEmpty() && !getSecondInput().isEmpty()) {
            if (++orphanTicks >= BATCH_TICKS) { eject(1); orphanTicks = 0; }
            setChanged();
        } else orphanTicks = 0;
        if (!same(getInput(), waitingInput)) { waitingInput = getInput().copy(); waitTicks = 0; }
        SpiceRecipe recipe = recipe();
        boolean delay = binding.isEmpty() && !singleOnly() && (recipe == null || !recipe.dual())
            && SpiceRecipes.overlaps(getInput()) && waitTicks < WAIT_TICKS;
        if (delay) {
            idleTicks = 0; setWorking(true); waitTicks++;
            progress = 0; processingRecipe = null; setChanged(); return;
        }
        if (recipe != null) {
            idleTicks = 0;
            if (!working) setWorking(true);
            else if (lastWorkingSoundTick == Long.MIN_VALUE) playWorkingSound();
            waitTicks = WAIT_TICKS;
            if (processingRecipe != recipe) { processingRecipe = recipe; progress = 0; }
            if (++progress >= BATCH_TICKS) {
                finish(recipe); progress = 0;
                level.playSound(null, worldPosition, SpiceRegistry.grinderComplete, SoundSource.BLOCKS, 0.5F, 1.0F);
                if (recipe() == null) setWorking(false); else playWorkingSound();
            }
        } else {
            progress = 0; processingRecipe = null; setWorking(false);
            if (idleTicks <= BATCH_TICKS) idleTicks++;
            if (idleTicks > BATCH_TICKS && !getOutput().isEmpty() && !hasCollectorBelow()) ejectOutput();
        }
        if (recipe != null || !getOutput().isEmpty()) setChanged();
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
            IItemHandler handler = level.getBlockEntity(worldPosition.below()) == null ? null : level.getBlockEntity(worldPosition.below()).getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).orElse(null);
            if (handler != null) stack = net.minecraftforge.items.ItemHandlerHelper.insertItemStacked(handler, stack, false);
        }
        while (!stack.isEmpty()) drop(stack.split(stack.getMaxStackSize()));
    }
    protected boolean hasCollectorBelow() { return GrinderConfig.isCollector(level.getBlockState(worldPosition.below()).getBlock()); }
    private void eject(int slot) { returnStack(inventory.extractItem(slot, 64, false)); }
    public void ejectOutput() { eject(2); }
    protected void drop(ItemStack stack) {
        if (level == null || level.isClientSide || stack.isEmpty()) return;
        ItemEntity item = new ItemEntity(level, worldPosition.getX() + .5, worldPosition.getY() + .8, worldPosition.getZ() + .5, stack.copy());
        item.setDeltaMovement((level.random.nextDouble() - .5) * .16, .18, (level.random.nextDouble() - .5) * .16);
        item.setDefaultPickUpDelay(); level.addFreshEntity(item);
    }
    public void dropContents() { for (int slot = 0; slot < 3; slot++) drop(inventory.extractItem(slot, 64, false)); }
    public boolean insertHeld(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (held.isEmpty()) { binding = ""; changedBinding(); return true; }
            var kinds = Seasoning.ingredient(held);
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
            var recipe = SpiceRecipes.ingredient(held, slot == 1 ? getInput() : getSecondInput(), binding, singleOnly());
            if (recipe == null) continue;
            ItemStack remainder = input.insertItem(slot, held, true);
            int batch = recipe.countFor(held);
            int amount = (held.getCount() - remainder.getCount()) / batch * batch;
            if (amount == 0) continue;
            input.insertItem(slot, held.copyWithCount(amount), false);
            if (!player.getAbilities().instabuild) held.shrink(amount);
            return true;
        }
        return false;
    }
    private void changedBinding() {
        progress = 0; processingRecipe = null;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    private void setWorking(boolean value) {
        if (working == value) return;
        working = value;
        if (!value) lastWorkingSoundTick = Long.MIN_VALUE;
        else { animationStart = level.getGameTime(); playWorkingSound(); }
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); setChanged();
    }
    private void playWorkingSound() {
        if (level == null || level.isClientSide) return;
        level.playSound(null, worldPosition, SpiceRegistry.grinderWorking, SoundSource.BLOCKS, 0.5F, 1.0F);
        lastWorkingSoundTick = level.getGameTime();
    }
    private static boolean same(ItemStack first, ItemStack second) { return first.isEmpty() && second.isEmpty() || SpiceRecipe.same(first, second); }
    private java.util.List<ItemStack> preview() {
        var result = new java.util.ArrayList<ItemStack>();
        if (!binding.isEmpty()) for (var recipe : SpiceRecipes.all()) if (recipe.boundTo(binding)) {
            result.add(recipe.result()); result.add(recipe.input.copyWithCount(recipe.inputCount));
            if (recipe.dual()) result.add(recipe.secondInput.copyWithCount(recipe.secondCount));
        }
        return result;
    }
    public ItemStack displayStack(float partial) {
        var items = level.isClientSide ? clientPreview : preview();
        int interval = level.isClientSide ? clientPreviewTicks : GrinderConfig.previewTicks;
        return items.isEmpty() ? ItemStack.EMPTY : items.get((int) ((level.getGameTime() / Math.max(1, interval)) % items.size()));
    }
    private final class SidedHandler implements IItemHandler {
        private final boolean insertion;
        SidedHandler(boolean insertion) { this.insertion = insertion; }
        public int getSlots() { return insertion ? 2 : 1; }
        private int slot(int index) { return insertion ? index : 2; }
        public ItemStack getStackInSlot(int index) { return index >= 0 && index < getSlots() ? inventory.getStackInSlot(slot(index)) : ItemStack.EMPTY; }
        public boolean isItemValid(int index, ItemStack stack) {
            if (!insertion || index < 0 || index > 1 || stack.isEmpty()) return false;
            if (index == 1 && (singleOnly() || getInput().isEmpty() || ItemStack.isSameItem(getInput(), stack))) return false;
            if (index == 0 && !getSecondInput().isEmpty() && ItemStack.isSameItem(getSecondInput(), stack)) return false;
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
    private static CompoundTag migratedInventory(CompoundTag old) {
        CompoundTag data = old.copy();
        if (data.getInt("Size") == 2) {
            var entries = data.getList("Items", 10);
            for (int i = 0; i < entries.size(); i++) if (entries.getCompound(i).getInt("Slot") == 1) entries.getCompound(i).putInt("Slot", 2);
        }
        data.putInt("Size", 3); return data;
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("Progress", progress);
        tag.putInt("IdleTicks", idleTicks); tag.putInt("OrphanTicks", orphanTicks); tag.putInt("WaitTicks", waitTicks);
        tag.put("WaitingInput", waitingInput.save(new CompoundTag())); tag.putString("Binding", binding);
        tag.putBoolean("Working", working); tag.putLong("AnimationStart", animationStart);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); inventory.deserializeNBT(migratedInventory(tag.getCompound("Inventory")));
        idleTicks = Math.max(0, Math.min(61, tag.getInt("IdleTicks"))); orphanTicks = Math.max(0, Math.min(59, tag.getInt("OrphanTicks")));
        waitTicks = Math.max(0, Math.min(WAIT_TICKS, tag.getInt("WaitTicks"))); waitingInput = ItemStack.of(tag.getCompound("WaitingInput"));
        binding = tag.getString("Binding"); working = tag.getBoolean("Working"); animationStart = tag.getLong("AnimationStart");
        processingRecipe = recipe();
        progress = processingRecipe == null ? 0 : Math.max(0, Math.min(BATCH_TICKS - 1, tag.getInt("Progress")));
    }
    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag(); tag.putBoolean("Working", working); tag.putLong("AnimationStart", animationStart);
        tag.putString("Binding", binding); tag.putInt("PreviewTicks", GrinderConfig.previewTicks);
        var items = new net.minecraft.nbt.ListTag();
        for (var item : preview()) items.add(item.save(new CompoundTag())); tag.put("Preview", items); return tag;
    }
    @Override public void handleUpdateTag(CompoundTag tag) {
        working = tag.getBoolean("Working"); animationStart = tag.getLong("AnimationStart"); binding = tag.getString("Binding");
        clientPreviewTicks = Math.max(1, tag.getInt("PreviewTicks"));
        var items = tag.getList("Preview", 10); var stacks = new java.util.ArrayList<ItemStack>();
        for (int i = 0; i < items.size(); i++) stacks.add(ItemStack.of(items.getCompound(i))); clientPreview = stacks;
    }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) { handleUpdateTag(packet.getTag()); }
    @Override public AABB getRenderBoundingBox() { return new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 2.3, worldPosition.getZ() + 1); }
    private LazyOptional<IItemHandler> inputCapability=LazyOptional.of(()->input),outputCapability=LazyOptional.of(()->output);
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,@Nullable Direction side) {
        if(capability==ForgeCapabilities.ITEM_HANDLER)return (side==Direction.DOWN?outputCapability:inputCapability).cast();
        return super.getCapability(capability,side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps();inputCapability.invalidate();outputCapability.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps();inputCapability=LazyOptional.of(()->input);outputCapability=LazyOptional.of(()->output); }
}
