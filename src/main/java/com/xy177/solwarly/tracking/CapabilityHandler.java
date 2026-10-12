// Modified by xy177 on 2026-10-10 for Spice of Life Warly Edition; upstream LGPL 2.1.
package com.xy177.solwarly.tracking;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.SOLWarlyConfig;
import com.cazsius.solcarrot.api.FoodCapability;
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
	private static final ResourceLocation FOOD = SOLWarly.resourceLocation("food");
	private static final ResourceLocation DIET = new ResourceLocation(SOLWarly.MOD_ID, "diet");
	
	@SubscribeEvent
	public static void preInit(SOLWarly.PreInitializationEvent e) {
		CapabilityManager.INSTANCE.register(FoodCapability.class, new FoodList.Storage(), FoodList::new);
		DietCapability.register();
	}
	
	@SubscribeEvent
	public static void attachPlayerCapability(AttachCapabilitiesEvent<Entity> event) {
		if (!(event.getObject() instanceof EntityPlayer)) return;
		
		event.addCapability(FOOD, new FoodList());
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
		FoodList original = FoodList.get(event.getOriginal());
		FoodList newInstance = FoodList.get(event.getEntityPlayer());
		newInstance.deserializeNBT(original.serializeNBT());
		DietData oldDiet = DietCapability.get(event.getOriginal());
		DietData newDiet = DietCapability.get(event.getEntityPlayer());
		if (oldDiet != null && newDiet != null) newDiet.deserializeNBT(oldDiet.serializeNBT());
		if (event.isWasDeath() && SOLWarlyConfig.shouldResetOnDeath) {
			newInstance.clearFood();
			if (newDiet != null) newDiet.reset(true);
		}
		syncFoodList(event.getEntityPlayer());
	}
	
	public static void syncFoodList(EntityPlayer player) {
		MaxHealthHandler.updateFoodHPModifier(player);
		FoodList foodList = FoodList.get(player);
		PacketHandler.channel.sendTo(new MessageFoodList(player), (EntityPlayerMP) player);
	}
	
	private CapabilityHandler() {}
}
