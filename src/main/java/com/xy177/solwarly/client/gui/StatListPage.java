// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/37c243c5bcf50d5582ce81596e85b3f465b222a8/src/main/java/com/cazsius/solcarrot/client/gui/StatListPage.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client.gui;

import com.xy177.solwarly.SOLWarlyConfig;
import com.xy177.solwarly.client.gui.elements.UIBox;
import com.xy177.solwarly.tracking.ProgressInfo;

import java.awt.*;

import static com.xy177.solwarly.lib.Localization.localized;

final class StatListPage extends Page {
	StatListPage(FoodData foodData, Rectangle frame) {
		super(frame, localized("gui", "food_book.stats"));
		
		ProgressInfo progressInfo = foodData.progressInfo;
		ProgressGraph progressGraph = new ProgressGraph(foodData, getCenterX(), (int) mainStack.frame.getMinY() + 43);
		children.add(progressGraph);
		
		mainStack.addChild(new UIBox(progressGraph.frame, new Color(0, 0, 0, 0))); // invisible placeholder box
		
		mainStack.addChild(makeSeparatorLine());
		
		String foodsTasted;
		if (SOLWarlyConfig.shouldShowUneatenFoods()) {
			foodsTasted = fraction(progressInfo.foodsEaten, foodData.validFoods.size());
		} else {
			foodsTasted = "" + progressInfo.foodsEaten;
		}
		
		mainStack.addChild(statWithIcon(
			FoodBookScreen.carrotImage,
			foodsTasted,
			localized("gui", "food_book.stats.foods_tasted")
		));
		
		mainStack.addChild(makeSeparatorLine());
		
		int heartsPerMilestone = SOLWarlyConfig.getHeartsPerMilestone();
		String heartsGained = fraction(
			heartsPerMilestone * progressInfo.milestonesAchieved(),
			heartsPerMilestone * SOLWarlyConfig.getMilestoneCount()
		);
		
		mainStack.addChild(statWithIcon(
			FoodBookScreen.heartImage,
			heartsGained,
			localized("gui", "food_book.stats.hearts_gained")
		));
		
		mainStack.addChild(makeSeparatorLine());
		
		updateMainStack();
	}
}
