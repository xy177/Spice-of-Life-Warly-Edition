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

Optional integrations with Farmer’s Delight and supported addons provide additional ingredients and seasonings. Available integrations vary by Minecraft version.

Install Warly Edition **in place of Spice of Life: Carrot Edition**; do not install both together.

## Credits

This mod is based on [Spice of Life: Carrot Edition](https://github.com/Cazsius/Spice-of-Life-Carrot-Edition).<br>
Some gameplay mechanics and items are inspired by [Spice of Life: Apple Pie Edition](https://github.com/txnimc/Spice-of-Life-Apple-Pie).
