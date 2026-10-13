package com.cazsius.solcarrot.api;

import com.xy177.solwarly.tracking.CapabilityHandler;
import com.xy177.solwarly.tracking.FoodList;
import net.minecraft.world.entity.player.Player;

/**
 Provides a stable API for interfacing with Spice of Life: Carrot Edition.
 */
public final class SOLCarrotAPI {
	public static final net.neoforged.neoforge.capabilities.EntityCapability<FoodCapability,Void> foodCapability = net.neoforged.neoforge.capabilities.EntityCapability.createVoid(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("solcarrot","food"),FoodCapability.class);
	
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
