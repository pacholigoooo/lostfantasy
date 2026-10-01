package dev.lostfantasy.entity;

import dev.lostfantasy.combat.Combat;
import dev.lostfantasy.combat.EchoCombat;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import java.util.UUID;

/** Visible flying paper; collision sweeps the full travel segment, including walls. */
public final class EntityOfuda extends Entity {
    private UUID owner;
    private float damage=4;
    private boolean spell;
    private int life=60;
    public EntityOfuda(World world) {super(world);setSize(.25f,.4f);setNoGravity(true);}
    public EntityOfuda(World world,EntityPlayer player,Vec3d from,Vec3d direction,float damage,boolean spell) {
        this(world);owner=player.getUniqueID();this.damage=damage;this.spell=spell;setPosition(from.x,from.y,from.z);
        Vec3d velocity=direction.normalize().scale(1.35);motionX=velocity.x;motionY=velocity.y;motionZ=velocity.z;
        rotationYaw=(float)Math.toDegrees(Math.atan2(velocity.x,velocity.z));rotationPitch=(float)-Math.toDegrees(Math.atan2(velocity.y,Math.sqrt(velocity.x*velocity.x+velocity.z*velocity.z)));
        prevRotationYaw=rotationYaw;prevRotationPitch=rotationPitch;
    }
    @Override protected void entityInit() {}
    @Override public void onUpdate() {
        super.onUpdate();if(world.isRemote)return;
        EntityPlayer player=owner==null?null:world.getPlayerEntityByUUID(owner);
        if(--life<=0||player==null||!player.isEntityAlive()) {setDead();return;}
        Vec3d start=getPositionVector(),end=start.add(motionX,motionY,motionZ);
        RayTraceResult wall=world.rayTraceBlocks(start,end,false,true,false);if(wall!=null)end=wall.hitVec;
        EntityLivingBase victim=null;double nearest=start.squareDistanceTo(end);
        for(EntityLivingBase e:world.getEntitiesWithinAABB(EntityLivingBase.class,new AxisAlignedBB(start,end).grow(.4),v->v!=null&&(spell?EchoCombat.canAttack(player,v):Combat.hostile(player,v)))) {
            AxisAlignedBB box=e.getEntityBoundingBox().grow(.15);RayTraceResult hit=box.calculateIntercept(start,end);
            double distance=box.contains(start)?0:hit==null?Double.POSITIVE_INFINITY:start.squareDistanceTo(hit.hitVec);
            if(distance<=nearest) {nearest=distance;victim=e;}
        }
        if(victim!=null) {
            if(spell)EchoCombat.damage(player,victim,damage);
            else victim.attackEntityFrom(new EntityDamageSource("lostfantasy.magic",player).setMagicDamage(),damage);
            setDead();return;
        }
        setPosition(end.x,end.y,end.z);if(wall!=null)setDead();
    }
    @Override protected void readEntityFromNBT(NBTTagCompound n) {if(n.hasUniqueId("owner"))owner=n.getUniqueId("owner");damage=dev.lostfantasy.core.Rules.clamp(n.getFloat("damage"),0,1000);spell=n.getBoolean("spell");life=Math.max(1,Math.min(60,n.getInteger("life")));}
    @Override protected void writeEntityToNBT(NBTTagCompound n) {if(owner!=null)n.setUniqueId("owner",owner);n.setFloat("damage",damage);n.setBoolean("spell",spell);n.setInteger("life",life);}
}
