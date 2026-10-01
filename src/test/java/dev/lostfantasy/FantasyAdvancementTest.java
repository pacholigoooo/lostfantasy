package dev.lostfantasy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class FantasyAdvancementTest {
    @BeforeClass public static void bootstrap() {
        Bootstrap.register();
        for (Item item : ModItems.ALL.values()) {
            if (!ForgeRegistries.ITEMS.containsKey(item.getRegistryName())) ForgeRegistries.ITEMS.register(item);
        }
        if (!ForgeRegistries.ITEMS.containsKey(ModBlocks.BARRIER_STUDY.getRegistryName()))
            ForgeRegistries.ITEMS.register(new net.minecraft.item.ItemBlock(ModBlocks.BARRIER_STUDY).setRegistryName(ModBlocks.BARRIER_STUDY.getRegistryName()));
        if (!ForgeRegistries.ITEMS.containsKey(ModBlocks.EMERALD_STUDY.getRegistryName()))
            ForgeRegistries.ITEMS.register(new net.minecraft.item.ItemBlock(ModBlocks.EMERALD_STUDY).setRegistryName(ModBlocks.EMERALD_STUDY.getRegistryName()));
    }

    private static JsonObject resource(String id) throws Exception {
        try (Reader reader = new InputStreamReader(FantasyAdvancementTest.class.getResourceAsStream(
                "/assets/lostfantasy/advancements/" + id + ".json"), StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }

    @Test public void native112ParserLoadsEveryAdvancementAndResolvesTheEntireTree() throws Exception {
        Map<ResourceLocation, Advancement.Builder> builders = new HashMap<>();
        for (FantasyAdvancement event : FantasyAdvancement.values()) {
            builders.put(event.id, AdvancementManager.GSON.fromJson(resource(event.id.getPath()), Advancement.Builder.class));
        }
        for (String id : new String[]{"root", "manuscript", "gap_fragment"}) {
            builders.put(new ResourceLocation(LostFantasy.ID, id), AdvancementManager.GSON.fromJson(resource(id), Advancement.Builder.class));
        }
        AdvancementList list = new AdvancementList();
        list.loadAdvancements(builders);
        int count = 0;
        for (Advancement ignored : list.getAdvancements()) count++;
        assertEquals(16, count);
        assertTrue(list.getRoots().iterator().hasNext());
        assertNull(list.getAdvancement(new ResourceLocation(LostFantasy.ID, "root")).getParent());
        for (FantasyAdvancement event : FantasyAdvancement.values()) {
            Advancement advancement = list.getAdvancement(event.id);
            assertNotNull(advancement);
            assertEquals(new ResourceLocation("minecraft:impossible"), advancement.getCriteria().get("event").getCriterionInstance().getId());
            AdvancementProgress progress = new AdvancementProgress();
            progress.update(advancement.getCriteria(), advancement.getRequirements());
            assertFalse(progress.isDone());
            assertFalse(progress.grantCriterion("wrong_event"));
            assertTrue(progress.grantCriterion("event"));
            assertTrue(progress.isDone());
            assertFalse(progress.grantCriterion("event"));
        }
    }

    @Test public void inventoryCriteriaMatchOnlyTheIntendedModItems() throws Exception {
        String[] ids = {"manuscript", "gap_fragment"};
        Item[] items = {ModItems.MANUSCRIPT, ModItems.GAP_FRAGMENT};
        for (int i = 0; i < ids.length; i++) {
            JsonObject criterion = resource(ids[i]).getAsJsonObject("criteria").getAsJsonObject("collected");
            assertEquals("minecraft:inventory_changed", criterion.get("trigger").getAsString());
            ItemPredicate predicate = ItemPredicate.deserializeArray(criterion.getAsJsonObject("conditions").get("items"))[0];
            assertTrue(predicate.test(new ItemStack(items[i])));
            assertFalse(predicate.test(new ItemStack(items[1 - i])));
            assertFalse(predicate.test(new ItemStack(Items.PAPER)));
            assertFalse(predicate.test(ItemStack.EMPTY));
        }
    }
}
