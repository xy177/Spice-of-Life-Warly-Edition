package com.xy177.solwarly.diet;

import net.minecraft.nbt.*;
import net.minecraftforge.common.util.INBTSerializable;
import com.xy177.solwarly.spice.Seasoning;
import java.util.*;

public final class DietData implements INBTSerializable<NBTTagCompound> {
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

    @Override public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("version", initialized ? 2 : 0);
        tag.setDouble("legacyHealthFloor", legacyHealthFloor);
        tag.setByte("warlyOverride", warlyOverride);
        NBTTagList repeatsTag = new NBTTagList();
        for (Map.Entry<String, Repeat> entry : repeats.entrySet()) {
            NBTTagCompound value = new NBTTagCompound(); value.setString("food", entry.getKey());
            value.setInteger("count", entry.getValue().count); value.setLong("lastClock", entry.getValue().lastClock);
            repeatsTag.appendTag(value);
        }
        tag.setTag("warlyRepeats", repeatsTag);
        NBTTagList history = new NBTTagList();
        for (String food : bases.keySet()) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("food", food); entry.setDouble("base", bases.get(food));
            entry.setDouble("fraction", fractions.get(food));
            NBTTagList tastes = new NBTTagList();
            flavors.get(food).forEach(flavor -> tastes.appendTag(new NBTTagString(flavor)));
            entry.setTag("flavors", tastes); history.appendTag(entry);
        }
        tag.setTag("history", history);
        NBTTagList recent = new NBTTagList();
        for (Meal meal : meals) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("food", meal.food); entry.setString("flavor", meal.flavor);
            entry.setDouble("contribution", meal.contribution); recent.appendTag(entry);
        }
        tag.setTag("meals", recent);
        return tag;
    }
    @Override public void deserializeNBT(NBTTagCompound tag) {
        bases.clear(); fractions.clear(); flavors.clear(); meals.clear(); repeats.clear();
        initialized = tag.getInteger("version") > 0;
        legacyHealthFloor = tag.getDouble("legacyHealthFloor");
        warlyOverride = tag.hasKey("warlyOverride") ? tag.getByte("warlyOverride") : -1;
        NBTTagList repeatsTag = tag.getTagList("warlyRepeats", 10);
        for (int i = 0; i < repeatsTag.tagCount(); i++) {
            NBTTagCompound value = repeatsTag.getCompoundTagAt(i);
            Repeat repeat = new Repeat(); repeat.count = Math.max(0, value.getInteger("count"));
            repeat.lastClock = value.getLong("lastClock"); repeats.put(value.getString("food"), repeat);
        }
        NBTTagList history = tag.getTagList("history", 10);
        for (int i = 0; i < history.tagCount(); i++) {
            NBTTagCompound entry = history.getCompoundTagAt(i);
            String food = entry.getString("food");
            NBTTagList tastes = entry.getTagList("flavors", 8);
            Set<String> set = new LinkedHashSet<>();
            for (int j = 0; j < tastes.tagCount(); j++) set.add(Seasoning.canonicalFlavor(tastes.getStringTagAt(j)));
            if (!set.isEmpty()) {
                bases.put(food, entry.getDouble("base")); flavors.put(food, set);
                fractions.put(food, entry.hasKey("fraction") ? entry.getDouble("fraction") : 0.5);
            }
        }
        NBTTagList recent = tag.getTagList("meals", 10);
        for (int i = 0; i < recent.tagCount(); i++) {
            NBTTagCompound entry = recent.getCompoundTagAt(i);
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
