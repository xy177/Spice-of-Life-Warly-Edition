package com.xy177.solwarly.diet;

import net.minecraft.nbt.*;
import net.minecraftforge.common.util.INBTSerializable;
import com.xy177.solwarly.spice.Seasoning;
import java.util.*;

public final class DietData implements INBTSerializable<CompoundTag> {
    public boolean initialized;
    public double legacyHealthFloor;
    private final Map<String, Double> bases = new LinkedHashMap<>();
    private final Map<String, Double> fractions = new HashMap<>();
    private final Map<String, Set<String>> flavors = new LinkedHashMap<>();
    private final Deque<Meal> meals = new ArrayDeque<>();
    private final Map<String, Repeat> repeats = new HashMap<>();
    private byte warlyOverride = -1; // -1 follows the global setting, 0 disables, 1 enables

    public boolean isWarlyModeEnabled() { return warlyOverride < 0 ? DietConfig.warlyMode : warlyOverride > 0; }
    public int warlyOverride() { return warlyOverride; }
    public void setWarlyOverride(int value) { warlyOverride = (byte) (value < 0 ? -1 : value == 0 ? 0 : 1); }
    public int warlyRepeatCount(String food, long clock) {
        Repeat repeat = repeats.get(food);
        return repeat != null && active(repeat, clock, DietConfig.warlyDurationDays) ? repeat.count : 0;
    }
    public double warlyMultiplier(String food, long clock) {
        return warlyMultiplier(food, clock, DietConfig.warlyMode, DietConfig.warlyRepeatMultipliers, DietConfig.warlyDurationDays);
    }
    public double warlyMultiplier(String food, long clock, boolean globalEnabled, double[] multipliers, int durationDays) {
        if (warlyOverride < 0 ? !globalEnabled : warlyOverride == 0) return 1;
        if (multipliers == null || multipliers.length == 0) return 1;
        Repeat repeat = repeats.get(food);
        int count = repeat != null && active(repeat, clock, durationDays) ? repeat.count : 0;
        return multipliers[Math.min(count, multipliers.length - 1)];
    }
    /** Records a non-craved meal after it has actually been eaten. */
    public void recordWarlyMeal(String food, long clock) {
        if (!isWarlyModeEnabled()) return;
        repeats.entrySet().removeIf(entry -> !active(entry.getValue(), clock, DietConfig.warlyDurationDays));
        Repeat repeat = repeats.get(food);
        if (repeat == null || !active(repeat, clock, DietConfig.warlyDurationDays)) repeat = new Repeat();
        repeat.count = Math.min(DietConfig.warlyRepeatMultipliers.length - 1, repeat.count + 1);
        repeat.lastClock = clock;
        repeats.put(food, repeat);
    }
    private boolean active(Repeat repeat, long clock, int durationDays) {
        if (clock < repeat.lastClock) return true; // a clock rebase must not expire a record
        return clock - repeat.lastClock < Math.max(1, durationDays) * 24000L;
    }

    public double eat(String food, String flavor, double base, double contribution, int count) {
        flavor = Seasoning.canonicalFlavor(flavor);
        double gain = 0;
        Set<String> tried = flavors.computeIfAbsent(food, key -> new LinkedHashSet<>());
        if (!tried.contains(flavor)) {
            if (tried.isEmpty()) { bases.put(food, base); fractions.put(food, DietConfig.cumulativeFlavorFraction); }
            gain = bases.get(food) * (tried.isEmpty() ? 1 : fractions.get(food));
            tried.add(flavor);
        }
        meals.addLast(new Meal(food, flavor, contribution));
        trim(count);
        return gain;
    }

    public void migrateFood(String food, double base) {
        if (!bases.containsKey(food)) {
            bases.put(food, base);
            fractions.put(food, DietConfig.cumulativeFlavorFraction);
            flavors.put(food, new LinkedHashSet<>(Collections.singleton("plain")));
        }
    }

    public void trim(int count) { while (meals.size() > count) meals.removeFirst(); }
    public int mealCount() { return meals.size(); }
    public List<Meal> meals() { return new ArrayList<>(meals); }
    public boolean hasTried(String food, String flavor) {
        return flavors.getOrDefault(food, Collections.emptySet()).contains(Seasoning.canonicalFlavor(flavor));
    }
    public boolean hasTriedFood(String food) { return flavors.containsKey(food); }
    public double nextGain(String food, String flavor, double base) {
        if (hasTried(food, flavor)) return 0;
        return bases.containsKey(food) ? bases.get(food) * fractions.get(food) : base;
    }
    public double cumulative() {
        double sum = 0;
        for (Map.Entry<String, Double> entry : bases.entrySet())
            sum += entry.getValue() * (1 + (flavors.get(entry.getKey()).size() - 1) * fractions.get(entry.getKey()));
        return sum;
    }
    public double average() {
        return average(meals);
    }
    public double variety(double extraFlavor) {
        return variety(meals, extraFlavor);
    }
    private static double average(Collection<Meal> meals) {
        return meals.stream().mapToDouble(meal -> meal.contribution).average().orElse(0);
    }
    private static double variety(Collection<Meal> meals, double extraFlavor) {
        Map<String, Set<String>> types = new HashMap<>();
        for (Meal meal : meals) types.computeIfAbsent(meal.food, key -> new HashSet<>()).add(meal.flavor);
        return types.values().stream().mapToDouble(set -> 1 + (set.size() - 1) * extraFlavor).sum();
    }
    public double diversity(double extraFlavor, double scale) { return average() * variety(extraFlavor) * scale; }
    /** Preview a meal without changing history, saved contributions or first-taste opportunities. */
    public double diversityAfter(String food, String flavor, double contribution, int count, double extraFlavor, double scale) {
        List<Meal> recent = meals();
        recent.add(new Meal(food, flavor, contribution));
        if (recent.size() > count) recent = recent.subList(recent.size() - count, recent.size());
        return average(recent) * variety(recent, extraFlavor) * scale;
    }

    public void reset(boolean clearFloor) {
        bases.clear(); fractions.clear(); flavors.clear(); meals.clear(); initialized = true;
        if (clearFloor) legacyHealthFloor = 0;
    }
    public void recalculate(java.util.function.ToDoubleFunction<String> coefficient) {
        bases.replaceAll((food, previous) -> {
            double value = coefficient.applyAsDouble(food);
            return Double.isFinite(value) && value >= 0 ? value : previous;
        });
    }

    @Override public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("version", initialized ? 2 : 0);
        tag.putDouble("legacyHealthFloor", legacyHealthFloor);
        tag.putByte("warlyOverride", warlyOverride);
        ListTag repeatsTag = new ListTag();
        for (Map.Entry<String, Repeat> entry : repeats.entrySet()) {
            CompoundTag value = new CompoundTag(); value.putString("food", entry.getKey());
            value.putInt("count", entry.getValue().count); value.putLong("lastClock", entry.getValue().lastClock);
            repeatsTag.add(value);
        }
        tag.put("warlyRepeats", repeatsTag);
        ListTag history = new ListTag();
        for (String food : bases.keySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("food", food); entry.putDouble("base", bases.get(food));
            entry.putDouble("fraction", fractions.get(food));
            ListTag tastes = new ListTag();
            flavors.get(food).forEach(flavor -> tastes.add(StringTag.valueOf(flavor)));
            entry.put("flavors", tastes); history.add(entry);
        }
        tag.put("history", history);
        ListTag recent = new ListTag();
        for (Meal meal : meals) {
            CompoundTag entry = new CompoundTag();
            entry.putString("food", meal.food); entry.putString("flavor", meal.flavor);
            entry.putDouble("contribution", meal.contribution); recent.add(entry);
        }
        tag.put("meals", recent);
        return tag;
    }
    @Override public void deserializeNBT(CompoundTag tag) {
        bases.clear(); fractions.clear(); flavors.clear(); meals.clear(); repeats.clear();
        initialized = tag.getInt("version") > 0;
        legacyHealthFloor = tag.getDouble("legacyHealthFloor");
        warlyOverride = tag.contains("warlyOverride") ? tag.getByte("warlyOverride") : -1;
        ListTag repeatsTag = tag.getList("warlyRepeats", 10);
        for (int i = 0; i < repeatsTag.size(); i++) {
            CompoundTag value = repeatsTag.getCompound(i);
            Repeat repeat = new Repeat(); repeat.count = Math.max(0, value.getInt("count"));
            repeat.lastClock = value.getLong("lastClock"); repeats.put(value.getString("food"), repeat);
        }
        ListTag history = tag.getList("history", 10);
        for (int i = 0; i < history.size(); i++) {
            CompoundTag entry = history.getCompound(i);
            String food = entry.getString("food");
            ListTag tastes = entry.getList("flavors", 8);
            Set<String> set = new LinkedHashSet<>();
            for (int j = 0; j < tastes.size(); j++) set.add(Seasoning.canonicalFlavor(tastes.getString(j)));
            if (!set.isEmpty()) {
                bases.put(food, entry.getDouble("base")); flavors.put(food, set);
                fractions.put(food, entry.contains("fraction") ? entry.getDouble("fraction") : 0.5);
            }
        }
        ListTag recent = tag.getList("meals", 10);
        for (int i = 0; i < recent.size(); i++) {
            CompoundTag entry = recent.getCompound(i);
            meals.addLast(new Meal(entry.getString("food"), entry.getString("flavor"), entry.getDouble("contribution")));
        }
    }
    public static final class Meal {
        public final String food, flavor;
        public final double contribution;
        public Meal(String food, String flavor, double contribution) {
            this.food = food; this.flavor = Seasoning.canonicalFlavor(flavor); this.contribution = contribution;
        }
    }
    private static final class Repeat {
        int count;
        long lastClock;
    }
}
