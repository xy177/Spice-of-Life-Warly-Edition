// Adapted from Spice of Life: Apple Pie Edition (2d9de8b8d045b1f9b93940b681c74021d326f207); LGPL-2.1.
// Modified by xy177 on 2026-10-27 for Warly Edition.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/SOLSweetPotatoConfig.java
package com.xy177.solwarly;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
@Mod.EventBusSubscriber(modid=SOLWarly.MOD_ID)
@Config(modid=SOLWarly.MOD_ID,name="solwarly/solwarly-general")
public final class SOLWarlyConfig {
    @Config.Comment({"初始生命心数。", "Starting hearts."})
    public static int baseHearts = 10;
    @Config.Comment({"不参与饮食统计的食物，支持通配符和子类型。", "Foods excluded from dietary tracking; supports wildcards and metadata."})
    public static String[] foodBlacklist = {};
    @Config.Comment({"非空时只统计白名单食物。", "When nonempty, only listed foods count."})
    public static String[] foodWhitelist = {};
    @Config.Comment({"参与饮食统计的最低饥饿恢复值。", "Minimum hunger restoration for dietary tracking."})
    public static int minimumFoodValue = 1;
    @Config.Comment({"死亡时重置饮食统计。", "Reset dietary progress on death."})
    public static boolean shouldResetOnDeath = false;
    @Config.Comment({"显示食物兴趣和尝试记录。", "Show food appetite and taste history."})
    public static boolean isFoodTooltipEnabled = true;
    @Config.Comment({"阅历奖励提示显示在快捷栏上方。", "Show experience reward messages above the hotbar."})
    public static boolean shouldShowProgressAboveHotbar = true;
    @Config.Comment({"在食物书中显示未尝食物。", "Show untasted foods in the Food Book."})
    public static boolean shouldShowUneatenFoods = true;
    @Config.Comment({"达到阅历奖励档时播放音效。", "Play a sound when unlocking experience rewards."})
    public static boolean shouldPlayMilestoneSounds = true;
    @Config.Comment({"首次尝试食物时显示粒子。", "Show particles when tasting a new food."})
    public static boolean shouldSpawnIntermediateParticles = true;
    @Config.Comment({"获得阅历奖励时显示粒子。", "Show particles when unlocking experience rewards."})
    public static boolean shouldSpawnMilestoneParticles = true;
    @SubscribeEvent public static void changed(ConfigChangedEvent.OnConfigChangedEvent event) {
        if(!SOLWarly.MOD_ID.equals(event.getModID()))return;
        ConfigManager.sync(SOLWarly.MOD_ID,Config.Type.INSTANCE);
        net.minecraft.server.MinecraftServer server=net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();
        if(server!=null)for(net.minecraft.entity.player.EntityPlayerMP player:server.getPlayerList().getPlayers())
            com.xy177.solwarly.tracking.CapabilityHandler.syncFoodList(player);
    }
    private SOLWarlyConfig() {}
}
