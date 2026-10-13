// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/37c243c5bcf50d5582ce81596e85b3f465b222a8/src/main/java/com/cazsius/solcarrot/client/FoodItems.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.SOLWarlyConfig;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = SOLWarly.MOD_ID, bus = MOD)
public final class FoodItems {
	private static List<Item> foodsBeforeBlacklist;
	private static List<Item> foods;
	private static boolean isConfigLoaded = false;
	
	/** @return a list of all item stacks that can be eaten, including blacklisted/hidden ones */
	public static List<Item> getAllFoodsIgnoringBlacklist() {
		return new ArrayList<>(foodsBeforeBlacklist);
	}
	
	/** @return a list of all item stacks that can be eaten */
	public static List<Item> getAllFoods() {
		return getAllFoodsIgnoringBlacklist().stream().filter(SOLWarlyConfig::isAllowed).toList();
	}
	
	@SubscribeEvent
	public static void setUp(FMLLoadCompleteEvent event) {
		foodsBeforeBlacklist = ForgeRegistries.ITEMS.getValues().stream()
			.filter(Item::isEdible)
			// sort by name
			.sorted(Comparator.comparing(food -> I18n.get(food.getDescriptionId())))
			.collect(Collectors.toList());
		
		// depending on the other mods involved, config might be loaded before or after this event
		tryApplyBlacklist();
	}
	
	@SubscribeEvent
	public static void onConfigUpdate(ModConfigEvent event) {
		if (event.getConfig().getType() == ModConfig.Type.CLIENT) return;
		
		isConfigLoaded = true;
		
		tryApplyBlacklist();
	}
	
	private static void tryApplyBlacklist() {
		if (foodsBeforeBlacklist == null) return;
		if (!isConfigLoaded) return;
		
		foods = foodsBeforeBlacklist.stream()
			.filter(SOLWarlyConfig::isAllowed)
			.collect(Collectors.toList());
	}
}
