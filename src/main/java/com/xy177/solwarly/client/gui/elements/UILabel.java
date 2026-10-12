// Modified by xy177 on 2026-10-10 for Spice of Life Warly Edition; upstream LGPL 2.1.
package com.xy177.solwarly.client.gui.elements;

import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class UILabel extends UIElement {
	public String text = "";
	public TextAlignment alignment = TextAlignment.CENTER;
	public Color color = Color.BLACK;
	private float textScale = 1;
	
	/** sets frame to text size */
	public UILabel(String text) {
		this(new Rectangle(fontRenderer.getStringWidth(text) - 1, 7), text);
	}

	/** Keep complete book labels inside the page, shrinking only text that exceeds its width. */
	public UILabel(String text, int maxWidth) {
		this(text);
		int width = fontRenderer.getStringWidth(text);
		if (width > maxWidth) {
			textScale = (float) maxWidth / width;
			frame.width = maxWidth;
		}
	}
	
	public UILabel(Rectangle frame, String text) {
		super(frame);
		this.text = text;
	}
	
	public UILabel(Rectangle frame) {
		super(frame);
	}
	
	@Override
	protected void render() {
		super.render();
		
		int textWidth = (int) Math.ceil((fontRenderer.getStringWidth(text) - 1) * textScale);
		int x = frame.x + (frame.width - textWidth) * alignment.ordinal / 2;
		int y = frame.y + (frame.height - 7) / 2;
		if (color.getTransparency() == Color.TRANSLUCENT) {
			GlStateManager.enableBlend();
		}
		if (textScale == 1) fontRenderer.drawString(text, x, y, color.getRGB());
		else {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, 0);
			GlStateManager.scale(textScale, textScale, 1);
			fontRenderer.drawString(text, 0, 0, color.getRGB());
			GlStateManager.popMatrix();
		}
	}
	
	enum TextAlignment {
		LEFT(0), CENTER(1), RIGHT(2);
		
		final int ordinal;
		
		TextAlignment(int ordinal) {
			this.ordinal = ordinal;
		}
	}
}
