package dev.lostfantasy.entity;

import dev.lostfantasy.combat.EchoCombat;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import java.util.UUID;

public final class EntityEchoArrow extends EntityTippedArrow {
    private UUID owner;
    private int echoKnockback;
    public EntityEchoArrow(World world) {super(world);pickupStatus=PickupStatus.DISALLOWED;}
    public EntityEchoArrow(World world,EntityLivingBase echo,EntityPlayer owner) {super(world,echo);this.owner=owner.getUniqueID();pickupStatus=PickupStatus.DISALLOWED;}
    public void setEchoKnockback(int value) {echoKnockback=value;}
    @Override public void onUpdate() {super.onUpdate();if(!world.isRemote&&ticksExisted>100)setDead();}
    @Override protected Entity findEntityOnPath(Vec3d start,Vec3d end) {
        EntityPlayer player=owner==null?null:world.getPlayerEntityByUUID(owner);
        if(player==null || !player.isEntityAlive())return null;
        Entity closest=null;double distance=start.squareDistanceTo(end);
        for(EntityLivingBase target:world.getEntitiesWithinAABB(EntityLivingBase.class,new AxisAlignedBB(start,end).grow(1),e->e!=null&&e.canBeCollidedWith()&&EchoCombat.canAttack(player,e))) {
            AxisAlignedBB box=target.getEntityBoundingBox().grow(.3);RayTraceResult hit=box.calculateIntercept(start,end);
            double d=box.contains(start)?0:hit==null?Double.POSITIVE_INFINITY:start.squareDistanceTo(hit.hitVec);
            if(d<=distance){distance=d;closest=target;}
        }
        return closest;
    }
    @Override protected void onHit(RayTraceResult result) {
        if(world.isRemote)return;
        if(result.entityHit instanceof EntityLivingBase) {
            EntityPlayer player=owner==null?null:world.getPlayerEntityByUUID(owner);
            EntityLivingBase target=(EntityLivingBase)result.entityHit;
            if(player==null||!player.isEntityAlive()) {setDead();return;}
            if(!EchoCombat.canAttack(player,target))return;
            float damage=(float)Math.ceil(Math.sqrt(motionX*motionX+motionY*motionY+motionZ*motionZ)*getDamage());
            if(EchoCombat.damage(player,target,damage)) {
                if(isBurning())target.setFire(5);
                if(echoKnockback>0)target.knockBack(this,echoKnockback*.5f,-motionX,-motionZ);
            }setDead();
        }else {super.onHit(result);pickupStatus=PickupStatus.DISALLOWED;}
    }
    @Override public void writeEntityToNBT(NBTTagCompound n) {super.writeEntityToNBT(n);if(owner!=null)n.setUniqueId("echoOwner",owner);n.setInteger("echoKnockback",echoKnockback);}
    @Override public void readEntityFromNBT(NBTTagCompound n) {super.readEntityFromNBT(n);if(n.hasUniqueId("echoOwner"))owner=n.getUniqueId("echoOwner");echoKnockback=Math.max(0,Math.min(20,n.getInteger("echoKnockback")));pickupStatus=PickupStatus.DISALLOWED;}
}
