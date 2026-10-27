// Adapted from Spice of Life: Apple Pie Edition; upstream contributors retain copyright.
// Ported and modified by xy177 on 2026-10-27; LGPL-2.1.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/client/gui/elements/UIStack.java
// Warly rules are retained; upstream credits are recorded in THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client.gui.elements;

import java.awt.*;
import java.util.List;

public final class UIStack extends UIElement {
	public Axis axis = Axis.HORIZONTAL;
	public int spacing = 1;

	public UIStack() {
		super(new Rectangle(0, 0));
	}

	public void addChild(UIElement child) {
		children.add(child);
		updateFrames();
	}

	public List<UIElement> getChildren() {
		return children;
	}

	public void updateFrames() {
		switch (axis) {
			case HORIZONTAL:
				int x = 0;
				int height = children.stream().mapToInt(child -> child.frame.height).max().orElse(0);
				for (UIElement child : children) {
					child.frame.x = frame.x + x;
					child.frame.y = frame.y + (height - child.frame.height) / 2;
					if (child instanceof UIStack) {
						((UIStack) child).updateFrames();
					}
					x += child.frame.width;
					x += spacing;
				}
				frame.width = x - spacing;
				frame.height = height;
				break;
			case VERTICAL:
				int y = 0;
				int width = children.stream().mapToInt(child -> child.frame.width).max().orElse(0);
				for (UIElement child : children) {
					child.frame.x = frame.x + (width - child.frame.width) / 2;
					child.frame.y = frame.y + y;
					if (child instanceof UIStack) {
						((UIStack) child).updateFrames();
					}
					y += child.frame.height;
					y += spacing;
				}
				frame.width = width;
				frame.height = y - spacing;
				break;
			default:
				throw new AssertionError();
		}
	}

	public enum Axis {
		HORIZONTAL, VERTICAL
	}
}
