// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/37c243c5bcf50d5582ce81596e85b3f465b222a8/src/main/java/com/cazsius/solcarrot/client/gui/elements/UILabel.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

public class UILabel extends UIElement {
	public String text = "";
	public TextAlignment alignment = TextAlignment.CENTER;
	public Color color = Color.BLACK;
    private float textScale=1;
	
	/** sets frame to text size */
	public UILabel(String text) {
		this(new Rectangle(font.width(text) - 1, 7), text);
	}
	
	public UILabel(String text,int maxWidth) {
        this(text);int width=font.width(text);
        if(width>maxWidth) { textScale=(float)maxWidth/width;frame.width=maxWidth; }
    }
    public UILabel(Rectangle frame, String text) {
		super(frame);
		this.text = text;
	}
	
	public UILabel(Rectangle frame) {
		super(frame);
	}
	
	@Override
	protected void render(GuiGraphics graphics) {
		super.render(graphics);
		
		int textWidth = (int)Math.ceil((font.width(text)-1)*textScale);
		int x = frame.x + (frame.width - textWidth) * alignment.ordinal / 2;
		int y = frame.y + (frame.height - 7) / 2;
		if (color.getTransparency() == Color.TRANSLUCENT) {
			RenderSystem.enableBlend();
		}
		graphics.pose().pushPose();graphics.pose().translate(x,y,0);graphics.pose().scale(textScale,textScale,1);
        graphics.drawString(font,text,0,0,color.getRGB(),false);graphics.pose().popPose();
	}
	
	enum TextAlignment {
		LEFT(0), CENTER(1), RIGHT(2);
		
		final int ordinal;
		
		TextAlignment(int ordinal) {
			this.ordinal = ordinal;
		}
	}
}
