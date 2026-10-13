// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/37c243c5bcf50d5582ce81596e85b3f465b222a8/src/main/java/com/cazsius/solcarrot/client/gui/ConfigInfoPage.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client.gui;

import com.xy177.solwarly.SOLWarlyConfig;
import com.xy177.solwarly.client.FoodItems;
import com.xy177.solwarly.client.gui.elements.ImageData;
import com.xy177.solwarly.client.gui.elements.UIElement;
import com.xy177.solwarly.tracking.FoodInstance;

import java.awt.*;

import static com.xy177.solwarly.lib.Localization.localized;

final class ConfigInfoPage extends Page {
	ConfigInfoPage(FoodData foodData, Rectangle frame) {
		super(frame, localized("gui", "food_book.config"));
		
		int totalFoods = FoodItems.getAllFoods().size();
		int validFoods = foodData.validFoods.size();
		int cheapFoods = totalFoods - validFoods;
		int eatenCheapFoods = (int) foodData.foodList.getEatenFoods().stream()
			.map(FoodInstance::getItem)
			.filter(food -> SOLWarlyConfig.isAllowed(food) && !SOLWarlyConfig.isHearty(food))
			.count();
		
		{
			int minValue = SOLWarlyConfig.getMinimumFoodValue();
			String minValueDesc = "" + (minValue / 2);
			if (minValue % 2 == 1) {
				minValueDesc += ".5";
			}
			
			UIElement minValueStat = statWithIcon(
				FoodBookScreen.drumstickImage,
				minValueDesc,
				localized("gui", "food_book.config.minimum_food_value")
			);
			minValueStat.tooltip = localized("gui", "food_book.config.tooltip.minimum_food_value");
			mainStack.addChild(minValueStat);
		}
		
		{
			UIElement cheapStat = statWithIcon(
				FoodBookScreen.spiderEyeImage,
				fraction(eatenCheapFoods, cheapFoods),
				localized("gui", "food_book.config.eaten_cheap_foods")
			);
			cheapStat.tooltip = localized("gui", "food_book.config.tooltip.eaten_cheap_foods", cheapFoods, eatenCheapFoods);
			mainStack.addChild(cheapStat);
		}
		
		mainStack.addChild(makeSeparatorLine());
		
		{
			boolean hasWhitelist = SOLWarlyConfig.hasWhitelist();
			String listKey = hasWhitelist ? "whitelist" : "blacklist";
			
			ImageData listIcon = hasWhitelist ? FoodBookScreen.whitelistImage : FoodBookScreen.blacklistImage;
			
			int allFoods = FoodItems.getAllFoodsIgnoringBlacklist().size();
			int allowedFoods = FoodItems.getAllFoods().size();
			String fraction = hasWhitelist
				? fraction(allowedFoods, allFoods)
				: fraction(allFoods - allowedFoods, allFoods);
			UIElement listStat = statWithIcon(
				listIcon,
				fraction,
				localized("gui", "food_book.config." + listKey)
			);
			listStat.tooltip = localized("gui", "food_book.config.tooltip." + listKey);
			mainStack.addChild(listStat);
		}
		
		mainStack.addChild(makeSeparatorLine());
		
		updateMainStack();
	}
}
