// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/412fa94a07d93f09378fc0e3393de38eb0099824/src/main/java/com/cazsius/solcarrot/tracking/ProgressInfo.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.tracking;

import com.xy177.solwarly.SOLWarlyConfig;

/** contains all relevant variables for current progress */
public final class ProgressInfo {
	/** the number of unique foods eaten */
	public final int foodsEaten;
	
	ProgressInfo(FoodList foodList) {
		foodsEaten = (int) foodList.getEatenFoods().stream()
			.filter(food -> SOLWarlyConfig.shouldCount(food.item))
			.count();
	}
	
	public boolean hasReachedMax() {
		return foodsEaten >= SOLWarlyConfig.highestMilestone();
	}
	
	/** the next milestone to reach, or a negative value if the maximum has been reached */
	public int nextMilestone() {
		return hasReachedMax() ? -1 : SOLWarlyConfig.milestone(milestonesAchieved());
	}
	
	/** the number of foods remaining until the next milestone, or a negative value if the maximum has been reached */
	public int foodsUntilNextMilestone() {
		return nextMilestone() - foodsEaten;
	}
	
	/** the number of milestones achieved, doubling as the index of the next milestone */
	public int milestonesAchieved() {
		return (int) SOLWarlyConfig.getMilestones().stream()
			.filter(milestone -> foodsEaten >= milestone).count();
	}
}
