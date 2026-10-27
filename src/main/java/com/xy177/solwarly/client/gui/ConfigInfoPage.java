// Warly Edition configuration overview, by xy177, 2026-10-27; LGPL-2.1.
package com.xy177.solwarly.client.gui;

import com.xy177.solwarly.client.FoodItemStacks;
import com.xy177.solwarly.client.gui.elements.UILabel;
import net.minecraft.client.resources.I18n;
import java.awt.Rectangle;

final class ConfigInfoPage extends Page {
    ConfigInfoPage(FoodData data, Rectangle frame) {
        super(frame, I18n.format("gui.solwarly.food_book.config"));
        int total = FoodItemStacks.getAllFoodsIgnoringBlacklist().size();
        int allowed = FoodItemStacks.getAllFoods().size();
        long eatenCheap = data.foodList.getEatenFoods().stream()
            .map(food -> food.getItemStack())
            .filter(food -> com.xy177.solwarly.diet.FoodRules.current().isAllowed(food) && !com.xy177.solwarly.diet.FoodRules.current().isHearty(food)).count();
        boolean whitelist = com.xy177.solwarly.diet.FoodRules.current().hasWhitelist();
        row("minimum_food_value", com.xy177.solwarly.lib.DisplayNumbers.format(com.xy177.solwarly.diet.FoodRules.current().minimumFoodValue/2.0));
        row("eaten_cheap_foods", eatenCheap+"/"+(allowed-data.validFoods.size()));
        row("hearty_fraction", data.validFoods.size()+"/"+allowed);
        row(whitelist ? "whitelist" : "blacklist", (whitelist ? allowed : total-allowed)+"/"+total);
        updateMainStack();
    }

    private void row(String key, String value) {
        UILabel label = new UILabel(I18n.format("gui.solwarly.food_book.config."+key)+": "+value, contentWidth);
        if (!key.equals("hearty_fraction") && !key.equals("eaten_cheap_foods"))
            label.tooltip = I18n.format("gui.solwarly.food_book.config.tooltip."+key);
        mainStack.addChild(label);
    }
}
