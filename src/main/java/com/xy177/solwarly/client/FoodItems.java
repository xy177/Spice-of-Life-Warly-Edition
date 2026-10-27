// Warly client food catalog, by xy177, 2026-10-27; LGPL-2.1.
package com.xy177.solwarly.client;
import com.xy177.solwarly.SOLWarlyConfig;
import net.minecraft.world.item.Item;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.api.distmarker.Dist;
import java.util.*;
@EventBusSubscriber(modid="solwarly",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class FoodItems {
    private static final List<Item> catalog=new ArrayList<>();
    @SubscribeEvent public static void ready(FMLLoadCompleteEvent event) {
        catalog.clear();
        for(Item item:net.minecraftforge.registries.ForgeRegistries.ITEMS.getValues())if(item.isEdible())catalog.add(item);
        catalog.sort(Comparator.comparing(item->I18n.get(item.getDescriptionId())));
    }
    public static List<Item> getAllFoodsIgnoringBlacklist() { return new ArrayList<>(catalog); }
    public static List<Item> getAllFoods() { return catalog.stream().filter(SOLWarlyConfig::isAllowed).toList(); }
    private FoodItems() {}
}
