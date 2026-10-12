package com.xy177.solwarly.diet;

import java.util.*;

/** Quartiles are score boundaries so equal-scoring foods always share a tier. */
public final class FoodQuality {
    public static double[] boundaries(Collection<Double> eligibleScores, double meal, double cuisine, double delicacy) {
        List<Double> sorted = new ArrayList<>();
        for (double score : eligibleScores) if (Double.isFinite(score) && score >= 0) sorted.add(score);
        Collections.sort(sorted);
        double[] result = {meal, cuisine, delicacy};
        for (int i = 0; i < 3; i++) if (result[i] == 0)
            result[i] = sorted.isEmpty() ? Double.POSITIVE_INFINITY : sorted.get(Math.min(sorted.size() - 1, sorted.size() * (i + 1) / 4));
        return result;
    }
    public static int tier(double score, double[] boundaries) {
        for (int i = 2; i >= 0; i--) if (score >= boundaries[i]) return i + 1;
        return 0;
    }
    private FoodQuality() {}
}
