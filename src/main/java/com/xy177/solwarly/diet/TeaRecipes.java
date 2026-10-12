package com.xy177.solwarly.diet;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import java.util.*;
import java.lang.reflect.*;

/** Optional adapter for Tea the Story 3.3.3-B33.418, without client classes. */
final class TeaRecipes {
    static void add(PreparationGraph graph) {
        try {
            Class<?> registry = Class.forName("roito.teastory.common.RecipeRegister");
            String[][] managers = {{"managerCookingPan", "cookingPan"}, {"managerDryingPan", "dryingPan"},
                {"managerTeapanInSun", "sunDrying"}, {"managerTeapanIndoors", "indoorAging"}, {"managerWet", "wetting"},
                {"managerStoveDrying", "stoveDrying"}, {"managerStoveSteam", "stoveSteam"}, {"managerTeaTable", "teaTable"}};
            // The hot-water pot is reusable equipment. Count boiling its water, not making the pot.
            String hotWater = "solwarly:teastory_heated_water";
            graph.add(hotWater, "teastory:waterHeating", weight("waterHeating"), Collections.emptyList());
            int count = 0;
            for (String[] manager : managers) {
                Object instance = registry.getField(manager[0]).get(null);
                for (Object recipe : (Collection<?>) call(instance, "getRecipes")) {
                    ItemStack output = (ItemStack) call(recipe, "getOutput");
                    if (output.isEmpty()) continue;
                    List<List<String>> ingredients = new ArrayList<>();
                    if (manager[1].equals("teaTable")) {
                        ingredients.add(Collections.singletonList(hotWater));
                        options(ingredients, (Collection<?>) call(recipe, "getTeaLeafInput"), false);
                        options(ingredients, (Collection<?>) call(recipe, "getSugarInput"), false);
                        // This slot also holds consumed milk/lemon; only durable tools are ignored.
                        options(ingredients, (Collection<?>) call(recipe, "getToolInput"), true);
                    } else options(ingredients, (Collection<?>) call(recipe, "getInputs"), false);
                    if (ingredients.isEmpty()) continue;
                    FoodCatalog.discover(output);
                    graph.add(FoodCatalog.id(output), "teastory:" + manager[1] + ":" + count++, weight(manager[1]), ingredients);
                }
            }
            // Rice is washed as an entity in water, outside the recipe managers.
            Class<?> general = Class.forName("roito.teastory.config.ConfigMain").getField("general").getType();
            Object settings = Class.forName("roito.teastory.config.ConfigMain").getField("general").get(null);
            boolean oreRice = general.getField("useOreDictionaryWhenWashingRice").getBoolean(settings);
            Item rice = Item.getByNameOrId("teastory:xian_rice"), washed = Item.getByNameOrId("teastory:washed_rice");
            if (rice != null && washed != null) {
                List<List<String>> inputs = new ArrayList<>();
                options(inputs, oreRice ? OreDictionary.getOres("cropRice") : Collections.singletonList(new ItemStack(rice)), false);
                if (!inputs.isEmpty()) graph.add(FoodCatalog.id(new ItemStack(washed)), "teastory:riceWashing", weight("riceWashing"), inputs);
            }
            // Cup variants poured from a kettle inherit the kettle's complete tea route.
            Class<?> kettle = Class.forName("roito.teastory.block.FullKettle");
            for (Block block : ForgeRegistries.BLOCKS) if (kettle.isInstance(block)) {
                Item drink = (Item) kettle.getMethod("getDrink", kettle).invoke(block, block);
                ItemStack whole = new ItemStack(block);
                if (drink == null || whole.isEmpty()) continue;
                for (int meta : new int[] {0, 2, 3, 4, 5}) {
                    ItemStack portion = new ItemStack(drink, 1, meta); FoodCatalog.discover(portion);
                    graph.add(FoodCatalog.id(portion), "teastory:pour:" + block.getRegistryName(), DietConfig.servingWeight,
                        Collections.singletonList(Collections.singletonList(FoodCatalog.id(whole))));
                }
            }
            SOLWarly.LOGGER.info("Tea the Story preparation adapter loaded {} processing recipes.", count);
        } catch (ReflectiveOperationException e) { throw new IllegalStateException("Tea the Story 3.3.3 recipe adapter failed", e); }
    }
    private static double weight(String process) { return DietConfig.teaWeights.getOrDefault(process, 1.75); }
    private static Object call(Object instance, String name) throws ReflectiveOperationException { return instance.getClass().getMethod(name).invoke(instance); }
    private static void options(List<List<String>> ingredients, Collection<?> stacks, boolean toolSlot) {
        Set<String> options = new TreeSet<>(); boolean optional = false;
        for (Object entry : stacks) {
            ItemStack stack = (ItemStack) entry;
            if (stack.isEmpty()) { optional = true; continue; }
            if (toolSlot && stack.isItemStackDamageable()) { optional = true; continue; }
            FoodCatalog.discover(stack);
            if (stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                // Wildcard is a matching rule, never a separate ingredient identity.
                for (ItemStack candidate : OreDictionary.getOres("listAllmilk"))
                    if (candidate.getItem() == stack.getItem() && candidate.getMetadata() != OreDictionary.WILDCARD_VALUE) options.add(FoodCatalog.id(candidate));
                options.add(FoodCatalog.id(new ItemStack(stack.getItem())));
            } else options.add(FoodCatalog.id(stack));
        }
        if (!optional) ingredients.add(new ArrayList<>(options));
    }
    private TeaRecipes() {}
}
