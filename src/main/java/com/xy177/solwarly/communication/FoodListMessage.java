// Adapted from Spice of Life: Apple Pie Edition (2d9de8b8d045b1f9b93940b681c74021d326f207); LGPL-2.1.
// Modified by xy177 on 2026-10-27 for Warly Edition.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/communication/FoodListMessage.java
package com.xy177.solwarly.communication;

import com.xy177.solwarly.tracking.FoodList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FoodListMessage(CompoundTag dietNBT) implements CustomPacketPayload {
    public static final Type<FoodListMessage> ID=new Type<>(ResourceLocation.fromNamespaceAndPath("solwarly","food_list_message"));
    public static final StreamCodec<RegistryFriendlyByteBuf,FoodListMessage> CODEC=CustomPacketPayload.codec(FoodListMessage::write,FoodListMessage::new);
    public FoodListMessage(Player player) { this(com.xy177.solwarly.diet.DietRuntime.snapshot(player)); }
    private FoodListMessage(RegistryFriendlyByteBuf buffer) { this(buffer.readNbt()); }
    private void write(RegistryFriendlyByteBuf buffer) { buffer.writeNbt(dietNBT); }
    @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("2").playToClient(ID,CODEC,FoodListMessage::handle);
    }
    private static void handle(FoodListMessage message,IPayloadContext context) {
        context.enqueueWork(()-> {
            Player player=context.player();
            com.xy177.solwarly.diet.DietRuntime.readSnapshot(player,message.dietNBT);
        });
    }
}
