package com.xy177.solwarly;

import com.xy177.solwarly.diet.DietConfig;
import com.xy177.solwarly.diet.FoodCatalog;
import com.xy177.solwarly.spice.GrinderConfig;
import com.xy177.solwarly.spice.SpiceConfig;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import static org.junit.Assert.*;

public class ConfigPersistenceTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder(new File("E:/BOX"));
    private Field minecraftHome;
    private Object previousHome;
    private String previousCatalyst;
    private int previousCatalystMetadata;
    private boolean previousConsume;
    @Before public void setup() throws Exception {
        previousCatalyst = SpiceConfig.mixedCatalystItem;
        previousCatalystMetadata = SpiceConfig.mixedCatalystMetadata;
        previousConsume = SpiceConfig.consumeMixedCatalyst;
        minecraftHome = FMLInjectionData.class.getDeclaredField("minecraftHome");
        minecraftHome.setAccessible(true);
        previousHome = minecraftHome.get(null);
        minecraftHome.set(null, temp.getRoot());
    }
    @After public void restore() throws Exception {
        minecraftHome.set(null, previousHome);
        SpiceConfig.mixedCatalystItem = previousCatalyst;
        SpiceConfig.mixedCatalystMetadata = previousCatalystMetadata;
        SpiceConfig.consumeMixedCatalyst = previousConsume;
    }

    @Test public void rewritingDescriptionsPreservesExistingCustomValues() throws Exception {
        File directory = temp.newFolder();
        Configuration diet = config(directory, "solwarly.cfg");
        diet.get("statistics", "recentMeals", 32).set(17);
        diet.get("statistics", "frequencyWeightMultiplier", -1.0).set(.7);
        diet.get("preparation", "difficultyWeight", .65).set(1.23);
        diet.get("warlyMode", "warlyMode", false).set(true);
        diet.get("warlyMode", "repeatDurationDays", 2).set(3);
        diet.get("warlyMode", "repeatMultipliers", new double[] {1, .9, .8, .65, .5, .3}).set(new double[] {1, .95, .9, .8, .7, .5});
        diet.save();
        Configuration spices = config(directory, "solwarly-spices.cfg");
        spices.get("mix", "output", 2).set(5);
        spices.get("mix", "catalystItem", "minecraft:nether_star").set("minecraft:dye");
        spices.get("mix", "catalystMetadata", 0).set(4);
        spices.get("mix", "consumeCatalyst", false).set(true);
        spices.get("recipes.salt_water", "outputCount", 1).set(6);
        spices.save();
        Configuration grinder = config(directory, "solwarly-grinder.cfg");
        grinder.get("automation", "bottomCollectors", new String[] {}).set(new String[] {"example:pipe"});
        grinder.save();

        // Run twice: comment-only changes must also be written when no keys are missing.
        for (int i = 0; i < 2; i++) {
            DietConfig.load(directory); SpiceConfig.load(directory); GrinderConfig.load(directory);
            assertEquals(6, SpiceConfig.recipeCount("salt_water", false, 1, 64));
            SpiceConfig.save();
        }
        assertEquals(17, DietConfig.recentCount);
        assertEquals(.7, DietConfig.frequencyWeight, .000001);
        assertEquals(1.23, DietConfig.difficultyWeight, .000001);
        assertTrue(DietConfig.warlyMode); assertEquals(3, DietConfig.warlyDurationDays);
        assertArrayEquals(new double[] {1, .95, .9, .8, .7, .5}, DietConfig.warlyRepeatMultipliers, .000001);
        assertEquals(5, SpiceConfig.mixedCount);
        assertEquals("minecraft:dye", SpiceConfig.mixedCatalystItem);
        assertEquals(4, SpiceConfig.mixedCatalystMetadata); assertTrue(SpiceConfig.consumeMixedCatalyst);
        assertArrayEquals(new String[] {"example:pipe"}, config(directory, "solwarly-grinder.cfg")
            .getCategory("automation").get("bottomCollectors").getStringList());
        assertTrue(text(directory, "solwarly.cfg").contains("# 用于计算个人多样性的最近进食次数。\n# Number of recent meals"));
        assertTrue(text(directory, "solwarly-spices.cfg").contains("# 每批产出的调味粉数量。\n# Spice items produced per batch."));
        assertTrue(text(directory, "solwarly-spices.cfg").contains("\n# Whether to consume one extra item per craft;"));
        assertTrue(text(directory, "solwarly-grinder.cfg").contains("\n# Registry IDs of bottom extractors"));
    }
    @Test public void existingFoodValueIsPreservedWithoutRepeatedDescriptionsOrRecalculation() throws Exception {
        File directory = temp.newFolder();
        Configuration food = config(directory, "solwarly-foods.cfg");
        food.get("foods", "example:food@0", 7.891).set(7.891);
        food.save();
        food = config(directory, "solwarly-foods.cfg");
        Field field = FoodCatalog.class.getDeclaredField("foodConfig"); field.setAccessible(true);
        Object previous = field.get(null);
        try {
            field.set(null, food);
            Method configured = FoodCatalog.class.getDeclaredMethod("configured", String.class, ItemStack.class, boolean.class);
            configured.setAccessible(true);
            // A null stack cannot be analyzed: a complete manual value must return directly.
            assertEquals(7.891, (Double) configured.invoke(null, "example:food@0", null, false), 0);
            food.save();
            assertEquals(7.891, config(directory, "solwarly-foods.cfg").getCategory("foods").get("example:food@0").getDouble(), 0);
            assertFalse(text(directory, "solwarly-foods.cfg").contains("# 最终基础系数"));
        } finally { field.set(null, previous); }
    }
    @Test public void configurationDirectoryCopiesAllLegacyFilesWithoutOverwritingEitherLocation() throws Exception {
        File root = temp.newFolder();
        for (String name : new String[] {"solcarrot.cfg", "solwarly.cfg", "solwarly-foods.cfg", "solwarly-spices.cfg", "solwarly-grinder.cfg"}) {
            Files.write(new File(root, name).toPath(), ("custom settings in " + name).getBytes(StandardCharsets.UTF_8));
        }
        File directory = ConfigFiles.prepare(root);
        assertEquals(new File(root, "solwarly"), directory);
        assertEquals(5, directory.listFiles().length);
        for (File migrated : directory.listFiles()) assertArrayEquals(Files.readAllBytes(new File(root, migrated.getName()).toPath()), Files.readAllBytes(migrated.toPath()));
        Files.write(new File(directory, "solcarrot.cfg").toPath(), "new settings".getBytes(StandardCharsets.UTF_8));
        ConfigFiles.prepare(root);
        assertEquals("new settings", text(directory, "solcarrot.cfg"));
        assertEquals("custom settings in solcarrot.cfg", text(root, "solcarrot.cfg"));
        assertEquals("solwarly/solcarrot", SOLWarlyConfig.class.getAnnotation(net.minecraftforge.common.config.Config.class).name());
    }
    private Configuration config(File directory, String name) {
        Configuration config = new Configuration(new File(directory, name)); config.load(); return config;
    }
    private String text(File directory, String name) throws Exception {
        return new String(Files.readAllBytes(new File(directory, name).toPath()), StandardCharsets.UTF_8)
            .replace("\r\n", "\n").replaceAll("(?m)^ +#", "#");
    }
}
