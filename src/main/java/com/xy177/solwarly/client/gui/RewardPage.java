// Modified by xy177 from the carrot book's ProgressGraph layout; upstream LGPL 2.1.
package com.xy177.solwarly.client.gui;

import com.xy177.solwarly.client.gui.elements.*;
import com.xy177.solwarly.item.SOLWarlyItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import java.awt.*;
import java.util.Map;
import java.util.SortedMap;
import static com.xy177.solwarly.lib.DisplayNumbers.format;

final class RewardPage extends Page {
    RewardPage(Rectangle frame, String title, double score, SortedMap<Double, String> totals,
               Map<Double, Double> hearts, boolean diversity, double inheritedHearts) {
        super(frame, title);
        RewardProgress progress = new RewardProgress(score, totals.keySet());
        if (progress.maximum == null) {
            UILabel empty = new UILabel(I18n.format("solwarly.book.no_rewards"));
            empty.setCenterX(getCenterX()); empty.setMinY(getMinY() + 72); children.add(empty);
            return;
        }
        int y = getMinY() + 72, left = getCenterX() - 32, right = getCenterX() + 48;
        boolean maximum = progress.next == null;
        boolean beyondMaximum = score > progress.maximum;
        UIItemStack book = new UIItemStack(new ItemStack(SOLWarlyItems.foodBook));
        book.setMaxX(left - 12); book.setCenterY(y); children.add(book);
        children.add(UIBox.horizontalLine(left - 10, left, y, GuiFoodBook.fullBlack));
        children.add(UIBox.horizontalLine(left, right, y, maximum ? GuiFoodBook.fullBlack : GuiFoodBook.lessBlack));

        double currentHearts = Math.max(inheritedHearts, progress.currentTier == null ? 0 : hearts.getOrDefault(progress.currentTier, 0.0));
        String currentReward = diversity
            ? (progress.currentTier == null ? I18n.format("solwarly.book.no_reward") : totals.get(progress.currentTier))
            : I18n.format("solwarly.book.total_hearts", format(currentHearts));
        String kind = diversity ? "diversity" : "experience";
        milestone(beyondMaximum ? right : left, y, score, true, currentHearts, diversity,
            tooltip(kind, "current", score, currentReward));
        if (!maximum) milestone((left + right) / 2, y, progress.next, false,
            hearts.getOrDefault(progress.next, 0.0), diversity,
            tooltip(kind, "next", progress.next, totals.get(progress.next)));
        milestone(beyondMaximum ? left : right, y, progress.maximum, maximum, hearts.getOrDefault(progress.maximum, 0.0), diversity,
            tooltip(kind, "maximum", progress.maximum, totals.get(progress.maximum)));
    }

    private String tooltip(String kind, String stage, double score, String reward) {
        return I18n.format("solwarly.book." + kind + "." + stage + "_score", format(score)) + "\n"
            + I18n.format("solwarly.book." + kind + "." + stage + "_reward", reward);
    }

    private void milestone(int x, int y, double score, boolean reached, double hearts, boolean diversity, String tooltip) {
        Color color = reached ? GuiFoodBook.fullBlack : GuiFoodBook.lessBlack;
        UILabel label = new UILabel(format(score)); label.color = color;
        label.setCenterX(x); label.setMaxY(y - 3); label.tooltip = tooltip; children.add(label);
        UIElement tick = UIBox.verticalLine(x, y - 2, y + 4, color);
        tick.tooltip = tooltip; children.add(tick);
        UIStack reward = new UIStack(); reward.spacing = 1;
        if (diversity) {
            UIItemStack bottle = new UIItemStack(new ItemStack(Items.EXPERIENCE_BOTTLE));
            bottle.tooltip = tooltip; reward.addChild(bottle);
        } else {
            UIImage heart = new UIImage(GuiFoodBook.heartImage);
            heart.alpha = reached ? 1 : .5f;
            reward.addChild(heart);
            UILabel count = new UILabel("×" + format(hearts)); count.color = color;
            reward.addChild(count);
        }
        reward.setCenterX(x); reward.setMaxY(label.getMinY() - 4); reward.updateFrames();
        reward.tooltip = tooltip; children.add(reward);
    }
}
