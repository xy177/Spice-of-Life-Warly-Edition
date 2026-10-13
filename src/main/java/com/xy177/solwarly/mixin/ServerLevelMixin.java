package com.xy177.solwarly.mixin;

import com.xy177.solwarly.diet.WarlyClockData;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Redirect(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V"))
    private void sleepTime(ServerLevel level,long time) {
        if(level==level.getServer().overworld())WarlyClockData.get(level).recordSleep(time-level.getDayTime());
        level.setDayTime(time);
    }
}
