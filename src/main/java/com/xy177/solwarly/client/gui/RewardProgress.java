package com.xy177.solwarly.client.gui;

import java.util.*;

/** Finds the current reward tier, next target and final target for the book. */
final class RewardProgress {
    final Double currentTier, next, maximum;

    RewardProgress(double score, Collection<Double> thresholds) {
        NavigableSet<Double> tiers = new TreeSet<>(thresholds);
        currentTier = tiers.floor(score);
        next = tiers.higher(score);
        maximum = tiers.isEmpty() ? null : tiers.last();
    }
}
