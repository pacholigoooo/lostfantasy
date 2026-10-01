package dev.lostfantasy.entity;

import dev.lostfantasy.world.gensokyo.RopewayPath;
import dev.lostfantasy.world.gensokyo.RopewayRide;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

/** A passenger trip saved with vanilla's RootVehicle data; it requires no chunk-loading ticket. */
public final class EntityCablecar extends Entity {
    private UUID owner;private int start,age,idle;
    public boolean controlledExit,loggingOut;
    private double targetX,targetY,targetZ;private int interpolation;private boolean hasPosition;
    public EntityCablecar(World world) {super(world);setSize(3.5f,2.7f);setNoGravity(true);noClip=true;isImmuneToFire=true;}
    public void begin(EntityPlayer player,int origin) {
        owner=player.getUniqueID();start=origin==0?0:1;age=0;place();
    }
    public int origin() {return start;}
    public int age() {return age;}
    public int exitStation() {return RopewayPath.nearest(start,age);}
    @Override protected void entityInit() {}
    @Override protected boolean canFitPassenger(Entity passenger) {return getPassengers().isEmpty() && passenger instanceof EntityPlayer && passenger.getUniqueID().equals(owner);}
    @Override public boolean shouldRiderSit() {return false;}
    @Override public void updatePassenger(Entity passenger) {
        if(isPassenger(passenger)) {passenger.setPosition(posX,posY+CablecarShape.STANDING_Y,posZ);passenger.fallDistance=0;}
    }
    @Override public void onUpdate() {
        super.onUpdate();prevPosX=posX;prevPosY=posY;prevPosZ=posZ;motionX=motionY=motionZ=0;
        if(world.isRemote) {
            if(interpolation>0) {setPosition(posX+(targetX-posX)/interpolation,posY+(targetY-posY)/interpolation,posZ+(targetZ-posZ)/interpolation);interpolation--;}
            return;
        }
        if(getPassengers().isEmpty()) {if(++idle>100)setDead();return;}idle=0;
        if(age>=RopewayPath.TRAVEL_TICKS) {
            if(ticksExisted%20==0)RopewayRide.arrive(this,1-start);return;
        }
        Vec3d next=RopewayPath.ride(start,age+1);AxisAlignedBB body=new AxisAlignedBB(next.x-2.15,next.y,next.z-2.65,next.x+2.15,next.y+3,next.z+2.65);
        if(!world.isAreaLoaded(new BlockPos(body.minX,body.minY,body.minZ),new BlockPos(body.maxX,body.maxY,body.maxZ),false))return;
        if(!world.getCollisionBoxes(null,body).isEmpty())return;
        age++;place();
    }
    private void place() {Vec3d p=RopewayPath.ride(start,age);setPosition(p.x,p.y,p.z);rotationYaw=RopewayPath.yaw(start);prevRotationYaw=rotationYaw;}
    @Override public void setPositionAndRotationDirect(double x,double y,double z,float yaw,float pitch,int steps,boolean teleport) {
        rotationYaw=prevRotationYaw=yaw;
        if(!hasPosition) {setPosition(x,y,z);hasPosition=true;return;}
        targetX=x;targetY=y;targetZ=z;interpolation=Math.max(1,Math.min(5,steps));
    }
    @Override public AxisAlignedBB getRenderBoundingBox() {return new AxisAlignedBB(posX-3,posY,posZ-3,posX+3,posY+8.5,posZ+3);}
    @Override public boolean canBeCollidedWith() {return false;}
    @Override public boolean canBePushed() {return false;}
    @Override public void applyEntityCollision(Entity e) {}
    @Override public boolean attackEntityFrom(DamageSource source,float amount) {return false;}
    @Override protected void writeEntityToNBT(NBTTagCompound n) {
        if(owner!=null)n.setUniqueId("Owner",owner);n.setInteger("Origin",start);n.setInteger("TravelAge",age);
    }
    @Override protected void readEntityFromNBT(NBTTagCompound n) {
        owner=n.hasUniqueId("Owner")?n.getUniqueId("Owner"):null;start=n.getInteger("Origin")==0?0:1;
        age=Math.max(0,Math.min(RopewayPath.TRAVEL_TICKS,n.getInteger("TravelAge")));place();
    }
}
