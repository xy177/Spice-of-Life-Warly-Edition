// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/3fbe133834091bbbc73ba0f37cf582d2bb66b10d/src/main/java/com/cazsius/solcarrot/client/gui/FoodData.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client.gui;

import com.xy177.solwarly.api.FlavorAPI;
import com.xy177.solwarly.client.FoodItemStacks;
import com.xy177.solwarly.diet.*;
import com.xy177.solwarly.tracking.FoodList;
import com.xy177.solwarly.tracking.ProgressInfo;
import net.minecraft.item.ItemStack;
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
        validFoods = FoodItemStacks.getAllFoods().stream().filter(progressInfo.configInfo::shouldCount).collect(Collectors.toList());
        for (ItemStack food : validFoods) {
            boolean eaten = foodList.hasEaten(food) || diet != null && diet.hasTriedFood(FoodCatalog.id(food));
            (eaten ? missingFlavors(food).isEmpty() ? completeFoods : eatenFoods : uneatenFoods).add(food);
        }
    }
    List<ItemStack> missingFlavors(ItemStack food) {
        String id = FoodCatalog.id(food);
        return FlavorAPI.variants(food).stream().filter(variant -> {
            String flavor = FlavorAPI.resolve(variant).id;
            if (diet != null && diet.hasTried(id, flavor)) return false;
            return !(flavor.equals("plain") && foodList.hasEaten(food) && (diet == null || !diet.hasTriedFood(id)));
        }).collect(Collectors.toList());
    }
}
