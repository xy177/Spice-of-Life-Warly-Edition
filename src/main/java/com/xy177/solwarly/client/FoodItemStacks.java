// Warly client food catalog, by xy177, 2026-10-27; LGPL-2.1.
package com.xy177.solwarly.client;
import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.diet.FoodRules;
import net.minecraft.item.*;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import java.util.*;
import java.util.stream.Collectors;
@Mod.EventBusSubscriber(modid=SOLWarly.MOD_ID,value=Side.CLIENT)
public final class FoodItemStacks {
    private static final List<ItemStack> catalog=new ArrayList<>();
    public static List<ItemStack> getAllFoodsIgnoringBlacklist() { return new ArrayList<>(catalog); }
    public static List<ItemStack> getAllFoods() { return catalog.stream().filter(FoodRules.current()::isAllowed).collect(Collectors.toList()); }
    @SubscribeEvent public static void ready(SOLWarly.PostInitializationEvent event) {
        catalog.clear();
        for(Item item:net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.getValuesCollection()) {
            NonNullList<ItemStack> variants=NonNullList.create();
            for(net.minecraft.creativetab.CreativeTabs tab:item.getCreativeTabs())if(tab!=null)item.getSubItems(tab,variants);
            for(ItemStack stack:variants)if(squeek.applecore.api.AppleCoreAPI.accessor.isFood(stack))catalog.add(stack);
        }
        catalog.sort(Comparator.comparing(ItemStack::getDisplayName).thenComparingInt(ItemStack::getMetadata));
    }
    public static void applyServerRules(FoodRules rules) { /* Queries use the latest synchronized rules. */ }
    private FoodItemStacks() {}
}
