package dev.lostfantasy.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

/** One fixed fairground balloon. Animation follows world time and never loads surrounding chunks. */
public final class EntityHisoutensoku extends Entity {
    public EntityHisoutensoku(World world) {super(world);setSize(1,1);noClip=true;isImmuneToFire=true;setNoGravity(true);}
    @Override protected void entityInit() {}
    @Override public void onUpdate() {super.onUpdate();motionX=motionY=motionZ=0;}
    @Override public AxisAlignedBB getRenderBoundingBox() {return new AxisAlignedBB(posX-52,posY-1,posZ-10,posX+52,posY+74,posZ+12);}
    @Override public boolean isInRangeToRenderDist(double distance) {return distance<256*256;}
    @Override public boolean canBeCollidedWith() {return false;}
    @Override public boolean canBePushed() {return false;}
    @Override public void applyEntityCollision(Entity entity) {}
    @Override public boolean attackEntityFrom(DamageSource source,float amount) {return false;}
    @Override protected void writeEntityToNBT(NBTTagCompound n) {}
    @Override protected void readEntityFromNBT(NBTTagCompound n) {}
}
