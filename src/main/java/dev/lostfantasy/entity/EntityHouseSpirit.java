package dev.lostfantasy.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** A few peaceful house spirits, saved as normal entities and free to pass through walls. */
public final class EntityHouseSpirit extends Entity {
    private double homeX,homeY,homeZ,phase;private long age;private boolean anchored;
    private double targetX,targetY,targetZ;private int lerpTicks;
    public EntityHouseSpirit(World world) {super(world);setSize(.75f,1.5f);noClip=true;isImmuneToFire=true;setNoGravity(true);}
    @Override protected void entityInit() {}
    public void anchor(BlockPos at,double phase) {
        homeX=at.getX()+.5;homeY=at.getY();homeZ=at.getZ()+.5;this.phase=phase;anchored=true;
        setPosition(homeX,homeY,homeZ);
    }
    @Override public void onUpdate() {
        super.onUpdate();
        if(!world.isRemote)drift();
        else if(lerpTicks>0) {
            setPosition(posX+(targetX-posX)/lerpTicks,posY+(targetY-posY)/lerpTicks,posZ+(targetZ-posZ)/lerpTicks);lerpTicks--;
        }
    }
    void drift() {
        if(!anchored) {homeX=posX;homeY=posY;homeZ=posZ;anchored=true;}
        age=(age+1)%36000;double angle=age*Math.PI/600+phase;
        Vec3d delta=new Vec3d(homeX+Math.cos(angle)*3-posX,homeY+Math.sin(angle*2)*.55-posY,homeZ+Math.sin(angle)*2.4-posZ);
        if(delta.lengthSquared()>.045*.045)delta=delta.normalize().scale(.045);
        motionX=delta.x;motionY=delta.y;motionZ=delta.z;setPosition(posX+motionX,posY+motionY,posZ+motionZ);
    }
    @Override public void setPositionAndRotationDirect(double x,double y,double z,float yaw,float pitch,int steps,boolean teleport) {
        if(teleport) {setPosition(x,y,z);lerpTicks=0;return;}
        targetX=x;targetY=y;targetZ=z;lerpTicks=Math.max(1,Math.min(5,steps));
    }
    @Override public boolean isInRangeToRenderDist(double distance) {return distance<48*48;}
    @Override public boolean canBeCollidedWith() {return false;}
    @Override public boolean canBePushed() {return false;}
    @Override public void applyEntityCollision(Entity entity) {}
    @Override public boolean attackEntityFrom(DamageSource source,float amount) {return false;}
    @Override protected void writeEntityToNBT(NBTTagCompound n) {
        n.setBoolean("Anchored",anchored);n.setDouble("HomeX",homeX);n.setDouble("HomeY",homeY);n.setDouble("HomeZ",homeZ);n.setDouble("Phase",phase);n.setLong("DriftAge",age);
    }
    @Override protected void readEntityFromNBT(NBTTagCompound n) {
        anchored=n.getBoolean("Anchored");homeX=n.getDouble("HomeX");homeY=n.getDouble("HomeY");homeZ=n.getDouble("HomeZ");phase=n.getDouble("Phase");age=Math.floorMod(n.getLong("DriftAge"),36000);
    }
}
