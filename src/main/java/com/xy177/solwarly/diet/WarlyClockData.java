package com.xy177.solwarly.diet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldSavedData;

/** Persists elapsed natural time while ignoring commands that only change worldTime. */
public final class WarlyClockData extends WorldSavedData {
    private long elapsedTicks;
    private long lastTotalTime = Long.MIN_VALUE;
    private long tickWorldTime = Long.MIN_VALUE;
    private boolean sleepHandled, syncPending;

    public WarlyClockData() { super("solwarly_clock"); }
    public WarlyClockData(String name) { super(name); }

    public static WarlyClockData get(World world) {
        WorldServer overworld = world.getMinecraftServer().getWorld(0);
        WarlyClockData data = (WarlyClockData) overworld.getMapStorage().getOrLoadData(WarlyClockData.class, "solwarly_clock");
        if (data == null) {
            data = new WarlyClockData();
            overworld.getMapStorage().setData("solwarly_clock", data);
        }
        return data;
    }

    public long advance(World world) {
        long total = world.getWorldInfo().getWorldTotalTime();
        if (lastTotalTime == Long.MIN_VALUE) lastTotalTime = total;
        else {
            long delta = total - lastTotalTime;
            if (delta > 0) elapsedTicks += delta;
            lastTotalTime = total;
            if (delta > 0) markDirty();
        }
        return elapsedTicks;
    }

    public void beginTick(World world) {
        tickWorldTime = world.getWorldTime();
        sleepHandled = false;
    }

    /** Count only the time skipped during this tick's successful sleep, once for all sleepers. */
    public void recordSleep(World world) {
        long worldTime = world.getWorldTime();
        if (!sleepHandled && tickWorldTime != Long.MIN_VALUE && worldTime > tickWorldTime
            && worldTime % 24000L == 0 && world.getGameRules().getBoolean("doDaylightCycle")) {
            elapsedTicks += worldTime - tickWorldTime;
            sleepHandled = true;
            syncPending = true;
            markDirty();
        }
    }

    public boolean consumePendingSync() {
        boolean result = syncPending; syncPending = false; return result;
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        elapsedTicks = Math.max(0, tag.getLong("elapsedTicks"));
        lastTotalTime = tag.hasKey("lastTotalTime") ? tag.getLong("lastTotalTime") : Long.MIN_VALUE;
        tickWorldTime = Long.MIN_VALUE; sleepHandled = false; syncPending = false;
    }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setLong("elapsedTicks", elapsedTicks); tag.setLong("lastTotalTime", lastTotalTime);
        return tag;
    }
}
