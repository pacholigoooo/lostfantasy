package dev.lostfantasy.combat;

import dev.lostfantasy.Balance;
import dev.lostfantasy.core.FourfoldBarrier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.function.BooleanSupplier;

public final class BarrierCombat {
    private BarrierCombat() {}

    public static void tick(EntityPlayer owner, Vec3d direction, int age, BooleanSupplier valid) {
        if (!valid.getAsBoolean() || (!FourfoldBarrier.pulling(age) && age != FourfoldBarrier.FINISH_HIT)) return;
        Vec3d origin = owner.getPositionVector();
        Vec3d center = origin.add(direction.scale(FourfoldBarrier.CENTER));
        boolean damage = FourfoldBarrier.smallHit(age) || age == FourfoldBarrier.FINISH_HIT;
        // A bounded query only; never request or generate chunks to find targets.
        AxisAlignedBB query = new AxisAlignedBB(origin, origin.add(direction.scale(7))).grow(3, 0, 3)
                .expand(0, FourfoldBarrier.TOP, 0).expand(0, FourfoldBarrier.BOTTOM, 0);
        for (EntityLivingBase target : owner.world.getEntitiesWithinAABB(EntityLivingBase.class, query,
                entity -> entity != null && Combat.hostile(owner, entity))) {
            if (!valid.getAsBoolean()) return;
            if (!inRange(origin, direction, target.getEntityBoundingBox(), true) || !visible(owner, target)) continue;
            if (FourfoldBarrier.pulling(age)) pull(target, center, Balance.barrierPullSpeed);
            if (damage && inRange(origin, direction, target.getEntityBoundingBox(), false)) {
                float amount = age == FourfoldBarrier.FINISH_HIT ? Balance.barrierFinalDamage : Balance.barrierDamage;
                Combat.spellDamageWithoutKnockback(owner, target, amount);
            }
        }
        if (damage && valid.getAsBoolean()) owner.world.playSound(null, center.x, center.y + 1, center.z, SoundEvents.BLOCK_NOTE_HARP,
                SoundCategory.PLAYERS, age == FourfoldBarrier.FINISH_HIT ? .9f : .35f,
                age == FourfoldBarrier.FINISH_HIT ? .65f : 1.25f + (age - FourfoldBarrier.OPEN) / 120f);
    }

    static boolean inRange(Vec3d origin, Vec3d direction, AxisAlignedBB box, boolean pull) {
        return FourfoldBarrier.intersects(direction.x, direction.z,
                box.minX-origin.x, box.minY-origin.y, box.minZ-origin.z,
                box.maxX-origin.x, box.maxY-origin.y, box.maxZ-origin.z, pull);
    }

    static boolean visible(EntityPlayer owner, EntityLivingBase target) {
        Vec3d eye = owner.getPositionEyes(1);
        AxisAlignedBB box = target.getEntityBoundingBox();
        Vec3d point = new Vec3d(target.posX, Math.max(box.minY + .1, Math.min(box.maxY - .1, eye.y)), target.posZ);
        World world = owner.world;
        if (!world.isAreaLoaded(new BlockPos(Math.min(eye.x, point.x), Math.min(eye.y, point.y), Math.min(eye.z, point.z)),
                new BlockPos(Math.max(eye.x, point.x), Math.max(eye.y, point.y), Math.max(eye.z, point.z)))) return false;
        return world.rayTraceBlocks(eye, point, false, true, false) == null;
    }

    static void pull(EntityLivingBase target, Vec3d center, double maximum) {
        if (!target.isNonBoss()) return;
        double resistance = target.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue();
        double limit = maximum * Math.max(0, 1 - resistance);
        double x = center.x-target.posX, z = center.z-target.posZ, distance = Math.sqrt(x*x+z*z);
        if (distance < .1 || limit <= 0) return;
        x /= distance; z /= distance;
        double inward = target.motionX*x + target.motionZ*z;
        double addition = Math.min(.03, Math.min(distance, limit) - inward);
        if (addition <= 0) return; // Do not cancel the target's own movement or external forces.
        double vx = x*addition, vz = z*addition;
        AxisAlignedBB swept = target.getEntityBoundingBox().expand(target.motionX+vx, 0, target.motionZ+vz);
        if (!target.world.isAreaLoaded(new BlockPos(swept.minX, swept.minY, swept.minZ), new BlockPos(swept.maxX, swept.maxY, swept.maxZ))
                || !target.world.getCollisionBoxes(target, swept).isEmpty()) return;
        target.addVelocity(vx, 0, vz);
        target.velocityChanged = true;
    }
}
