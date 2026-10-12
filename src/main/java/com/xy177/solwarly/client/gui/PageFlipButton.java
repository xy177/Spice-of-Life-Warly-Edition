// Modified by xy177 on 2026-10-10 for Spice of Life Warly Edition; upstream LGPL 2.1.
package com.xy177.solwarly.client.gui;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
final class PageFlipButton extends GuiButton {
	private static final ResourceLocation texture = SOLWarly.resourceLocation("textures/gui/food_book.png");
	public static final int width = 23;
	public static final int height = 13;
	
	private final Direction direction;
	
	PageFlipButton(int buttonId, int x, int y, Direction direction) {
		super(buttonId, x, y, 23, 13, "");
		
		this.direction = direction;
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!visible) return;
		
		int textureX = 0;
		
		boolean isHovered = x <= mouseX && mouseX < x + width && y <= mouseY && mouseY < y + height;
		if (isHovered) {
			textureX += 23;
		}
		
		int textureY = direction == Direction.FORWARD ? 192 : 205;
		
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(texture);
		drawTexturedModalRect(x, y, textureX, textureY, 23, 13);
	}
	
	enum Direction {
		FORWARD, BACKWARD
	}
}
