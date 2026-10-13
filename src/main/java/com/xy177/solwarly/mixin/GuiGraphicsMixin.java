package com.xy177.solwarly.mixin;

import com.xy177.solwarly.client.SeasoningBadge;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Inject(method="renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V",at=@At("RETURN"))
    private void badge(LivingEntity entity,Level level,ItemStack stack,int x,int y,int seed,int depth,CallbackInfo ci) {
        SeasoningBadge.render((GuiGraphics)(Object)this,stack,x,y);
    }
}
