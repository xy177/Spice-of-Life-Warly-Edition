package com.xy177.solwarly.compat.emi;

import com.xy177.solwarly.lunch.*;
import com.xy177.solwarly.spice.*;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.*;
import dev.emi.emi.runtime.EmiReloadManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.SubscribeEvent;
import java.util.*;

/** Optional, native EMI integration; no JEI installation is needed. */
@EmiEntrypoint
public final class SpiceEmiPlugin implements EmiPlugin {
    private static final EmiRecipeCategory GRINDING = new EmiRecipeCategory(id("grinding"),
        EmiStack.of(SpiceRegistry.GRINDER.get())) {
        @Override public Component getName() { return Component.translatable("block.solwarly.spice_grinder"); }
    };
    private static boolean listening, refreshQueued;

    @Override public void register(EmiRegistry registry) {
        if (!listening) {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(this);
            listening = true;
        }
        registry.setDefaultComparison(SpiceRegistry.spice,
            Comparison.compareData(stack -> Seasoning.mixed(stack.getItemStack()) ? "mixed" : ItemSpice.flavor(stack.getItemStack())));
        registry.addCategory(GRINDING);
        registry.addWorkstation(GRINDING, EmiStack.of(SpiceRegistry.GRINDER.get()));
        // Invalidators run after every plugin, including EMI's optional JEI bridge.
        registry.removeRecipes(SpiceEmiPlugin::duplicate);
        List<SpiceRecipe> recipes = new ArrayList<>();
        for (SpiceRecipe recipe : SpiceRecipes.all()) if (!CustomSpices.owns(recipe)) recipes.add(recipe);
        recipes.addAll(CustomSpices.displayRecipes());
        for (int index = 0; index < recipes.size(); index++) {
            registry.addRecipe(new GrindingRecipe(recipes.get(index), id("/grinding/" + index)));
        }
        List<SpiceKind> kinds = SpiceKind.all().stream()
            .filter(kind -> !kind.accompaniment() && kind.available()).toList();
        for (SpiceKind kind : kinds) registry.addEmiStack(EmiStack.of(Seasoning.material(kind)));
        if (!kinds.isEmpty() && !SpiceConfig.mixedCatalyst().isEmpty()) {
            MixingRecipe mixing = new MixingRecipe(kinds.stream().filter(kind -> kind != SpiceKind.CREATIVE).toList());
            registry.addRecipe(mixing);
            registry.addEmiStack(mixing.getOutputs().get(0));
        }
        registry.addRecipe(upgrade(LunchRegistry.BAG.get(), Items.IRON_INGOT, LunchRegistry.BOX.get(), "lunchbox"));
        registry.addRecipe(upgrade(LunchRegistry.BOX.get(), Items.GOLD_INGOT, LunchRegistry.GOLD.get(), "golden_lunchbox"));
    }

    private static boolean duplicate(EmiRecipe recipe) {
        if (recipe instanceof NativeRecipe) return false;
        var holder = recipe.getBackingRecipe();
        Recipe<?> backing = holder == null ? null : holder.value();
        ResourceLocation recipeId = recipe.getId();
        return recipe.getCategory().getId().equals(GRINDING.getId())
            || backing instanceof MixedSpiceRecipe || backing instanceof LunchUpgradeRecipe
            || recipeId != null && recipeId.getNamespace().equals("solwarly")
                && Set.of("mixed_spice", "jei_mixing", "lunch_upgrade").contains(recipeId.getPath());
    }

    @SubscribeEvent public void changed(CustomSpices.DefinitionsChanged event) { refresh(); }
    @SubscribeEvent public void changed(SpiceConfig.MixingRecipeChanged event) { refresh(); }
    private static void refresh() {
        if (refreshQueued) return;
        refreshQueued = true;
        Minecraft.getInstance().execute(() -> {
            refreshQueued = false;
            if (Minecraft.getInstance().level != null) EmiReloadManager.reload();
        });
    }

    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("solwarly", path); }
    private interface NativeRecipe {}

    private static final class GrindingRecipe implements EmiRecipe, NativeRecipe {
        private final ResourceLocation id;
        private final List<EmiIngredient> inputs = new ArrayList<>();
        private final List<EmiStack> outputs;
        GrindingRecipe(SpiceRecipe recipe, ResourceLocation id) {
            this.id = id;
            inputs.add(EmiStack.of(recipe.input, recipe.inputCount).comparison(Comparison.compareComponents()));
            if (recipe.dual()) inputs.add(EmiStack.of(recipe.secondInput, recipe.secondCount).comparison(Comparison.compareComponents()));
            outputs = new ArrayList<>();
            outputs.add(EmiStack.of(recipe.result()));
            if (!recipe.returnedContainers().isEmpty()) outputs.add(EmiStack.of(recipe.returnedContainers()));
            if (!recipe.secondReturnedContainers().isEmpty()) outputs.add(EmiStack.of(recipe.secondReturnedContainers()));
        }
        @Override public EmiRecipeCategory getCategory() { return GRINDING; }
        @Override public ResourceLocation getId() { return id; }
        @Override public List<EmiIngredient> getInputs() { return inputs; }
        @Override public List<EmiStack> getOutputs() { return outputs; }
        @Override public int getDisplayWidth() { return 150; }
        @Override public int getDisplayHeight() { return 46; }
        @Override public void addWidgets(WidgetHolder widgets) {
            int x = (getDisplayWidth() - ((inputs.size() + outputs.size()) * 22 + 28)) / 2;
            for (int i = 0; i < inputs.size(); i++) widgets.addSlot(inputs.get(i), x + i * 22, 14);
            widgets.addTexture(EmiTexture.EMPTY_ARROW, x + inputs.size() * 22 + 2, 15);
            for (int i = 0; i < outputs.size(); i++) widgets.addSlot(outputs.get(i), x + inputs.size() * 22 + 32 + i * 22, 14).recipeContext(this);
        }
    }

    /** A single workbench entry; identical widget seeds keep all four stacks in sync. */
    private static final class MixingRecipe extends EmiPatternCraftingRecipe implements NativeRecipe {
        private final List<SpiceKind> kinds;
        private final List<EmiStack> outputs = new ArrayList<>();
        MixingRecipe(List<SpiceKind> kinds) {
            super(mixingInputs(kinds), EmiStack.of(Seasoning.mixedPowder(Collections.nCopies(SpiceConfig.mixedIngredients, kinds.get(0)), SpiceConfig.mixedCount)),
                id("/mixing"), true);
            this.kinds = List.copyOf(kinds);
            for (var row : MixingPreview.combinations(kinds)) outputs.add(EmiStack.of(Seasoning.mixedPowder(row, SpiceConfig.mixedCount)));
        }
        private static List<EmiIngredient> mixingInputs(List<SpiceKind> kinds) {
            EmiIngredient powders = EmiIngredient.of(kinds.stream().map(kind -> EmiStack.of(Seasoning.material(kind))).toList());
            ItemStack material = SpiceConfig.mixedCatalyst();
            EmiStack catalyst = EmiStack.of(material).comparison(Comparison.compareData(stack -> stack.getItemStack().getDamageValue()));
            if (!SpiceConfig.consumeMixedCatalyst) catalyst.setRemainder(catalyst.copy());
            else catalyst.setRemainder(EmiStack.of(material.getCraftingRemainingItem()));
            List<EmiIngredient> ingredients = new ArrayList<>(Collections.nCopies(9, EmiStack.EMPTY));
            for(int i=0;i<SpiceConfig.mixedIngredients;i++)ingredients.set(MixingPreview.gridSlot(i),powders);
            ingredients.set(MixingPreview.catalystSlot(),catalyst);return ingredients;
        }
        private List<SpiceKind> sample(Random random) { return MixingPreview.sample(kinds,random); }
        @Override public List<EmiStack> getOutputs() { return outputs; }
        @Override public SlotWidget getInputWidget(int slot, int x, int y) {
            int component = -1;
            for(int i=0;i<SpiceConfig.mixedIngredients;i++)if(slot==MixingPreview.gridSlot(i))component=i;
            final int index=component;
            if (component >= 0) return new GeneratedSlotWidget(random ->
                EmiStack.of(Seasoning.material(sample(random).get(index))), unique, x, y);
            SlotWidget widget = new SlotWidget(input.get(slot), x, y);
            if (slot == MixingPreview.catalystSlot() && !SpiceConfig.consumeMixedCatalyst) widget.appendTooltip(Component.translatable("solwarly.jei.not_consumed"));
            return widget;
        }
        @Override public SlotWidget getOutputWidget(int x, int y) {
            return new GeneratedSlotWidget(random -> EmiStack.of(Seasoning.mixedPowder(sample(random), SpiceConfig.mixedCount)), unique, x, y);
        }
    }

    private static EmiRecipe upgrade(Item source, Item metal, Item target, String name) {
        EmiIngredient ingot = EmiStack.of(metal);
        List<EmiIngredient> inputs = List.of(EmiStack.EMPTY, ingot, EmiStack.EMPTY, ingot,
            EmiStack.of(source), ingot, ingot, ingot, ingot);
        return new UpgradeRecipe(inputs, EmiStack.of(target), id("/" + name));
    }
    private static final class UpgradeRecipe extends EmiCraftingRecipe implements NativeRecipe {
        UpgradeRecipe(List<EmiIngredient> inputs, EmiStack output, ResourceLocation id) { super(inputs, output, id); }
        @Override public RecipeHolder<?> getBackingRecipe() {
            var level = Minecraft.getInstance().level;
            return level == null ? null : level.getRecipeManager().byKey(id("lunch_upgrade")).orElse(null);
        }
    }
}
