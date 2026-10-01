package dev.lostfantasy.entity;

import com.google.common.base.Optional;
import dev.lostfantasy.combat.EchoCombat;
import dev.lostfantasy.combat.SpellManager;
import dev.lostfantasy.core.EchoRoaming;
import dev.lostfantasy.core.EchoMovement;
import dev.lostfantasy.core.EchoNavigation;
import dev.lostfantasy.core.EchoTargeting;
import dev.lostfantasy.core.Facing;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.List;
import java.util.UUID;

public final class EntityCompanion extends EntityCreature {
    private static final double PATROL_SPEED = .22;
    private static final double CHASE_SPEED = .32;
    private static final DataParameter<Optional<UUID>> OWNER = EntityDataManager.createKey(EntityCompanion.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Integer> INDEX = EntityDataManager.createKey(EntityCompanion.class, DataSerializers.VARINT);

    private final EchoRoaming roaming = new EchoRoaming();
    private final EchoNavigation navigation = new EchoNavigation(Math.floorMod(getEntityId(), 7));
    private EchoPathfinder flightPaths;
    private int remaining = 1200;
    private int attackTimer;
    private Vec3d patrolPoint;
    private int nextPatrolTick;

    public EntityCompanion(World world) {
        super(world);
        setSize(.55f, 1.7f);
        setNoGravity(true);
        enablePersistence();
    }

    public EntityCompanion(World world, EntityPlayer owner, int index, int lifetime) {
        this(world);
        dataManager.set(OWNER, Optional.of(owner.getUniqueID()));
        dataManager.set(INDEX, index);
        remaining = lifetime;
        Vec3d position = orbit(owner);
        setPosition(position.x, position.y, position.z);
        ItemStack weapon = owner.getHeldItemMainhand().copy();
        if (!weapon.isEmpty()) weapon.setCount(1);
        setItemStackToSlot(EntityEquipmentSlot.MAINHAND, weapon);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(OWNER, Optional.absent());
        dataManager.register(INDEX, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.3);
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1);
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_SPEED).setBaseValue(4);
    }

    @Override
    protected void initEntityAI() {}

    @Override
    protected float updateDistance(float yaw, float distance) {

        // This runs after travel, on both sides. Face the actual step after collision handling.
        double dx = posX - prevPosX, dy = posY - prevPosY, dz = posZ - prevPosZ;
        if (Facing.moving(dx, dy, dz)) {
            rotationYaw = Facing.yaw(dx, dz, rotationYaw);
            rotationPitch = Facing.pitch(dx, dy, dz);
        } else if (!world.isRemote) {
            EntityLivingBase target = getAttackTarget();
            if (target != null && target.isEntityAlive()) {
                dx = target.posX - posX;
                dy = target.posY + target.getEyeHeight() - posY - getEyeHeight();
                dz = target.posZ - posZ;
                rotationYaw = Facing.yaw(dx, dz, rotationYaw);
                rotationPitch = Facing.pitch(dx, dy, dz);
            } else {
                rotationPitch = 0;
            }
        }
        renderYawOffset = rotationYawHead = rotationYaw;
        return distance;
    }

    public UUID ownerId() {
        return dataManager.get(OWNER).orNull();
    }

    public EntityPlayer owner() {
        UUID id = ownerId();
        return id == null ? null : world.getPlayerEntityByUUID(id);
    }

    private Vec3d orbit(EntityPlayer owner) {
        int index = dataManager.get(INDEX);
        double angle = Math.PI * .5 * (index + 1) + Math.toRadians(owner.rotationYaw);
        return new Vec3d(owner.posX + Math.cos(angle) * 2.5,
                owner.posY, owner.posZ + Math.sin(angle) * 2.5);
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();

        EntityPlayer owner = owner();
        if (owner == null || !owner.isEntityAlive() || --remaining <= 0 || !SpellManager.ownsEcho(owner, getUniqueID())) {
            setDead();
            return;
        }
        if (attackTimer > 0) attackTimer--;
        // Decide before travel() so losing a target cannot leave last tick's chase velocity.
        updateEcho(owner);
    }

    private void updateEcho(EntityPlayer owner) {
        EntityLivingBase target = getAttackTarget();
        if (target != null && !validEchoTarget(owner, target)) {
            setAttackTarget(null);
            target = null;
        }
        double ownerDistance = getDistanceSq(owner);
        EchoRoaming.Mode mode = roaming.update(target != null, ownerDistance);
        if (mode == EchoRoaming.Mode.RETURN) {
            setAttackTarget(null);
            target = null;
        }
        // Keep searches staggered. Only a recent owner attack may interrupt a valid chase.
        if (EchoTargeting.mayAssist(mode, ownerDistance) && (ticksExisted + dataManager.get(INDEX) * 3) % 10 == 0) {
            EntityLivingBase preferred = EchoCombat.ownerAttackTarget(owner);
            if (preferred != null && preferred != target && validEchoTarget(owner, preferred)
                    && getEntitySenses().canSee(preferred)) {
                target = preferred;
            }
            if (target == null) target = findEchoTarget(owner);
            if (target != getAttackTarget()) setAttackTarget(target);
            mode = roaming.update(target != null, ownerDistance);
        }
        motionX = motionY = motionZ = 0;
        if (target != null) {
            chase(owner, target);
        } else if (mode == EchoRoaming.Mode.RETURN) {
            returnToRange(owner);
        } else {
            patrol(owner);
        }
        fallDistance = 0;
    }

    private EntityLivingBase findEchoTarget(EntityPlayer owner) {
        EntityLivingBase selected = null;
        EchoTargeting selection = new EchoTargeting();
        List<EntityCompanion> peers = SpellManager.echoPeers(owner, this);
        for (EntityLivingBase candidate : world.getEntitiesWithinAABB(EntityLivingBase.class,
                getEntityBoundingBox().grow(EchoTargeting.RANGE), entity -> entity != null && EchoCombat.canAttack(owner, entity))) {
            double distanceSquared = getDistanceSq(candidate);
            if (distanceSquared > EchoTargeting.RANGE * EchoTargeting.RANGE) continue;

            boolean claimed = false;
            for (EntityCompanion peer : peers) {
                if (peer.getAttackTarget() == candidate) {
                    claimed = true;
                    break;
                }
            }
            boolean player = candidate instanceof EntityPlayer;
            double ownerDistance = owner.getDistanceSq(candidate);
            if (selection.canImprove(player, ownerDistance, claimed, distanceSquared) && getEntitySenses().canSee(candidate)) {
                selection.select(player, ownerDistance, claimed, distanceSquared);
                selected = candidate;
            }
        }
        return selected;
    }

    private void chase(EntityPlayer owner, EntityLivingBase target) {
        patrolPoint = null;
        nextPatrolTick = ticksExisted;

        boolean ranged = EchoCombat.ranged(this);
        double stopDistance = ranged ? 12 : 2;
        double attackRange = ranged ? 24 : 3;
        double distanceSquared = getDistanceSq(target);
        // Vanilla clears this cache each AI tick; selection and attack share the same ray.
        boolean visible = getEntitySenses().canSee(target);
        steer(owner, target.getPositionVector(), CHASE_SPEED, visible ? stopDistance : 1, false);
        if (attackTimer == 0 && distanceSquared < attackRange * attackRange && visible) {
            attackTimer = EchoCombat.strike(this, owner, target);
            swingArm(EnumHand.MAIN_HAND);
        }
        if (!validEchoTarget(owner, target)) {
            motionX = motionY = motionZ = 0;
            setAttackTarget(null);
            if (roaming.update(false, getDistanceSq(owner)) == EchoRoaming.Mode.RETURN) returnToRange(owner);
        }
    }

    private void patrol(EntityPlayer owner) {
        Vec3d here = getPositionVector();
        Vec3d home = owner.getPositionVector();
        if (patrolPoint != null && (!EchoRoaming.inside(patrolPoint, home) || here.squareDistanceTo(patrolPoint) < 1)) {
            patrolPoint = null;
            nextPatrolTick = ticksExisted;
        }
        if (ticksExisted >= nextPatrolTick) {
            patrolPoint = EchoRoaming.patrolPoint(here, home, getRNG());
            nextPatrolTick = ticksExisted + 40 + getRNG().nextInt(41);
        }
        if (patrolPoint != null && !steer(owner, patrolPoint, PATROL_SPEED, .5, true)) {
            patrolPoint = null;
            nextPatrolTick = ticksExisted + 10;
        }
    }

    private boolean validEchoTarget(EntityPlayer owner, EntityLivingBase target) {
        return target.world == world && world.getEntityByID(target.getEntityId()) == target
                && getDistanceSq(target) <= EchoTargeting.RANGE * EchoTargeting.RANGE
                && EchoCombat.canAttack(owner, target);
    }

    private void returnToRange(EntityPlayer owner) {
        patrolPoint = null;
        nextPatrolTick = ticksExisted;
        Vec3d here = getPositionVector();
        Vec3d goal = EchoRoaming.returnPoint(here, owner.getPositionVector());
        steer(owner, goal, CHASE_SPEED, 0, true);
    }

    private boolean steer(EntityPlayer owner, Vec3d goal, double speed, double stop, boolean stayInRange) {
        Vec3d step = navigation.step(getPositionVector(), goal, speed, stop, ticksExisted,
                desired -> resolveStep(owner, desired, stayInRange), () -> {
                    if (flightPaths == null) flightPaths = new EchoPathfinder(this);
                    return flightPaths.routeTo(goal);
                });
        motionX = step.x;
        motionY = step.y;
        motionZ = step.z;
        return step.lengthSquared() > 1e-12 || getPositionVector().squareDistanceTo(goal) <= stop * stop;
    }

    private Vec3d resolveStep(EntityPlayer owner, Vec3d desired, boolean stayInRange) {
        Vec3d here = getPositionVector();
        Vec3d home = owner.getPositionVector();
        AxisAlignedBB box = getEntityBoundingBox();
        AxisAlignedBB swept = box.expand(desired.x, desired.y, desired.z);
        if (!world.isAreaLoaded(new BlockPos(swept.minX - 1, 0, swept.minZ - 1),
                new BlockPos(swept.maxX + 1, world.getHeight() - 1, swept.maxZ + 1), false)) return Vec3d.ZERO;
        Vec3d step = EchoMovement.slide(box, desired, world.getCollisionBoxes(this, swept));
        if (stayInRange && EchoRoaming.inside(here, home) && !EchoRoaming.inside(here.add(step), home)) return Vec3d.ZERO;
        return step;
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public boolean isInRangeToRenderDist(double distanceSquared) {
        return distanceSquared < 128 * 128;
    }

    @Override
    protected void dropEquipment(boolean recentlyHit, int looting) {}

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return source.getTrueSource() != owner() && super.attackEntityFrom(source, amount);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        UUID owner = ownerId();
        if (owner != null) nbt.setUniqueId("owner", owner);
        nbt.setInteger("index", dataManager.get(INDEX));
        nbt.setInteger("remaining", remaining);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        if (nbt.hasUniqueId("owner")) dataManager.set(OWNER, Optional.of(nbt.getUniqueId("owner")));
        dataManager.set(INDEX, Math.max(0, Math.min(2, nbt.getInteger("index"))));
        remaining = Math.max(1, Math.min(12000, nbt.getInteger("remaining")));
        setNoGravity(true);
    }
}
