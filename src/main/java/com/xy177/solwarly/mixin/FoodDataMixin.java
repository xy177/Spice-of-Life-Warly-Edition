package com.xy177.solwarly.mixin;

import com.xy177.solwarly.diet.WarlyNutrition;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(Player.class)
public abstract class FoodDataMixin {
    @Redirect(method="eat",at=@At(value="INVOKE",target="Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"))
    private void nutrition(FoodData data,FoodProperties properties,Level level,ItemStack food,FoodProperties original) {
        WarlyNutrition.restore((Player)(Object)this,food);
    }
}
