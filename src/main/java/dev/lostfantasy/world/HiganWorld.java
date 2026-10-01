package dev.lostfantasy.world;

import dev.lostfantasy.Balance;
import dev.lostfantasy.FlightController;
import dev.lostfantasy.combat.SpellManager;
import dev.lostfantasy.core.RiverJourney;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.entity.EntityRiverFerry;
import dev.lostfantasy.network.Network;
import dev.lostfantasy.world.gensokyo.GensokyoAtlas;
import dev.lostfantasy.world.gensokyo.SanzuCoast;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

public final class HiganWorld {
    private static final Set<Entity> AUTHORIZED=Collections.newSetFromMap(new IdentityHashMap<>());
    public static final BlockPos ARRIVAL=new BlockPos(GensokyoAtlas.SANZU_PIER.x+19,77,GensokyoAtlas.SANZU_PIER.z);
    private HiganWorld() {}
    public static boolean inside(Entity e) {return e!=null && e.world!=null && inside(e.dimension,e.posX,e.posZ);}
    public static boolean inside(int dimension,double x,double z) {return dimension==Balance.gensokyoDimensionId && SanzuCoast.region(x,z);}
    public static boolean authorized(Entity e) {return AUTHORIZED.contains(e);}
    public static void authorize(Entity e,Runnable action) {
        boolean added=AUTHORIZED.add(e);try {action.run();}finally {if(added)AUTHORIZED.remove(e);}
    }
    public static void register() {
        MinecraftForge.EVENT_BUS.register(new HiganRestrictions());
        net.minecraftforge.fml.common.registry.GameRegistry.registerWorldGenerator(new HiganFlowers(),25);
    }
    public static void enter(EntityPlayerMP player) {
        PlayerData d=PlayerData.get(player);
        if(inside(player) || !player.isEntityAlive() || player.isRiding()
                || d.trappedInGap || SpellManager.active(player) || SpellManager.locked(player))return;
        WorldServer world=player.getServer().getWorld(Balance.gensokyoDimensionId);if(world==null)return;
        boolean externalFlight=player.capabilities.allowFlying && !d.flightGranted && !player.isCreative() && !player.isSpectator();
        world.getChunk(ARRIVAL);
        if(!transfer(player,world,ARRIVAL,-90))return;
        d.journey.arrive();d.journey.suspendedFlight(externalFlight);d.dirty=true;Network.sync(player);
    }
    public static void tick(EntityPlayerMP player) {
        PlayerData d=PlayerData.get(player);RiverJourney j=d.journey;
        if(!inside(player)) {
            if(j.phase()!=RiverJourney.Phase.NONE) {j.leave();d.dirty=true;FlightController.update(player);Network.sync(player);}
            return;
        }
        if(j.phase()==RiverJourney.Phase.NONE) {j.arrive();Network.sync(player);}
        boolean sailing=j.phase()==RiverJourney.Phase.SAILING;
        if(!sailing && player.ticksExisted%20!=0)return;
        boolean fromFar=sailing?j.fromFarBank():HiganTerrain.farSide(player.posX,player.posZ);
        int elapsed=sailing?j.elapsed():0;
        double x=HiganTerrain.boatX(elapsed,fromFar),z=HiganTerrain.boatZ(elapsed,fromFar);
        if(!sailing && player.getDistanceSq(x,HiganTerrain.WATER,z)>64*64)return;
        WorldServer world=player.getServerWorld();world.getChunk(new BlockPos(x,HiganTerrain.WATER,z));
        EntityRiverFerry ferry=player.getRidingEntity() instanceof EntityRiverFerry?(EntityRiverFerry)player.getRidingEntity():null;
        if(ferry==null || ferry.isDead || !ferry.belongsTo(player)) {
            ferry=null;
            for(EntityRiverFerry candidate:world.getEntitiesWithinAABB(EntityRiverFerry.class,new AxisAlignedBB(x-16,HiganTerrain.WATER-6,z-16,x+16,HiganTerrain.WATER+12,z+16))) {
                if(!candidate.isDead && (sailing?candidate.belongsTo(player):candidate.available())) {ferry=candidate;break;}
            }
        }
        if(ferry==null) {
            ferry=sailing?new EntityRiverFerry(world,player.getUniqueID()):new EntityRiverFerry(world);
            ferry.setLocationAndAngles(x,HiganTerrain.WATER+.88,z,HiganTerrain.boatYaw(elapsed,fromFar),0);
            if(!world.spawnEntity(ferry))return;
            ferry.ensureFerryman();
        }
        if(!sailing)return;
        final EntityRiverFerry vessel=ferry;
        if(player.getRidingEntity()!=ferry)authorize(player,()->player.startRiding(vessel,true));
        if(j.advance(player.getRidingEntity()==ferry))arrive(player,ferry);
    }
    private static void arrive(EntityPlayerMP player,EntityRiverFerry ferry) {
        PlayerData d=PlayerData.get(player);RiverJourney j=d.journey;
        boolean farBank=!j.fromFarBank();BlockPos landing=HiganTerrain.landing(farBank);
        WorldServer world=player.getServerWorld();world.getChunk(landing);
        // Use the actual pier, including player edits, before letting the passenger off.
        BlockPos safe=GapWorld.findSafePosition(landing,p->p.distanceSq(landing)<=36 && GapWorld.safeLanding(world,p));
        if(safe==null)return;
        if(!j.finishCrossing())return;
        final BlockPos exit=safe;
        authorize(player,()->{
            player.dismountRidingEntity();
            player.connection.setPlayerLocation(exit.getX()+.5,exit.getY(),exit.getZ()+.5,player.rotationYaw,player.rotationPitch);
        });
        player.motionX=player.motionY=player.motionZ=0;player.fallDistance=0;
        ferry.dock(farBank);Network.sync(player);
    }
    private static boolean transfer(EntityPlayerMP player,WorldServer world,BlockPos pos,float yaw) {
        authorize(player,()->{
            player.dismountRidingEntity();
            if(player.world!=world)player.changeDimension(world.provider.getDimension(),new FlowerTeleporter(world,pos,yaw));
            if(player.world==world)player.connection.setPlayerLocation(pos.getX()+.5,pos.getY(),pos.getZ()+.5,yaw,0);
        });
        if(player.world!=world)return false;
        player.motionX=player.motionY=player.motionZ=0;player.fallDistance=0;player.timeUntilPortal=80;
        SpellManager.cancel(player);return true;
    }
    private static final class FlowerTeleporter extends Teleporter {
        final BlockPos pos;final float yaw;
        FlowerTeleporter(WorldServer world,BlockPos pos,float yaw) {super(world);this.pos=pos;this.yaw=yaw;}
        @Override public void placeInPortal(Entity e,float ignored) { e.setLocationAndAngles(pos.getX()+.5,pos.getY(),pos.getZ()+.5,yaw,0); }
        @Override public boolean placeInExistingPortal(Entity e,float ignored) {placeInPortal(e,ignored);return true;}
        @Override public boolean makePortal(Entity e) {return true;}
        @Override public void removeStalePortalLocations(long t) {}
    }
}
