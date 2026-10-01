package dev.lostfantasy.entity;
import dev.lostfantasy.data.PlayerData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;
import java.util.UUID;
public final class EntityPowerOrb extends Entity {
    private static final DataParameter<Integer> AMOUNT=EntityDataManager.createKey(EntityPowerOrb.class,DataSerializers.VARINT);
    private UUID owner;
    public EntityPowerOrb(World world) {super(world);setSize(.3f,.3f);}
    public EntityPowerOrb(World world,EntityPlayer owner,int amount,double x,double y,double z) {this(world);this.owner=owner.getUniqueID();dataManager.set(AMOUNT,Math.max(1,Math.min(5,amount)));setPosition(x,y,z);motionY=.12;}
    @Override protected void entityInit() {dataManager.register(AMOUNT,1);}
    public int amount() {return dataManager.get(AMOUNT);}
    @Override public void onUpdate() {
        super.onUpdate();if(world.isRemote)return;
        if(ticksExisted>1200) {setDead();return;}
        EntityPlayer p=owner==null?null:world.getPlayerEntityByUUID(owner);
        if(p==null || !p.isEntityAlive()) {motionY=0;return;}
        PlayerData d=PlayerData.get(p);if(d.power()>=5) {setDead();return;}
        double dx=p.posX-posX,dy=p.posY+.7-posY,dz=p.posZ-posZ,length=Math.sqrt(dx*dx+dy*dy+dz*dz);
        if(length<1.2) {d.setPower(Math.min(5,d.power()+amount()));d.dirty=true;world.playSound(null,p.posX,p.posY,p.posZ,SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,SoundCategory.PLAYERS,.4f,1.4f);setDead();return;}
        if(length<16) {double speed=.12+Math.max(0,1-length/16)*.25;setPosition(posX+dx/length*speed,posY+dy/length*speed,posZ+dz/length*speed);}
    }
    @Override protected void readEntityFromNBT(NBTTagCompound n) {if(n.hasUniqueId("owner"))owner=n.getUniqueId("owner");dataManager.set(AMOUNT,Math.max(1,Math.min(5,n.getInteger("amount"))));ticksExisted=n.getInteger("age");}
    @Override protected void writeEntityToNBT(NBTTagCompound n) {if(owner!=null)n.setUniqueId("owner",owner);n.setInteger("amount",amount());n.setInteger("age",ticksExisted);}
}
