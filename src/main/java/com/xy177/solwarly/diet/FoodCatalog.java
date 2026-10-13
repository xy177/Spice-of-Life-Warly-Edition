package com.xy177.solwarly.diet;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.SOLWarlyConfig;
import com.xy177.solwarly.config.Configuration;
import com.xy177.solwarly.registry.RegistryLookup;
import com.xy177.solwarly.spice.Seasoning;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.ForgeRegistries;
import java.io.File;
import java.util.*;

public final class FoodCatalog {
    private static final Map<String,ItemStack> foods=new TreeMap<>();
    private static final Map<String,Double> coefficients=new HashMap<>();
    private static Configuration config;
    private static boolean ready;
    public static String id(ItemStack stack) { return RegistryLookup.id(stack.getItem()).toString(); }
    public static ItemStack stack(String id) { Item item=RegistryLookup.item(id);return item==null?ItemStack.EMPTY:new ItemStack(item); }
    public static boolean isReady() { return ready; }
    public static Set<String> ids() { return new TreeSet<>(foods.keySet()); }
    public static Collection<String> candidates() {
        return foods.entrySet().stream().filter(entry->SOLWarlyConfig.shouldCount(entry.getValue().getItem())&&!Arrays.asList(DietConfig.excludedInterest).contains(entry.getKey())).map(Map.Entry::getKey).toList();
    }
    public static double coefficient(ItemStack stack) { return coefficients.getOrDefault(id(stack),0.0); }
    public static void build() { build(false,null); }
    public static void rebuild() { build(false,null); }
    public static void build(boolean regenerate) { build(regenerate,null); }
    public static void build(boolean regenerate,net.minecraft.server.MinecraftServer server) {
        long started=System.nanoTime();foods.clear();coefficients.clear();
        config=new Configuration(new File(DietConfig.directory,"solwarly-foods.toml"));config.load();
        config.setCategoryComment("foods","食物最终分数：缺项在启动时生成，有效人工值优先；未识别制作链只按营养估算。\nFinal food scores: missing entries are generated at startup; valid manual values take precedence. Unrecognized preparation chains use nutrition only.");
        for(Item item:ForgeRegistries.ITEMS) { ItemStack stack=new ItemStack(item);if(stack.isEdible())foods.put(id(stack),stack); }
        List<String> missing=foods.keySet().stream().filter(id->regenerate||!configured(id)).toList();
        Map<String,Double> difficulties=Map.of();Set<String> outputs=Set.of();
        if(!missing.isEmpty()) {
            PreparationGraph graph=new PreparationGraph();for(String root:DietConfig.rawSources)graph.root(root);
            if(server==null)StartupRecipes.load().addTo(graph);else WorldRecipes.add(graph,server);
            FarmerRecipes.addServings(graph);
            SOLWarly.LOGGER.info("Starting food preparation analysis: {} missing coefficients",missing.size());
            int[] completed={0};long[] lastProgress={0};
            difficulties=graph.analyzeTargets(missing,id-> {
                long now=System.nanoTime();
                if(completed[0]==0||now-lastProgress[0]>=2_000_000_000L) {
                    SOLWarly.LOGGER.info("Analyzing food preparation: {}/{} ({})",completed[0],missing.size(),id);
                    lastProgress[0]=now;
                }
                completed[0]++;
            });outputs=graph.outputs();
        }else SOLWarly.LOGGER.info("Food coefficients are complete; skipping preparation analysis");
        for(var entry:foods.entrySet()) {
            String id=entry.getKey();Configuration.Property property=config.getCategory("foods").get(id);
            if(!regenerate&&configured(id)) { property.setComment(null);coefficients.put(id,property.getDouble());continue; }
            var nutrition=entry.getValue().getFoodProperties(null);
            double value=difficulties.getOrDefault(id,0.0)*DietConfig.difficultyWeight;
            if(nutrition!=null)value+=nutrition.getNutrition()*DietConfig.hungerWeight+nutrition.getNutrition()*nutrition.getSaturationModifier()*2*DietConfig.saturationWeight;
            value=Math.round(Math.max(DietConfig.minimum,Math.min(DietConfig.maximum,value))*100)/100.0;
            config.get("foods",id,value).set(value);coefficients.put(id,value);
            if(outputs.contains(id)&&!difficulties.containsKey(id))SOLWarly.LOGGER.warn("Unresolved food preparation chain: {}",id);
        }
        config.save();ready=true;SOLWarly.LOGGER.info("Food catalog ready: {} foods in {} ms",foods.size(),(System.nanoTime()-started)/1000000);
    }
    public static void regenerate(String id,net.minecraft.server.MinecraftServer server) {
        config.getCategory("foods").remove(id);config.save();build(false,server);
    }
    private static boolean configured(String id) {
        Configuration.Property property=config.getCategory("foods").get(id);
        if(property==null)return false;double value=property.getDouble(Double.NaN);return Double.isFinite(value)&&value>=0&&value<=100000;
    }
    private FoodCatalog() {}
}
