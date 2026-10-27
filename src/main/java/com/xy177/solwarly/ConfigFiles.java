package com.xy177.solwarly;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/** Move new configuration generation into one directory without discarding existing settings. */
public final class ConfigFiles {
    public static File prepare(File root) {
        File directory = new File(root, SOLWarly.MOD_ID);
        try {
            Files.createDirectories(directory.toPath());
            for (String name : new String[] {"solwarly-general.cfg", "solwarly.cfg", "solwarly-foods.cfg", "solwarly-spices.cfg", "solwarly-grinder.cfg"}) {
                File source = new File(root, name), target = new File(directory, name);
                if (source.isFile() && !target.exists()) Files.copy(source.toPath(), target.toPath());
            }
        } catch (IOException e) { throw new IllegalStateException("Cannot prepare Warly configuration directory: " + directory, e); }
        return directory;
    }
    private ConfigFiles() {}
}
