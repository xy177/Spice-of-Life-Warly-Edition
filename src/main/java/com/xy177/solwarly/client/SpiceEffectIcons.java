package com.xy177.solwarly.client;

import com.xy177.solwarly.spice.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

public final class SpiceEffectIcons implements IClientMobEffectExtensions {
    private final PotionSpice effect;
    public SpiceEffectIcons(PotionSpice effect) { this.effect=effect; }
    private void render(GuiGraphics graphics,int x,int y) { graphics.renderItem(Seasoning.powder(java.util.List.of(effect.kind),1),x,y); }
    @Override public boolean renderInventoryIcon(MobEffectInstance effect,EffectRenderingInventoryScreen<?> screen,GuiGraphics graphics,int x,int y,int z) {
        render(graphics,x+6,y+7);return true;
    }
    @Override public boolean renderGuiIcon(MobEffectInstance effect,Gui gui,GuiGraphics graphics,int x,int y,float z,float alpha) {
        render(graphics,x+3,y+3);return true;
    }
}
