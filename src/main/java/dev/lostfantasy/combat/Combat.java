package dev.lostfantasy.combat;
import dev.lostfantasy.Balance;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.entity.EntityCompanion;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
public final class Combat {
    private static final ThreadLocal<EntityLivingBase> QUIET_HIT = new ThreadLocal<>();
    private Combat() {}
    /** Called after all final Damage listeners, including cancellation, have returned. */
    static void settledLifeSteal(EntityLivingBase victim, DamageSource source, float damage) {
        if (victim.world.isRemote || !Float.isFinite(damage) || damage <= 0) return;
        Entity attacker = source.getTrueSource();
        if (!(attacker instanceof EntityPlayer) || attacker == victim || isSpell(source)) return;
        EntityPlayer player = (EntityPlayer) attacker;
        if (!player.isEntityAlive() || player.world != victim.world) return;
        PlayerData data = PlayerData.get(player);
        if (data.vampire() > 0) player.heal(Math.min(damage, victim.getHealth()) * data.vampire() * Balance.lifeStealPerTier);
    }
    public static boolean hostile(EntityPlayer owner,EntityLivingBase target) {
        return canHarm(owner,target) && (!(target instanceof EntityPlayer) || Balance.pvp);
    }
    /** Shared ownership/team/server protections; the caller supplies its targeting policy. */
    public static boolean canHarm(EntityPlayer owner,EntityLivingBase target) {
        if(owner==null || target==null || !owner.isEntityAlive() || owner.isSpectator() || owner.world!=target.world)return false;
        if(target==owner || !target.isEntityAlive() || (target instanceof EntityPlayer && ((EntityPlayer)target).isSpectator()) || target.isOnSameTeam(owner))return false;
        if(target instanceof EntityCompanion && owner.getUniqueID().equals(((EntityCompanion)target).ownerId()))return false;
        if(target instanceof EntityTameable && owner.getUniqueID().equals(((EntityTameable)target).getOwnerId()))return false;
        if(target instanceof EntityPlayer) return !((EntityPlayer)target).isCreative() && owner.getServer()!=null && owner.getServer().isPVPEnabled() && owner.canAttackPlayer((EntityPlayer)target);
        return true;
    }
    public static boolean spellDamage(EntityPlayer owner,EntityLivingBase target,float amount,boolean fire) {
        return spellDamage(owner,target,amount,fire,false);
    }
    private static boolean spellDamage(EntityPlayer owner,EntityLivingBase target,float amount,boolean fire,boolean confirm) {
        float damage=amount*Balance.spellDamageMultiplier;
        if(!Float.isFinite(damage) || damage<=0 || !hostile(owner,target))return false;
        DamageSource source=new EntityDamageSource("lostfantasy.spell",owner).setMagicDamage();
        if(fire)source.setFireDamage();
        // Royal Flare is explicitly tick damage. Restore any preexisting invulnerability window.
        int previous=target.hurtResistantTime;target.hurtResistantTime=0;
        try {
            if(!confirm)return target.attackEntityFrom(source,damage);
            try(DamageTransactions.HitResult hit=DamageTransactions.observe(target,source)) {
                boolean accepted=target.attackEntityFrom(source,damage);
                return accepted && hit.confirmed();
            }
        } finally {target.hurtResistantTime=Math.max(previous,target.hurtResistantTime);}
    }
    public static boolean suppressesKnockback(EntityLivingBase target) { return QUIET_HIT.get() == target; }
    public static boolean spellDamageWithoutKnockback(EntityPlayer owner, EntityLivingBase target, float amount) {
        return spellDamageWithoutKnockback(owner,target,amount,false);
    }
    /** Only a positive final Forge settlement permits an Emerald hit count and launch. */
    public static boolean confirmedSpellDamageWithoutKnockback(EntityPlayer owner, EntityLivingBase target, float amount) {
        return spellDamageWithoutKnockback(owner,target,amount,true);
    }
    private static boolean spellDamageWithoutKnockback(EntityPlayer owner, EntityLivingBase target, float amount, boolean confirm) {
        EntityLivingBase previous = QUIET_HIT.get();
        QUIET_HIT.set(target);
        try { return spellDamage(owner, target, amount, false, confirm); }
        finally { if (previous == null) QUIET_HIT.remove(); else QUIET_HIT.set(previous); }
    }
    public static EntityLivingBase target(EntityPlayer p,double distance) {
        Vec3d start=p.getPositionEyes(1),end=start.add(p.getLookVec().scale(distance));
        RayTraceResult block=p.world.rayTraceBlocks(start,end,false,true,false);
        if(block!=null)end=block.hitVec;
        double closest=start.squareDistanceTo(end);EntityLivingBase selected=null;
        for(EntityLivingBase e:p.world.getEntitiesWithinAABB(EntityLivingBase.class,new AxisAlignedBB(start,end).grow(1),v->v!=null&&hostile(p,v))) {
            RayTraceResult hit=e.getEntityBoundingBox().grow(.3).calculateIntercept(start,end);
            if(hit!=null && start.squareDistanceTo(hit.hitVec)<=closest) {closest=start.squareDistanceTo(hit.hitVec);selected=e;}
        }return selected;
    }
    public static boolean isSpell(DamageSource source) {return "lostfantasy.spell".equals(source.damageType) || EchoCombat.isEcho(source);}
}
