package com.xy177.solwarly.mixin;

import com.xy177.solwarly.diet.WarlyClockData;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Unique private long solwarly$dayTimeAtTickStart;
    @Unique private long solwarly$gameTimeAtTickStart;
    @Unique private boolean solwarly$playerSleepingAtTickStart;

    @Inject(method="tick", at=@At("HEAD"))
    private void solwarly$captureSleepClock(CallbackInfo callback) {
        ServerLevel level = (ServerLevel) (Object) this;
        solwarly$dayTimeAtTickStart = level.getDayTime();
        solwarly$gameTimeAtTickStart = level.getGameTime();
        solwarly$playerSleepingAtTickStart = level.players().stream().anyMatch(player -> player.isSleeping());
    }

    @Inject(method="tick", at=@At("TAIL"))
    private void solwarly$recordSleepTimeSkip(CallbackInfo callback) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (level != level.getServer().overworld() || !solwarly$playerSleepingAtTickStart) return;
        long dayTimeAdvance = level.getDayTime() - solwarly$dayTimeAtTickStart;
        long gameTimeAdvance = level.getGameTime() - solwarly$gameTimeAtTickStart;
        long skipped = dayTimeAdvance - gameTimeAdvance;
        if (skipped > 0) WarlyClockData.get(level).recordSleep(skipped);
    }
}
