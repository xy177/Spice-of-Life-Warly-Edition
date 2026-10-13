package com.xy177.solwarly.diet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/** Natural elapsed ticks plus successful sleep skips; time commands never add elapsed time. */
public final class WarlyClockData extends SavedData {
    private long elapsedTicks,lastTotalTime=Long.MIN_VALUE;
    private boolean syncPending;
    public static WarlyClockData get(Level level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(WarlyClockData::load,WarlyClockData::new,"solwarly_clock");
    }
    public long advance(Level level) {
        long total=level.getGameTime();
        if(lastTotalTime!=Long.MIN_VALUE&&total>lastTotalTime) { elapsedTicks+=total-lastTotalTime;setDirty(); }
        lastTotalTime=total;return elapsedTicks;
    }
    public void recordSleep(long skipped) {
        if(skipped>0) { elapsedTicks+=skipped;syncPending=true;setDirty(); }
    }
    public boolean consumePendingSync() { boolean result=syncPending;syncPending=false;return result; }
    private static WarlyClockData load(CompoundTag tag) {
        WarlyClockData data=new WarlyClockData();data.elapsedTicks=Math.max(0,tag.getLong("elapsedTicks"));
        data.lastTotalTime=tag.contains("lastTotalTime")?tag.getLong("lastTotalTime"):Long.MIN_VALUE;return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        tag.putLong("elapsedTicks",elapsedTicks);tag.putLong("lastTotalTime",lastTotalTime);return tag;
    }
}
