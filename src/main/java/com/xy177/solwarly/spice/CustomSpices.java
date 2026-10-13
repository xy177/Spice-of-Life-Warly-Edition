package com.xy177.solwarly.spice;

import com.google.gson.*;
import com.xy177.solwarly.SOLWarly;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.*;
import net.minecraft.nbt.*;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public final class CustomSpices {
    private static final Map<String, CustomSpice> local = new TreeMap<>();
    private static Map<String, CustomSpice> serverDefinitions;
    private static List<SpiceRecipe> serverRecipes;
    private static String serverSnapshot;
    private static final List<SpiceRecipe> registered = new ArrayList<>();
    private static Map<String, CustomSpice> definitions() {
        return serverDefinitions != null && EffectiveSide.get().isClient() ? serverDefinitions : local;
    }
    public static Collection<CustomSpice> all() { return Collections.unmodifiableCollection(definitions().values()); }
    public static CustomSpice find(String id) { return definitions().get(id); }
    public static void clearServerDefinitions() {
        serverDefinitions = null; serverRecipes = null; serverSnapshot = null;
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new DefinitionsChanged());
    }
    public static boolean owns(SpiceRecipe recipe) { return registered.contains(recipe); }
    public static Collection<SpiceRecipe> displayRecipes() {
        return serverRecipes != null && EffectiveSide.get().isClient() ? serverRecipes : registered;
    }
    public static final class DefinitionsChanged extends net.minecraftforge.eventbus.api.Event {}

    public static void load(File modDirectory) {
        registered.forEach(SpiceRecipes::remove); registered.clear(); local.clear();
        loadDirectory(modDirectory, false);
        loadDirectory(modDirectory, true);
    }
    private static void loadDirectory(File modDirectory, boolean accompaniment) {
        String folder = accompaniment ? "custom_accompaniments" : "custom_spices";
        File directory = new File(modDirectory, folder);
        try {
            Files.createDirectories(directory.toPath());
            File example = new File(directory, "_example.json");
            if (!example.exists()) try (InputStream input = CustomSpices.class.getResourceAsStream("/assets/solwarly/" + folder + "/_example.json")) {
                if (input == null) throw new IOException("Bundled example is missing");
                Files.copy(input, example.toPath());
            }
        } catch (IOException e) { throw new IllegalStateException("Cannot prepare custom spice directory", e); }
        File[] files = directory.listFiles(file -> file.isFile() && file.getName().endsWith(".json") && !file.getName().startsWith("_"));
        if (files == null) throw new IllegalStateException("Cannot list custom spice directory: " + directory);
        Arrays.sort(files, Comparator.comparing(File::getName));
        for (File file : files) try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            if (json.has("enabled") && !json.get("enabled").getAsBoolean()) continue;
            CustomSpice spice = new CustomSpice(json, accompaniment);
            if (Arrays.stream(SpiceKind.values()).anyMatch(kind -> kind.id().equals(spice.id())) || local.containsKey(spice.id()))
                throw new IllegalArgumentException("Duplicate spice ID: " + spice.id());
            List<SpiceRecipe> pending = new ArrayList<>();
            if (accompaniment) {
                ItemStack material = spice.material();
                for (SpiceKind builtin : SpiceKind.values()) if (builtin.nativeMaterial() && builtin.available() && Seasoning.material(builtin).getItem() == material.getItem())
                    throw new IllegalArgumentException("Material conflicts with built-in flavor: " + builtin.id());
                if (local.values().stream().anyMatch(other -> other.matches(material)))
                    throw new IllegalArgumentException("Duplicate accompaniment material");
            } else {
                JsonArray recipes = json.getAsJsonArray("recipes");
                if (recipes == null || recipes.size() == 0) throw new IllegalArgumentException("At least one grinding recipe is required");
                for (JsonElement element : recipes) {
                    JsonObject spec = element.getAsJsonObject();
                    ItemStack input = stack(spec.getAsJsonObject("input"));
                    int x = CustomSpice.integer(spec.getAsJsonObject("input"), "count", 1, input.getMaxStackSize());
                    int y = CustomSpice.integer(spec, "output_count", 1, 64);
                    if (SpiceRecipes.find(input) != null || pending.stream().anyMatch(recipe -> recipe.matches(input)))
                        throw new IllegalArgumentException("Conflicting grinding input: " + com.xy177.solwarly.registry.RegistryLookup.id(input.getItem()) + "@" + input.getDamageValue());
                    ItemStack container = spec.has("return") ? (spec.get("return").isJsonNull() ? ItemStack.EMPTY : stack(spec.getAsJsonObject("return"))) : container(input);
                    pending.add(new SpiceRecipe(input, x, Seasoning.powder(Collections.singletonList(spice), 1), y, container));
                }
            }
            local.put(spice.id(), spice);
            pending.forEach(SpiceRecipes::register); registered.addAll(pending);
            SOLWarly.LOGGER.info("Loaded custom spice {} from {}", spice.id(), file.getName());
        } catch (IOException | RuntimeException e) {
            SOLWarly.LOGGER.error("Skipping custom spice file {}: {}", file.getPath(), e.getMessage());
        }
    }
    static ItemStack stack(JsonObject spec) {
        String id = CustomSpice.required(spec, "item"); Item item = com.xy177.solwarly.registry.RegistryLookup.item(id);
        if (item == null || item == Items.AIR) throw new IllegalArgumentException("Unknown item: " + id);
        int meta = spec.has("metadata") ? CustomSpice.integer(spec, "metadata", 0, 32766) : 0;
        int count = spec.has("count") ? CustomSpice.integer(spec, "count", 1, item.getMaxStackSize()) : 1;
        ItemStack stack = new ItemStack(item, count);
        if (meta != 0) stack.setDamageValue(meta);
        if (spec.has("nbt")) try { stack.setTag(TagParser.parseTag(spec.get("nbt").getAsString())); }
        catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) { throw new IllegalArgumentException("Invalid input/return NBT", e); }
        return stack;
    }
    static ItemStack container(ItemStack input) {
        if (input.getItem().hasCraftingRemainingItem(input)) return input.getItem().getCraftingRemainingItem(input);
        return input.getItem() == Items.POTION ? new ItemStack(Items.GLASS_BOTTLE) : ItemStack.EMPTY;
    }
    public static ListTag snapshot() {
        ListTag result = new ListTag();
        for (CustomSpice spice : local.values()) {
            JsonObject client = new JsonParser().parse(spice.definition.toString()).getAsJsonObject();
            client.remove("_instructions");
            result.add(StringTag.valueOf(client.toString()));
        }
        return result;
    }
    public static void readSnapshot(ListTag list) {
        String signature = list.toString();
        if (signature.equals(serverSnapshot)) return;
        Map<String, CustomSpice> result = new TreeMap<>();
        List<SpiceRecipe> recipes = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            JsonObject definition = new JsonParser().parse(list.getString(i)).getAsJsonObject();
            CustomSpice spice = new CustomSpice(definition, definition.has("_accompaniment") && definition.get("_accompaniment").getAsBoolean());
            result.put(spice.id(), spice);
            if (spice.accompaniment()) continue;
            for (JsonElement element : spice.definition.getAsJsonArray("recipes")) {
                JsonObject spec = element.getAsJsonObject(); ItemStack input = stack(spec.getAsJsonObject("input"));
                ItemStack container = spec.has("return") ? (spec.get("return").isJsonNull() ? ItemStack.EMPTY : stack(spec.getAsJsonObject("return"))) : container(input);
                recipes.add(new SpiceRecipe(input, input.getCount(), Seasoning.powder(Collections.singletonList(spice), 1),
                    CustomSpice.integer(spec, "output_count", 1, 64), container));
            }
        }
        serverDefinitions = result; serverRecipes = recipes; serverSnapshot = signature;
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new DefinitionsChanged());
    }
    private CustomSpices() {}
}
