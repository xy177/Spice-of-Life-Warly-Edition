package com.xy177.solwarly.lunch;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.world.item.*;
import java.util.function.Supplier;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.*;

@EventBusSubscriber(modid=SOLWarly.MOD_ID,bus=EventBusSubscriber.Bus.MOD)
public final class LunchRegistry {
    private static final DeferredRegister<Item> ITEMS=DeferredRegister.create(net.minecraft.core.registries.Registries.ITEM,SOLWarly.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(net.minecraft.core.registries.Registries.MENU,SOLWarly.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPES=DeferredRegister.create(net.minecraft.core.registries.Registries.RECIPE_SERIALIZER,SOLWarly.MOD_ID);
    public static final Supplier<net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<LunchUpgradeRecipe>> UPGRADE=RECIPES.register("lunch_upgrade",()->new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(LunchUpgradeRecipe::new));
    public static final Supplier<ItemLunchContainer> BAG=ITEMS.register("lunchbag",()->new ItemLunchContainer(5)),BOX=ITEMS.register("lunchbox",()->new ItemLunchContainer(9)),GOLD=ITEMS.register("golden_lunchbox",()->new ItemLunchContainer(14));
    public static final Supplier<MenuType<ContainerLunch>> MENU=MENUS.register("lunch",()->IMenuTypeExtension.create((id,inventory,buffer)->new ContainerLunch(id,inventory.player,buffer.readVarInt())));
    public static void setUp(IEventBus bus) { ITEMS.register(bus);MENUS.register(bus);RECIPES.register(bus); }
    @SubscribeEvent public static void creative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey()==CreativeModeTabs.TOOLS_AND_UTILITIES) { event.accept(BAG.get());event.accept(BOX.get());event.accept(GOLD.get()); }
    }
    private LunchRegistry() {}
}
