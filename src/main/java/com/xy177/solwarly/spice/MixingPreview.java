package com.xy177.solwarly.spice;

import java.util.*;

/** Bounded recipe previews; never enumerate the combinatorial space of eight powders. */
public final class MixingPreview {
    public static final int LIMIT = 512;
    public static List<List<SpiceKind>> combinations(List<SpiceKind> kinds) {
        List<List<SpiceKind>> rows = new ArrayList<>();
        enumerate(kinds, 0, new ArrayList<>(), rows);
        if (rows.size() == LIMIT) {
            rows.clear();
            for (SpiceKind kind : kinds) rows.add(Collections.nCopies(SpiceConfig.mixedIngredients, kind));
            Random random = new Random(177);
            for (int i = 0; i < LIMIT; i++) rows.add(sample(kinds, random));
        }
        return rows;
    }
    private static void enumerate(List<SpiceKind> kinds, int first, List<SpiceKind> row, List<List<SpiceKind>> rows) {
        if (rows.size() >= LIMIT) return;
        if (row.size() == SpiceConfig.mixedIngredients) { rows.add(new ArrayList<>(row)); return; }
        for (int i = first; i < kinds.size() && rows.size() < LIMIT; i++) {
            row.add(kinds.get(i)); enumerate(kinds, i, row, rows); row.remove(row.size() - 1);
        }
    }
    public static List<SpiceKind> sample(List<SpiceKind> kinds, Random random) {
        List<SpiceKind> row = new ArrayList<>();
        for (int i = 0; i < SpiceConfig.mixedIngredients; i++) row.add(kinds.get(random.nextInt(kinds.size())));
        return row;
    }
    public static int gridSlot(int component) { return SpiceConfig.mixedIngredients <= 3 ? new int[]{0,1,3}[component] : component; }
    public static int catalystSlot() { return SpiceConfig.mixedIngredients <= 3 ? 4 : SpiceConfig.mixedIngredients; }
    private MixingPreview() {}
}
