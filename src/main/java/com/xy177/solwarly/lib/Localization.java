// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/37c243c5bcf50d5582ce81596e85b3f465b222a8/src/main/java/com/cazsius/solcarrot/lib/Localization.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.lib;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class Localization {
	/** Inherited translations retain the solcarrot namespace for resource-pack compatibility. */
	public static String keyString(String domain, String path) {
		return domain + ".solcarrot." + path;
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
