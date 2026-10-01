package dev.lostfantasy.world;

import dev.lostfantasy.Balance;
import dev.lostfantasy.core.RiverJourney;
import dev.lostfantasy.data.PlayerData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class HiganRestrictions {
    public static boolean blocksTeleport(Entity e) {return HiganWorld.inside(e) && !HiganWorld.authorized(e);}
    public static boolean blocksTransfer(Entity e,int destination) {
        return HiganWorld.inside(e) && !HiganWorld.authorized(e);
    }
    public static boolean blocksLocation(NetHandlerPlayServer handler,double x,double y,double z) {
        EntityPlayerMP p=handler.player;
        // Acknowledgements and rotation-only corrections are not teleport abilities.
        return !HiganWorld.authorized(p) && (HiganWorld.inside(p) || HiganWorld.inside(p.dimension,x,z)) && p.getDistanceSq(x,y,z)>.000001;
    }
    /** Also injected immediately before movement, after vanilla spectator/creative updates. */
    public static void ground(EntityLivingBase entity) {
        if(!HiganWorld.inside(entity))return;
        if(entity.isPotionActive(MobEffects.LEVITATION))entity.removePotionEffect(MobEffects.LEVITATION);
        if(entity instanceof EntityPlayer) {
            EntityPlayer p=(EntityPlayer)entity;
            if(p instanceof EntityPlayerMP && p.capabilities.allowFlying && !p.isCreative() && !p.isSpectator()) {
                PlayerData d=PlayerData.get(p);if(!d.flightGranted)d.journey.suspendedFlight(true);
            }
            boolean changed=p.capabilities.allowFlying || p.capabilities.isFlying;
            p.capabilities.allowFlying=false;p.capabilities.isFlying=false;p.noClip=false;
            if(p instanceof EntityPlayerMP) {
                ((EntityPlayerMP)p).clearElytraFlying();
                if(changed)p.sendPlayerAbilities();
            }
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void tick(TickEvent.PlayerTickEvent event) {
        ground(event.player);
        if(event.phase==TickEvent.Phase.END && event.player instanceof EntityPlayerMP && event.player.isEntityAlive())
            HiganWorld.tick((EntityPlayerMP)event.player);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void teleport(EnderTeleportEvent event) {
        Entity e=event.getEntity();if(!HiganWorld.authorized(e) && (HiganWorld.inside(e) || HiganWorld.inside(e.dimension,event.getTargetX(),event.getTargetZ())))event.setCanceled(true);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void dimension(EntityTravelToDimensionEvent event) {if(blocksTransfer(event.getEntity(),event.getDimension()))event.setCanceled(true);}
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void dismount(EntityMountEvent event) {
        if(event.isDismounting() && event.getEntityMounting() instanceof EntityPlayer && !HiganWorld.authorized(event.getEntityMounting())) {
            EntityPlayer p=(EntityPlayer)event.getEntityMounting();
            if(!p.world.isRemote && HiganWorld.inside(p) && p.isEntityAlive() && PlayerData.get(p).journey.phase()==RiverJourney.Phase.SAILING)
                event.setCanceled(true);
        }
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void voyageDamage(LivingAttackEvent event) {
        if(event.getEntityLiving() instanceof EntityPlayer && !event.getEntityLiving().world.isRemote) {
            EntityPlayer p=(EntityPlayer)event.getEntityLiving();
            if(HiganWorld.inside(p) && PlayerData.get(p).journey.phase()==RiverJourney.Phase.SAILING
                    && event.getAmount()<Float.MAX_VALUE)event.setCanceled(true);
        }
    }
}
