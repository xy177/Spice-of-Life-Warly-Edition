package com.xy177.solwarly.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.spice.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.pipeline.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;
import javax.vecmath.*;
import java.util.*;

/** Adds a GUI-only badge without replacing food items, their textures or hand models. */
@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID, value = Side.CLIENT)
public final class SeasonedFoodModels {
    private static final Cache<List<Object>, IBakedModel> GUI_MODELS = CacheBuilder.newBuilder().maximumSize(512).build();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void bake(ModelBakeEvent event) {
        GUI_MODELS.invalidateAll();
        for (ModelResourceLocation key : event.getModelRegistry().getKeys()) {
            IBakedModel original = event.getModelRegistry().getObject(key);
            event.getModelRegistry().putObject(key, new FoodModel(original));
        }
    }

    private static IBakedModel unwrap(IBakedModel model) {
        return model instanceof FoodModel ? ((FoodModel) model).original() : model;
    }

    private static final class FoodModel extends BakedModelWrapper<IBakedModel> {
        private final ItemOverrideList overrides = new ItemOverrideList(Collections.emptyList()) {
            @Override public IBakedModel handleItemState(IBakedModel model, ItemStack stack, World world, EntityLivingBase entity) {
                IBakedModel resolved = unwrap(originalModel.getOverrides().handleItemState(originalModel, stack, world, entity));
                List<SpiceKind> components = Seasoning.components(stack);
                if (!Seasoning.seasoned(stack) || components.isEmpty()) return resolved;
                List<ItemStack> badges = Seasoning.badges(stack);
                return new SeasonedModel(resolved, badges);

            }
        };
        FoodModel(IBakedModel original) { super(original); }
        IBakedModel original() { return originalModel; }
        @Override public ItemOverrideList getOverrides() { return overrides; }
    }

    private static final class SeasonedModel extends BakedModelWrapper<IBakedModel> {
        private final List<ItemStack> badges;
        SeasonedModel(IBakedModel original, List<ItemStack> badges) { super(original); this.badges = badges; }
        @Override public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType type) {
            Pair<? extends IBakedModel, Matrix4f> food = originalModel.handlePerspective(type);
            if (type != ItemCameraTransforms.TransformType.GUI || food.getLeft().isBuiltInRenderer()) return food;
            List<Pair<? extends IBakedModel, Matrix4f>> icons = new ArrayList<>();
            List<Object> key = new ArrayList<>(Arrays.asList(food.getLeft(), food.getRight()));
            for (ItemStack badge : badges) {
                IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(badge, null, null);
                Pair<? extends IBakedModel, Matrix4f> icon = model.handlePerspective(type); icons.add(icon);
                key.addAll(Arrays.asList(icon.getLeft(), icon.getRight(), badge.getItem(), badge.getMetadata(), badge.getTagCompound()));
            }
            IBakedModel cached = GUI_MODELS.getIfPresent(key);
            if (cached == null) { cached = compose(food, icons, badges); GUI_MODELS.put(key, cached); }
            return Pair.of(cached, null);
        }
    }

    private static List<BakedQuad> quads(IBakedModel model) {
        List<BakedQuad> result = new ArrayList<>(model.getQuads(null, null, 0));
        for (EnumFacing face : EnumFacing.values()) result.addAll(model.getQuads(null, face, 0));
        return result;
    }

    private static IBakedModel compose(Pair<? extends IBakedModel, Matrix4f> food,
                                       List<Pair<? extends IBakedModel, Matrix4f>> icons, List<ItemStack> badges) {
        List<BakedQuad> result = new ArrayList<>();
        float[] front = { .5f };
        for (BakedQuad quad : quads(food.getLeft())) result.add(transform(quad, food.getRight(), false, -1, front));
        float[] depth = { front[0] + .002f };
        for (int i = 0; i < icons.size(); i++) {
            Pair<? extends IBakedModel, Matrix4f> icon = icons.get(i); ItemStack badge = badges.get(i);
            for (BakedQuad quad : quads(icon.getLeft())) {
                int color = quad.hasTintIndex() ? Minecraft.getMinecraft().getItemColors().colorMultiplier(badge, quad.getTintIndex()) : 0xFFFFFF;
                result.add(transform(quad, icon.getRight(), true, color, depth, Math.min(i,1) * .5F, i == 2 ? .5F : 0));
            }
        }

        List<BakedQuad> immutable = Collections.unmodifiableList(result);
        return new BakedModelWrapper<IBakedModel>(food.getLeft()) {
            @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
                return side == null ? immutable : Collections.emptyList();
            }
        };
    }

    static BakedQuad transform(BakedQuad quad, Matrix4f camera, boolean badge, int color, float[] depth) {
        return transform(quad, camera, badge, color, depth, 0);
    }
    static BakedQuad transform(BakedQuad quad, Matrix4f camera, boolean badge, int color, float[] depth, float badgeX) {
        return transform(quad,camera,badge,color,depth,badgeX,0);
    }
    static BakedQuad transform(BakedQuad quad, Matrix4f camera, boolean badge, int color, float[] depth, float badgeX, float badgeY) {
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.ITEM);
        quad.pipe(new VertexTransformer(builder) {
            @Override public void setQuadTint(int tint) { super.setQuadTint(badge ? -1 : tint); }
            @Override public void setApplyDiffuseLighting(boolean diffuse) { super.setApplyDiffuseLighting(!badge && diffuse); }
            @Override public void put(int element, float... data) {
                float[] values = data.clone();
                switch (getVertexFormat().getElement(element).getUsage()) {
                    case POSITION:
                        Vector4f position = new Vector4f(values[0] - .5f, values[1] - .5f, values[2] - .5f, 1);
                        if (camera != null) camera.transform(position);
                        values[0] = badge ? badgeX + .25f + position.x * .5f : position.x + .5f;
                        values[1] = badge ? badgeY + .25f + position.y * .5f : position.y + .5f;
                        values[2] = badge ? depth[0] + position.z * .01f : position.z + .5f;
                        if (!badge) depth[0] = Math.max(depth[0], values[2]);
                        break;
                    case NORMAL:
                        if (camera != null) {
                            Vector3f normal = new Vector3f(values[0], values[1], values[2]);
                            camera.transform(normal); normal.normalize();
                            values[0] = normal.x; values[1] = normal.y; values[2] = normal.z;
                        }
                        break;
                    case COLOR:
                        if (badge) {
                            values[0] *= (color >> 16 & 255) / 255f;
                            values[1] *= (color >> 8 & 255) / 255f;
                            values[2] *= (color & 255) / 255f;
                        }
                        break;
                    default: break;
                }
                super.put(element, values);
            }
        });
        return builder.build();
    }
    private SeasonedFoodModels() {}
}
