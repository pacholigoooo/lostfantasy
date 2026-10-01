package dev.lostfantasy.entity;

import dev.lostfantasy.world.GapWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/** A physical, short-lived threshold. Possession of a key is checked on crossing. */
public final class EntityGapRift extends Entity {
    private int remaining=600;
    public EntityGapRift(World world) {super(world);setSize(2.4f,3.4f);setNoGravity(true);}
    @Override protected void entityInit() {}
    @Override public void onUpdate() {
        super.onUpdate();if(world.isRemote)return;
        if(--remaining<=0) {setDead();return;}
        if(ticksExisted<6)return;
        double nx=-Math.sin(Math.toRadians(rotationYaw)),nz=Math.cos(Math.toRadians(rotationYaw));
        for(EntityPlayerMP p:world.getEntitiesWithinAABB(EntityPlayerMP.class,getEntityBoundingBox().grow(.2))) {
            double dx=p.posX-posX,dz=p.posZ-posZ;
            if(Math.abs(dx*nx+dz*nz)<.55&&Math.abs(dx*nz-dz*nx)<1.35)GapWorld.crossRift(p);
        }
    }
    @Override public boolean canBeCollidedWith() {return true;}
    @Override public boolean processInitialInteract(EntityPlayer player,EnumHand hand) {
        if(player instanceof EntityPlayerMP&&player.getDistanceSq(this)<=25)GapWorld.crossRift((EntityPlayerMP)player);
        return true;
    }
    @Override protected void readEntityFromNBT(NBTTagCompound n) {remaining=Math.max(1,Math.min(600,n.getInteger("remaining")));}
    @Override protected void writeEntityToNBT(NBTTagCompound n) {n.setInteger("remaining",remaining);}
}
