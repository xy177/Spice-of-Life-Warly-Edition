// Adapted from Spice of Life: Apple Pie Edition (2d9de8b8d045b1f9b93940b681c74021d326f207); LGPL-2.1.
// Modified by xy177 on 2026-10-27 for Warly Edition.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/item/SOLSweetPotatoItems.java
// Earlier shared ancestry and notices are recorded in THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.item;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID)
public final class SOLWarlyItems {
	private static final List<Item> ITEMS = new ArrayList<>(); // has to be on top so it's loaded first

	public static final ItemFoodBook foodBook = item(new ItemFoodBook(), "food_book");

	private static <I extends Item> I item(I item, String name) {
		item.setRegistryName(SOLWarly.resourceLocation(name));
		item.setTranslationKey(SOLWarly.namespaced(name));
		ITEMS.add(item);
		return item;
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();
		ITEMS.forEach(registry::register);
	}

	@SubscribeEvent
	@SideOnly(CLIENT)
	public static void registerModels(@SuppressWarnings("unused") ModelRegistryEvent event) {
		ITEMS.forEach(item -> {
			ResourceLocation registryName = item.getRegistryName();
			assert registryName != null;
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(registryName, "inventory"));
		});
	}

	private SOLWarlyItems() {}
}
