package com.xy177.solwarly.spice;

import java.util.*;

/** Compact amounts with a bounded list view for existing display and integration APIs. */
public final class SeasoningAmounts extends AbstractList<SpiceKind> {
    final SortedMap<SpiceKind, Long> amounts;
    final List<SpiceKind> badges;
    final int powderUses;
    final boolean mixed;
    private final List<SpiceKind> view;
    SeasoningAmounts(Map<SpiceKind, Long> amounts, int powderUses, boolean mixed, List<SpiceKind> badges) {
        TreeMap<SpiceKind, Long> values = new TreeMap<>(amounts);
        long total = 0;
        for (long count : values.values()) {
            if (count < 1 || count > (long) Integer.MAX_VALUE * 8) throw new IllegalArgumentException("Invalid seasoning amount");
            total = Math.addExact(total, count);
        }
        if (powderUses < 0 || total == 0) throw new IllegalArgumentException("Invalid seasoning uses");
        this.amounts = Collections.unmodifiableSortedMap(values);
        this.powderUses = powderUses; this.mixed = mixed;
        this.badges = Collections.unmodifiableList(new ArrayList<>(badges.subList(0, Math.min(2, badges.size()))));
        List<SpiceKind> display = new ArrayList<>();
        values.forEach((kind, count) -> { for (int i = 0; i < Math.min(3L, count); i++) display.add(kind); });
        view = Collections.unmodifiableList(display);
    }
    static SeasoningAmounts of(List<SpiceKind> kinds) {
        if (kinds instanceof SeasoningAmounts) return (SeasoningAmounts) kinds;
        Map<SpiceKind, Long> values = new TreeMap<>(); List<SpiceKind> badges = new ArrayList<>(); long powders = 0;
        for (SpiceKind kind : kinds) {
            values.merge(kind, 1L, Long::sum);
            if (kind.accompaniment()) { if (badges.size() < 2) badges.add(kind); } else powders++;
        }
        return new SeasoningAmounts(values, powders > 0 ? 1 : 0, powders > 1, badges);
    }
    public long count(SpiceKind kind) { return amounts.getOrDefault(kind, 0L); }
    public long sideUses() { long count = 0; for (Map.Entry<SpiceKind, Long> e : amounts.entrySet()) if (e.getKey().accompaniment()) count += e.getValue(); return count; }
    SeasoningAmounts merge(SeasoningAmounts other) {
        Map<SpiceKind, Long> values = new TreeMap<>(amounts); other.amounts.forEach((kind, count) -> values.merge(kind, count, Math::addExact));
        List<SpiceKind> first = new ArrayList<>(badges); for (SpiceKind kind : other.badges) if (first.size() < 2) first.add(kind);
        long powders = values.keySet().stream().filter(kind -> !kind.accompaniment()).count();
        return new SeasoningAmounts(values, Math.addExact(powderUses, other.powderUses), mixed || other.mixed || powders > 1, first);
    }
    SeasoningAmounts filter(boolean sides) {
        Map<SpiceKind, Long> values = new TreeMap<>(); amounts.forEach((kind, count) -> { if (kind.accompaniment() == sides) values.put(kind, count); });
        return values.isEmpty() ? null : new SeasoningAmounts(values, sides ? 0 : powderUses, !sides && mixed, sides ? badges : Collections.emptyList());
    }
    @Override public SpiceKind get(int index) { return view.get(index); }
    @Override public int size() { return view.size(); }
}
