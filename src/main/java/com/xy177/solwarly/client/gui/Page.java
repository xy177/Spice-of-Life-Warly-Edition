// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/37c243c5bcf50d5582ce81596e85b3f465b222a8/src/main/java/com/cazsius/solcarrot/client/gui/Page.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client.gui;

import com.xy177.solwarly.client.gui.elements.*;

import java.awt.*;

import static com.xy177.solwarly.lib.Localization.localized;

abstract class Page extends UIElement {
	final UIStack mainStack;
	final int spacing = 6;
	
	Page(Rectangle frame, String header) {
		super(frame);
		
		mainStack = new UIStack();
		mainStack.axis = UIStack.Axis.VERTICAL;
		mainStack.spacing = spacing;
		
		UILabel headerLabel = new UILabel(header,frame.width-62);
		mainStack.addChild(headerLabel);
		
		mainStack.addChild(makeSeparatorLine());
		
		children.add(mainStack);
		updateMainStack();
	}
	
	void updateMainStack() {
		mainStack.setCenterX(getCenterX());
		mainStack.setMinY(getMinY() + 17);
		mainStack.updateFrames();
	}
	
	String fraction(int numerator, int denominator) {
		return localized("gui", "food_book.fraction",
			numerator,
			denominator
		);
	}
	
	UIElement makeSeparatorLine() {
		return UIBox.horizontalLine(0, getWidth() / 2, 0, FoodBookScreen.leastBlack);
	}
	
	UIElement statWithIcon(ImageData icon, String value, String name) {
		UIStack valueStack = new UIStack();
		valueStack.axis = UIStack.Axis.HORIZONTAL;
		valueStack.spacing = 3;
		
		valueStack.addChild(new UIImage(icon));
		valueStack.addChild(new UILabel(value));
		
		UIStack fullStack = new UIStack();
		fullStack.axis = UIStack.Axis.VERTICAL;
		fullStack.spacing = 2;
		
		fullStack.addChild(valueStack);
		UILabel nameLabel = new UILabel(name,frame.width-62);
		nameLabel.color = FoodBookScreen.lessBlack;
		fullStack.addChild(nameLabel);
		
		return fullStack;
	}
}

