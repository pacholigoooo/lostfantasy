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

public final class NetherworldWorld {
    public static DimensionType TYPE;
    private NetherworldWorld() {}
    static void register() {
        if(DimensionManager.isDimensionRegistered(Balance.netherworldDimensionId))throw new IllegalStateException("Lost Fantasy Netherworld dimension ID "+Balance.netherworldDimensionId+" is already used; change world.netherworldDimensionId in config/lostfantasy.cfg");
        TYPE=DimensionType.register("lostfantasy_netherworld","_netherworld",Balance.netherworldDimensionId,NetherworldProvider.class,false);
        DimensionManager.registerDimension(Balance.netherworldDimensionId,TYPE);MinecraftForge.EVENT_BUS.register(new NetherworldWorld());
    }
    static BlockPos local(int x,int y,int z) {return new BlockPos(GensokyoAtlas.NETHER_GATE.x+x,GensokyoAtlas.NETHER_GATE.y+y,GensokyoAtlas.NETHER_GATE.z+z);}
    public static BlockPos arrival() {return local(0,-95,927);}
    static BlockPos returnPoint() {return local(0,1,20);}
    static BlockPos gate(boolean inside) {return inside?local(0,-95,937):local(0,1,0);}
    static boolean doorway(boolean inside,double x,double y,double z) {
        BlockPos at=gate(inside);return Math.abs(x-at.getX()-.5)<2.5 && Math.abs(z-at.getZ()-.5)<1.5 && y>=at.getY()-.1 && y<at.getY()+1;
    }
    @SubscribeEvent public void tick(TickEvent.PlayerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP))return;
        EntityPlayerMP p=(EntityPlayerMP)event.player;boolean inside=p.dimension==Balance.netherworldDimensionId;
        if(!inside && p.dimension!=Balance.gensokyoDimensionId)return;
        if(!doorway(inside,p.posX,p.posY,p.posZ) || !p.onGround || p.timeUntilPortal>0 || !p.isEntityAlive()
                || p.isSpectator() || p.isRiding() || p.isBeingRidden() || PlayerData.get(p).trappedInGap || SpellManager.active(p) || SpellManager.locked(p))return;
        p.timeUntilPortal=20;
        RealmPassage.transfer(p,inside?Balance.gensokyoDimensionId:Balance.netherworldDimensionId,inside?returnPoint():arrival(),180);
    }
}
