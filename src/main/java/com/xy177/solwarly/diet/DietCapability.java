package com.xy177.solwarly.diet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.*;

public final class DietCapability implements ICapabilitySerializable<NBTTagCompound> {
    @CapabilityInject(DietData.class) public static Capability<DietData> TYPE;
    private final DietData data = new DietData();
    public static void register() {
        CapabilityManager.INSTANCE.register(DietData.class, new Capability.IStorage<DietData>() {
            public NBTBase writeNBT(Capability<DietData> capability, DietData data, EnumFacing side) { return data.serializeNBT(); }
            public void readNBT(Capability<DietData> capability, DietData data, EnumFacing side, NBTBase tag) { data.deserializeNBT((NBTTagCompound) tag); }
        }, DietData::new);
    }
    public static DietData get(EntityPlayer player) { return TYPE == null ? null : player.getCapability(TYPE, null); }
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) { return capability == TYPE; }
    @SuppressWarnings("unchecked") public <T> T getCapability(Capability<T> capability, EnumFacing facing) { return capability == TYPE ? (T) data : null; }
    public NBTTagCompound serializeNBT() { return data.serializeNBT(); }
    public void deserializeNBT(NBTTagCompound tag) { data.deserializeNBT(tag); }
}
