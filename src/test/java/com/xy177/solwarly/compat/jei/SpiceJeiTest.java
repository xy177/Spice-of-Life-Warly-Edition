package com.xy177.solwarly.compat.jei;
import com.xy177.solwarly.spice.*;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.*;
import mezz.jei.api.recipe.*;
import net.minecraft.init.*;
import net.minecraft.client.resources.*;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.After;
import java.util.*;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
public class SpiceJeiTest {
    private String previousCatalyst;
    private int previousMetadata;
    private boolean previousConsume;
    @Before public void defaultCatalyst() {
        previousCatalyst = SpiceConfig.mixedCatalystItem; previousMetadata = SpiceConfig.mixedCatalystMetadata;
        previousConsume = SpiceConfig.consumeMixedCatalyst;
        SpiceConfig.mixedCatalystItem = "minecraft:nether_star"; SpiceConfig.mixedCatalystMetadata = 0;
        SpiceConfig.consumeMixedCatalyst = false;
    }
    @After public void restoreCatalyst() {
        SpiceConfig.mixedCatalystItem = previousCatalyst; SpiceConfig.mixedCatalystMetadata = previousMetadata;
        SpiceConfig.consumeMixedCatalyst = previousConsume;
    }
    @BeforeClass public static void bootstrap() {
        Bootstrap.register();
        if (!ForgeRegistries.ITEMS.containsKey(new ResourceLocation("solwarly:spice"))) ForgeRegistries.ITEMS.register(SpiceRegistry.spice);
    }
    @Test public void grindingShowsBatchCountsAndReturnedBucketWithoutMutatingRecipe() {
        SpiceRecipe recipe = new SpiceRecipe(new ItemStack(Items.WATER_BUCKET),1,
            ItemSpice.create("salt",0xeeeeee,1),3,new ItemStack(Items.BUCKET));
        SpiceJeiPlugin.Wrapper wrapper = SpiceJeiPlugin.Wrapper.grinding(recipe);
        assertEquals(1,wrapper.inputs.get(0).getCount()); assertEquals(3,wrapper.outputs.get(0).getCount());
        assertEquals(Items.BUCKET,wrapper.outputs.get(1).getItem()); assertEquals(1,wrapper.outputs.get(1).getCount());
        assertEquals(1,recipe.output.getCount());
        IIngredients ingredients = mock(IIngredients.class); wrapper.getIngredients(ingredients);
        verify(ingredients).setInputs(VanillaTypes.ITEM,wrapper.inputs);
        verify(ingredients).setOutputs(VanillaTypes.ITEM,wrapper.outputs);
    }
    @Test public void threeInputsShowThreeReturnedContainers() {
        SpiceRecipe recipe = new SpiceRecipe(new ItemStack(Items.SUGAR),3,
            ItemSpice.create("sugar",0xEBC978,1),1,new ItemStack(Items.GLASS_BOTTLE));
        SpiceJeiPlugin.Wrapper wrapper = SpiceJeiPlugin.Wrapper.grinding(recipe);
        assertEquals(3,wrapper.inputs.get(0).getCount()); assertEquals(3,wrapper.outputs.get(1).getCount());
    }
    @Test @SuppressWarnings({"unchecked", "rawtypes"}) public void registersOneMixedEntryInCraftingCategory() {
        IModRegistry registry = mock(IModRegistry.class);
        new SpiceJeiPlugin().register(registry);
        ArgumentCaptor<List> recipes = ArgumentCaptor.forClass(List.class);
        verify(registry).addRecipes(recipes.capture(), eq(VanillaRecipeCategoryUid.CRAFTING));
        assertEquals(1, recipes.getValue().size());
        assertTrue(recipes.getValue().get(0) instanceof SpiceJeiPlugin.MixingWrapper);
        verify(registry, never()).addRecipes(anyList(), eq("solwarly:mixing"));
    }
    @Test @SuppressWarnings({"unchecked", "rawtypes"}) public void serverDefinitionsUpdateJeiIngredientsGrindingAndMixedCrafting() throws Exception {
        CustomSpices.clearServerDefinitions();
        SpiceJeiPlugin plugin = new SpiceJeiPlugin();
        IModRegistry registry = mock(IModRegistry.class);
        IIngredientRegistry ingredients = strictIngredients();
        when(registry.getIngredientRegistry()).thenReturn(ingredients);
        plugin.register(registry);
        mezz.jei.api.IJeiRuntime runtime = mock(mezz.jei.api.IJeiRuntime.class);
        IRecipeRegistry recipes = mock(IRecipeRegistry.class);
        when(runtime.getRecipeRegistry()).thenReturn(recipes);
        // Plain JUnit does not run Forge's event-subclass transformer; call the handler directly.
        java.lang.reflect.Field runtimeField = SpiceJeiPlugin.class.getDeclaredField("runtime");
        runtimeField.setAccessible(true); runtimeField.set(plugin, runtime);
        try (java.io.Reader reader = new java.io.InputStreamReader(getClass().getResourceAsStream(
                "/assets/solwarly/custom_spices/_example.json"), java.nio.charset.StandardCharsets.UTF_8)) {
            com.google.gson.JsonObject json = new com.google.gson.JsonParser().parse(reader).getAsJsonObject();
            net.minecraft.nbt.NBTTagList snapshot = new net.minecraft.nbt.NBTTagList();
            snapshot.appendTag(new net.minecraft.nbt.NBTTagString(json.toString()));
            CustomSpices.readSnapshot(snapshot);
            plugin.customDefinitionsChanged(new CustomSpices.DefinitionsChanged());
            ArgumentCaptor<Collection> items = ArgumentCaptor.forClass(Collection.class);
            verify(ingredients).addIngredientsAtRuntime(eq(VanillaTypes.ITEM), items.capture());
            assertEquals(1, items.getValue().size());
            assertEquals("custom:herb_powder", ItemSpice.flavor((ItemStack)items.getValue().iterator().next()));
            verify(ingredients, never()).removeIngredientsAtRuntime(eq(VanillaTypes.ITEM), anyCollection());
            ArgumentCaptor<mezz.jei.api.recipe.IRecipeWrapper> grinding = ArgumentCaptor.forClass(mezz.jei.api.recipe.IRecipeWrapper.class);
            verify(recipes, atLeastOnce()).addRecipe(grinding.capture(), eq("solwarly:grinding"));
            SpiceJeiPlugin.Wrapper herb = (SpiceJeiPlugin.Wrapper)grinding.getAllValues().stream()
                .filter(wrapper -> "custom:herb_powder".equals(ItemSpice.flavor(((SpiceJeiPlugin.Wrapper)wrapper).outputs.get(0))))
                .findFirst().get();
            assertEquals(3, herb.inputs.get(0).getCount()); assertEquals(1, herb.inputs.get(0).getMetadata());
            ArgumentCaptor<mezz.jei.api.recipe.IRecipeWrapper> mixed = ArgumentCaptor.forClass(mezz.jei.api.recipe.IRecipeWrapper.class);
            verify(recipes).addRecipe(mixed.capture(), eq(VanillaRecipeCategoryUid.CRAFTING));
            SpiceJeiPlugin.MixingWrapper blend = (SpiceJeiPlugin.MixingWrapper)mixed.getValue();
            assertTrue(blend.outputs.stream().anyMatch(stack -> ItemSpice.flavor(stack).contains("custom:herb_powder")));
            Display display = new Display(blend, null); display.assertCraftsOutput();
            clearInvocations(ingredients, recipes);
            CustomSpices.clearServerDefinitions();
            plugin.customDefinitionsChanged(new CustomSpices.DefinitionsChanged());
            verify(ingredients).removeIngredientsAtRuntime(eq(VanillaTypes.ITEM), argThat(stacks -> stacks.size() == 1));
            verify(ingredients, never()).addIngredientsAtRuntime(eq(VanillaTypes.ITEM), anyCollection());
            verify(recipes).removeRecipe(herb, "solwarly:grinding");
        } finally {
            CustomSpices.clearServerDefinitions();
        }
    }
    @Test @SuppressWarnings("unchecked") public void emptyServerDefinitionsKeepGrindingAndMixingAfterLoginAndDisconnect() throws Exception {
        CustomSpices.clearServerDefinitions();
        java.lang.reflect.Field recipeField = SpiceRecipes.class.getDeclaredField("RECIPES");
        recipeField.setAccessible(true);
        List<SpiceRecipe> registered = (List<SpiceRecipe>)recipeField.get(null);
        List<SpiceRecipe> previous = new ArrayList<>(registered);
        try {
            registered.clear();
            SpiceRecipes.register(new SpiceRecipe(new ItemStack(Items.SUGAR), 3,
                ItemSpice.create("sugar", 0xEBC978, 1), 1, ItemStack.EMPTY));
            SpiceJeiPlugin plugin = new SpiceJeiPlugin();
            IModRegistry registry = mock(IModRegistry.class);
            IIngredientRegistry ingredients = strictIngredients();
            when(registry.getIngredientRegistry()).thenReturn(ingredients);
            plugin.register(registry);
            mezz.jei.api.IJeiRuntime runtime = mock(mezz.jei.api.IJeiRuntime.class);
            IRecipeRegistry recipes = mock(IRecipeRegistry.class);
            when(runtime.getRecipeRegistry()).thenReturn(recipes);
            java.lang.reflect.Field runtimeField = SpiceJeiPlugin.class.getDeclaredField("runtime");
            runtimeField.setAccessible(true); runtimeField.set(plugin, runtime);
            for (int transition = 0; transition < 2; transition++) {
                if (transition == 0) CustomSpices.readSnapshot(new net.minecraft.nbt.NBTTagList());
                else CustomSpices.clearServerDefinitions();
                plugin.customDefinitionsChanged(new CustomSpices.DefinitionsChanged());
                ArgumentCaptor<mezz.jei.api.recipe.IRecipeWrapper> grinding =
                    ArgumentCaptor.forClass(mezz.jei.api.recipe.IRecipeWrapper.class);
                verify(recipes).addRecipe(grinding.capture(), eq("solwarly:grinding"));
                SpiceJeiPlugin.Wrapper restored = (SpiceJeiPlugin.Wrapper)grinding.getValue();
                assertEquals(Items.SUGAR, restored.inputs.get(0).getItem());
                assertEquals("sugar", ItemSpice.flavor(restored.outputs.get(0)));
                verify(recipes).removeRecipe(any(SpiceJeiPlugin.Wrapper.class), eq("solwarly:grinding"));
                verify(recipes).addRecipe(any(SpiceJeiPlugin.MixingWrapper.class), eq(VanillaRecipeCategoryUid.CRAFTING));
                verifyZeroInteractions(ingredients);
                clearInvocations(recipes);
            }
        } finally {
            registered.clear(); registered.addAll(previous);
            CustomSpices.clearServerDefinitions();
        }
    }
    private static IIngredientRegistry strictIngredients() {
        IIngredientRegistry ingredients = mock(IIngredientRegistry.class);
        org.mockito.stubbing.Answer<Void> requireItems = call -> {
            if (((Collection<?>)call.getArgument(1)).isEmpty())
                throw new IllegalArgumentException("ingredients must not be empty.");
            return null;
        };
        doAnswer(requireItems).when(ingredients).addIngredientsAtRuntime(eq(VanillaTypes.ITEM), anyCollection());
        doAnswer(requireItems).when(ingredients).removeIngredientsAtRuntime(eq(VanillaTypes.ITEM), anyCollection());
        return ingredients;
    }
    @Test public void everyDisplayedCombinationMatchesRealCraftingIncludingAfterJeiListLimit() {
        SpiceJeiPlugin.MixingWrapper wrapper = new SpiceJeiPlugin.MixingWrapper(Arrays.asList(
            SpiceKind.SUGAR, SpiceKind.SALT, SpiceKind.TOMATO, SpiceKind.ONION, SpiceKind.HAM,
            SpiceKind.SPICY, SpiceKind.CHORUS, SpiceKind.DRAGON), new Random(47));
        assertEquals(120, wrapper.outputs.size());
        IIngredients ingredients = mock(IIngredients.class); wrapper.getIngredients(ingredients);
        verify(ingredients).setInputLists(VanillaTypes.ITEM, wrapper.inputLists);
        verify(ingredients).setOutputLists(VanillaTypes.ITEM, Collections.singletonList(wrapper.outputs));
        Display display = new Display(wrapper, null);
        Set<String> seen = new HashSet<>();
        List<String> firstRound = new ArrayList<>(), secondRound = new ArrayList<>();
        for (int frame = 0; frame < wrapper.outputs.size() * 2; frame++) {
            wrapper.updateDisplay(frame * 1000L, false);
            display.assertCraftsOutput();
            String flavor = ItemSpice.flavor(display.items.get(0));
            if (frame < wrapper.outputs.size()) { seen.add(flavor); firstRound.add(flavor); }
            else secondRound.add(flavor);
        }
        List<String> ordered = new ArrayList<>();
        for (ItemStack output : wrapper.outputs) ordered.add(ItemSpice.flavor(output));
        assertEquals(new HashSet<>(ordered), seen);
        assertNotEquals(ordered, firstRound);
        assertNotEquals(firstRound, secondRound);
        assertEquals(4, wrapper.inputLists.size());
        assertEquals(5, display.items.size()); // Three powders, one star and output; star is not an extra output.
    }
    @Test public void shiftPausesAllSlotsTogether() {
        SpiceJeiPlugin.MixingWrapper wrapper = mixing();
        Display display = new Display(wrapper, null);
        wrapper.updateDisplay(0, false);
        wrapper.updateDisplay(1000, false);
        net.minecraft.nbt.NBTTagCompound before = display.items.get(0).getTagCompound().copy();
        wrapper.updateDisplay(10000, true);
        assertEquals(before, display.items.get(0).getTagCompound());
        wrapper.updateDisplay(11000, false);
        assertNotEquals(before, display.items.get(0).getTagCompound());
        display.assertCraftsOutput();
    }
    @Test @SuppressWarnings("unchecked") public void outputFocusShowsItsMatchingInputsThroughoutCycling() {
        SpiceJeiPlugin.MixingWrapper wrapper = mixing();
        ItemStack target = wrapper.outputs.get(2).copy(); target.setCount(64);
        IFocus<ItemStack> focus = mock(IFocus.class);
        when(focus.getValue()).thenReturn(target); when(focus.getMode()).thenReturn(IFocus.Mode.OUTPUT);
        Display display = new Display(wrapper, focus);
        for (int i = 0; i < 6; i++) {
            wrapper.updateDisplay(i * 1000L, false);
            assertEquals(target.getTagCompound(), display.items.get(0).getTagCompound());
            display.assertCraftsOutput();
        }
        verify(display.stacks).setOverrideDisplayFocus(null);
    }
    @Test @SuppressWarnings("unchecked") public void inputFocusCyclesOnlyCombinationsUsingThatPowder() {
        SpiceJeiPlugin.MixingWrapper wrapper = mixing();
        IFocus<ItemStack> focus = mock(IFocus.class);
        when(focus.getValue()).thenReturn(Seasoning.powder(Collections.singletonList(SpiceKind.SALT), 1));
        when(focus.getMode()).thenReturn(IFocus.Mode.INPUT);
        Display display = new Display(wrapper, focus);
        Set<String> recipes = new HashSet<>();
        for (int i = 0; i < 6; i++) {
            wrapper.updateDisplay(i * 1000L, false);
            assertTrue(Seasoning.components(display.items.get(0)).contains(SpiceKind.SALT));
            recipes.add(ItemSpice.flavor(display.items.get(0)));
            display.assertCraftsOutput();
        }
        assertEquals(3, recipes.size());
    }
    @Test @SuppressWarnings({"unchecked", "rawtypes"}) public void notConsumedTooltipAppearsOnlyOnReusableCatalyst() throws Exception {
        SpiceConfig.mixedCatalystItem = "minecraft:diamond";
        Display display = new Display(mixing(), null);
        ArgumentCaptor<ITooltipCallback> callback = ArgumentCaptor.forClass(ITooltipCallback.class);
        verify(display.stacks).addTooltipCallback(callback.capture());
        List<String> tooltip = new ArrayList<>();
        java.lang.reflect.Field field = I18n.class.getDeclaredField("i18nLocale"); field.setAccessible(true);
        Object previous = field.get(null);
        try {
            IResourceManager manager = mock(IResourceManager.class);
            when(manager.getResourceDomains()).thenReturn(Collections.singleton("solwarly"));
            when(manager.getAllResources(any(ResourceLocation.class))).thenAnswer(call -> {
                ResourceLocation location = call.getArgument(0);
                java.io.InputStream stream = getClass().getResourceAsStream("/assets/" + location.getNamespace() + "/" + location.getPath());
                if (stream == null) return Collections.emptyList();
                IResource resource = mock(IResource.class); when(resource.getInputStream()).thenReturn(stream);
                return Collections.singletonList(resource);
            });
            for (String language : Arrays.asList("zh_cn", "en_us")) {
                net.minecraft.client.resources.Locale locale = new net.minecraft.client.resources.Locale();
                locale.loadLocaleDataFiles(manager, Collections.singletonList(language)); field.set(null, locale);
                tooltip.clear(); callback.getValue().onTooltip(5, true, display.items.get(5), tooltip);
                assertEquals(Collections.singletonList(language.equals("zh_cn") ? "不消耗" : "Not consumed"), tooltip);
            }
            tooltip.clear();
            callback.getValue().onTooltip(1, true, display.items.get(1), tooltip);
            callback.getValue().onTooltip(0, false, display.items.get(0), tooltip);
            assertTrue(tooltip.isEmpty());
        } finally { field.set(null, previous); }
    }
    @Test @SuppressWarnings({"unchecked", "rawtypes"}) public void replacementCatalystFocusShowsMatchingRecipesAndConsumptionControlsTooltip() {
        SpiceConfig.mixedCatalystItem = "minecraft:dye"; SpiceConfig.mixedCatalystMetadata = 4;
        IFocus<ItemStack> focus = mock(IFocus.class);
        when(focus.getValue()).thenReturn(new ItemStack(Items.DYE, 12, 4)); when(focus.getMode()).thenReturn(IFocus.Mode.INPUT);
        Display reusable = new Display(mixing(), focus);
        assertEquals(Items.DYE, reusable.items.get(5).getItem()); assertEquals(4, reusable.items.get(5).getMetadata());
        assertEquals(4, reusable.items.size() - 1);
        for (int i = 0; i < 4; i++) { reusable.assertCraftsOutput(); reusable.wrapper.updateDisplay(i * 1000L, false); }
        ArgumentCaptor<ITooltipCallback> callback = ArgumentCaptor.forClass(ITooltipCallback.class);
        List<String> tooltip = new ArrayList<>();
        SpiceConfig.consumeMixedCatalyst = true;
        Display consumed = new Display(mixing(), focus);
        consumed.assertCraftsOutput();
        verify(consumed.stacks).addTooltipCallback(callback.capture());
        tooltip.clear(); callback.getValue().onTooltip(5, true, consumed.items.get(5), tooltip);
        assertTrue(tooltip.isEmpty());
    }
    @Test public void mixingSettingsRefreshJeiWithoutAnyCustomSpiceChange() throws Exception {
        CustomSpices.clearServerDefinitions();
        SpiceJeiPlugin plugin = new SpiceJeiPlugin();
        IModRegistry registry = mock(IModRegistry.class);
        IIngredientRegistry ingredients = strictIngredients();
        when(registry.getIngredientRegistry()).thenReturn(ingredients); plugin.register(registry);
        mezz.jei.api.IJeiRuntime runtime = mock(mezz.jei.api.IJeiRuntime.class);
        IRecipeRegistry recipes = mock(IRecipeRegistry.class); when(runtime.getRecipeRegistry()).thenReturn(recipes);
        java.lang.reflect.Field runtimeField = SpiceJeiPlugin.class.getDeclaredField("runtime");
        runtimeField.setAccessible(true); runtimeField.set(plugin, runtime);
        SpiceConfig.mixedCatalystItem = "minecraft:diamond"; SpiceConfig.consumeMixedCatalyst = true;
        plugin.mixingRecipeChanged(new SpiceConfig.MixingRecipeChanged());
        ArgumentCaptor<mezz.jei.api.recipe.IRecipeWrapper> restored = ArgumentCaptor.forClass(mezz.jei.api.recipe.IRecipeWrapper.class);
        verify(recipes).removeRecipe(any(SpiceJeiPlugin.MixingWrapper.class), eq(VanillaRecipeCategoryUid.CRAFTING));
        verify(recipes).addRecipe(restored.capture(), eq(VanillaRecipeCategoryUid.CRAFTING));
        Display display = new Display((SpiceJeiPlugin.MixingWrapper)restored.getValue(), null);
        assertEquals(Items.DIAMOND, display.items.get(5).getItem()); display.assertCraftsOutput();
    }
    private SpiceJeiPlugin.MixingWrapper mixing() {
        return new SpiceJeiPlugin.MixingWrapper(Arrays.asList(SpiceKind.SUGAR, SpiceKind.SALT));
    }
    private static final class Display {
        final SpiceJeiPlugin.MixingWrapper wrapper;
        final Map<Integer, ItemStack> items = new HashMap<>();
        final IGuiItemStackGroup stacks = mock(IGuiItemStackGroup.class);
        Display(SpiceJeiPlugin.MixingWrapper wrapper, IFocus<?> focus) {
            this.wrapper = wrapper;
            IRecipeLayout layout = mock(IRecipeLayout.class);
            when(layout.getItemStacks()).thenReturn(stacks);
            doReturn(focus).when(layout).getFocus();
            doAnswer(call -> { items.put(call.getArgument(0), call.getArgument(1)); return null; })
                .when(stacks).set(anyInt(), any(ItemStack.class));
            wrapper.setRecipe(layout, mock(IIngredients.class));
            verify(layout).setShapeless();
        }
        void assertCraftsOutput() {
            InventoryCrafting inventory = mock(InventoryCrafting.class);
            when(inventory.getSizeInventory()).thenReturn(4);
            int[] slots = {1, 2, 4, 5};
            for (int i = 0; i < slots.length; i++) when(inventory.getStackInSlot(i)).thenReturn(items.get(slots[i]));
            MixedSpiceRecipe recipe = new MixedSpiceRecipe();
            assertTrue(recipe.matches(inventory, null));
            assertTrue(ItemStack.areItemStacksEqual(recipe.getCraftingResult(inventory), items.get(0)));
            if (!SpiceConfig.consumeMixedCatalyst) assertTrue(ItemStack.areItemStacksEqual(items.get(5), recipe.getRemainingItems(inventory).get(3)));
            else assertTrue(recipe.getRemainingItems(inventory).get(3).isEmpty());
        }
    }
    @Test public void dualRecipeDisplaysIndependentIngredientAndReturnCounts() {
        SpiceRecipe recipe=new SpiceRecipe(new ItemStack(Items.WHEAT),3,new ItemStack(Items.CARROT),2,
            ItemSpice.create("salt",0xeeeeee,1),4,new ItemStack(Items.BOWL),new ItemStack(Items.GLASS_BOTTLE));
        SpiceJeiPlugin.Wrapper wrapper=SpiceJeiPlugin.Wrapper.grinding(recipe);
        assertEquals(2,wrapper.inputs.size()); assertEquals(3,wrapper.inputs.get(0).getCount()); assertEquals(2,wrapper.inputs.get(1).getCount());
        assertEquals(3,wrapper.outputs.size()); assertEquals(4,wrapper.outputs.get(0).getCount());
        assertEquals(3,wrapper.outputs.get(1).getCount()); assertEquals(2,wrapper.outputs.get(2).getCount());
    }
}
