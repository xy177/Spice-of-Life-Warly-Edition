# Third-Party Notices

Spice of Life Warly Edition, Minecraft 1.21.1.
Original contributors retain copyright in their respective work. Warly Edition modifications are by xy177.
No ownership of upstream work is claimed.

## Apple Pie source foundation

Source: https://github.com/txnimc/Spice-of-Life-Apple-Pie/tree/2d9de8b8d045b1f9b93940b681c74021d326f207
Branch: `1.20.1`. License: GNU Lesser General Public License v2.1; full text: `LICENSE.txt`.

Apple Pie components were ported and modified by xy177 on 2026-10-27 for Warly Edition.
The food-history index, food identity, public food-query interface, synchronization lifecycle,
food book and its UI elements follow Apple Pie. Warly keeps its own per-meal contributions,
flavor grouping, lifetime Food Experience, interests, rewards and seasoning rules rather than
substituting Apple's default score formula. Minecraft platform APIs are adapted for each version.

Warly's dietary state is stored under its own `solwarly:diet` identity. The separate Carrot API,
mod container, unique-food milestone implementation, legacy health floor and Carrot save/configuration
import have been removed. Only shared material also present in Apple Pie is retained from that ancestry.

The Food Book model, recipe and book art are adapted from Apple Pie. Lunch-container behavior and
textures also derive from Apple Pie's earlier source at
https://github.com/txnimc/Spice-of-Life-Apple-Pie/tree/f38436cb11c5b8d628e6f20f7b1670dbc811592d .
Resources have been moved to Warly's namespace. Warly's own logo and seasoning/grinder assets are
separate from the upstream book/container art.

## Inherited authors and acknowledgments

Apple Pie's upstream author record includes tarinoita. Its credit record acknowledges Vice,
creator of Apple Pie Edition, Kevun1, creator of Spice of Life: Potato Edition, and the
Spice of Life: Carrot Edition team. Apple Pie inherits work from Sweet Potato, Potato and Carrot;
those contributors' rights and acknowledgments remain in force.

The earlier shared Carrot ancestry is acknowledged here without retaining its exclusive compatibility code:
https://github.com/Cazsius/Spice-of-Life-Carrot-Edition .
Original author/credit records include Cazsius, Talonos and juliand665; modern branches also credit
Mrbysco. The upstream acknowledgments to Lewis_McReu for coding assistance and Vik for art are retained.
These records do not assign every resource to an individual contributor.

## Cyclic notice retained through the lunch-container upstream

The upstream Apple Pie files FoodContainer.java, FoodContainerCapabilityProvider.java and FoodContainerScreen.java carry this notice:

> Much of the following code was adapted from Cyclic's storage bag code.
> Copyright for portions of the code are held by Samson Basset (Lothrazar)
> as part of Cyclic, under the MIT license.

That upstream notice is retained here for the lunch-container adaptations. It does not attribute all of Warly Edition's rewritten container code to Cyclic.
The complete inherited MIT permission and warranty text is in licenses/Cyclic-MIT.txt, reproduced from Cyclic's trunk/1.19.2 LICENSE.
Source: https://github.com/Lothrazar/Cyclic/blob/trunk/1.19.2/LICENSE

## Current component sources and changes

| Warly component | Apple Pie source | Warly adaptation |
| --- | --- | --- |
| `tracking/FoodHistory.java` | `tracking/FoodList.java` | Recent-food index and preview; Warly contribution formula retained. |
| `tracking/FoodInstance.java` | `tracking/FoodInstance.java` | Food identity and registry encoding for each platform. |
| `tracking/FoodList.java`, `api/` | `tracking/FoodList.java`, `api/` | Read-only queries backed by one Warly diet record; no Carrot API or parallel archive. |
| `tracking/CapabilityHandler.java`, `communication/` | `tracking/CapabilityHandler.java`, `communication/` | Lifecycle and diet/rule snapshot synchronization for Forge/NeoForge. |
| `client/gui/Page.java`, `client/gui/elements/` | `client/gui/Page.java`, `client/gui/elements/` | Rendering and layout primitives; platform APIs, text fitting and icon scaling adapted. `ImageData` extracts Apple's nested `UIImage.Image`. |
| Food Book screen, item and item-list pages | `client/gui/FoodBookScreen.java`, `item/FoodBookItem.java`, `client/gui/ItemListPage.java` | Warly food/flavor catalog, interests, guide, language handling and menus. |
| `SOLWarlyConfig.java`, filtering | `SOLSweetPotatoConfig.java` | Shared filters and client controls; Warly settings; no legacy unique-food milestones. |
| `lunch/`, lunch screens and art | `item/foodcontainer/`, container art | Warly preview selection, food/NBT-preserving upgrades and platform ports. |
| Food Book art/model/recipe | `assets/solapplepie/`, `data/solapplepie/` | Warly resource identity and recipe format for each platform. |

Configuration summaries, reward timelines and their score-to-position calculations, seasoning,
accompaniments, grinders, culinary experience, interest selection and Warly mode are Warly-specific
implementations. A similar visual arrangement does not reintroduce the removed Carrot milestone subsystem.
Prominent per-file notices identify adapted files and the modification date. Historical source notices
are retained where necessary; they do not declare the removed compatibility code to be present.

## Distribution and warranty

Warly Edition is distributed under GNU LGPL v2.1, matching the license supplied by Apple Pie Edition.
The corresponding source is available at https://github.com/xy177/Spice-of-Life-Warly-Edition .
The software is provided WITHOUT ANY WARRANTY, as stated in `LICENSE.txt` and applicable third-party licenses.
