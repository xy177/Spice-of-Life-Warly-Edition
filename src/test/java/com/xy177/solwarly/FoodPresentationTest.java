package com.xy177.solwarly;

import com.xy177.solwarly.diet.FoodQuality;
import com.xy177.solwarly.client.TooltipHandler;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class FoodPresentationTest {
    @Test public void distinctScoresSplitIntoFourEqualGroups() {
        List<Double> scores = Arrays.asList(1.,2.,3.,4.,5.,6.,7.,8.);
        double[] boundaries = FoodQuality.boundaries(scores, 0, 0, 0);
        int[] counts = new int[4];
        for (double score : scores) counts[FoodQuality.tier(score, boundaries)]++;
        assertArrayEquals(new int[] {2,2,2,2}, counts);
    }
    @Test public void tiesCrossingQuartilesAlwaysRemainInOneTier() {
        double[] boundaries = FoodQuality.boundaries(Arrays.asList(1.,2.,2.,2.,2.,2.,3.,4.),0,0,0);
        assertArrayEquals(new double[] {2,2,3}, boundaries, 0);
        for (int i = 0; i < 5; i++) assertEquals(2, FoodQuality.tier(2, boundaries));
        double[] identical = FoodQuality.boundaries(Collections.nCopies(100, 4.),0,0,0);
        assertEquals(3, FoodQuality.tier(4, identical));
    }
    @Test public void manualBoundariesOverrideOnlyTheirConfiguredEntries() {
        double[] boundaries = FoodQuality.boundaries(Arrays.asList(1.,2.,3.,4.,5.,6.,7.,8.), 2.5, 0, 9);
        assertArrayEquals(new double[] {2.5,5,9}, boundaries, 0);
        assertEquals(0, FoodQuality.tier(2.4, boundaries));
        assertEquals(1, FoodQuality.tier(2.5, boundaries));
        assertEquals(2, FoodQuality.tier(8, boundaries));
    }
    @Test public void historyMessagesDistinguishFoodFlavorAndEligibility() {
        double[] b = {2,5,8};
        assertEquals("untasted", TooltipHandler.historyKey(false,false,false,1,b));
        assertEquals("unhelpful", TooltipHandler.historyKey(true,false,false,1,b));
        assertEquals("new_flavor", TooltipHandler.historyKey(true,true,false,12,b));
        assertEquals("quality.0", TooltipHandler.historyKey(true,true,true,1,b));
        assertEquals("quality.3", TooltipHandler.historyKey(true,true,true,8,b));
    }
}
