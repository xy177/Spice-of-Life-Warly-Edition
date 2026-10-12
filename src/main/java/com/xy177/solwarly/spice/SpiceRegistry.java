package com.xy177.solwarly.spice;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID)
public final class SpiceRegistry {
    public static final ItemSpice spice = new ItemSpice();
    public static final PotionSpice sweet = new PotionSpice(SpiceKind.SUGAR, "sweet");
    public static final PotionSpice tasty = new PotionSpice(SpiceKind.SALT, "tasty");
    public static final PotionSpice aromatic = new PotionSpice(SpiceKind.ONION, "aromatic");
    public static final PotionSpice hot = new PotionSpice(SpiceKind.SPICY, "hot");
    public static final PotionSpice appetizing = new PotionSpice(SpiceKind.PICKLE, "appetizing");
    public static final SoundEvent grinderWorking = sound("spice_grinder_working");
    public static final SoundEvent grinderComplete = sound("spice_grinder_complete");
    static { spice.setRegistryName(SOLWarly.MOD_ID, "spice"); spice.setTranslationKey(SOLWarly.MOD_ID + ".spice"); }

    private static SoundEvent sound(String name) {
        ResourceLocation id = new ResourceLocation(SOLWarly.MOD_ID, name);
        return new SoundEvent(id).setRegistryName(id);
    }

    @SubscribeEvent public static void register(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(spice);
        event.getRegistry().register(new net.minecraft.item.ItemBlock(com.xy177.solwarly.block.BlockSpiceGrinder.INSTANCE)
            .setRegistryName(SOLWarly.MOD_ID, "spice_grinder"));
    }
    @SubscribeEvent public static void preInit(SOLWarly.PreInitializationEvent event) {
        net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity(TileEntitySpiceGrinder.class,
            new net.minecraft.util.ResourceLocation(SOLWarly.MOD_ID, "spice_grinder"));
    }
    private SpiceRegistry() {}
    @SubscribeEvent public static void potions(RegistryEvent.Register<net.minecraft.potion.Potion> event) {
        event.getRegistry().registerAll(sweet, tasty, aromatic, hot, appetizing);
    }
    @SubscribeEvent public static void sounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(grinderWorking);
        event.getRegistry().register(grinderComplete);
    }
    @SubscribeEvent public static void recipes(RegistryEvent.Register<net.minecraft.item.crafting.IRecipe> event) {
        event.getRegistry().register(new SeasonFoodRecipe().setRegistryName(SOLWarly.MOD_ID, "season_food"));
        event.getRegistry().register(new MixedSpiceRecipe().setRegistryName(SOLWarly.MOD_ID, "mixed_spice"));
    }
    @SubscribeEvent public static void postInit(SOLWarly.PostInitializationEvent event) {
        SeasoningPotions.loadFruits();
        BuiltinSpiceRecipes.reload();
        CustomSpices.load(com.xy177.solwarly.diet.DietConfig.directory);
        com.xy177.solwarly.api.FlavorAPI.register(new net.minecraft.util.ResourceLocation("solwarly:seasoning"), Seasoning::flavor);
        com.xy177.solwarly.api.FlavorAPI.registerVariants(new net.minecraft.util.ResourceLocation("solwarly:seasoning"), Seasoning::variants);
    }
}
