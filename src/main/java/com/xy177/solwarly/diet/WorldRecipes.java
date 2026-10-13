package com.xy177.solwarly.diet;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Administrator-only analysis of the recipe manager including world datapacks and scripts. */
public final class WorldRecipes {
    public static void add(PreparationGraph graph,MinecraftServer server) {
        for(var holder:server.getRecipeManager().getRecipes()) {
            var recipe=holder.value();
            ItemStack output=recipe.getResultItem(server.registryAccess());if(output.isEmpty())continue;
            String type=recipe.getType().toString();
            double weight=switch(type) {
                case "minecraft:crafting" -> DietConfig.craftWeight;
                case "minecraft:smelting","minecraft:blasting","minecraft:smoking","minecraft:campfire_cooking" -> DietConfig.furnaceWeight;
                case "farmersdelight:cooking" -> DietConfig.cookingWeight;
                case "farmersdelight:cutting","minecraft:stonecutting" -> DietConfig.cuttingWeight;
                default -> -1;
            };
            if(weight<0)continue;List<List<String>> inputs=new ArrayList<>();boolean invalid=false;
            for(var ingredient:recipe.getIngredients()) {
                if(ingredient.isEmpty())continue;List<String> options=new ArrayList<>();
                for(ItemStack stack:ingredient.getItems())if(!stack.isEmpty()&&(!stack.hasCraftingRemainingItem()||(stack.getFoodProperties(null)!=null)))options.add(FoodCatalog.id(stack));
                if(options.isEmpty()&&ingredient.getItems().length==0)invalid=true;
                if(!options.isEmpty())inputs.add(options);
            }
            if(!invalid&&!inputs.isEmpty()) {
                List<ItemStack> outputs=new ArrayList<>();outputs.add(output);
                if(type.equals("farmersdelight:cutting"))try {
                    var results=(Iterable<?>)recipe.getClass().getMethod("getRollableResults").invoke(recipe);outputs.clear();
                    for(var result:results)if(((Number)result.getClass().getMethod("getChance").invoke(result)).doubleValue()>0)
                        outputs.add((ItemStack)result.getClass().getMethod("getStack").invoke(result));
                }catch(ReflectiveOperationException e) { com.xy177.solwarly.SOLWarly.LOGGER.warn("Cannot read cutting outputs for {}",holder.id(),e); }
                for(ItemStack result:outputs)if(!result.isEmpty())graph.add(FoodCatalog.id(result),"recipe:"+holder.id(),weight,inputs);
            }
        }
    }
    private WorldRecipes() {}
}
