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

/** The Yakumo residence, reached through the boundary gate in Gensokyo. */
public final class BoundaryWorld {
    public static DimensionType TYPE;
    private BoundaryWorld() {}
    static void register() {
        if(DimensionManager.isDimensionRegistered(Balance.boundaryDimensionId))throw new IllegalStateException(
                "Lost Fantasy boundary dimension ID "+Balance.boundaryDimensionId+" is already used; change world.boundaryDimensionId in config/lostfantasy.cfg");
        TYPE=DimensionType.register("lostfantasy_boundary","_boundary",Balance.boundaryDimensionId,BoundaryProvider.class,false);
        DimensionManager.registerDimension(Balance.boundaryDimensionId,TYPE);MinecraftForge.EVENT_BUS.register(new BoundaryWorld());
    }
    public static BlockPos arrival() {return local(0,1,50);}
    static BlockPos returnPoint() {return local(0,1,70);}
    static BlockPos local(int x,int y,int z) {GensokyoAtlas s=GensokyoAtlas.YAKUMO;return new BlockPos(s.x+x,s.y+y,s.z+z);}
    static boolean doorway(double x,double y,double z) {return Math.abs(x)<2.5 && y>.8 && y<2 && z>=60 && z<=62;}
    @SubscribeEvent public void tick(TickEvent.PlayerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP))return;
        EntityPlayerMP p=(EntityPlayerMP)event.player;
        boolean inside=p.dimension==Balance.boundaryDimensionId;
        if(!inside && p.dimension!=Balance.gensokyoDimensionId)return;
        GensokyoAtlas s=GensokyoAtlas.YAKUMO;
        if(!doorway(p.posX-s.x-.5,p.posY-s.y,p.posZ-s.z-.5) || !p.onGround || p.timeUntilPortal>0
                || !p.isEntityAlive() || p.isSpectator() || p.isRiding() || p.isBeingRidden()
                || PlayerData.get(p).trappedInGap || SpellManager.active(p) || SpellManager.locked(p))return;
        // A cancelled travel event is retried at most once per second while standing in the doorway.
        p.timeUntilPortal=20;
        if(RealmPassage.transfer(p,inside?Balance.gensokyoDimensionId:Balance.boundaryDimensionId,inside?returnPoint():arrival(),inside?0:180)
                && !inside)dev.lostfantasy.FantasyAdvancement.YAKUMO.grant(p);
    }
}
