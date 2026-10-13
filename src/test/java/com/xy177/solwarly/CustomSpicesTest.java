package com.xy177.solwarly;

import com.google.gson.*;
import com.xy177.solwarly.spice.*;
import com.xy177.solwarly.api.FlavorAPI;
import com.xy177.solwarly.client.HexTooltipFont;
import com.xy177.solwarly.client.SpiceTooltips;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.inventory.*;
import net.minecraft.nbt.*;
import net.minecraft.potion.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import squeek.applecore.api.*;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CustomSpicesTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder(new File("E:/BOX"));
    private Field minecraftHome, recipeField, localField;
    private Object previousHome;
    private List<SpiceRecipe> previousRecipes;
    private IAppleCoreAccessor previousAccessor;
    private File directory, customDirectory;
    @BeforeClass public static void bootstrap() {
        Bootstrap.register();
        if (!ForgeRegistries.ITEMS.containsKey(SpiceRegistry.spice.getRegistryName())) ForgeRegistries.ITEMS.register(SpiceRegistry.spice);
    }
    @Before @SuppressWarnings("unchecked") public void setup() throws Exception {
        minecraftHome = FMLInjectionData.class.getDeclaredField("minecraftHome"); minecraftHome.setAccessible(true);
        previousHome = minecraftHome.get(null); minecraftHome.set(null, temp.getRoot());
        recipeField = SpiceRecipes.class.getDeclaredField("RECIPES"); recipeField.setAccessible(true);
        previousRecipes = new ArrayList<>((List<SpiceRecipe>)recipeField.get(null)); ((List<?>)recipeField.get(null)).clear();
        localField = CustomSpices.class.getDeclaredField("local"); localField.setAccessible(true); ((Map<?, ?>)localField.get(null)).clear();
        CustomSpices.clearServerDefinitions();
        previousAccessor = AppleCoreAPI.accessor; AppleCoreAPI.accessor = mock(IAppleCoreAccessor.class);
        when(AppleCoreAPI.accessor.isFood(any(ItemStack.class))).thenAnswer(call -> ((ItemStack)call.getArgument(0)).getItem() instanceof ItemFood);
        directory = temp.newFolder(); customDirectory = new File(directory, "custom_spices");
        SpiceConfig.load(directory); CustomSpices.load(directory);
    }
    @After @SuppressWarnings("unchecked") public void restore() throws Exception {
        CustomSpices.load(temp.newFolder()); CustomSpices.clearServerDefinitions();
        List<SpiceRecipe> recipes = (List<SpiceRecipe>)recipeField.get(null); recipes.clear(); recipes.addAll(previousRecipes);
        ((Map<?, ?>)localField.get(null)).clear(); minecraftHome.set(null, previousHome); AppleCoreAPI.accessor = previousAccessor;
    }
    private JsonObject example() throws Exception {
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/assets/solwarly/custom_spices/_example.json"), StandardCharsets.UTF_8)) {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject(); json.addProperty("enabled", true); return json;
        }
    }
    private void write(String filename, JsonObject json) throws Exception {
        Files.write(new File(customDirectory, filename).toPath(), json.toString().getBytes(StandardCharsets.UTF_8));
    }
    private InventoryCrafting grid(ItemStack... stacks) {
        InventoryCrafting grid = new InventoryCrafting(mock(Container.class), 3, 3);
        for (int i = 0; i < stacks.length; i++) grid.setInventorySlotContents(i, stacks[i]); return grid;
    }
    @Test public void exampleIsGeneratedButNotEnabledAndUserChangesArePreserved() throws Exception {
        File file = new File(customDirectory, "_example.json"); assertTrue(file.isFile()); assertTrue(CustomSpices.all().isEmpty());
        byte[] changed = "user edited example".getBytes(StandardCharsets.UTF_8); Files.write(file.toPath(), changed);
        CustomSpices.load(directory); assertArrayEquals(changed, Files.readAllBytes(file.toPath()));
    }
    @Test public void loadedPowderGrindsSeasonsMixesAndUsesExactPerCopyEffects() throws Exception {
        write("herb.json", example()); CustomSpices.load(directory);
        CustomSpice herb = (CustomSpice)SpiceKind.from("custom:herb_powder"); assertNotNull(herb);
        SpiceRecipe recipe = SpiceRecipes.find(new ItemStack(Blocks.TALLGRASS, 3, 1));
        assertNotNull(recipe); assertEquals(3, recipe.inputCount); assertEquals(1, recipe.outputCount);
        ItemStack powder = recipe.result(); assertEquals(0x74A34C, Seasoning.color(powder));
        assertEquals("草本调味粉", herb.text("zh_cn", "name")); assertEquals("Herb Spice Powder", herb.text("fr_fr", "name"));
        ItemStack food = new SeasonFoodRecipe().getCraftingResult(grid(new ItemStack(Items.APPLE), powder));
        assertFalse(food.isEmpty()); assertTrue(Seasoning.seasoned(food));
        assertEquals(1.2, Seasoning.flavor(food).diversityMultiplier, 0);
        assertEquals("solwarly:custom:herb_powder", Seasoning.flavor(food).id);
        MixedSpiceRecipe mixing = new MixedSpiceRecipe();
        ItemStack salt = Seasoning.powder(Collections.singletonList(SpiceKind.SALT), 1);
        ItemStack blend = mixing.getCraftingResult(grid(powder, salt, powder, new ItemStack(Items.NETHER_STAR)));
        assertEquals(3, Seasoning.components(blend).size()); assertEquals(2, blend.getCount());
        assertEquals(Seasoning.MIXED_FLAVOR, Seasoning.flavor(Seasoning.apply(new ItemStack(Items.APPLE), Seasoning.components(blend))).id);
        assertTrue(mixing.getCraftingResult(grid(blend, salt, powder, new ItemStack(Items.NETHER_STAR))).isEmpty());
        for (int copies = 1; copies <= 3; copies++) {
            List<SeasoningPotions.Entry> effects = SeasoningPotions.additions(Collections.nCopies(copies, herb), Collections.emptyMap());
            assertEquals(copies == 3 ? 2 : 1, effects.size());
            assertEquals(copies - 1, effects.get(0).effect.getAmplifier());
            assertEquals((copies == 1 ? 120 : copies == 2 ? 150 : 180) * 20, effects.get(0).effect.getDuration());
        }
        assertEquals(1.2, SpiceConfig.mixedMultiplier, 0);
        assertTrue(Seasoning.variants(new ItemStack(Items.APPLE)).stream().anyMatch(stack -> herb == Seasoning.components(stack).get(0)));
        Map<Potion, PotionEffect> original = Collections.singletonMap(MobEffects.SPEED, new PotionEffect(MobEffects.SPEED, 600, 2));
        EntityPlayer player = mock(EntityPlayer.class);
        PotionEffect speed = SpiceEffects.apply(player, Collections.singletonList(herb), original).get(MobEffects.SPEED);
        assertEquals(3000, speed.getDuration()); assertEquals(2, speed.getAmplifier());
        verify(player).addPotionEffect(argThat(effect -> effect.getPotion() == MobEffects.SPEED && effect.getDuration() == 3000));
    }
    @Test public void badFilesAndRecipeConflictsDoNotPreventOtherFilesLoading() throws Exception {
        SpiceRecipes.register(new SpiceRecipe(new ItemStack(Items.SUGAR), 3, Seasoning.powder(Collections.singletonList(SpiceKind.SUGAR), 1), 1));
        JsonObject conflict = example(); conflict.addProperty("id", "custom:conflict");
        conflict.getAsJsonArray("recipes").get(0).getAsJsonObject().getAsJsonObject("input").addProperty("item", "minecraft:sugar");
        conflict.getAsJsonArray("recipes").get(0).getAsJsonObject().getAsJsonObject("input").addProperty("metadata", 0);
        write("a_conflict.json", conflict);
        JsonObject missing = example(); missing.addProperty("id", "custom:missing");
        missing.getAsJsonObject("effects").getAsJsonArray("1").get(0).getAsJsonObject().addProperty("potion", "absent:effect");
        write("b_missing.json", missing); write("c_valid.json", example()); write("d_duplicate.json", example());
        CustomSpices.load(directory);
        assertEquals(1, CustomSpices.all().size()); assertNull(SpiceKind.from("custom:conflict")); assertNull(SpiceKind.from("custom:missing"));
        assertEquals(2, SpiceRecipes.all().size());
        assertEquals(SpiceKind.SUGAR, Seasoning.components(SpiceRecipes.find(new ItemStack(Items.SUGAR)).result()).get(0));
    }
    @Test public void metadataNbtAndContainerReturnsAreExact() throws Exception {
        JsonObject json = example(); JsonObject input = json.getAsJsonArray("recipes").get(0).getAsJsonObject().getAsJsonObject("input");
        input.addProperty("item", "minecraft:potion"); input.addProperty("metadata", 0); input.addProperty("count", 1); input.addProperty("nbt", "{Potion:\"minecraft:healing\"}");
        write("healing.json", json); CustomSpices.load(directory);
        ItemStack bottle = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.HEALING);
        SpiceRecipe recipe = SpiceRecipes.find(bottle); assertNotNull(recipe);
        assertNull(SpiceRecipes.find(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER)));
        assertEquals(Items.GLASS_BOTTLE, recipe.returnedContainers().getItem());
        JsonObject returned = new JsonObject(); returned.addProperty("item", "minecraft:bucket"); returned.addProperty("count", 2);
        json.getAsJsonArray("recipes").get(0).getAsJsonObject().add("return", returned);
        write("healing.json", json); CustomSpices.load(directory);
        assertEquals(2, SpiceRecipes.find(bottle).returnedContainers().getCount());
    }
    @Test public void serverSnapshotCarriesTheCompleteDefinitionsAndRecipes() throws Exception {
        write("herb.json", example()); CustomSpices.load(directory);
        NBTTagList snapshot = CustomSpices.snapshot(); assertEquals(1, snapshot.tagCount());
        CustomSpices.load(temp.newFolder()); assertTrue(CustomSpices.all().isEmpty());
        CustomSpices.readSnapshot(snapshot);
        Field server = CustomSpices.class.getDeclaredField("serverDefinitions"); server.setAccessible(true);
        Map<?, ?> definitions = (Map<?, ?>)server.get(null);
        CustomSpice herb = (CustomSpice)definitions.get("custom:herb_powder"); assertNotNull(herb);
        assertEquals(0x74A34C, herb.color()); assertEquals(1.2, herb.multiplier(), 0);
        assertEquals("草本调味粉", herb.text("zh_cn", "name")); assertEquals(2, herb.effects(3).size());
        assertEquals("清新草本，让脚步更轻快。\n为这一餐增添自然风味。", herb.text("zh_cn", "tooltip"));
        assertEquals(herb.text("en_us", "tooltip"), herb.text("fr_fr", "tooltip"));
        Field recipes = CustomSpices.class.getDeclaredField("serverRecipes"); recipes.setAccessible(true);
        assertEquals(1, ((List<?>)recipes.get(null)).size());
        CustomSpices.readSnapshot(snapshot); assertSame(definitions, server.get(null));
        CustomSpices.clearServerDefinitions(); assertNull(server.get(null));
    }
    @Test @SuppressWarnings("unchecked") public void customTooltipIsMultilineColoredAndSharedByPowdersBlendsAndFoods() throws Exception {
        write("herb.json", example()); CustomSpices.load(directory);
        CustomSpice herb = CustomSpices.find("custom:herb_powder");
        CustomSpice rendered = spy(herb);
        doAnswer(call -> herb.text("zh_cn", call.getArgument(0))).when(rendered).text(anyString());
        ((Map<String, CustomSpice>)localField.get(null)).put(herb.id(), rendered);
        Field localeField = net.minecraft.client.resources.I18n.class.getDeclaredField("i18nLocale"); localeField.setAccessible(true);
        Object previousLocale = localeField.get(null);
        Field modsField = net.minecraftforge.fml.common.Loader.class.getDeclaredField("namedMods"); modsField.setAccessible(true);
        Object previousMods = modsField.get(net.minecraftforge.fml.common.Loader.instance());
        try {
            modsField.set(net.minecraftforge.fml.common.Loader.instance(), Collections.emptyMap());
            net.minecraft.client.resources.Locale locale = mock(net.minecraft.client.resources.Locale.class);
            when(locale.formatMessage(anyString(), any(Object[].class))).thenAnswer(call -> {
                String key = call.getArgument(0); Object[] arguments = call.getArgument(1);
                return key.equals("solwarly.tooltip.food.effect") ? arguments[0] + " (" + arguments[1] + ")" : key;
            });
            localeField.set(null, locale);
            List<List<SpiceKind>> combinations = Arrays.asList(Collections.singletonList(herb),
                Arrays.asList(herb, herb, SpiceKind.SALT), Collections.nCopies(3, herb));
            for (List<SpiceKind> kinds : combinations) {
                for (ItemStack stack : Arrays.asList(Seasoning.powder(kinds, 1), Seasoning.apply(new ItemStack(Items.APPLE), kinds))) {
                    List<String> lines = tooltip(stack);
                    List<String> herbLines = new ArrayList<>();
                    for (String line : lines) if (HexTooltipFont.marked(line) && HexTooltipFont.color(line, 0) == herb.color())
                        herbLines.add(HexTooltipFont.text(line));
                    assertEquals(2, herbLines.size());
                    assertEquals("清新草本，让脚步更轻快。", herbLines.get(0));
                    assertTrue(herbLines.get(1).startsWith("为这一餐增添自然风味。"));
                    int copies = Collections.frequency(kinds, herb);
                    if (copies > 1) assertTrue(herbLines.get(1).endsWith("solwarly.spice." + (copies == 3 ? "too_much" : "lots")));
                    if (Seasoning.seasoned(stack)) assertTrue(lines.stream().anyMatch(line -> line.contains("(2:") || line.contains("(3:")));
                }
            }
        } finally {
            localeField.set(null, previousLocale);
            modsField.set(net.minecraftforge.fml.common.Loader.instance(), previousMods);
        }
    }
    @Test @SuppressWarnings("unchecked") public void absentOrBlankTooltipKeepsAutomaticDescriptionAndInvalidTooltipIsRejected() throws Exception {
        JsonObject json = example();
        JsonObject translations = json.getAsJsonObject("translations");
        for (Map.Entry<String, JsonElement> entry : translations.entrySet()) entry.getValue().getAsJsonObject().remove("tooltip");
        Field localeField = net.minecraft.client.resources.I18n.class.getDeclaredField("i18nLocale"); localeField.setAccessible(true);
        Object previousLocale = localeField.get(null);
        try {
            localeField.set(null, new net.minecraft.client.resources.Locale());
            for (boolean blank : new boolean[] {false, true}) {
                if (blank) translations.getAsJsonObject("zh_cn").addProperty("tooltip", " \n ");
                write("herb.json", json); CustomSpices.load(directory);
                CustomSpice herb = CustomSpices.find("custom:herb_powder"), rendered = spy(herb);
                doAnswer(call -> herb.text("zh_cn", call.getArgument(0))).when(rendered).text(anyString());
                ((Map<String, CustomSpice>)localField.get(null)).put(herb.id(), rendered);
                List<String> lines = tooltip(Seasoning.powder(Collections.singletonList(herb), 1));
                assertEquals("草本调味粉: effect.moveSpeed (2:00)", HexTooltipFont.text(lines.get(1)));
            }
            translations.getAsJsonObject("zh_cn").add("tooltip", new JsonArray());
            try { new CustomSpice(json); fail("Non-string tooltip must be rejected"); }
            catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("tooltip must be a string")); }
        } finally { localeField.set(null, previousLocale); }
    }
    @Test @SuppressWarnings("unchecked") public void foodPotionTooltipIncludesConfiguredLevelAndBlueDuration() throws Exception {
        JsonObject json = sideExample();
        json.getAsJsonArray("effects").get(0).getAsJsonObject().addProperty("level", 3);
        writeSide("carrot.json", json); CustomSpices.load(directory);
        CustomSpice side = CustomSpices.find("custom:carrot_side"), rendered = spy(side);
        doAnswer(call -> side.text("en_us", call.getArgument(0))).when(rendered).text(anyString());
        ((Map<String, CustomSpice>)localField.get(null)).put(side.id(), rendered);
        Field localeField = net.minecraft.client.resources.I18n.class.getDeclaredField("i18nLocale"); localeField.setAccessible(true);
        Object previousLocale = localeField.get(null);
        Field modsField = net.minecraftforge.fml.common.Loader.class.getDeclaredField("namedMods"); modsField.setAccessible(true);
        Object previousMods = modsField.get(net.minecraftforge.fml.common.Loader.instance());
        try {
            modsField.set(net.minecraftforge.fml.common.Loader.instance(), Collections.emptyMap());
            net.minecraft.client.resources.Locale locale = mock(net.minecraft.client.resources.Locale.class);
            when(locale.formatMessage(anyString(), any(Object[].class))).thenAnswer(call -> {
                String key = call.getArgument(0); Object[] arguments = call.getArgument(1);
                if (key.equals("potion.potency.2")) return "III";
                if (key.equals("solwarly.tooltip.food.effect")) return arguments[0] + " (" + arguments[1] + ")";
                return key;
            });
            localeField.set(null, locale);
            String effect = tooltip(Seasoning.apply(new ItemStack(Items.APPLE), Arrays.asList(rendered, rendered))).get(1);
            assertTrue(effect.contains(net.minecraft.util.text.TextFormatting.BLUE + " III"));
            assertTrue(effect.contains(net.minecraft.util.text.TextFormatting.BLUE + " (1:00"));
            assertTrue(effect.endsWith(net.minecraft.util.text.TextFormatting.BLUE + ")"));
        } finally {
            localeField.set(null, previousLocale);
            modsField.set(net.minecraftforge.fml.common.Loader.instance(), previousMods);
        }
    }
    private List<String> tooltip(ItemStack stack) {
        List<String> lines = new ArrayList<>(Collections.singletonList("Food"));
        SpiceTooltips.tooltip(new net.minecraftforge.event.entity.player.ItemTooltipEvent(stack, null, lines,
            net.minecraft.client.util.ITooltipFlag.TooltipFlags.NORMAL));
        return lines;
    }

    private JsonObject sideExample() throws Exception {
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/assets/solwarly/custom_accompaniments/_example.json"), StandardCharsets.UTF_8)) {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject(); json.addProperty("enabled", true); return json;
        }
    }
    private void writeSide(String filename, JsonObject json) throws Exception {
        Files.write(new File(new File(directory,"custom_accompaniments"),filename).toPath(), json.toString().getBytes(StandardCharsets.UTF_8));
    }
    @Test public void accompanimentExampleIsGeneratedDisabledAndPreservesEdits() throws Exception {
        File example = new File(directory,"custom_accompaniments/_example.json");
        assertTrue(example.isFile()); assertTrue(CustomSpices.all().isEmpty());
        Files.write(example.toPath(), "user changes".getBytes(StandardCharsets.UTF_8)); CustomSpices.load(directory);
        assertEquals("user changes",new String(Files.readAllBytes(example.toPath()),StandardCharsets.UTF_8));
    }
    @Test public void nativeAccompanimentSeasonsInAnySlotAndAddsTwoServingsWithoutMixing() throws Exception {
        writeSide("carrot.json",sideExample());CustomSpices.load(directory);
        CustomSpice side=CustomSpices.find("custom:carrot_side");assertNotNull(side);assertTrue(side.accompaniment());
        ItemStack carrot=new ItemStack(Items.CARROT);assertEquals(Collections.singletonList(side),Seasoning.ingredient(carrot));
        assertTrue(Seasoning.components(carrot).isEmpty()); assertEquals(Items.CARROT,Seasoning.material(side).getItem());
        SeasonFoodRecipe recipe=new SeasonFoodRecipe();ItemStack one=recipe.getCraftingResult(grid(carrot,new ItemStack(Items.APPLE)));
        assertEquals(Items.APPLE,one.getItem());assertEquals(Collections.singletonList(side),Seasoning.components(one));
        ItemStack two=recipe.getCraftingResult(grid(carrot,one));assertEquals(Arrays.asList(side,side),Seasoning.components(two));
        assertFalse(recipe.matches(grid(carrot,two),null));assertFalse(recipe.matches(grid(two,new ItemStack(Items.BREAD)),null));
        assertFalse(recipe.matches(grid(one,Seasoning.powder(Collections.singletonList(SpiceKind.SALT),1)),null));
        assertEquals(1.2,Seasoning.flavor(one).diversityMultiplier,0);assertEquals(SpiceConfig.accompanimentMultiplier,Seasoning.flavor(two).diversityMultiplier,0);
        squeek.applecore.api.food.FoodEvent.GetFoodValues values=new squeek.applecore.api.food.FoodEvent.GetFoodValues(two,new squeek.applecore.api.food.FoodValues(4,.3F));
        SpiceEffects.foodValues(values);assertEquals(8,values.foodValues.hunger);assertEquals(4.8,values.foodValues.getUnboundedSaturationIncrement(),.001);
        assertEquals(1200,side.effects(2).get(0).effect.getDuration());assertEquals(0,side.effects(2).get(0).effect.getAmplifier());
        Map<Potion,PotionEffect> merged=SeasoningPotions.merge(Collections.singletonMap(MobEffects.NIGHT_VISION,new PotionEffect(MobEffects.NIGHT_VISION,300,2)),side.effects(2));
        assertEquals(1500,merged.get(MobEffects.NIGHT_VISION).getDuration());assertEquals(2,merged.get(MobEffects.NIGHT_VISION).getAmplifier());
        assertTrue(SpiceRecipes.all().isEmpty());
        assertTrue(Seasoning.variants(new ItemStack(Items.APPLE)).stream().anyMatch(v->Seasoning.components(v).equals(Arrays.asList(side,side))));
        assertTrue(Seasoning.effectKinds(Collections.singletonList(SpiceKind.CREATIVE)).contains(side));
        net.minecraft.util.NonNullList<ItemStack> creative=net.minecraft.util.NonNullList.create();SpiceRegistry.spice.getSubItems(net.minecraft.creativetab.CreativeTabs.FOOD,creative);
        assertTrue(creative.stream().noneMatch(v->Seasoning.components(v).contains(side)));
    }
    @Test public void exactMaterialDataConflictIsolationAndCustomReturnsArePreserved() throws Exception {
        JsonObject first=sideExample();first.getAsJsonObject("material").addProperty("nbt","{Side:1}");
        JsonObject returned=new JsonObject();returned.addProperty("item","minecraft:bowl");returned.addProperty("count",2);first.add("return",returned);
        writeSide("a_valid.json",first);
        JsonObject duplicate=new JsonParser().parse(first.toString()).getAsJsonObject();duplicate.addProperty("id","custom:duplicate");writeSide("b_duplicate.json",duplicate);
        JsonObject other=new JsonParser().parse(first.toString()).getAsJsonObject();other.addProperty("id","custom:other");other.getAsJsonObject("material").addProperty("nbt","{Side:2}");other.add("return",JsonNull.INSTANCE);writeSide("c_other.json",other);
        JsonObject invalid=new JsonParser().parse(first.toString()).getAsJsonObject();invalid.addProperty("id","custom:invalid");invalid.addProperty("hunger",-1);writeSide("d_invalid.json",invalid);
        CustomSpices.load(directory);assertEquals(2,CustomSpices.all().size());assertNull(CustomSpices.find("custom:duplicate"));assertNull(CustomSpices.find("custom:invalid"));
        assertTrue(Seasoning.ingredient(new ItemStack(Items.CARROT)).isEmpty());
        CustomSpice side=CustomSpices.find("custom:carrot_side");ItemStack material=side.material();assertTrue(side.matches(material));
        InventoryCrafting input=grid(new ItemStack(Items.APPLE),material);assertEquals(2,new SeasonFoodRecipe().getRemainingItems(input).get(1).getCount());
        assertTrue(new SeasonFoodRecipe().getRemainingItems(grid(new ItemStack(Items.APPLE),CustomSpices.find("custom:other").material())).get(1).isEmpty());
        material.getTagCompound().setInteger("Side",9);assertFalse(side.matches(material));assertTrue(side.matches(side.material()));
    }
    @Test @SuppressWarnings("unchecked") public void snapshotIncludesAccompanimentDefinitionButNoGrindingRecipe() throws Exception {
        write("herb.json",example());writeSide("carrot.json",sideExample());CustomSpices.load(directory);
        NBTTagList snapshot=CustomSpices.snapshot();CustomSpices.load(temp.newFolder());CustomSpices.readSnapshot(snapshot);
        Field server=CustomSpices.class.getDeclaredField("serverDefinitions");server.setAccessible(true);
        CustomSpice side=((Map<String,CustomSpice>)server.get(null)).get("custom:carrot_side");assertTrue(side.accompaniment());
        assertEquals(2,side.hungerBonus);assertEquals(1.2,side.saturationBonus,0);assertEquals("Carrot Accompaniment",side.text("en_us","name"));
        assertEquals(Items.CARROT,side.material().getItem());assertEquals(1200,side.effects(2).get(0).effect.getDuration());
        Field recipes=CustomSpices.class.getDeclaredField("serverRecipes");recipes.setAccessible(true);assertEquals(1,((List<?>)recipes.get(null)).size());
    }
}
