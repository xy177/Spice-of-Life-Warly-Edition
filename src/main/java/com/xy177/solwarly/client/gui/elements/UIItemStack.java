// Adapted from Spice of Life: Apple Pie Edition; upstream contributors retain copyright.
// Ported and modified by xy177 on 2026-10-27; LGPL-2.1.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/client/gui/elements/UIItemStack.java
// Warly rules and legacy save/API compatibility are retained; see THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client.gui.elements;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.List;

import static net.minecraft.world.item.TooltipFlag.Default.ADVANCED;
import static net.minecraft.world.item.TooltipFlag.Default.NORMAL;

public class UIItemStack extends UIElement {
	public static final int size = 16;

	public ItemStack itemStack;

	public UIItemStack(ItemStack itemStack) {
		super(new Rectangle(size, size));

		this.itemStack = itemStack;
	}

	@Override
	protected void render(GuiGraphics matrices) {
		super.render(matrices);

		matrices.renderItem(
			itemStack,
			frame.x + (frame.width - size) / 2,
			frame.y + (frame.height - size) / 2
		);
	}

	@Override
	protected boolean hasTooltip() {
		return true;
	}

	@Override
	protected void renderTooltip(GuiGraphics matrices, int mouseX, int mouseY) {
		List<Component> tooltip = itemStack.getTooltipLines(mc.player, mc.options.advancedItemTooltips ? ADVANCED : NORMAL);
		if (this.tooltip != null) tooltip = java.util.Arrays.stream(this.tooltip.split("\n")).map(Component::literal).map(value -> (Component) value).toList();
		renderTooltip(matrices, itemStack, tooltip, mouseX, mouseY);
	}
}
