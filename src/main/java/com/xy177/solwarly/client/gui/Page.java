// Adapted from Spice of Life: Apple Pie Edition; upstream contributors retain copyright.
// Ported and modified by xy177 on 2026-10-27; LGPL-2.1.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/client/gui/Page.java
// Warly behavior and legacy save/API compatibility retained; see THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client.gui;

import com.xy177.solwarly.client.gui.elements.*;

import java.awt.*;

import static com.xy177.solwarly.lib.Localization.localized;

abstract class Page extends UIElement {
	final UIStack mainStack;
	final int spacing = 6;
	final UILabel headerLabel;
	final int contentWidth;

	Page(Rectangle frame, String header) {
		super(frame);
		contentWidth = Math.max(1, frame.width - 70);

		mainStack = new UIStack();
		mainStack.axis = UIStack.Axis.VERTICAL;
		mainStack.spacing = spacing;

		headerLabel = new UILabel(header, contentWidth);
		mainStack.addChild(headerLabel);

		mainStack.addChild(makeSeparatorLine());

		children.add(mainStack);
		updateMainStack();
	}

	public void setHeaderTooltip(String tooltip) {
		headerLabel.tooltip = tooltip;
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
		return UIBox.horizontalLine(0, getWidth() / 2, 0, GuiFoodBook.leastBlack);
	}

	UIImage icon(UIImage.Image image) {
		UIImage icon = new UIImage(image);
		icon.setWidth(11);
		icon.setHeight(11);
		return icon;
	}

	UIElement statWithIcon(UIImage icon, String value, String name) {
		UIStack valueStack = new UIStack();
		valueStack.axis = UIStack.Axis.HORIZONTAL;
		valueStack.spacing = 3;

		valueStack.addChild(icon);
		valueStack.addChild(new UILabel(value));

		UIStack fullStack = new UIStack();
		fullStack.axis = UIStack.Axis.VERTICAL;
		fullStack.spacing = 2;

		fullStack.addChild(valueStack);
		UILabel nameLabel = new UILabel(name, contentWidth);
		nameLabel.color = GuiFoodBook.lessBlack;
		fullStack.addChild(nameLabel);

		return fullStack;
	}
}
