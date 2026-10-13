package com.xy177.solwarly.spice;

import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class GrinderConfig {
    public static int previewTicks = 30;
    private static final Set<String> collectors = new HashSet<>();
    public static void load(File directory) {
        Configuration config = new Configuration(new File(directory, "solwarly-grinder.cfg")); config.load();
        collectors.clear(); collectors.addAll(Arrays.asList(config.get("automation", "bottomCollectors",
            new String[] {"minecraft:hopper"},
            "正下方抽取方块注册名：阻止闲置成品弹出，并优先接收送出的原料、成品和容器；添加所用模组管道，普通容器不自动视为抽取器。\nRegistry IDs of bottom extractors: prevent idle output ejection and receive expelled ingredients, products and containers first. Add your modded pipes; ordinary containers are not automatically extractors.").getStringList()));
        previewTicks = config.getInt("boundPreviewTicks", "display", 30, 1, 72000,
            "绑定配方后上方虚影切换间隔（刻），30刻为1.5秒。\nTicks between bound-recipe previews; 30 ticks means 1.5 seconds.");
        config.save(); // Forge does not mark comment-only updates as changed.
    }
    public static boolean isCollector(Block block) {
        return block.getRegistryName() != null && collectors.contains(block.getRegistryName().toString());
    }
    private GrinderConfig() {}
}
