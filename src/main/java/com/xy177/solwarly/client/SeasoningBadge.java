package com.xy177.solwarly.client;

import com.xy177.solwarly.spice.*;
import com.xy177.solwarly.registry.RegistryLookup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/** GUI-only badge; the original food renderer and its tint are kept intact. */
public final class SeasoningBadge {
    public static void render(GuiGraphics graphics,ItemStack food,int x,int y) {
        if(!Seasoning.seasoned(food))return;
        var kinds=Seasoning.components(food);if(kinds.isEmpty())return;
        var badges = Seasoning.badges(food);
        for (int i = 0; i < badges.size(); i++) {
            ItemStack badge = badges.get(i);
            if (badge.isEmpty()) continue;
            if (badge.getItem() == SpiceRegistry.spice) badge.getOrCreateTag().putInt(ItemSpice.COLOR_KEY, Seasoning.color(food));
            graphics.pose().pushPose(); graphics.pose().translate(x + Math.min(i,1) * 8, y + (i == 2 ? 0 : 8), 1); graphics.pose().scale(.5F, .5F, 1);
            graphics.renderItem(badge, 0, 0); graphics.pose().popPose();
        }
    }

    private SeasoningBadge() {}
}
