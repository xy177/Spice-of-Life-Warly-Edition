// Warly Edition lunch-container adaptation; modifications by xy177 on 2026-10-13.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/tree/f38436cb11c5b8d628e6f20f7b1670dbc811592d/src/main/java/com/tarinoita/solsweetpotato/item/foodcontainer
// Original Apple Pie/Sweet Potato contributors retain copyright.
// Upstream Cyclic attribution: copyright Samson Basset (Lothrazar), MIT.
// See THIRD_PARTY_NOTICES.md, licenses/Cyclic-MIT.txt, and LICENSE.txt.
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
