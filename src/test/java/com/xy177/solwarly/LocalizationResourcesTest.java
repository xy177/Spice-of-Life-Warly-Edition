package com.xy177.solwarly;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xy177.solwarly.lib.Localization;
import org.junit.Test;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

public class LocalizationResourcesTest {
    @Test public void inheritedTranslationsResolveInBothBundledLanguages() throws Exception {
        JsonObject english = language("en_us");
        JsonObject chinese = language("zh_cn");
        int checked = 0;
        for (String key : english.keySet()) {
            String[] parts = key.split("\\.", 3);
            if (parts.length != 3 || !parts[1].equals("solcarrot")) continue;
            String resolved = Localization.keyString(parts[0], parts[2]);
            assertTrue("Missing English translation: " + resolved, english.has(resolved));
            assertTrue("Missing Chinese translation: " + resolved, chinese.has(resolved));
            assertFalse(english.get(resolved).getAsString().isBlank());
            assertFalse(chinese.get(resolved).getAsString().isBlank());
            checked++;
        }
        assertTrue("No inherited translations checked", checked > 30);
    }

    private JsonObject language(String code) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream("/assets/solcarrot/lang/" + code + ".json")) {
            assertNotNull("Missing language resource: " + code, stream);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
