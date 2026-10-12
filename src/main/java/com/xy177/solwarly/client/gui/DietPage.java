package com.xy177.solwarly.client.gui;

import com.xy177.solwarly.client.gui.elements.UILabel;
import com.xy177.solwarly.diet.*;
import net.minecraft.client.resources.I18n;
import java.awt.Rectangle;
import java.util.*;
import static com.xy177.solwarly.lib.DisplayNumbers.format;

final class DietPage extends Page {
    DietPage(Rectangle frame, String title, List<String> lines) {
        super(frame, title);
        for (String line : lines) {
            mainStack.addChild(new UILabel(line, frame.width - 42));
        }
        updateMainStack();
    }
    private static double cumulativeHealth(double score) {
        double threshold = -1, health = 0;
        for (String line : DietRuntime.clientCumulativeRewards) {
            String[] parts = line.split("\\|"); double candidate = Double.parseDouble(parts[0]);
            if (candidate <= score && candidate >= threshold) { threshold = candidate; health = Double.parseDouble(parts[1]); }
        }
        return health;
    }
    static List<Page> statistics(Rectangle frame, DietData diet) {
        List<Page> pages = new ArrayList<>();
        if (diet == null) return pages;
        List<String> statistics = new ArrayList<>(Arrays.asList(
            I18n.format("solwarly.book.cumulative", format(diet.cumulative())),
            I18n.format("solwarly.book.diversity", format(diet.diversity(DietRuntime.clientExtraFlavor, DietRuntime.clientScale))),
            I18n.format("solwarly.book.meals", diet.mealCount(), DietRuntime.clientRecentCount),
            I18n.format("solwarly.book.average", format(diet.average())),
            I18n.format("solwarly.book.variety", format(diet.variety(DietRuntime.clientExtraFlavor)))));
        if (diet.legacyHealthFloor > cumulativeHealth(diet.cumulative())) statistics.add(I18n.format("solwarly.book.floor", format(diet.legacyHealthFloor / 2)));
        pages.add(new DietPage(frame, I18n.format("solwarly.book.stats"), statistics));
        SortedMap<Double, Double> cumulative = new TreeMap<>();
        for (String line : DietRuntime.clientCumulativeRewards) {
            String[] parts = line.split("\\|");
            if (parts.length == 2) cumulative.put(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]) / 2);
        }
        SortedMap<Double, String> experienceTotals = new TreeMap<>();
        cumulative.forEach((tier, hearts) -> experienceTotals.put(tier, I18n.format("solwarly.book.total_hearts", format(hearts))));
        pages.add(new RewardPage(frame, I18n.format("solwarly.book.cumulative_rewards"), diet.cumulative(), experienceTotals, cumulative, false, diet.legacyHealthFloor / 2));
        double score = diet.diversity(DietRuntime.clientExtraFlavor, DietRuntime.clientScale);
        SortedMap<Double, List<String>> rewards = new TreeMap<>();
        Map<Double, Double> health = new HashMap<>();
        for (String line : DietRuntime.clientAttributes) {
            String[] parts = line.split("\\|");
            if (parts.length != 4) continue;
            double tier = Double.parseDouble(parts[0]), amount = Double.parseDouble(parts[2]);
            int operation = Integer.parseInt(parts[3]);
            String description;
            if (parts[1].equals("generic.maxHealth") && operation == 0) {
                health.merge(tier, amount / 2, Double::sum);
                description = I18n.format("solwarly.book.total_hearts", format(amount / 2));
            } else description = I18n.format("attribute.name." + parts[1]) + " " + (amount >= 0 ? "+" : "") + format(operation == 0 ? amount : amount * 100) + (operation == 0 ? "" : "%");
            rewards.computeIfAbsent(tier, key -> new ArrayList<>()).add(description);
        }
        for (String line : DietRuntime.clientEffects) {
            String[] parts = line.split("\\|");
            if (parts.length != 3) continue;
            net.minecraft.potion.Potion potion = net.minecraft.potion.Potion.REGISTRY.getObject(new net.minecraft.util.ResourceLocation(parts[1]));
            rewards.computeIfAbsent(Double.parseDouble(parts[0]), key -> new ArrayList<>()).add(
                (potion == null ? parts[1] : I18n.format(potion.getName())) + " " + (Integer.parseInt(parts[2]) + 1));
        }
        SortedMap<Double, String> diversityTotals = new TreeMap<>();
        rewards.forEach((tier, descriptions) -> diversityTotals.put(tier, String.join(", ", descriptions)));
        pages.add(new RewardPage(frame, I18n.format("solwarly.book.diversity_rewards"), score, diversityTotals, health, true, 0));
        return pages;
    }
}
