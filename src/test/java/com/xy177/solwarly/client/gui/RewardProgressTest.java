package com.xy177.solwarly.client.gui;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class RewardProgressTest {
    @Test public void targetsUseActualNextTierAndHighestTierWithoutInventingZeroTier() {
        List<Double> tiers = Arrays.asList(20.,50.,90.);
        RewardProgress first = new RewardProgress(10, tiers);
        assertNull(first.currentTier); assertEquals(20, first.next, 0); assertEquals(90, first.maximum, 0);
        RewardProgress second = new RewardProgress(35, tiers);
        assertEquals(20, second.currentTier, 0); assertEquals(50, second.next, 0); assertEquals(90, second.maximum, 0);
    }
    @Test public void exactTierAdvancesAndFinalUpcomingTierCanAlsoBeMaximum() {
        RewardProgress progress = new RewardProgress(15, Arrays.asList(5.,10.,15.,25.,40.));
        assertEquals(15, progress.currentTier, 0); assertEquals(25, progress.next, 0); assertEquals(40, progress.maximum, 0);
        RewardProgress last = new RewardProgress(25, Arrays.asList(5.,10.,15.,25.,40.));
        assertEquals(last.next, last.maximum);
    }
    @Test public void maximumHasNoNextTierAndEmptyRewardsHaveNoTargets() {
        for (double score : new double[]{90,100}) {
            RewardProgress progress = new RewardProgress(score, Arrays.asList(20.,50.,90.));
            assertEquals(90, progress.currentTier, 0); assertNull(progress.next); assertEquals(90, progress.maximum, 0);
        }
        RewardProgress empty = new RewardProgress(0, Collections.emptyList());
        assertNull(empty.currentTier); assertNull(empty.next); assertNull(empty.maximum);
    }
}
