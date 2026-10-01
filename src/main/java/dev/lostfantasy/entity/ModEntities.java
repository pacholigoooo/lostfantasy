package dev.lostfantasy.entity;
import dev.lostfantasy.LostFantasy;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
public final class ModEntities {
    private ModEntities() {}
    public static void register() {
        EntityRegistry.registerModEntity(new ResourceLocation(LostFantasy.ID,"cablecar"),EntityCablecar.class,"cablecar",11,LostFantasy.INSTANCE,128,3,false);
        EntityRegistry.registerModEntity(new ResourceLocation(LostFantasy.ID,"hisoutensoku"),EntityHisoutensoku.class,"hisoutensoku",10,LostFantasy.INSTANCE,256,20,false);
        EntityRegistry.registerModEntity(new ResourceLocation(LostFantasy.ID,"house_spirit"),EntityHouseSpirit.class,"house_spirit",9,LostFantasy.INSTANCE,48,3,false);
        EntityRegistry.registerModEntity(new ResourceLocation(LostFantasy.ID,"komachi"),EntityKomachi.class,"komachi",8,LostFantasy.INSTANCE,96,3,true,0xb91642,0x275b9b);
        EntityRegistry.registerModEntity(new ResourceLocation(LostFantasy.ID,"river_ferry"),EntityRiverFerry.class,"river_ferry",7,LostFantasy.INSTANCE,128,2,true);
        EntityRegistry.registerModEntity(new ResourceLocation(LostFantasy.ID,"power_orb"),EntityPowerOrb.class,"power_orb",1,LostFantasy.INSTANCE,64,5,true);
        EntityRegistry.registerModEntity(new ResourceLocation(LostFantasy.ID,"companion"),EntityCompanion.class,"companion",2,LostFantasy.INSTANCE,128,3,true);
        EntityRegistry.registerModEntity(new ResourceLocation(LostFantasy.ID,"ofuda_projectile"),EntityOfuda.class,"ofuda_projectile",3,LostFantasy.INSTANCE,80,1,true);
        EntityRegistry.registerModEntity(new ResourceLocation(LostFantasy.ID,"gap_rift"),EntityGapRift.class,"gap_rift",4,LostFantasy.INSTANCE,96,5,false);
        EntityRegistry.registerModEntity(new ResourceLocation(LostFantasy.ID,"echo_arrow"),EntityEchoArrow.class,"echo_arrow",5,LostFantasy.INSTANCE,80,2,true);
    }
}
