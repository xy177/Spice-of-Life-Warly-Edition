package com.xy177.solwarly.diet;

import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import java.util.*;
import java.lang.reflect.*;

/** Optional adapter: class names never resolve unless Farmer's Delight is installed. */
final class FarmerRecipes {
    private static final String PREFIX = "com.wdcftgg.farmersdelightlegacy.";
    static void add(PreparationGraph graph) {
        try {
            for (Object recipe : recipes("CookingPotRecipeApi")) {
                ItemStack output = (ItemStack) call(recipe, "getResultStack");
                FoodCatalog.discover(output);
                List<List<String>> inputs = new ArrayList<>();
                for (Object ingredient : (List<?>) call(recipe, "getIngredients")) {
                    List<String> options = ids((List<?>) call(ingredient, "getDisplayStacks"));
                    if (!options.isEmpty()) inputs.add(options);
                }
                graph.add(FoodCatalog.id(output), "pot:" + call(recipe, "getRecipeId"), DietConfig.cookingWeight, inputs);
            }
            for (Object recipe : recipes("CuttingBoardRecipeApi")) {
                List<String> input = ids((List<?>) call(recipe, "getInputOptions"));
                for (Object output : (List<?>) call(recipe, "getResultStacks")) {
                    ItemStack stack = (ItemStack) output;
                    FoodCatalog.discover(stack);
                    if (!stack.isEmpty()) graph.add(FoodCatalog.id(stack), "cut:" + call(recipe, "getRecipeId"),
                        DietConfig.cuttingWeight, Collections.singletonList(input));
                }
            }
            Class<?> feast = Class.forName(PREFIX + "common.block.BlockFeast");
            Class<?> pie = Class.forName(PREFIX + "common.block.BlockPie");
            for (Block block : ForgeRegistries.BLOCKS) {
                ItemStack whole = new ItemStack(Item.getItemFromBlock(block));
                if (pie.isInstance(block) && block.getRegistryName().getPath().equals("pumpkin_pie"))
                    whole = new ItemStack(net.minecraft.init.Items.PUMPKIN_PIE);
                if (whole.isEmpty()) continue;
                ItemStack portion = ItemStack.EMPTY;
                if (feast.isInstance(block)) portion = (ItemStack) call(block, "getCookingPotServingStack");
                else if (pie.isInstance(block)) {
                    Field field = pie.getDeclaredField("sliceItemId"); field.setAccessible(true);
                    Item item = ForgeRegistries.ITEMS.getValue((net.minecraft.util.ResourceLocation) field.get(block));
                    if (item != null) portion = new ItemStack(item);
                }
                if (!portion.isEmpty()) {
                    FoodCatalog.discover(portion);
                    graph.add(FoodCatalog.id(portion), "serve:" + block.getRegistryName(),
                        DietConfig.servingWeight, Collections.singletonList(Collections.singletonList(FoodCatalog.id(whole))));
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Farmer's Delight Legacy 1.1.2 recipe adapter failed", e);
        }
    }
    private static List<?> recipes(String name) throws ReflectiveOperationException {
        return (List<?>) Class.forName(PREFIX + "api.recipe." + name).getMethod("getRecipes").invoke(null);
    }
    private static Object call(Object object, String name) throws ReflectiveOperationException { return object.getClass().getMethod(name).invoke(object); }
    private static List<String> ids(List<?> stacks) {
        List<String> result = new ArrayList<>();
        for (Object object : stacks) {
            ItemStack stack = (ItemStack) object;
            FoodCatalog.discover(stack);
            if (!stack.isEmpty()) result.add(FoodCatalog.id(stack));
        }
        return result;
    }
    private FarmerRecipes() {}
}
