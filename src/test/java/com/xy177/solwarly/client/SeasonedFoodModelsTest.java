package com.xy177.solwarly.client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraft.util.EnumFacing;
import org.junit.Test;
import javax.vecmath.Matrix4f;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class SeasonedFoodModelsTest {
    private BakedQuad quad() {
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.ITEM);
        builder.setTexture(mock(TextureAtlasSprite.class)); builder.setQuadOrientation(EnumFacing.SOUTH); builder.setQuadTint(2);
        float[][] positions = {{0, 0, .5f}, {1, 0, .5f}, {1, 1, .5f}, {0, 1, .5f}};
        for (float[] position : positions) for (int e = 0; e < DefaultVertexFormats.ITEM.getElementCount(); e++) {
            switch (DefaultVertexFormats.ITEM.getElement(e).getUsage()) {
                case POSITION: builder.put(e, position); break;
                case COLOR: builder.put(e, 1, 1, 1, 1); break;
                case NORMAL: builder.put(e, 0, 0, 1); break;
                default: builder.put(e, 0, 0); break;
            }
        }
        return builder.build();
    }
    private float[] attribute(BakedQuad quad, int vertex, int element) {
        float[] data = new float[4]; LightUtil.unpack(quad.getVertexData(), data, quad.getFormat(), vertex, element); return data;
    }
    @Test public void badgeOccupiesBottomLeftQuarterAndKeepsItsOwnColor() {
        BakedQuad badge = SeasonedFoodModels.transform(quad(), null, true, 0xD84C3F, new float[]{.8f});
        assertArrayEquals(new float[]{0, 0, .8f}, java.util.Arrays.copyOf(attribute(badge, 0, 0), 3), .0001f);
        assertArrayEquals(new float[]{.5f, .5f, .8f}, java.util.Arrays.copyOf(attribute(badge, 2, 0), 3), .0001f);
        assertEquals(-1, badge.getTintIndex());
        float[] rgb = attribute(badge, 0, 1);
        assertEquals(216 / 255f, rgb[0], .004f); assertEquals(76 / 255f, rgb[1], .004f); assertEquals(63 / 255f, rgb[2], .004f);
        assertFalse(badge.shouldApplyDiffuseLighting());
    }
    @Test public void foodKeepsTintAndGuiTransformWhileBadgeCanCoverItsFront() {
        Matrix4f camera = new Matrix4f(); camera.setIdentity(); camera.m03 = .1f; camera.m23 = .2f;
        float[] front = {.5f}; BakedQuad food = SeasonedFoodModels.transform(quad(), camera, false, -1, front);
        assertEquals(2, food.getTintIndex());
        assertArrayEquals(new float[]{.1f, 0, .7f}, java.util.Arrays.copyOf(attribute(food, 0, 0), 3), .0001f);
        assertEquals(.7f, front[0], .0001f);
        assertArrayEquals(new float[]{1, 1, 1, 1}, attribute(food, 0, 1), .004f);
    }
    @Test public void grinderDirectionalShadeTracksFacingAndAnimatedTilt() {
        javax.vecmath.Vector3f normal=new javax.vecmath.Vector3f(0,0,1);
        assertEquals(.8F,SpiceGrinderRenderer.shade(normal),.001F);
        SpiceGrinderRenderer.orientation(90,0).transform(normal); assertEquals(.6F,SpiceGrinderRenderer.shade(normal),.001F);
        normal.set(0,1,0); SpiceGrinderRenderer.orientation(0,180).transform(normal); assertEquals(.5F,SpiceGrinderRenderer.shade(normal),.001F);
    }
    @Test public void itemMeshWithoutLightmapCanBeRenderedAsLitBlock() {
        UnpackedBakedQuad.Builder block = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
        quad().pipe(SpiceGrinderRenderer.litConsumer(block, .6F, (240 << 16) | 144));
        BakedQuad lit = block.build();
        float[] color = attribute(lit, 0, 1);
        assertEquals(.6F, color[0], .004F); assertEquals(1F, color[3], .004F);
        int uv = lit.getFormat().getUvOffsetById(1) / 4;
        int packedLight = lit.getVertexData()[uv];
        assertEquals(144, packedLight & 65535); assertEquals(240, packedLight >>> 16);
    }
    @Test public void secondAccompanimentBadgeOccupiesOtherBottomQuarter() {
        BakedQuad badge = SeasonedFoodModels.transform(quad(), null, true, 0xFFFFFF, new float[]{.8F}, .5F);
        assertArrayEquals(new float[]{.5F, 0, .8F}, java.util.Arrays.copyOf(attribute(badge, 0, 0), 3), .0001F);
        assertArrayEquals(new float[]{1F, .5F, .8F}, java.util.Arrays.copyOf(attribute(badge, 2, 0), 3), .0001F);
    }
}
