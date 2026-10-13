# Third-Party Notices

Spice of Life Warly Edition, Minecraft 1.21.1.
Original upstream contributors retain copyright in their respective work.
Warly Edition modifications are by xy177. No copyright ownership in upstream work is claimed.

## Spice of Life: Carrot Edition

Source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/tree/412fa94a07d93f09378fc0e3393de38eb0099824
License: GNU Lesser General Public License v2.1; the full text is in LICENSE.txt.
This version uses the carrot-1.21 source baseline. The 1.12.2 baseline also includes compatibility corrections checked against the upstream 1.8.4 release.

Original upstream author record: Cazsius, Talonos, juliand665, Mrbysco.

Original upstream credit record: Lewis_McReu for some coding assistance, Vik for the great new art :)

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
| `src/main/java/com/cazsius/solcarrot/api/FoodCapability.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/api/FoodCapability.java` | Copied without content changes. |
| `src/main/java/com/cazsius/solcarrot/api/package-info.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/api/package-info.java` | Copied without content changes. |
| `src/main/java/com/cazsius/solcarrot/api/SOLCarrotAPI.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/api/SOLCarrotAPI.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/FoodItems.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/FoodItems.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/ConfigInfoPage.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/ConfigInfoPage.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/elements/ImageData.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/elements/ImageData.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/elements/package-info.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/elements/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/elements/UIBox.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/elements/UIBox.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/elements/UIElement.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/elements/UIElement.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/elements/UIImage.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/elements/UIImage.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/elements/UIItemStack.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/elements/UIItemStack.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/elements/UILabel.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/elements/UILabel.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/elements/UIStack.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/elements/UIStack.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/FoodBookScreen.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/FoodBookScreen.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/FoodData.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/FoodData.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/ItemListPage.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/ItemListPage.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/package-info.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/Page.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/Page.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/PageFlipButton.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/PageFlipButton.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/ProgressGraph.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/ProgressGraph.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/StatListPage.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/StatListPage.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/package-info.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/TooltipHandler.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/TooltipHandler.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/command/FoodListCommand.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/command/FoodListCommand.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/command/package-info.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/command/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/communication/FoodListMessage.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/communication/FoodListMessage.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/communication/package-info.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/communication/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/item/FoodBookItem.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/item/FoodBookItem.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/item/package-info.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/item/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/item/SOLWarlyItems.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/item/SOLCarrotItems.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/lib/Localization.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/lib/Localization.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/lib/package-info.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/lib/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/package-info.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/SOLWarly.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/SOLCarrot.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/SOLWarlyConfig.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/SOLCarrotConfig.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/tracking/CapabilityHandler.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/tracking/CapabilityHandler.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/tracking/FoodInstance.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/tracking/FoodInstance.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/tracking/FoodList.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/tracking/FoodList.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/tracking/FoodTracker.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/tracking/FoodTracker.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/tracking/MaxHealthHandler.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/tracking/MaxHealthHandler.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/tracking/package-info.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/tracking/package-info.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/tracking/ProgressInfo.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/tracking/ProgressInfo.java` | Adapted for Warly Edition; modification notice reviewed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/gui/RewardPage.java` | `carrot-1.21: src/main/java/com/cazsius/solcarrot/client/gui/ProgressGraph.java` | Adapted reward layout; notice completed 2026-10-13. |
| `src/main/java/com/xy177/solwarly/lunch/ContainerLunch.java` | `apple: f38436cb11c5b8d628e6f20f7b1670dbc811592d, item/foodcontainer implementation and behavior` | Warly adaptation; upstream notices retained 2026-10-13. |
| `src/main/java/com/xy177/solwarly/lunch/ItemLunchContainer.java` | `apple: f38436cb11c5b8d628e6f20f7b1670dbc811592d, item/foodcontainer implementation and behavior` | Warly adaptation; upstream notices retained 2026-10-13. |
| `src/main/java/com/xy177/solwarly/lunch/LunchInventory.java` | `apple: f38436cb11c5b8d628e6f20f7b1670dbc811592d, item/foodcontainer implementation and behavior` | Warly adaptation; upstream notices retained 2026-10-13. |
| `src/main/java/com/xy177/solwarly/lunch/LunchRegistry.java` | `apple: f38436cb11c5b8d628e6f20f7b1670dbc811592d, item/foodcontainer implementation and behavior` | Warly adaptation; upstream notices retained 2026-10-13. |
| `src/main/java/com/xy177/solwarly/lunch/LunchUpgradeRecipe.java` | `apple: f38436cb11c5b8d628e6f20f7b1670dbc811592d, item/foodcontainer implementation and behavior` | Warly adaptation; upstream notices retained 2026-10-13. |
| `src/main/java/com/xy177/solwarly/client/LunchScreen.java` | `apple: f38436cb11c5b8d628e6f20f7b1670dbc811592d, item/foodcontainer implementation and behavior` | Warly adaptation; upstream notices retained 2026-10-13. |
| `src/main/resources/assets/solcarrot/lang/en_us.json` | `carrot-1.21: src/main/resources/assets/solcarrot/lang/en_us.json` | Modified resource; per-file .license notice documented 2026-10-13. |
| `src/main/resources/assets/solcarrot/lang/zh_cn.json` | `carrot-1.21: src/main/resources/assets/solcarrot/lang/zh_cn.json` | Modified resource; per-file .license notice documented 2026-10-13. |
| `src/main/resources/assets/solcarrot/models/item/food_book.json` | `carrot-1.21: src/main/resources/assets/solcarrot/models/item/food_book.json` | Modified resource; per-file .license notice documented 2026-10-13. |
| `src/main/resources/assets/solcarrot/textures/gui/food_book.png` | `carrot-1.21: src/main/resources/assets/solcarrot/textures/gui/food_book.png` | Modified resource; per-file .license notice documented 2026-10-13. |
| `src/main/resources/data/solcarrot/recipe/food_book.json` | `carrot-1.21: src/main/resources/data/solcarrot/recipe/food_book.json` | Modified resource; per-file .license notice documented 2026-10-13. |
| `src/main/resources/META-INF/neoforge.mods.toml` | `carrot-1.21: src/main/resources/META-INF/neoforge.mods.toml` | Modified resource; per-file .license notice documented 2026-10-13. |
| `src/main/resources/pack.mcmeta` | `carrot-1.21: src/main/resources/pack.mcmeta` | Modified resource; per-file .license notice documented 2026-10-13. |
| `src/main/resources/assets/solcarrot/textures/forge/logo.png` | `carrot-old: src/main/resources/assets/solcarrot/textures/forge/logo.png, with upstream 1.8.4 compatibility baseline corrections` | Legacy compatibility resource; notice documented 2026-10-13. |
| `src/main/resources/assets/solcarrot/textures/items/food_book.png` | `carrot-old: src/main/resources/assets/solcarrot/textures/items/food_book.png` | Copied without content changes; compatible resource path. |
| `src/main/resources/assets/solwarly/textures/gui/lunch_inventory.png` | `apple: src/main/resources/assets/solapplepie/textures/gui/inventory.png` | Copied without content changes; resource path renamed. |
| `src/main/resources/assets/solwarly/textures/gui/lunch_slot.png` | `apple: src/main/resources/assets/solapplepie/textures/gui/slot.png` | Copied without content changes; resource path renamed. |
| `src/main/resources/assets/solwarly/textures/items/golden_lunchbox.png` | `apple: src/main/resources/assets/solapplepie/textures/items/golden_lunchbox.png` | Copied without content changes; resource path renamed. |
| `src/main/resources/assets/solwarly/textures/items/lunchbag.png` | `apple: src/main/resources/assets/solapplepie/textures/items/lunchbag.png` | Copied without content changes; resource path renamed. |
| `src/main/resources/assets/solwarly/textures/items/lunchbox.png` | `apple: src/main/resources/assets/solapplepie/textures/items/lunchbox.png` | Copied without content changes; resource path renamed. |
| `build.gradle` | `carrot-1.21: build.gradle` | Warly build adaptation; notice reviewed 2026-10-13. |
| `settings.gradle` | `carrot-1.21: settings.gradle` | Warly build adaptation; notice reviewed 2026-10-13. |
| `gradle/wrapper/gradle-wrapper.properties` | `carrot-1.21: gradle/wrapper/gradle-wrapper.properties` | Warly build adaptation; notice reviewed 2026-10-13. |

## No warranty

The software is provided WITHOUT ANY WARRANTY, including implied warranties of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, to the extent stated in LICENSE.txt and the applicable third-party licenses.
