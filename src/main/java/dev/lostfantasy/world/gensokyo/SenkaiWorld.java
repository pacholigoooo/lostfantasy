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

public final class SenkaiWorld {
    public static DimensionType TYPE;
    private SenkaiWorld() {}
    static void register() {
        if(DimensionManager.isDimensionRegistered(Balance.senkaiDimensionId))throw new IllegalStateException("Lost Fantasy Senkai dimension ID "+Balance.senkaiDimensionId+" is already used; change world.senkaiDimensionId in config/lostfantasy.cfg");
        TYPE=DimensionType.register("lostfantasy_senkai","_senkai",Balance.senkaiDimensionId,SenkaiProvider.class,false);
        DimensionManager.registerDimension(Balance.senkaiDimensionId,TYPE);MinecraftForge.EVENT_BUS.register(new SenkaiWorld());
    }
    static BlockPos local(int x,int y,int z) {return new BlockPos(GensokyoAtlas.MYOUREN.x+x,GensokyoAtlas.MYOUREN.y+y,GensokyoAtlas.MYOUREN.z+z);}
    public static BlockPos arrival() {return local(0,1,88);}
    static BlockPos returnPoint() {return local(0,-54,3);}
    static BlockPos gate(boolean inside) {return inside?local(0,1,99):local(0,-54,9);}
    static boolean doorway(boolean inside,double x,double y,double z) {
        BlockPos at=gate(inside);return Math.abs(x-at.getX()-.5)<2.5 && Math.abs(z-at.getZ()-.5)<1.5 && y>=at.getY()-.1 && y<at.getY()+1;
    }
    @SubscribeEvent public void tick(TickEvent.PlayerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP))return;
        EntityPlayerMP p=(EntityPlayerMP)event.player;boolean inside=p.dimension==Balance.senkaiDimensionId;
        if(!inside && p.dimension!=Balance.gensokyoDimensionId)return;
        if(!doorway(inside,p.posX,p.posY,p.posZ) || !p.onGround || p.timeUntilPortal>0 || !p.isEntityAlive()
                || p.isSpectator() || p.isRiding() || p.isBeingRidden() || PlayerData.get(p).trappedInGap || SpellManager.active(p) || SpellManager.locked(p))return;
        p.timeUntilPortal=20;
        RealmPassage.transfer(p,inside?Balance.gensokyoDimensionId:Balance.senkaiDimensionId,inside?returnPoint():arrival(),180);
    }
}
