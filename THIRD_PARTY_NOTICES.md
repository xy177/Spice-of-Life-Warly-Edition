# Third-Party Notices

Spice of Life Warly Edition, Minecraft 1.12.2.
Original upstream contributors retain copyright in their respective work.
Warly Edition modifications are by xy177. No copyright ownership in upstream work is claimed.

## Current source foundation (2026-10-27)

The recent-food store, food identity and book layout primitives are now adapted from
Spice of Life: Apple Pie Edition, pinned at commit `2d9de8b8d045b1f9b93940b681c74021d326f207` on its `1.20.1` branch.
Source: https://github.com/txnimc/Spice-of-Life-Apple-Pie/tree/2d9de8b8d045b1f9b93940b681c74021d326f207
These modules were ported and modified by xy177 on 2026-10-27 for Warly Edition's
Forge 1.20.1, NeoForge 1.21.1 and Forge 1.12.2 implementations. The legacy version
is a backport of these modules, not an official Apple Pie 1.12.2 release.

Apple Pie's food / last-eaten index supplies Warly's primary recent-food storage
and non-mutating meal preview. Warly's saved per-meal contributions, repeated-meal
average, flavor grouping and cumulative experience rules are retained; Apple Pie's
default decay and score formula are not substituted. The lifetime archive and
legacy capability, health, configuration and API adapters remain compatible with
existing Warly and Carrot saves. Directly retained older components are identified
separately below; their copyright and license notices remain in place.

Apple Pie itself inherits earlier work from Sweet Potato, Potato and Carrot Edition.
This adaptation does not remove those upstream authors' rights or attribution.
The older lunch-container resource source listed below is also retained.

## Spice of Life: Carrot Edition

Source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/tree/3fbe133834091bbbc73ba0f37cf582d2bb66b10d
License: GNU Lesser General Public License v2.1; the full text is in LICENSE.txt.
Earlier revisions used the carrot-old source baseline; retained compatibility components are listed below. The 1.12.2 compatibility layer also includes corrections checked against the upstream 1.8.4 release.

Original upstream author record: Cazsius, Talonos, juliand665.

Original upstream credit record: Lewis_McReu for some coding assistance

Other Carrot branches additionally credit Mrbysco and Vik where applicable.

## Spice of Life: Apple Pie Edition

Source: https://github.com/txnimc/Spice-of-Life-Apple-Pie/tree/f38436cb11c5b8d628e6f20f7b1670dbc811592d
License: GNU Lesser General Public License v2.1; the full text is in LICENSE.txt.
The lunchbag, lunchbox, golden lunchbox and two container GUI textures are copied from that source. Warly Edition adapts the lunch-container behavior and draws on its dietary-diversity gameplay.

Original upstream author record: tarinoita.

Original upstream credit record: Vice, creator of Apple Pie Edition, Kevun1, creator of Spice of Life: Potato Edition, and the Spice of Life: Carrot Edition team, which that project was forked from.

The Apple Pie README also credits the Potato Edition and Carrot Edition projects. These acknowledgments are retained; they do not assign every resource to a specific named contributor.

## Cyclic notice retained through the lunch-container upstream

The upstream Apple Pie files FoodContainer.java, FoodContainerCapabilityProvider.java and FoodContainerScreen.java carry this notice:

> Much of the following code was adapted from Cyclic's storage bag code.
> Copyright for portions of the code are held by Samson Basset (Lothrazar)
> as part of Cyclic, under the MIT license.

That upstream notice is retained here for the lunch-container adaptations. It does not attribute all of Warly Edition's rewritten container code to Cyclic.
The complete inherited MIT permission and warranty text is in licenses/Cyclic-MIT.txt, reproduced from Cyclic's trunk/1.19.2 LICENSE.
Source: https://github.com/Lothrazar/Cyclic/blob/trunk/1.19.2/LICENSE

## Corresponding source

Repository: https://github.com/xy177/Spice-of-Life-Warly-Edition
Minecraft 1.12.2: main branch; Minecraft 1.20.1: 1.20.1 branch; Minecraft 1.21.1: 1.21.1 branch.
When distributing a binary, provide the complete source corresponding to that build, including its modifications and build files, and preserve these notices and the applicable license texts.

## Modified and reused files

The following records identify inherited files and resources. Java and build files carry in-file modification notices. Resources have adjacent .license sidecars so their binary or JSON format remains intact. Dates added during this review identify the notice completion on 2026-10-13; existing earlier modification notices are preserved.

| Warly Edition file | Upstream source | Modification / copying record |
|---|---|---|
| `src/main/java/com/cazsius/solcarrot/api/FoodCapability.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/api/FoodCapability.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/cazsius/solcarrot/api/package-info.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/api/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/cazsius/solcarrot/api/SOLCarrotAPI.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/api/SOLCarrotAPI.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/FoodItemStacks.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/client/FoodItemStacks.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/ConfigInfoPage.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/client/gui/ConfigInfoPage.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/elements/package-info.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/client/gui/elements/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/elements/UIBox.java` | `Apple Pie 2d9de8b8d045b1f9b93940b681c74021d326f207: src/main/java/com/tarinoita/solsweetpotato/client/gui/elements/UIBox.java` | Ported and adapted by xy177 on 2026-10-27; Warly rules and legacy compatibility retained. |
| `src/main/java/com/xy177/solwarly/client/gui/elements/UIElement.java` | `Apple Pie 2d9de8b8d045b1f9b93940b681c74021d326f207: src/main/java/com/tarinoita/solsweetpotato/client/gui/elements/UIElement.java` | Ported and adapted by xy177 on 2026-10-27; Warly rules and legacy compatibility retained. |
| `src/main/java/com/xy177/solwarly/client/gui/elements/UIImage.java` | `Apple Pie 2d9de8b8d045b1f9b93940b681c74021d326f207: src/main/java/com/tarinoita/solsweetpotato/client/gui/elements/UIImage.java` | Ported and adapted by xy177 on 2026-10-27; Warly rules and legacy compatibility retained. |
| `src/main/java/com/xy177/solwarly/client/gui/elements/UIItemStack.java` | `Apple Pie 2d9de8b8d045b1f9b93940b681c74021d326f207: src/main/java/com/tarinoita/solsweetpotato/client/gui/elements/UIItemStack.java` | Ported and adapted by xy177 on 2026-10-27; Warly rules and legacy compatibility retained. |
| `src/main/java/com/xy177/solwarly/client/gui/elements/UILabel.java` | `Apple Pie 2d9de8b8d045b1f9b93940b681c74021d326f207: src/main/java/com/tarinoita/solsweetpotato/client/gui/elements/UILabel.java` | Ported and adapted by xy177 on 2026-10-27; Warly rules and legacy compatibility retained. |
| `src/main/java/com/xy177/solwarly/client/gui/elements/UIStack.java` | `Apple Pie 2d9de8b8d045b1f9b93940b681c74021d326f207: src/main/java/com/tarinoita/solsweetpotato/client/gui/elements/UIStack.java` | Ported and adapted by xy177 on 2026-10-27; Warly rules and legacy compatibility retained. |
| `src/main/java/com/xy177/solwarly/client/gui/FoodData.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/client/gui/FoodData.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/GuiFoodBook.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/client/gui/GuiFoodBook.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/ItemListPage.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/client/gui/ItemListPage.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/package-info.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/client/gui/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/Page.java` | `Apple Pie 2d9de8b8d045b1f9b93940b681c74021d326f207: src/main/java/com/tarinoita/solsweetpotato/client/gui/Page.java` | Ported and adapted by xy177 on 2026-10-27; Warly rules and legacy compatibility retained. |
| `src/main/java/com/xy177/solwarly/client/gui/PageFlipButton.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/client/gui/PageFlipButton.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/ProgressGraph.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/client/gui/ProgressGraph.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/StatListPage.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/client/gui/StatListPage.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/package-info.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/client/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/TooltipHandler.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/client/TooltipHandler.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/command/CommandClearFoodList.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/command/CommandClearFoodList.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/command/CommandFoodList.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/command/CommandFoodList.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/command/CommandSizeFoodList.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/command/CommandSizeFoodList.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/command/CommandSyncFoodList.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/command/CommandSyncFoodList.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/command/package-info.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/command/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/communication/GuiHandler.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/communication/GuiHandler.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/communication/MessageFoodList.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/communication/MessageFoodList.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/communication/NBTMessage.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/communication/NBTMessage.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/communication/package-info.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/communication/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/communication/PacketHandler.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/communication/PacketHandler.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/item/ItemFoodBook.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/item/ItemFoodBook.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/item/package-info.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/item/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/item/SOLWarlyItems.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/item/SOLCarrotItems.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/lib/Localization.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/lib/Localization.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/lib/package-info.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/lib/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/package-info.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/SOLWarly.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/SOLCarrot.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/SOLWarlyConfig.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/SOLCarrotConfig.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/tracking/CapabilityHandler.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/tracking/CapabilityHandler.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/tracking/FoodInstance.java` | `Apple Pie 2d9de8b8d045b1f9b93940b681c74021d326f207: src/main/java/com/tarinoita/solsweetpotato/tracking/FoodInstance.java` | Ported and adapted by xy177 on 2026-10-27; Warly rules and legacy compatibility retained. |
| `src/main/java/com/xy177/solwarly/tracking/FoodHistory.java` | `Apple Pie 2d9de8b8d045b1f9b93940b681c74021d326f207: src/main/java/com/tarinoita/solsweetpotato/tracking/FoodList.java` | Ported and adapted by xy177 on 2026-10-27; Warly rules and legacy compatibility retained. |
| `src/main/java/com/xy177/solwarly/tracking/FoodList.java` | `Apple Pie 2d9de8b8d045b1f9b93940b681c74021d326f207: src/main/java/com/tarinoita/solsweetpotato/tracking/FoodList.java` | Ported and adapted by xy177 on 2026-10-27; Warly rules and legacy compatibility retained. |
| `src/main/java/com/xy177/solwarly/tracking/FoodTracker.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/tracking/FoodTracker.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/tracking/MaxHealthHandler.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/tracking/MaxHealthHandler.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/tracking/package-info.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/tracking/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/tracking/ProgressInfo.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/tracking/ProgressInfo.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/RewardPage.java` | `carrot-old: src/main/java/com/cazsius/solcarrot/client/gui/ProgressGraph.java` | Adapted reward layout; notice completed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/lunch/ContainerLunch.java` | `apple: f38436cb11c5b8d628e6f20f7b1670dbc811592d, item/foodcontainer implementation and behavior` | Warly adaptation; upstream notices retained 2026-10-13. |
| `src/main/java/com/xy177/solwarly/lunch/ItemLunchContainer.java` | `apple: f38436cb11c5b8d628e6f20f7b1670dbc811592d, item/foodcontainer implementation and behavior` | Warly adaptation; upstream notices retained 2026-10-13. |
| `src/main/java/com/xy177/solwarly/lunch/LunchInventory.java` | `apple: f38436cb11c5b8d628e6f20f7b1670dbc811592d, item/foodcontainer implementation and behavior` | Warly adaptation; upstream notices retained 2026-10-13. |
| `src/main/java/com/xy177/solwarly/lunch/LunchRegistry.java` | `apple: f38436cb11c5b8d628e6f20f7b1670dbc811592d, item/foodcontainer implementation and behavior` | Warly adaptation; upstream notices retained 2026-10-13. |
| `src/main/java/com/xy177/solwarly/lunch/LunchUpgradeRecipe.java` | `apple: f38436cb11c5b8d628e6f20f7b1670dbc811592d, item/foodcontainer implementation and behavior` | Warly adaptation; upstream notices retained 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/GuiLunch.java` | `apple: f38436cb11c5b8d628e6f20f7b1670dbc811592d, item/foodcontainer implementation and behavior` | Warly adaptation; upstream notices retained 2026-10-13. |
| `src/main/resources/assets/solcarrot/lang/en_us.lang` | `carrot-old: src/main/resources/assets/solcarrot/lang/en_us.lang` | Modified resource; per-file .license notice documented 2026-10-13. |
| `src/main/resources/assets/solcarrot/lang/zh_cn.lang` | `carrot-old: src/main/resources/assets/solcarrot/lang/zh_cn.lang` | Modified resource; per-file .license notice documented 2026-10-13. |
| `src/main/resources/assets/solcarrot/models/item/food_book.json` | `carrot-old: src/main/resources/assets/solcarrot/models/item/food_book.json` | Copied without content changes. |
| `src/main/resources/assets/solcarrot/recipes/food_book.json` | `carrot-old: src/main/resources/assets/solcarrot/recipes/food_book.json` | Copied without content changes. |
| `src/main/resources/assets/solcarrot/textures/forge/logo.png` | `carrot-old: src/main/resources/assets/solcarrot/textures/forge/logo.png` | Modified resource; per-file .license notice documented 2026-10-13. |
| `src/main/resources/assets/solcarrot/textures/gui/food_book.png` | `carrot-old: src/main/resources/assets/solcarrot/textures/gui/food_book.png` | Copied without content changes. |
| `src/main/resources/assets/solcarrot/textures/items/food_book.png` | `carrot-old: src/main/resources/assets/solcarrot/textures/items/food_book.png` | Copied without content changes. |
| `src/main/resources/mcmod.info` | `carrot-old: src/main/resources/mcmod.info` | Modified resource; per-file .license notice documented 2026-10-13. |
| `src/main/resources/pack.mcmeta` | `carrot-old: src/main/resources/pack.mcmeta` | Modified resource; per-file .license notice documented 2026-10-13. |
| `src/main/resources/assets/solwarly/textures/gui/lunch_inventory.png` | `apple: src/main/resources/assets/solapplepie/textures/gui/inventory.png` | Copied without content changes; resource path renamed. |
| `src/main/resources/assets/solwarly/textures/gui/lunch_slot.png` | `apple: src/main/resources/assets/solapplepie/textures/gui/slot.png` | Copied without content changes; resource path renamed. |
| `src/main/resources/assets/solwarly/textures/items/golden_lunchbox.png` | `apple: src/main/resources/assets/solapplepie/textures/items/golden_lunchbox.png` | Copied without content changes; resource path renamed. |
| `src/main/resources/assets/solwarly/textures/items/lunchbag.png` | `apple: src/main/resources/assets/solapplepie/textures/items/lunchbag.png` | Copied without content changes; resource path renamed. |
| `src/main/resources/assets/solwarly/textures/items/lunchbox.png` | `apple: src/main/resources/assets/solapplepie/textures/items/lunchbox.png` | Copied without content changes; resource path renamed. |
| `build.gradle` | `carrot-old: build.gradle` | Warly build adaptation; notice reviewed 2026-10-13. |
| `settings.gradle` | `carrot-old: settings.gradle` | Warly build adaptation; notice reviewed 2026-10-13. |

## No warranty

The software is provided WITHOUT ANY WARRANTY, including implied warranties of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, to the extent stated in LICENSE.txt and the applicable third-party licenses.
