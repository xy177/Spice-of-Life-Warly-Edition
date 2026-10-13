package com.xy177.solwarly.mixin;

import com.xy177.solwarly.spice.SpiceEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Placed pies use the older food overload, which does not supply the eating player. */
@Pseudo
@Mixin(targets="vectorwing.farmersdelight.common.block.PieBlock",remap=false)
public abstract class FarmerPieMixin {
    @Shadow public abstract ItemStack getPieSliceItem();
    @Redirect(method="consumeBite",at=@At(value="INVOKE",target="Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V",remap=false))
    private void eatSlice(FoodData data,net.minecraft.world.food.FoodProperties food,Level level,BlockPos pos,BlockState state,Player player) {
        com.xy177.solwarly.diet.WarlyNutrition.restore(player,getPieSliceItem());
    }
    @Inject(method="consumeBite",at=@At("RETURN"))
    private void finishSlice(Level level,BlockPos pos,BlockState state,Player player,CallbackInfoReturnable<InteractionResult> result) {
        SpiceEffects.finish(player);
    }
}
