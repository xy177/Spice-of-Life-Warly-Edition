// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/412fa94a07d93f09378fc0e3393de38eb0099824/src/main/java/com/cazsius/solcarrot/client/FoodItems.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.SOLWarlyConfig;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static net.neoforged.fml.common.EventBusSubscriber.Bus.MOD;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = SOLWarly.MOD_ID, bus = MOD)
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
		foodsBeforeBlacklist = net.minecraft.core.registries.BuiltInRegistries.ITEM.stream()
			.filter(item->item.getDefaultInstance().getFoodProperties(null)!=null)
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
