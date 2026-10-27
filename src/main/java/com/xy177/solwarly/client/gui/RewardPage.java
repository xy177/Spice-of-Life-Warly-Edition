// Modified by xy177 on 2026-10-27 from the carrot book's ProgressGraph layout; upstream LGPL 2.1.
package com.xy177.solwarly.client.gui;

import com.xy177.solwarly.client.gui.elements.*;
import com.xy177.solwarly.item.SOLWarlyItems;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import java.awt.*;
import java.util.Map;
import java.util.SortedMap;
import static com.xy177.solwarly.lib.DisplayNumbers.format;

final class RewardPage extends Page {
    RewardPage(Rectangle frame, String title) {
        super(frame, title);
    }

    void addProgress(int offset, String title, double score, SortedMap<Double, String> totals,
                     Map<Double, Double> hearts, boolean diversity, double inheritedHearts) {
        if (offset > 60) {
            UILabel heading = new UILabel(title, contentWidth);
            heading.setCenterX(getCenterX()); heading.setMinY(getMinY() + offset - 38); children.add(heading);
        }
        RewardProgress progress = new RewardProgress(score, totals.keySet());
        if (progress.maximum == null) {
            UILabel empty = new UILabel(I18n.get("solwarly.book.no_rewards"), contentWidth);
            empty.setCenterX(getCenterX()); empty.setCenterY(getMinY() + offset); children.add(empty);
            return;
        }
        int y = getMinY() + offset, left = getCenterX() - 24, right = getCenterX() + 24;
        double currentHearts = Math.max(inheritedHearts, progress.currentTier == null ? 0 : hearts.getOrDefault(progress.currentTier, 0.0));
        String currentReward = diversity
            ? (progress.currentTier == null ? I18n.get("solwarly.book.no_reward") : totals.get(progress.currentTier))
            : I18n.get("solwarly.book.total_hearts", format(currentHearts));
        String kind = diversity ? "diversity" : "experience";
        double previous = progress.currentTier == null ? 0 : progress.currentTier;
        boolean maximum = progress.next == null;
        int current = maximum ? (score > progress.maximum ? right : left)
            : left + (int) Math.round((right - left) * Math.max(0, Math.min(1, (score - previous) / (progress.next - previous))));
        String currentTip = tooltip(kind, "current", score, currentReward);
        String maximumTip = tooltip(kind, "maximum", progress.maximum, totals.get(progress.maximum));
        String remainingTip = maximum ? I18n.get("solwarly.book.max_rewards")
            : I18n.get("solwarly.book." + kind + ".remaining", format(progress.next - score));
        UIItemStack book = new UIItemStack(new ItemStack(SOLWarlyItems.FOOD_BOOK.get()));
        book.setCenterX(getCenterX() - 49); book.setCenterY(y); book.tooltip = remainingTip; children.add(book);
        UIElement complete = UIBox.horizontalLine(getCenterX() - 36, current, y, FoodBookScreen.fullBlack);
        complete.tooltip = currentTip + "\n" + remainingTip; children.add(complete);
        if (!maximum) {
            UIElement pending = UIBox.horizontalLine(current, getCenterX() + 36, y, FoodBookScreen.leastBlack);
            pending.tooltip = currentTip + "\n" + remainingTip; children.add(pending);
        }
        if (progress.currentTier == null) {
            String startTip = tooltip(kind, "previous", 0, I18n.get("solwarly.book.no_reward"));
            UILabel start = new UILabel("0"); start.setCenterX(left); start.setMaxY(y - 3); start.tooltip = startTip; children.add(start);
            UIElement startTick = UIBox.verticalLine(left, y - 3, y, FoodBookScreen.fullBlack);
            startTick.tooltip = startTip; children.add(startTick);
        } else milestone(left, y, previous, true, hearts.getOrDefault(previous, 0.0), diversity,
            maximum ? maximumTip : tooltip(kind, "previous", previous, totals.get(previous)));
        if (!maximum) {
            milestone(right, y, progress.next, false, hearts.getOrDefault(progress.next, 0.0), diversity,
                tooltip(kind, "next", progress.next, totals.get(progress.next)));
            UILabel last = new UILabel(format(progress.maximum), 20); last.color = FoodBookScreen.leastBlack;
            last.setMinX(getCenterX() + 39); last.setCenterY(y); last.tooltip = maximumTip; children.add(last);
        }
        UIElement marker = UIBox.verticalLine(current, y, y + 6, FoodBookScreen.fullBlack);
        marker.tooltip = currentTip; children.add(marker);
        UILabel currentLabel = new UILabel(format(score), 48);
        currentLabel.setCenterX(current); currentLabel.setMinY(y + 9); currentLabel.tooltip = currentTip; children.add(currentLabel);

        children.add(UIBox.horizontalLine(getCenterX() - 47, getCenterX() + 47, y + 22, FoodBookScreen.leastBlack));
    }

    private String tooltip(String kind, String stage, double score, String reward) {
        return I18n.get("solwarly.book." + kind + "." + stage + "_score", format(score)) + "\n"
            + I18n.get("solwarly.book." + kind + "." + stage + "_reward", kind.equals("diversity") ? "\n" + reward : reward);
    }

    private void milestone(int x, int y, double score, boolean reached, double hearts, boolean diversity, String tooltip) {
        Color color = reached ? FoodBookScreen.fullBlack : FoodBookScreen.lessBlack;
        UILabel label = new UILabel(format(score)); label.color = color;
        label.setCenterX(x); label.setMaxY(y - 3); label.tooltip = tooltip; children.add(label);
        UIElement tick = UIBox.verticalLine(x, y - 3, y, color);
        tick.tooltip = tooltip; children.add(tick);
        UIStack reward = new UIStack(); reward.spacing = 1;
        if (diversity) {
            UIItemStack bottle = new UIItemStack(new ItemStack(Items.EXPERIENCE_BOTTLE));
            bottle.tooltip = tooltip; reward.addChild(bottle);
        } else {
            UIImage heart = new UIImage(FoodBookScreen.heartImage);
            heart.setSize(11, 11);
            heart.alpha = reached ? 1 : .5f;
            reward.addChild(heart);
            UILabel count = new UILabel("×" + format(hearts)); count.color = color;
            reward.addChild(count);
        }
        reward.setCenterX(x); reward.setMaxY(label.getMinY() - 4); reward.updateFrames();
        reward.tooltip = tooltip; children.add(reward);
    }
}
