// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/412fa94a07d93f09378fc0e3393de38eb0099824/src/main/java/com/cazsius/solcarrot/tracking/MaxHealthHandler.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.tracking;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.SOLWarlyConfig;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

@EventBusSubscriber(modid = SOLWarly.MOD_ID)
public final class MaxHealthHandler {
	public static final net.minecraft.resources.ResourceLocation MILESTONE_HEALTH_MODIFIER_ID = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("solcarrot","health_gained");
	
	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		updateFoodHPModifier(event.getEntity());
	}
	
	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event) {
		if(com.xy177.solwarly.diet.DietCapability.get(event.getEntity())!=null)return;
		var prevModifier = getHealthModifier(event.getOriginal());
		if (prevModifier == null) return;
		
		updateHealthModifier(event.getEntity(), prevModifier);
	}
	
	/** @return whether the player reached a new milestone in this update */
	public static boolean updateFoodHPModifier(Player player) {
		if(player.level().isClientSide)return false;
		com.xy177.solwarly.diet.DietRuntime.initialize(player);
		var diet=com.xy177.solwarly.diet.DietCapability.get(player);
		var prevModifier = getHealthModifier(player);
		
		int healthPenalty = 2 * (SOLWarlyConfig.getBaseHearts() - 10);
		
		double totalHealthModifier = healthPenalty + Math.max(diet.legacyHealthFloor,com.xy177.solwarly.diet.DietRewards.cumulativeHealth(diet.cumulative()));
		boolean hasChanged = prevModifier == null || prevModifier.amount() != totalHealthModifier;
		
		if (hasChanged) {
			AttributeModifier modifier = new AttributeModifier(
				MILESTONE_HEALTH_MODIFIER_ID,
				totalHealthModifier,
				AttributeModifier.Operation.ADD_VALUE
			);
			
			updateHealthModifier(player, modifier);
		}
		com.xy177.solwarly.diet.DietRewards.updateAttributes(player);
		com.xy177.solwarly.diet.DietRewards.refreshEffects(player);
		if(player.getHealth()>player.getMaxHealth())player.setHealth(player.getMaxHealth());
		
		return hasChanged;
	}
	
	@Nullable
	private static AttributeModifier getHealthModifier(Player player) {
		return maxHealthAttribute(player).getModifier(MILESTONE_HEALTH_MODIFIER_ID);
	}
	
	private static void updateHealthModifier(Player player, AttributeModifier modifier) {
		var attribute = maxHealthAttribute(player);
		attribute.removeModifier(modifier);
		attribute.addPermanentModifier(modifier);
		
	}
	
	private static AttributeInstance maxHealthAttribute(Player player) {
		return Objects.requireNonNull(player.getAttribute(Attributes.MAX_HEALTH));
	}
	
	private MaxHealthHandler() {}
}
