package com.xy177.solwarly.diet;

import net.minecraft.util.FoodStats;
import squeek.applecore.api.food.FoodEvent;
import squeek.applecore.api.food.FoodValues;

/** Scale actual saturation independently of rounded integer hunger. */
public final class WarlyNutrition {
    public static FoodValues scale(FoodValues values, double multiplier) {
        if (multiplier == 1) return values;
        int hunger = com.xy177.solwarly.spice.Seasoning.hunger(values.hunger * multiplier);
        float saturation = (float) (values.getUnboundedSaturationIncrement() * multiplier);
        return hunger > 0 ? new FoodValues(hunger, saturation / (2.0F * hunger)) : new ZeroHungerValues(saturation);
    }

    public static void restoreZeroHungerSaturation(FoodEvent.FoodEaten event) {
        if (!(event.foodValues instanceof ZeroHungerValues) || event.player.world.isRemote) return;
        ZeroHungerValues values = (ZeroHungerValues) event.foodValues;
        FoodStats stats = event.player.getFoodStats();
        float before = stats.getSaturationLevel();
        stats.setFoodSaturationLevel(Math.min(stats.getFoodLevel(), before + values.saturation));
        values.restored = stats.getSaturationLevel() - before;
    }

    public static float saturationAdded(FoodEvent.FoodEaten event) {
        return event.saturationAdded + (event.foodValues instanceof ZeroHungerValues ? ((ZeroHungerValues) event.foodValues).restored : 0);
    }

    private static final class ZeroHungerValues extends FoodValues {
        final float saturation;
        float restored;
        ZeroHungerValues(float saturation) { super(0, 0); this.saturation = saturation; }
        @Override public float getUnboundedSaturationIncrement() { return saturation; }
    }
    private WarlyNutrition() {}
}
