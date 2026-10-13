// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/37c243c5bcf50d5582ce81596e85b3f465b222a8/src/main/java/com/cazsius/solcarrot/tracking/FoodTracker.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.tracking;

import com.xy177.solwarly.SOLWarlyConfig;
import com.xy177.solwarly.diet.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import static com.xy177.solwarly.lib.Localization.*;

public final class FoodTracker {
    public static void complete(Player player,ItemStack food) {
        if(player.level().isClientSide||SOLWarlyConfig.limitProgressionToSurvival()&&player instanceof net.minecraft.server.level.ServerPlayer server&&server.gameMode.getGameModeForPlayer()!=net.minecraft.world.level.GameType.SURVIVAL)return;
        DietRuntime.initialize(player);DietData data=DietCapability.get(player);
        double before=DietRewards.cumulativeHealth(data.cumulative());
        boolean first=FoodList.get(player).addFood(food.getItem());
        DietRuntime.eat(player,food);CapabilityHandler.syncFoodList(player);
        double after=DietRewards.cumulativeHealth(data.cumulative());ServerLevel level=(ServerLevel)player.level();
        if(after>before) {
            if(SOLWarlyConfig.shouldPlayMilestoneSounds())level.playSound(null,player.blockPosition(),SoundEvents.PLAYER_LEVELUP,SoundSource.PLAYERS,1,1);
            if(SOLWarlyConfig.shouldSpawnMilestoneParticles())level.sendParticles(ParticleTypes.HEART,player.getX(),player.getEyeY(),player.getZ(),12,.5,.5,.5,0);
            var hearts=localizedQuantityComponent("message","hearts",(int)((after-before)/2));
            player.displayClientMessage(localizedComponent("message",DietRewards.reachedFinalCumulativeTier(data.cumulative())?"finished.hotbar":"milestone_achieved",hearts),SOLWarlyConfig.shouldShowProgressAboveHotbar());
        }else if(first&&SOLWarlyConfig.shouldSpawnIntermediateParticles())level.sendParticles(ParticleTypes.END_ROD,player.getX(),player.getEyeY(),player.getZ(),12,.5,.5,.5,0);
    }
    private FoodTracker() {}
}
