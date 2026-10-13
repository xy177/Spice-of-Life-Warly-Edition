package com.xy177.solwarly.client;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xy177.solwarly.block.BlockSpiceGrinder;
import com.xy177.solwarly.spice.TileEntitySpiceGrinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import java.io.Reader;
import java.util.*;

public final class SpiceGrinderRenderer implements BlockEntityRenderer<TileEntitySpiceGrinder> {
    public static final ResourceLocation STAND=new ResourceLocation("solwarly","block/spice_grinder_stand"),JAR=new ResourceLocation("solwarly","block/spice_grinder_jar");
    private static final NavigableMap<Double,Float> rotations=new TreeMap<>();
    private static double animationLength=1.2;
    public SpiceGrinderRenderer(BlockEntityRendererProvider.Context context) {}
    public static void loadAnimation(net.minecraft.server.packs.resources.ResourceManager resources) {
        rotations.clear();
        try(Reader reader=resources.getResource(new ResourceLocation("solwarly","animations/spice_grinder.json")).orElseThrow().openAsReader()) {
            var animation=JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("animations").getAsJsonObject("animation.portable_grinding_mill.grinding");
            animationLength=animation.get("animation_length").getAsDouble();
            for(var entry:animation.getAsJsonObject("bones").getAsJsonObject("jar").getAsJsonObject("rotation").entrySet())rotations.put(Double.parseDouble(entry.getKey()),entry.getValue().getAsJsonArray().get(0).getAsFloat());
        }catch(Exception e) { throw new IllegalStateException("Cannot read spice grinder animation",e); }
    }
    public static float rotation(double seconds) {
        if(rotations.isEmpty())return 0;double time=((seconds%animationLength)+animationLength)%animationLength;
        var left=rotations.floorEntry(time);var right=rotations.ceilingEntry(time);
        if(left==null)return rotations.firstEntry().getValue();if(right==null||right.getKey().equals(left.getKey()))return left.getValue();
        return (float)(left.getValue()+(right.getValue()-left.getValue())*(time-left.getKey())/(right.getKey()-left.getKey()));
    }
    @Override public void render(TileEntitySpiceGrinder tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
        var minecraft=Minecraft.getInstance();var models=minecraft.getModelManager();
        float yaw=-tile.getBlockState().getValue(BlockSpiceGrinder.FACING).toYRot();
        var orientation=Axis.YP.rotationDegrees(yaw);
        var consumer=buffers.getBuffer(RenderType.cutout());pose.pushPose();pose.translate(.5,0,.5);
        pose.mulPose(orientation);pose.translate(-.5,0,-.5);
        renderPart(tile,models.getModel(STAND),pose,consumer,orientation,0,overlay);
        pose.translate(.5,13.957447/16,.5);
        float tilt=tile.isWorking()?rotation((tile.getLevel().getGameTime()-tile.animationStart()+partial)/20.0):0;
        pose.mulPose(Axis.XP.rotationDegrees(tilt));
        pose.translate(-.5,-13.957447/16,-.5);renderPart(tile,models.getModel(JAR),pose,consumer,orientation,tilt,overlay);pose.popPose();
        var preview = tile.displayStack(partial);
        if (!preview.isEmpty()) {
            double time = tile.getLevel().getGameTime() + partial;
            pose.pushPose(); pose.translate(.5, 1.85 + Math.sin(time / 10) * .04, .5);
            pose.mulPose(Axis.YP.rotationDegrees((float) (time * 3 % 360))); pose.scale(.65F, .65F, .65F);
            int previewLight=LevelRenderer.getLightColor(tile.getLevel(),tile.getBlockPos().above(2));
            minecraft.getItemRenderer().renderStatic(preview, net.minecraft.world.item.ItemDisplayContext.GROUND, previewLight, overlay, pose, buffers, tile.getLevel(), 0);
            pose.popPose();
        }
    }
    /** World light and directional shading follow the model's actual animated orientation. */
    private static void renderPart(TileEntitySpiceGrinder tile,net.minecraft.client.resources.model.BakedModel model,
                                   PoseStack pose,com.mojang.blaze3d.vertex.VertexConsumer consumer,
                                   org.joml.Quaternionf yaw,float tilt,int overlay) {
        var rotation=new org.joml.Quaternionf(yaw).mul(Axis.XP.rotationDegrees(tilt));
        var random=net.minecraft.util.RandomSource.create(42);
        var directions=new ArrayList<net.minecraft.core.Direction>(Arrays.asList(net.minecraft.core.Direction.values()));
        directions.add(null);
        for(var direction:directions) {
            random.setSeed(42);
            for(var quad:model.getQuads(tile.getBlockState(),direction,random)) {
                var normal=new org.joml.Vector3f(quad.getDirection().step()).rotate(rotation);
                float shade=tile.getLevel().getShade(normal.x,normal.y,normal.z,quad.isShade());
                var center=new org.joml.Vector3f();var data=quad.getVertices();int stride=data.length/4;
                for(int vertex=0;vertex<4;vertex++)center.add(Float.intBitsToFloat(data[vertex*stride]),Float.intBitsToFloat(data[vertex*stride+1]),Float.intBitsToFloat(data[vertex*stride+2]));
                center.mul(.25F).sub(.5F,13.957447F/16,.5F).rotate(Axis.XP.rotationDegrees(tilt))
                    .add(0,13.957447F/16,0).rotate(yaw).add(.5F,0,.5F).fma(.01F,normal);
                var pos=tile.getBlockPos();
                int light=LevelRenderer.getLightColor(tile.getLevel(),net.minecraft.core.BlockPos.containing(pos.getX()+center.x,pos.getY()+center.y,pos.getZ()+center.z));
                consumer.putBulkData(pose.last(),quad,shade,shade,shade,light,overlay);
            }
        }
    }
}
