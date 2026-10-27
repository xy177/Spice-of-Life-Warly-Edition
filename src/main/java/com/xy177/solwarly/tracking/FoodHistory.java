// Adapted from Spice of Life: Apple Pie Edition's FoodList and its recent-food index.
// Upstream contributors retain copyright. Ported and modified by xy177 on 2026-10-27.
// Source: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/tracking/FoodList.java
// LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.tracking;

import com.xy177.solwarly.diet.DietData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Apple Pie's food/last-eaten index, extended for Warly flavors and repeated meals.
 * The saved meal contributions remain authoritative: there is no age decay,
 * nutrition recalculation or replacement with Apple Pie's default score formula.
 * This is the primary in-memory recent-food store; persistent NBT belongs to
 * DietData, allowing existing worlds to load unchanged.
 */
public final class FoodHistory implements Iterable<DietData.Meal> {
    private final List<DietData.Meal> meals;
    private boolean indexed;
    private Map<String, Map<String, Integer>> uniqueFoods = new HashMap<>();

    public FoodHistory() { this.meals = new ArrayList<>(); }

    public FoodHistory(Collection<DietData.Meal> meals) { this.meals = new ArrayList<>(meals); }

    public void addLast(DietData.Meal meal) { meals.add(meal); indexed = false; }
    public int size() { return meals.size(); }
    public void clear() { meals.clear(); uniqueFoods.clear(); indexed = true; }
    public void trim(int count) {
        if (meals.size() > count) { meals.subList(0, meals.size() - count).clear(); indexed = false; }
    }
    public List<DietData.Meal> snapshot() { return new ArrayList<>(meals); }
    @Override public java.util.Iterator<DietData.Meal> iterator() {
        return java.util.Collections.unmodifiableList(meals).iterator();
    }

    private void index() {
        if (indexed) return;
        uniqueFoods = new HashMap<>();
        // Keep first-seen key order, updating the latest age on every occurrence.
        for (int i = 0; i < this.meals.size(); i++) {
            DietData.Meal meal = this.meals.get(i);
            Map<String, Integer> flavors = uniqueFoods.computeIfAbsent(meal.food, key -> new HashMap<>());
            flavors.put(meal.flavor, this.meals.size() - 1 - i);
        }
        indexed = true;
    }

    public int getLastEaten(String food, String flavor) {
        index();
        Map<String, Integer> flavors = uniqueFoods.get(food);
        return flavors == null ? -1 : flavors.getOrDefault(flavor, -1);
    }

    public double averageContribution() {
        return meals.stream().mapToDouble(meal -> meal.contribution).average().orElse(0);
    }

    public double variety(double extraFlavor) {
        index();
        return uniqueFoods.values().stream().mapToDouble(flavors -> 1 + (flavors.size() - 1) * extraFlavor).sum();
    }

    public double foodDiversity(double extraFlavor, double scale) {
        return averageContribution() * variety(extraFlavor) * scale;
    }

    /** Apple Pie-style meal preview, without changing first tastes or saved history. */
    public double simulateFoodAdd(DietData.Meal meal, int count, double extraFlavor, double scale) {
        List<DietData.Meal> preview = new ArrayList<>(meals);
        preview.add(meal);
        if (preview.size() > count) preview = preview.subList(preview.size() - count, preview.size());
        return new FoodHistory(preview).foodDiversity(extraFlavor, scale);
    }
}
