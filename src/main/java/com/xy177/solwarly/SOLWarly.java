// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/37c243c5bcf50d5582ce81596e85b3f465b222a8/src/main/java/com/cazsius/solcarrot/SOLCarrot.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly;

import com.xy177.solwarly.communication.FoodListMessage;
import com.xy177.solwarly.item.SOLWarlyItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

@Mod(SOLWarly.MOD_ID)
@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID, bus = MOD)
public final class SOLWarly {
	public static final String MOD_ID = "solwarly";
	
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	
	private static final String PROTOCOL_VERSION = "1.0";
	public static SimpleChannel channel;
	
	public static ResourceLocation resourceLocation(String path) {
		return new ResourceLocation("solcarrot", path);
	}
	
	@SubscribeEvent
	public static void setUp(FMLCommonSetupEvent event) {
        channel = NetworkRegistry.ChannelBuilder
		.named(resourceLocation("main"))
		.clientAcceptedVersions(PROTOCOL_VERSION::equals)
		.serverAcceptedVersions(PROTOCOL_VERSION::equals)
		.networkProtocolVersion(() -> PROTOCOL_VERSION)
		.simpleChannel();
		channel.messageBuilder(FoodListMessage.class, 0)
			.encoder(FoodListMessage::write)
			.decoder(FoodListMessage::new)
			.consumerMainThread(FoodListMessage::handle)
			.add();
	}
	
	public SOLWarly() {
		java.io.File directory=net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve("solwarly").toFile();
		directory.mkdirs();
		SOLWarlyConfig.setUp();
		SOLWarlyItems.setUp();
		var bus=net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
		com.xy177.solwarly.spice.SpiceRegistry.setUp(bus);
		com.xy177.solwarly.lunch.LunchRegistry.setUp(bus);
		bus.addListener(this::complete);
	}
	private void complete(net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent event) {
		event.enqueueWork(()-> {
			java.io.File directory=net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve("solwarly").toFile();
			com.xy177.solwarly.diet.DietConfig.load(directory);
			com.xy177.solwarly.spice.SpiceConfig.load(directory);
			com.xy177.solwarly.spice.GrinderConfig.load(directory);
			com.xy177.solwarly.spice.SeasoningPotions.loadFruits();
			com.xy177.solwarly.spice.BuiltinSpiceRecipes.reload();
			com.xy177.solwarly.spice.CustomSpices.load(directory);
			var seasoningId=new ResourceLocation("solwarly","seasoning");
			com.xy177.solwarly.api.FlavorAPI.register(seasoningId,com.xy177.solwarly.spice.Seasoning::flavor);
			com.xy177.solwarly.api.FlavorAPI.registerVariants(seasoningId,com.xy177.solwarly.spice.Seasoning::variants);
			com.xy177.solwarly.diet.FoodCatalog.build();
		});
	}
}
