package com.xy177.solwarly;

import com.xy177.solwarly.spice.*;
import com.xy177.solwarly.block.BlockSpiceGrinder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.*;
import org.junit.*;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SpiceGrinderTest {
    private Capability<IItemHandler> oldCapability;
    private List<SpiceRecipe> previousRecipes;
    private List<SpiceRecipe> recipes;

    @Test public void placementFacesThePlayerAndDirectionSurvivesMetadataRoundTrip() {
        BlockSpiceGrinder block = BlockSpiceGrinder.INSTANCE;
        EntityLivingBase placer = mock(EntityLivingBase.class);
        for (EnumFacing direction : EnumFacing.HORIZONTALS) {
            when(placer.getHorizontalFacing()).thenReturn(direction);
            IBlockState placed = block.getStateForPlacement(mock(World.class), BlockPos.ORIGIN, EnumFacing.UP,
                .5F, .5F, .5F, 0, placer, EnumHand.MAIN_HAND);
            assertEquals(direction.getOpposite(), placed.getValue(BlockSpiceGrinder.FACING));
            assertEquals(placed, block.getStateFromMeta(block.getMetaFromState(placed)));
        }
        // Old grinders had metadata 0 and must retain the unrotated appearance.
        assertEquals(EnumFacing.SOUTH, block.getStateFromMeta(0).getValue(BlockSpiceGrinder.FACING));
    }

    @BeforeClass public static void bootstrap() {
        Bootstrap.register();
        net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity(Probe.class, new ResourceLocation("solwarly:grinder_test"));
        net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(SpiceRegistry.spice);
    }
    @Before @SuppressWarnings("unchecked") public void setup() throws Exception {
        oldCapability = CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY = mock(Capability.class);
        when(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(any())).thenAnswer(call -> call.getArgument(0));
        java.lang.reflect.Field field = SpiceRecipes.class.getDeclaredField("RECIPES"); field.setAccessible(true);
        recipes = (List<SpiceRecipe>) field.get(null); previousRecipes = new ArrayList<>(recipes); recipes.clear();
        SpiceRecipes.register(new SpiceRecipe(new ItemStack(Items.WHEAT), 3, spice("a", 0xFF0000), 2));
        SpiceRecipes.register(new SpiceRecipe(new ItemStack(Items.CARROT), 2, spice("b", 0x00FF00), 3));
    }
    @After public void restore() {
        recipes.clear(); recipes.addAll(previousRecipes);
        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY = oldCapability;
    }
    private ItemStack spice(String id, int color) { return ItemSpice.create(id, color, 1); }
    private Probe mill() {
        Probe tile = new Probe(); World world = mock(World.class);
        when(world.getBlockState(any(BlockPos.class))).thenReturn(Blocks.AIR.getDefaultState());
        tile.setWorld(world); tile.setPos(new BlockPos(0, 64, 0)); return tile;
    }
    private IItemHandler side(TileEntitySpiceGrinder mill, EnumFacing side) {
        return mill.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
    }
    private void ticks(TileEntitySpiceGrinder mill, int count) { for (int i = 0; i < count; i++) mill.update(); }
    @Test public void batchConsumesExactlyXAfterSixtyTicksAndKeepsNbt() {
        Probe mill = mill(); side(mill, EnumFacing.UP).insertItem(0, new ItemStack(Items.WHEAT, 7), false);
        ticks(mill, 59); assertTrue(mill.isWorking()); assertTrue(mill.getOutput().isEmpty()); assertEquals(7, mill.getInput().getCount());
        ticks(mill, 1); assertEquals(4, mill.getInput().getCount()); assertEquals(2, mill.getOutput().getCount());
        assertEquals("a", ItemSpice.flavor(mill.getOutput()));
        ticks(mill, 60); assertEquals(1, mill.getInput().getCount()); assertEquals(4, mill.getOutput().getCount()); assertFalse(mill.isWorking());
    }
    @Test public void sidesExtractInputAndBottomExtractsOnlyOutput() {
        Probe mill = mill(); IItemHandler top = side(mill, EnumFacing.UP), bottom = side(mill, EnumFacing.DOWN);
        assertEquals(3, top.insertItem(0, new ItemStack(Items.STICK, 3), false).getCount());
        assertEquals(3, bottom.insertItem(0, new ItemStack(Items.WHEAT, 3), false).getCount());
        top.insertItem(0, new ItemStack(Items.WHEAT, 6), false); ticks(mill, 60);
        assertEquals(Items.WHEAT, side(mill, EnumFacing.EAST).extractItem(0, 1, false).getItem());
        ItemStack output = bottom.extractItem(0, 1, true); assertEquals("a", ItemSpice.flavor(output));
        assertEquals(2, mill.getOutput().getCount()); bottom.extractItem(0, 1, false); assertEquals(1, mill.getOutput().getCount());
    }
    @Test public void changedSpiceEjectsOldOutputBeforeProducingNewSpice() {
        Probe mill = mill(); side(mill, EnumFacing.UP).insertItem(0, new ItemStack(Items.WHEAT, 3), false); ticks(mill, 60);
        side(mill, EnumFacing.NORTH).insertItem(0, new ItemStack(Items.CARROT, 2), false); ticks(mill, 60);
        assertEquals(1, mill.drops.size()); assertEquals("a", ItemSpice.flavor(mill.drops.get(0)));
        assertEquals("b", ItemSpice.flavor(mill.getOutput())); assertEquals(3, mill.getOutput().getCount());
    }
    @Test public void insufficientOutputRoomEjectsWholeOldStack() {
        Probe mill = mill();
        for (int i = 0; i < 33; i++) {
            side(mill, EnumFacing.UP).insertItem(0, new ItemStack(Items.WHEAT, 3), false); ticks(mill, 60);
        }
        assertEquals(1, mill.drops.size()); assertEquals(64, mill.drops.get(0).getCount()); assertEquals(2, mill.getOutput().getCount());
    }
    @Test public void idleOutputEjectsOnlyAfterThreeSecondsWithoutCollector() {
        Probe mill = mill(); side(mill, EnumFacing.UP).insertItem(0, new ItemStack(Items.WHEAT, 3), false); ticks(mill, 60);
        mill.collector = false; ticks(mill, 60); assertFalse(mill.getOutput().isEmpty());
        ticks(mill, 1); assertTrue(mill.getOutput().isEmpty()); assertEquals(1, mill.drops.size());
        Probe collected = mill(); side(collected, EnumFacing.UP).insertItem(0, new ItemStack(Items.WHEAT, 3), false); ticks(collected, 200);
        assertEquals(2, collected.getOutput().getCount()); assertTrue(collected.drops.isEmpty());
    }
    @Test public void stateSurvivesUnloadAndBreakDropsBothSlotsOnce() {
        Probe original = mill(); side(original, EnumFacing.UP).insertItem(0, new ItemStack(Items.WHEAT, 9), false); ticks(original, 80);
        Probe loaded = mill(); loaded.readFromNBT(original.writeToNBT(new net.minecraft.nbt.NBTTagCompound()));
        assertEquals(20, loaded.getProgress()); assertTrue(loaded.isWorking()); ticks(loaded, 40);
        assertEquals(3, loaded.getInput().getCount()); assertEquals(4, loaded.getOutput().getCount());
        loaded.dropContents(); loaded.dropContents(); assertEquals(2, loaded.drops.size());
        assertTrue(loaded.getInput().isEmpty()); assertTrue(loaded.getOutput().isEmpty());
    }
    @Test public void removingInputCancelsPartialBatchAndNeverConsumesAnIncompleteBatch() {
        Probe mill = mill(); side(mill, EnumFacing.UP).insertItem(0, new ItemStack(Items.WHEAT, 3), false); ticks(mill, 30);
        side(mill, EnumFacing.WEST).extractItem(0, 1, false); ticks(mill, 1); assertFalse(mill.isWorking()); assertEquals(0, mill.getProgress());
        side(mill, EnumFacing.UP).insertItem(0, new ItemStack(Items.WHEAT, 1), false); ticks(mill, 59); assertTrue(mill.getOutput().isEmpty());
        ticks(mill, 1); assertEquals(2, mill.getOutput().getCount());
    }
    @Test public void manualInsertionUsesWholeBatchesAndLeavesRemainderInHand() {
        Probe mill = mill(); EntityPlayer player = mock(EntityPlayer.class); player.capabilities = new PlayerCapabilities();
        ItemStack held = new ItemStack(Items.WHEAT, 10); when(player.getHeldItem(EnumHand.MAIN_HAND)).thenReturn(held);
        assertTrue(mill.insertHeld(player, EnumHand.MAIN_HAND)); assertEquals(9, mill.getInput().getCount()); assertEquals(1, held.getCount());
        assertFalse(mill.insertHeld(player, EnumHand.MAIN_HAND));
        ItemStack big = new ItemStack(Items.WHEAT, 64); when(player.getHeldItem(EnumHand.MAIN_HAND)).thenReturn(big);
        assertTrue(mill.insertHeld(player, EnumHand.MAIN_HAND)); assertEquals(63, mill.getInput().getCount()); assertEquals(10, big.getCount());
    }
    private ItemStack powder(SpiceKind kind) { return Seasoning.powder(Collections.singletonList(kind), 1); }
    private void dualRecipes() {
        recipes.clear();
        SpiceRecipes.register(new SpiceRecipe(new ItemStack(Items.WHEAT), 3, powder(SpiceKind.SUGAR), 1));
        SpiceRecipes.register(new SpiceRecipe(new ItemStack(Items.WHEAT), 3, new ItemStack(Items.CARROT), 2,
            powder(SpiceKind.SALT), 2, new ItemStack(Items.GLASS_BOTTLE), new ItemStack(Items.BOWL)));
    }
    @Test public void overlappingIngredientAnimatesForFiveSecondsThenMakesContinuousSingleBatches() {
        dualRecipes(); Probe mill = mill(); IItemHandler input = side(mill, EnumFacing.UP);
        input.insertItem(0, new ItemStack(Items.WHEAT, 9), false);
        ticks(mill, 100); assertTrue(mill.isWorking()); assertEquals(0, mill.getProgress()); assertTrue(mill.getOutput().isEmpty());
        ticks(mill, 59); assertTrue(mill.getOutput().isEmpty()); ticks(mill, 1); assertEquals(1, mill.getOutput().getCount());
        ticks(mill, 60); assertEquals(2, mill.getOutput().getCount());
    }
    @Test public void completeDualIngredientsSkipWaitingInEitherOrderAndReturnBothContainers() {
        dualRecipes();
        for (boolean reverse : new boolean[]{false, true}) {
            Probe mill = mill(); IItemHandler input = side(mill, EnumFacing.UP);
            input.insertItem(0, reverse ? new ItemStack(Items.CARROT, 2) : new ItemStack(Items.WHEAT, 3), false);
            ticks(mill, 30);
            input.insertItem(1, reverse ? new ItemStack(Items.WHEAT, 3) : new ItemStack(Items.CARROT, 2), false);
            ticks(mill, 59); assertTrue(mill.getOutput().isEmpty()); ticks(mill, 1);
            assertEquals("salt", ItemSpice.flavor(mill.getOutput())); assertEquals(2, mill.getOutput().getCount());
            assertTrue(mill.getInput().isEmpty()); assertTrue(mill.getSecondInput().isEmpty());
            assertEquals(3, mill.drops.stream().filter(item -> item.getItem() == Items.GLASS_BOTTLE).mapToInt(ItemStack::getCount).sum());
            assertEquals(2, mill.drops.stream().filter(item -> item.getItem() == Items.BOWL).mapToInt(ItemStack::getCount).sum());
        }
    }
    @Test public void redstoneEjectsSecondIngredientIntoCollectorAndStartsSingleRecipeWithoutWaiting() {
        dualRecipes(); Probe mill = mill(); IItemHandler input = side(mill, EnumFacing.UP);
        input.insertItem(0, new ItemStack(Items.WHEAT, 3), false); input.insertItem(1, new ItemStack(Items.CARROT, 2), false);
        net.minecraft.tileentity.TileEntity collector = mock(net.minecraft.tileentity.TileEntity.class);
        ItemStackHandler storage = new ItemStackHandler(1);
        when(collector.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP)).thenReturn(storage);
        when(mill.getWorld().getTileEntity(mill.getPos().down())).thenReturn(collector);
        when(mill.getWorld().isBlockPowered(mill.getPos())).thenReturn(true);
        assertEquals(1, input.insertItem(1, new ItemStack(Items.CARROT), false).getCount());
        ticks(mill, 60); assertEquals(2, storage.getStackInSlot(0).getCount()); assertEquals("sugar", ItemSpice.flavor(mill.getOutput()));
        assertTrue(mill.getSecondInput().isEmpty()); assertTrue(mill.drops.isEmpty());
    }
    @Test public void bindingAcceptsAllProductRoutesRejectsOtherInputsAndOverridesRedstone() {
        dualRecipes(); SpiceRecipes.register(new SpiceRecipe(new ItemStack(Items.SUGAR), 2, powder(SpiceKind.SALT), 1));
        Probe mill = mill(); IItemHandler input = side(mill, EnumFacing.UP);
        input.insertItem(0, new ItemStack(Items.WHEAT, 3), false);
        EntityPlayer player = mock(EntityPlayer.class); player.capabilities = new PlayerCapabilities();
        when(player.isSneaking()).thenReturn(true); when(player.getHeldItem(EnumHand.MAIN_HAND)).thenReturn(powder(SpiceKind.SALT));
        assertTrue(mill.insertHeld(player, EnumHand.MAIN_HAND)); assertEquals("salt", mill.binding());
        when(mill.getWorld().isBlockPowered(mill.getPos())).thenReturn(true);
        assertTrue(input.insertItem(1, new ItemStack(Items.CARROT, 2), false).isEmpty()); ticks(mill, 60);
        assertEquals("salt", ItemSpice.flavor(mill.getOutput()));
        assertEquals(1, input.insertItem(0, new ItemStack(Items.STICK), false).getCount());
        assertTrue(input.insertItem(0, new ItemStack(Items.SUGAR, 2), false).isEmpty()); ticks(mill, 60); assertEquals(3, mill.getOutput().getCount());
        when(player.getHeldItem(EnumHand.MAIN_HAND)).thenReturn(ItemStack.EMPTY);
        assertTrue(mill.insertHeld(player, EnumHand.MAIN_HAND)); assertEquals("", mill.binding());
    }
    @Test public void secondInputRejectsIdenticalItemsAndOrphansAreReturnedAfterThreeSeconds() {
        dualRecipes(); Probe mill = mill(); IItemHandler input = side(mill, EnumFacing.UP);
        assertEquals(2, input.getSlots()); assertEquals(1, input.insertItem(1, new ItemStack(Items.CARROT), false).getCount());
        input.insertItem(0, new ItemStack(Items.WHEAT, 3), false);
        assertEquals(1, input.insertItem(1, new ItemStack(Items.WHEAT), false).getCount());
        input.insertItem(1, new ItemStack(Items.CARROT, 2), false); input.extractItem(0, 3, false);
        ticks(mill, 59); assertEquals(2, mill.getSecondInput().getCount()); ticks(mill, 1);
        assertTrue(mill.getSecondInput().isEmpty()); assertEquals(2, mill.drops.get(0).getCount());
    }
    @Test public void oldTwoSlotInventoryMigratesOutputAndRetainsPartialSingleBatch() {
        Probe original = mill(); ItemStackHandler old = new ItemStackHandler(2);
        old.setStackInSlot(0, new ItemStack(Items.WHEAT, 6)); old.setStackInSlot(1, spice("a",0xFF0000));
        net.minecraft.nbt.NBTTagCompound tag = original.writeToNBT(new net.minecraft.nbt.NBTTagCompound());
        tag.setTag("Inventory", old.serializeNBT()); tag.setInteger("Progress", 25); tag.setBoolean("Working", true);
        Probe loaded = mill(); loaded.readFromNBT(tag);
        assertEquals("a",ItemSpice.flavor(loaded.getOutput())); assertTrue(loaded.getSecondInput().isEmpty()); assertEquals(25,loaded.getProgress());
        ticks(loaded,35); assertEquals(3,loaded.getOutput().getCount()); assertEquals(3,loaded.getInput().getCount());
    }
    @Test public void waitingProgressBindingAndPreviewSurviveSaveAndClientPacket() throws Exception {
        dualRecipes(); Probe mill = mill(); side(mill,EnumFacing.UP).insertItem(0,new ItemStack(Items.WHEAT,6),false); ticks(mill,40);
        Probe loaded = mill(); loaded.readFromNBT(mill.writeToNBT(new net.minecraft.nbt.NBTTagCompound()));
        ticks(loaded,60); assertEquals(0,loaded.getProgress()); ticks(loaded,60); assertEquals(1,loaded.getOutput().getCount());
        EntityPlayer player=mock(EntityPlayer.class); player.capabilities=new PlayerCapabilities(); when(player.isSneaking()).thenReturn(true);
        when(player.getHeldItem(EnumHand.MAIN_HAND)).thenReturn(powder(SpiceKind.SALT)); assertTrue(loaded.insertHeld(player,EnumHand.MAIN_HAND));
        Probe client=mill(); java.lang.reflect.Field remote=World.class.getDeclaredField("isRemote"); remote.setAccessible(true); remote.setBoolean(client.getWorld(),true); client.handleUpdateTag(loaded.getUpdateTag());
        assertEquals("salt",client.binding()); when(client.getWorld().getTotalWorldTime()).thenReturn(0L); assertEquals("salt",ItemSpice.flavor(client.displayStack(0)));
        when(client.getWorld().getTotalWorldTime()).thenReturn(30L); assertEquals(Items.WHEAT,client.displayStack(0).getItem());
        when(client.getWorld().getTotalWorldTime()).thenReturn(60L); assertEquals(Items.CARROT,client.displayStack(0).getItem());
    }
    @Test public void overflowFirstFillsCollectorAndOnlyDropsRemainder() {
        Probe mill=mill(); IItemHandler input=side(mill,EnumFacing.UP);
        net.minecraft.tileentity.TileEntity collector=mock(net.minecraft.tileentity.TileEntity.class); ItemStackHandler storage=new ItemStackHandler(1);
        when(collector.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,EnumFacing.UP)).thenReturn(storage);
        when(mill.getWorld().getTileEntity(mill.getPos().down())).thenReturn(collector);
        storage.setStackInSlot(0,spice("a",0xFF0000)); storage.getStackInSlot(0).setCount(63);
        input.insertItem(0,new ItemStack(Items.WHEAT,3),false); ticks(mill,60);
        input.insertItem(0,new ItemStack(Items.CARROT,2),false); ticks(mill,60);
        assertEquals(64,storage.getStackInSlot(0).getCount()); assertEquals(1,mill.drops.size()); assertEquals(1,mill.drops.get(0).getCount());
        assertEquals("b",ItemSpice.flavor(mill.getOutput()));
    }
    private static final class Probe extends TileEntitySpiceGrinder {
        final List<ItemStack> drops = new ArrayList<>(); boolean collector = true;
        @Override protected boolean hasCollectorBelow() { return collector; }
        @Override protected void drop(ItemStack stack) { if (!stack.isEmpty()) drops.add(stack.copy()); }
    }
}
