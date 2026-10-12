// Added by xy177 on 2026-10-10 for Spice of Life Warly Edition (LGPL 2.1).
package com.xy177.solwarly;

import com.cazsius.solcarrot.api.SOLCarrotAPI;
import com.xy177.solwarly.tracking.FoodList;
import com.xy177.solwarly.tracking.FoodInstance;
import com.xy177.solwarly.tracking.MaxHealthHandler;
import com.xy177.solwarly.diet.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModMetadata;
import org.junit.BeforeClass;
import org.junit.Test;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.IAppleCoreAccessor;
import squeek.applecore.api.food.FoodValues;

import java.util.HashSet;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Exercises the 1.8.4 persistent format and existing health modifier together. */
public class CarrotReplacementTest {
    private static final UUID LEGACY_HEALTH_ID = UUID.fromString("b20d3436-0d39-4868-96ab-d0a4856e68c6");

    @BeforeClass
    public static void bootstrap() {
        Bootstrap.register();
        AppleCoreAPI.accessor = mock(IAppleCoreAccessor.class);
        when(AppleCoreAPI.accessor.isFood(any(ItemStack.class))).thenReturn(true);
        when(AppleCoreAPI.accessor.getFoodValues(any(ItemStack.class))).thenReturn(new FoodValues(4, 0.3F));
    }

    private FoodList readLegacyHistory() {
        SOLWarlyConfig.baseHearts = 10;
        SOLWarlyConfig.heartsPerMilestone = 2;
        SOLWarlyConfig.milestones = new int[] { 2, 5, 10 };
        SOLWarlyConfig.foodBlacklist = new String[0];
        SOLWarlyConfig.foodWhitelist = new String[0];
        SOLWarlyConfig.minimumFoodValue = 1;

        NBTTagList saved = new NBTTagList();
        saved.appendTag(new NBTTagString("minecraft:apple@0"));
        saved.appendTag(new NBTTagString("minecraft:bread@0"));
        saved.appendTag(new NBTTagString("minecraft:fish@1"));
        FoodList loaded = new FoodList();
        loaded.deserializeNBT(saved);
        return loaded;
    }

    @Test
    public void legacyHistoryRetainsFoodsMetadataAndProgressAfterSavingAgain() {
        FoodList loaded = readLegacyHistory();
        assertEquals(3, loaded.getEatenFoodCount());
        assertTrue(loaded.hasEaten(new ItemStack(Items.APPLE)));
        assertTrue(loaded.hasEaten(new ItemStack(Items.FISH, 1, 1)));
        assertFalse(loaded.hasEaten(new ItemStack(Items.FISH, 1, 0)));
        assertEquals(1, loaded.getProgressInfo().milestonesAchieved());
        assertEquals(2, loaded.getProgressInfo().foodsUntilNextMilestone());

        NBTTagList savedAgain = (NBTTagList) loaded.serializeNBT();
        Set<String> entries = new HashSet<>();
        for (int i = 0; i < savedAgain.tagCount(); i++) {
            entries.add(savedAgain.getStringTagAt(i));
        }
        assertEquals(3, entries.size());
        assertTrue(entries.contains("minecraft:apple@0"));
        assertTrue(entries.contains("minecraft:bread@0"));
        assertTrue(entries.contains("minecraft:fish@1"));

        FoodList reloaded = new FoodList();
        reloaded.deserializeNBT(savedAgain);
        assertEquals(loaded.getEatenFoodCount(), reloaded.getEatenFoodCount());
        assertEquals(loaded.getProgressInfo().milestonesAchieved(), reloaded.getProgressInfo().milestonesAchieved());
    }

    @Test
    public void replacingCarrotDoesNotDuplicateBonusOrChangeCurrentHealth() {
        FoodList foodList = readLegacyHistory();
        AttributeMap attributes = new AttributeMap();
        IAttributeInstance maxHealth = attributes.registerAttribute(SharedMonsterAttributes.MAX_HEALTH);
        maxHealth.setBaseValue(20);
        maxHealth.applyModifier(new AttributeModifier(LEGACY_HEALTH_ID, "Health Gained from Trying New Foods", 4, 0));

        EntityPlayer player = mock(EntityPlayer.class);
        player.world = mock(World.class);
        when(player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)).thenReturn(maxHealth);
        when(player.getCapability(SOLCarrotAPI.foodCapability, null)).thenReturn(foodList);
        when(player.getMaxHealth()).thenAnswer(invocation -> (float) maxHealth.getAttributeValue());
        when(player.getHealth()).thenReturn(12.0F);

        assertFalse(MaxHealthHandler.updateFoodHPModifier(player));
        assertEquals(24.0, maxHealth.getAttributeValue(), 0.0);
        assertEquals(1, maxHealth.getModifiers().size());
        verify(player, never()).setHealth(anyFloat());

        // A later legitimate milestone replaces the old modifier instead of stacking it.
        SOLWarlyConfig.milestones = new int[] { 1, 2, 3 };
        foodList.updateProgressInfo();
        assertTrue(MaxHealthHandler.updateFoodHPModifier(player));
        assertEquals(32.0, maxHealth.getAttributeValue(), 0.0);
        assertEquals(1, maxHealth.getModifiers().size());
        verify(player, never()).setHealth(anyFloat());
    }

    @Test
    public void legacyHistoryWithNoLongerEdibleItemRemainsReadable() {
        readLegacyHistory();
        when(AppleCoreAPI.accessor.isFood(argThat(stack -> stack != null && stack.getItem() == Items.BOWL))).thenReturn(false);
        when(AppleCoreAPI.accessor.getFoodValues(argThat(stack -> stack != null && stack.getItem() == Items.BOWL))).thenReturn(null);
        try {
            NBTTagList saved = new NBTTagList();
            saved.appendTag(new NBTTagString("minecraft:bowl@0"));
            FoodList loaded = new FoodList();
            loaded.deserializeNBT(saved);
            assertEquals(1, loaded.getEatenFoodCount());
            assertEquals(0, loaded.getProgressInfo().foodsEaten);
            assertEquals("minecraft:bowl@0", ((NBTTagList) loaded.serializeNBT()).getStringTagAt(0));
            assertNull(FoodInstance.decode("removed_mod:missing_food@0"));
        } finally {
            when(AppleCoreAPI.accessor.isFood(argThat(stack -> stack != null && stack.getItem() == Items.BOWL))).thenReturn(true);
            when(AppleCoreAPI.accessor.getFoodValues(argThat(stack -> stack != null && stack.getItem() == Items.BOWL))).thenReturn(new FoodValues(4, 0.3F));
        }
    }

    @Test
    public void forgeReadsMainAndLegacyMetadataFromTheBuiltResources() throws Exception {
        try (java.io.InputStream input = getClass().getResourceAsStream("/mcmod.info")) {
            assertNotNull(input);
            MetadataCollection collection = MetadataCollection.from(input, "solwarly");
            ModMetadata main = collection.getMetadataForId("solwarly", Collections.emptyMap());
            ModMetadata legacy = collection.getMetadataForId("solcarrot", Collections.emptyMap());
            assertFalse(main.autogenerated);
            assertFalse(legacy.autogenerated);
            assertEquals("1.0.0", main.version);
            assertTrue(main.authorList.contains("xy177"));
            assertEquals("1.8.4", legacy.version);
            assertEquals("solwarly", legacy.parent);
        }
    }

    @Test public void newCumulativeAndDiversityRespectLegacyFloorWithoutHealing() {
        Capability<DietData> previousType = DietCapability.TYPE;
        DietCapability.TYPE = mock(Capability.class);
        try {
            SOLWarlyConfig.baseHearts = 10;
            DietData diet = new DietData(); diet.initialized = true; diet.legacyHealthFloor = 8;
            diet.eat("a", "plain", 20, 5, 32);
            AttributeMap attributes = new AttributeMap();
            IAttributeInstance health = attributes.registerAttribute(SharedMonsterAttributes.MAX_HEALTH);
            health.setBaseValue(20);
            attributes.registerAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.1);
            attributes.registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1);
            EntityPlayer player = mock(EntityPlayer.class); player.world = mock(World.class);
            when(player.getCapability(DietCapability.TYPE, null)).thenReturn(diet);
            when(player.getAttributeMap()).thenReturn(attributes);
            when(player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)).thenReturn(health);
            when(player.getMaxHealth()).thenAnswer(invocation -> (float) health.getAttributeValue());
            when(player.getHealth()).thenReturn(12F);
            MaxHealthHandler.updateFoodHPModifier(player);
            assertEquals(30, health.getAttributeValue(), 0);
            assertEquals(2, health.getModifiers().size());
            MaxHealthHandler.updateFoodHPModifier(player);
            assertEquals(30, health.getAttributeValue(), 0);
            verify(player, never()).setHealth(anyFloat());
            diet.reset(false);
            when(player.getHealth()).thenReturn(30F);
            MaxHealthHandler.updateFoodHPModifier(player);
            assertEquals(28, health.getAttributeValue(), 0);
            verify(player).setHealth(28F);
        } finally { DietCapability.TYPE = previousType; }
    }

    @Test public void missingFoodIdentifiersArePreservedForLaterRestoration() {
        readLegacyHistory();
        NBTTagList tag = new NBTTagList(); tag.appendTag(new NBTTagString("missing_mod:food@3"));
        FoodList list = new FoodList(); list.deserializeNBT(tag);
        assertEquals("missing_mod:food@3", ((NBTTagList) list.serializeNBT()).getStringTagAt(0));
    }
    @Test public void potionRewardsLeaveExternalEffectsAloneAndWithdrawOwnedEffects() {
        Capability<DietData> previousType = DietCapability.TYPE;
        String[] oldAttributes = DietConfig.attributes, oldEffects = DietConfig.effects;
        DietCapability.TYPE = mock(Capability.class);
        try {
            DietConfig.attributes = new String[0]; DietConfig.effects = new String[] {"5|minecraft:speed|0"};
            DietData diet = new DietData(); diet.initialized = true; diet.eat("a", "plain", 5, 5, 32);
            EntityPlayer player = mock(EntityPlayer.class);
            when(player.getCapability(DietCapability.TYPE, null)).thenReturn(diet);
            net.minecraft.potion.Potion potion = net.minecraft.init.MobEffects.SPEED;
            net.minecraft.potion.PotionEffect external = new net.minecraft.potion.PotionEffect(potion, 600, 1);
            java.util.Map<net.minecraft.potion.Potion, net.minecraft.potion.PotionEffect> active = new java.util.HashMap<>();
            active.put(potion, external);
            when(player.getActivePotionEffect(potion)).thenAnswer(call -> active.get(potion));
            doAnswer(call -> { net.minecraft.potion.PotionEffect effect = call.getArgument(0); active.put(effect.getPotion(), effect); return null; })
                .when(player).addPotionEffect(any(net.minecraft.potion.PotionEffect.class));
            doAnswer(call -> { active.remove(potion); return null; }).when(player).removePotionEffect(potion);
            DietRewards.refreshEffects(player); assertSame(external, active.get(potion));
            active.clear(); DietRewards.refreshEffects(player); assertNotNull(active.get(potion));
            DietRewards.onEffectAdded(new net.minecraftforge.event.entity.living.PotionEvent.PotionAddedEvent(player, active.get(potion), external));
            active.put(potion, external); diet.reset(false); DietRewards.refreshEffects(player); assertSame(external, active.get(potion));
            active.clear(); diet.eat("a", "plain", 5, 5, 32); DietRewards.refreshEffects(player);
            diet.reset(false); DietRewards.refreshEffects(player); assertNull(active.get(potion));
        } finally { DietCapability.TYPE = previousType; DietConfig.attributes = oldAttributes; DietConfig.effects = oldEffects; }
    }
    @Test public void runtimeMigrationRunsOnceAndKeepsTheActualLegacyHealthFloor() throws Exception {
        Capability<DietData> previousType = DietCapability.TYPE;
        DietCapability.TYPE = mock(Capability.class);
        java.lang.reflect.Field ready = FoodCatalog.class.getDeclaredField("ready"); ready.setAccessible(true);
        java.lang.reflect.Field values = FoodCatalog.class.getDeclaredField("coefficients"); values.setAccessible(true);
        boolean wasReady = ready.getBoolean(null);
        java.util.Map<String, Double> coefficients = (java.util.Map<String, Double>) values.get(null);
        java.util.Map<String, Double> previous = new java.util.HashMap<>(coefficients);
        try {
            FoodList legacy = readLegacyHistory();
            coefficients.put("minecraft:apple@0", 2.0); coefficients.put("minecraft:bread@0", 3.0); coefficients.put("minecraft:fish@1", 4.0);
            ready.setBoolean(null, true);
            DietData diet = new DietData(); EntityPlayer player = mock(EntityPlayer.class);
            when(player.getCapability(SOLCarrotAPI.foodCapability, null)).thenReturn(legacy);
            when(player.getCapability(DietCapability.TYPE, null)).thenReturn(diet);
            AttributeMap attributes = new AttributeMap();
            IAttributeInstance health = attributes.registerAttribute(SharedMonsterAttributes.MAX_HEALTH);
            health.applyModifier(new AttributeModifier(LEGACY_HEALTH_ID, "Health Gained from Trying New Foods", 8, 0));
            when(player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)).thenReturn(health);
            DietRuntime.initialize(player);
            assertTrue(diet.initialized); assertEquals(9, diet.cumulative(), 0); assertEquals(8, diet.legacyHealthFloor, 0);
            assertEquals(0, diet.mealCount()); assertEquals(0, diet.nextGain("minecraft:apple@0", "plain", 20), 0);
            coefficients.put("minecraft:apple@0", 99.0); DietRuntime.initialize(player);
            assertEquals(9, diet.cumulative(), 0);
        } finally {
            coefficients.clear(); coefficients.putAll(previous); ready.setBoolean(null, wasReady); DietCapability.TYPE = previousType;
        }
    }
}
