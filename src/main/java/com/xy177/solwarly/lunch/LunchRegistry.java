package com.xy177.solwarly.lunch;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.*;

@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID)
public final class LunchRegistry {
    public static final ItemLunchContainer bag = new ItemLunchContainer("lunchbag", 5);
    public static final ItemLunchContainer box = new ItemLunchContainer("lunchbox", 9);
    public static final ItemLunchContainer goldenBox = new ItemLunchContainer("golden_lunchbox", 14);

    @SubscribeEvent public static void items(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(bag, box, goldenBox);
    }
    @SubscribeEvent @SideOnly(Side.CLIENT) public static void models(ModelRegistryEvent event) {
        for (Item item : new Item[] { bag, box, goldenBox })
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
    @SubscribeEvent public static void recipes(RegistryEvent.Register<IRecipe> event) {
        event.getRegistry().register(new LunchUpgradeRecipe(bag, Items.IRON_INGOT, box).setRegistryName(SOLWarly.MOD_ID, "lunchbox"));
        event.getRegistry().register(new LunchUpgradeRecipe(box, Items.GOLD_INGOT, goldenBox).setRegistryName(SOLWarly.MOD_ID, "golden_lunchbox"));
    }
    private LunchRegistry() {}
}
