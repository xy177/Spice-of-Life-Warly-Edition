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
    private static final class Probe extends TileEntitySpiceGrinder {
        final List<ItemStack> drops = new ArrayList<>(); boolean collector = true;
        @Override protected boolean hasCollectorBelow() { return collector; }
        @Override protected void drop(ItemStack stack) { if (!stack.isEmpty()) drops.add(stack.copy()); }
    }
}
