// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/412fa94a07d93f09378fc0e3393de38eb0099824/src/main/java/com/cazsius/solcarrot/client/gui/elements/ImageData.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client.gui.elements;

import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public final class ImageData {
	public final ResourceLocation textureLocation;
	public final Rectangle partOfTexture;
	public int visualWidth;
	public int visualHeight;
	
	public ImageData(ResourceLocation textureLocation, Rectangle partOfTexture) {
		this(textureLocation, partOfTexture, partOfTexture.width, partOfTexture.height);
	}
	
	public ImageData(ResourceLocation textureLocation, Rectangle partOfTexture, int visualWidth, int visualHeight) {
		this.textureLocation = textureLocation;
		this.partOfTexture = partOfTexture;
		this.visualWidth = visualWidth;
		this.visualHeight = visualHeight;
	}
}
