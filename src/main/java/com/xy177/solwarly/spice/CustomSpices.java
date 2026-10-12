package com.xy177.solwarly.spice;

import com.google.gson.*;
import com.xy177.solwarly.SOLWarly;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
        return serverDefinitions != null && FMLCommonHandler.instance().getEffectiveSide().isClient() ? serverDefinitions : local;
    }
    public static Collection<CustomSpice> all() { return Collections.unmodifiableCollection(definitions().values()); }
    public static CustomSpice find(String id) { return definitions().get(id); }
    public static void clearServerDefinitions() {
        serverDefinitions = null; serverRecipes = null; serverSnapshot = null;
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new DefinitionsChanged());
    }
    public static boolean owns(SpiceRecipe recipe) { return registered.contains(recipe); }
    public static Collection<SpiceRecipe> displayRecipes() {
        return serverRecipes != null && FMLCommonHandler.instance().getEffectiveSide().isClient() ? serverRecipes : registered;
    }
    public static final class DefinitionsChanged extends net.minecraftforge.fml.common.eventhandler.Event {}

    public static void load(File modDirectory) {
        registered.forEach(SpiceRecipes::remove); registered.clear(); local.clear();
        File directory = new File(modDirectory, "custom_spices");
        try {
            Files.createDirectories(directory.toPath());
            File example = new File(directory, "_example.json");
            if (!example.exists()) try (InputStream input = CustomSpices.class.getResourceAsStream("/assets/solwarly/custom_spices/_example.json")) {
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
            CustomSpice spice = new CustomSpice(json);
            if (Arrays.stream(SpiceKind.values()).anyMatch(kind -> kind.id().equals(spice.id())) || local.containsKey(spice.id()))
                throw new IllegalArgumentException("Duplicate spice ID: " + spice.id());
            JsonArray recipes = json.getAsJsonArray("recipes");
            if (recipes == null || recipes.size() == 0) throw new IllegalArgumentException("At least one grinding recipe is required");
            List<SpiceRecipe> pending = new ArrayList<>();
            for (JsonElement element : recipes) {
                JsonObject spec = element.getAsJsonObject();
                ItemStack input = stack(spec.getAsJsonObject("input"));
                int x = CustomSpice.integer(spec.getAsJsonObject("input"), "count", 1, input.getMaxStackSize());
                int y = CustomSpice.integer(spec, "output_count", 1, 64);
                if (SpiceRecipes.find(input) != null || pending.stream().anyMatch(recipe -> recipe.matches(input)))
                    throw new IllegalArgumentException("Conflicting grinding input: " + input.getItem().getRegistryName() + "@" + input.getMetadata());
                ItemStack container = spec.has("return") ? (spec.get("return").isJsonNull() ? ItemStack.EMPTY : stack(spec.getAsJsonObject("return"))) : container(input);
                pending.add(new SpiceRecipe(input, x, Seasoning.powder(Collections.singletonList(spice), 1), y, container));
            }
            local.put(spice.id(), spice);
            pending.forEach(SpiceRecipes::register); registered.addAll(pending);
            SOLWarly.LOGGER.info("Loaded custom spice {} from {}", spice.id(), file.getName());
        } catch (IOException | RuntimeException e) {
            SOLWarly.LOGGER.error("Skipping custom spice file {}: {}", file.getName(), e.getMessage());
        }
    }
    private static ItemStack stack(JsonObject spec) {
        String id = CustomSpice.required(spec, "item"); Item item = Item.getByNameOrId(id);
        if (item == null || item == Items.AIR) throw new IllegalArgumentException("Unknown item: " + id);
        int meta = spec.has("metadata") ? CustomSpice.integer(spec, "metadata", 0, 32766) : 0;
        int count = spec.has("count") ? CustomSpice.integer(spec, "count", 1, item.getItemStackLimit()) : 1;
        ItemStack stack = new ItemStack(item, count, meta);
        if (spec.has("nbt")) try { stack.setTagCompound(JsonToNBT.getTagFromJson(spec.get("nbt").getAsString())); }
        catch (NBTException e) { throw new IllegalArgumentException("Invalid input/return NBT", e); }
        return stack;
    }
    private static ItemStack container(ItemStack input) {
        if (input.getItem().hasContainerItem(input)) return input.getItem().getContainerItem(input);
        return input.getItem() == Items.POTIONITEM ? new ItemStack(Items.GLASS_BOTTLE) : ItemStack.EMPTY;
    }
    public static NBTTagList snapshot() {
        NBTTagList result = new NBTTagList();
        for (CustomSpice spice : local.values()) {
            JsonObject client = new JsonParser().parse(spice.definition.toString()).getAsJsonObject();
            client.remove("_instructions");
            result.appendTag(new NBTTagString(client.toString()));
        }
        return result;
    }
    public static void readSnapshot(NBTTagList list) {
        String signature = list.toString();
        if (signature.equals(serverSnapshot)) return;
        Map<String, CustomSpice> result = new TreeMap<>();
        List<SpiceRecipe> recipes = new ArrayList<>();
        for (int i = 0; i < list.tagCount(); i++) {
            CustomSpice spice = new CustomSpice(new JsonParser().parse(list.getStringTagAt(i)).getAsJsonObject());
            result.put(spice.id(), spice);
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
