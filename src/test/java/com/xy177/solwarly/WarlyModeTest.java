package com.xy177.solwarly;

import com.cazsius.solcarrot.api.*;
import com.xy177.solwarly.command.CommandWarly;
import com.xy177.solwarly.diet.*;
import com.xy177.solwarly.spice.SpiceEffects;
import com.xy177.solwarly.tracking.*;
import net.minecraft.command.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FoodStats;
import net.minecraft.world.*;
import net.minecraft.world.storage.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.junit.*;
import squeek.applecore.api.*;
import squeek.applecore.api.food.*;
import java.lang.reflect.Field;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WarlyModeTest {
    private Capability<DietData> previousDiet;
    private Capability<FoodCapability> previousFood;
    private IAppleCoreAccessor previousAccessor;
    private boolean previousMode, previousReset, previousReady;
    private int previousDays;
    private double[] previousMultipliers;
    private String[] previousCraved, previousBoring;
    private Map<String, Double> coefficients;
    private final DietData diet = new DietData();
    private final AppetiteData appetite = new AppetiteData();
    private final WarlyClockData clock = new WarlyClockData();
    private EntityPlayer player;
    private WorldServer world;
    private WorldInfo worldInfo;
    private FoodStats stats;

    @BeforeClass public static void bootstrap() { Bootstrap.register(); }
    @Before @SuppressWarnings("unchecked") public void setup() throws Exception {
        previousDiet = DietCapability.TYPE; previousFood = SOLCarrotAPI.foodCapability;
        previousAccessor = AppleCoreAPI.accessor;
        previousMode = DietConfig.warlyMode; previousDays = DietConfig.warlyDurationDays;
        previousMultipliers = DietConfig.warlyRepeatMultipliers; previousReset = SOLWarlyConfig.shouldResetOnDeath;
        previousCraved = DietConfig.alwaysCraved; previousBoring = DietConfig.alwaysBoring;
        DietConfig.warlyMode = true; DietConfig.warlyDurationDays = 2;
        DietConfig.warlyRepeatMultipliers = new double[] {1, .9, .8, .65, .5, .3};
        DietConfig.alwaysCraved = new String[] {"minecraft:bread@0"}; DietConfig.alwaysBoring = new String[0];
        DietCapability.TYPE = mock(Capability.class); SOLCarrotAPI.foodCapability = mock(Capability.class);
        AppleCoreAPI.accessor = mock(IAppleCoreAccessor.class);
        when(AppleCoreAPI.accessor.isFood(any(ItemStack.class))).thenAnswer(call -> ((ItemStack)call.getArgument(0)).getItem() instanceof net.minecraft.item.ItemFood);
        when(AppleCoreAPI.accessor.getFoodValues(any(ItemStack.class))).thenReturn(new FoodValues(4, .3F));
        Field ready = FoodCatalog.class.getDeclaredField("ready"); ready.setAccessible(true);
        previousReady = ready.getBoolean(null); ready.setBoolean(null, true);
        Field values = FoodCatalog.class.getDeclaredField("coefficients"); values.setAccessible(true);
        coefficients = new HashMap<>((Map<String, Double>) values.get(null));
        ((Map<String, Double>) values.get(null)).put("minecraft:apple@0", 4.0);
        ((Map<String, Double>) values.get(null)).put("minecraft:bread@0", 4.0);
        world = mock(WorldServer.class); worldInfo = mock(WorldInfo.class);
        Field provider = World.class.getDeclaredField("provider"); provider.setAccessible(true); provider.set(world, mock(WorldProvider.class));
        when(world.getWorldInfo()).thenReturn(worldInfo);
        when(world.getGameRules()).thenReturn(new GameRules());
        Field random = World.class.getDeclaredField("rand"); random.setAccessible(true); random.set(world, new Random(177));
        MinecraftServer server = mock(MinecraftServer.class);
        when(world.getMinecraftServer()).thenReturn(server); when(server.getWorld(0)).thenReturn(world);
        MapStorage storage = mock(MapStorage.class); when(world.getMapStorage()).thenReturn(storage);
        when(storage.getOrLoadData(AppetiteData.class, "solwarly_appetite")).thenReturn(appetite);
        when(storage.getOrLoadData(WarlyClockData.class, "solwarly_clock")).thenReturn(clock);
        player = mock(EntityPlayer.class); player.world = world;
        when(player.getServer()).thenReturn(server);
        when(player.getCapability(DietCapability.TYPE, null)).thenReturn(diet);
        when(player.getCapability(SOLCarrotAPI.foodCapability, null)).thenReturn(new FoodList());
        stats = new FoodStats(); stats.setFoodLevel(10); stats.setFoodSaturationLevel(0);
        when(player.getFoodStats()).thenReturn(stats);
        diet.initialized = true;
    }
    @After @SuppressWarnings("unchecked") public void restore() throws Exception {
        DietCapability.TYPE = previousDiet; SOLCarrotAPI.foodCapability = previousFood;
        AppleCoreAPI.accessor = previousAccessor;
        DietConfig.warlyMode = previousMode; DietConfig.warlyDurationDays = previousDays;
        DietConfig.warlyRepeatMultipliers = previousMultipliers; SOLWarlyConfig.shouldResetOnDeath = previousReset;
        DietConfig.alwaysCraved = previousCraved; DietConfig.alwaysBoring = previousBoring;
        Field ready = FoodCatalog.class.getDeclaredField("ready"); ready.setAccessible(true); ready.setBoolean(null, previousReady);
        Field values = FoodCatalog.class.getDeclaredField("coefficients"); values.setAccessible(true);
        Map<String, Double> current = (Map<String, Double>) values.get(null); current.clear(); current.putAll(coefficients);
    }

    @Test public void naturalTimeCommandsSleepRollbackAndReloadKeepElapsedTimeCorrect() {
        when(worldInfo.getWorldTotalTime()).thenReturn(100L); assertEquals(0, clock.advance(world));
        when(worldInfo.getWorldTotalTime()).thenReturn(101L); assertEquals(1, clock.advance(world));
        when(world.getWorldTime()).thenReturn(1000000L); assertEquals(1, clock.advance(world)); // /time add changes only daylight
        when(world.getWorldTime()).thenReturn(23000L); clock.beginTick(world); // /time set backwards
        when(world.getWorldTime()).thenReturn(24000L); clock.recordSleep(world);
        assertEquals(1001, clock.advance(world));
        clock.recordSleep(world); assertEquals(1001, clock.advance(world)); // another sleeper, same skip
        assertTrue(clock.consumePendingSync()); assertFalse(clock.consumePendingSync());
        WarlyClockData loaded = new WarlyClockData(); loaded.readFromNBT(clock.writeToNBT(new NBTTagCompound()));
        when(worldInfo.getWorldTotalTime()).thenReturn(102L); assertEquals(1002, loaded.advance(world));
        when(worldInfo.getWorldTotalTime()).thenReturn(10L); assertEquals(1002, loaded.advance(world));
        when(worldInfo.getWorldTotalTime()).thenReturn(11L); assertEquals(1003, loaded.advance(world));
        when(world.getWorldTime()).thenReturn(23000L); loaded.beginTick(world);
        when(world.getWorldTime()).thenReturn(24000L); loaded.recordSleep(world); // same dawn after a later rollback must still count
        assertEquals(2003, loaded.advance(world));
    }
    @Test public void canceledSleepAndFrozenDaylightDoNotAddSkippedTime() {
        when(world.getWorldTime()).thenReturn(23000L); clock.beginTick(world);
        clock.recordSleep(world); assertEquals(0, clock.advance(world));
        world.getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
        when(world.getWorldTime()).thenReturn(24000L); clock.recordSleep(world); assertEquals(0, clock.advance(world));
    }
    @Test public void actualNutritionAndContributionUseSameRepeatButExperienceIsUnchanged() {
        ItemStack apple = new ItemStack(Items.APPLE);
        DietRuntime.eat(player, apple);
        FoodEvent.GetPlayerFoodValues nutrition = new FoodEvent.GetPlayerFoodValues(player, apple, new FoodValues(4, .3F));
        SpiceEffects.appetizingValues(nutrition);
        assertEquals(4, nutrition.foodValues.hunger);
        assertEquals(2.16, nutrition.foodValues.getUnboundedSaturationIncrement(), .00001);
        DietRuntime.eat(player, apple);
        assertEquals(4, diet.cumulative(), .00001);
        assertEquals(3.6, diet.meals().get(1).contribution, .00001);
        ItemStack salted = apple.copy(); salted.setTagCompound(new NBTTagCompound());
        salted.getTagCompound().setTag("SolWarlySeasoning", new NBTTagCompound());
        assertEquals(.8, DietRuntime.warlyMultiplier(player, salted), .00001);
    }
    @Test public void cravedMealsDoNotCountOrResetExistingExpiry() {
        String bread = "minecraft:bread@0";
        diet.recordWarlyMeal(bread, 0);
        appetite.refresh(0, 1, Collections.emptyList(), new Random(1));
        when(worldInfo.getWorldTotalTime()).thenReturn(0L); clock.advance(world);
        when(worldInfo.getWorldTotalTime()).thenReturn(24000L);
        assertEquals(1, DietRuntime.warlyMultiplier(player, new ItemStack(Items.BREAD)), 0);
        DietRuntime.eat(player, new ItemStack(Items.BREAD));
        assertEquals(1, diet.warlyRepeatCount(bread, 24000));
        assertEquals(0, diet.warlyRepeatCount(bread, 48000));
        assertEquals(4 * DietConfig.cravingMultiplier, diet.meals().get(0).contribution, .00001);
    }
    @Test public void hungerRoundingKeepsSaturationAndSpeedForEveryTier() {
        for (double multiplier : DietConfig.warlyRepeatMultipliers) {
            FoodValues scaled = WarlyNutrition.scale(new FoodValues(5, .7F), multiplier);
            assertEquals((int) Math.round(5 * multiplier), scaled.hunger);
            assertEquals(7 * multiplier, scaled.getUnboundedSaturationIncrement(), .00001);
        }
        assertEquals(107, DietRuntime.scaledUseDuration(32, .3));
        assertEquals(0, DietRuntime.scaledUseDuration(0, .3));
        FoodValues scaled = WarlyNutrition.scale(new FoodValues(1, .5F), .3);
        assertEquals(0, scaled.hunger); assertEquals(.3, scaled.getUnboundedSaturationIncrement(), .00001);
        FoodEvent.FoodEaten eaten = new FoodEvent.FoodEaten(player, new ItemStack(Items.APPLE), scaled, 0, 0);
        SpiceEffects.zeroHungerSaturation(eaten);
        assertEquals(.3, stats.getSaturationLevel(), .00001); assertEquals(10, stats.getFoodLevel());
        assertEquals(.3, WarlyNutrition.saturationAdded(eaten), .00001);
    }
    @Test public void slowEatingDoesNotAffectBowOrUnrelatedDrinkActions() {
        diet.recordWarlyMeal("minecraft:apple@0", 0);
        LivingEntityUseItemEvent.Start apple = new LivingEntityUseItemEvent.Start(player, new ItemStack(Items.APPLE), 32);
        SpiceEffects.useStarted(apple); assertEquals(36, apple.getDuration());
        LivingEntityUseItemEvent.Start bow = new LivingEntityUseItemEvent.Start(player, new ItemStack(Items.BOW), 72000);
        SpiceEffects.useStarted(bow); assertEquals(72000, bow.getDuration());
        LivingEntityUseItemEvent.Start milk = new LivingEntityUseItemEvent.Start(player, new ItemStack(Items.MILK_BUCKET), 32);
        SpiceEffects.useStarted(milk); assertEquals(32, milk.getDuration());
    }
    @Test public void deathResetPreservesModeAndRepeatsButResetsExistingDietRules() {
        diet.setWarlyOverride(1); diet.recordWarlyMeal("minecraft:apple@0", 0);
        diet.eat("minecraft:apple@0", "plain", 4, 4, 32);
        EntityPlayerMP clone = mock(EntityPlayerMP.class); clone.world = world;
        DietData cloned = new DietData(); FoodList clonedFood = new FoodList();
        when(clone.getCapability(DietCapability.TYPE, null)).thenReturn(cloned);
        when(clone.getCapability(SOLCarrotAPI.foodCapability, null)).thenReturn(clonedFood);
        SOLWarlyConfig.shouldResetOnDeath = true;
        // Stop immediately before the network sync, which requires a running Forge server.
        net.minecraft.entity.ai.attributes.IAttributeInstance health = mock(net.minecraft.entity.ai.attributes.IAttributeInstance.class);
        when(clone.getEntityAttribute(any())).thenReturn(health);
        doThrow(new IllegalStateException("sync boundary")).when(health).removeModifier(any(net.minecraft.entity.ai.attributes.AttributeModifier.class));
        try { CapabilityHandler.onClone(new PlayerEvent.Clone(clone, player, true)); fail(); }
        catch (IllegalStateException e) { assertEquals("sync boundary", e.getMessage()); }
        assertEquals(1, cloned.warlyOverride()); assertEquals(.9, cloned.warlyMultiplier("minecraft:apple@0", 1), 0);
        assertEquals(0, cloned.cumulative(), 0); assertEquals(0, cloned.mealCount());
    }
    @Test public void modeCommandIsRestrictedToAdministrators() throws Exception {
        ICommandSender sender = mock(ICommandSender.class);
        try { new CommandWarly().execute(world.getMinecraftServer(), sender, new String[] {"mode", "xy177", "true"}); fail(); }
        catch (CommandException e) { assertEquals("commands.generic.permission", e.getMessage()); }
    }
    @Test public void commandTabCompletionSupportsSubcommandsPlayerNamesAndModeValues() {
        CommandWarly command = new CommandWarly(); ICommandSender sender = mock(ICommandSender.class);
        MinecraftServer server = world.getMinecraftServer();
        when(server.getOnlinePlayerNames()).thenReturn(new String[] {"xy177", "other"});
        when(sender.canUseCommand(2, "solwarly")).thenReturn(true);
        assertEquals(Collections.singletonList("mode"), command.getTabCompletions(server, sender, new String[] {"mo"}, null));
        assertEquals(Collections.singletonList("xy177"), command.getTabCompletions(server, sender, new String[] {"mode", "xy"}, null));
        assertEquals(Arrays.asList("true", "false", "default"), command.getTabCompletions(server, sender, new String[] {"mode", "xy177", ""}, null));
        assertEquals(Collections.singletonList("false"), command.getTabCompletions(server, sender, new String[] {"mode", "xy177", "f"}, null));
        assertTrue(command.getTabCompletions(server, sender, new String[] {"mode", "xy177", "true", ""}, null).isEmpty());
        assertTrue(command.isUsernameIndex(new String[] {"mode", "xy177"}, 1));
        assertFalse(command.isUsernameIndex(new String[] {"regenerate", "food"}, 1));
        when(sender.canUseCommand(2, "solwarly")).thenReturn(false);
        assertFalse(command.getTabCompletions(server, sender, new String[] {""}, null).contains("mode"));
        assertTrue(command.getTabCompletions(server, sender, new String[] {"mode", "xy177", ""}, null).isEmpty());
    }
    @Test public void clientUsesServerModeSettingsForNutritionAndLunchSelection() throws Exception {
        boolean oldMode = DietRuntime.clientWarlyMode;
        int oldDays = DietRuntime.clientWarlyDurationDays;
        double[] oldMultipliers = DietRuntime.clientWarlyRepeatMultipliers;
        long oldClock = DietRuntime.clientWarlyClock;
        Field totalTime = DietRuntime.class.getDeclaredField("clientWarlyTotalTime"); totalTime.setAccessible(true);
        long oldTotal = totalTime.getLong(null);
        NBTTagCompound oldAppetite = DietRuntime.clientAppetite.writeToNBT(new NBTTagCompound());
        Map<String, Double> oldCoefficients = new HashMap<>(DietRuntime.clientCoefficients);
        try {
            Field remote = World.class.getDeclaredField("isRemote"); remote.setAccessible(true); remote.setBoolean(world, true);
            DietRuntime.clientWarlyMode = true; DietRuntime.clientWarlyDurationDays = 2;
            DietRuntime.clientWarlyRepeatMultipliers = new double[] {1, .9, .8, .65, .5, .3};
            DietRuntime.clientWarlyClock = 0; totalTime.setLong(null, 0);
            DietRuntime.clientAppetite.readFromNBT(new NBTTagCompound());
            for (int i = 0; i < 5; i++) diet.recordWarlyMeal("minecraft:apple@0", 0);
            DietConfig.warlyMode = false; // local global config must not override the remote server
            FoodValues scaled = DietRuntime.applyWarlyValues(player, new ItemStack(Items.APPLE), new FoodValues(4, .3F));
            assertEquals(1, scaled.hunger); assertEquals(.72, scaled.getUnboundedSaturationIncrement(), .00001);
            assertEquals(107, DietRuntime.useDuration(player, new ItemStack(Items.APPLE), 32));
            DietRuntime.clientCoefficients.put("minecraft:apple@0", 4.0); DietRuntime.clientCoefficients.put("minecraft:bread@0", 2.0);
            com.xy177.solwarly.lunch.LunchInventory inventory = new com.xy177.solwarly.lunch.LunchInventory(
                new ItemStack(com.xy177.solwarly.lunch.LunchRegistry.bag));
            inventory.setStackInSlot(0, new ItemStack(Items.APPLE)); inventory.setStackInSlot(1, new ItemStack(Items.BREAD));
            assertEquals(1, com.xy177.solwarly.lunch.ItemLunchContainer.bestFood(inventory, player));
            ItemStack bag = new ItemStack(com.xy177.solwarly.lunch.LunchRegistry.bag);
            new com.xy177.solwarly.lunch.LunchInventory(bag).setStackInSlot(0, new ItemStack(Items.APPLE));
            LivingEntityUseItemEvent.Start eatBag = new LivingEntityUseItemEvent.Start(player, bag, 32);
            SpiceEffects.useStarted(eatBag); assertEquals(107, eatBag.getDuration());
            DietRuntime.clientAppetite.craved.add("minecraft:apple@0");
            assertEquals(32, DietRuntime.useDuration(player, new ItemStack(Items.APPLE), 32));
        } finally {
            DietRuntime.clientWarlyMode = oldMode; DietRuntime.clientWarlyDurationDays = oldDays;
            DietRuntime.clientWarlyRepeatMultipliers = oldMultipliers; DietRuntime.clientWarlyClock = oldClock;
            totalTime.setLong(null, oldTotal); DietRuntime.clientAppetite.readFromNBT(oldAppetite);
            DietRuntime.clientCoefficients.clear(); DietRuntime.clientCoefficients.putAll(oldCoefficients);
        }
    }
}
