package com.xy177.solwarly.client.gui;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.lunch.ContainerLunch;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

public final class GuiLunch extends GuiContainer {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(SOLWarly.MOD_ID, "textures/gui/lunch_inventory.png");
    private static final ResourceLocation SLOT = new ResourceLocation(SOLWarly.MOD_ID, "textures/gui/lunch_slot.png");
    private final ContainerLunch lunch;
    private final EntityPlayer player;

    public GuiLunch(EntityPlayer player, int slot) {
        super(new ContainerLunch(player, slot));
        this.player = player;
        lunch = (ContainerLunch) inventorySlots;
    }
    @Override public void drawScreen(int x, int y, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(x, y, partialTicks);
        renderHoveredToolTip(x, y);
    }
    @Override protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(BACKGROUND);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        mc.getTextureManager().bindTexture(SLOT);
        for (int i = 0; i < lunch.slots; i++) {
            Slot slot = lunch.getSlot(i);
            drawModalRectWithCustomSizedTexture(guiLeft + slot.xPos - 1, guiTop + slot.yPos - 1, 0, 0, 18, 18, 18, 18);
        }
    }
    @Override protected void drawGuiContainerForegroundLayer(int x, int y) {
        fontRenderer.drawString(lunch.containerItem.getDisplayName(), 8, 6, 0x404040);
        fontRenderer.drawString(player.inventory.getDisplayName().getUnformattedText(), 8, 72, 0x404040);
    }
}
