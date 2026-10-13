package com.xy177.solwarly.mixin;

import com.xy177.solwarly.diet.WarlyNutrition;
import com.xy177.solwarly.spice.SpiceEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @Inject(method="eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)V",at=@At("HEAD"),cancellable=true,remap=false)
    private void nutrition(Item item,ItemStack stack,LivingEntity entity,CallbackInfo ci) {
        if(!(entity instanceof Player player)||!stack.isEdible())return;
        SpiceEffects.ensureMeal(player,stack);
        var values=WarlyNutrition.values(player,stack);FoodData data=(FoodData)(Object)this;
        float before=data.getSaturationLevel();
        data.setFoodLevel((int)Math.min(20L,(long)data.getFoodLevel()+values.hunger()));
        data.setSaturation(Math.min(data.getFoodLevel(),before+values.saturation()));
        if(!player.level().isClientSide)SpiceEffects.saturation(player,data.getSaturationLevel()-before);
        ci.cancel();
    }
}
