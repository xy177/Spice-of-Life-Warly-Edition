// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/3fbe133834091bbbc73ba0f37cf582d2bb66b10d/src/main/java/com/cazsius/solcarrot/client/TooltipHandler.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.SOLWarlyConfig;
import com.xy177.solwarly.tracking.FoodList;
import com.xy177.solwarly.diet.*;
import com.xy177.solwarly.api.FlavorAPI;
import com.xy177.solwarly.lib.DisplayNumbers;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import squeek.applecore.api.AppleCoreAPI;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = SOLWarly.MOD_ID)
public final class TooltipHandler {
    /** Shared presentation decision, independent of client rendering. */
    public static String historyKey(boolean eatenFood, boolean eligible, boolean eatenFlavor, double base, double[] boundaries) {
        if (!eatenFood) return "untasted";
        if (!eligible) return "unhelpful";
        if (!eatenFlavor) return "new_flavor";
        return "quality." + FoodQuality.tier(base, boundaries);
    }
    @SubscribeEvent public static void onItemTooltip(ItemTooltipEvent event) {
        if (!SOLWarlyConfig.isFoodTooltipEnabled || event.getEntityPlayer() == null) return;
        ItemStack food = event.getItemStack();
        if (!AppleCoreAPI.accessor.isFood(food)) return;
        EntityPlayer player = event.getEntityPlayer();
        FoodList legacy = FoodList.get(player);
        DietData diet = DietCapability.get(player);
        String id = FoodCatalog.id(food);
        FlavorAPI.Flavor flavor = FlavorAPI.resolve(food);
        boolean eaten = legacy.hasEaten(food) || diet != null && diet.hasTriedFood(id);
        boolean tried = diet != null && diet.hasTried(id, flavor.id)
            || flavor.id.equals("plain") && legacy.hasEaten(food) && (diet == null || !diet.hasTriedFood(id));
        boolean eligible = legacy.getConfigInfo().shouldCount(food);
        Double base = DietRuntime.clientCoefficients.get(id);
        String key = historyKey(eaten, eligible, tried, base == null ? 0 : base, DietRuntime.clientQualityBoundaries);
        event.getToolTip().add((!eaten || !tried ? TextFormatting.DARK_AQUA : eligible ? TextFormatting.DARK_GREEN : TextFormatting.DARK_GRAY)
            + I18n.format("solwarly.tooltip." + key));
        if (!eligible) return;
        int state = DietRuntime.clientAppetite.state(id);
        event.getToolTip().add((state > 0 ? TextFormatting.GOLD : state < 0 ? TextFormatting.GRAY : TextFormatting.WHITE)
            + I18n.format("solwarly.tooltip.interest", I18n.format("solwarly.interest." + state)));
        if (base != null) {
            double interest = state > 0 ? DietRuntime.clientCravingMultiplier : state < 0 ? DietRuntime.clientBoringMultiplier : DietRuntime.clientNormalMultiplier;
            event.getToolTip().add(TextFormatting.BLUE + I18n.format("solwarly.tooltip.contribution",
                DisplayNumbers.format(DietRuntime.contribution(player, food, base * interest * flavor.diversityMultiplier * DietRuntime.warlyMultiplier(player, food)))));
        }
    }
    private TooltipHandler() {}
}
