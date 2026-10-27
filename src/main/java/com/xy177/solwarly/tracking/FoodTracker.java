// Warly meal tracking and experience celebrations, by xy177, 2026-10-27; LGPL-2.1.
package com.xy177.solwarly.tracking;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.SOLWarlyConfig;
import com.xy177.solwarly.diet.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.text.*;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import squeek.applecore.api.food.FoodEvent;
import static com.xy177.solwarly.lib.Localization.*;

@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID)
public final class FoodTracker {
    @SubscribeEvent
    public static void onFoodEaten(FoodEvent.FoodEaten event) {
        EntityPlayer player = event.player;
        if (player.world.isRemote) return;
        DietRuntime.initialize(player);
        DietData data = DietCapability.get(player);
        double before = DietRewards.cumulativeHealth(data.cumulative());
        boolean first = FoodList.get(player).addFood(event.food);
        DietRuntime.eat(player, event.food);
        CapabilityHandler.syncFoodList(player);
        double gained = DietRewards.cumulativeHealth(data.cumulative()) - before;
        if (gained > 0) {
            boolean finished = DietRewards.reachedFinalCumulativeTier(data.cumulative());
            if (SOLWarlyConfig.shouldPlayMilestoneSounds)
                player.world.playSound(null, player.posX, player.posY, player.posZ,
                    SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1, 1);
            if (SOLWarlyConfig.shouldSpawnMilestoneParticles) {
                particles(player, EnumParticleTypes.HEART, 12);
                if (finished) particles(player, EnumParticleTypes.VILLAGER_HAPPY, 16);
            }
            ITextComponent hearts = localizedQuantityComponent("message", "hearts", (int)(gained/2));
            if (SOLWarlyConfig.shouldShowProgressAboveHotbar)
                player.sendStatusMessage(localizedComponent("message", finished ? "finished.hotbar" : "milestone_achieved", hearts), true);
            else {
                chat(player, "milestone_achieved", TextFormatting.DARK_AQUA, hearts);
                if (finished) chat(player, "finished.chat", TextFormatting.GOLD);
            }
        } else if (first && SOLWarlyConfig.shouldSpawnIntermediateParticles) {
            particles(player, EnumParticleTypes.END_ROD, 12);
        }
    }

    private static void particles(EntityPlayer player, EnumParticleTypes type, int count) {
        ((WorldServer)player.world).spawnParticle(type, player.posX, player.posY+player.getEyeHeight(), player.posZ,
            count, .5, .5, .5, 0);
    }

    private static void chat(EntityPlayer player, String key, TextFormatting color, Object... arguments) {
        ITextComponent message = localizedComponent("message", "chat_wrapper", localizedComponent("message", key, arguments));
        message.setStyle(new Style().setColor(color));
        player.sendStatusMessage(message, false);
    }
    private FoodTracker() {}
}
