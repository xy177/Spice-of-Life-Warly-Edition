# Spice of Life Warly Edition

Spice of Life Warly Edition is a fork of [Spice of Life: Carrot Edition](https://www.curseforge.com/minecraft/mc-mods/spice-of-life-carrot-edition). Discover new dishes for lasting health rewards, keep your recent meals varied for extra benefits, and add seasonings to give familiar foods new flavors and effects.

## Two Ways to Enjoy Your Food

- **Culinary Experience** rewards discovery with extra maximum health. Your first taste of a dish earns its full value; trying additional flavors earns a smaller contribution. Plain food counts as a flavor, too.
- **Diversity** reflects your recent meals. It considers both their contributions and how many different foods you have eaten, providing rewards that change with your diet. Different flavors add variety, though less than entirely different dishes.
- **Food values** take nutrition and preparation complexity into account. Elaborate, nourishing meals generally contribute more than simple snacks.

## Season, Mix, and Serve

Turn ingredients into seasoning powders with the **Spice Grinding Mill**, then craft a powder together with food to give it a new flavor and additional effects.

- Different seasonings offer different benefits, including potion effects.
- Mixed powders combine their ingredients’ effects. Repeating an ingredient can increase its effect level or extend its duration.
- All mixed powders count as one mixed flavor for dietary tracking, regardless of their ingredients.
- **Accompaniments**, such as supported cooked rice, add nutrition and effects directly to a dish.

By default, a food accepts one powder application or up to two accompaniment servings. These limits—and whether powders and accompaniments can coexist—are configurable. Placeable feasts should be divided into edible servings before seasoning.

## A Mill That Works Without a Menu

The grinding mill accepts ingredients through right-clicking or automation and produces a batch every three seconds once processing begins.

- Right-click with an ingredient to insert complete recipe batches.
- Right-click with an empty hand to retrieve ingredients.
- Feed ingredients from above or the sides, and collect products from below.
- Sneak-right-click with a powder to bind the mill to its recipes. A rotating preview above the mill shows the product and ingredients; sneak-right-click with an empty hand to clear the binding.

The mill also supports recipes with two ingredients. An unbound mill can be forced into single-input operation with redstone.

## Your Food Book and Lunch Containers

The **Food Book** keeps your discoveries together: Culinary Experience, Diversity, reward milestones, daily food interests, and lists of foods you have or have not tried. Select an eaten food to see its remaining flavors; foods with every available flavor tasted have their own list.

Three portable containers help keep a varied menu handy:

| Container | Storage |
|---|---:|
| Lunchbag | 5 slots |
| Lunchbox | 9 slots |
| Golden Lunchbox | 14 slots |

Sneak-right-click to open a container. Use it normally to eat the available food predicted to give you the highest Diversity after the meal. Upgrading a container preserves its contents.

## A Changing Appetite

At sunrise, the server selects foods that players crave and foods they find boring. Craved foods contribute more to Diversity; boring foods contribute less.

Recent eating habits influence the selection, while randomness keeps the menu from becoming predictable. Check the Food Book or food tooltips to see the current interests. These interests do not change Culinary Experience gains.

## Optional Warly Mode

Want a stronger reason to change your menu? **Warly Mode** is disabled by default.

When enabled, repeatedly eating the same dish within two in-game days gradually restores less hunger and saturation, takes longer to eat, and contributes less Diversity. Different seasonings share the dish’s repetition history. Leave it alone for two days to reset that history.

Craved foods are exempt and do not extend the repetition timer. Culinary Experience remains unaffected. Administrators can enable the mode globally or override it for individual players.

## Getting Started

1. Craft a **Food Book** with a book and a carrot.
2. Try different foods to build Culinary Experience and establish a varied diet.
3. Build a **Spice Grinding Mill** and explore the available seasoning recipes.
4. Pack a lunch container to carry several meals wherever you go.

JEI displays grinding and mixed-powder recipes. The modern versions also support EMI.

## Make It Your Own

Configuration files are grouped under `config/solwarly/`, with English and Chinese descriptions. Adjust rewards, food values, recent-meal tracking, daily interests, Warly Mode, seasoning effects, mixing ingredients, and application limits.

You can also define your own seasonings and accompaniments through JSON files. Generated examples provide a starting point.

## Versions and Integrations

| Minecraft | Loader | Required additional mod |
|---|---|---|
| 1.12.2 | Forge | AppleCore |
| 1.20.1 | Forge | — |
| 1.21.1 | NeoForge | — |

Bring a few other mods into your kitchen and try even more flavors! The tables below count the extra powders, ready-to-use seasonings, and accompaniments they unlock. Each seasoning counts once, however many ways you can make or mix it.

A “+” means you’ll need both mods to enjoy those seasonings.

<details>
<summary>Minecraft 1.12.2 (Forge) — Seasonings from Other Mods</summary>

| Integration Mod | New Seasonings |
|---|---:|
| [Farmer's Delight Legacy](https://www.curseforge.com/minecraft/mc-mods/farmers-delight-legacy) | 4 |
| [End's Delight Legacy](https://www.curseforge.com/minecraft/mc-mods/ends-delight-legacy) | 1 |
| [Fruits Delight Legacy](https://www.curseforge.com/minecraft/mc-mods/fruits-delight-legact) | 22 |
| [Brewin' And Chewin' Legacy](https://www.curseforge.com/minecraft/mc-mods/brewin-and-chewin-legacy) | 2 |
| [Miner's Delight +](https://www.curseforge.com/minecraft/mc-mods/miners-delight-plus) + [Miner's Delight Bridge](https://www.curseforge.com/minecraft/mc-mods/miners-delight-bridge) | 1 |
| [The Twilight Forest](https://www.curseforge.com/minecraft/mc-mods/the-twilight-forest) + [Twilight Delight Legacy](https://www.curseforge.com/minecraft/mc-mods/twilight-delight-legacy) | 3 |
| [Twilight Spark's Delight](https://www.curseforge.com/minecraft/mc-mods/twilight-sparks-delight) | 4 |

</details>

<details>
<summary>Minecraft 1.20.1 (Forge) — Seasonings from Other Mods</summary>

| Integration Mod | New Seasonings |
|---|---:|
| [Farmer's Delight](https://www.curseforge.com/minecraft/mc-mods/farmers-delight) | 4 |
| [End's Delight](https://www.curseforge.com/minecraft/mc-mods/ends-delight) | 1 |
| [Fruits Delight](https://www.curseforge.com/minecraft/mc-mods/fruits-delight) | 22 |
| [Brewin' And Chewin'](https://www.curseforge.com/minecraft/mc-mods/brewin-and-chewin) | 2 |
| [Miner's Delight +](https://www.curseforge.com/minecraft/mc-mods/miners-delight-plus) | 1 |
| [The Twilight Forest](https://www.curseforge.com/minecraft/mc-mods/the-twilight-forest) + [Twilight's Flavors & Delight](https://www.curseforge.com/minecraft/mc-mods/twilights-flavors-delight) | 3 |
| [Twilight Spark's Delight](https://www.curseforge.com/minecraft/mc-mods/twilight-sparks-delight) | 4 |
| [Immortalers Delight](https://www.curseforge.com/minecraft/mc-mods/immortalers-delight) | 24 |

</details>

<details>
<summary>Minecraft 1.21.1 (NeoForge) — Seasonings from Other Mods</summary>

| Integration Mod | New Seasonings |
|---|---:|
| [Farmer's Delight](https://www.curseforge.com/minecraft/mc-mods/farmers-delight) | 4 |
| [End's Delight](https://www.curseforge.com/minecraft/mc-mods/ends-delight) | 1 |
| [Fruits Delight](https://www.curseforge.com/minecraft/mc-mods/fruits-delight) | 22 |
| [Brewin' And Chewin'](https://www.curseforge.com/minecraft/mc-mods/brewin-and-chewin) | 2 |
| [Miner's Delight +](https://www.curseforge.com/minecraft/mc-mods/miners-delight-plus) | 1 |
| [The Twilight Forest](https://www.curseforge.com/minecraft/mc-mods/the-twilight-forest) + [Twilight's Flavors & Delight](https://www.curseforge.com/minecraft/mc-mods/twilights-flavors-delight) | 3 |
| [Twilight Spark's Delight](https://www.curseforge.com/minecraft/mc-mods/twilight-sparks-delight) | 4 |
| [Immortalers Delight](https://www.curseforge.com/minecraft/mc-mods/immortalers-delight) | 24 |

</details>

Install Warly Edition **in place of Spice of Life: Carrot Edition**; do not install both together.

## Credits

Warly Edition is based on [Spice of Life: Carrot Edition](https://github.com/Cazsius/Spice-of-Life-Carrot-Edition), by Cazsius, Talonos, juliand665, and its contributors, including Mrbysco in the modern upstream. We also retain its credits to Lewis_McReu for coding assistance and Vik for artwork.

Some gameplay and items draw on [Spice of Life: Apple Pie Edition](https://github.com/txnimc/Spice-of-Life-Apple-Pie). Its lunchbag, lunchbox, golden lunchbox, and two lunch-container interface textures are reused under LGPL 2.1. Thanks to the Apple Pie project and its contributors, including the upstream credits to Vice, tarinoita, Kevun1, and the Carrot Edition team.

See [Third-Party Notices](THIRD_PARTY_NOTICES.md) for the original credit records, resource sources, modification records, and the inherited Cyclic/MIT notice.

## License and Source Code

Warly Edition is distributed under the [GNU Lesser General Public License v2.1](LICENSE.txt), the same license provided by Carrot Edition and Apple Pie Edition. The original contributors retain the copyright in their work; Warly Edition's modifications are by xy177. The software is provided without warranty, as described in the license.

Browse the source for [Minecraft 1.12.2](https://github.com/xy177/Spice-of-Life-Warly-Edition/tree/main), [Minecraft 1.20.1](https://github.com/xy177/Spice-of-Life-Warly-Edition/tree/1.20.1), or [Minecraft 1.21.1](https://github.com/xy177/Spice-of-Life-Warly-Edition/tree/1.21.1). When redistributing a build, provide its corresponding source and retain the applicable license and attribution notices.

Warly Edition is licensed under the GNU Lesser General Public License v2.1, as are Spice of Life: Carrot Edition and Spice of Life: Apple Pie Edition. For the code and resources reused from these projects, the required attribution, license texts, modification notices, and corresponding source are provided. This mod's use of those upstream materials therefore complies with their licensing terms, with no outstanding licensing issues identified.
