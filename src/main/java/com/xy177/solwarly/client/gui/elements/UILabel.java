// Adapted from Spice of Life: Apple Pie Edition; upstream contributors retain copyright.
// Ported and modified by xy177 on 2026-10-27; LGPL-2.1.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/client/gui/elements/UILabel.java
// Warly rules and legacy save/API compatibility are retained; see THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

public class UILabel extends UIElement {
	public String text = "";
	public TextAlignment alignment = TextAlignment.CENTER;
	public Color color = Color.BLACK;
	private float textScale = 1;

	/** sets frame to text size */
	public UILabel(String text) {
		this(new Rectangle(font.width(text), 7), text);
	}

	public UILabel(String text, int maxWidth) {
		this(text);
		int width = font.width(text);
		if (width > maxWidth) { textScale = (float) maxWidth / width; frame.width = maxWidth; }
	}

	public UILabel(Rectangle frame, String text) {
		super(frame);
		this.text = text;
	}

	public UILabel(Rectangle frame) {
		super(frame);
	}

	@Override
	protected void render(GuiGraphics matrices) {
		super.render(matrices);

		int textWidth = (int) Math.ceil((font.width(text)) * textScale);
		int x = frame.x + (frame.width - textWidth) * alignment.ordinal / 2;
		int y = frame.y + (frame.height - (int) Math.ceil(7 * textScale)) / 2;
		if (color.getTransparency() == Color.TRANSLUCENT) {
			RenderSystem.enableBlend();
		}

		matrices.pose().pushPose();
		matrices.pose().translate(x, y, 0);
		matrices.pose().scale(textScale, textScale, 1);
		matrices.drawString(font, text, 0, 0, color.getRGB(), false);
		matrices.pose().popPose();
	}

	enum TextAlignment {
		LEFT(0), CENTER(1), RIGHT(2);

		final int ordinal;

		TextAlignment(int ordinal) {
			this.ordinal = ordinal;
		}
	}
}
