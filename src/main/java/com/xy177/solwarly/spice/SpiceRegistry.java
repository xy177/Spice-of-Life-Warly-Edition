package com.xy177.solwarly.spice;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid=SOLWarly.MOD_ID,bus=EventBusSubscriber.Bus.MOD)
public final class SpiceRegistry {
    private static final DeferredRegister<Item> ITEMS=DeferredRegister.create(net.minecraft.core.registries.Registries.ITEM,SOLWarly.MOD_ID);
    private static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(net.minecraft.core.registries.Registries.MOB_EFFECT,SOLWarly.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(net.minecraft.core.registries.Registries.SOUND_EVENT,SOLWarly.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS=DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK,SOLWarly.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> ENTITIES=DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE,SOLWarly.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPES=DeferredRegister.create(net.minecraft.core.registries.Registries.RECIPE_SERIALIZER,SOLWarly.MOD_ID);
    public static final java.util.function.Supplier<net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<SeasonFoodRecipe>> SEASON=RECIPES.register("season_food",()->new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(SeasonFoodRecipe::new));
    public static final java.util.function.Supplier<net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<MixedSpiceRecipe>> MIXED=RECIPES.register("mixed_spice",()->new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(MixedSpiceRecipe::new));
    public static final java.util.function.Supplier<com.xy177.solwarly.block.BlockSpiceGrinder> GRINDER=BLOCKS.register("spice_grinder",com.xy177.solwarly.block.BlockSpiceGrinder::new);
    public static final java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<TileEntitySpiceGrinder>> GRINDER_ENTITY=ENTITIES.register("spice_grinder",()->net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(TileEntitySpiceGrinder::new,GRINDER.get()).build(null));
    public static ItemSpice spice;
    public static PotionSpice sweet,tasty,aromatic,hot,appetizing;
    public static final SoundEvent grinderWorking=SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.parse("solwarly:spice_grinder_working")),
        grinderComplete=SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.parse("solwarly:spice_grinder_complete"));
    static {
        ITEMS.register("spice",()->spice=new ItemSpice());
        ITEMS.register("spice_grinder",()->new net.minecraft.world.item.BlockItem(GRINDER.get(),new Item.Properties()));
        EFFECTS.register("sweet",()->sweet=new PotionSpice(SpiceKind.SUGAR));EFFECTS.register("tasty",()->tasty=new PotionSpice(SpiceKind.SALT));EFFECTS.register("aromatic",()->aromatic=new PotionSpice(SpiceKind.ONION));
        EFFECTS.register("hot",()->hot=new PotionSpice(SpiceKind.SPICY));EFFECTS.register("appetizing",()->appetizing=new PotionSpice(SpiceKind.PICKLE));
        SOUNDS.register("spice_grinder_working",()->grinderWorking);SOUNDS.register("spice_grinder_complete",()->grinderComplete);
    }
    public static void setUp(IEventBus bus) { SpiceKindIngredient.register(bus); BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);EFFECTS.register(bus);SOUNDS.register(bus);RECIPES.register(bus); }
    @SubscribeEvent public static void creative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey()==CreativeModeTabs.FUNCTIONAL_BLOCKS)event.accept(GRINDER.get());
        if(event.getTabKey()==CreativeModeTabs.FOOD_AND_DRINKS)for(SpiceKind kind:SpiceKind.all())
            if(!kind.nativeMaterial()&&kind.available())event.accept(Seasoning.powder(java.util.List.of(kind),1));
    }
    @SubscribeEvent public static void capabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,GRINDER_ENTITY.get(),TileEntitySpiceGrinder::handler);
    }
    private SpiceRegistry() {}
}
