package com.xy177.solwarly.item;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import java.util.function.Supplier;

import static net.neoforged.fml.common.EventBusSubscriber.Bus.MOD;

@EventBusSubscriber(modid = SOLWarly.MOD_ID, bus = MOD)
public final class SOLWarlyItems {
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(net.minecraft.core.registries.Registries.ITEM, "solcarrot");
	
	public static final Supplier<Item> FOOD_BOOK = ITEMS.register("food_book", FoodBookItem::new);
	
	public static void setUp(IEventBus eventBus) {
		
		ITEMS.register(eventBus);
	}
	
	@SubscribeEvent
	public static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			event.accept(FOOD_BOOK.get());
		}
	}
}
