package com.xy177.solwarly;

import com.xy177.solwarly.client.*;
import com.xy177.solwarly.lib.DisplayNumbers;
import com.xy177.solwarly.spice.SpiceKind;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import org.junit.Test;
import java.time.LocalDate;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TooltipPresentationTest {
    @Test public void rgbMarkerSurvivesPrependedTooltipFormattingWithoutLeakingText() {
        String line = "§7" + HexTooltipFont.mark(0xEBC978, "Mining and attack speed");
        assertTrue(HexTooltipFont.marked(line));
        assertEquals(0xEBC978, HexTooltipFont.color(line, 0));
        assertEquals("Mining and attack speed", HexTooltipFont.text(line));
        assertEquals("Mining and attack speed", net.minecraft.util.text.TextFormatting.getTextWithoutFormattingCodes(line));
    }
    @Test public void displayRoundsFloatingPointArtifactsWithoutChangingCalculations() {
        assertEquals("0.975", DisplayNumbers.format(0.9750000000000001));
        assertEquals("1.235", DisplayNumbers.format(1.23456));
        assertEquals("2", DisplayNumbers.format(2));
        assertEquals("0", DisplayNumbers.format(-0.00001));
    }
    @Test public void seasoningSpacesAndSuffixPrecedeAdvancedItemIds() throws Exception {
        java.lang.reflect.Field field = I18n.class.getDeclaredField("i18nLocale"); field.setAccessible(true);
        Object previous = field.get(null);
        try {
            IResourceManager manager = mock(IResourceManager.class);
            when(manager.getResourceDomains()).thenReturn(Collections.singleton("solwarly"));
            when(manager.getAllResources(any(ResourceLocation.class))).thenAnswer(call -> {
                ResourceLocation location = call.getArgument(0);
                java.io.InputStream input = getClass().getResourceAsStream("/assets/" + location.getNamespace() + "/" + location.getPath());
                if (input == null) return Collections.emptyList();
                IResource resource = mock(IResource.class); when(resource.getInputStream()).thenReturn(input);
                return Collections.singletonList(resource);
            });
            net.minecraft.client.resources.Locale locale = new net.minecraft.client.resources.Locale();
            locale.loadLocaleDataFiles(manager, Arrays.asList("en_us", "zh_cn")); field.set(null, locale);
            LocalDate date = LocalDate.of(2026, 10, 11);
            assertEquals("米饭 佐米饭 (#4831)", SpiceTooltips.foodName("米饭 (#4831)", Collections.singletonList(SpiceKind.RICE), date));
            assertEquals("甜 苹果", SpiceTooltips.foodName("苹果", Collections.singletonList(SpiceKind.SUGAR), date));
            assertEquals("超·甜 苹果", SpiceTooltips.foodName("苹果", Arrays.asList(SpiceKind.SUGAR, SpiceKind.SUGAR), date));
        } finally { field.set(null, previous); }
    }
}
