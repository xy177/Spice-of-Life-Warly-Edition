// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/412fa94a07d93f09378fc0e3393de38eb0099824/src/main/java/com/cazsius/solcarrot/communication/FoodListMessage.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
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

public record FoodListMessage(CompoundTag capabilityNBT,CompoundTag dietNBT) implements CustomPacketPayload {
    public static final Type<FoodListMessage> ID=new Type<>(ResourceLocation.fromNamespaceAndPath("solcarrot","food_list_message"));
    public static final StreamCodec<RegistryFriendlyByteBuf,FoodListMessage> CODEC=CustomPacketPayload.codec(FoodListMessage::write,FoodListMessage::new);
    public FoodListMessage(Player player) { this(FoodList.get(player).serializeNBT(),com.xy177.solwarly.diet.DietRuntime.snapshot(player)); }
    private FoodListMessage(RegistryFriendlyByteBuf buffer) { this(buffer.readNbt(),buffer.readNbt()); }
    private void write(RegistryFriendlyByteBuf buffer) { buffer.writeNbt(capabilityNBT);buffer.writeNbt(dietNBT); }
    @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("solwarly").playToClient(ID,CODEC,FoodListMessage::handle);
    }
    private static void handle(FoodListMessage message,IPayloadContext context) {
        context.enqueueWork(()-> {
            Player player=context.player();
            FoodList.get(player).deserializeNBT(message.capabilityNBT);
            com.xy177.solwarly.diet.DietRuntime.readSnapshot(player,message.dietNBT);
        });
    }
}
