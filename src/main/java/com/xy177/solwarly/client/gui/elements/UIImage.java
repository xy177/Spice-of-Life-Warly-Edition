// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/37c243c5bcf50d5582ce81596e85b3f465b222a8/src/main/java/com/cazsius/solcarrot/client/gui/elements/UIImage.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client.gui.elements;

import net.minecraft.client.gui.GuiGraphics;

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
	protected void render(GuiGraphics graphics) {
		super.render(graphics);
		
		int imageWidth = data.partOfTexture.width;
		int imageHeight = data.partOfTexture.height;
		
		graphics.setColor(1,1,1,alpha);
        graphics.blit(
			data.textureLocation,
			frame.x + (int) Math.floor((frame.width - imageWidth) / 2d),
			frame.y + (int) Math.floor((frame.height - imageHeight) / 2d),
			0,
			data.partOfTexture.x, data.partOfTexture.y,
			data.partOfTexture.width, data.partOfTexture.height,
			256, 256
		);
        graphics.setColor(1,1,1,1);
	}
}
