package com.xy177.solwarly.client;

import com.google.gson.*;
import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.block.BlockSpiceGrinder;
import com.xy177.solwarly.spice.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.obj.*;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID, value = Side.CLIENT)
public final class SpiceGrinderRenderer extends TileEntitySpecialRenderer<TileEntitySpiceGrinder> {
    private static IModel mesh;
    private static IBakedModel stand, jar;
    private static final NavigableMap<Double, Float> rotations = new TreeMap<>();
    private static double animationLength = 1.2;
    private static final List<String> STAND = Arrays.asList("left_leg", "right_leg", "left_foot", "right_foot", "waist_axle");
    private static final ResourceLocation MODEL = new ResourceLocation(SOLWarly.MOD_ID, "models/block/spice_grinder.obj");

    @SubscribeEvent public static void models(ModelRegistryEvent event) {
        OBJLoader.INSTANCE.addDomain(SOLWarly.MOD_ID);
        // The TESR handles direction; all states share the particle-supplying block model.
        ModelLoader.setCustomStateMapper(BlockSpiceGrinder.INSTANCE, new StateMap.Builder().ignore(BlockSpiceGrinder.FACING).build());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySpiceGrinder.class, new SpiceGrinderRenderer());
        ModelLoader.setCustomModelResourceLocation(Item.getByNameOrId("solwarly:spice_grinder"), 0,
            new ModelResourceLocation("solwarly:spice_grinder", "inventory"));
        ModelLoader.setCustomModelResourceLocation(SpiceRegistry.spice, 0, new ModelResourceLocation("solwarly:spice", "inventory"));
    }
    @SubscribeEvent public static void textures(TextureStitchEvent.Pre event) {
        try {
            mesh = OBJLoader.INSTANCE.loadModel(MODEL);
            for (ResourceLocation texture : mesh.getTextures()) event.getMap().registerSprite(texture);
            rotations.clear();
            try (InputStreamReader reader = new InputStreamReader(Minecraft.getMinecraft().getResourceManager()
                .getResource(new ResourceLocation("solwarly:animations/spice_grinder.json")).getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject animation = new JsonParser().parse(reader).getAsJsonObject().getAsJsonObject("animations")
                    .getAsJsonObject("animation.portable_grinding_mill.grinding");
                animationLength = animation.get("animation_length").getAsDouble();
                JsonObject keys = animation.getAsJsonObject("bones").getAsJsonObject("jar").getAsJsonObject("rotation");
                for (Map.Entry<String, JsonElement> key : keys.entrySet())
                    rotations.put(Double.parseDouble(key.getKey()), key.getValue().getAsJsonArray().get(0).getAsFloat());
            }
        } catch (Exception e) { throw new IllegalStateException("Cannot load spice grinder model/animation", e); }
    }
    @SubscribeEvent public static void bake(ModelBakeEvent event) {
        OBJModel model = (OBJModel) mesh;
        List<String> animated = new ArrayList<>(model.getMatLib().getGroups().keySet()); animated.removeAll(STAND);
        java.util.function.Function<ResourceLocation, net.minecraft.client.renderer.texture.TextureAtlasSprite> textures =
            location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
        stand = model.bake(new OBJModel.OBJState(STAND, true), DefaultVertexFormats.ITEM, textures);
        jar = model.bake(new OBJModel.OBJState(animated, true), DefaultVertexFormats.ITEM, textures);
        IBakedModel whole = model.bake(new OBJModel.OBJState(new ArrayList<>(model.getMatLib().getGroups().keySet()), true),
            DefaultVertexFormats.ITEM, textures);
        net.minecraft.client.renderer.texture.TextureAtlasSprite particle = textures.apply(new ResourceLocation("minecraft:blocks/brick"));
        IBakedModel baked = new BakedModelWrapper<IBakedModel>(whole) {
            @Override public net.minecraft.client.renderer.texture.TextureAtlasSprite getParticleTexture() { return particle; }
        };
        try (InputStreamReader reader = new InputStreamReader(Minecraft.getMinecraft().getResourceManager()
            .getResource(new ResourceLocation("solwarly:models/item/spice_grinder.json")).getInputStream(), StandardCharsets.UTF_8)) {
            ModelBlock display = ModelBlock.deserialize(reader);
            event.getModelRegistry().putObject(new ModelResourceLocation("solwarly:spice_grinder", "inventory"),
                new PerspectiveMapWrapper(baked, PerspectiveMapWrapper.getTransforms(display.getAllTransforms())));
        } catch (java.io.IOException e) { throw new IllegalStateException("Cannot load spice grinder item transforms", e); }
        // The world uses the TESR; the block model still supplies its debris sprite.
        event.getModelRegistry().putObject(new ModelResourceLocation("solwarly:spice_grinder", "normal"), baked);
    }
    public static float rotation(double seconds) {
        if (rotations.isEmpty()) return 0;
        double time = ((seconds % animationLength) + animationLength) % animationLength;
        Map.Entry<Double, Float> left = rotations.floorEntry(time), right = rotations.ceilingEntry(time);
        if (left == null) return rotations.firstEntry().getValue();
        if (right == null || right.getKey().equals(left.getKey())) return left.getValue();
        return (float) (left.getValue() + (right.getValue() - left.getValue()) * (time - left.getKey()) / (right.getKey() - left.getKey()));
    }
    @Override public void render(TileEntitySpiceGrinder tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (stand == null || jar == null) return;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(.5, 0, .5);
        GlStateManager.rotate(-tile.getWorld().getBlockState(tile.getPos()).getValue(BlockSpiceGrinder.FACING).getHorizontalAngle(), 0, 1, 0);
        GlStateManager.translate(-.5, 0, -.5);
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.disableCull();
        int light = tile.getWorld().getCombinedLight(tile.getPos().up(), 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light & 65535, light >> 16);
        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(stand, 1, 1, 1, 1);
        GlStateManager.translate(.5, 13.957447 / 16, .5);
        if (tile.isWorking()) GlStateManager.rotate(rotation((tile.getWorld().getTotalWorldTime() - tile.animationStart() + partialTicks) / 20.0), 1, 0, 0);
        GlStateManager.translate(-.5, -13.957447 / 16, -.5);
        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(jar, 1, 1, 1, 1);
        GlStateManager.enableCull(); GlStateManager.popMatrix();
    }
    @SubscribeEvent public static void init(SOLWarly.InitializationEvent event) {
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(ItemSpice::color, SpiceRegistry.spice);
    }
}
