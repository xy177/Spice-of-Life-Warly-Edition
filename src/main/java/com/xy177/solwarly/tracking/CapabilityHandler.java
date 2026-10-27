// Adapted from Spice of Life: Apple Pie Edition (2d9de8b8d045b1f9b93940b681c74021d326f207); LGPL-2.1.
// Modified by xy177 on 2026-10-27 for Warly Edition.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/tracking/CapabilityHandler.java
package com.xy177.solwarly.tracking;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.SOLWarlyConfig;
import com.xy177.solwarly.communication.MessageFoodList;
import com.xy177.solwarly.communication.PacketHandler;
import com.xy177.solwarly.diet.DietCapability;
import com.xy177.solwarly.diet.DietData;
import com.xy177.solwarly.diet.DietRuntime;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID)
public final class CapabilityHandler {
	private static final ResourceLocation DIET = new ResourceLocation(SOLWarly.MOD_ID, "diet");

	@SubscribeEvent
	public static void preInit(SOLWarly.PreInitializationEvent e) {
		DietCapability.register();
	}

	@SubscribeEvent
	public static void attachPlayerCapability(AttachCapabilitiesEvent<Entity> event) {
		if (!(event.getObject() instanceof EntityPlayer)) return;

		event.addCapability(DIET, new DietCapability());
	}

	@SubscribeEvent
	public static void onPlayerLogin(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote) return;
		if (!(event.getEntity() instanceof EntityPlayer)) return;

		// server needs to send any loaded data to the client
		syncFoodList((EntityPlayer) event.getEntity());
	}

	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event) {
		DietData oldDiet = DietCapability.get(event.getOriginal());
		DietData newDiet = DietCapability.get(event.getEntityPlayer());
		if (oldDiet != null && newDiet != null) newDiet.deserializeNBT(oldDiet.serializeNBT());
		if (event.isWasDeath() && SOLWarlyConfig.shouldResetOnDeath) {
			if (newDiet != null) newDiet.reset();
		}
		syncFoodList(event.getEntityPlayer());
	}

	public static void syncFoodList(EntityPlayer player) {
        if(player.world.isRemote || !(player instanceof EntityPlayerMP))return;
		com.xy177.solwarly.diet.DietRewards.update(player);
		PacketHandler.channel.sendTo(new MessageFoodList(player), (EntityPlayerMP) player);
	}

	private CapabilityHandler() {}
}
