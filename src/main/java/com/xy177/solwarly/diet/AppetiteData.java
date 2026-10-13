package com.xy177.solwarly.diet;

import net.minecraft.nbt.*;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

public final class AppetiteData extends SavedData {
    @Override public CompoundTag save(CompoundTag tag,net.minecraft.core.HolderLookup.Provider provider) { return save(tag); }

    public long lastDay = Long.MIN_VALUE;
    private boolean syncPending;
    public int retainedCount = 20;
    private final Deque<String> recent = new ArrayDeque<>();
    public final Set<String> craved = new LinkedHashSet<>(), boring = new LinkedHashSet<>();
    private final Set<String> fixedCraved = new LinkedHashSet<>(), fixedBoring = new LinkedHashSet<>();
    public AppetiteData() {  }
    public AppetiteData(String name) {  }
    public void eat(String food) { recent.addLast(food); trim(); setDirty(); }
    private void trim() { while (recent.size() > retainedCount) recent.removeFirst(); }
    public int recentSize() { return recent.size(); }
    public int state(String food) {
        return fixedBoring.contains(food) ? -1 : fixedCraved.contains(food) ? 1 : craved.contains(food) ? 1 : boring.contains(food) ? -1 : 0;
    }
    public Set<String> cravedFoods() {
        Set<String> result = new LinkedHashSet<>(craved); result.addAll(fixedCraved); result.removeAll(fixedBoring); return result;
    }
    public Set<String> boringFoods() {
        Set<String> result = new LinkedHashSet<>(boring); result.addAll(fixedBoring); result.removeAll(fixedCraved); return result;
    }
    public double multiplier(String food) {
        return state(food) > 0 ? DietConfig.cravingMultiplier : state(food) < 0 ? DietConfig.boringMultiplier : DietConfig.normalMultiplier;
    }
    public boolean refresh(long day, int players, Collection<String> candidates, Random random) {
        Set<String> nextBoring = new LinkedHashSet<>(Arrays.asList(DietConfig.alwaysBoring));
        Set<String> nextCraved = new LinkedHashSet<>(Arrays.asList(DietConfig.alwaysCraved)); nextCraved.removeAll(nextBoring);
        if (!fixedCraved.equals(nextCraved) || !fixedBoring.equals(nextBoring)) {
            fixedCraved.clear(); fixedCraved.addAll(nextCraved); fixedBoring.clear(); fixedBoring.addAll(nextBoring);
            craved.removeAll(fixedCraved); craved.removeAll(fixedBoring);
            boring.removeAll(fixedCraved); boring.removeAll(fixedBoring);
            syncPending = true; setDirty();
        }
        if (day < lastDay) {
            // Rebase after /time set without redrawing today's interests.
            lastDay = day;
            setDirty();
            return false;
        }
        if (day == lastDay) return false;
        lastDay = day;
        retainedCount = Math.max(2, Math.min(DietConfig.frequencyMaximum, Math.max(1, players) * DietConfig.frequencyPerPlayer));
        trim(); craved.clear(); boring.clear();
        List<String> available = new ArrayList<>(new TreeSet<>(candidates));
        available.removeAll(fixedCraved); available.removeAll(fixedBoring);
        // Keep the day's scale fixed while drawing without replacement.
        double frequencyWeight = DietConfig.frequencyWeight < 0 ? 0.43 * available.size() / retainedCount : DietConfig.frequencyWeight;
        Map<String, Integer> frequencies = new HashMap<>();
        recent.forEach(food -> frequencies.merge(food, 1, Integer::sum));
        int wanted = DietConfig.cravingCount + DietConfig.boringCount;
        int cravings = DietConfig.cravingCount, dislikes = DietConfig.boringCount;
        if (available.size() < wanted) {
            cravings = Math.min(cravings, (available.size() + 1) / 2);
            dislikes = Math.min(dislikes, available.size() - cravings);
            cravings = Math.min(DietConfig.cravingCount, available.size() - dislikes);
        }
        for (int i = 0; i < cravings; i++) craved.add(draw(available, frequencies, true, frequencyWeight, random));
        for (int i = 0; i < dislikes; i++) boring.add(draw(available, frequencies, false, frequencyWeight, random));
        syncPending = true;
        setDirty(); return true;
    }
    public boolean consumePendingSync() {
        boolean pending = syncPending;
        syncPending = false;
        return pending;
    }
    private String draw(List<String> choices, Map<String, Integer> frequencies, boolean craving, double frequencyWeight, Random random) {
        if (random.nextDouble() < DietConfig.uniformChance) return choices.remove(random.nextInt(choices.size()));
        double sum = 0;
        for (String food : choices) sum += weight(frequencies.getOrDefault(food, 0), craving, frequencyWeight);
        double pick = random.nextDouble() * sum;
        for (int i = 0; i < choices.size(); i++) {
            pick -= weight(frequencies.getOrDefault(choices.get(i), 0), craving, frequencyWeight);
            if (pick <= 0) return choices.remove(i);
        }
        return choices.remove(choices.size() - 1);
    }
    private double weight(int count, boolean craving, double frequencyWeight) {
        double weight = 1 + count * frequencyWeight;
        return craving ? 1 / weight : weight;
    }
    public void readFromNBT(CompoundTag tag) {
        syncPending = false;
        lastDay = tag.contains("day") ? tag.getLong("day") : Long.MIN_VALUE;
        retainedCount = Math.max(2, tag.getInt("retainedCount"));
        recent.clear(); craved.clear(); boring.clear();
        recent.addAll(strings(tag, "recent")); craved.addAll(strings(tag, "craved")); boring.addAll(strings(tag, "boring")); trim();
        fixedCraved.clear(); fixedCraved.addAll(strings(tag, "fixedCraved"));
        fixedBoring.clear(); fixedBoring.addAll(strings(tag, "fixedBoring")); fixedCraved.removeAll(fixedBoring);
    }
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("day", lastDay); tag.putInt("retainedCount", retainedCount);
        tag.put("recent", list(recent)); tag.put("craved", list(craved)); tag.put("boring", list(boring));
        tag.put("fixedCraved", list(fixedCraved)); tag.put("fixedBoring", list(fixedBoring)); return tag;
    }
    private static ListTag list(Collection<String> strings) {
        ListTag list = new ListTag(); strings.forEach(value -> list.add(StringTag.valueOf(value))); return list;
    }
    private static List<String> strings(CompoundTag tag, String key) {
        ListTag list = tag.getList(key, 8); List<String> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) result.add(list.getString(i)); return result;
    }
}
