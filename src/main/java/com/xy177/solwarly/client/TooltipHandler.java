// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/37c243c5bcf50d5582ce81596e85b3f465b222a8/src/main/java/com/cazsius/solcarrot/client/TooltipHandler.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.SOLWarlyConfig;
import com.xy177.solwarly.tracking.FoodList;
import com.xy177.solwarly.diet.*;
import com.xy177.solwarly.api.FlavorAPI;
import com.xy177.solwarly.lib.DisplayNumbers;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.network.chat.Component;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = SOLWarly.MOD_ID)
public final class TooltipHandler {
    /** Shared presentation decision, independent of client rendering. */
    public static String historyKey(boolean eatenFood, boolean eligible, boolean eatenFlavor, double base, double[] boundaries) {
        if (!eatenFood) return "untasted";
        if (!eligible) return "unhelpful";
        if (!eatenFlavor) return "new_flavor";
        return "quality." + FoodQuality.tier(base, boundaries);
    }
    @SubscribeEvent public static void onItemTooltip(ItemTooltipEvent event) {
        if (!SOLWarlyConfig.isFoodTooltipEnabled() || event.getEntity() == null) return;
        ItemStack food = event.getItemStack();
        if (!food.isEdible()) return;
        Player player = event.getEntity();
        FoodList legacy = FoodList.get(player);
        DietData diet = DietCapability.get(player);
        String id = FoodCatalog.id(food);
        FlavorAPI.Flavor flavor = FlavorAPI.resolve(food);
        boolean eaten = legacy.hasEaten(food.getItem()) || diet != null && diet.hasTriedFood(id);
        boolean tried = diet != null && diet.hasTried(id, flavor.id)
            || flavor.id.equals("plain") && legacy.hasEaten(food.getItem()) && (diet == null || !diet.hasTriedFood(id));
        boolean eligible = SOLWarlyConfig.shouldCount(food.getItem());
        Double base = DietRuntime.clientCoefficients.get(id);
        String key = historyKey(eaten, eligible, tried, base == null ? 0 : base, DietRuntime.clientQualityBoundaries);
        event.getToolTip().add(Component.literal(I18n.get("solwarly.tooltip." + key)).withStyle(!eaten || !tried ? ChatFormatting.DARK_AQUA : eligible ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_GRAY));
        if (!eligible) return;
        int state = DietRuntime.clientAppetite.state(id);
        event.getToolTip().add(Component.translatable("solwarly.tooltip.interest", Component.translatable("solwarly.interest."+state)).withStyle(state>0?ChatFormatting.GOLD:state<0?ChatFormatting.GRAY:ChatFormatting.WHITE));
        if (base != null) {
            double interest = state > 0 ? DietRuntime.clientCravingMultiplier : state < 0 ? DietRuntime.clientBoringMultiplier : DietRuntime.clientNormalMultiplier;
            event.getToolTip().add(Component.translatable("solwarly.tooltip.contribution", DisplayNumbers.format(DietRuntime.contribution(player, food, base * interest * flavor.diversityMultiplier * DietRuntime.warlyMultiplier(player, food)))).withStyle(ChatFormatting.BLUE));
        }
    }
    private TooltipHandler() {}
}
