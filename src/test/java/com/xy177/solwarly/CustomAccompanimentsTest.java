package com.xy177.solwarly;
import com.google.gson.*;
import com.xy177.solwarly.spice.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.effect.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.Assert.*;
public class CustomAccompanimentsTest {
 @Rule public TemporaryFolder temp=new TemporaryFolder(new File("E:/BOX"));
 @BeforeClass public static void bootstrap(){net.minecraft.SharedConstants.tryDetectVersion();try(var loading=org.mockito.Mockito.mockStatic(net.neoforged.fml.loading.LoadingModList.class)){var mods=org.mockito.Mockito.mock(net.neoforged.fml.loading.LoadingModList.class);org.mockito.Mockito.when(mods.getModFiles()).thenReturn(List.of());loading.when(net.neoforged.fml.loading.LoadingModList::get).thenReturn(mods);net.minecraft.server.Bootstrap.bootStrap();}}
 @After public void restore()throws Exception{CustomSpices.load(temp.newFolder());CustomSpices.clearServerDefinitions();}
 private JsonObject example()throws Exception{try(var in=getClass().getResourceAsStream("/assets/solwarly/custom_accompaniments/_example.json")){var json=JsonParser.parseReader(new InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();json.addProperty("enabled",true);return json;}}
 private File folder()throws Exception{File dir=temp.newFolder();CustomSpices.load(dir);return dir;}
 private void write(File dir,String name,JsonObject json)throws Exception{Files.writeString(dir.toPath().resolve("custom_accompaniments/"+name),json.toString());}
 @Test public void exampleRemainsDisabledAndUserEditsAreKept()throws Exception{
  File dir=folder();Path path=dir.toPath().resolve("custom_accompaniments/_example.json");assertTrue(Files.exists(path));assertTrue(CustomSpices.all().isEmpty());
  Files.writeString(path,"user example");CustomSpices.load(dir);assertEquals("user example",Files.readString(path));
 }
 @Test public void definitionUsesNativeMaterialAndPerServingEffects()throws Exception{
  File dir=folder();write(dir,"carrot.json",example());CustomSpices.load(dir);
  CustomSpice side=CustomSpices.find("custom:carrot_side");assertNotNull(side);assertTrue(side.accompaniment());assertTrue(side.nativeMaterial());
  assertEquals(Items.CARROT,Seasoning.material(side).getItem());assertEquals(List.of(side),Seasoning.ingredient(new ItemStack(Items.CARROT)));assertTrue(Seasoning.components(new ItemStack(Items.CARROT)).isEmpty());
  assertEquals(2,side.hungerBonus);assertEquals(1.2,side.saturationBonus,0);assertEquals(600,side.effects(1).get(0).effect.getDuration());assertEquals(1200,side.effects(2).get(0).effect.getDuration());assertEquals(0,side.effects(2).get(0).effect.getAmplifier());
  assertTrue(Seasoning.valid(List.of(side,side)));assertFalse(Seasoning.valid(List.of(side,side,side)));assertFalse(Seasoning.valid(List.of(side,SpiceKind.SALT)));
  assertTrue(Seasoning.effectKinds(List.of(SpiceKind.CREATIVE)).contains(side));assertTrue(SpiceRecipes.all().isEmpty());
  var meal=Seasoning.apply(new ItemStack(Items.APPLE),List.of(side));assertTrue(Seasoning.ingredient(meal).isEmpty());
  assertEquals(1.2,Seasoning.flavor(meal).diversityMultiplier,0);
  assertTrue(Seasoning.variants(new ItemStack(Items.APPLE)).stream().anyMatch(v->Seasoning.components(v).equals(List.of(side,side))));
 }
 @Test public void conflictsAndInvalidFilesDoNotStopOtherDefinitions()throws Exception{
  File dir=folder();JsonObject json=example();write(dir,"a_valid.json",json);JsonObject duplicate=json.deepCopy();duplicate.addProperty("id","custom:duplicate");write(dir,"b_duplicate.json",duplicate);
  JsonObject bad=json.deepCopy();bad.addProperty("id","custom:bad");bad.addProperty("saturation",-1);write(dir,"c_bad.json",bad);
  JsonObject another=json.deepCopy();another.addProperty("id","custom:other");another.getAsJsonObject("material").addProperty("nbt","{Side:1}");another.add("return",JsonNull.INSTANCE);write(dir,"d_other.json",another);
  CustomSpices.load(dir);assertEquals(2,CustomSpices.all().size());assertNull(CustomSpices.find("custom:duplicate"));assertNull(CustomSpices.find("custom:bad"));
  var side=CustomSpices.find("custom:other");assertTrue(side.matches(side.material()));assertFalse(side.matches(new ItemStack(Items.CARROT)));assertTrue(side.returned().isEmpty());
 }
 @Test public void snapshotCarriesSideNutritionMaterialAndEffectsWithoutGrinding()throws Exception{
  File dir=folder();write(dir,"carrot.json",example());CustomSpices.load(dir);var snapshot=CustomSpices.snapshot();CustomSpices.load(temp.newFolder());CustomSpices.readSnapshot(snapshot);
  var field=CustomSpices.class.getDeclaredField("serverDefinitions");field.setAccessible(true);CustomSpice side=(CustomSpice)((Map<?,?>)field.get(null)).get("custom:carrot_side");
  assertTrue(side.accompaniment());assertEquals(2,side.hungerBonus);assertEquals(1200,side.effects(2).get(0).effect.getDuration());assertEquals(Items.CARROT,side.material().getItem());assertEquals("Carrot Accompaniment",side.text("en_us","name"));
  var recipes=CustomSpices.class.getDeclaredField("serverRecipes");recipes.setAccessible(true);assertTrue(((List<?>)recipes.get(null)).isEmpty());
 }
}
