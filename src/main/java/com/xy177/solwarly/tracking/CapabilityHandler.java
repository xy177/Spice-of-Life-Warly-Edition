// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/412fa94a07d93f09378fc0e3393de38eb0099824/src/main/java/com/cazsius/solcarrot/tracking/CapabilityHandler.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.tracking;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.SOLWarlyConfig;
import com.cazsius.solcarrot.api.FoodCapability;
import com.xy177.solwarly.communication.FoodListMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;


import static net.neoforged.fml.common.EventBusSubscriber.Bus.MOD;

@EventBusSubscriber(modid = SOLWarly.MOD_ID)
public final class CapabilityHandler {
	private static final ResourceLocation FOOD = SOLWarly.resourceLocation("food");
	
	@EventBusSubscriber(modid = SOLWarly.MOD_ID, bus = MOD)
	private static final class Setup {
		@SubscribeEvent
		public static void registerCapabilities(RegisterCapabilitiesEvent event) {
			event.registerEntity(com.cazsius.solcarrot.api.SOLCarrotAPI.foodCapability,net.minecraft.world.entity.EntityType.PLAYER,(player,ctx)->player.getData(com.xy177.solwarly.diet.DietCapability.FOOD));
		}
	}
	
	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		// server needs to send any loaded data to the client
		syncFoodList(event.getEntity());
	}
	
	@SubscribeEvent
	public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
		syncFoodList(event.getEntity());
	}
	
	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event) {
		var originalPlayer = event.getOriginal();
		
		if (!event.isWasDeath() || !SOLWarlyConfig.shouldResetOnDeath())
			FoodList.get(event.getEntity()).deserializeNBT(FoodList.get(originalPlayer).serializeNBT());
		var oldDiet=com.xy177.solwarly.diet.DietCapability.get(originalPlayer);
		var newDiet=com.xy177.solwarly.diet.DietCapability.get(event.getEntity());
		if(oldDiet!=null&&newDiet!=null) {
			newDiet.deserializeNBT(oldDiet.serializeNBT());
			if(event.isWasDeath()&&SOLWarlyConfig.shouldResetOnDeath())newDiet.reset(true);
		}
		// can't sync yet; client hasn't attached capabilities yet
		
		
	}
	
	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		syncFoodList(event.getEntity());
	}
	
	public static void syncFoodList(Player player) {
		if (player.level().isClientSide) return;
		
		MaxHealthHandler.updateFoodHPModifier(player);
		var target = (ServerPlayer) player;
		target.connection.send(new FoodListMessage(player));
		
	}
}
