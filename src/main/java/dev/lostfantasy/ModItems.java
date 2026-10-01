package dev.lostfantasy;

import dev.lostfantasy.item.FantasyItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import java.util.*;

@Mod.EventBusSubscriber(modid=LostFantasy.ID)
public final class ModItems {
    public static final Map<String,Item> ALL=new LinkedHashMap<>();
    public static final CreativeTabs TAB=new CreativeTabs(LostFantasy.ID) {
        @Override public ItemStack createIcon() {return new ItemStack(GAP_KEY);}
    };
    public static final Item BEGINNER=add("beginner_grimoire"),INTERMEDIATE=add("intermediate_grimoire"),ADVANCED=add("advanced_grimoire"),MANUSCRIPT=add("grimoire_manuscript");
    public static final Item YOUKAI_POTION=add("youkai_potion"),TEA=add("suspicious_tea"),PLASMA=add("plasma"),DRAGON_ESSENCE=add("dragon_essence"),DRAGON_BLOOD=add("dragon_blood");
    public static final Item GAP_FRAGMENT=add("gap_fragment"),GAP_KEY=add("gap_key"),GUNGNIR_STUDY=add("gungnir_study"),FOUR_STUDY=add("four_study");
    public static final Item OFUDA=add("ofuda"),GOHEI=add("gohei"),YIN_YANG=add("yin_yang_orb"),FURNACE=add("mini_hakkero"),GUIDE=add("fantasy_guide");
    private ModItems() {}
    public static final Item RESEARCH_COPY=add("research_copy");
    public static final Item HAKUREI_CHARM=add("hakurei_charm",new dev.lostfantasy.item.HakureiCharm());
    public static final Item HIGAN_LILY=river("higan_lily",false),MISFORTUNE_RIBBON=river("misfortune_ribbon",true);
    public static final Item CUCUMBER=add("cucumber",new ItemFood(2,.2f,false));
    public static final Item DRAGON_GEM=add("dragon_gem",new Item());
    public static final Item CUCUMBER_SEEDS=add("cucumber_seeds",new ItemSeeds(ModBlocks.CUCUMBER_CROP,net.minecraft.init.Blocks.FARMLAND));
    public static final Item RICE_GRAIN=add("rice_grain",new ItemSeeds(ModBlocks.RICE_CROP,net.minecraft.init.Blocks.FARMLAND));
    public static final Item COOKED_RICE=add("cooked_rice",new ItemFood(3,.4f,false));
    private static Item add(String id,Item item) {
        item.setRegistryName(LostFantasy.ID,id).setTranslationKey(LostFantasy.ID+"."+id).setCreativeTab(TAB);
        ALL.put(id,item);return item;
    }
    private static Item river(String id,boolean ribbon) {
        Item item=new dev.lostfantasy.item.HiganItem(ribbon).setRegistryName(LostFantasy.ID,id).setTranslationKey(LostFantasy.ID+"."+id).setCreativeTab(TAB);
        ALL.put(id,item);return item;
    }
    private static Item add(String id) {
        Item item=new FantasyItem(id).setRegistryName(LostFantasy.ID,id).setTranslationKey(LostFantasy.ID+"."+id).setCreativeTab(TAB);
        ALL.put(id,item);return item;
    }
    @SubscribeEvent public static void register(RegistryEvent.Register<Item> event) {for(Item i:ALL.values())event.getRegistry().register(i);}
    public static void initialize() {
        net.minecraftforge.fml.common.registry.GameRegistry.addSmelting(new ItemStack(RICE_GRAIN),new ItemStack(COOKED_RICE),.1f);
        BrewingRecipeRegistry.addRecipe(new ItemStack(Items.DRAGON_BREATH),new ItemStack(Items.BLAZE_POWDER),new ItemStack(DRAGON_ESSENCE));
        BrewingRecipeRegistry.addRecipe(new ItemStack(DRAGON_ESSENCE),new ItemStack(Items.SKULL,1,5),new ItemStack(DRAGON_BLOOD));
        net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession librarian=ForgeRegistries.VILLAGER_PROFESSIONS.getValue(new ResourceLocation("minecraft:librarian"));
        if(librarian!=null) {
            Item[] books={BEGINNER,INTERMEDIATE,ADVANCED};int[] emeralds={4,12,24};
            for(int i=0;i<books.length;i++) {
                final Item book=books[i];final int cost=emeralds[i];
                librarian.getCareer(0).addTrade(i+1,(villager,recipes,random)->recipes.add(new MerchantRecipe(new ItemStack(Items.EMERALD,cost),new ItemStack(book))));
            }
        }
    }
}
