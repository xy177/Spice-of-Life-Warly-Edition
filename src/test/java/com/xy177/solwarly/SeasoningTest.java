package com.xy177.solwarly;

import com.xy177.solwarly.spice.*;
import com.xy177.solwarly.diet.DietData;
import net.minecraft.init.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import squeek.applecore.api.*;
import squeek.applecore.api.food.*;
import java.io.File;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SeasoningTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder(new File("E:/BOX"));
    private IAppleCoreAccessor previousAccessor;
    private java.lang.reflect.Field minecraftHome;
    private Object previousHome;
    @BeforeClass public static void bootstrap() {
        Bootstrap.register();
        if (!ForgeRegistries.ITEMS.containsKey(new ResourceLocation("solwarly:spice"))) ForgeRegistries.ITEMS.register(SpiceRegistry.spice);
        for (Potion potion : Arrays.asList(SpiceRegistry.sweet, SpiceRegistry.tasty, SpiceRegistry.aromatic))
            if (!ForgeRegistries.POTIONS.containsKey(potion.getRegistryName())) ForgeRegistries.POTIONS.register(potion);
        for (String id : Arrays.asList("farmersdelight:comfort", "farmersdelight:nourishment"))
            if (!ForgeRegistries.POTIONS.containsKey(new ResourceLocation(id))) ForgeRegistries.POTIONS.register(new TestPotion(id));
        if (Item.getByNameOrId("farmersdelight:cooked_rice") == null)
            ForgeRegistries.ITEMS.register(new ItemFood(6, .4F, false).setRegistryName("farmersdelight:cooked_rice"));
    }
    @Before public void setup() throws Exception {
        previousAccessor = AppleCoreAPI.accessor; AppleCoreAPI.accessor = mock(IAppleCoreAccessor.class);
        when(AppleCoreAPI.accessor.isFood(any(ItemStack.class))).thenAnswer(call -> ((ItemStack)call.getArgument(0)).getItem() instanceof ItemFood);
        minecraftHome = net.minecraftforge.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome");
        minecraftHome.setAccessible(true); previousHome = minecraftHome.get(null); minecraftHome.set(null, temp.getRoot());
        SpiceConfig.load(temp.newFolder());
    }
    @After public void restore() throws Exception {
        AppleCoreAPI.accessor = previousAccessor; minecraftHome.set(null, previousHome);
        SpiceConfig.mixedCatalystItem = "minecraft:nether_star";
        SpiceConfig.mixedCatalystMetadata = 0; SpiceConfig.consumeMixedCatalyst = false;
    }
    private ItemStack powder(SpiceKind... kinds) { return Seasoning.powder(Arrays.asList(kinds), 1); }
    private ItemStack rice() { return new ItemStack(Item.getByNameOrId("farmersdelight:cooked_rice")); }
    private InventoryCrafting grid(ItemStack... stacks) {
        InventoryCrafting inventory = new InventoryCrafting(mock(Container.class), 3, 3);
        for (int i = 0; i < stacks.length; i++) inventory.setInventorySlotContents(i, stacks[i]); return inventory;
    }
    @Test public void attainableFlavorsIncludePlainSinglesAndOnlyOneMixedFlavor() throws Exception {
        java.lang.reflect.Field resolvers = com.xy177.solwarly.api.FlavorAPI.class.getDeclaredField("providers"); resolvers.setAccessible(true);
        java.lang.reflect.Field variants = com.xy177.solwarly.api.FlavorAPI.class.getDeclaredField("variants"); variants.setAccessible(true);
        Map originalResolvers = new LinkedHashMap((Map)resolvers.get(null)), originalVariants = new LinkedHashMap((Map)variants.get(null));
        try {
            com.xy177.solwarly.api.FlavorAPI.register(new ResourceLocation("solwarly:test"), Seasoning::flavor);
            com.xy177.solwarly.api.FlavorAPI.registerVariants(new ResourceLocation("solwarly:test"), Seasoning::variants);
            ItemStack apple = new ItemStack(Items.APPLE);
            int singles = 0, powders = 0;
            for (SpiceKind kind : SpiceKind.values()) if (kind != SpiceKind.CREATIVE && kind.available()) { singles++; if (kind != SpiceKind.RICE) powders++; }
            List<ItemStack> all = com.xy177.solwarly.api.FlavorAPI.variants(apple);
            assertEquals(1 + singles + (SpiceKind.RICE.available() ? 1 : 0) + (powders > 0 ? 1 : 0), all.size());
            assertEquals("plain", com.xy177.solwarly.api.FlavorAPI.resolve(all.get(0)).id);
            Set<String> ids = new HashSet<>();
            for (ItemStack food : all) assertTrue(ids.add(com.xy177.solwarly.api.FlavorAPI.resolve(food).id));
            com.xy177.solwarly.api.FlavorAPI.register(new ResourceLocation("solwarly:future"), food -> food.hasTagCompound() && food.getTagCompound().getBoolean("FutureFlavor")
                ? new com.xy177.solwarly.api.FlavorAPI.Flavor("test:future",1) : null);
            com.xy177.solwarly.api.FlavorAPI.registerVariants(new ResourceLocation("solwarly:future"), food -> {
                food.setTagCompound(new NBTTagCompound()); food.getTagCompound().setBoolean("FutureFlavor",true); return Arrays.asList(food,food.copy());
            });
            assertEquals(all.size()+1, com.xy177.solwarly.api.FlavorAPI.variants(apple).size());
            assertFalse(apple.hasTagCompound());
        } finally {
            ((Map)resolvers.get(null)).clear(); ((Map)resolvers.get(null)).putAll(originalResolvers);
            ((Map)variants.get(null)).clear(); ((Map)variants.get(null)).putAll(originalVariants);
        }
    }
    @Test public void seasoningPreservesCustomFoodNbtAndDoesNotMutateInput() {
        ItemStack original = new ItemStack(Items.APPLE, 12); original.setStackDisplayName("My Apple");
        original.getTagCompound().setString("OtherMod", "keep");
        ItemStack output = new SeasonFoodRecipe().getCraftingResult(grid(original, powder(SpiceKind.SUGAR)));
        assertEquals(1, output.getCount()); assertEquals("My Apple", output.getDisplayName());
        assertEquals("keep", output.getTagCompound().getString("OtherMod")); assertFalse(Seasoning.seasoned(original));
        assertEquals(12, original.getCount()); assertEquals(original.getTagCompound(), Seasoning.plain(output).getTagCompound());
    }
    @Test public void foodKeepsItsPowderColorEvenIfTheConfiguredPaletteChangesLater() {
        ItemStack powder = powder(SpiceKind.SUGAR, SpiceKind.SUGAR, SpiceKind.SALT);
        int color = ItemSpice.color(powder, 0);
        ItemStack food = new SeasonFoodRecipe().getCraftingResult(grid(new ItemStack(Items.APPLE), powder));
        SpiceConfig.colors.put(SpiceKind.SUGAR, 0x112233);
        assertEquals(color, Seasoning.color(food)); assertEquals(color, Seasoning.color(powder));
    }
    @Test public void allSeasoningPathsRejectAlreadySeasonedFoodsAndSeasonedRiceAsMaterial() {
        SeasonFoodRecipe recipe = new SeasonFoodRecipe(); ItemStack apple = Seasoning.apply(new ItemStack(Items.APPLE), Arrays.asList(SpiceKind.SALT));
        assertFalse(recipe.matches(grid(apple, powder(SpiceKind.SUGAR)), null)); assertFalse(recipe.matches(grid(apple, rice()), null));
        ItemStack seasonedRice = Seasoning.apply(rice(), Arrays.asList(SpiceKind.SUGAR));
        assertFalse(recipe.matches(grid(new ItemStack(Items.APPLE), seasonedRice), null));
        assertFalse(new MixedSpiceRecipe().matches(grid(seasonedRice, powder(SpiceKind.SUGAR), powder(SpiceKind.SALT), new ItemStack(Items.NETHER_STAR)), null));
    }
    @Test public void plainRiceCanBeSeasonedButBowlIsReturnedOnlyForTheSeasoningMaterial() {
        SeasonFoodRecipe recipe = new SeasonFoodRecipe(); InventoryCrafting serving = grid(new ItemStack(Items.MUSHROOM_STEW), rice());
        assertEquals(Arrays.asList(SpiceKind.RICE), Seasoning.components(recipe.getCraftingResult(serving)));
        assertTrue(recipe.getRemainingItems(serving).get(0).isEmpty()); assertEquals(Items.BOWL, recipe.getRemainingItems(serving).get(1).getItem());
        InventoryCrafting riceFood = grid(rice(), powder(SpiceKind.SALT)); assertTrue(recipe.matches(riceFood, null));
        assertTrue(recipe.getRemainingItems(riceFood).get(0).isEmpty());
    }
    @Test public void blendsAreOrderIndependentReturnOneStarAndCannotBeMixedAgain() {
        MixedSpiceRecipe recipe = new MixedSpiceRecipe(); ItemStack star = new ItemStack(Items.NETHER_STAR, 8);
        InventoryCrafting first = grid(powder(SpiceKind.SUGAR), powder(SpiceKind.SALT), powder(SpiceKind.SUGAR), star);
        InventoryCrafting second = grid(star, powder(SpiceKind.SUGAR), powder(SpiceKind.SUGAR), powder(SpiceKind.SALT));
        ItemStack output = recipe.getCraftingResult(first); assertEquals(2, output.getCount());
        assertTrue(ItemStack.areItemStacksEqual(output, recipe.getCraftingResult(second)));
        assertEquals(1, recipe.getRemainingItems(first).get(3).getCount()); assertEquals(8, star.getCount());
        assertFalse(recipe.matches(grid(output, powder(SpiceKind.SALT), powder(SpiceKind.SUGAR), star), null));
        SpiceConfig.mixedCount = 5; assertEquals(5, recipe.getCraftingResult(first).getCount());
    }
    @Test public void mixedCatalystReplacementMatchesMetadataAndReturnsOneWithOriginalNbt() {
        SpiceConfig.mixedCatalystItem = "minecraft:dye"; SpiceConfig.mixedCatalystMetadata = 4;
        MixedSpiceRecipe recipe = new MixedSpiceRecipe();
        ItemStack catalyst = new ItemStack(Items.DYE, 12, 4); catalyst.setStackDisplayName("Keep this name");
        InventoryCrafting inventory = grid(powder(SpiceKind.SUGAR), catalyst, powder(SpiceKind.SALT), powder(SpiceKind.SUGAR));
        assertTrue(recipe.matches(inventory, null));
        ItemStack returned = recipe.getRemainingItems(inventory).get(1);
        assertEquals(1, returned.getCount()); assertEquals(catalyst.getTagCompound(), returned.getTagCompound());
        assertEquals(12, catalyst.getCount());
        assertFalse(recipe.matches(grid(powder(SpiceKind.SUGAR), powder(SpiceKind.SALT), powder(SpiceKind.SUGAR), new ItemStack(Items.NETHER_STAR)), null));
        assertFalse(recipe.matches(grid(powder(SpiceKind.SUGAR), powder(SpiceKind.SALT), powder(SpiceKind.SUGAR), new ItemStack(Items.DYE, 1, 3)), null));
        inventory.setInventorySlotContents(4, catalyst.copy()); assertFalse(recipe.matches(inventory, null));
    }
    @Test public void consumedMixedCatalystUsesNormalContainerRulesAndInvalidItemsDisableMixing() {
        SpiceConfig.consumeMixedCatalyst = true; SpiceConfig.mixedCatalystItem = "minecraft:diamond";
        MixedSpiceRecipe recipe = new MixedSpiceRecipe();
        InventoryCrafting inventory = grid(powder(SpiceKind.SUGAR), powder(SpiceKind.SALT), powder(SpiceKind.SUGAR), new ItemStack(Items.DIAMOND, 8));
        assertTrue(recipe.matches(inventory, null)); assertTrue(recipe.getRemainingItems(inventory).get(3).isEmpty());
        SpiceConfig.mixedCatalystItem = "minecraft:water_bucket";
        inventory.setInventorySlotContents(3, new ItemStack(Items.WATER_BUCKET));
        assertTrue(recipe.matches(inventory, null)); assertEquals(Items.BUCKET, recipe.getRemainingItems(inventory).get(3).getItem());
        SpiceConfig.mixedCatalystItem = "missing:nonexistent";
        assertTrue(SpiceConfig.mixedCatalyst().isEmpty()); assertFalse(recipe.matches(inventory, null));
        assertTrue(recipe.getCraftingResult(inventory).isEmpty());
    }
    @Test public void mixedCatalystSettingsAreIncludedInServerSnapshot() {
        NBTTagCompound previous = SpiceConfig.snapshot();
        SpiceConfig.mixedCatalystItem = "minecraft:dye"; SpiceConfig.mixedCatalystMetadata = 4;
        SpiceConfig.consumeMixedCatalyst = true;
        NBTTagCompound server = SpiceConfig.snapshot();
        SpiceConfig.readSnapshot(previous); SpiceConfig.readSnapshot(server);
        assertEquals("minecraft:dye", SpiceConfig.mixedCatalystItem);
        assertEquals(4, SpiceConfig.mixedCatalystMetadata); assertTrue(SpiceConfig.consumeMixedCatalyst);
        SpiceConfig.readSnapshot(previous);
    }
    @Test public void waterRecipesRequireExactWaterAndReturnContainersAndOtherLiquidsNeverMatch() {
        BuiltinSpiceRecipes.reload();
        SpiceRecipe bottle = SpiceRecipes.find(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER));
        assertNotNull(bottle); assertEquals(1, bottle.inputCount); assertEquals(1, bottle.result().getCount());
        assertEquals(Items.GLASS_BOTTLE, bottle.returnedContainers().getItem());
        SpiceRecipe bucket = SpiceRecipes.find(new ItemStack(Items.WATER_BUCKET)); assertEquals(3, bucket.result().getCount());
        assertEquals(Items.BUCKET, bucket.returnedContainers().getItem());
        assertNull(SpiceRecipes.find(new ItemStack(Items.LAVA_BUCKET)));
        assertNull(SpiceRecipes.find(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.HEALING)));
        int count = SpiceRecipes.all().size(); BuiltinSpiceRecipes.reload(); assertEquals(count, SpiceRecipes.all().size());
    }
    @Test public void grinderDropsContainerAtBatchCompletionAndKeepsSpiceInOutput() throws Exception {
        BuiltinSpiceRecipes.reload();
        java.lang.reflect.Field field = net.minecraftforge.items.CapabilityItemHandler.class.getDeclaredField("ITEM_HANDLER_CAPABILITY");
        Object previous = field.get(null);
        net.minecraftforge.common.capabilities.Capability cap = mock(net.minecraftforge.common.capabilities.Capability.class);
        when(cap.cast(any())).thenAnswer(call -> call.getArgument(0)); field.set(null, cap);
        try {
            List<ItemStack> drops = new ArrayList<>();
            TileEntitySpiceGrinder mill = new TileEntitySpiceGrinder() { @Override protected void drop(ItemStack stack) { drops.add(stack.copy()); } };
            net.minecraft.world.World world = mock(net.minecraft.world.World.class);
            when(world.getBlockState(any())).thenReturn(Blocks.AIR.getDefaultState()); mill.setWorld(world); mill.setPos(new net.minecraft.util.math.BlockPos(0, 64, 0));
            net.minecraftforge.items.IItemHandler input = (net.minecraftforge.items.IItemHandler) mill.getCapability(cap, net.minecraft.util.EnumFacing.UP);
            input.insertItem(0, new ItemStack(Items.WATER_BUCKET), false);
            for (int i = 0; i < 59; i++) mill.update(); assertTrue(drops.isEmpty()); mill.update();
            assertEquals(1, drops.size()); assertEquals(Items.BUCKET, drops.get(0).getItem());
            assertEquals(3, mill.getOutput().getCount()); assertEquals(Arrays.asList(SpiceKind.SALT), Seasoning.components(mill.getOutput()));
        } finally { field.set(null, previous); }
    }
    @Test public void riceAddsActualSaturationPointsWithoutChangingPlainFood() {
        FoodValues base = new FoodValues(4, .3F); FoodValues rice = SpiceEffects.riceValues(base);
        assertEquals(11, rice.hunger); assertEquals(9.4, rice.getUnboundedSaturationIncrement(), .00001);
        assertEquals(4, base.hunger); assertEquals(2.4, base.getUnboundedSaturationIncrement(), .00001);
    }
    @Test public void blendFlavorCountsOnceAndUsesOverallMultiplierWithoutMultiplyingIngredients() {
        ItemStack food = Seasoning.apply(new ItemStack(Items.APPLE), Arrays.asList(SpiceKind.SUGAR, SpiceKind.SALT, SpiceKind.SUGAR));
        com.xy177.solwarly.api.FlavorAPI.Flavor flavor = Seasoning.flavor(food); assertEquals(1.2, flavor.diversityMultiplier, 0);
        DietData diet = new DietData(); assertEquals(4, diet.eat("apple", flavor.id, 4, 4.8, 32), 0);
        ItemStack different = Seasoning.apply(new ItemStack(Items.APPLE), Arrays.asList(SpiceKind.SALT, SpiceKind.SALT, SpiceKind.SALT));
        assertEquals(flavor.id, Seasoning.flavor(different).id);
        assertNotEquals(Seasoning.components(food), Seasoning.components(different));
        assertEquals(0, diet.eat("apple", Seasoning.flavor(different).id, 4, 7, 32), 0);
        assertTrue(diet.hasTried("apple", Seasoning.flavor(different).id));
        assertEquals(1, diet.variety(.25), 0);
        assertEquals(0, diet.eat("apple", flavor.id, 4, 4.8, 32), 0); assertEquals(2, diet.eat("apple", "plain", 4, 4, 32), 0);
        assertEquals(1.25, diet.variety(.25), 0);
    }
    @Test public void repeatedScalableEffectsGainLevelsWhileNonScalingEffectsGainDuration() {
        EntityPlayer player = mock(EntityPlayer.class);
        SpiceEffects.apply(player, Arrays.asList(SpiceKind.SUGAR, SpiceKind.SUGAR, SpiceKind.SALT), 0, 0);
        verify(player).addPotionEffect(argThat(effect -> effect.getPotion() == SpiceRegistry.sweet && effect.getAmplifier() == 1 && effect.getDuration() == 2400));
        SpiceEffects.apply(player, Arrays.asList(SpiceKind.TOMATO, SpiceKind.TOMATO, SpiceKind.SALT), 0, 0);
        verify(player).addPotionEffect(argThat(effect -> effect.getPotion() == SpiceKind.TOMATO.potion() && effect.getAmplifier() == 0 && effect.getDuration() == 3600));
    }
    @Test public void hamExtendsOriginalFoodDurationRatherThanAccumulatingPlayersExistingDuration() {
        EntityPlayer player = mock(EntityPlayer.class);
        SpiceEffects.apply(player, Arrays.asList(SpiceKind.HAM), 1200, 0);
        verify(player).addPotionEffect(argThat(effect -> effect.getPotion() == SpiceKind.HAM.potion() && effect.getDuration() == 3600));
        SpiceEffects.apply(player, Arrays.asList(SpiceKind.HAM, SpiceKind.HAM, SpiceKind.SALT), 0, 0);
        verify(player).addPotionEffect(argThat(effect -> effect.getPotion() == SpiceKind.HAM.potion() && effect.getDuration() == 2700));
    }
    @Test public void healingDamageAndMiningUseOneBasedPotionLevels() {
        EntityPlayer player = mock(EntityPlayer.class);
        when(player.getActivePotionEffect(SpiceRegistry.tasty)).thenReturn(new PotionEffect(SpiceRegistry.tasty, 2400, 1));
        LivingHealEvent heal = new LivingHealEvent(player, 5); SpiceEffects.healing(heal); assertEquals(6, heal.getAmount(), .00001);
        when(player.getActivePotionEffect(SpiceRegistry.aromatic)).thenReturn(new PotionEffect(SpiceRegistry.aromatic, 2400, 0));
        LivingDamageEvent damage = new LivingDamageEvent(player, net.minecraft.util.DamageSource.GENERIC, 10); SpiceEffects.damage(damage); assertEquals(8.5, damage.getAmount(), .00001);
        when(player.getActivePotionEffect(SpiceRegistry.sweet)).thenReturn(new PotionEffect(SpiceRegistry.sweet, 2400, 2));
        PlayerEvent.BreakSpeed speed = new PlayerEvent.BreakSpeed(player, Blocks.STONE.getDefaultState(), 10, net.minecraft.util.math.BlockPos.ORIGIN);
        SpiceEffects.mining(speed); assertEquals(16, speed.getNewSpeed(), .00001);
    }
    private static final class TestPotion extends Potion { TestPotion(String id) { super(false, 0xFFFFFF); setRegistryName(id); } }
    @Test public void ordinaryFoodAcceptsTwoRiceServingsInAnySlotsAndNeverAThird() {
        SeasonFoodRecipe recipe=new SeasonFoodRecipe();
        for(int a=0;a<9;a++)for(int b=0;b<9;b++)if(a!=b) {
            ItemStack[] stacks=new ItemStack[9]; Arrays.fill(stacks,ItemStack.EMPTY); stacks[a]=new ItemStack(Items.APPLE); stacks[b]=rice();
            InventoryCrafting input=grid(stacks); ItemStack first=recipe.getCraftingResult(input);
            assertEquals(Collections.singletonList(SpiceKind.RICE),Seasoning.components(first));
            stacks[a]=first; input=grid(stacks); ItemStack second=recipe.getCraftingResult(input);
            assertEquals(Arrays.asList(SpiceKind.RICE,SpiceKind.RICE),Seasoning.components(second));
            assertEquals(Items.BOWL,recipe.getRemainingItems(input).get(b).getItem()); assertTrue(recipe.getRemainingItems(input).get(a).isEmpty());
            stacks[a]=second; assertFalse(recipe.matches(grid(stacks),null));
        }
        ItemStack second=recipe.getCraftingResult(grid(new ItemStack(Items.APPLE),rice(),rice()));
        assertEquals("solwarly:meal:rice+rice",Seasoning.flavor(second).id); assertEquals(1.2,Seasoning.flavor(second).diversityMultiplier,0);
        FoodEvent.GetFoodValues values=new FoodEvent.GetFoodValues(second,new FoodValues(4,.3F)); SpiceEffects.foodValues(values);
        assertEquals(18,values.foodValues.hunger); assertEquals(16.4,values.foodValues.getUnboundedSaturationIncrement(),.001);
    }
    @Test public void creativeSeasoningCombinesAvailableEffectsButIsNotMixedOrRequired() {
        ItemStack creative=powder(SpiceKind.CREATIVE); assertTrue(SpiceRegistry.spice.hasEffect(creative));
        ItemStack food=new SeasonFoodRecipe().getCraftingResult(grid(creative,new ItemStack(Items.APPLE)));
        assertTrue(Seasoning.creative(food));
        List<SpiceKind> all=Seasoning.effectKinds(Seasoning.components(food));
        assertFalse(all.contains(SpiceKind.CREATIVE)); assertTrue(all.contains(SpiceKind.RICE));
        assertFalse(Seasoning.variants(new ItemStack(Items.APPLE)).stream().anyMatch(Seasoning::creative));
        assertFalse(new MixedSpiceRecipe().matches(grid(creative,powder(SpiceKind.SALT),powder(SpiceKind.SUGAR),new ItemStack(Items.NETHER_STAR)),null));
        List<SeasoningPotions.Entry> actual=SeasoningPotions.additions(Seasoning.components(food),Collections.emptyMap());
        List<SeasoningPotions.Entry> expected=new ArrayList<>();
        for(SpiceKind kind:all) expected.addAll(SeasoningPotions.additions(Collections.singletonList(kind),Collections.emptyMap()));
        assertEquals(expected.size(),actual.size());
        for(int i=0;i<expected.size();i++) { assertEquals(expected.get(i).effect.getPotion(),actual.get(i).effect.getPotion()); assertEquals(expected.get(i).effect.getDuration(),actual.get(i).effect.getDuration()); }
        EntityPlayer player=mock(EntityPlayer.class); player.world=mock(net.minecraft.world.World.class);
        String[] oldA=com.xy177.solwarly.diet.DietConfig.attributes,oldE=com.xy177.solwarly.diet.DietConfig.effects;
        try {
            com.xy177.solwarly.diet.DietConfig.attributes=new String[]{"42|generic.maxHealth|8|0"}; com.xy177.solwarly.diet.DietConfig.effects=new String[]{"87.5|minecraft:speed|0"};
            assertEquals(87.5,com.xy177.solwarly.diet.DietRuntime.contribution(player,food,.03),0);
            assertEquals(.03,com.xy177.solwarly.diet.DietRuntime.contribution(player,new ItemStack(Items.APPLE),.03),0);
        } finally { com.xy177.solwarly.diet.DietConfig.attributes=oldA; com.xy177.solwarly.diet.DietConfig.effects=oldE; }
    }
}
