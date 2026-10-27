// Modified by xy177 on 2026-10-10 for Spice of Life Warly Edition; upstream LGPL 2.1.
package com.xy177.solwarly.client.gui;

import com.xy177.solwarly.client.gui.elements.UIItemStack;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

final class ItemListPage extends Page {
	private static final int itemsPerRow = 5;
	private static final int rowsPerPage = 6;
	private static final int itemsPerPage = itemsPerRow * rowsPerPage;
	private static final int itemSpacing = UIItemStack.size + 4;
	
	static List<ItemListPage> pages(Rectangle frame, String header, List<ItemStack> items) {
		List<ItemListPage> pages = new ArrayList<>();
		for (int startIndex = 0; startIndex < items.size(); startIndex += ItemListPage.itemsPerPage) {
			int endIndex = Math.min(startIndex + ItemListPage.itemsPerPage, items.size());
			pages.add(new ItemListPage(frame, header, items.subList(startIndex, endIndex)));
		}
		return pages;
	}
	
	ItemStack itemAt(int x, int y) {
        for (com.xy177.solwarly.client.gui.elements.UIElement child : children)
            if (child instanceof UIItemStack && child.frame.contains(x, y)) return ((UIItemStack) child).itemStack;
        return ItemStack.EMPTY;
    }
    private ItemListPage(Rectangle frame, String header, List<ItemStack> items) {
		super(frame, header);
		
		int minX = (1 - itemsPerRow) * itemSpacing / 2;
		int minY = (1 - rowsPerPage) * itemSpacing / 2 - 4;
		
		for (int i = 0; i < items.size(); i++) {
			ItemStack itemStack = items.get(i);
			int x = minX + itemSpacing * (i % itemsPerRow);
			int y = minY + itemSpacing * ((i / itemsPerRow) % rowsPerPage);
			
			UIItemStack view = new UIItemStack(itemStack);
            java.util.List<com.xy177.solwarly.spice.SpiceKind> kinds = com.xy177.solwarly.spice.Seasoning.components(itemStack);
            if (com.xy177.solwarly.spice.Seasoning.sideUses(kinds) > 1) {
                view.tooltip = net.minecraft.client.resources.language.I18n.get("solwarly.book.combined_accompaniments") + "\n"
                    + net.minecraft.client.resources.language.I18n.get("solwarly.book.any_accompaniments");
                java.util.List<com.xy177.solwarly.spice.SpiceKind> powders = com.xy177.solwarly.spice.Seasoning.powders(kinds);
                if (!powders.isEmpty()) {
                    boolean mixed = com.xy177.solwarly.spice.Seasoning.mixed(kinds);
                    String powderName = mixed ? net.minecraft.client.resources.language.I18n.get("solwarly.book.mixed_flavor")
                        : com.xy177.solwarly.spice.Seasoning.material(powders.get(0)).getHoverName().getString();
                    view.tooltip = powderName + " + " + view.tooltip;
                    if (mixed) view.tooltip += "\n" + net.minecraft.client.resources.language.I18n.get("solwarly.book.any_mixed");
                }
            } else if (com.xy177.solwarly.spice.Seasoning.MIXED_FLAVOR.equals(com.xy177.solwarly.api.FlavorAPI.resolve(itemStack).id))
                view.tooltip = net.minecraft.client.resources.language.I18n.get("solwarly.book.mixed_flavor") + "\n"
                    + net.minecraft.client.resources.language.I18n.get("solwarly.book.any_mixed");
			view.setCenterX(getCenterX() + x);
			view.setCenterY(getCenterY() + y);
			children.add(view);
		}
	}
}
