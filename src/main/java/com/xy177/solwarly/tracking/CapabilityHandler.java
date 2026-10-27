// Adapted from Spice of Life: Apple Pie Edition (2d9de8b8d045b1f9b93940b681c74021d326f207); LGPL-2.1.
// Modified by xy177 on 2026-10-27 for Warly Edition.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/tracking/CapabilityHandler.java
package com.xy177.solwarly.tracking;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.SOLWarlyConfig;
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

		var oldDiet=com.xy177.solwarly.diet.DietCapability.get(originalPlayer);
		var newDiet=com.xy177.solwarly.diet.DietCapability.get(event.getEntity());
		if(oldDiet!=null&&newDiet!=null) {
			newDiet.deserializeNBT(oldDiet.serializeNBT());
			if(event.isWasDeath()&&SOLWarlyConfig.shouldResetOnDeath())newDiet.reset();
		}
		// can't sync yet; client hasn't attached capabilities yet


	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		syncFoodList(event.getEntity());
	}

	public static void syncFoodList(Player player) {
		if (player.level().isClientSide) return;

		com.xy177.solwarly.diet.DietRewards.update(player);
		var target = (ServerPlayer) player;
		target.connection.send(new FoodListMessage(player));

	}
}
