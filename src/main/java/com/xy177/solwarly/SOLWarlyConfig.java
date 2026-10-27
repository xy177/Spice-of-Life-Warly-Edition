// Adapted from Spice of Life: Apple Pie Edition (2d9de8b8d045b1f9b93940b681c74021d326f207); LGPL-2.1.
// Modified by xy177 on 2026-10-27 for Warly Edition.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/SOLSweetPotatoConfig.java
package com.xy177.solwarly;

import com.xy177.solwarly.tracking.CapabilityHandler;
import com.xy177.solwarly.tracking.FoodList;
import com.google.common.collect.Lists;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID, bus = MOD)
public final class SOLWarlyConfig {
    private static net.minecraft.nbt.CompoundTag clientRules;
    public static void clearClientRules() { clientRules=null; }
    private static List<String> readList(net.minecraft.nbt.CompoundTag tag,String key) {
        var list=tag.getList(key,8);List<String> result=new ArrayList<>();for(int i=0;i<list.size();i++)result.add(list.getString(i));return result;
    }
    public static net.minecraft.nbt.CompoundTag snapshotRules() {
        var tag=new net.minecraft.nbt.CompoundTag();
        tag.putInt("minimum",SERVER.minimumFoodValue.get());
        for(String key:List.of("blacklist","whitelist")) {
            var list=new net.minecraft.nbt.ListTag();
            for(String value:key.equals("blacklist")?SERVER.blacklist.get():SERVER.whitelist.get())list.add(net.minecraft.nbt.StringTag.valueOf(value));
            tag.put(key,list);
        }
        return tag;
    }
    public static void readRules(net.minecraft.nbt.CompoundTag tag) { clientRules=tag.copy(); }

	private static String localizationPath(String path) {
		return "config." + SOLWarly.MOD_ID + "." + path;
	}

	public static final Server SERVER;
	public static final ForgeConfigSpec SERVER_SPEC;

	static {
		Pair<Server, ForgeConfigSpec> specPair = new Builder().configure(Server::new);
		SERVER = specPair.getLeft();
		SERVER_SPEC = specPair.getRight();
	}

	public static final Client CLIENT;
	public static final ForgeConfigSpec CLIENT_SPEC;

	static {
		Pair<Client, ForgeConfigSpec> specPair = new Builder().configure(Client::new);
		CLIENT = specPair.getLeft();
		CLIENT_SPEC = specPair.getRight();
	}

	public static void setUp() {
		ModLoadingContext context = ModLoadingContext.get();
		context.registerConfig(ModConfig.Type.COMMON, SERVER_SPEC, "solwarly/solwarly-common.toml");
		context.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC, "solwarly/solwarly-client.toml");
	}

	@SubscribeEvent
	public static void onConfigReload(ModConfigEvent.Reloading event) {
		MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
		if (currentServer == null) return;

		var players = currentServer.getPlayerList();
		for (var player : players.getPlayers()) {

			CapabilityHandler.syncFoodList(player);
		}
	}

    public static int getBaseHearts() { return SERVER_SPEC.isLoaded()?SERVER.baseHearts.get():SERVER.baseHearts.getDefault(); }
	public static List<String> getBlacklist() {
        return clientRules==null?new ArrayList<>(SERVER_SPEC.isLoaded()?SERVER.blacklist.get():SERVER.blacklist.getDefault()):readList(clientRules,"blacklist");
	}

	public static List<String> getWhitelist() {
        return clientRules==null?new ArrayList<>(SERVER_SPEC.isLoaded()?SERVER.whitelist.get():SERVER.whitelist.getDefault()):readList(clientRules,"whitelist");
	}

	public static int getMinimumFoodValue() {
        return clientRules==null?(SERVER_SPEC.isLoaded()?SERVER.minimumFoodValue.get():SERVER.minimumFoodValue.getDefault()):clientRules.getInt("minimum");
	}

	public static boolean shouldResetOnDeath() {
        return SERVER_SPEC.isLoaded()?SERVER.shouldResetOnDeath.get():SERVER.shouldResetOnDeath.getDefault();
	}

	public static boolean limitProgressionToSurvival() {
        return SERVER_SPEC.isLoaded()?SERVER.limitProgressionToSurvival.get():SERVER.limitProgressionToSurvival.getDefault();
	}

	public static class Server {
		public final IntValue baseHearts;

		public final ConfigValue<List<? extends String>> blacklist;
		public final ConfigValue<List<? extends String>> whitelist;
		public final IntValue minimumFoodValue;

		public final BooleanValue shouldResetOnDeath;
		public final BooleanValue limitProgressionToSurvival;

		Server(Builder builder) {
			builder.push("experience");

			baseHearts = builder
				.translation(localizationPath("base_hearts"))
				.comment("初始基础生命值，单位为颗心。", "Number of hearts you start out with.")
				.defineInRange("baseHearts", 10, 0, 1000);

			builder.pop();
			builder.push("filtering");

			blacklist = builder
				.translation(localizationPath("blacklist"))
				.comment("黑名单食物不计入阅历、多样性或每日兴趣，也不显示在食物书中。", "Excluded foods do not count for experience, diversity, daily interests or the Food Book.")
				.defineList("blacklist", Lists.newArrayList(), e -> e instanceof String);

			whitelist = builder
				.translation(localizationPath("whitelist"))
				.comment("白名单非空时只统计其中的食物，并忽略黑名单。", "When this list contains anything, the blacklist is ignored and instead only foods from here count.")
				.defineList("whitelist", Lists.newArrayList(), e -> e instanceof String);

			minimumFoodValue = builder
				.translation(localizationPath("minimum_food_value"))
				.comment("可计入饮食统计的最低饥饿恢复值，单位为半格饥饿图标。", "Minimum hunger restoration for dietary tracking, in half drumsticks.")
				.defineInRange("minimumFoodValue", 1, 0, 1000);

			builder.pop();
			builder.push("miscellaneous");

			shouldResetOnDeath = builder
				.translation(localizationPath("reset_on_death"))
				.comment("死亡时是否重置饮食收益；个人沃利模式选择和重复进食记录保留。", "Whether or not to reset the food list on death, effectively losing all bonus hearts.")
				.define("resetOnDeath", false);

			limitProgressionToSurvival = builder
				.translation(localizationPath("limit_progression_to_survival"))
				.comment("仅在生存模式下记录饮食进度。", "If true, eating foods outside of survival mode (e.g. creative/adventure) is not tracked and thus does not contribute towards progression.")
				.define("limitProgressionToSurvival", false);

			builder.pop();
		}
	}

	private static boolean clientFlag(BooleanValue value) { return CLIENT_SPEC.isLoaded()?value.get():value.getDefault(); }
	public static boolean shouldPlayMilestoneSounds() {
		return clientFlag(CLIENT.shouldPlayMilestoneSounds);
	}

	public static boolean shouldSpawnIntermediateParticles() {
		return clientFlag(CLIENT.shouldSpawnIntermediateParticles);
	}

	public static boolean shouldSpawnMilestoneParticles() {
		return clientFlag(CLIENT.shouldSpawnMilestoneParticles);
	}

	public static boolean isFoodTooltipEnabled() {
		return clientFlag(CLIENT.isFoodTooltipEnabled);
	}

	public static boolean shouldShowProgressAboveHotbar() {
		return clientFlag(CLIENT.shouldShowProgressAboveHotbar);
	}

	public static boolean shouldShowUneatenFoods() {
		return clientFlag(CLIENT.shouldShowUneatenFoods);
	}

	public static class Client {
		public final BooleanValue shouldPlayMilestoneSounds;
		public final BooleanValue shouldSpawnIntermediateParticles;
		public final BooleanValue shouldSpawnMilestoneParticles;

		public final BooleanValue isFoodTooltipEnabled;
		public final BooleanValue shouldShowProgressAboveHotbar;
		public final BooleanValue shouldShowUneatenFoods;

		Client(Builder builder) {
			builder.push("milestone celebration");

			shouldPlayMilestoneSounds = builder
				.translation(localizationPath("should_play_milestone_sounds"))
				.comment("达到新的阅历奖励档时播放音效。", "If true, reaching a new milestone plays a ding sound.")
				.define("shouldPlayMilestoneSounds", true);

			shouldSpawnIntermediateParticles = builder
				.translation(localizationPath("should_spawn_intermediate_particles"))
				.comment("尝试新食物时生成粒子。", "If true, trying a new food spawns particles.")
				.define("shouldSpawnIntermediateParticles", true);

			shouldSpawnMilestoneParticles = builder
				.translation(localizationPath("should_spawn_milestone_particles"))
				.comment("达到新的阅历奖励档时生成粒子。", "If true, reaching a new milestone spawns particles.")
				.define("shouldSpawnMilestoneParticles", true);

			builder.pop();
			builder.push("miscellaneous");

			isFoodTooltipEnabled = builder
				.translation(localizationPath("is_food_tooltip_enabled"))
				.comment("显示食物的已尝状态、兴趣和多样性贡献提示。", "If true, foods indicate in their tooltips whether or not they have been eaten.")
				.define("isFoodTooltipEnabled", true);

			shouldShowProgressAboveHotbar = builder
				.translation(localizationPath("should_show_progress_above_hotbar"))
				.comment("新的阅历奖励提示显示在快捷栏上方；关闭后显示在聊天栏。", "Whether the messages notifying you of reaching new milestones should be displayed above the hotbar or in chat.")
				.define("shouldShowProgressAboveHotbar", true);

			shouldShowUneatenFoods = builder
				.translation(localizationPath("should_show_uneaten_foods"))
				.comment("食物书中显示尚未吃过的食物。", "If true, the food book also lists foods that you haven't eaten, in addition to the ones you have.")
				.define("shouldShowUneatenFoods", true);

			builder.pop();
		}
	}

	// TODO: investigate performance of all these get() calls

	public static boolean hasWhitelist() {
		return !getWhitelist().isEmpty();
	}

	public static boolean isAllowed(Item food) {
		String id = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(food)).toString();
		if (hasWhitelist()) {
			return matchesAnyPattern(id, getWhitelist());
		} else {
			return !matchesAnyPattern(id, getBlacklist());
		}
	}

	public static boolean shouldCount(Item food) {
		return isHearty(food) && isAllowed(food);
	}

	public static boolean isHearty(Item food) {
		var foodInfo = food.getFoodProperties();
		if (foodInfo == null) return false;
		return foodInfo.getNutrition() >= getMinimumFoodValue();
	}

	private static boolean matchesAnyPattern(String query, Collection<? extends String> patterns) {
		for (String glob : patterns) {
			StringBuilder pattern = new StringBuilder(glob.length());
			for (String part : glob.split("\\*", -1)) {
				if (!part.isEmpty()) { // not necessary
					pattern.append(Pattern.quote(part));
				}
				pattern.append(".*");
			}

			// delete extraneous trailing ".*" wildcard
			pattern.delete(pattern.length() - 2, pattern.length());

			if (Pattern.matches(pattern.toString(), query)) {
				return true;
			}
		}
		return false;
	}
}
