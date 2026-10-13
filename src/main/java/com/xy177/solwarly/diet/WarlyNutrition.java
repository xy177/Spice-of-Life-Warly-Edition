package com.xy177.solwarly.diet;

import com.xy177.solwarly.spice.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class WarlyNutrition {
    public record Values(int hunger,float saturation) {}
    public static Values values(Player player,ItemStack food) {
        var properties=food.getFoodProperties(player);
        if(properties==null)return new Values(0,0);
        double hunger=properties.nutrition(),saturation=properties.saturation();
        var kinds = Seasoning.effectKinds(Seasoning.components(food));
        for (SpiceKind kind : new java.util.LinkedHashSet<>(kinds)) {
            long copies = Seasoning.amount(kinds, kind);
            if (kind instanceof CustomSpice && kind.accompaniment()) {
                hunger += ((CustomSpice) kind).hungerBonus * copies; saturation += ((CustomSpice) kind).saturationBonus * copies;
            }
            if (kind == SpiceKind.RICE) { hunger += SpiceConfig.riceHunger * copies; saturation += SpiceConfig.riceSaturation * copies; }
            if (kind == SpiceKind.TRAVARICE) { hunger += ImmortalSpices.travariceHunger * copies; saturation += ImmortalSpices.travariceSaturation * copies; }
        }
        var appetizing=player.getEffect(SpiceKind.PICKLE.potion());
        if(appetizing!=null) {
            double scale=1+SpiceConfig.appetiteBase+SpiceConfig.appetitePerLevel*(appetizing.getAmplifier()+1);
            hunger=Math.round(hunger*scale);saturation*=scale;
        }
        double repeat=DietRuntime.warlyMultiplier(player,food);
        return new Values(Seasoning.ticks(hunger*repeat),(float)Math.min(Float.MAX_VALUE,saturation*repeat));
    }
    public static void restore(Player player,ItemStack food) {
        SpiceEffects.ensureMeal(player,food);
        Values values=values(player,food);var data=player.getFoodData();float before=data.getSaturationLevel();
        data.setFoodLevel((int)Math.min(20L,(long)data.getFoodLevel()+values.hunger()));
        data.setSaturation(Math.min(data.getFoodLevel(),before+values.saturation()));
        if(!player.level().isClientSide)SpiceEffects.saturation(player,data.getSaturationLevel()-before);
    }
    private WarlyNutrition() {}
}
