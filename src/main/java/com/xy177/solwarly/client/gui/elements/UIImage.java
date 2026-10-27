// Adapted from Spice of Life: Apple Pie Edition; upstream contributors retain copyright.
// Ported and modified by xy177 on 2026-10-27; LGPL-2.1.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/client/gui/elements/UIImage.java
// Warly rendering and compatibility adapters retained; see THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client.gui.elements;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public class UIImage extends UIElement {
	public ImageData data;
	public float alpha = 1;

	public UIImage(ImageData data) {
		this(new Rectangle(data.visualWidth, data.visualHeight), data);
	}

	public UIImage(Rectangle frame, ImageData data) {
		super(frame);

		this.data = data;
	}

	@Override
	protected void render(GuiGraphics matrices) {
		super.render(matrices);

		int imageWidth = data.partOfTexture.width;
		int imageHeight = data.partOfTexture.height;

		matrices.setColor(1, 1, 1, alpha);

		matrices.blit(
			data.textureLocation,
			frame.x + (int) Math.floor((frame.width - imageWidth) / 2d),
			frame.y + (int) Math.floor((frame.height - imageHeight) / 2d),
			0,
			data.partOfTexture.x, data.partOfTexture.y,
			imageWidth, imageHeight,
			256, 256
		);
		matrices.setColor(1, 1, 1, 1);
	}

}
