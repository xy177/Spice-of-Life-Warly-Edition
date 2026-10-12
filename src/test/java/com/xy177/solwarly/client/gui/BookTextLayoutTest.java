package com.xy177.solwarly.client.gui;

import com.xy177.solwarly.client.gui.elements.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import org.junit.*;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BookTextLayoutTest {
    private static Field minecraftField, localeField, scaleField;
    private static Object previousMinecraft, previousLocale;
    private static FontRenderer font;
    @BeforeClass public static void setupClient() throws Exception {
        minecraftField = Minecraft.class.getDeclaredField("instance"); minecraftField.setAccessible(true);
        previousMinecraft = minecraftField.get(null);
        Minecraft client = mock(Minecraft.class); font = mock(FontRenderer.class);
        // Wide glyphs exercise fitting under different fonts without an OpenGL context.
        when(font.getStringWidth(anyString())).thenAnswer(call -> ((String)call.getArgument(0)).length() * 6);
        client.fontRenderer = font; minecraftField.set(null, client);
        localeField = I18n.class.getDeclaredField("i18nLocale"); localeField.setAccessible(true);
        previousLocale = localeField.get(null);
        scaleField = UILabel.class.getDeclaredField("textScale"); scaleField.setAccessible(true);
    }
    @AfterClass public static void restoreClient() throws Exception {
        minecraftField.set(null, previousMinecraft); localeField.set(null, previousLocale);
    }
    private void language(String language) throws Exception {
        IResourceManager manager = mock(IResourceManager.class);
        when(manager.getResourceDomains()).thenReturn(new LinkedHashSet<>(Arrays.asList("solcarrot", "solwarly")));
        when(manager.getAllResources(any(ResourceLocation.class))).thenAnswer(call -> {
            ResourceLocation id = call.getArgument(0);
            java.io.InputStream input = getClass().getResourceAsStream("/assets/" + id.getNamespace() + "/" + id.getPath());
            if (input == null) return Collections.emptyList();
            IResource resource = mock(IResource.class); when(resource.getInputStream()).thenReturn(input);
            return Collections.singletonList(resource);
        });
        net.minecraft.client.resources.Locale locale = new net.minecraft.client.resources.Locale();
        locale.loadLocaleDataFiles(manager, Collections.singletonList(language)); localeField.set(null, locale);
    }
    private void assertFits(UILabel label, Rectangle page) throws Exception {
        assertTrue(label.getMinX() >= page.x + 21);
        assertTrue(label.getMaxX() <= page.x + page.width - 21);
        assertTrue(font.getStringWidth(label.text) * scaleField.getFloat(label) <= page.width - 42 + .001);
    }
    @Test public void englishStatisticsKeepCompleteScoresInsidePageIncludingLargeValues() throws Exception {
        language("en_us"); Rectangle frame = new Rectangle(100, 40, 186, 192);
        List<String> lines = Arrays.asList(I18n.format("solwarly.book.cumulative", "3.82"),
            I18n.format("solwarly.book.diversity", "2.793"), I18n.format("solwarly.book.meals", 14, 32),
            I18n.format("solwarly.book.average", "1.862"), I18n.format("solwarly.book.variety", "1.5"),
            I18n.format("solwarly.book.floor", "99999.999"));
        DietPage page = new DietPage(frame, I18n.format("solwarly.book.stats"), lines);
        List<UIElement> children = page.mainStack.getChildren();
        assertFits((UILabel)children.get(0), frame);
        for (int i = 0; i < lines.size(); i++) {
            UILabel label = (UILabel)children.get(i + 2);
            assertEquals(lines.get(i), label.text); assertFits(label, frame);
            assertTrue(label.getMaxY() < frame.y + 156);
        }
        assertTrue(scaleField.getFloat(children.get(2)) < 1);
    }
    @Test public void longBookHeadersFitAndShortChineseStatisticsKeepOriginalSize() throws Exception {
        Rectangle frame = new Rectangle(100, 40, 186, 192); language("en_us");
        for (String key : Arrays.asList("solwarly.book.cumulative_rewards", "solwarly.book.diversity_rewards", "solwarly.book.all_flavors")) {
            String header = I18n.format(key, 12345);
            Page page = new DietPage(frame, header, Collections.emptyList());
            UILabel label = (UILabel)page.mainStack.getChildren().get(0);
            assertEquals(header, label.text); assertFits(label, frame);
        }
        language("zh_cn");
        String line = I18n.format("solwarly.book.cumulative", "3.82");
        DietPage page = new DietPage(frame, I18n.format("solwarly.book.stats"), Collections.singletonList(line));
        UILabel label = (UILabel)page.mainStack.getChildren().get(2);
        assertEquals(line, label.text); assertEquals(1, scaleField.getFloat(label), 0);
        assertFits(label, frame);
    }
}
