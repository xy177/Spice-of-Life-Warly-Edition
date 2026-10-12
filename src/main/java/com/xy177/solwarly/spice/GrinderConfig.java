package com.xy177.solwarly.spice;

import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class GrinderConfig {
    private static final Set<String> collectors = new HashSet<>();
    public static void load(File directory) {
        Configuration config = new Configuration(new File(directory, "solwarly-grinder.cfg")); config.load();
        collectors.clear(); collectors.addAll(Arrays.asList(config.get("automation", "bottomCollectors",
            new String[] {"minecraft:hopper"},
            "正下方阻止闲置成品弹出的抽取方块注册名；添加所用模组管道，普通容器不自动视为抽取器。\nRegistry IDs of bottom extractors that prevent idle output ejection; add your modded pipes. Ordinary containers are not automatically extractors.").getStringList()));
        config.save(); // Forge does not mark comment-only updates as changed.
    }
    public static boolean isCollector(Block block) {
        return block.getRegistryName() != null && collectors.contains(block.getRegistryName().toString());
    }
    private GrinderConfig() {}
}
