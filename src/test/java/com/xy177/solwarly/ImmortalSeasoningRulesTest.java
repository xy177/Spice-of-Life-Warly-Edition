package com.xy177.solwarly;

import com.xy177.solwarly.spice.*;
import com.google.gson.*;
import org.junit.Test;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

public class ImmortalSeasoningRulesTest {
    @Test public void accompanimentAndPowderLimitsAreSeparate() {
        assertTrue(Seasoning.valid(List.of(SpiceKind.RICE, SpiceKind.TRAVARICE)));
        assertTrue(Seasoning.valid(List.of(SpiceKind.MILK_TEA, SpiceKind.MILK_TEA)));
        assertFalse(Seasoning.valid(List.of(SpiceKind.RICE, SpiceKind.SUGAR)));
        assertFalse(Seasoning.valid(Collections.nCopies(3, SpiceKind.TRAVARICE)));
        assertFalse(Seasoning.valid(List.of(SpiceKind.MUSTARD, SpiceKind.ASH)));
        assertTrue(Seasoning.valid(List.of(SpiceKind.MUSTARD, SpiceKind.ASH, SpiceKind.SUGAR)));
    }
    @Test public void creativeSeasoningHasOneIdentityAndCannotEnterBlends() {
        assertTrue(Seasoning.valid(List.of(SpiceKind.CREATIVE)));
        assertFalse(Seasoning.valid(List.of(SpiceKind.CREATIVE, SpiceKind.SALT, SpiceKind.SUGAR)));
        assertFalse(Seasoning.valid(Collections.nCopies(3, SpiceKind.CREATIVE)));
        assertFalse(Seasoning.valid(List.of(SpiceKind.CREATIVE, SpiceKind.RICE)));
        assertFalse(SpiceKind.CREATIVE.immortal());
        assertEquals("creative", Seasoning.identity(List.of(SpiceKind.CREATIVE)));
    }
    @Test public void pairedAccompanimentsHaveOrderIndependentNonMixedIdentity() {
        assertEquals(Seasoning.identity(List.of(SpiceKind.RICE, SpiceKind.MILK_TEA)), Seasoning.identity(List.of(SpiceKind.MILK_TEA, SpiceKind.RICE)));
        assertFalse(Seasoning.identity(List.of(SpiceKind.RICE, SpiceKind.MILK_TEA)).startsWith("mixed:"));
        assertNotEquals(Seasoning.identity(List.of(SpiceKind.RICE)), Seasoning.identity(List.of(SpiceKind.RICE, SpiceKind.RICE)));
        assertEquals(Seasoning.MIXED_FLAVOR, Seasoning.canonicalFlavor("solwarly:mixed:mustard+ash+sugar"));
    }
    @Test public void shortEffectsKeepTheirReviewedBaseDurations() {
        assertEquals(20, ImmortalSpices.defaultDuration(SpiceKind.MIRACLE));
        assertEquals(20, ImmortalSpices.defaultDuration(SpiceKind.HIMEKAIDO));
        assertEquals(600, ImmortalSpices.defaultDuration(SpiceKind.TRAVARICE));
        assertEquals(1200, ImmortalSpices.defaultDuration(SpiceKind.PURGATORY_ALE));
        assertEquals(3600, ImmortalSpices.defaultDuration(SpiceKind.SPIDER_EYE));
    }
    @Test public void everyNewSeasoningHasBothLanguagesAndSpacedFoodNames() throws Exception {
        for (String language : List.of("en_us", "zh_cn")) {
            try (var stream = getClass().getResourceAsStream("/assets/solwarly/lang/" + language + ".json")) {
                var strings = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                int checked = 0;
                for (SpiceKind kind : SpiceKind.values()) if (kind.immortal()) {
                    for (String key : List.of("item.solwarly.spice." + kind.id() + ".name", "solwarly.spice." + kind.id() + ".tooltip", "solwarly.seasoning." + kind.id())) {
                        assertTrue(key, strings.has(key)); assertFalse(key, strings.get(key).getAsString().isBlank());
                    }
                    String format = strings.get("solwarly.seasoning." + kind.id()).getAsString();
                    int at = format.indexOf("%s"); assertTrue(format, at >= 0);
                    assertTrue(format, at == 0 || format.charAt(at - 1) == ' ');
                    assertTrue(format, at + 2 == format.length() || format.charAt(at + 2) == ' '); checked++;
                }
                assertEquals(24, checked);
                assertTrue(strings.has("solwarly.spice.accompaniment"));
                for(int i=1;i<=5;i++)assertTrue(strings.has("solwarly.book.grinder."+i));
            }
        }
    }
}
