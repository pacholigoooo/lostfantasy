package dev.lostfantasy.entity;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** A peaceful ferryman. Boarding is decided by the server. */
public final class EntityKomachi extends EntityCreature {
    private boolean ferryman;
    private int orphanTicks;
    public EntityKomachi(World world) {super(world);setSize(.58f,1.81f);isImmuneToFire=true;enablePersistence();}
    @Override protected void applyEntityAttributes() {
        super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.2);
    }
    @Override protected void initEntityAI() {
        tasks.addTask(0,new EntityAISwimming(this));
        tasks.addTask(2,new EntityAIWatchClosest(this,EntityPlayer.class,7,.9f));
        tasks.addTask(3,new EntityAILookIdle(this));
    }
    public void anchor(BlockPos pos) {setHomePosAndDistance(pos,3);}
    public void ferryman() {ferryman=true;}
    @Override public float getEyeHeight() {return 1.63f;}
    @Override public boolean canBePushed() {return false;}
    @Override public boolean canBeLeashedTo(EntityPlayer player) {return false;}
    @Override protected boolean canDespawn() {return false;}
    @Override public boolean attackEntityFrom(DamageSource source,float amount) {return false;}
    @Override protected void collideWithEntity(net.minecraft.entity.Entity entity) {}
    @Override public double getYOffset() {return 0;}
    @Override public boolean processInteract(EntityPlayer player,EnumHand hand) {
        if (hand != EnumHand.MAIN_HAND || !player.isEntityAlive()
                || player.isSpectator() || getDistanceSq(player) > 36) return false;
        if (world.isRemote) return true;
        if (ferryman || getRidingEntity() instanceof EntityRiverFerry) {
            if (getRidingEntity() instanceof EntityRiverFerry) {
                return ((EntityRiverFerry)getRidingEntity()).processInitialInteract(player, hand);
            }
            for (EntityRiverFerry ferry : world.getEntitiesWithinAABB(EntityRiverFerry.class, getEntityBoundingBox().grow(24))) {
                if (ferry.available()) return ferry.processInitialInteract(player, hand);
            }
            return false;
        }
        return false;
    }
    @Override public void onLivingUpdate() {
        super.onLivingUpdate();
        if(!world.isRemote) {
            if(ferryman && !isRiding()) {if(++orphanTicks>80)setDead();} else orphanTicks=0;
        }
        if(getRidingEntity() instanceof EntityRiverFerry) {
            EntityRiverFerry ferry=(EntityRiverFerry)getRidingEntity();
            motionX=motionY=motionZ=0;rotationYaw=ferry.rotationYaw+(ferry.sailing()?0:90);
            renderYawOffset=rotationYaw;if(ferry.sailing())rotationYawHead=rotationYaw;
        }
    }
    @Override public void writeEntityToNBT(NBTTagCompound n) {super.writeEntityToNBT(n);n.setBoolean("Ferryman",ferryman);}
    @Override public void readEntityFromNBT(NBTTagCompound n) {super.readEntityFromNBT(n);ferryman=n.getBoolean("Ferryman");}
}
