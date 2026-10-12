// Modified by xy177 on 2026-10-10 for Spice of Life Warly Edition; upstream LGPL 2.1.
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
	public static final String LEGACY_ID = "solcarrot";
	
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	
	@Mod.Instance(MOD_ID)
	public static SOLWarly instance;

    public SOLWarly() {
        // Forge loads @Config before preInit: migrate the legacy file before that happens.
        ConfigFiles.prepare(net.minecraftforge.fml.common.Loader.instance().getConfigDir());
    }
	
	public static ResourceLocation resourceLocation(String path) {
		// Existing registry and resource names are part of the replacement contract.
		return new ResourceLocation(LEGACY_ID, path);
	}
	
	public static String namespaced(String path) {
		return LEGACY_ID + "." + path;
	}
	
	@EventHandler
	public static void onFingerprintViolation(FMLFingerprintViolationEvent event) {
		// This complains if jar not signed, even if certificateFingerprint is blank
		LOGGER.warn("Invalid Fingerprint!");
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
