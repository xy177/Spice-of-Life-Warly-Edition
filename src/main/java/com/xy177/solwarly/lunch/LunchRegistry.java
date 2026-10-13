// Warly Edition lunch-container adaptation; modifications by xy177 on 2026-10-13.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/tree/f38436cb11c5b8d628e6f20f7b1670dbc811592d/src/main/java/com/tarinoita/solsweetpotato/item/foodcontainer
// Original Apple Pie/Sweet Potato contributors retain copyright.
// Upstream Cyclic attribution: copyright Samson Basset (Lothrazar), MIT.
// See THIRD_PARTY_NOTICES.md, licenses/Cyclic-MIT.txt, and LICENSE.txt.
package com.xy177.solwarly.lunch;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;

@Mod.EventBusSubscriber(modid=SOLWarly.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class LunchRegistry {
    private static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,SOLWarly.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(ForgeRegistries.MENU_TYPES,SOLWarly.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPES=DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS,SOLWarly.MOD_ID);
    public static final RegistryObject<net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<LunchUpgradeRecipe>> UPGRADE=RECIPES.register("lunch_upgrade",()->new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(LunchUpgradeRecipe::new));
    public static final RegistryObject<ItemLunchContainer> BAG=ITEMS.register("lunchbag",()->new ItemLunchContainer(5)),BOX=ITEMS.register("lunchbox",()->new ItemLunchContainer(9)),GOLD=ITEMS.register("golden_lunchbox",()->new ItemLunchContainer(14));
    public static final RegistryObject<MenuType<ContainerLunch>> MENU=MENUS.register("lunch",()->IForgeMenuType.create((id,inventory,buffer)->new ContainerLunch(id,inventory.player,buffer.readVarInt())));
    public static void setUp(IEventBus bus) { ITEMS.register(bus);MENUS.register(bus);RECIPES.register(bus); }
    @SubscribeEvent public static void creative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey()==CreativeModeTabs.TOOLS_AND_UTILITIES) { event.accept(BAG.get());event.accept(BOX.get());event.accept(GOLD.get()); }
    }
    private LunchRegistry() {}
}
