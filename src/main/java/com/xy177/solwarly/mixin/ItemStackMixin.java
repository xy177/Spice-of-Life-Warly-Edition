package com.xy177.solwarly.mixin;

import com.xy177.solwarly.spice.SpiceEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method="finishUsingItem",at=@At("HEAD"))
    private void beforeMeal(Level level,LivingEntity entity,CallbackInfoReturnable<ItemStack> ci) {
        ItemStack food=(ItemStack)(Object)this;
        if(entity instanceof Player player&&food.isEdible())SpiceEffects.begin(player,food);
    }
    @Inject(method="finishUsingItem",at=@At("RETURN"))
    private void afterMeal(Level level,LivingEntity entity,CallbackInfoReturnable<ItemStack> ci) {
        if(entity instanceof Player player)SpiceEffects.finish(player);
    }
}
