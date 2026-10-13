package com.xy177.solwarly.spice;

import com.google.gson.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.effect.*;
import net.minecraft.resources.ResourceLocation;
import com.xy177.solwarly.registry.RegistryLookup;
import org.junit.*;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SeasoningLimitsTest {
    private ItemSpice oldItem;
    private static ItemSpice testItem;
    @BeforeClass public static void registerTestItem() throws Exception {
        net.minecraft.SharedConstants.tryDetectVersion();
        try(var loading=org.mockito.Mockito.mockStatic(net.neoforged.fml.loading.LoadingModList.class)){var mods=mock(net.neoforged.fml.loading.LoadingModList.class);when(mods.getModFiles()).thenReturn(Collections.emptyList());loading.when(net.neoforged.fml.loading.LoadingModList::get).thenReturn(mods);net.minecraft.server.Bootstrap.bootStrap();}
        var registry=net.minecraft.core.registries.BuiltInRegistries.ITEM;
        var frozen=net.minecraft.core.MappedRegistry.class.getDeclaredField("frozen");frozen.setAccessible(true);frozen.setBoolean(registry,false);
        var holders=net.minecraft.core.MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");holders.setAccessible(true);holders.set(registry,new java.util.IdentityHashMap<>());
        testItem=new ItemSpice();net.minecraft.core.Registry.register(registry, net.minecraft.resources.ResourceLocation.parse("test:limits_powder"),testItem);registry.freeze();
    }
    @BeforeClass public static void bootstrap() { net.minecraft.SharedConstants.tryDetectVersion();try(var loading=org.mockito.Mockito.mockStatic(net.neoforged.fml.loading.LoadingModList.class)){var mods=mock(net.neoforged.fml.loading.LoadingModList.class);when(mods.getModFiles()).thenReturn(Collections.emptyList());loading.when(net.neoforged.fml.loading.LoadingModList::get).thenReturn(mods);net.minecraft.server.Bootstrap.bootStrap();} }
    @Before public void setup() {
        oldItem=SpiceRegistry.spice; if(oldItem==null)SpiceRegistry.spice=testItem;
        SpiceConfig.mixedIngredients=3;SpiceConfig.maxSeasonings=1;SpiceConfig.maxAccompaniments=2;SpiceConfig.allowSeasoningWithAccompaniments=false;
        SpiceConfig.doubleDuration=1.5;SpiceConfig.tripleDuration=2;
        
    }
    @After public void restore() {
        SpiceRegistry.spice=oldItem;SpiceConfig.mixedIngredients=3;SpiceConfig.maxSeasonings=1;SpiceConfig.maxAccompaniments=2;SpiceConfig.allowSeasoningWithAccompaniments=false;CustomSpices.clearServerDefinitions();try {java.lang.reflect.Field f=CustomSpices.class.getDeclaredField("local");f.setAccessible(true);((Map<?,?>)f.get(null)).remove("test:side");}catch(Exception e){throw new AssertionError(e);}
    }
    
    private net.minecraft.world.item.crafting.CraftingInput grid(ItemStack... stacks) { List<ItemStack> slots=new ArrayList<>(Collections.nCopies(9,ItemStack.EMPTY));for(int i=0;i<stacks.length;i++)slots.set(i,stacks[i]);return net.minecraft.world.item.crafting.CraftingInput.of(3,3,slots); }
    private SeasonFoodRecipe foodRecipe() { return new SeasonFoodRecipe(CraftingBookCategory.MISC); }
    private MixedSpiceRecipe mixedRecipe() { return new MixedSpiceRecipe(CraftingBookCategory.MISC); }
    private ItemStack craft(ItemStack... stacks) { return foodRecipe().assemble(grid(stacks),null); }
    private ItemStack powder(SpiceKind kind) { return Seasoning.powder(Collections.singletonList(kind),1); }
    @SuppressWarnings("unchecked") private void registerSide(CustomSpice side) {
        try { java.lang.reflect.Field field=CustomSpices.class.getDeclaredField("local");field.setAccessible(true);((Map<String,CustomSpice>)field.get(null)).put(side.id(),side); }
        catch(ReflectiveOperationException e){throw new AssertionError(e);}
    }
    private CustomSpice side() {
        return new CustomSpice(new JsonParser().parse("{\"id\":\"test:side\",\"material\":{\"item\":\"minecraft:carrot\"},\"return\":null,\"color\":\"#123456\",\"hunger\":2,\"saturation\":1.2,\"translations\":{\"en_us\":{\"name\":\"Side\"}},\"effects\":[{\"potion\":\"minecraft:night_vision\",\"level\":1,\"duration_seconds\":30}]}").getAsJsonObject(),true);
    }
    @Test public void everyMixedSizeCraftsAndOneCopyStaysMixed() {
        for(int n=1;n<=8;n++) {
            SpiceConfig.mixedIngredients=n;
            ItemStack[] stacks=new ItemStack[n+1];Arrays.fill(stacks,powder(SpiceKind.BEEF));stacks[n]=SpiceConfig.mixedCatalyst();
            MixedSpiceRecipe recipe=mixedRecipe();assertTrue(recipe.matches(grid(stacks),null));
            ItemStack mixed=recipe.assemble(grid(stacks),null);
            assertTrue(Seasoning.mixed(mixed));assertEquals(n,Seasoning.amount(Seasoning.components(mixed),SpiceKind.BEEF));assertEquals(1,Seasoning.powderUses(Seasoning.components(mixed)));
            stacks[0]=mixed;assertFalse(recipe.matches(grid(stacks),null));
            assertFalse(recipe.canCraftInDimensions(n,1));
        }
    }
    @Test public void sequentialAndSimultaneousPowdersCountApplicationsSeparately() {
        ItemStack apple=new ItemStack(Items.APPLE),beef=powder(SpiceKind.BEEF);
        ItemStack one=craft(beef,apple);assertFalse(one.isEmpty());assertTrue(craft(one,beef).isEmpty());
        SpiceConfig.maxSeasonings=5;
        ItemStack two=craft(beef,one),five=craft(two,beef,beef,beef);
        assertEquals(5,Seasoning.powderUses(Seasoning.components(five)));assertEquals(5,Seasoning.amount(Seasoning.components(five),SpiceKind.BEEF));assertFalse(Seasoning.mixed(Seasoning.components(five)));
        assertEquals(Seasoning.flavor(one).id,Seasoning.flavor(five).id);assertTrue(craft(five,beef).isEmpty());
        ItemStack mixed=Seasoning.mixedPowder(Collections.nCopies(8,SpiceKind.BEEF),1);
        ItemStack combined=craft(two,mixed);assertEquals(3,Seasoning.powderUses(Seasoning.components(combined)));assertEquals(10,Seasoning.amount(Seasoning.components(combined),SpiceKind.BEEF));assertEquals(Seasoning.MIXED_FLAVOR,Seasoning.flavor(combined).id);
        assertEquals(Seasoning.MIXED_FLAVOR,Seasoning.flavor(craft(one,powder(SpiceKind.SALT))).id);
    }
    @Test public void coexistingCountsAreIndependentAndOrderDoesNotChangeFlavor() {
        SpiceConfig.maxSeasonings=5;SpiceConfig.maxAccompaniments=5;
        CustomSpice side=side();registerSide(side);
        SpiceKind registered=SpiceKind.from("test:side");assertNotNull(registered);
        ItemStack ingredient=Seasoning.material(registered),apple=new ItemStack(Items.APPLE),beef=powder(SpiceKind.BEEF);
        assertTrue(craft(apple,beef,ingredient).isEmpty());
        SpiceConfig.allowSeasoningWithAccompaniments=true;
        com.xy177.solwarly.item.StackData.update(beef,tag->tag.putInt(ItemSpice.COLOR_KEY,0xabcdef));
        ItemStack simultaneous=craft(apple,ingredient,beef),firstPowder=craft(craft(apple,beef),ingredient),firstSide=craft(craft(apple,ingredient),beef);
        assertEquals(0xabcdef,Seasoning.color(simultaneous));
        assertFalse(simultaneous.isEmpty());assertEquals(Seasoning.flavor(simultaneous).id,Seasoning.flavor(firstPowder).id);assertEquals(Seasoning.flavor(simultaneous).id,Seasoning.flavor(firstSide).id);
        assertEquals(1,Seasoning.powderUses(Seasoning.components(simultaneous)));assertEquals(1,Seasoning.sideUses(Seasoning.components(simultaneous)));
        ItemStack fiveSides=craft(simultaneous,ingredient,ingredient,ingredient,ingredient);assertEquals(5,Seasoning.sideUses(Seasoning.components(fiveSides)));assertEquals(3,Seasoning.badges(fiveSides).size());assertTrue(craft(fiveSides,ingredient).isEmpty());
        SpiceConfig.maxSeasonings=1;SpiceConfig.maxAccompaniments=2;SpiceConfig.allowSeasoningWithAccompaniments=false;
        assertEquals(5,Seasoning.sideUses(Seasoning.components(fiveSides)));assertNotNull(Seasoning.flavor(fiveSides));
        CustomSpices.clearServerDefinitions();try {java.lang.reflect.Field f=CustomSpices.class.getDeclaredField("local");f.setAccessible(true);((Map<?,?>)f.get(null)).remove("test:side");}catch(Exception e){throw new AssertionError(e);}
    }
    @Test public void netherSpicePrefersOriginalAndOnlyFallsBackWhenAvailable() {
        try (var lookup = mockStatic(com.xy177.solwarly.registry.RegistryLookup.class, CALLS_REAL_METHODS)) {
            lookup.when(() -> com.xy177.solwarly.registry.RegistryLookup.item("nethersdelight:propelpearl")).thenReturn(null);
            lookup.when(() -> com.xy177.solwarly.registry.RegistryLookup.item("mynethersdelight:bullet_pepper")).thenReturn(null);
            assertFalse(SpiceKind.usesMyNethersDelight());
            assertEquals("nethersdelight:propelpearl", SpiceKind.spicyIngredientId());
            lookup.when(() -> com.xy177.solwarly.registry.RegistryLookup.item("mynethersdelight:bullet_pepper")).thenReturn(Items.FIRE_CHARGE);
            assertTrue(SpiceKind.usesMyNethersDelight());
            assertEquals("mynethersdelight:bullet_pepper", SpiceKind.spicyIngredientId());
            lookup.when(() -> com.xy177.solwarly.registry.RegistryLookup.item("nethersdelight:propelpearl")).thenReturn(Items.GUNPOWDER);
            assertFalse(SpiceKind.usesMyNethersDelight());
            assertEquals("nethersdelight:propelpearl", SpiceKind.spicyIngredientId());
        }
    }
    @Test public void myNetherSpicyAddsPositivePungentWithDurationOnlyScaling() {
        int oldDuration = SpiceConfig.spicyPungentDuration;
        try (var lookup = mockStatic(RegistryLookup.class, CALLS_REAL_METHODS)) {
            lookup.when(() -> RegistryLookup.item("nethersdelight:propelpearl")).thenReturn(null);
            lookup.when(() -> RegistryLookup.item("mynethersdelight:bullet_pepper")).thenReturn(Items.FIRE_CHARGE);
            lookup.when(() -> RegistryLookup.effect(ResourceLocation.tryParse("solwarly:hot"))).thenReturn(MobEffects.DAMAGE_BOOST);
            lookup.when(() -> RegistryLookup.effect(ResourceLocation.tryParse("mynethersdelight:g_pungent"))).thenReturn(MobEffects.FIRE_RESISTANCE);
            SpiceConfig.spicyPungentDuration = 2400;
            for (int copies : new int[] { 1, 2, 3, 4, 5 }) {
                var kinds = Seasoning.components(Seasoning.mixedPowder(Collections.nCopies(copies, SpiceKind.SPICY), 1));
                var effects = SeasoningPotions.additions(kinds, Collections.emptyMap(), false);
                assertEquals(2, effects.size());
                assertEquals(copies - 1, effects.get(0).effect.getAmplifier());
                var pungent = effects.get(1);
                assertEquals(MobEffects.FIRE_RESISTANCE, pungent.effect.getEffect());
                assertEquals(2400 + (copies - 1) * 1200, pungent.effect.getDuration());
                assertEquals(0, pungent.effect.getAmplifier());
                assertEquals(1F, pungent.chance, 0);
            }
            var maximum = new SeasoningAmounts(Map.of(SpiceKind.SPICY, (long) Integer.MAX_VALUE * 8), Integer.MAX_VALUE, true, List.of());
            assertEquals(Integer.MAX_VALUE, SeasoningPotions.additions(maximum, Collections.emptyMap(), false).get(1).effect.getDuration());
            SpiceConfig.spicyPungentDuration = 0;
            assertEquals(1, SeasoningPotions.additions(List.of(SpiceKind.SPICY), Collections.emptyMap(), false).size());
            SpiceConfig.spicyPungentDuration = 2400;
            lookup.when(() -> RegistryLookup.item("nethersdelight:propelpearl")).thenReturn(Items.GUNPOWDER);
            assertEquals(1, SeasoningPotions.additions(List.of(SpiceKind.SPICY), Collections.emptyMap(), false).size());
            lookup.when(() -> RegistryLookup.item("nethersdelight:propelpearl")).thenReturn(null);
            lookup.when(() -> RegistryLookup.effect(ResourceLocation.tryParse("mynethersdelight:g_pungent"))).thenReturn(null);
            assertEquals(1, SeasoningPotions.additions(List.of(SpiceKind.SPICY), Collections.emptyMap(), false).size());
        } finally { SpiceConfig.spicyPungentDuration = oldDuration; }
    }
    @Test public void myNetherPungentMergesFoodEffectAndRefreshesPlayerEffect() {
        try (var lookup = mockStatic(RegistryLookup.class, CALLS_REAL_METHODS)) {
            lookup.when(() -> RegistryLookup.item("nethersdelight:propelpearl")).thenReturn(null);
            lookup.when(() -> RegistryLookup.item("mynethersdelight:bullet_pepper")).thenReturn(Items.FIRE_CHARGE);
            lookup.when(() -> RegistryLookup.effect(ResourceLocation.tryParse("solwarly:hot"))).thenReturn(MobEffects.DAMAGE_BOOST);
            lookup.when(() -> RegistryLookup.effect(ResourceLocation.tryParse("mynethersdelight:g_pungent"))).thenReturn(MobEffects.FIRE_RESISTANCE);
            var original = Map.of(MobEffects.FIRE_RESISTANCE, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 1));
            var merged = SeasoningPotions.merge(original, SeasoningPotions.additions(List.of(SpiceKind.SPICY), original, false));
            assertEquals(2, merged.size());
            assertEquals(3000, merged.get(MobEffects.FIRE_RESISTANCE).getDuration());
            assertEquals(1, merged.get(MobEffects.FIRE_RESISTANCE).getAmplifier());
            var player = mock(net.minecraft.world.entity.player.Player.class);
            when(player.getEffect(MobEffects.FIRE_RESISTANCE)).thenReturn(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0));
            var applied = SpiceEffects.apply(player, List.of(SpiceKind.SPICY), original);
            assertEquals(3000, applied.get(MobEffects.FIRE_RESISTANCE).getDuration());
            var effects = org.mockito.ArgumentCaptor.forClass(MobEffectInstance.class);
            verify(player, times(2)).addEffect(effects.capture());
            var pungent = effects.getAllValues().stream().filter(e -> e.getEffect().equals(MobEffects.FIRE_RESISTANCE)).findFirst().orElseThrow();
            assertEquals(6000, pungent.getDuration());
            assertEquals(1, pungent.getAmplifier());
        }
    }
    @Test public void pungentDurationSynchronizesToClient() {
        int oldDuration = SpiceConfig.spicyPungentDuration;
        try {
            SpiceConfig.spicyPungentDuration = 3600;
            var snapshot = SpiceConfig.snapshot();
            SpiceConfig.spicyPungentDuration = 1;
            SpiceConfig.readSnapshot(snapshot);
            assertEquals(3600, SpiceConfig.spicyPungentDuration);
        } finally { SpiceConfig.spicyPungentDuration = oldDuration; }
    }
    @Test public void highTierEffectsAndImmediateFunctionsContinueBeyondThree() {
        List<SpiceKind> kinds=Seasoning.components(Seasoning.mixedPowder(Collections.nCopies(5,SpiceKind.BEEF),1));
        assertEquals(4,SeasoningPotions.additions(kinds,Collections.emptyMap(),false).get(0).effect.getAmplifier());
        assertEquals(6,SpiceEffects.mysteryCureCount(4));assertEquals(8,SpiceEffects.mysteryCureCount(5));assertEquals(Integer.MAX_VALUE,SpiceEffects.mysteryCureCount(Integer.MAX_VALUE));
        assertEquals(2.5,Seasoning.durationScale(4),0);assertEquals(3,Seasoning.durationScale(5),0);
        assertEquals(3000,side().effects(5).get(0).effect.getDuration());assertEquals(0,side().effects(5).get(0).effect.getAmplifier());
    }
    @Test public void optionalCustomTiersOverrideFallbackAndMissingTiersExtrapolate() {
        JsonObject json=new JsonParser().parse("{\"id\":\"test:powder\",\"color\":\"#123456\",\"translations\":{\"en_us\":{\"name\":\"Test\"}},\"effects\":{\"1\":[{\"potion\":\"minecraft:speed\",\"level\":1,\"duration_seconds\":20}],\"2\":[{\"potion\":\"minecraft:speed\",\"level\":2,\"duration_seconds\":20}],\"3\":[{\"potion\":\"minecraft:speed\",\"level\":3,\"duration_seconds\":20},{\"potion\":\"minecraft:night_vision\",\"level\":1,\"duration_seconds\":40}],\"4\":[{\"potion\":\"minecraft:speed\",\"level\":8,\"duration_seconds\":10}]}}").getAsJsonObject();
        CustomSpice powder=new CustomSpice(json);assertEquals(7,powder.effects(4).get(0).effect.getAmplifier());assertEquals(200,powder.effects(4).get(0).effect.getDuration());
        assertEquals(4,powder.effects(5).get(0).effect.getAmplifier());assertEquals(400,powder.effects(5).get(0).effect.getDuration());assertEquals(1200,powder.effects(5).get(1).effect.getDuration());
        assertEquals(255,powder.effects(Integer.MAX_VALUE).get(0).effect.getAmplifier());assertEquals(Integer.MAX_VALUE,powder.effects(Integer.MAX_VALUE).get(1).effect.getDuration());
    }
    @Test public void maximumCountsHaveBoundedDataAndCannotOverflowApplications() {
        SpiceConfig.maxSeasonings=Integer.MAX_VALUE;SpiceConfig.maxAccompaniments=Integer.MAX_VALUE;
        Map<SpiceKind,Long> counts=new TreeMap<>();counts.put(SpiceKind.BEEF,8L*Integer.MAX_VALUE);
        SeasoningAmounts max=new SeasoningAmounts(counts,Integer.MAX_VALUE,true,Collections.emptyList());
        ItemStack food=Seasoning.apply(new ItemStack(Items.APPLE),max);assertEquals(3,Seasoning.components(food).size());assertEquals(8L*Integer.MAX_VALUE,Seasoning.amount(Seasoning.components(food),SpiceKind.BEEF));assertEquals(Integer.MAX_VALUE,Seasoning.powderUses(Seasoning.components(food)));assertFalse(Seasoning.canAdd(Seasoning.components(food),Collections.singletonList(SpiceKind.BEEF)));
        assertTrue(Seasoning.identity(Seasoning.components(food)).length()<80);assertEquals(Seasoning.MIXED_FLAVOR,Seasoning.flavor(food).id);assertEquals(Integer.MAX_VALUE,Seasoning.ticks(1e25));
    }
    @Test public void effectScalingUsesAmountsAboveTheIntegerRange() {
        long amount=(long)Integer.MAX_VALUE+100;
        assertEquals((amount+1)/2.0,Seasoning.durationScale(amount),0);
        String tier="[{\"potion\":\"minecraft:night_vision\",\"level\":1,\"duration_seconds\":0.05}]";
        JsonObject definition=JsonParser.parseString("{\"id\":\"test:tiny\",\"color\":\"#123456\",\"translations\":{\"en_us\":{\"name\":\"Test\"}},\"effects\":{\"1\":"+tier+",\"2\":"+tier+",\"3\":"+tier+"}}").getAsJsonObject();
        assertEquals(Seasoning.ticks((amount+1)/4.0),new CustomSpice(definition).effects(amount).get(0).effect.getDuration());
    }
    @Test public void extremeEffectsKeepTheirLevelAndDurationAfterNativeNbtReload() {
        long count = 8L * Integer.MAX_VALUE;
        int amplifier = Seasoning.amplifier(count - 1), duration = Seasoning.ticks(1e30);
        assertEquals(255, amplifier); assertEquals(Integer.MAX_VALUE, duration);
        net.minecraft.world.effect.MobEffectInstance effect = new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, duration, amplifier);
        net.minecraft.nbt.CompoundTag saved = (net.minecraft.nbt.CompoundTag) effect.save();
        net.minecraft.world.effect.MobEffectInstance loaded = net.minecraft.world.effect.MobEffectInstance.load(saved);
        assertNotNull(loaded);
        assertEquals(amplifier, loaded.getAmplifier()); assertEquals(duration, loaded.getDuration());
        assertEquals(0, Seasoning.amplifier(-1)); assertEquals(0, Seasoning.ticks(-1));
    }
    @Test public void customExplicitLevelsUseTheSameSafeCap() {
        JsonObject definition = new JsonParser().parse("{\"id\":\"test:cap\",\"color\":\"#123456\",\"translations\":{\"en_us\":{\"name\":\"Test\"}},\"effects\":{\"1\":[{\"potion\":\"minecraft:strength\",\"level\":256,\"duration_seconds\":120}],\"2\":[],\"3\":[]}}").getAsJsonObject();
        net.minecraft.world.effect.MobEffectInstance effect = new CustomSpice(definition).effects(1).get(0).effect;
        assertEquals(255, effect.getAmplifier());
        net.minecraft.nbt.CompoundTag saved = (net.minecraft.nbt.CompoundTag) effect.save();
        net.minecraft.world.effect.MobEffectInstance loaded = net.minecraft.world.effect.MobEffectInstance.load(saved);
        assertNotNull(loaded); assertEquals(255, loaded.getAmplifier());
        assertEquals(2400, loaded.getDuration());
    }
    @Test public void serverSnapshotIncludesAllFourSettings() {
        SpiceConfig.mixedIngredients=8;SpiceConfig.maxSeasonings=123;SpiceConfig.maxAccompaniments=456;SpiceConfig.allowSeasoningWithAccompaniments=true;
        net.minecraft.nbt.CompoundTag saved=SpiceConfig.snapshot();SpiceConfig.mixedIngredients=1;SpiceConfig.readSnapshot(saved);
        assertEquals(8,SpiceConfig.mixedIngredients);assertEquals(123,SpiceConfig.maxSeasonings);assertEquals(456,SpiceConfig.maxAccompaniments);assertTrue(SpiceConfig.allowSeasoningWithAccompaniments);
    }
    @Test public void previewIsBoundedAndEveryGridFitsNineSlots() {
        for(int n=1;n<=8;n++) {
            SpiceConfig.mixedIngredients=n;List<List<SpiceKind>> rows=MixingPreview.combinations(Arrays.asList(SpiceKind.values()));assertTrue(rows.size()<=MixingPreview.LIMIT+SpiceKind.values().length);
            for(List<SpiceKind> row:rows)assertEquals(n,row.size());Set<Integer> slots=new HashSet<>();for(int j=0;j<n;j++)assertTrue(slots.add(MixingPreview.gridSlot(j)));assertTrue(slots.add(MixingPreview.catalystSlot()));assertEquals(n+1,slots.size());assertTrue(Collections.max(slots)<=8);
        }
    }
}
