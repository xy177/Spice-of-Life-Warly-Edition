package com.xy177.solwarly.diet;

import com.xy177.solwarly.tracking.FoodList;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.*;
import java.util.function.Supplier;

/** Retains the upstream 1.21 attachment identity and its serialized foodList. */
public final class DietCapability {
    private static final DeferredRegister<AttachmentType<?>> LEGACY=DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES,"solcarrot");
    private static final DeferredRegister<AttachmentType<?>> TYPES=DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES,"solwarly");
    public static final Supplier<AttachmentType<FoodList>> FOOD=LEGACY.register("food",()->AttachmentType.serializable(FoodList::new).build());
    public static final Supplier<AttachmentType<DietData>> DIET=TYPES.register("diet",()->AttachmentType.serializable(DietData::new).build());
    public static void setUp(IEventBus bus) { LEGACY.register(bus);TYPES.register(bus); }
    public static DietData get(Player player) { return player.getData(DIET); }
    private DietCapability() {}
}
