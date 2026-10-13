package com.xy177.solwarly.client;

import com.xy177.solwarly.lunch.ContainerLunch;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class LunchScreen extends AbstractContainerScreen<ContainerLunch> {
    public LunchScreen(ContainerLunch menu,Inventory inventory,Component title) { super(menu,inventory,title); }
    @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY) {
        graphics.blit(new ResourceLocation("solwarly","textures/gui/lunch_inventory.png"),leftPos,topPos,0,0,imageWidth,imageHeight);
        for(int i=0;i<menu.size;i++) {
            var slot=menu.getSlot(i);
            graphics.blit(new ResourceLocation("solwarly","textures/gui/lunch_slot.png"),leftPos+slot.x-1,topPos+slot.y-1,0,0,18,18,18,18);
        }
    }
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial) {
        renderBackground(graphics);super.render(graphics,mouseX,mouseY,partial);renderTooltip(graphics,mouseX,mouseY);
    }
}
