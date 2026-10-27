// Adapted from Spice of Life: Apple Pie Edition (2d9de8b8d045b1f9b93940b681c74021d326f207); LGPL-2.1.
// Modified by xy177 on 2026-10-27 for Warly Edition.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/SOLSweetPotato.java
// Earlier shared ancestry and notices are recorded in THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly;

import com.xy177.solwarly.command.CommandFoodList;
import com.xy177.solwarly.diet.DietConfig;
import com.xy177.solwarly.diet.DietCapability;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
	modid = SOLWarly.MOD_ID,
	name = "Spice of Life Warly Edition",
	version = "1.0.0",
	acceptedMinecraftVersions = "[1.12.2]",
	dependencies = "required-after:applecore"
)
public final class SOLWarly {
	public static final String MOD_ID = "solwarly";

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Mod.Instance(MOD_ID)
	public static SOLWarly instance;

    public SOLWarly() {
        // Forge loads @Config before preInit, so prepare the configuration directory here.
        ConfigFiles.prepare(net.minecraftforge.fml.common.Loader.instance().getConfigDir());
    }

	public static ResourceLocation resourceLocation(String path) {
		// Warly owns its items, packets and resources.
		return new ResourceLocation(MOD_ID, path);
	}

	public static String namespaced(String path) {
		return MOD_ID + "." + path;
	}


	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
        java.io.File directory = ConfigFiles.prepare(e.getModConfigurationDirectory());
		DietConfig.load(directory);
        com.xy177.solwarly.spice.GrinderConfig.load(directory);
        com.xy177.solwarly.spice.SpiceConfig.load(directory);
		MinecraftForge.EVENT_BUS.post(new PreInitializationEvent());
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {
		MinecraftForge.EVENT_BUS.post(new InitializationEvent());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {
		MinecraftForge.EVENT_BUS.post(new PostInitializationEvent());
		com.xy177.solwarly.diet.FoodCatalog.build();
	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		if (!com.xy177.solwarly.diet.FoodCatalog.isReady()) com.xy177.solwarly.diet.FoodCatalog.build();
		event.registerServerCommand(new CommandFoodList());
		event.registerServerCommand(new com.xy177.solwarly.command.CommandWarly());
	}
	@EventHandler
	public void serverStopped(FMLServerStoppedEvent event) {
		com.xy177.solwarly.diet.FoodCatalog.invalidate();
	}

	public static final class PreInitializationEvent extends Event {}

	public static final class InitializationEvent extends Event {}

	public static final class PostInitializationEvent extends Event {}
}
