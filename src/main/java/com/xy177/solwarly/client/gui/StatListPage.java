// Modified by xy177 on 2026-10-10 for Spice of Life Warly Edition; upstream LGPL 2.1.
package com.xy177.solwarly.client.gui;

import com.xy177.solwarly.client.gui.elements.UIBox;
import com.xy177.solwarly.tracking.ProgressInfo;

import java.awt.*;

import static com.xy177.solwarly.lib.Localization.localized;

final class StatListPage extends Page {
	StatListPage(FoodData foodData, Rectangle frame) {
		super(frame, localized("gui", "food_book.stats"));
		
		ProgressInfo progressInfo = foodData.progressInfo;
		ProgressInfo.ConfigInfo configInfo = progressInfo.configInfo;
		ProgressGraph progressGraph = new ProgressGraph(foodData, getCenterX(), (int) mainStack.frame.getMinY() + 43);
		children.add(progressGraph);
		
		mainStack.addChild(new UIBox(progressGraph.frame, new Color(0, 0, 0, 0))); // invisible placeholder box
		
		mainStack.addChild(makeSeparatorLine());
		
		String foodsTasted;
		if (configInfo.shouldShowUneatenFoods) {
			foodsTasted = fraction(progressInfo.foodsEaten, foodData.validFoods.size());
		} else {
			foodsTasted = "" + progressInfo.foodsEaten;
		}
		
		mainStack.addChild(statWithIcon(
			icon(GuiFoodBook.carrotImage),
			foodsTasted,
			localized("gui", "food_book.stats.foods_tasted")
		));
		
		mainStack.addChild(makeSeparatorLine());
		
		String heartsGained = fraction(
			configInfo.heartsPerMilestone * progressInfo.milestonesAchieved(),
			configInfo.heartsPerMilestone * configInfo.milestones.length
		);
		
		mainStack.addChild(statWithIcon(
			icon(GuiFoodBook.heartImage),
			heartsGained,
			localized("gui", "food_book.stats.hearts_gained")
		));
		
		mainStack.addChild(makeSeparatorLine());
		
		updateMainStack();
	}
}
