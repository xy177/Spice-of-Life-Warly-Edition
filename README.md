# Spice of Life Warly Edition

Spice of Life Warly Edition builds on [Spice of Life: Apple Pie Edition](https://www.curseforge.com/minecraft/mc-mods/spice-of-life-apple-pie-edition), adding its own blend of food discovery, dietary variety, and seasoning. Try new dishes for lasting health rewards, keep a varied menu for extra benefits, and give familiar meals a different flavor with powders and accompaniments.

## Good Food, Two Kinds of Progress

- **Culinary Experience** rewards trying something new. Your first taste of a dish earns its full value; discovering another flavor of that dish earns a smaller contribution. Plain food counts as a flavor, too. Reach milestones to gain extra maximum health.
- **Diversity** follows your recent meals. Both the meals' contributions and the variety of foods you eat matter. Its rewards change with your diet, so there is a reason to keep a few different dishes on the menu. Extra flavors help, though less than an entirely different food.
- **Nutrition and preparation** both influence a food's value. A meal with more involved preparation can contribute more than a simple snack. Pack makers can adjust individual food values and rewards.

## Season, Mix, and Serve

Use the **Spice Grinding Mill** to turn ingredients into seasoning powders, then craft a powder together with food. The food keeps its original identity and gains the seasoning's flavor and effects.

- **Seasoning powders** offer different benefits, including potion effects. Food tooltips show what your seasoned meal provides.
- **Mixed powders** combine their ingredients' effects. Repeating an ingredient can raise an effect's level or extend its duration.
- **Accompaniments**, such as supported cooked rice, add nutrition and effects directly to a dish without first becoming a powder.
- All mixed powders share one flavor for dietary tracking. Foods with two or more accompaniment servings likewise share one combined-accompaniment flavor; you do not need to try every possible pairing to complete the Food Book.

By default, each food accepts one powder application or up to two accompaniment servings. Application limits, the number of ingredients in mixed powders, and whether powders and accompaniments can share a dish are configurable. Divide placeable feasts into edible servings before seasoning them.

## A Mill Without a Menu

The grinding mill works through right-clicking and automation:

- Right-click with ingredients to insert complete recipe batches, or with an empty hand to retrieve them.
- Insert ingredients from above or the sides and collect products from below.
- Sneak-right-click with a powder to bind the mill to that powder's recipes. A rotating preview above it shows the product and ingredients. Sneak-right-click with an empty hand to clear the binding.
- Recipes can use one or two ingredients. Redstone forces an unbound mill into single-input operation.

Once processing begins, a batch takes three seconds. If an ingredient fits both a single-input and a two-input recipe, an unbound mill briefly waits for the second ingredient before committing to the single-input recipe. Returned containers and expelled items go into suitable storage below when possible.

## Keep Your Discoveries in a Food Book

Craft a **Food Book** with a book and a carrot. It brings together your progress, current food interests, and foods you have or have not tried.

Culinary Experience and Diversity milestones share a page. Hover over the progress lines and markers to check your score, rewards, and next target. Select an eaten food to see its remaining flavors; foods with every available flavor tasted move to their own list.

Three lunch containers help you take a varied menu along:

| Container | Storage |
|---|---:|
| Lunchbag | 5 slots |
| Lunchbox | 9 slots |
| Golden Lunchbox | 14 slots |

Sneak-right-click to open one. Use it normally to eat the available food predicted to leave you with the highest Diversity. Upgrading a container preserves its contents.

## A Changing Appetite

At sunrise, the server selects foods that players crave and foods they find boring. Craved foods contribute more to Diversity; boring foods contribute less. Recent eating habits influence the selection, with some randomness to keep things interesting.

Check the Food Book or a food's tooltip for today's interests. They affect the contribution of your next meal, not meals already recorded, and do not change Culinary Experience gains.

## Optional Warly Mode

Want a stronger reason to change your menu? **Warly Mode** is disabled by default and can be enabled globally or for individual players by an administrator.

In this mode, repeatedly eating the same dish within two in-game days restores progressively less hunger and saturation, takes longer, and contributes less Diversity. Seasoning a dish differently does not reset its repetition history. Each non-exempt meal restarts the two-day timer; leave that dish alone until the timer expires to start fresh.

Craved foods are exempt: they neither add repetitions nor restart the timer. Sleep advances the timer, while `/time add` does not. Culinary Experience remains unaffected.

## Getting Started

1. Make a **Food Book** and try a few different foods.
2. Build a **Spice Grinding Mill** and explore the seasoning recipes available in your pack.
3. Craft a powder or accompaniment together with a serving of food, then try the new flavor.
4. Pack a lunch container to keep several meal choices within reach.

**JEI** shows grinding and mixed-powder recipes in all three versions. The 1.20.1 and 1.21.1 versions also support **EMI**.

## Make It Your Own

Configuration files live in `config/solwarly/` and include English and Chinese descriptions. You can adjust rewards, food values, recent-meal tracking, daily food interests, Warly Mode, seasoning effects, mixing ingredients, and application limits. Fixed-interest lists can keep selected foods permanently craved or boring.

JSON files let pack makers add custom seasonings and accompaniments, including their names, food prefixes and suffixes, tooltip text, colors, nutrition, and potion effects as applicable. Start with the generated examples in `custom_spices/` and `custom_accompaniments/` inside the configuration folder.

## Versions and Integrations

| Minecraft | Loader | Required additional mod |
|---|---|---|
| 1.12.2 | Forge | AppleCore |
| 1.20.1 | Forge | — |
| 1.21.1 | NeoForge | — |

Other food mods can bring more flavors to your kitchen. The tables below count the extra powders, ready-to-use seasonings, and accompaniments each integration adds. Each kind counts once, regardless of how many recipes or mixtures use it. A “+” means both mods are needed.

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

The integrations listed above are optional, with their own dependencies.

## Credits

Warly Edition's current food-history and book components are adapted from [Spice of Life: Apple Pie Edition](https://github.com/txnimc/Spice-of-Life-Apple-Pie). Its lunchbag, lunchbox, golden lunchbox, and two lunch-container interface textures are also reused under LGPL 2.1. Thanks to the Apple Pie project and its contributors, including its credits to Vice, tarinoita, Kevun1, and the Carrot Edition team.

See [Third-Party Notices](THIRD_PARTY_NOTICES.md) for the detailed source records, resource credits, modification notices, and the inherited Cyclic/MIT notice.

## License and Source

Warly Edition is distributed under the [GNU Lesser General Public License v2.1](LICENSE.txt), the license supplied by Apple Pie Edition. Original contributors retain the copyright in their work. The full license, applicable third-party notices, and modification records are included with the project. The software is provided without warranty, as described in those licenses.

Source code is available for [Minecraft 1.12.2](https://github.com/xy177/Spice-of-Life-Warly-Edition/tree/main), [Minecraft 1.20.1](https://github.com/xy177/Spice-of-Life-Warly-Edition/tree/1.20.1), and [Minecraft 1.21.1](https://github.com/xy177/Spice-of-Life-Warly-Edition/tree/1.21.1). If you redistribute a build, provide its corresponding source and retain the applicable license and attribution notices.
