package com.xy177.solwarly.client.gui;

import com.xy177.solwarly.api.FlavorAPI;
import com.xy177.solwarly.client.FoodItems;
import com.xy177.solwarly.diet.*;
import com.xy177.solwarly.tracking.FoodList;
import com.xy177.solwarly.tracking.ProgressInfo;
import net.minecraft.world.item.ItemStack;
import java.util.*;
import java.util.stream.Collectors;

final class FoodData {
    final FoodList foodList;
    final ProgressInfo progressInfo;
    final List<ItemStack> validFoods, eatenFoods = new ArrayList<>(), uneatenFoods = new ArrayList<>(), completeFoods = new ArrayList<>();
    private final DietData diet;
    FoodData(FoodList foodList, DietData diet) {
        this.foodList = foodList; this.diet = diet;
        progressInfo = foodList.getProgressInfo();
        validFoods = FoodItems.getAllFoods().stream().map(net.minecraft.world.item.ItemStack::new).toList().stream().filter(food->com.xy177.solwarly.SOLWarlyConfig.shouldCount(food.getItem())).collect(Collectors.toList());
        for (ItemStack food : validFoods) {
            boolean eaten = foodList.hasEaten(food.getItem()) || diet != null && diet.hasTriedFood(FoodCatalog.id(food));
            (eaten ? missingFlavors(food).isEmpty() ? completeFoods : eatenFoods : uneatenFoods).add(food);
        }
    }
    List<ItemStack> missingFlavors(ItemStack food) {
        String id = FoodCatalog.id(food);
        return FlavorAPI.variants(food).stream().filter(variant -> {
            String flavor = FlavorAPI.resolve(variant).id;
            if (diet != null && diet.hasTried(id, flavor)) return false;
            return !(flavor.equals("plain") && foodList.hasEaten(food.getItem()) && (diet == null || !diet.hasTriedFood(id)));
        }).collect(Collectors.toList());
    }
}
