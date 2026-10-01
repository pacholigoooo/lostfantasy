package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.Balance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

public final class GensokyoWorld {
    public static DimensionType TYPE;
    private GensokyoWorld() {}
    public static void register() {
        for(String name:new String[]{"liminal_fair","prismriver_scores","fairy_keepsakes","nazrin_salvage","signal_salvage","ghost_storehouse","boundary_books","boundary_collection","boundary_household","peony_tools","mayohiga_household","kasen_books","kasen_supplies","hermit_path","forest_alchemy","forest_books","doll_materials","kourindou_tools","night_stall",
                "bookbinding","school_supplies","hieda_records","village_pantry","eientei_herbs","eientei_clinic","lunar_archive","cucumber_supplies","kappa_tools","kappa_parts","tengu_printing","tengu_patrol","nemuno_tools","rainbow_mining","yamawaro_trade","yamawaro_tools","komakusa_supplies","concert_supplies","prismriver_rehearsal","ship_supplies","ship_gear","ship_records","castle_records","castle_household","castle_crafts"})
            net.minecraft.world.storage.loot.LootTableList.register(new net.minecraft.util.ResourceLocation("lostfantasy","chests/"+name));
        if(DimensionManager.isDimensionRegistered(Balance.gensokyoDimensionId))throw new IllegalStateException(
                "Lost Fantasy Gensokyo dimension ID "+Balance.gensokyoDimensionId+" is already used; change world.gensokyoDimensionId in config/lostfantasy.cfg");
        TYPE=DimensionType.register("lostfantasy_gensokyo","_gensokyo",Balance.gensokyoDimensionId,GensokyoProvider.class,false);
        DimensionManager.registerDimension(Balance.gensokyoDimensionId,TYPE);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new GensokyoTravel());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new RopewayRide());
        for(String name:new String[]{"old_hell_household","old_hell_pantry","old_hell_trade","palace_books","palace_household","palace_pet_care","hell_maintenance"})
            net.minecraft.world.storage.loot.LootTableList.register(new net.minecraft.util.ResourceLocation("lostfantasy","chests/"+name));
        net.minecraft.world.storage.loot.LootTableList.register(new net.minecraft.util.ResourceLocation("lostfantasy","chests/blood_pool_relics"));
        net.minecraft.world.storage.loot.LootTableList.register(new net.minecraft.util.ResourceLocation("lostfantasy","chests/rice_farming"));
        KasenWorld.register();BoundaryWorld.register();OldHellWorld.register();SenkaiWorld.register();NetherworldWorld.register();BloodPoolWorld.register();
    }
    public static BlockPos arrival() {
        GensokyoAtlas shrine=GensokyoAtlas.HAKUREI;
        return new BlockPos(shrine.x,shrine.y+1,shrine.z+61);
    }
}
