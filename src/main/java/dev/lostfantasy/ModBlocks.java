package dev.lostfantasy;

import dev.lostfantasy.block.RuinedBookcase;
import dev.lostfantasy.block.LibraryDecoration;
import dev.lostfantasy.block.CeilingFurniture;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.LinkedHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = LostFantasy.ID)
public final class ModBlocks {
    public static final Map<String, Block> ALL = new LinkedHashMap<>();
    public static final CreativeTabs TAB = new CreativeTabs(LostFantasy.ID + ".ruins") {
        @Override public ItemStack createIcon() { return new ItemStack(RUINED_BOOKCASE); }
    };
    public static final Block RUINED_BOOKCASE = add("ruined_bookcase", new RuinedBookcase());
    public static final Block ARCHIVE_CATALOG = add("archive_catalog", new LibraryDecoration(LibraryDecoration.Kind.CATALOG));
    public static final Block LIBRARY_LAMP = add("library_lamp", new LibraryDecoration(LibraryDecoration.Kind.LAMP));
    public static final Block HANGING_LAMP = add("hanging_lamp", new LibraryDecoration(LibraryDecoration.Kind.HANGING_LAMP));
    public static final Block SAISEN_BOX = add("saisen_box", new LibraryDecoration(LibraryDecoration.Kind.SAISEN_BOX));
    public static final Block TEA_TABLE = add("tea_table", new LibraryDecoration(LibraryDecoration.Kind.TEA_TABLE));
    public static final Block FLOOR_CUSHION = add("floor_cushion", new LibraryDecoration(LibraryDecoration.Kind.FLOOR_CUSHION));
    public static final Block DRAWER_CABINET = add("drawer_cabinet", new LibraryDecoration(LibraryDecoration.Kind.DRAWER_CABINET));
    public static final Block KITCHEN_SHELF = add("kitchen_shelf", new LibraryDecoration(LibraryDecoration.Kind.KITCHEN_SHELF));
    public static final Block WASHSTAND = add("washstand", new LibraryDecoration(LibraryDecoration.Kind.WASHSTAND));
    public static final Block UPHOLSTERED_CHAIR = add("upholstered_chair", new LibraryDecoration(LibraryDecoration.Kind.UPHOLSTERED_CHAIR));
    public static final Block RESEARCH_NOTES = add("research_notes", new LibraryDecoration(LibraryDecoration.Kind.NOTES));
    public static final Block BARRIER_STUDY = add("barrier_study", new dev.lostfantasy.block.BarrierStudyBlock());
    public static final Block EMERALD_STUDY = add("emerald_study", new dev.lostfantasy.block.EmeraldStudyBlock());
    public static final Block ARMILLARY = add("armillary", new LibraryDecoration(LibraryDecoration.Kind.ARMILLARY));
    public static final Block DOLL_DISPLAY = add("doll_display", new LibraryDecoration(LibraryDecoration.Kind.DOLL));
    public static final Block RED_LANTERN = add("red_lantern", new LibraryDecoration(LibraryDecoration.Kind.RED_LANTERN));
    public static final Block OUTSIDE_TELEVISION = add("outside_television", new LibraryDecoration(LibraryDecoration.Kind.TELEVISION));
    public static final Block WRITING_DESK = add("writing_desk", new LibraryDecoration(LibraryDecoration.Kind.WRITING_DESK));
    public static final Block GRAMOPHONE = add("gramophone", new LibraryDecoration(LibraryDecoration.Kind.GRAMOPHONE));
    public static final Block MAPLE_LEAVES = add("maple_leaves", new dev.lostfantasy.block.GardenLeaves());
    public static final Block ORANGE_MAPLE_LEAVES = add("orange_maple_leaves", new dev.lostfantasy.block.GardenLeaves());
    public static final Block GOLDEN_MAPLE_LEAVES = add("golden_maple_leaves", new dev.lostfantasy.block.GardenLeaves());
    public static final Block AUTUMN_LEAVES = add("autumn_leaves", new dev.lostfantasy.block.FallenPetals());
    public static final Block CLOVER = add("clover", new dev.lostfantasy.block.Clover());
    public static final Block WOODLAND_BONFIRE = add("woodland_bonfire", new dev.lostfantasy.block.WoodlandBonfire());
    public static final Block CHERRY_LEAVES = add("cherry_leaves", new dev.lostfantasy.block.GardenLeaves());
    public static final Block PURPLE_CHERRY_LEAVES = add("purple_cherry_leaves", new dev.lostfantasy.block.GardenLeaves());
    public static final Block FALLEN_PETALS = add("fallen_petals", new dev.lostfantasy.block.FallenPetals());
    public static final Block JIZO = add("jizo", new LibraryDecoration(LibraryDecoration.Kind.JIZO));
    public static final Block BAMBOO_STEM = add("bamboo_stem", new dev.lostfantasy.block.BambooStem());
    public static final Block BAMBOO_FOLIAGE = add("bamboo_foliage", new dev.lostfantasy.block.BambooFoliage());
    public static final Block PHARMACY_CABINET = add("pharmacy_cabinet", new LibraryDecoration(LibraryDecoration.Kind.CATALOG));
    public static final Block MEDICINE_TRAY = add("medicine_tray", new LibraryDecoration(LibraryDecoration.Kind.MEDICINE_TRAY));
    public static final Block SHRINE_ROPE = add("shrine_rope", new LibraryDecoration(LibraryDecoration.Kind.SHRINE_ROPE));
    public static final Block SHIDE = add("shide", new LibraryDecoration(LibraryDecoration.Kind.SHIDE));
    public static final Block CUCUMBER_CROP = add("cucumber_crop", new dev.lostfantasy.block.CucumberCrop());
    public static final Block RICE_CROP = add("rice_crop", new dev.lostfantasy.block.RiceCrop());
    public static final Block COLUMNAR_BASALT = add("columnar_basalt", new Block(net.minecraft.block.material.Material.ROCK).setHardness(2).setResistance(10));
    public static final Block MOSSY_BASALT = add("mossy_basalt", new Block(net.minecraft.block.material.Material.ROCK).setHardness(2).setResistance(10));
    public static final Block BLUE_ROOF_TILE = add("blue_roof_tile", new Block(net.minecraft.block.material.Material.ROCK).setHardness(2).setResistance(10));
    public static final Block BLUE_ROOF_STAIRS = add("blue_roof_stairs", new dev.lostfantasy.block.BlueRoofStairs(BLUE_ROOF_TILE.getDefaultState()));
    public static final Block KAPPA_PIPE = add("kappa_pipe",new dev.lostfantasy.block.KappaPipe());
    public static final Block ROPEWAY_CABLE = add("ropeway_cable",new dev.lostfantasy.block.RopewayCable());
    public static final Block ROPEWAY_STOP = add("ropeway_stop",new dev.lostfantasy.block.RopewayStop());
    public static final Block TENGU_PRESS = add("tengu_press",new LibraryDecoration(LibraryDecoration.Kind.PRESS));
    public static final Block TENGU_CAMERA = add("tengu_camera",new LibraryDecoration(LibraryDecoration.Kind.CAMERA));
    public static final Block THATCH = add("thatch",new dev.lostfantasy.block.ThatchBlock());
    public static final Block DRAGON_GEM_ORE = add("dragon_gem_ore",new dev.lostfantasy.block.DragonGemOre());
    public static final Block DRAGON_GEM_BLOCK = add("dragon_gem_block",new Block(net.minecraft.block.material.Material.ROCK).setHardness(3).setResistance(5).setLightLevel(5/15.0f));
    public static final Block KOMAKUSA = add("komakusa",new dev.lostfantasy.block.KomakusaFlower());
    public static final Block POND_LOTUS = add("pond_lotus",new dev.lostfantasy.block.PondLotus());
    public static final Block SUZURAN = add("suzuran",new dev.lostfantasy.block.SuzuranFlower());
    public static final Block MOUNTAIN_GAMING_TABLE = add("mountain_gaming_table",new dev.lostfantasy.block.MountainGamingTable());
    public static final Block DRAGON_PIPE = add("dragon_pipe",new LibraryDecoration(LibraryDecoration.Kind.DRAGON_PIPE));
    public static final Block LACQUER_BOWL = add("lacquer_bowl",new LibraryDecoration(LibraryDecoration.Kind.LACQUER_BOWL));
    public static final Block CEILING_CHEST = add("ceiling_chest",new CeilingFurniture(LibraryDecoration.Kind.STORAGE_CHEST,CeilingFurniture.Use.CHEST));
    public static final Block CEILING_WORKBENCH = add("ceiling_workbench",new CeilingFurniture(LibraryDecoration.Kind.CATALOG,CeilingFurniture.Use.WORKBENCH));
    public static final Block CEILING_BOOKSHELF = add("ceiling_bookshelf",new CeilingFurniture(LibraryDecoration.Kind.CATALOG,CeilingFurniture.Use.NONE));
    public static final Block CEILING_WRITING_DESK = add("ceiling_writing_desk",new CeilingFurniture(LibraryDecoration.Kind.WRITING_DESK,CeilingFurniture.Use.NONE));
    public static final Block CEILING_LANTERN = add("ceiling_lantern",new CeilingFurniture(LibraryDecoration.Kind.RED_LANTERN,CeilingFurniture.Use.NONE));
    public static final Block CEILING_LACQUER_BOWL = add("ceiling_lacquer_bowl",new CeilingFurniture(LibraryDecoration.Kind.LACQUER_BOWL,CeilingFurniture.Use.NONE));
    public static final Block CEILING_BED = add("ceiling_bed",new dev.lostfantasy.block.CeilingBed());
    public static final Block VIOLIN_STAND = add("violin_stand",new dev.lostfantasy.block.RehearsalInstrument(dev.lostfantasy.block.RehearsalInstrument.Voice.STRINGS));
    public static final Block TRUMPET_STAND = add("trumpet_stand",new dev.lostfantasy.block.RehearsalInstrument(dev.lostfantasy.block.RehearsalInstrument.Voice.BRASS));
    public static final Block REHEARSAL_KEYBOARD = add("rehearsal_keyboard",new dev.lostfantasy.block.RehearsalInstrument(dev.lostfantasy.block.RehearsalInstrument.Voice.KEYS));
    public static final Block HIGAN_SOIL=add("higan_soil",dev.lostfantasy.block.RiverBlocks.soil());
    public static final Block SANZU_WATER=add("sanzu_water",new dev.lostfantasy.block.RiverBlocks.Water());
    public static final Block CURSED_BLOOD=add("cursed_blood",new dev.lostfantasy.block.CursedBlood());
    public static final Block SPIDER_LILY=add("spider_lily",new dev.lostfantasy.block.RiverBlocks.Lily());
    public static final Block FLOATING_LILY=add("floating_lily",new dev.lostfantasy.block.RiverBlocks.Lily(true));
    public static final Block RIVER_LANTERN=add("river_lantern",new dev.lostfantasy.block.RiverBlocks.Lantern());

    private ModBlocks() {}

    private static Block add(String id, Block block) {
        block.setRegistryName(LostFantasy.ID, id).setTranslationKey(LostFantasy.ID + "." + id)
                .setCreativeTab(TAB);
        ALL.put(id, block);
        return block;
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        dev.lostfantasy.block.CeilingChestTile.register();
        for (Block block : ALL.values()) event.getRegistry().register(block);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        for (Block block : ALL.values()) {
            ItemBlock item = block==CEILING_BED?new dev.lostfantasy.item.CeilingBedItem(block):new ItemBlock(block) {
                @Override
                public int getMetadata(int damage) {
                    return damage;
                }

                @Override
                public String getTranslationKey(ItemStack stack) {
                    return block.getTranslationKey();
                }
            };
            item.setRegistryName(block.getRegistryName()).setHasSubtypes(true);
            event.getRegistry().register(item);
        }
    }
}
