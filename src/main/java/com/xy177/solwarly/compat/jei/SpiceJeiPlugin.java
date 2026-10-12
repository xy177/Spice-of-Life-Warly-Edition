package com.xy177.solwarly.compat.jei;

import com.xy177.solwarly.block.BlockSpiceGrinder;
import com.xy177.solwarly.spice.*;
import mezz.jei.api.*;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.*;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.wrapper.ICustomCraftingRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import java.util.*;

@JEIPlugin
public final class SpiceJeiPlugin implements IModPlugin {
    private static final String GRINDING = "solwarly:grinding";
    private IJeiRuntime runtime;
    private IIngredientRegistry ingredients;
    private final List<Wrapper> grinding = new ArrayList<>();
    private MixingWrapper mixing;
    private List<ItemStack> customItems = Collections.emptyList();
    @Override public void registerItemSubtypes(ISubtypeRegistry registry) {
        registry.useNbtForSubtypes(SpiceRegistry.spice);
    }
    @Override public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper gui = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new Category(gui));
    }
    @Override public void register(IModRegistry registry) {
        ingredients = registry.getIngredientRegistry();
        updateRecipes();
        registry.addRecipes(grinding, GRINDING);
        registry.addRecipeCatalyst(new ItemStack(BlockSpiceGrinder.INSTANCE), GRINDING);
        if (mixing != null) registry.addRecipes(Collections.singletonList(mixing), VanillaRecipeCategoryUid.CRAFTING);
        customItems = customItems();
    }
    private void updateRecipes() {
        grinding.clear();
        for (SpiceRecipe recipe : SpiceRecipes.all()) if (!CustomSpices.owns(recipe)) grinding.add(Wrapper.grinding(recipe));
        for (SpiceRecipe recipe : CustomSpices.displayRecipes()) grinding.add(Wrapper.grinding(recipe));
        List<SpiceKind> powders = new ArrayList<>();
        for (SpiceKind kind : SpiceKind.all()) if (kind != SpiceKind.RICE && kind.available()) powders.add(kind);
        mixing = powders.isEmpty() || SpiceConfig.mixedCatalyst().isEmpty() ? null : new MixingWrapper(powders);
    }
    private List<ItemStack> customItems() {
        List<ItemStack> result = new ArrayList<>();
        for (CustomSpice spice : CustomSpices.all()) result.add(Seasoning.powder(Collections.singletonList(spice), 1));
        return result;
    }
    @Override public void onRuntimeAvailable(IJeiRuntime runtime) {
        this.runtime = runtime;
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }
    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public void customDefinitionsChanged(CustomSpices.DefinitionsChanged event) {
        refreshRecipes();
    }
    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public void mixingRecipeChanged(SpiceConfig.MixingRecipeChanged event) {
        refreshRecipes();
    }
    private void refreshRecipes() {
        if (runtime == null) return;
        IRecipeRegistry recipes = runtime.getRecipeRegistry();
        for (Wrapper wrapper : grinding) recipes.removeRecipe(wrapper, GRINDING);
        if (mixing != null) recipes.removeRecipe(mixing, VanillaRecipeCategoryUid.CRAFTING);
        if (!customItems.isEmpty()) ingredients.removeIngredientsAtRuntime(VanillaTypes.ITEM, customItems);
        updateRecipes(); customItems = customItems();
        if (!customItems.isEmpty()) ingredients.addIngredientsAtRuntime(VanillaTypes.ITEM, customItems);
        for (Wrapper wrapper : grinding) recipes.addRecipe(wrapper, GRINDING);
        if (mixing != null) recipes.addRecipe(mixing, VanillaRecipeCategoryUid.CRAFTING);
    }
    public static final class Wrapper implements IRecipeWrapper {
        final List<ItemStack> inputs, outputs;
        Wrapper(List<ItemStack> inputs, List<ItemStack> outputs) { this.inputs = inputs; this.outputs = outputs; }
        static Wrapper grinding(SpiceRecipe recipe) {
            ItemStack input = recipe.input.copy(); input.setCount(recipe.inputCount);
            List<ItemStack> outputs = new ArrayList<>(); outputs.add(recipe.result());
            if (!recipe.returnedContainers().isEmpty()) outputs.add(recipe.returnedContainers());
            return new Wrapper(Collections.singletonList(input), outputs);
        }
        @Override public void getIngredients(IIngredients ingredients) {
            ingredients.setInputs(VanillaTypes.ITEM, inputs); ingredients.setOutputs(VanillaTypes.ITEM, outputs);
        }
    }
    static final class MixingWrapper implements ICustomCraftingRecipeWrapper {
        private static final int[] INPUT_SLOTS = {1, 2, 4};
        final List<List<ItemStack>> inputLists = Arrays.asList(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
            Collections.singletonList(SpiceConfig.mixedCatalyst()));
        private final boolean consumesCatalyst = SpiceConfig.consumeMixedCatalyst;
        final List<ItemStack> outputs = new ArrayList<>();
        private final List<Integer> displayedRecipes = new ArrayList<>();
        private final Random random;
        private IGuiItemStackGroup stacks;
        private long lastUpdate = -1, elapsed;
        private long displayedStep = -1;

        MixingWrapper(List<SpiceKind> powders) {
            this(powders, new Random());
        }
        MixingWrapper(List<SpiceKind> powders, Random random) {
            this.random = random;
            for (int a = 0; a < powders.size(); a++) for (int b = a; b < powders.size(); b++) for (int c = b; c < powders.size(); c++) {
                List<SpiceKind> kinds = Arrays.asList(powders.get(a), powders.get(b), powders.get(c));
                for (int i = 0; i < 3; i++) inputLists.get(i).add(Seasoning.powder(Collections.singletonList(kinds.get(i)), 1));
                outputs.add(Seasoning.powder(kinds, SpiceConfig.mixedCount));
            }
        }
        @Override public void getIngredients(IIngredients ingredients) {
            ingredients.setInputLists(VanillaTypes.ITEM, inputLists);
            ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(outputs));
        }
        @Override public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
            stacks = layout.getItemStacks();
            // Select complete recipes before clearing JEI's per-slot focus filtering.
            IFocus<?> focus = layout.getFocus();
            displayedRecipes.clear();
            for (int i = 0; i < outputs.size(); i++) if (matchesFocus(i, focus)) displayedRecipes.add(i);
            if (displayedRecipes.isEmpty()) for (int i = 0; i < outputs.size(); i++) displayedRecipes.add(i);
            stacks.setOverrideDisplayFocus(null);
            stacks.set(5, inputLists.get(3).get(0));
            stacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
                if (input && slotIndex == 5 && !consumesCatalyst) tooltip.add(I18n.format("solwarly.jei.not_consumed"));
            });
            layout.setShapeless();
            lastUpdate = -1; elapsed = 0; displayedStep = -1;
            updateDisplay(0, false);
            lastUpdate = -1;
        }
        private boolean matchesFocus(int index, IFocus<?> focus) {
            if (focus == null || !(focus.getValue() instanceof ItemStack)) return true;
            ItemStack value = (ItemStack) focus.getValue();
            if (focus.getMode() == IFocus.Mode.OUTPUT) return same(outputs.get(index), value);
            if (ItemStack.areItemsEqual(value, inputLists.get(3).get(0))) return true;
            for (int i = 0; i < 3; i++) if (same(inputLists.get(i).get(index), value)) return true;
            return false;
        }
        private static boolean same(ItemStack first, ItemStack second) {
            return ItemStack.areItemsEqual(first, second) && ItemStack.areItemStackTagsEqual(first, second);
        }
        void updateDisplay(long time, boolean paused) {
            if (stacks == null || displayedRecipes.isEmpty()) return;
            if (lastUpdate >= 0 && !paused) elapsed += Math.max(0, time - lastUpdate);
            lastUpdate = time;
            long step = elapsed / 1000;
            if (step == displayedStep) return;
            displayedStep = step;
            int frame = (int) (step % displayedRecipes.size());
            if (frame == 0) Collections.shuffle(displayedRecipes, random);
            int index = displayedRecipes.get(frame);
            // Set single stacks together: JEI truncates/filter-cycles lists independently.
            for (int i = 0; i < 3; i++) stacks.set(INPUT_SLOTS[i], inputLists.get(i).get(index));
            stacks.set(0, outputs.get(index));
        }
        @Override public void drawInfo(Minecraft mc, int width, int height, int mouseX, int mouseY) {
            updateDisplay(System.nanoTime() / 1000000, GuiScreen.isShiftKeyDown());
        }
    }
    private static final class Category implements IRecipeCategory<Wrapper> {
        final IDrawable background, icon, slot, arrow;
        Category(IGuiHelper gui) {
            background = gui.createBlankDrawable(150, 54); slot = gui.getSlotDrawable();
            icon = gui.createDrawableIngredient(new ItemStack(BlockSpiceGrinder.INSTANCE));
            arrow = gui.createDrawable(new net.minecraft.util.ResourceLocation("textures/gui/container/furnace.png"), 79, 34, 24, 17);
        }
        @Override public String getUid() { return GRINDING; }
        @Override public String getTitle() { return I18n.format("tile.solwarly.spice_grinder.name"); }
        @Override public String getModName() { return "Spice of Life Warly Edition"; }
        @Override public IDrawable getBackground() { return background; }
        @Override public IDrawable getIcon() { return icon; }
        @Override public void drawExtras(Minecraft mc) {
            slot.draw(mc, 23, 9);
            arrow.draw(mc, 57, 10); slot.draw(mc, 94, 9); slot.draw(mc, 123, 9);
        }
        @Override public void setRecipe(IRecipeLayout layout, Wrapper recipe, IIngredients ingredients) {
            IGuiItemStackGroup stacks = layout.getItemStacks();
            for (int i = 0; i < recipe.inputs.size(); i++) {
                stacks.init(i, true, 23, 9); stacks.set(i, recipe.inputs.get(i));
            }
            for (int i = 0; i < recipe.outputs.size(); i++) {
                stacks.init(4 + i, false, i == 0 ? 94 : 123, 9); stacks.set(4 + i, recipe.outputs.get(i));
            }
        }
    }
}
