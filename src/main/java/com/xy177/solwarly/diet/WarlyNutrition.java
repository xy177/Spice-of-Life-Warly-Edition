package com.xy177.solwarly.diet;

import com.xy177.solwarly.spice.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class WarlyNutrition {
    public record Values(int hunger,float saturation) {}
    public static Values values(Player player,ItemStack food) {
        var properties=food.getFoodProperties(player);
        if(properties==null)return new Values(0,0);
        double hunger=properties.getNutrition(),saturation=hunger*properties.getSaturationModifier()*2;
        var kinds = Seasoning.effectKinds(Seasoning.components(food));
        for (SpiceKind kind : new java.util.LinkedHashSet<>(kinds)) {
            long copies = Seasoning.amount(kinds, kind);
            if (kind instanceof CustomSpice && kind.accompaniment()) {
                hunger += ((CustomSpice) kind).hungerBonus * copies; saturation += ((CustomSpice) kind).saturationBonus * copies;
            }
            if (kind == SpiceKind.RICE) { hunger += SpiceConfig.riceHunger * copies; saturation += SpiceConfig.riceSaturation * copies; }
            if (kind == SpiceKind.TRAVARICE) { hunger += ImmortalSpices.travariceHunger * copies; saturation += ImmortalSpices.travariceSaturation * copies; }
        }
        var appetizing=player.getEffect(SpiceRegistry.appetizing);
        if(appetizing!=null) {
            double scale=1+SpiceConfig.appetiteBase+SpiceConfig.appetitePerLevel*(appetizing.getAmplifier()+1);
            hunger=Math.round(hunger*scale);saturation*=scale;
        }
        double repeat=DietRuntime.warlyMultiplier(player,food);
        return new Values(Seasoning.ticks(hunger*repeat),(float)Math.min(Float.MAX_VALUE,saturation*repeat));
    }
    private WarlyNutrition() {}
}
