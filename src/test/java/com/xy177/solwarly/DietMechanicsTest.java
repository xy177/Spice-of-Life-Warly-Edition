package com.xy177.solwarly;

import com.xy177.solwarly.diet.*;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Before;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class DietMechanicsTest {
    @Before public void defaults() {
        DietConfig.cumulativeFlavorFraction = 0.5;
        DietConfig.cravingCount = 5; DietConfig.boringCount = 5;
        DietConfig.frequencyPerPlayer = 20; DietConfig.frequencyMaximum = 4096;
        DietConfig.uniformChance = 0.2;
        DietConfig.frequencyWeight = -1;
        DietConfig.warlyMode = false; DietConfig.warlyDurationDays = 2;
        DietConfig.warlyRepeatMultipliers = new double[] {1.0, .9, .8, .65, .5, .3};
        DietConfig.alwaysCraved = new String[0]; DietConfig.alwaysBoring = new String[0];
    }
    @Test public void seasonedFirstGetsFullValueAndPlainLaterGetsHalf() {
        DietData data = new DietData();
        assertEquals(4, data.eat("stew", "spicy", 4, 6, 32), 0);
        assertEquals(2, data.eat("stew", "plain", 100, 3, 32), 0);
        assertEquals(0, data.eat("stew", "spicy", 100, 5, 32), 0);
        assertEquals(2, data.eat("stew", "salt", 100, 4, 32), 0);
        assertEquals(8, data.cumulative(), 0);
    }
    @Test public void repeatedMealsAffectAverageButNotUniqueFlavorCount() {
        DietData data = new DietData();
        data.eat("stew", "plain", 4, 4, 32);
        data.eat("stew", "plain", 4, 8, 32);
        data.eat("stew", "salt", 4, 6, 32);
        data.eat("bread", "plain", 2, 2, 32);
        assertEquals(5, data.average(), 0);
        assertEquals(2.25, data.variety(0.25), 0);
        assertEquals(11.25, data.diversity(0.25, 1), 0);
    }
    @Test public void oldestMealLeavesAndSavedContributionsNeverDecay() {
        DietData data = new DietData();
        data.eat("a", "plain", 4, 5, 2);
        data.eat("b", "plain", 2, 2, 2);
        assertEquals(7, data.diversity(0.25, 1), 0);
        data.eat("b", "plain", 2, 1, 2);
        assertEquals(2, data.mealCount());
        assertEquals(1.5, data.diversity(0.25, 1), 0);
        assertEquals(6, data.cumulative(), 0);
    }
    @Test public void historyAndMealsSurviveSaveCloneAndConfigChanges() {
        DietData data = new DietData(); data.initialized = true; data.legacyHealthFloor = 8;
        data.eat("a", "salt", 4, 5, 32);
        DietData loaded = new DietData(); loaded.deserializeNBT(data.serializeNBT());
        DietConfig.cumulativeFlavorFraction = 0.1;
        loaded.eat("a", "plain", 20, 2, 32);
        assertEquals(6, loaded.cumulative(), 0);
        assertEquals(3.5, loaded.average(), 0);
        assertEquals(8, loaded.legacyHealthFloor, 0);
        assertTrue(loaded.initialized);
        loaded.reset(false); assertEquals(8, loaded.legacyHealthFloor, 0); assertEquals(0, loaded.cumulative(), 0);
        loaded.reset(true); assertEquals(0, loaded.legacyHealthFloor, 0);
    }
    @Test public void migrationDoesNotInventMealsOrAllowSecondPlainGain() {
        DietData data = new DietData();
        data.migrateFood("a", 4); data.migrateFood("a", 50);
        assertEquals(4, data.cumulative(), 0); assertEquals(0, data.mealCount());
        assertEquals(0, data.eat("a", "plain", 20, 5, 32), 0);
        assertEquals(2, data.nextGain("a", "salt", 20), 0);
    }
    @Test public void legacyMixedCombinationsMergeWithoutLosingMealsOrSavedContributions() {
        DietData data = new DietData(); data.initialized = true; data.legacyHealthFloor = 8;
        data.eat("apple", "plain", 4, 2, 32);
        data.eat("apple", "solwarly:mixed:sugar+sugar+salt", 4, 6, 32);
        data.eat("apple", "solwarly:mixed:salt+salt+salt", 4, 9, 32);
        NBTTagCompound save = data.serializeNBT();
        net.minecraft.nbt.NBTTagList tastes = new net.minecraft.nbt.NBTTagList();
        for (String flavor : Arrays.asList("plain", "solwarly:mixed:sugar+sugar+salt", "solwarly:mixed:salt+salt+salt"))
            tastes.appendTag(new net.minecraft.nbt.NBTTagString(flavor));
        save.getTagList("history", 10).getCompoundTagAt(0).setTag("flavors", tastes);
        save.getTagList("meals", 10).getCompoundTagAt(1).setString("flavor", "solwarly:mixed:sugar+sugar+salt");
        save.getTagList("meals", 10).getCompoundTagAt(2).setString("flavor", "solwarly:mixed:salt+salt+salt");
        DietData loaded = new DietData(); loaded.deserializeNBT(save);
        assertEquals(6, loaded.cumulative(), 0); assertEquals(3, loaded.mealCount());
        assertEquals(17. / 3, loaded.average(), .000001); assertEquals(1.25, loaded.variety(.25), 0);
        assertEquals(8, loaded.legacyHealthFloor, 0); assertTrue(loaded.initialized);
        assertTrue(loaded.hasTried("apple", "solwarly:mixed"));
        assertEquals(0, loaded.nextGain("apple", "solwarly:mixed:ham+ham+ham", 4), 0);
        assertEquals(0, loaded.eat("apple", "solwarly:mixed:ham+ham+ham", 4, 4, 32), 0);
        assertEquals(4, loaded.eat("bread", "solwarly:mixed", 4, 4, 32), 0);
        DietData again = new DietData(); again.deserializeNBT(loaded.serializeNBT());
        assertEquals(loaded.cumulative(), again.cumulative(), 0);
        assertEquals(loaded.variety(.25), again.variety(.25), 0);
    }
    @Test public void dailySelectionIsSharedPersistentAndDoesNotRepeatAfterClockRollback() {
        AppetiteData data = new AppetiteData();
        List<String> foods = new ArrayList<>(); for (int i = 0; i < 12; i++) foods.add("food" + i);
        assertTrue(data.refresh(3, 2, foods, new Random(1)));
        assertEquals(40, data.retainedCount); assertEquals(5, data.craved.size()); assertEquals(5, data.boring.size());
        assertTrue(Collections.disjoint(data.craved, data.boring));
        AppetiteData restored = new AppetiteData(); restored.readFromNBT(data.writeToNBT(new NBTTagCompound()));
        assertEquals(data.craved, restored.craved);
        assertFalse(restored.refresh(3, 1, foods, new Random(2)));
        restored.setDirty(false);
        assertFalse(restored.refresh(2, 1, foods, new Random(2)));
        assertEquals(2, restored.lastDay);
        assertTrue(restored.isDirty());
        assertEquals(data.craved, restored.craved);
        assertEquals(data.boring, restored.boring);
        AppetiteData restarted = new AppetiteData();
        restarted.readFromNBT(restored.writeToNBT(new NBTTagCompound()));
        assertFalse(restarted.refresh(2, 1, foods, new Random(3)));
        assertEquals(data.craved, restarted.craved);
        assertTrue(restarted.refresh(3, 1, foods, new Random(3)));
        assertTrue(restored.refresh(4, 1, foods, new Random(2)));
    }
    @Test public void forwardTimeAndSleepAfterTimeSetRefreshWithoutChangingSavedMeals() {
        AppetiteData data = new AppetiteData();
        List<String> foods = Arrays.asList("a", "b", "c", "d");
        assertTrue(data.refresh(Math.floorDiv(72000L, 24000L), 1, foods, new Random(1)));
        DietData meals = new DietData(); meals.eat("a", "plain", 4, 5, 32);
        assertTrue(data.refresh(Math.floorDiv(96000L, 24000L), 1, foods, new Random(2))); // /time add 24000
        assertFalse(data.refresh(Math.floorDiv(13000L, 24000L), 1, foods, new Random(3))); // /time set night
        assertTrue(data.refresh(Math.floorDiv(24000L, 24000L), 1, foods, new Random(4))); // sleep to morning
        assertFalse(data.refresh(Math.floorDiv(25000L, 24000L), 1, foods, new Random(5)));
        assertEquals(5, meals.average(), 0);
        assertEquals(4, meals.cumulative(), 0);
    }
    @Test public void refreshBeforeWorldTickStillRequestsOneBroadcast() {
        AppetiteData data = new AppetiteData();
        List<String> foods = Arrays.asList("a", "b", "c");
        assertTrue(data.refresh(0, 1, foods, new Random(1)));
        assertTrue(data.consumePendingSync());
        assertFalse(data.consumePendingSync());
        // A meal/login refresh wins the race; the subsequent tick must still broadcast.
        assertTrue(data.refresh(1, 1, foods, new Random(2)));
        assertFalse(data.refresh(1, 1, foods, new Random(3)));
        assertTrue(data.consumePendingSync());
        assertFalse(data.consumePendingSync());
        assertFalse(data.refresh(0, 1, foods, new Random(4)));
        assertFalse(data.consumePendingSync());
    }
    @Test public void insufficientCandidatesAreSplitAndMinimumCountIsTwo() {
        AppetiteData data = new AppetiteData(); DietConfig.frequencyMaximum = 2;
        data.refresh(0, 0, Arrays.asList("a", "b", "c"), new Random(4));
        assertEquals(2, data.craved.size()); assertEquals(1, data.boring.size()); assertEquals(2, data.retainedCount);
        data.eat("a"); data.eat("b"); data.eat("c"); assertEquals(2, data.recentSize());
        AppetiteData empty = new AppetiteData(); empty.refresh(0, 1, Collections.emptyList(), new Random(1));
        assertTrue(empty.craved.isEmpty()); assertTrue(empty.boring.isEmpty());
    }
    @Test public void fixedAppetitesAreExcludedFromDrawsAndSurviveSnapshotWithBoringPrecedence() {
        DietConfig.alwaysCraved = new String[] {"fixed", "conflict"};
        DietConfig.alwaysBoring = new String[] {"disliked", "conflict"};
        List<String> foods = new ArrayList<>(Arrays.asList("fixed", "disliked", "conflict"));
        for (int i = 0; i < 12; i++) foods.add("food" + i);
        AppetiteData data = new AppetiteData();
        assertTrue(data.refresh(0, 1, foods, new Random(1)));
        assertEquals(5, data.craved.size()); assertEquals(5, data.boring.size());
        assertFalse(data.craved.contains("fixed")); assertFalse(data.boring.contains("disliked"));
        assertEquals(1, data.state("fixed")); assertEquals(-1, data.state("conflict"));
        assertEquals(6, data.cravedFoods().size()); assertEquals(7, data.boringFoods().size());
        data.refresh(1, 1, foods, new Random(3));
        assertEquals(1, data.state("fixed")); assertEquals(-1, data.state("disliked"));
        AppetiteData client = new AppetiteData(); client.readFromNBT(data.writeToNBT(new NBTTagCompound()));
        assertEquals(data.cravedFoods(), client.cravedFoods()); assertEquals(data.boringFoods(), client.boringFoods());
        assertEquals(-1, client.state("conflict"));
        DietConfig.alwaysCraved = new String[0]; DietConfig.alwaysBoring = new String[0];
        assertFalse(data.refresh(1, 1, foods, new Random(4)));
        assertEquals(0, data.state("fixed")); assertEquals(0, data.state("conflict"));
    }
    @Test public void heavilyEatenFoodStillHasChanceToBeCraved() {
        int chosen = 0;
        DietConfig.cravingCount = 1; DietConfig.boringCount = 0;
        for (int seed = 0; seed < 500; seed++) {
            AppetiteData data = new AppetiteData();
            for (int i = 0; i < 20; i++) data.eat("a");
            data.refresh(0, 1, Arrays.asList("a", "b", "c"), new Random(seed * 997L));
            if (data.craved.contains("a")) chosen++;
        }
        assertTrue(chosen > 0); assertTrue(chosen < 200);
    }
    @Test public void automaticFrequencyWeightsKeepRepeatedFoodNearSeventyFivePercentAcrossLargeCatalogs() {
        for (int candidates : new int[] {1000, 2000}) {
            double probability = boringRate(candidates, 2500);
            assertTrue("catalog=" + candidates + ", boring rate=" + probability,
                probability > .71 && probability < .79);
        }
    }
    @Test public void manualFrequencyWeightsSupportOriginalBiasAndNoFrequencyBias() {
        DietConfig.frequencyWeight = 1;
        double original = boringRate(1000, 2500);
        assertTrue("original boring rate=" + original, original > .05 && original < .115);
        DietConfig.frequencyWeight = 0;
        List<String> candidates = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k");
        AppetiteData repeated = new AppetiteData(), uneaten = new AppetiteData();
        for (int i = 0; i < 40; i++) repeated.eat("a");
        repeated.refresh(0, 1, candidates, new Random(177));
        uneaten.refresh(0, 1, candidates, new Random(177));
        assertEquals(uneaten.craved, repeated.craved); assertEquals(uneaten.boring, repeated.boring);
    }
    private double boringRate(int count, int trials) {
        List<String> candidates = new ArrayList<>(); candidates.add("repeated");
        for (int i = 1; i < count; i++) candidates.add("food" + i);
        int chosen = 0; Random random = new Random(177);
        for (int trial = 0; trial < trials; trial++) {
            AppetiteData data = new AppetiteData();
            for (int meal = 0; meal < 40; meal++) data.eat("repeated");
            data.refresh(0, 1, candidates, random);
            if (data.boring.contains("repeated")) chosen++;
        }
        return chosen / (double) trials;
    }
    private void recipe(PreparationGraph graph, String output, String name, double weight, String... inputs) {
        List<List<String>> ingredients = new ArrayList<>();
        for (String input : inputs) ingredients.add(Collections.singletonList(input));
        graph.add(output, name, weight, ingredients);
    }
    @Test public void reversiblePackingCannotBecomeFreeSource() {
        PreparationGraph graph = new PreparationGraph(); graph.root("ore");
        recipe(graph, "ingot", "smelt", 1, "ore");
        recipe(graph, "block", "pack", 1, "ingot");
        recipe(graph, "ingot", "unpack", 1, "block");
        assertEquals(1, graph.analyze().get("ingot"), 0);
        assertEquals(2, graph.analyze().get("block"), 0);
    }
    @Test public void feastServingsInheritPreparationAndReassemblyCannotShortenIt() {
        PreparationGraph graph = new PreparationGraph(); graph.root("raw");
        recipe(graph, "prepared", "cut", 1, "raw");
        recipe(graph, "pie", "cook", 1.5, "prepared");
        recipe(graph, "slice", "serve", 0.25, "pie");
        recipe(graph, "pie", "reassemble", 1, "slice");
        assertEquals(2.5, graph.analyze().get("pie"), 0);
        assertEquals(2.75, graph.analyze().get("slice"), 0);
    }
    @Test public void allParallelIngredientsCountAndSharedProcessesAreDeduplicated() {
        PreparationGraph graph = new PreparationGraph(); graph.root("raw");
        recipe(graph, "a", "aProcess", 5, "raw"); recipe(graph, "b", "bProcess", 3, "raw");
        recipe(graph, "c", "cProcess", 3, "raw"); recipe(graph, "d", "dProcess", 1, "raw"); recipe(graph, "e", "eProcess", 1, "raw");
        recipe(graph, "first", "first", 1, "a", "b", "c"); recipe(graph, "second", "second", 1, "a", "d", "e");
        assertEquals(12, graph.analyze().get("first"), 0); assertEquals(8, graph.analyze().get("second"), 0);
        recipe(graph, "b2", "b2", 1, "a"); recipe(graph, "c2", "c2", 1, "a");
        recipe(graph, "shared", "shared", 1, "b2", "c2", "b2");
        assertEquals(8, graph.analyze().get("shared"), 0);
    }
    @Test public void isolatedCycleRemainsUnresolved() {
        PreparationGraph graph = new PreparationGraph();
        recipe(graph, "a", "a", 1, "b"); recipe(graph, "b", "b", 1, "a");
        assertFalse(graph.analyze().containsKey("a")); assertFalse(graph.analyze().containsKey("b"));
    }
    @Test public void sharedRouteCanBeatIndividuallyCheapestIngredients() {
        PreparationGraph graph = new PreparationGraph(); graph.root("raw");
        recipe(graph, "shared", "shared", 3, "raw");
        recipe(graph, "a", "cheapA", 2, "raw");
        recipe(graph, "a", "sharedA", 0.25, "shared");
        recipe(graph, "b", "cheapB", 2, "raw");
        recipe(graph, "b", "sharedB", 0.25, "shared");
        recipe(graph, "dish", "combine", 1, "a", "b");
        assertEquals(4.5, graph.analyze().get("dish"), 0);
    }
    @Test public void downstreamRecipeIsRecheckedWhenUpstreamRouteAppearsLater() {
        PreparationGraph graph = new PreparationGraph(); graph.root("raw");
        recipe(graph, "dish", "combine", 1, "prepared");
        recipe(graph, "prepared", "prepare", 2, "raw");
        assertEquals(3, graph.analyze().get("dish"), 0);
    }
    @Test public void recalculationPreservesUnavailableFoodAndSavedContributions() {
        DietData data = new DietData(); data.eat("missing", "plain", 4, 5, 32);
        data.eat("present", "plain", 2, 3, 32);
        data.recalculate(id -> id.equals("missing") ? Double.NaN : 6);
        assertEquals(10, data.cumulative(), 0);
        assertEquals(4, data.average(), 0);
    }
    @Test public void malformedRewardsAreIgnoredWithoutDiscardingValidEntries() {
        assertArrayEquals(new String[] {"20|4"}, DietConfig.validRewards(new String[] {"20|4", "20|8", "NaN|2", "4|oops", "2|-1"}, 2));
        assertArrayEquals(new String[] {"5|generic.maxHealth|2|0"}, DietConfig.validRewards(new String[] {"5|generic.maxHealth|2|0", "5|generic.maxHealth|4|0", "2|generic.maxHealth|Infinity|0", "3|generic.maxHealth|2|3"}, 4));
        assertArrayEquals(new String[] {"5|minecraft:speed|0"}, DietConfig.validRewards(new String[] {"5|minecraft:speed|0", "3|minecraft:speed|-1", "4|minecraft:speed|256"}, 3));
    }
    @Test public void warlyModeUsesConfiguredRepeatTiersAndExpiresAfterTwoDays() {
        DietConfig.warlyMode = true;
        DietData data = new DietData();
        assertEquals(1, data.warlyMultiplier("stew", 0), 0);
        data.recordWarlyMeal("stew", 0);
        assertEquals(.9, data.warlyMultiplier("stew", 1), 0);
        data.recordWarlyMeal("stew", 1);
        assertEquals(.8, data.warlyMultiplier("stew", 2), 0);
        data.recordWarlyMeal("stew", 2);
        assertEquals(.65, data.warlyMultiplier("stew", 3), 0);
        data.recordWarlyMeal("stew", 3);
        assertEquals(.5, data.warlyMultiplier("stew", 4), 0);
        data.recordWarlyMeal("stew", 4);
        assertEquals(.3, data.warlyMultiplier("stew", 5), 0);
        data.recordWarlyMeal("stew", 5);
        assertEquals(1, data.warlyMultiplier("stew", 48005), 0);
    }
    @Test public void warlyModeOverrideAndRepeatRecordsSurviveNbt() {
        DietConfig.warlyMode = false;
        DietData data = new DietData(); data.setWarlyOverride(1); data.recordWarlyMeal("stew", 100);
        DietData loaded = new DietData(); loaded.deserializeNBT(data.serializeNBT());
        assertEquals(1, loaded.warlyOverride());
        assertEquals(.9, loaded.warlyMultiplier("stew", 101), 0);
        loaded.setWarlyOverride(0); assertEquals(1, loaded.warlyMultiplier("stew", 101), 0);
        loaded.setWarlyOverride(-1); assertEquals(1, loaded.warlyMultiplier("stew", 101), 0);
    }
}
