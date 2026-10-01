package dev.lostfantasy.combat;

import dev.lostfantasy.Balance;
import dev.lostfantasy.core.EmeraldCity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class EmeraldCombat {
    public final List<EmeraldCity.Column> columns;
    private final EmeraldCity.Hits hits=new EmeraldCity.Hits();
    public EmeraldCombat(List<EmeraldCity.Column> columns) { this.columns=columns; }

    public void tick(EntityPlayer owner,int age,BooleanSupplier valid) {
        boolean sounded=false;
        for(int index=0;index<columns.size();index++) {
            EmeraldCity.Column column=columns.get(index);
            if(!valid.getAsBoolean()) return;
            if(age==column.start() && !sounded) {
                owner.world.playSound(null,column.x,column.y,column.z,SoundEvents.BLOCK_GLASS_PLACE,
                        SoundCategory.PLAYERS,.8f,.75f+column.round*.15f);
                sounded=true;
            }
            if(!column.rising(age)) continue;
            AxisAlignedBB area=column.bounds(age+1);
            // Newly placed ceilings or walls also stop damage, without modifying any block.
            double clearance=EmeraldGround.clearance(owner.world,column.x,column.y,column.z,EmeraldCity.RADIUS);
            area=new AxisAlignedBB(area.minX,area.minY,area.minZ,area.maxX,Math.min(area.maxY,column.y+clearance),area.maxZ);
            if(area.maxY<=area.minY) continue;
            for(EntityLivingBase target:owner.world.getEntitiesWithinAABB(EntityLivingBase.class,area,
                    entity->entity!=null && Combat.hostile(owner,entity))) {
                if(!valid.getAsBoolean()) return;
                if(!hits.attempt(index,target.getUniqueID())) continue;
                if(Combat.confirmedSpellDamageWithoutKnockback(owner,target,Balance.emeraldDamage)) {
                    if(!valid.getAsBoolean()) return;
                    hits.landed(target.getUniqueID());
                    if(target.world==owner.world) launch(target);
                }
            }
        }
        if(!valid.getAsBoolean() || columns.isEmpty()) return;
        AxisAlignedBB area=columns.get(0).bounds(age);
        for(EmeraldCity.Column column:columns) area=area.union(column.bounds(age));
        // Normal arrows move only a few blocks per tick. Impact events handle longer segments before vanilla damage.
        for(EntityArrow arrow:owner.world.getEntitiesWithinAABB(EntityArrow.class,area.grow(16),
                entity->entity!=null && supported(owner,entity))) {
            Vec3d from=new Vec3d(arrow.lastTickPosX,arrow.lastTickPosY,arrow.lastTickPosZ);
            intercept(owner,arrow,from,arrow.getPositionVector(),age);
        }
    }
    static void launch(EntityLivingBase target) {
        if(!target.isEntityAlive() || !target.isNonBoss()) return;
        double resistance=target.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue();
        double impulse=Balance.emeraldLift*Math.max(0,1-resistance);
        if(impulse<=0 || target.motionY>=impulse) return;
        target.motionY=impulse;
        target.velocityChanged=true;
        // Vanilla landing damage is left alone; no extra spell landing damage is scheduled.
    }
    static boolean supported(EntityPlayer owner,EntityArrow arrow) {
        return !arrow.isDead && arrow.world==owner.world
                && (arrow.getClass()==EntityTippedArrow.class || arrow.getClass()==EntitySpectralArrow.class)
                && arrow.shootingEntity instanceof EntityLivingBase
                && Combat.hostile(owner,(EntityLivingBase)arrow.shootingEntity);
    }
    public boolean intercept(EntityPlayer owner,EntityArrow arrow,Vec3d from,Vec3d to,int age) {
        if(!supported(owner,arrow) || from.squareDistanceTo(to)<1e-8) return false;
        Vec3d closest=null;
        for(EmeraldCity.Column column:columns) {
            if(!column.shielding(age)) continue;
            Vec3d contact=column.contact(from,to,age);
            if(contact!=null && (closest==null || from.squareDistanceTo(contact)<from.squareDistanceTo(closest))) closest=contact;
        }
        if(closest==null) return false;
        // A preceding terrain impact wins; never intercept through an intervening wall.
        AxisAlignedBB path=new AxisAlignedBB(from,closest);
        if(!EmeraldGround.loaded(owner.world,path)) return false;
        RayTraceResult wall=owner.world.rayTraceBlocks(from,closest,false,true,false);
        if(wall!=null && from.squareDistanceTo(wall.hitVec)+1e-6<from.squareDistanceTo(closest)) return false;
        arrow.setDead();
        if(owner.world instanceof WorldServer) ((WorldServer)owner.world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
                closest.x,closest.y,closest.z,3,.025,.04,.025,0);
        return true;
    }
}
