package com.xy177.solwarly;

import com.xy177.solwarly.diet.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class WarlyModeTest {
    @Test public void repeatRatiosExpireAndSurviveStorage() {
        DietData data=new DietData();data.setWarlyOverride(1);
        double[] expected={1,.9,.8,.65,.5,.3,.3};
        for(double ratio:expected) {
            assertEquals(ratio,data.warlyMultiplier("minecraft:apple",100),0);
            data.recordWarlyMeal("minecraft:apple",100);
        }
        DietData restored=new DietData();restored.deserializeNBT(data.serializeNBT());
        assertEquals(.3,restored.warlyMultiplier("minecraft:apple",48099),0);
        assertEquals(1,restored.warlyMultiplier("minecraft:apple",48100),0);
        assertEquals(.3,restored.warlyMultiplier("minecraft:apple",50),0);
        restored.setWarlyOverride(0);assertEquals(1,restored.warlyMultiplier("minecraft:apple",100),0);
        restored.setWarlyOverride(1);assertEquals(.3,restored.warlyMultiplier("minecraft:apple",100),0);
    }
    @Test public void deathResetPreservesPersonalModeAndRepeatMemory() {
        DietData data=new DietData();data.setWarlyOverride(1);data.recordWarlyMeal("minecraft:apple",50);
        data.eat("minecraft:apple","plain",4,4,32);data.legacyHealthFloor=12;
        data.reset(true);
        assertEquals(0,data.cumulative(),0);assertEquals(0,data.mealCount());assertEquals(0,data.legacyHealthFloor,0);
        assertEquals(1,data.warlyOverride());assertEquals(.9,data.warlyMultiplier("minecraft:apple",50),0);
    }
    @Test public void durationScalesWithoutChangingFoodExperience() {
        assertEquals(32,DietRuntime.scaledUseDuration(32,1));
        assertEquals(36,DietRuntime.scaledUseDuration(32,.9));
        assertEquals(107,DietRuntime.scaledUseDuration(32,.3));
        DietData data=new DietData();data.eat("minecraft:apple","plain",4,1.2,32);
        assertEquals(4,data.cumulative(),0);assertEquals(1.2,data.average(),0);
    }
}
