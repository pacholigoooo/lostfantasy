package dev.lostfantasy.entity;

import dev.lostfantasy.core.RiverJourney;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.world.HiganTerrain;
import dev.lostfantasy.world.HiganWorld;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public final class EntityRiverFerry extends Entity {
    // Positive offsets point toward the bow; leave room behind the lantern for the ferryman.
    public static final double FERRYMAN_OFFSET=.90, PASSENGER_OFFSET=-1.12;
    public static final double FERRYMAN_HEIGHT=.19;
    private static final net.minecraft.network.datasync.DataParameter<Boolean> SAILING=net.minecraft.network.datasync.EntityDataManager.createKey(EntityRiverFerry.class,net.minecraft.network.datasync.DataSerializers.BOOLEAN);
    private UUID owner;
    private double targetX,targetY,targetZ;private float targetYaw;private int lerp;
    private final FerryHeading heading=new FerryHeading();
    private boolean receivedPosition;
    public EntityRiverFerry(World world) { super(world);setSize(2.2f,.7f);isImmuneToFire=true; }
    public EntityRiverFerry(World world,UUID owner) {this(world);this.owner=owner;}
    public boolean belongsTo(EntityPlayer p) {return owner!=null && owner.equals(p.getUniqueID());}
    public boolean available() {return owner==null && getPassengers().stream().noneMatch(e->e instanceof EntityPlayer);}
    public boolean sailing() {return dataManager.get(SAILING);}
    @Override protected void entityInit() {dataManager.register(SAILING,false);}
    public void dock(boolean farBank) {
        owner=null;dataManager.set(SAILING,false);
        setPosition(HiganTerrain.boatX(0,farBank),HiganTerrain.WATER+.88,HiganTerrain.boatZ(0,farBank));
        // Keep only one waiting vessel when independent voyages reach the same pier.
        for(EntityRiverFerry other:world.getEntitiesWithinAABB(EntityRiverFerry.class,getEntityBoundingBox().grow(12))) {
            if(other!=this && !other.isDead && other.available()) {setDead();return;}
        }
    }
    @Override public boolean canBeCollidedWith() {return !isDead;}
    @Override public boolean canBePushed() {return false;}
    @Override protected boolean canFitPassenger(Entity e) {
        if(e instanceof EntityKomachi)return getPassengers().stream().noneMatch(p->p instanceof EntityKomachi);
        return e instanceof EntityPlayer && belongsTo((EntityPlayer)e) && getPassengers().stream().noneMatch(p->p instanceof EntityPlayer);
    }
    public void ensureFerryman() {
        if(world.isRemote || isDead || getPassengers().stream().anyMatch(e->e instanceof EntityKomachi))return;
        EntityKomachi pilot=new EntityKomachi(world);pilot.ferryman();pilot.setLocationAndAngles(posX,posY+.15,posZ,rotationYaw,0);
        if(world.spawnEntity(pilot) && !pilot.startRiding(this,true))pilot.setDead();
    }
    @Override public void updatePassenger(Entity passenger) {
        if(!isPassenger(passenger))return;
        double along=passenger instanceof EntityKomachi?FERRYMAN_OFFSET:PASSENGER_OFFSET;
        double angle=Math.toRadians(rotationYaw);
        passenger.setPosition(posX-Math.sin(angle)*along,posY+(passenger instanceof EntityKomachi?FERRYMAN_HEIGHT:getMountedYOffset()+passenger.getYOffset()),posZ+Math.cos(angle)*along);
        if(passenger instanceof EntityPlayer) {
            // Keep the seated body over the bench while the passenger can look around freely.
            EntityPlayer player=(EntityPlayer)passenger;
            player.renderYawOffset=rotationYaw;player.prevRenderYawOffset=prevRotationYaw;
        }
    }
    @Override public double getMountedYOffset() {return .21;}
    @Override public boolean attackEntityFrom(DamageSource source,float amount) {return false;}
    @Override public boolean processInitialInteract(EntityPlayer player,EnumHand hand) {
        if(hand!=EnumHand.MAIN_HAND)return false;
        if(world.isRemote)return true;
        if((owner!=null && !belongsTo(player)) || !HiganWorld.inside(player) || !player.isEntityAlive() || player.isSpectator() || getDistanceSq(player)>36)return false;
        RiverJourney j=PlayerData.get(player).journey;
        if(j.phase()!=RiverJourney.Phase.SHORE)return false;
        owner=player.getUniqueID();
        if(player.startRiding(this,true)) {
            ensureFerryman();
            j.board(HiganTerrain.farSide(posX,posZ));dataManager.set(SAILING,true);
            dev.lostfantasy.network.Network.sync((EntityPlayerMP)player);
        } else owner=null;
        return true;
    }
    @Override public void onUpdate() {
        super.onUpdate();prevPosX=posX;prevPosY=posY;prevPosZ=posZ;prevRotationYaw=rotationYaw;
        if(world.isRemote) {
            if(lerp>0) {
                setPosition(posX+(targetX-posX)/lerp,posY+(targetY-posY)/lerp,posZ+(targetZ-posZ)/lerp);lerp--;
            }
            if(receivedPosition)rotationYaw=heading.turn(rotationYaw,targetYaw);
            return;
        }
        if(ticksExisted%20==0)ensureFerryman();
        if(owner==null) {
            rotationYaw=heading.turn(rotationYaw,HiganTerrain.boatYaw(0,HiganTerrain.farSide(posX,posZ)));
            return;
        }
        EntityPlayerMP p=world.getMinecraftServer().getPlayerList().getPlayerByUUID(owner);
        if(p==null || p.world!=world || !p.isEntityAlive()) {setDead();return;}
        RiverJourney j=PlayerData.get(p).journey;
        if(j.phase()!=RiverJourney.Phase.SAILING) {setDead();return;}
        Entity mount=p.getRidingEntity();
        if(mount instanceof EntityRiverFerry && mount!=this && ((EntityRiverFerry)mount).belongsTo(p)) {setDead();return;}
        dataManager.set(SAILING,true);
        double x=HiganTerrain.boatX(j.elapsed(),j.fromFarBank()),z=HiganTerrain.boatZ(j.elapsed(),j.fromFarBank());
        rotationYaw=heading.turn(rotationYaw,HiganTerrain.boatYaw(j.elapsed(),j.fromFarBank()));
        setPosition(x,HiganTerrain.WATER+.88+Math.sin(ticksExisted*.055)*.025,z);
    }
    @Override public void setPositionAndRotationDirect(double x,double y,double z,float yaw,float pitch,int steps,boolean teleport) {
        targetX=x;targetY=y;targetZ=z;targetYaw=yaw;lerp=Math.max(1,steps);
        receivedPosition=true;
        // Vanilla also marks periodic absolute tracking packets as teleports; interpolate those too.
    }
    @Override protected void readEntityFromNBT(NBTTagCompound n) {owner=n.hasUniqueId("owner")?n.getUniqueId("owner"):null;}
    @Override protected void writeEntityToNBT(NBTTagCompound n) {if(owner!=null)n.setUniqueId("owner",owner);}
    @Override public void setDead() {
        for(Entity passenger:new java.util.ArrayList<>(getPassengers()))if(passenger instanceof EntityKomachi)passenger.setDead();
        super.setDead();
    }
}
