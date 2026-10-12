// Modified by xy177 on 2026-10-10 for Spice of Life Warly Edition; upstream LGPL 2.1.
package com.xy177.solwarly.tracking;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.SOLWarlyConfig;
import com.xy177.solwarly.diet.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.*;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import squeek.applecore.api.food.FoodEvent;

import static com.xy177.solwarly.lib.Localization.localizedComponent;
import static com.xy177.solwarly.lib.Localization.localizedQuantityComponent;

@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID)
public final class FoodTracker {
	@SubscribeEvent
	public static void onFoodEaten(FoodEvent.FoodEaten event) {
		if (event.player.world.isRemote) return;
		WorldServer world = (WorldServer) event.player.world;
		
		EntityPlayer player = event.player;
		
		FoodList foodList = FoodList.get(player);
		DietRuntime.initialize(player);
		double before = DietRewards.cumulativeHealth(DietCapability.get(player).cumulative());
		boolean hasTriedNewFood = foodList.addFood(event.food);
		DietRuntime.eat(player, event.food);
		
		// check this before syncing, because the sync entails an hp update
		MaxHealthHandler.updateFoodHPModifier(player);
		boolean newMilestoneReached = DietRewards.cumulativeHealth(DietCapability.get(player).cumulative()) > before;
		
		CapabilityHandler.syncFoodList(player);
        boolean finished = DietRewards.reachedFinalCumulativeTier(DietCapability.get(player).cumulative());
		
		if (newMilestoneReached) {
			if (SOLWarlyConfig.shouldPlayMilestoneSounds) {
				// passing the player makes it not play for some reason
				world.playSound(
					null,
					player.posX, player.posY, player.posZ,
					SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS,
					1.0F, 1.0F
				);
			}
			
			if (SOLWarlyConfig.shouldSpawnMilestoneParticles) {
				spawnParticles(world, player, EnumParticleTypes.HEART, 12);
				
                if (finished) {
					spawnParticles(world, player, EnumParticleTypes.VILLAGER_HAPPY, 16);
				}
			}
			
			ITextComponent heartsDescription = localizedQuantityComponent("message", "hearts", (int) ((DietRewards.cumulativeHealth(DietCapability.get(player).cumulative()) - before) / 2));
			
			if (SOLWarlyConfig.shouldShowProgressAboveHotbar) {
                String messageKey = finished ? "finished.hotbar" : "milestone_achieved";
				player.sendStatusMessage(localizedComponent("message", messageKey, heartsDescription), true);
			} else {
				showChatMessage(player, TextFormatting.DARK_AQUA, localizedComponent("message", "milestone_achieved", heartsDescription));
                if (finished) {
					showChatMessage(player, TextFormatting.GOLD, localizedComponent("message", "finished.chat"));
				}
			}
		} else if (hasTriedNewFood) {
			if (SOLWarlyConfig.shouldSpawnIntermediateParticles) {
				spawnParticles(world, player, EnumParticleTypes.END_ROD, 12);
			}
		}
	}
	
	private static void spawnParticles(WorldServer world, EntityPlayer player, EnumParticleTypes type, int count) {
		// this overload sends a packet to the client
		world.spawnParticle(
			type,
			player.posX, player.posY + player.getEyeHeight(), player.posZ,
			count,
			0.5F, 0.5F, 0.5F,
			0.0F
		);
	}
	
	private static void showChatMessage(EntityPlayer player, TextFormatting color, ITextComponent message) {
		ITextComponent component = localizedComponent("message", "chat_wrapper", message);
		component.setStyle(new Style().setColor(color));
		player.sendStatusMessage(component, false);
	}
	
	private FoodTracker() {}
}
