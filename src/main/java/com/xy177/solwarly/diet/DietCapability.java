package com.xy177.solwarly.diet;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid=SOLWarly.MOD_ID)
public final class DietCapability implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<DietData> TYPE=CapabilityManager.get(new CapabilityToken<>() {});
    private final DietData data=new DietData();
    private final LazyOptional<DietData> optional=LazyOptional.of(()->data);
    public static DietData get(Player player) { return player.getCapability(TYPE).orElse(null); }
    @SubscribeEvent public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof Player) {
            DietCapability provider=new DietCapability();
            event.addCapability(new ResourceLocation("solwarly:diet"),provider);
            event.addListener(provider.optional::invalidate);
        }
    }
    @Mod.EventBusSubscriber(modid=SOLWarly.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent public static void register(RegisterCapabilitiesEvent event) { event.register(DietData.class); }
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,@Nullable Direction side) {
        return capability==TYPE ? optional.cast() : LazyOptional.empty();
    }
    @Override public CompoundTag serializeNBT() { return data.serializeNBT(); }
    @Override public void deserializeNBT(CompoundTag tag) { data.deserializeNBT(tag); }
}
