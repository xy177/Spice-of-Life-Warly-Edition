// Modified by xy177 on 2026-10-10 for Spice of Life Warly Edition; upstream LGPL 2.1.
package com.xy177.solwarly.tracking;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.SOLWarlyConfig;
import com.xy177.solwarly.diet.*;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

import javax.annotation.Nullable;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID)
public final class MaxHealthHandler {
	public static final UUID MILESTONE_HEALTH_MODIFIER_ID = UUID.fromString("b20d3436-0d39-4868-96ab-d0a4856e68c6");
	
	@SubscribeEvent
	public static void onPlayerLogin(PlayerLoggedInEvent event) {
		EntityPlayer player = event.player;
		
		updateFoodHPModifier(player);
	}
	
	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event) {
		if (DietCapability.get(event.getEntityPlayer()) != null) return;
		AttributeModifier prevModifier = getHealthModifier(event.getOriginal());
		if (prevModifier == null) return;
		
		updateHealthModifier(event.getEntityPlayer(), prevModifier);
	}
	
	/** @return whether or not the player reached a new milestone in this update */
	public static boolean updateFoodHPModifier(EntityPlayer player) {
		if (player.world.isRemote) return false;
		DietData data = DietCapability.get(player);
		if (data != null) {
			DietRuntime.initialize(player);
			float oldHealth = player.getHealth();
			AttributeModifier previous = getHealthModifier(player);
			double amount = 2 * (SOLWarlyConfig.baseHearts - 10) + Math.max(data.legacyHealthFloor, DietRewards.cumulativeHealth(data.cumulative()));
			boolean changed = previous == null || previous.getAmount() != amount;
			if (changed) updateHealthModifier(player, new AttributeModifier(MILESTONE_HEALTH_MODIFIER_ID, "Health Gained from Trying New Foods", amount, 0));
			DietRewards.updateAttributes(player);
			DietRewards.refreshEffects(player);
			if (oldHealth > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
			return changed;
		}
		
		AttributeModifier prevModifier = getHealthModifier(player);
		
		int healthPenalty = 2 * (SOLWarlyConfig.baseHearts - 10);
		
		ProgressInfo progressInfo = FoodList.get(player).getProgressInfo();
		int milestonesAchieved = progressInfo.milestonesAchieved();
		int addedHealthFromFood = milestonesAchieved * 2 * SOLWarlyConfig.heartsPerMilestone;
		
		double totalHealthModifier = healthPenalty + addedHealthFromFood;
		
		if (prevModifier == null || prevModifier.getAmount() != totalHealthModifier) {
			AttributeModifier modifier = new AttributeModifier(
				MILESTONE_HEALTH_MODIFIER_ID,
				"Health Gained from Trying New Foods",
				totalHealthModifier,
				0
			);
			
			float oldMax = player.getMaxHealth();
			updateHealthModifier(player, modifier);
			
			if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
			
			return true;
		} else {
			return false;
		}
	}
	
	@Nullable
	private static AttributeModifier getHealthModifier(EntityPlayer player) {
		return maxHealthAttribute(player).getModifier(MILESTONE_HEALTH_MODIFIER_ID);
	}
	
	private static void updateHealthModifier(EntityPlayer player, AttributeModifier modifier) {
		IAttributeInstance attribute = maxHealthAttribute(player);
		attribute.removeModifier(modifier);
		attribute.applyModifier(modifier);
	}
	
	private static IAttributeInstance maxHealthAttribute(EntityPlayer player) {
		return player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
	}
	
	private MaxHealthHandler() {}
}
