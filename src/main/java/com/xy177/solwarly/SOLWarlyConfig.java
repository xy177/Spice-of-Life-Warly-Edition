// Modified by xy177 on 2026-10-10 for Spice of Life Warly Edition; upstream LGPL 2.1.
package com.xy177.solwarly;

import com.xy177.solwarly.tracking.CapabilityHandler;
import com.xy177.solwarly.tracking.FoodList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// new config names + versioning commented out for now
// TODO switch to new names when updating to next mc version

@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID)
@Config(modid = SOLWarly.MOD_ID, name = "solwarly/solcarrot")
public final class SOLWarlyConfig {
	//static final String version = "1.5"; // only change this if the new version is not backwards-compatible with the old one.
	private static final String langPath = "config.solcarrot.";
	
	//@Config.Name("Base Hearts")
	@Config.Name("defaultHeartCount")
	@Config.LangKey(langPath + "base_hearts")
	@Config.Comment("初始生命心数，新累计系统继续使用。\nStarting hearts; also used by the new cumulative system.")
	public static int baseHearts = 10;
	
	//@Config.Name("Hearts per Milestone")
	@Config.Name("heartsPerMilestone")
	@Config.LangKey(langPath + "hearts_per_milestone")
	@Config.Comment("旧档迁移时每个胡萝卜版里程碑的奖励心数；新奖励见solwarly.cfg。\nLegacy hearts per milestone for migration; new rewards are in solwarly.cfg.")
	public static int heartsPerMilestone = 2;
	
	//@Config.Name("Milestones")
	@Config.Name("Milestone amounts")
	@Config.LangKey(langPath + "milestones")
	@Config.Comment("旧档迁移保底使用的原食物种类阈值；新累计阈值见solwarly.cfg。\nLegacy unique-food thresholds for migration; new cumulative thresholds are in solwarly.cfg.")
	public static int[] milestones = {5, 10, 15, 20, 25};
	
	//@Config.Name("Enable Food Status Tooltip")
	@Config.Name("isFoodTooltipEnabled")
	@Config.LangKey(langPath + "is_food_tooltip_enabled")
	@Config.Comment("显示食物的尝试记录、兴趣与系数提示。\nShow food history, appetite and coefficient tooltips.")
	public static boolean isFoodTooltipEnabled = true;
	
	//@Config.Name("Show Progress Above Hotbar")
	@Config.Name("shouldShowProgressAboveHotbar")
	@Config.LangKey(langPath + "should_show_progress_above_hotbar")
	@Config.Comment("累计奖励提示显示在快捷栏上方，否则显示在聊天中。\nShow cumulative reward messages above the hotbar, otherwise in chat.")
	public static boolean shouldShowProgressAboveHotbar = true;
	
	@Config.Name("Show Uneaten Foods")
	@Config.LangKey(langPath + "should_show_uneaten_foods")
	@Config.Comment("食物书是否同时显示尚未吃过的食物。\nShow uneaten foods in the food book.")
	public static boolean shouldShowUneatenFoods = true;
	
	@Config.Name("Enable Milestone Sounds")
	@Config.LangKey(langPath + "should_play_milestone_sounds")
	@Config.Comment("获得新的累计奖励时播放提示音。\nPlay a sound when unlocking a cumulative reward.")
	public static boolean shouldPlayMilestoneSounds = true;
	
	@Config.Name("Enable Intermediate Particles")
	@Config.LangKey(langPath + "should_spawn_intermediate_particles")
	@Config.Comment("首次尝试食物时显示粒子。\nShow particles when trying a new food.")
	public static boolean shouldSpawnIntermediateParticles = true;
	
	@Config.Name("Enable Milestone Particles")
	@Config.LangKey(langPath + "should_spawn_milestone_particles")
	@Config.Comment("获得累计奖励时显示粒子。\nShow particles when unlocking a cumulative reward.")
	public static boolean shouldSpawnMilestoneParticles = true;
	
	@Config.Name("Food Blacklist")
	@Config.LangKey(langPath + "food_blacklist")
	@Config.Comment("排除的食物不参与累计、多样性、服务器进食统计和食物书；沿用modid:item或modid:item:子类型及通配符。\nExcluded foods do not count for progression, diversity, server meals or the book; accepts legacy modid:item, modid:item:metadata and wildcards.")
	public static String[] foodBlacklist = {};
	
	@Config.Name("Food Whitelist")
	@Config.LangKey(langPath + "food_whitelist")
	@Config.Comment("非空时仅统计此列表内的食物，忽略黑名单。\nWhen nonempty, only listed foods count and the blacklist is ignored.")
	public static String[] foodWhitelist = {};
	
	@Config.Name("Minimum Food Value")
	@Config.LangKey(langPath + "minimum_food_value")
	@Config.Comment("参与累计、多样性和服务器统计的最低饥饿恢复值，2点为一格图标。\nMinimum hunger restoration for cumulative, diversity and server statistics; 2 points equal one hunger icon.")
	public static int minimumFoodValue = 1;
	
	@Config.Name("Reset on Death")
	@Config.LangKey(langPath + "reset_on_death")
	@Config.Comment("死亡时是否清除食物历史、累计、多样性和旧奖励保底；默认保留。\nClear food history, cumulative, diversity and legacy reward floor on death; disabled by default.")
	public static boolean shouldResetOnDeath = false;
	
	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (!event.getModID().equals(SOLWarly.MOD_ID)) return;
		
		ConfigManager.sync(SOLWarly.MOD_ID, Config.Type.INSTANCE);
		
		if (event.isWorldRunning()) {
			PlayerList players = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
			for (EntityPlayer player : players.getPlayers()) {
				FoodList.get(player).updateProgressInfo();
				CapabilityHandler.syncFoodList(player);
			}
		}
	}
	
	private SOLWarlyConfig() {}
}
