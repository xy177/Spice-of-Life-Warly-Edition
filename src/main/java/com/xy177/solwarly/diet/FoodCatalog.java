package com.xy177.solwarly.diet;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.tracking.FoodInstance;
import com.xy177.solwarly.tracking.ProgressInfo;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.food.FoodValues;
import java.io.File;
import java.util.*;

public final class FoodCatalog {
    private static final Map<String, ItemStack> foods = new TreeMap<>();
    private static final Map<String, Double> coefficients = new HashMap<>();
    private static Map<String, Double> difficulties = Collections.emptyMap();
    private static Set<String> outputs = Collections.emptySet();
    private static Configuration foodConfig;
    private static boolean ready;
    public static String id(ItemStack stack) { return new FoodInstance(stack).encode(); }
    public static ItemStack stack(String id) {
        FoodInstance food = FoodInstance.decode(id);
        return food == null ? ItemStack.EMPTY : food.getItemStack();
    }
    public static Collection<String> candidates() {
        List<String> result = new ArrayList<>();
        ProgressInfo.ConfigInfo rules = new ProgressInfo.ConfigInfo();
        for (Map.Entry<String, ItemStack> entry : foods.entrySet())
            if (rules.shouldCount(entry.getValue()) && !Arrays.asList(DietConfig.excludedInterest).contains(entry.getKey()))
                result.add(entry.getKey());
        return result;
    }
    public static double coefficient(ItemStack stack) {
        stack = com.xy177.solwarly.spice.Seasoning.plain(stack);
        String id = id(stack);
        if (!coefficients.containsKey(id)) {
            foods.put(id, stack.copy());
            coefficients.put(id, configured(id, stack, false));
            if (foodConfig.hasChanged()) foodConfig.save();
        }
        return coefficients.get(id);
    }
    public static boolean isReady() { return ready; }
    public static void invalidate() { ready = false; }
    public static Set<String> ids() { return new TreeSet<>(foods.keySet()); }
    private static double nutrition(ItemStack stack, double difficulty) {
        FoodValues values = AppleCoreAPI.accessor.getFoodValues(stack);
        double value = difficulty * DietConfig.difficultyWeight;
        if (values != null) value += values.hunger * DietConfig.hungerWeight
            + values.hunger * values.saturationModifier * 2 * DietConfig.saturationWeight;
        return Math.round(Math.max(DietConfig.minimum, Math.min(DietConfig.maximum, value)) * 100) / 100.0;
    }
    public static void build() { build(false, false); }
    public static void rebuild() { build(false, true); }
    public static void build(boolean regenerate) { build(regenerate, false); }
    private static void build(boolean regenerate, boolean forceAnalysis) {
        long started = System.nanoTime();
        foods.clear(); coefficients.clear();
        foodConfig = new Configuration(new File(DietConfig.directory, "solwarly-foods.cfg"));
        foodConfig.load();
        foodConfig.setCategoryComment("foods", "食物最终基础系数，自动生成缺项，人工修改优先，已有有效值不被覆盖；制作链无法确定时仅按营养估算，具体条目见日志。\nFinal food base coefficients: missing entries are generated, manual values take precedence, and existing valid values are preserved. Unresolved preparation chains use nutrition only; see the log for affected foods.");
        PreparationGraph graph = new PreparationGraph();
        for (String root : DietConfig.rawSources) graph.root(root.contains("@") ? root : root + "@0");
        for (Item item : ForgeRegistries.ITEMS) {
            discover(new ItemStack(item));
        }
        for (int meta = 0; meta < 4; meta++) discover(new ItemStack(net.minecraft.init.Items.FISH, 1, meta));
        for (String ore : net.minecraftforge.oredict.OreDictionary.getOreNames())
            for (ItemStack stack : net.minecraftforge.oredict.OreDictionary.getOres(ore)) discover(stack);
        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            ItemStack output = recipe.getRecipeOutput();
            if (output.isEmpty()) continue;
            discover(output);
            List<List<String>> ingredients = new ArrayList<>();
            boolean teaMortar = false;
            for (Ingredient ingredient : recipe.getIngredients()) {
                List<String> options = new ArrayList<>();
                for (ItemStack input : ingredient.getMatchingStacks()) {
                    discover(input);
                    if (!input.isEmpty() && new net.minecraft.util.ResourceLocation("teastory:wooden_mortar_and_pestle").equals(input.getItem().getRegistryName())) teaMortar = true;
                    if (!input.isEmpty() && (!input.getItem().hasContainerItem(input) || AppleCoreAPI.accessor.isFood(input))) options.add(id(input));
                }
                if (!options.isEmpty()) ingredients.add(options);
            }
            if (!ingredients.isEmpty()) graph.add(id(output), "craft:" + recipe.getRegistryName(), teaMortar ? DietConfig.teaWeights.getOrDefault("mortarGrinding", 1.75) : DietConfig.craftWeight, ingredients);
        }
        FurnaceRecipes.instance().getSmeltingList().forEach((input, output) -> {
            discover(input); discover(output);
            graph.add(id(output), "smelt:" + id(input) + ":" + id(output), DietConfig.furnaceWeight,
                Collections.singletonList(Collections.singletonList(id(input))));
        });
        if (Loader.isModLoaded("farmersdelight")) FarmerRecipes.add(graph);
        if (Loader.isModLoaded("teastory")) TeaRecipes.add(graph);
        List<String> missing = new ArrayList<>();
        for (String id : foods.keySet()) if (!hasConfiguredValue(id)) missing.add(id);
        if (forceAnalysis || regenerate || !missing.isEmpty()) {
            Collection<String> targets = forceAnalysis || regenerate ? foods.keySet() : missing;
            SOLWarly.LOGGER.info("Starting food preparation analysis: {} targets, {} missing coefficients.", targets.size(), missing.size());
            long analysisStarted = System.nanoTime();
            difficulties = graph.analyze(targets);
            outputs = graph.outputs();
            SOLWarly.LOGGER.info("Food preparation analysis finished in {} ms.", (System.nanoTime() - analysisStarted) / 1000000);
        } else {
            difficulties = Collections.emptyMap();
            outputs = Collections.emptySet();
            SOLWarly.LOGGER.info("Food coefficients are complete; skipping preparation analysis.");
        }
        for (Map.Entry<String, ItemStack> entry : foods.entrySet()) {
            coefficients.put(entry.getKey(), configured(entry.getKey(), entry.getValue(), regenerate));
        }
        foodConfig.save(); // Persist updated comments even when every coefficient already exists.
        ready = true;
        SOLWarly.LOGGER.info("Food catalog ready: {} foods in {} ms.", foods.size(), (System.nanoTime() - started) / 1000000);
    }
    private static boolean hasConfiguredValue(String id) {
        net.minecraftforge.common.config.Property property = foodConfig.getCategory("foods").get(id);
        if (property == null) return false;
        double value = property.getDouble(Double.NaN);
        return Double.isFinite(value) && value >= 0 && value <= 100000;
    }
    private static double configured(String id, ItemStack stack, boolean regenerate) {
        if (!regenerate && hasConfiguredValue(id)) {
            net.minecraftforge.common.config.Property property = foodConfig.getCategory("foods").get(id);
            property.setComment(null);
            return property.getDouble();
        }
        double calculated = nutrition(stack, difficulties.getOrDefault(id, 0.0));
        if (outputs.contains(id) && !difficulties.containsKey(id)) {
            SOLWarly.LOGGER.warn("Unresolved food preparation chain: {}", id);
        }
        net.minecraftforge.common.config.Property property = foodConfig.get("foods", id, calculated, null, 0, 100000);
        if (regenerate) property.set(calculated);
        double value = property.getDouble(Double.NaN);
        if (!Double.isFinite(value) || value < 0 || value > 100000) {
            property.set(calculated);
            return calculated;
        }
        return value;
    }
    public static void discover(ItemStack stack) {
        if (!stack.isEmpty() && stack.getMetadata() != net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE
            && AppleCoreAPI.accessor.isFood(stack)) foods.put(id(stack), stack.copy());
    }
    private FoodCatalog() {}
}
