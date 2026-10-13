package com.xy177.solwarly.diet;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.registry.RegistryLookup;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import java.lang.reflect.Method;
import java.util.*;

public final class FarmerRecipes {
    public static void addServings(PreparationGraph graph) {
        if(!ModList.get().isLoaded("farmersdelight"))return;
        for(var block:net.minecraft.core.registries.BuiltInRegistries.BLOCK) {
            try {
                Method serving;
                try { serving=block.getClass().getMethod("getServingItem",BlockState.class); }
                catch(NoSuchMethodException absent) {
                    try { serving=block.getClass().getMethod("getPieSliceItem"); }
                    catch(NoSuchMethodException unsupported) { continue; }
                }
                ItemStack part=(ItemStack)(serving.getParameterCount()==0?serving.invoke(block):serving.invoke(block,block.defaultBlockState()));
                Item item=block.asItem();
                if(item==Items.AIR&&RegistryLookup.id(block).toString().equals("farmersdelight:pumpkin_pie"))item=Items.PUMPKIN_PIE;
                if(item==Items.AIR||part.isEmpty())continue;
                graph.add(FoodCatalog.id(part),"serving:"+RegistryLookup.id(block),DietConfig.servingWeight,List.of(List.of(RegistryLookup.id(item).toString())));
            }catch(ReflectiveOperationException|RuntimeException e) { SOLWarly.LOGGER.warn("Cannot read feast serving for {}",RegistryLookup.id(block),e); }
        }
    }
    private FarmerRecipes() {}
}
