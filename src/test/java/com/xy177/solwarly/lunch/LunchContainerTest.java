package com.xy177.solwarly.lunch;

import com.cazsius.solcarrot.api.*;
import com.xy177.solwarly.SOLWarlyConfig;
import com.xy177.solwarly.api.FlavorAPI;
import com.xy177.solwarly.diet.*;
import com.xy177.solwarly.spice.*;
import com.xy177.solwarly.tracking.FoodList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.*;
import squeek.applecore.api.*;
import squeek.applecore.api.food.FoodValues;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LunchContainerTest {
    private IAppleCoreAccessor previousAccessor;
    private Capability<DietData> previousDiet;
    private Capability<FoodCapability> previousLegacy;
    private final Map<String, Double> coefficients = new HashMap<>();
    private NBTTagCompound appetite;
    private double oldExtra, oldScale, oldCraving, oldNormal, oldBoring;
    private int oldCount;

    @BeforeClass public static void bootstrap() {
        Bootstrap.register();
        for (Item item : Arrays.asList(LunchRegistry.bag, LunchRegistry.box, LunchRegistry.goldenBox))
            if (!ForgeRegistries.ITEMS.containsKey(item.getRegistryName())) ForgeRegistries.ITEMS.register(item);
    }
    @Before public void setup() {
        previousAccessor = AppleCoreAPI.accessor;
        AppleCoreAPI.accessor = mock(IAppleCoreAccessor.class);
        when(AppleCoreAPI.accessor.isFood(any(ItemStack.class))).thenAnswer(call -> ((ItemStack)call.getArgument(0)).getItem() instanceof ItemFood);
        when(AppleCoreAPI.accessor.getFoodValues(any(ItemStack.class))).thenReturn(new FoodValues(4, .3F));
        previousDiet = DietCapability.TYPE;
        previousLegacy = SOLCarrotAPI.foodCapability;
        DietCapability.TYPE = mock(Capability.class);
        SOLCarrotAPI.foodCapability = mock(Capability.class);
        coefficients.putAll(DietRuntime.clientCoefficients);
        appetite = DietRuntime.clientAppetite.writeToNBT(new NBTTagCompound());
        oldCount = DietRuntime.clientRecentCount; oldExtra = DietRuntime.clientExtraFlavor; oldScale = DietRuntime.clientScale;
        oldCraving = DietRuntime.clientCravingMultiplier; oldNormal = DietRuntime.clientNormalMultiplier; oldBoring = DietRuntime.clientBoringMultiplier;
        DietRuntime.clientCoefficients.clear(); DietRuntime.clientAppetite.craved.clear(); DietRuntime.clientAppetite.boring.clear();
        DietRuntime.clientRecentCount = 2; DietRuntime.clientExtraFlavor = .25; DietRuntime.clientScale = 1;
        DietRuntime.clientCravingMultiplier = 1.25; DietRuntime.clientNormalMultiplier = 1; DietRuntime.clientBoringMultiplier = .75;
    }
    @After public void restore() {
        AppleCoreAPI.accessor = previousAccessor;
        DietCapability.TYPE = previousDiet; SOLCarrotAPI.foodCapability = previousLegacy;
        DietRuntime.clientCoefficients.clear(); DietRuntime.clientCoefficients.putAll(coefficients);
        DietRuntime.clientAppetite.readFromNBT(appetite);
        DietRuntime.clientRecentCount = oldCount; DietRuntime.clientExtraFlavor = oldExtra; DietRuntime.clientScale = oldScale;
        DietRuntime.clientCravingMultiplier = oldCraving; DietRuntime.clientNormalMultiplier = oldNormal; DietRuntime.clientBoringMultiplier = oldBoring;
    }
    private ItemStack flavoredApple(int count) {
        ItemStack food = Seasoning.apply(new ItemStack(Items.APPLE), Collections.singletonList(SpiceKind.SALT), 0x123456);
        food.setCount(count); food.setStackDisplayName("Custom Apple");
        food.getTagCompound().setString("OtherMod", "keep"); return food;
    }
    private EntityPlayer player(World world) {
        EntityPlayer player = mock(EntityPlayer.class);
        player.world = world; player.inventory = new InventoryPlayer(player);
        when(player.canEat(false)).thenReturn(true); return player;
    }
    private InventoryCrafting upgradeGrid(ItemStack source, Item metal) {
        InventoryCrafting grid = new InventoryCrafting(new Container() {
            @Override public boolean canInteractWith(EntityPlayer player) { return true; }
        }, 3, 3);
        for (int slot : new int[] {1, 3, 5, 6, 7, 8}) grid.setInventorySlotContents(slot, new ItemStack(metal));
        grid.setInventorySlotContents(4, source); return grid;
    }

    @Test public void capacitiesAcceptOnlyFoodAndPersistFullStacksAndNbt() {
        for (ItemLunchContainer item : Arrays.asList(LunchRegistry.bag, LunchRegistry.box, LunchRegistry.goldenBox)) {
            ItemStack bag = new ItemStack(item);
            LunchInventory inventory = new LunchInventory(bag);
            assertEquals(item.slots, inventory.getSlots()); assertEquals(1, bag.getMaxStackSize());
            ItemStack food = flavoredApple(37);
            assertTrue(inventory.insertItem(item.slots - 1, food, false).isEmpty());
            assertFalse(inventory.insertItem(0, new ItemStack(Items.DIAMOND), false).isEmpty());
            assertFalse(inventory.insertItem(0, new ItemStack(LunchRegistry.bag), false).isEmpty());
            ItemStack saved = new ItemStack(bag.writeToNBT(new NBTTagCompound()).copy());
            LunchInventory loaded = new LunchInventory(saved);
            assertTrue(ItemStack.areItemStacksEqual(food, loaded.getStackInSlot(item.slots - 1)));
            loaded.extractItem(item.slots - 1, 3, false);
            assertEquals(34, new LunchInventory(saved).getStackInSlot(item.slots - 1).getCount());
            assertEquals(37, new LunchInventory(bag).getStackInSlot(item.slots - 1).getCount());
        }
    }
    @Test public void simulatedTransfersDoNotAlterPersistentContents() {
        ItemStack bag = new ItemStack(LunchRegistry.bag);
        LunchInventory inventory = new LunchInventory(bag);
        assertTrue(inventory.insertItem(0, flavoredApple(8), true).isEmpty()); assertFalse(bag.hasTagCompound());
        inventory.insertItem(0, flavoredApple(8), false);
        NBTTagCompound before = bag.getTagCompound().copy();
        assertEquals(2, inventory.extractItem(0, 2, true).getCount());
        assertEquals(before, bag.getTagCompound());
    }
    @Test public void bothShapedUpgradesKeepContentsNamesAndIndependentNbt() {
        ItemStack bag = new ItemStack(LunchRegistry.bag); bag.setStackDisplayName("Lunch");
        new LunchInventory(bag).setStackInSlot(4, flavoredApple(17));
        NBTTagCompound original = bag.getTagCompound().copy();
        LunchUpgradeRecipe iron = new LunchUpgradeRecipe(LunchRegistry.bag, Items.IRON_INGOT, LunchRegistry.box);
        InventoryCrafting grid = upgradeGrid(bag, Items.IRON_INGOT);
        assertTrue(iron.matches(grid, null)); assertFalse(iron.canFit(2, 2));
        grid.setInventorySlotContents(0, new ItemStack(Items.IRON_INGOT)); assertFalse(iron.matches(grid, null));
        grid.setInventorySlotContents(0, ItemStack.EMPTY);
        ItemStack box = iron.getCraftingResult(grid);
        assertEquals("Lunch", box.getDisplayName()); assertEquals(original, box.getTagCompound());
        LunchInventory boxContents = new LunchInventory(box); assertEquals(9, boxContents.getSlots());
        boxContents.setStackInSlot(8, new ItemStack(Items.BREAD, 12));
        assertEquals(original, bag.getTagCompound());
        LunchUpgradeRecipe gold = new LunchUpgradeRecipe(LunchRegistry.box, Items.GOLD_INGOT, LunchRegistry.goldenBox);
        InventoryCrafting goldGrid = upgradeGrid(box, Items.GOLD_INGOT); assertTrue(gold.matches(goldGrid, null));
        LunchInventory golden = new LunchInventory(gold.getCraftingResult(goldGrid));
        assertEquals(14, golden.getSlots()); assertEquals(17, golden.getStackInSlot(4).getCount());
        assertEquals(12, golden.getStackInSlot(8).getCount());
        assertTrue(Seasoning.seasoned(golden.getStackInSlot(4)));
    }
    @Test public void predictionMatchesRealMealScoringWithoutChangingAnyHistory() {
        DietData data = new DietData();
        data.eat("apple", "plain", 4, 5, 2); data.eat("bread", "plain", 2, 2, 2);
        NBTTagCompound original = data.serializeNBT();
        for (String flavor : Arrays.asList("plain", "solwarly:salt", "solwarly:mixed:sugar+salt+salt")) {
            double predicted = data.diversityAfter("bread", flavor, 6, 2, .25, 1.3);
            DietData eaten = new DietData(); eaten.deserializeNBT(original);
            eaten.eat("bread", flavor, 2, 6, 2);
            assertEquals(eaten.diversity(.25, 1.3), predicted, 0);
        }
        assertEquals(original, data.serializeNBT());
        DietData empty = new DietData(); assertEquals(7, empty.diversityAfter("a", "plain", 7, 32, .25, 1), 0);
    }
    @Test public void selectingConsidersTheOldestMealLeavingInsteadOfJustTheHighestContribution() {
        LunchInventory inventory = new LunchInventory(new ItemStack(LunchRegistry.bag));
        inventory.setStackInSlot(0, new ItemStack(Items.APPLE)); inventory.setStackInSlot(1, new ItemStack(Items.BREAD));
        DietData data = new DietData(); data.eat("bread", "plain", 4, 4, 2); data.eat("apple", "plain", 4, 4, 2);
        assertEquals(1, ItemLunchContainer.select(inventory, food -> data.diversityAfter(
            food.getItem() == Items.APPLE ? "apple" : "bread", "plain", food.getItem() == Items.APPLE ? 8 : 5, 2, .25, 1)));
    }
    @Test public void actualSelectorUsesSyncedInterestFlavorRulesAndPreservesDiet() throws Exception {
        World world = mock(World.class);
        java.lang.reflect.Field remote = World.class.getDeclaredField("isRemote"); remote.setAccessible(true); remote.setBoolean(world, true);
        EntityPlayer player = player(world);
        SOLWarlyConfig.foodBlacklist = new String[0]; SOLWarlyConfig.foodWhitelist = new String[0]; SOLWarlyConfig.minimumFoodValue = 1;
        SOLWarlyConfig.milestones = new int[] {5};
        FoodList legacy = new FoodList(); DietData diet = new DietData();
        when(player.getCapability(SOLCarrotAPI.foodCapability, null)).thenReturn(legacy);
        when(player.getCapability(DietCapability.TYPE, null)).thenReturn(diet);
        FlavorAPI.register(new ResourceLocation("solwarly:lunch_test"), Seasoning::flavor);
        SpiceConfig.multipliers.put(SpiceKind.SALT, 1.2);
        LunchInventory inventory = new LunchInventory(new ItemStack(LunchRegistry.bag));
        inventory.setStackInSlot(0, new ItemStack(Items.APPLE));
        inventory.setStackInSlot(1, flavoredApple(1));
        inventory.setStackInSlot(2, new ItemStack(Items.BREAD));
        DietRuntime.clientCoefficients.put("minecraft:apple@0", 4.0);
        DietRuntime.clientCoefficients.put("minecraft:bread@0", 4.0);
        NBTTagCompound before = diet.serializeNBT();
        assertEquals(1, ItemLunchContainer.bestFood(inventory, player));
        DietRuntime.clientAppetite.craved.add("minecraft:bread@0");
        assertEquals(2, ItemLunchContainer.bestFood(inventory, player));
        assertEquals(before, diet.serializeNBT());
    }
    @Test public void shiftTransfersAndDirectStackMergesPersistWithoutMovingTheOpenBag() {
        EntityPlayer player = player(mock(World.class));
        ItemStack bag = new ItemStack(LunchRegistry.bag);
        player.inventory.setInventorySlotContents(0, bag);
        LunchInventory contents = new LunchInventory(bag); contents.setStackInSlot(0, new ItemStack(Items.APPLE, 5));
        player.inventory.setInventorySlotContents(9, new ItemStack(Items.APPLE, 3));
        ContainerLunch container = new ContainerLunch(player, 0);
        assertEquals(3, container.transferStackInSlot(player, 5).getCount());
        assertEquals(8, new LunchInventory(bag).getStackInSlot(0).getCount());
        assertTrue(player.inventory.getStackInSlot(9).isEmpty());
        assertTrue(container.transferStackInSlot(player, 32).isEmpty());
        assertTrue(container.slotClick(32, 0, ClickType.THROW, player).isEmpty());
        assertTrue(container.slotClick(0, 0, ClickType.SWAP, player).isEmpty());
        assertSame(bag, player.inventory.getStackInSlot(0));
        assertEquals(8, container.transferStackInSlot(player, 0).getCount());
        assertTrue(new LunchInventory(bag).getStackInSlot(0).isEmpty());
        assertTrue(container.canInteractWith(player));
        player.inventory.setInventorySlotContents(0, ItemStack.EMPTY); assertFalse(container.canInteractWith(player));
    }
    @Test public void offhandContainerAlsoBlocksHotkeySwappingAndRejectsInvalidGuiSlots() {
        EntityPlayer player = player(mock(World.class));
        ItemStack bag = new ItemStack(LunchRegistry.goldenBox);
        player.inventory.setInventorySlotContents(40, bag);
        assertTrue(ContainerLunch.validSlot(player, 40));
        assertFalse(ContainerLunch.validSlot(player, -1)); assertFalse(ContainerLunch.validSlot(player, 36));
        ContainerLunch container = new ContainerLunch(player, 40);
        assertTrue(container.slotClick(0, 40, ClickType.SWAP, player).isEmpty());
        assertSame(bag, player.inventory.getStackInSlot(40));
    }
    @Test public void consumingUsesActualFoodOnceKeepsSeasoningAndReturnsTheContainerItem() throws Exception {
        World world = mock(World.class); EntityPlayer player = player(world);
        final List<ItemStack> eaten = new ArrayList<>();
        ItemFood testFood = new ItemFood(4, .3F, false) {
            @Override public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
                eaten.add(stack.copy()); stack.shrink(1); return new ItemStack(Items.GLASS_BOTTLE);
            }
        };
        ItemStack food = Seasoning.apply(new ItemStack(testFood), Collections.singletonList(SpiceKind.SALT), 0x123456);
        food.setCount(3);
        ItemStack bag = new ItemStack(LunchRegistry.bag);
        LunchInventory inventory = new LunchInventory(bag); inventory.setStackInSlot(0, food);
        // Plain JUnit does not run Forge's event subclass transformer or add its no-arg constructors.
        java.lang.reflect.Field bus = EventBus.class.getDeclaredField("busID"); bus.setAccessible(true);
        int busId = bus.getInt(MinecraftForge.EVENT_BUS);
        int[] finishes = {0};
        IEventListener listener = event -> { if (event instanceof LivingEntityUseItemEvent.Finish) finishes[0]++; };
        ListenerList listeners = new LivingEntityUseItemEvent.Finish(player, food, 0, ItemStack.EMPTY).getListenerList();
        listeners.register(busId, EventPriority.NORMAL, listener);
        try { ItemLunchContainer.consume(inventory, 0, world, player); }
        finally { listeners.unregister(busId, listener); }
        assertEquals(1, eaten.size()); assertEquals(1, eaten.get(0).getCount());
        assertTrue(Seasoning.seasoned(eaten.get(0))); assertEquals(0x123456, Seasoning.color(eaten.get(0)));
        assertEquals(2, inventory.getStackInSlot(0).getCount());
        assertEquals(1, finishes[0]); assertEquals(Items.GLASS_BOTTLE, player.inventory.getStackInSlot(0).getItem());
    }
}
