package com.xy177.solwarly;

import net.minecraft.client.resources.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.*;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Uses Forge's actual mesh parser and quad baker without opening an OpenGL window. */
public class SpiceGrinderModelTest {
    @Test public void suppliedMeshLoadsAndBakesAsSeparateStandAndJar() throws Exception {
        IResourceManager resources = mock(IResourceManager.class);
        when(resources.getResource(any(ResourceLocation.class))).thenAnswer(call -> {
            ResourceLocation location = call.getArgument(0);
            String path = "/assets/" + location.getNamespace() + "/" + location.getPath();
            java.io.InputStream input = getClass().getResourceAsStream(path);
            if (input == null) throw new java.io.FileNotFoundException(path);
            IResource resource = mock(IResource.class);
            when(resource.getResourceLocation()).thenReturn(location); when(resource.getInputStream()).thenReturn(input);
            return resource;
        });
        OBJLoader.INSTANCE.onResourceManagerReload(resources);
        OBJModel model = (OBJModel) OBJLoader.INSTANCE.loadModel(new ResourceLocation("solwarly:models/block/spice_grinder.obj"));
        assertTrue(model.getTextures().contains(new ResourceLocation("solwarly:blocks/spice_grinder")));
        List<String> stand = Arrays.asList("left_leg", "right_leg", "left_foot", "right_foot", "waist_axle");
        List<String> jar = new ArrayList<>(model.getMatLib().getGroups().keySet()); jar.removeAll(stand);
        TextureAtlasSprite sprite = mock(TextureAtlasSprite.class);
        when(sprite.getInterpolatedU(anyDouble())).thenAnswer(call -> ((Double)call.getArgument(0)).floatValue() / 16);
        when(sprite.getInterpolatedV(anyDouble())).thenAnswer(call -> ((Double)call.getArgument(0)).floatValue() / 16);
        IBakedModel fixed = model.bake(new OBJModel.OBJState(stand, true), DefaultVertexFormats.ITEM, texture -> sprite);
        IBakedModel moving = model.bake(new OBJModel.OBJState(jar, true), DefaultVertexFormats.ITEM, texture -> sprite);
        assertEquals(30, fixed.getQuads(null, null, 0).size());
        assertEquals(146, moving.getQuads(null, null, 0).size());
    }
    @Test public void suppliedWorkingAnimationHasStaticRestPoseAndRealMovement() throws Exception {
        try (java.io.Reader reader = new java.io.InputStreamReader(getClass().getResourceAsStream("/assets/solwarly/animations/spice_grinder.json"), java.nio.charset.StandardCharsets.UTF_8)) {
            com.google.gson.JsonObject animations = new com.google.gson.JsonParser().parse(reader).getAsJsonObject().getAsJsonObject("animations");
            assertEquals(0, animations.getAsJsonObject("animation.portable_grinding_mill.idle").getAsJsonObject("bones").size());
            com.google.gson.JsonObject working = animations.getAsJsonObject("animation.portable_grinding_mill.grinding");
            assertEquals(1.2, working.get("animation_length").getAsDouble(), 0);
            com.google.gson.JsonObject bones = working.getAsJsonObject("bones"); assertEquals(1, bones.size());
            com.google.gson.JsonObject rotation = bones.getAsJsonObject("jar").getAsJsonObject("rotation");
            assertEquals(73, rotation.size()); assertEquals(-30, rotation.getAsJsonArray("0.15").get(0).getAsDouble(), 0);
            assertEquals(0, rotation.getAsJsonArray("0").get(0).getAsDouble(), 0);
        }
    }
    @Test public void spiceLayersTintOnlyPowderAndUseSuppliedTransparentImages() throws Exception {
        net.minecraft.item.ItemStack spice = com.xy177.solwarly.spice.ItemSpice.create("red", 0xCA3412, 1);
        assertEquals(0xCA3412, com.xy177.solwarly.spice.ItemSpice.color(spice, 0));
        assertEquals(0xFFFFFF, com.xy177.solwarly.spice.ItemSpice.color(spice, 1));
        try (java.io.Reader reader = new java.io.InputStreamReader(getClass().getResourceAsStream("/assets/solwarly/models/item/spice.json"), java.nio.charset.StandardCharsets.UTF_8)) {
            com.google.gson.JsonObject textures = new com.google.gson.JsonParser().parse(reader).getAsJsonObject().getAsJsonObject("textures");
            assertEquals("solwarly:items/spice_powder", textures.get("layer0").getAsString());
            assertEquals("solwarly:items/spice_container_paper", textures.get("layer1").getAsString());
        }
        for (String path : new String[] {"spice_powder", "spice_container_paper"}) {
            try (java.io.InputStream input = getClass().getResourceAsStream("/assets/solwarly/textures/items/" + path + ".png")) {
                java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(input);
                assertEquals(16, image.getWidth()); assertEquals(16, image.getHeight()); assertTrue(image.getColorModel().hasAlpha());
            }
        }
    }
}
