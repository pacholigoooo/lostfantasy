package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.Balance;
import dev.lostfantasy.combat.SpellManager;
import dev.lostfantasy.data.PlayerData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Each physical exit returns to its own surface cave, regardless of the entrance used. */
public final class OldHellWorld {
    public static DimensionType TYPE;
    static final GensokyoAtlas ORIGIN=GensokyoAtlas.EARTH_RAINBOW;
    private OldHellWorld() {}
    static void register() {
        if(DimensionManager.isDimensionRegistered(Balance.oldHellDimensionId))throw new IllegalStateException(
                "Lost Fantasy Old Hell dimension ID "+Balance.oldHellDimensionId+" is already used; change world.oldHellDimensionId in config/lostfantasy.cfg");
        TYPE=DimensionType.register("lostfantasy_old_hell","_old_hell",Balance.oldHellDimensionId,OldHellProvider.class,false);
        DimensionManager.registerDimension(Balance.oldHellDimensionId,TYPE);MinecraftForge.EVENT_BUS.register(new OldHellWorld());
    }
    static BlockPos local(int x,int y,int z) {return new BlockPos(ORIGIN.x+x,ORIGIN.y+y,ORIGIN.z+z);}
    static BlockPos gate(int route) {return route==2?GeyserCenter.lowerGate():route==0?HellDeepRoad.gate():local(OldHellCity.EAST_GATE_X,1,80);}
    public static BlockPos arrival(int route) {return route==2?GeyserCenter.lowerArrival():route==0?HellDeepRoad.arrival():local(OldHellCity.EAST_ARRIVAL_X,1,80);}
    static boolean doorway(int route,boolean inside,double x,double y,double z) {
        BlockPos p=inside?gate(route):route==2?GeyserCenter.gate():UndergroundEntrances.gate(route);
        return Math.abs(y-p.getY())<.7 && (inside && route==1
                ? Math.abs(x-p.getX()-.5)<1.5 && Math.abs(z-p.getZ()-.5)<2.5
                : Math.abs(x-p.getX()-.5)<2.5 && Math.abs(z-p.getZ()-.5)<1.5);
    }
    @SubscribeEvent public void tick(TickEvent.PlayerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP))return;
        EntityPlayerMP p=(EntityPlayerMP)event.player;boolean inside=p.dimension==Balance.oldHellDimensionId;
        if(!inside && p.dimension!=Balance.gensokyoDimensionId)return;
        if(!p.onGround || p.timeUntilPortal>0 || !p.isEntityAlive() || p.isSpectator() || p.isRiding() || p.isBeingRidden())return;
        for(int route=0;route<3;route++)if(doorway(route,inside,p.posX,p.posY,p.posZ)) {
            if(PlayerData.get(p).trappedInGap || SpellManager.active(p) || SpellManager.locked(p))return;
            p.timeUntilPortal=20;
            RealmPassage.transfer(p,inside?Balance.gensokyoDimensionId:Balance.oldHellDimensionId,
                    inside?(route==2?GeyserCenter.arrival():UndergroundEntrances.returnPoint(route)):arrival(route),route==2?0:inside?180:route==0?0:90);
            return;
        }
    }
}
