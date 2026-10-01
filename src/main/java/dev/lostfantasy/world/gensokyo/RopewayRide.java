package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.Balance;
import dev.lostfantasy.entity.EntityCablecar;
import java.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class RopewayRide {
    private final Map<UUID,Exit> exits=new HashMap<>();
    public static void board(EntityPlayer player,BlockPos pos) {
        if(!(player instanceof EntityPlayerMP) || !player.isEntityAlive() || player.isSpectator() || player.isRiding()
                || player.dimension!=Balance.gensokyoDimensionId || player.getDistanceSq(pos)>36)return;
        int end=pos.equals(RopewayPath.console(0))?0:pos.equals(RopewayPath.console(1))?1:-1;if(end<0)return;
        EntityCablecar car=new EntityCablecar(player.world);car.begin(player,end);
        if(player.world.spawnEntity(car)) {
            if(!player.startRiding(car,true))car.setDead();
            else player.sendStatusMessage(new TextComponentTranslation("message.lostfantasy.ropeway_depart"),true);
        }
    }
    public static void arrive(EntityCablecar car,int end) {
        for(Entity e:new ArrayList<>(car.getPassengers()))if(e instanceof EntityPlayerMP) {
            EntityPlayerMP p=(EntityPlayerMP)e;BlockPos safe=RealmPassage.findLanding(p.world,RopewayPath.landing(end));
            if(safe==null)return;car.controlledExit=true;p.dismountRidingEntity();
            if(p.getRidingEntity()==car) {car.controlledExit=false;return;}
            move(p,safe);car.setDead();
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void leave(EntityMountEvent event) {
        if(!event.isDismounting() || !(event.getEntityBeingMounted() instanceof EntityCablecar) || !(event.getEntityMounting() instanceof EntityPlayerMP))return;
        EntityCablecar car=(EntityCablecar)event.getEntityBeingMounted();EntityPlayerMP p=(EntityPlayerMP)event.getEntityMounting();
        if(car.controlledExit || car.loggingOut || !p.isEntityAlive() || p.world!=car.world)return;
        BlockPos safe=RealmPassage.findLanding(p.world,RopewayPath.landing(car.exitStation()));
        if(safe==null) {event.setCanceled(true);return;}
        // EntityLivingBase changes position after the mount event, so relocate after that work finishes.
        exits.put(p.getUniqueID(),new Exit(p.world,safe,p.getPositionVector()));
    }
    @SubscribeEvent public void tick(TickEvent.PlayerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP))return;
        EntityPlayerMP p=(EntityPlayerMP)event.player;Exit exit=exits.remove(p.getUniqueID());if(exit==null)return;
        if(p.isEntityAlive() && p.world==exit.world && !p.isRiding() && p.getPositionVector().squareDistanceTo(exit.from)<64)move(p,exit.at);
    }
    @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        exits.remove(event.player.getUniqueID());
        if(event.player.getRidingEntity() instanceof EntityCablecar)((EntityCablecar)event.player.getRidingEntity()).loggingOut=true;
        // Forge emits this before writePlayerData. Vanilla then saves/restores RootVehicle and removes the live mount.
    }
    private static void move(EntityPlayerMP p,BlockPos at) {
        p.connection.setPlayerLocation(at.getX()+.5,at.getY(),at.getZ()+.5,p.rotationYaw,p.rotationPitch);
        p.motionX=p.motionY=p.motionZ=0;p.fallDistance=0;
    }
    private static final class Exit {
        final World world;final BlockPos at;final Vec3d from;
        Exit(World world,BlockPos at,Vec3d from) {this.world=world;this.at=at;this.from=from;}
    }
}
