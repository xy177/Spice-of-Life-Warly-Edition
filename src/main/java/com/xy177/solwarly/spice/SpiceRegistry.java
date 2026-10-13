package com.xy177.solwarly.spice;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid=SOLWarly.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class SpiceRegistry {
    private static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,SOLWarly.MOD_ID);
    private static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,SOLWarly.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,SOLWarly.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS=DeferredRegister.create(ForgeRegistries.BLOCKS,SOLWarly.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> ENTITIES=DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,SOLWarly.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPES=DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS,SOLWarly.MOD_ID);
    public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<SeasonFoodRecipe>> SEASON=RECIPES.register("season_food",()->new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(SeasonFoodRecipe::new));
    public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<MixedSpiceRecipe>> MIXED=RECIPES.register("mixed_spice",()->new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(MixedSpiceRecipe::new));
    public static final net.minecraftforge.registries.RegistryObject<com.xy177.solwarly.block.BlockSpiceGrinder> GRINDER=BLOCKS.register("spice_grinder",com.xy177.solwarly.block.BlockSpiceGrinder::new);
    public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<TileEntitySpiceGrinder>> GRINDER_ENTITY=ENTITIES.register("spice_grinder",()->net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(TileEntitySpiceGrinder::new,GRINDER.get()).build(null));
    public static ItemSpice spice;
    public static PotionSpice sweet,tasty,aromatic,hot,appetizing;
    public static final SoundEvent grinderWorking=SoundEvent.createVariableRangeEvent(new ResourceLocation("solwarly:spice_grinder_working")),
        grinderComplete=SoundEvent.createVariableRangeEvent(new ResourceLocation("solwarly:spice_grinder_complete"));
    static {
        ITEMS.register("spice",()->spice=new ItemSpice());
        ITEMS.register("spice_grinder",()->new net.minecraft.world.item.BlockItem(GRINDER.get(),new Item.Properties()));
        EFFECTS.register("sweet",()->sweet=new PotionSpice(SpiceKind.SUGAR));EFFECTS.register("tasty",()->tasty=new PotionSpice(SpiceKind.SALT));EFFECTS.register("aromatic",()->aromatic=new PotionSpice(SpiceKind.ONION));
        EFFECTS.register("hot",()->hot=new PotionSpice(SpiceKind.SPICY));EFFECTS.register("appetizing",()->appetizing=new PotionSpice(SpiceKind.PICKLE));
        SOUNDS.register("spice_grinder_working",()->grinderWorking);SOUNDS.register("spice_grinder_complete",()->grinderComplete);
    }
    public static void setUp(IEventBus bus) { BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);EFFECTS.register(bus);SOUNDS.register(bus);RECIPES.register(bus); }
    @SubscribeEvent public static void creative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey()==CreativeModeTabs.FUNCTIONAL_BLOCKS)event.accept(GRINDER.get());
        if(event.getTabKey()==CreativeModeTabs.FOOD_AND_DRINKS)for(SpiceKind kind:SpiceKind.all())
            if(!kind.nativeMaterial()&&kind.available())event.accept(Seasoning.powder(java.util.List.of(kind),1));
    }
    private SpiceRegistry() {}
}
