package com.xy177.solwarly.compat;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.xy177.solwarly.SOLWarly;
import net.minecraft.nbt.*;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.util.thread.EffectiveSide;
import java.nio.file.*;
import java.util.*;

/** Per-world compatibility settings are read without rewriting the old or new configuration. */
@EventBusSubscriber(modid=SOLWarly.MOD_ID)
public final class LegacyRules {
    private static CompoundTag world=new CompoundTag(),client=new CompoundTag();
    private static final String[] NUMBERS={"milestones.baseHearts","milestones.heartsPerMilestone","filtering.minimumFoodValue"};
    private static final String[] FLAGS={"miscellaneous.resetOnDeath","miscellaneous.limitProgressionToSurvival"};
    private static final String[] LISTS={"filtering.blacklist","filtering.whitelist","milestones.milestones"};
    private static CompoundTag current() { return EffectiveSide.get().isClient()?client:world; }
    @SubscribeEvent public static void start(ServerAboutToStartEvent event) {
        world=new CompoundTag();Path path=event.getServer().getWorldPath(LevelResource.ROOT).resolve("serverconfig/solcarrot-server.toml");
        if(!Files.isRegularFile(path))return;
        try(CommentedFileConfig config=CommentedFileConfig.builder(path).build()) {
            config.load();
            for(String key:NUMBERS) { Object value=config.get(key);if(value instanceof Number n&&n.intValue()>=0)world.putInt(key,n.intValue()); }
            for(String key:FLAGS) { Object value=config.get(key);if(value instanceof Boolean b)world.putBoolean(key,b); }
            for(String key:LISTS) {
                Object value=config.get(key);if(!(value instanceof List<?> list))continue;ListTag tag=new ListTag();
                for(Object entry:list) { if(entry instanceof Number n&&key.endsWith("milestones"))tag.add(IntTag.valueOf(n.intValue()));else if(entry instanceof String s)tag.add(StringTag.valueOf(s)); }
                world.put(key,tag);
            }
            SOLWarly.LOGGER.info("Read legacy Carrot compatibility settings from {}",path);
        }catch(RuntimeException e) { SOLWarly.LOGGER.error("Cannot read legacy Carrot settings {}",path,e); }
    }
    @SubscribeEvent public static void stop(ServerStoppedEvent event) { world=new CompoundTag(); }
    public static void clearClient() { client=new CompoundTag(); }
    public static void readSnapshot(CompoundTag tag) { client=tag.copy(); }
    public static CompoundTag snapshot() {
        CompoundTag tag=world.copy();var rules=com.xy177.solwarly.SOLWarlyConfig.SERVER;
        if(!tag.contains("milestones.baseHearts"))tag.putInt("milestones.baseHearts",rules.baseHearts.get());
        if(!tag.contains("milestones.heartsPerMilestone"))tag.putInt("milestones.heartsPerMilestone",rules.heartsPerMilestone.get());
        if(!tag.contains("filtering.minimumFoodValue"))tag.putInt("filtering.minimumFoodValue",rules.minimumFoodValue.get());
        if(!tag.contains("miscellaneous.resetOnDeath"))tag.putBoolean("miscellaneous.resetOnDeath",rules.shouldResetOnDeath.get());
        if(!tag.contains("miscellaneous.limitProgressionToSurvival"))tag.putBoolean("miscellaneous.limitProgressionToSurvival",rules.limitProgressionToSurvival.get());
        for(String key:LISTS)if(!tag.contains(key)) {
            ListTag list=new ListTag();
            if(key.equals("milestones.milestones"))rules.milestones.get().forEach(value->list.add(IntTag.valueOf(value)));
            else (key.endsWith("blacklist")?rules.blacklist.get():rules.whitelist.get()).forEach(value->list.add(StringTag.valueOf(value)));
            tag.put(key,list);
        }
        return tag;
    }
    public static int number(String key,int fallback) { return current().contains(key)?current().getInt(key):fallback; }
    public static boolean flag(String key,boolean fallback) { return current().contains(key)?current().getBoolean(key):fallback; }
    public static List<String> strings(String key,List<? extends String> fallback) {
        if(!current().contains(key))return new ArrayList<>(fallback);List<String> values=new ArrayList<>();
        for(Tag entry:current().getList(key,Tag.TAG_STRING))values.add(entry.getAsString());return values;
    }
    public static List<Integer> milestones(List<? extends Integer> fallback) {
        String key="milestones.milestones";if(!current().contains(key))return new ArrayList<>(fallback);List<Integer> values=new ArrayList<>();
        for(Tag entry:current().getList(key,Tag.TAG_INT))values.add(((IntTag)entry).getAsInt());return values;
    }
    private LegacyRules() {}
}
