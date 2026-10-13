package com.xy177.solwarly;

import com.xy177.solwarly.communication.FoodListMessage;
import com.xy177.solwarly.item.SOLWarlyItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.Mod;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(SOLWarly.MOD_ID)
public final class SOLWarly {
	public static final String MOD_ID = "solwarly";
	
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	
	public static ResourceLocation resourceLocation(String path) {
		return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("solcarrot",path);
	}
	
	public SOLWarly(net.neoforged.bus.api.IEventBus bus,net.neoforged.fml.ModContainer container) {
		java.io.File directory=net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("solwarly").toFile();
		directory.mkdirs();
		SOLWarlyConfig.setUp(container);
		SOLWarlyItems.setUp(bus);
        com.xy177.solwarly.diet.DietCapability.setUp(bus);
		
		com.xy177.solwarly.spice.SpiceRegistry.setUp(bus);
		com.xy177.solwarly.lunch.LunchRegistry.setUp(bus);
        bus.addListener(com.xy177.solwarly.communication.FoodListMessage::register);
		bus.addListener(this::complete);
	}
	private void complete(net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent event) {
		event.enqueueWork(()-> {
			java.io.File directory=net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("solwarly").toFile();
			com.xy177.solwarly.diet.DietConfig.load(directory);
			com.xy177.solwarly.spice.SpiceConfig.load(directory);
			com.xy177.solwarly.spice.GrinderConfig.load(directory);
			com.xy177.solwarly.spice.SeasoningPotions.loadFruits();
			com.xy177.solwarly.spice.BuiltinSpiceRecipes.reload();
			com.xy177.solwarly.spice.CustomSpices.load(directory);
			var seasoningId=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("solwarly","seasoning");
			com.xy177.solwarly.api.FlavorAPI.register(seasoningId,com.xy177.solwarly.spice.Seasoning::flavor);
			com.xy177.solwarly.api.FlavorAPI.registerVariants(seasoningId,com.xy177.solwarly.spice.Seasoning::variants);
			com.xy177.solwarly.diet.FoodCatalog.build();
		});
	}
}
