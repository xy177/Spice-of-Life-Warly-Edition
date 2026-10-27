// Adapted from Spice of Life: Apple Pie Edition; upstream contributors retain copyright.
// Ported and modified by xy177 on 2026-10-27; LGPL-2.1.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/lib/Localization.java
// Warly rules are retained; upstream credits are recorded in THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.lib;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public final class Localization {

	/** e.g. keyString("tooltip", "eaten_status.not_eaten_1") -> "tooltip.solapplepie.eatenStatus.not_eaten_1") */
	public static String keyString(String domain, String path) {
		return domain + ".solwarly." + path;
	}

	@OnlyIn(Dist.CLIENT)
	public static String localized(String domain, String path, Object... args) {
		return I18n.get(keyString(domain, path), args);
	}

	public static MutableComponent localizedComponent(String domain, String path, Object... args) {
		return Component.translatable(keyString(domain, path), args);
	}

	@OnlyIn(Dist.CLIENT)
	public static String localizedQuantity(String domain, String path, int number) {
		return number == 1
			? I18n.get(keyString(domain, path + ".singular"))
			: I18n.get(keyString(domain, path + ".plural"), number);
	}

	public static MutableComponent localizedQuantityComponent(String domain, String path, int number) {
		return number == 1
			? Component.translatable(keyString(domain, path + ".singular"))
			: Component.translatable(keyString(domain, path + ".plural"), number);
	}

	public static String formatBigNumber(int number) {
		if (number < 1000) {
			return "" + number;
		} else if (number < 10_000) {
			return Math.floor(number / 1000F) + "." + Math.floor((number % 1000) / 100F) + "k";
		} else {
			return Math.round(number / 1000F) + "k";
		}
	}

	private Localization() {}
}
