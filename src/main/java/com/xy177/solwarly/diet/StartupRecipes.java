package com.xy177.solwarly.diet;

import com.google.gson.*;
import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.registry.RegistryLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.resource.ResourcePackLoader;
import java.io.*;
import java.util.*;

/** Reads built-in resources without loading a world or changing live registry tags. */
public final class StartupRecipes {
    private final Map<String,List<String>> tags=new TreeMap<>();
    private final Map<String,List<String>> resolved=new HashMap<>();
    private final Map<ResourceLocation,JsonObject> recipes=new TreeMap<>();
    public static StartupRecipes load() {
        List<PackResources> packs=new ArrayList<>();
        packs.add(ServerPacksSource.createVanillaPackSource());
        ModList.get().getModFiles().forEach(file->packs.add(ResourcePackLoader.createPackForMod(file)));
        StartupRecipes result=new StartupRecipes();
        try(MultiPackResourceManager resources=new MultiPackResourceManager(PackType.SERVER_DATA,packs)) {
            resources.listResourceStacks("tags/items",id->id.getPath().endsWith(".json")).forEach((id,stack)-> {
                String key=id.getNamespace()+":"+id.getPath().substring("tags/items/".length(),id.getPath().length()-5);
                List<String> values=new ArrayList<>();
                for(Resource resource:stack)try(Reader reader=resource.openAsReader()) {
                    JsonObject json=JsonParser.parseReader(reader).getAsJsonObject();
                    if(json.has("replace")&&json.get("replace").getAsBoolean())values.clear();
                    for(JsonElement entry:json.getAsJsonArray("values"))values.add(entry.isJsonPrimitive()?entry.getAsString():entry.getAsJsonObject().get("id").getAsString());
                }catch(IOException|RuntimeException e) { SOLWarly.LOGGER.warn("Cannot read startup item tag {}",id,e); }
                result.tags.put(key,values);
            });
            resources.listResources("recipes",id->id.getPath().endsWith(".json")).forEach((id,resource)-> {
                try(Reader reader=resource.openAsReader()) {
                    result.recipes.put(new ResourceLocation(id.getNamespace(),id.getPath().substring(8,id.getPath().length()-5)),JsonParser.parseReader(reader).getAsJsonObject());
                }catch(IOException|RuntimeException e) { SOLWarly.LOGGER.warn("Cannot read startup recipe {}",id,e); }
            });
        }
        return result;
    }
    public void addTo(PreparationGraph graph) {
        recipes.forEach((id,json)-> {
            try {
                if(!enabled(json))return;
                String type=json.has("type")?json.get("type").getAsString():"";
                double weight=switch(type) {
                    case "minecraft:crafting_shaped","minecraft:crafting_shapeless","forge:ore_shaped","forge:ore_shapeless" -> DietConfig.craftWeight;
                    case "minecraft:smelting","minecraft:blasting","minecraft:smoking","minecraft:campfire_cooking" -> DietConfig.furnaceWeight;
                    case "farmersdelight:cooking" -> DietConfig.cookingWeight;
                    case "farmersdelight:cutting","minecraft:stonecutting" -> DietConfig.cuttingWeight;
                    default -> -1;
                };
                if(weight<0)return;
                List<List<String>> inputs=new ArrayList<>();
                if(json.has("pattern")) {
                    Set<Character> used=new LinkedHashSet<>();
                    for(JsonElement row:json.getAsJsonArray("pattern"))for(char symbol:row.getAsString().toCharArray())if(symbol!=' ')used.add(symbol);
                    for(char symbol:used)inputs.add(ingredient(json.getAsJsonObject("key").get(String.valueOf(symbol))));
                }else if(json.has("ingredients"))for(JsonElement input:json.getAsJsonArray("ingredients"))inputs.add(ingredient(input));
                else if(json.has("ingredient"))inputs.add(ingredient(json.get("ingredient")));
                if(inputs.stream().anyMatch(List::isEmpty))return;
                inputs.removeIf(slot->slot.size()==1&&slot.get(0).equals("!returned"));
                if(inputs.isEmpty())return;
                List<JsonElement> outputs=new ArrayList<>();
                if(json.has("result")) {
                    JsonElement output=json.get("result");
                    if(output.isJsonArray())output.getAsJsonArray().forEach(outputs::add);else outputs.add(output);
                }
                for(JsonElement output:outputs) {
                    if(output.isJsonObject()&&output.getAsJsonObject().has("item")) {
                        JsonObject spec=output.getAsJsonObject();
                        if(spec.has("chance")&&spec.get("chance").getAsDouble()<=0)continue;
                    }
                    String name=output.isJsonPrimitive()?output.getAsString():output.getAsJsonObject().get("item").getAsString();
                    if(RegistryLookup.item(name)!=null)graph.add(name,"recipe:"+id,weight,inputs);
                }
            }catch(RuntimeException e) { SOLWarly.LOGGER.warn("Skipping unsupported startup recipe {}: {}",id,e.getMessage()); }
        });
    }
    private List<String> ingredient(JsonElement value) {
        Set<String> result=new TreeSet<>();
        if(value.isJsonArray())for(JsonElement entry:value.getAsJsonArray())result.addAll(ingredient(entry));
        else {
            JsonObject json=value.getAsJsonObject();
            if(json.has("item")) {
                String id=json.get("item").getAsString();
                var item=RegistryLookup.item(id);
                if(item!=null) {
                    ItemStack stack=new ItemStack(item);
                    // Containers and tools are not consumed preparation branches.
                    result.add(!stack.hasCraftingRemainingItem()||stack.isEdible()?id:"!returned");
                }
            }else if(json.has("tag"))for(String id:resolve(json.get("tag").getAsString(),new HashSet<>())) {
                ItemStack stack=new ItemStack(RegistryLookup.item(id));
                result.add(!stack.hasCraftingRemainingItem()||stack.isEdible()?id:"!returned");
            }
        }
        if(result.size()>1)result.remove("!returned");
        return new ArrayList<>(result);
    }
    private List<String> resolve(String tag,Set<String> visiting) {
        if(resolved.containsKey(tag))return resolved.get(tag);
        if(!visiting.add(tag))return List.of();
        Set<String> items=new TreeSet<>();
        for(String value:tags.getOrDefault(tag,List.of())) {
            if(value.startsWith("#"))items.addAll(resolve(value.substring(1),visiting));
            else if(RegistryLookup.item(value)!=null)items.add(value);
        }
        visiting.remove(tag);List<String> result=new ArrayList<>(items);resolved.put(tag,result);return result;
    }
    private boolean enabled(JsonObject json) {
        if(!json.has("conditions"))return true;
        for(JsonElement element:json.getAsJsonArray("conditions")) {
            JsonObject condition=element.getAsJsonObject();String type=condition.get("type").getAsString();
            boolean passes=switch(type) {
                case "forge:mod_loaded" -> ModList.get().isLoaded(condition.get("modid").getAsString());
                case "forge:item_exists" -> RegistryLookup.item(condition.get("item").getAsString())!=null;
                case "forge:tag_empty" -> resolve(condition.get("tag").getAsString(),new HashSet<>()).isEmpty();
                case "forge:true" -> true;
                case "forge:false" -> false;
                case "forge:not" -> !singleCondition(condition.getAsJsonObject("value"));
                case "forge:and" -> condition.getAsJsonArray("values").asList().stream().allMatch(c->singleCondition(c.getAsJsonObject()));
                case "forge:or" -> condition.getAsJsonArray("values").asList().stream().anyMatch(c->singleCondition(c.getAsJsonObject()));
                default -> throw new IllegalArgumentException("Unsupported recipe condition "+type);
            };
            if(!passes)return false;
        }
        return true;
    }
    private boolean singleCondition(JsonObject condition) {
        JsonObject wrapper=new JsonObject();JsonArray array=new JsonArray();array.add(condition);wrapper.add("conditions",array);return enabled(wrapper);
    }
}
