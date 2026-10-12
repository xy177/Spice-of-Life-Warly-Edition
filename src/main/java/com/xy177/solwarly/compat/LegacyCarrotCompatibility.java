// Added by xy177 on 2026-10-10 for Spice of Life Warly Edition (LGPL 2.1).
package com.xy177.solwarly.compat;

import net.minecraftforge.fml.common.Mod;

/** Keeps legacy mod checks and required-after:solcarrot dependencies working. */
@Mod(
    modid = "solcarrot",
    name = "Warly Edition: Carrot Compatibility",
    version = "1.8.4",
    acceptedMinecraftVersions = "[1.12.2]",
    dependencies = "required-after:solwarly",
    useMetadata = true
)
public final class LegacyCarrotCompatibility {}
