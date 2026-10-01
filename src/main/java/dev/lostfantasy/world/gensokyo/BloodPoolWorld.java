package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.Balance;
import dev.lostfantasy.combat.SpellManager;
import dev.lostfantasy.data.PlayerData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** The deeper cavern is joined at the end of a descending, enclosed passage. */
public final class BloodPoolWorld {
    public static DimensionType TYPE;
    private BloodPoolWorld() {}
    static void register() {
        if(DimensionManager.isDimensionRegistered(Balance.bloodPoolDimensionId))throw new IllegalStateException("Lost Fantasy Blood Pool dimension ID "+Balance.bloodPoolDimensionId+" is already used; change world.bloodPoolDimensionId in config/lostfantasy.cfg");
        TYPE=DimensionType.register("lostfantasy_blood_pool","_blood_pool",Balance.bloodPoolDimensionId,BloodPoolProvider.class,false);
        DimensionManager.registerDimension(Balance.bloodPoolDimensionId,TYPE);MinecraftForge.EVENT_BUS.register(new BloodPoolWorld());
    }
    static BlockPos local(int x,int y,int z) {return OldHellWorld.local(x,y,z);}
    public static BlockPos arrival() {return local(0,111,-282);}
    static BlockPos returnPoint() {return local(0,-58,1800);}
    static BlockPos gate(boolean inside) {return inside?local(0,111,-294):local(0,-58,1813);}
    static boolean doorway(boolean inside,double x,double y,double z) {
        BlockPos p=gate(inside);return Math.abs(x-p.getX()-.5)<3.5 && Math.abs(z-p.getZ()-.5)<1.4 && y>=p.getY()-.1 && y<p.getY()+1;
    }
    @SubscribeEvent public void tick(TickEvent.PlayerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP))return;
        EntityPlayerMP p=(EntityPlayerMP)event.player;boolean inside=p.dimension==Balance.bloodPoolDimensionId;
        if(!inside && p.dimension!=Balance.oldHellDimensionId)return;
        if(!doorway(inside,p.posX,p.posY,p.posZ) || !p.onGround || p.timeUntilPortal>0 || !p.isEntityAlive() || p.isSpectator()
                || p.isRiding() || p.isBeingRidden() || PlayerData.get(p).trappedInGap || SpellManager.active(p) || SpellManager.locked(p))return;
        p.timeUntilPortal=20;
        RealmPassage.transfer(p,inside?Balance.oldHellDimensionId:Balance.bloodPoolDimensionId,inside?returnPoint():arrival(),inside?180:0);
    }
}
