// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/37c243c5bcf50d5582ce81596e85b3f465b222a8/src/main/java/com/cazsius/solcarrot/api/SOLCarrotAPI.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.cazsius.solcarrot.api;

import com.xy177.solwarly.tracking.CapabilityHandler;
import com.xy177.solwarly.tracking.FoodList;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

/**
 Provides a stable API for interfacing with Spice of Life: Carrot Edition.
 */
public final class SOLCarrotAPI {
	public static Capability<FoodCapability> foodCapability = CapabilityManager.get(new CapabilityToken<>() {});
	
	private SOLCarrotAPI() {}
	
	/**
	 Retrieves the {@link com.cazsius.solcarrot.api.FoodCapability} for the given player.
	 */
	public static FoodCapability getFoodCapability(Player player) {
		return FoodList.get(player);
	}
	
	/**
	 Synchronizes the food list for the given player to the client, updating their max health in the process.
	 */
	public static void syncFoodList(Player player) {
		CapabilityHandler.syncFoodList(player);
	}
}
