package dev.lostfantasy.combat;

import dev.lostfantasy.Balance;
import dev.lostfantasy.FantasyAdvancement;
import dev.lostfantasy.core.CastMotion;
import dev.lostfantasy.core.FourfoldBarrier;
import dev.lostfantasy.core.EmeraldCity;
import dev.lostfantasy.core.Rules;
import dev.lostfantasy.core.Spell;
import dev.lostfantasy.core.SpellSessions;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.entity.EntityCompanion;
import dev.lostfantasy.item.FantasyItem;
import dev.lostfantasy.network.EffectMessage;
import dev.lostfantasy.network.Network;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class SpellManager {
    private static final SpellSessions<Cast> SESSIONS = new SpellSessions<>();
    private static final Map<UUID, Lock> LOCKS = new HashMap<>();

    private static final Map<Spell,java.util.function.ObjIntConsumer<Cast>> TICKS;
    static {
        Map<Spell,java.util.function.ObjIntConsumer<Cast>> ticks=new java.util.EnumMap<>(Spell.class);
        ticks.put(Spell.ROYAL_FLARE,Cast::flare);
        ticks.put(Spell.ABANDONED_TRAIN,Cast::train);
        ticks.put(Spell.GUNGNIR,Cast::spear);
        ticks.put(Spell.FOUR_OF_A_KIND,(cast,age)->{}); // Echo entities own their attack updates.
        ticks.put(Spell.FOURFOLD_BARRIER,(cast,age)->BarrierCombat.tick(cast.owner,cast.direction,age,cast::running));
        ticks.put(Spell.EMERALD_CITY,(cast,age)->cast.emerald.tick(cast.owner,age,cast::running));
        TICKS=Spell.completeRegistry(ticks);
    }
    public static void validateCatalog() { Spell.completeRegistry(TICKS); }
    private SpellManager() {}

    public static boolean active(EntityPlayer player) {
        return SESSIONS.foreground(player.getUniqueID()) != null;
    }

    public static boolean restrictsMovement(EntityPlayer player) {
        Cast cast = SESSIONS.foreground(player.getUniqueID());
        return cast != null && cast.owner == player && cast.valid() && cast.spell.restrictsMovement(cast.age());
    }

    public static boolean interceptEmeraldArrow(EntityArrow arrow,Vec3d end) {
        for(Cast cast:SESSIONS.snapshot()) {
            if(cast.emerald!=null && cast.world==arrow.world && cast.valid()
                    && cast.emerald.intercept(cast.owner,arrow,arrow.getPositionVector(),end,cast.age())) return true;
        }
        return false;
    }

    public static void unload(net.minecraft.world.World world) {
        for (Cast cast : SESSIONS.snapshot()) {
            if (cast.world == world && SESSIONS.remove(cast.owner.getUniqueID(), cast.spell, cast)) cast.finish();
        }
        LOCKS.values().removeIf(lock -> lock.world == world);
    }

    public static boolean protectsCaster(EntityPlayer player) {
        Cast cast = SESSIONS.foreground(player.getUniqueID());
        return cast != null && cast.owner == player && cast.valid()
                && cast.spell.protectsCaster(cast.age(), cast.duration);
    }

    public static boolean ownsEcho(EntityPlayer owner, UUID echo) {
        Cast cast = SESSIONS.get(owner.getUniqueID(), Spell.FOUR_OF_A_KIND);
        return cast != null && cast.echoes.contains(echo);
    }

    public static List<EntityCompanion> echoPeers(EntityPlayer owner, EntityCompanion querying) {
        Cast cast = SESSIONS.get(owner.getUniqueID(), Spell.FOUR_OF_A_KIND);
        if (cast == null || cast.world != querying.world) return Collections.emptyList();

        List<EntityCompanion> peers = new ArrayList<>(2);
        AxisAlignedBB area = querying.getEntityBoundingBox().grow(64);
        for (UUID id : cast.echoes) {
            Entity entity = cast.world.getEntityFromUuid(id);
            if (entity instanceof EntityCompanion && entity != querying && !entity.isDead
                    && area.intersects(entity.getEntityBoundingBox())) {
                EntityCompanion peer = (EntityCompanion) entity;
                if (owner.getUniqueID().equals(peer.ownerId())) peers.add(peer);
            }
        }
        return peers;
    }

    public static boolean locked(Entity entity) {
        return LOCKS.containsKey(entity.getUniqueID());
    }

    public static void clear() {
        for (Cast cast : SESSIONS.snapshot()) cast.finish();
        SESSIONS.clear();
        LOCKS.clear();
    }

    public static void cast(EntityPlayerMP player) {
        if(dev.lostfantasy.world.HiganWorld.inside(player)
                && PlayerData.get(player).journey.phase()==dev.lostfantasy.core.RiverJourney.Phase.SAILING)return;
        if (!player.isEntityAlive() || player.isSpectator()) return;
        PlayerData data = PlayerData.get(player);
        Spell spell = data.selectedSpell();
        if (spell == null || !data.knows(spell) || !data.qualifies(spell)) {
            FantasyItem.message(player, "invalid_spell");
            return;
        }
        if (spell == Spell.FOUR_OF_A_KIND && SESSIONS.get(player.getUniqueID(), spell) != null) {
            FantasyItem.message(player, "echo_active");
            return;
        }
        if (active(player) || locked(player) || data.cooldownFor(spell) > 0 || player.isRiding() || player.isElytraFlying()) {
            FantasyItem.message(player, "cooldown");
            return;
        }
        if (data.power() < 1) {
            FantasyItem.message(player, "no_power");
            return;
        }
        EntityLivingBase target = null;
        if (spell == Spell.ABANDONED_TRAIN) {
            target = Combat.target(player, 48);
            if (target == null || locked(target)) {
                FantasyItem.message(player, "no_target");
                return;
            }
        }
        if (spell == Spell.ROYAL_FLARE && !player.world.getCollisionBoxes(player,
                player.getEntityBoundingBox().offset(0, 2, 0)).isEmpty()) {
            FantasyItem.message(player, "need_space");
            return;
        }
        if (spell == Spell.EMERALD_CITY && (!player.onGround || player.capabilities.isFlying)) {
            FantasyItem.message(player,"emerald_ground");
            return;
        }
        Cast cast = new Cast(player, spell, target);
        if (spell == Spell.EMERALD_CITY && cast.emerald.columns.isEmpty()) {
            FantasyItem.message(player,"emerald_ground");
            return;
        }
        if (!SESSIONS.start(player.getUniqueID(), spell, cast)) return;
        if (spell == Spell.ABANDONED_TRAIN) lock(target, cast.duration, player);
        if (spell == Spell.FOUR_OF_A_KIND && !cast.spawnEchoes()) {
            SESSIONS.remove(player.getUniqueID(), spell, cast);
            FantasyItem.message(player, "echo_spawn_failed");
            return;
        }
        data.setPower(data.power()-1);
        if (spell.restrictsMovement(0)) {
            player.stopActiveHand();
            player.setSprinting(false);
            BarrierCasting.movementModifier(player, true);
        }
        data.dirty = true;
        FantasyAdvancement.cast(player, spell);
        // ActionMessage sends the final player state after this method returns.
        if (spell != Spell.FOUR_OF_A_KIND) cast.broadcast(spell.networkId);
    }

    public static void tick() {
        if (SESSIONS.isEmpty() && LOCKS.isEmpty()) return;
        tickCasts();
        tickLocks();
    }

    private static void tickCasts() {
        // Damage callbacks may cancel casts, so iterate a snapshot and recheck ownership.
        for (Cast cast : SESSIONS.snapshot()) {
            UUID owner = cast.owner.getUniqueID();
            if (SESSIONS.get(owner, cast.spell) != cast) continue;
            int age = cast.age();
            if (!cast.valid() || age >= cast.duration || (cast.spell == Spell.FOUR_OF_A_KIND && !cast.hasLiveEcho())) {
                SESSIONS.remove(owner, cast.spell, cast);
                cast.finish();
                continue;
            }
            if(cast.spell.restrictsMovement(age)) {
                cast.owner.stopActiveHand();cast.owner.setSprinting(false);
            } else if(cast.spell.restrictsMovement(0) && !cast.movementReleased) {
                BarrierCasting.movementModifier(cast.owner,false);cast.movementReleased=true;
            }
            TICKS.get(cast.spell).accept(cast,age);
        }
    }

    private static void tickLocks() {
        Iterator<Lock> locks = LOCKS.values().iterator();
        while (locks.hasNext()) {
            Lock lock = locks.next();
            if (!lock.target.isEntityAlive() || !lock.owner.isEntityAlive()
                    || lock.target.dimension != lock.dimension || lock.owner.dimension != lock.dimension
                    || lock.owner.connection == null || lock.world.getTotalWorldTime() >= lock.until) {
                locks.remove();
                continue;
            }
            lock.target.motionX = lock.target.motionY = lock.target.motionZ = 0;
            lock.target.fallDistance = 0;
            if (lock.target instanceof EntityPlayerMP) {
                ((EntityPlayerMP) lock.target).connection.setPlayerLocation(lock.at.x, lock.at.y, lock.at.z,
                        lock.target.rotationYaw, lock.target.rotationPitch);
            } else {
                lock.target.setPositionAndUpdate(lock.at.x, lock.at.y, lock.at.z);
            }
        }
    }

    public static void cancel(EntityPlayer player) {
        Cast foreground = SESSIONS.foreground(player.getUniqueID());
        if (foreground != null && SESSIONS.remove(player.getUniqueID(), foreground.spell, foreground)) foreground.finish();
        Cast echoes = SESSIONS.remove(player.getUniqueID(), Spell.FOUR_OF_A_KIND);
        if (echoes != null) echoes.finish();
        LOCKS.values().removeIf(lock -> lock.owner == player || lock.target == player);
    }

    private static void lock(EntityLivingBase target, int duration, EntityPlayerMP owner) {
        LOCKS.put(target.getUniqueID(), new Lock(target, owner, duration));
    }

    public static float interceptWithEcho(EntityPlayerMP owner, float damage) {
        Cast cast = SESSIONS.get(owner.getUniqueID(), Spell.FOUR_OF_A_KIND);
        if (cast == null) return 0;

        for (UUID id : cast.echoes) {
            Entity entity = cast.world.getEntityFromUuid(id);
            if (!(entity instanceof EntityCompanion) || !entity.isEntityAlive()) continue;

            EntityCompanion echo = (EntityCompanion) entity;
            float before = echo.getHealth();
            int invulnerability = echo.hurtResistantTime;
            echo.hurtResistantTime = 0;
            try {
                echo.attackEntityFrom(DamageSource.MAGIC, damage * .25f);
            } finally {
                echo.hurtResistantTime = Math.max(invulnerability, echo.hurtResistantTime);
            }
            return Math.max(0, Math.min(damage * .25f, before - echo.getHealth()));
        }
        return 0;
    }

    private static final class Lock {
        final EntityLivingBase target;
        final EntityPlayerMP owner;
        final Vec3d at;
        final int dimension;
        final WorldServer world;
        final long until;

        Lock(EntityLivingBase target, EntityPlayerMP owner, int duration) {
            this.target = target;
            this.owner = owner;
            at = target.getPositionVector();
            dimension = target.dimension;
            world = owner.getServerWorld();
            until = world.getTotalWorldTime() + duration;
        }
    }

    private static final class Cast {
        final EntityPlayerMP owner;
        final WorldServer world;
        final Spell spell;
        final long start;
        final int duration;
        final int dimension;
        final Vec3d direction;
        final float castYaw;
        final EntityLivingBase target;
        final Set<Integer> hit;
        final Set<Long> broken;
        final List<UUID> echoes;
        final EmeraldCombat emerald;
        Vec3d origin;
        boolean spearStopped;
        boolean finished;
        boolean movementReleased;

        Cast(EntityPlayerMP owner, Spell spell, EntityLivingBase target) {
            this.owner = owner;
            this.spell = spell;
            this.target = target;
            world = owner.getServerWorld();
            dimension = owner.dimension;
            castYaw = owner.renderYawOffset;
            start = world.getTotalWorldTime();
            hit = spell == Spell.ABANDONED_TRAIN || spell == Spell.GUNGNIR ? new HashSet<>() : Collections.emptySet();
            broken = spell == Spell.ABANDONED_TRAIN ? new HashSet<>() : Collections.emptySet();
            echoes = spell == Spell.FOUR_OF_A_KIND ? new ArrayList<>(3) : Collections.emptyList();

            double yaw = Math.toRadians(owner.rotationYaw);
            Vec3d forward = new Vec3d(-Math.sin(yaw), 0, Math.cos(yaw));
            emerald=spell==Spell.EMERALD_CITY?new EmeraldCombat(EmeraldGround.plan(world,owner.getPositionVector(),forward)):null;
            switch (spell) {
                case ROYAL_FLARE:
                    duration = Balance.chargeTicks + Balance.flareTicks;
                    origin = owner.getPositionVector();
                    direction = forward;
                    break;
                case ABANDONED_TRAIN:
                    duration = CastMotion.TRAIN_DURATION;
                    origin = owner.getPositionVector().add(forward.z * 3.2, 0, -forward.x * 3.2);
                    Vec3d aim = new Vec3d(target.posX - origin.x, 0, target.posZ - origin.z).normalize();
                    direction = aim.lengthSquared() < .001 ? forward : aim;
                    break;
                case GUNGNIR:
                    duration = CastMotion.SPEAR_DURATION;
                    origin = owner.getPositionEyes(1);
                    direction = owner.getLookVec();
                    break;
                case FOUR_OF_A_KIND:
                    duration = Balance.cloneTicks;
                    origin = owner.getPositionVector();
                    direction = owner.getLookVec();
                    break;
                case FOURFOLD_BARRIER:
                    duration = FourfoldBarrier.DURATION;
                    origin = owner.getPositionVector();
                    direction = forward;
                    break;
                case EMERALD_CITY:
                    duration=EmeraldCity.DURATION;
                    origin=owner.getPositionVector();
                    direction=forward;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported spell: " + spell);
            }
        }

        int age() {
            return (int) (world.getTotalWorldTime() - start);
        }

        boolean valid() {
            if (!owner.isEntityAlive() || owner.isSpectator() || owner.connection == null || owner.dimension != dimension || owner.world != world
                    || world.getPlayerEntityByUUID(owner.getUniqueID()) != owner) return false;
            return target == null || hit.contains(target.getEntityId())
                    || (target.isEntityAlive() && target.dimension == dimension && target.world == world
                    && (!(target instanceof EntityPlayer) || !((EntityPlayer) target).isSpectator()));
        }

        boolean running() {
            return !finished && SESSIONS.get(owner.getUniqueID(), spell) == this && valid();
        }

        boolean spawnEchoes() {
            for (int index = 0; index < 3; index++) {
                EntityCompanion echo = new EntityCompanion(world, owner, index, duration);
                if (world.spawnEntity(echo)) echoes.add(echo.getUniqueID());
            }
            return !echoes.isEmpty();
        }

        boolean hasLiveEcho() {
            for (UUID id : echoes) {
                Entity echo = world.getEntityFromUuid(id);
                if (echo != null && echo.isEntityAlive()) return true;
            }
            return false;
        }

        void broadcast(int kind) {
            float range = Balance.flareRadius;
            int charge = Balance.chargeTicks;
            if (spell == Spell.GUNGNIR) {
                RayTraceResult wall = world.rayTraceBlocks(origin, origin.add(direction.scale(60)), false, true, false);
                range = wall == null ? 60 : (float) origin.distanceTo(wall.hitVec);
                charge = CastMotion.SPEAR_RELEASE;
            } else if (spell == Spell.ABANDONED_TRAIN) {
                charge = CastMotion.TRAIN_CHARGE;
            } else if (spell == Spell.FOURFOLD_BARRIER) {
                charge = FourfoldBarrier.OPEN;
                range = (float) FourfoldBarrier.FAR;
            }
            EffectMessage effect = new EffectMessage(kind, owner.getEntityId(), dimension, start, duration, charge, origin, direction, range);
            effect.castYaw = castYaw;
            if (kind==Spell.EMERALD_CITY.networkId) effect.columns=emerald.columns;
            Network.CHANNEL.sendToAllAround(effect,
                    new NetworkRegistry.TargetPoint(dimension, origin.x, origin.y, origin.z, 160));
        }

        void finish() {
            if (finished) return;
            finished = true;
            for (UUID id : echoes) {
                Entity echo = world.getEntityFromUuid(id);
                if (echo != null) echo.setDead();
            }
            PlayerData data = PlayerData.get(owner);
            data.dirty = true;
            if (spell == Spell.FOUR_OF_A_KIND) {
                // Clone expiry must leave the owner's current spell and pose alone.
                data.echoCooldown = Balance.spellCooldown;
                return;
            }
            LOCKS.values().removeIf(lock -> lock.owner == owner && lock.target == target);
            if (spell.restrictsMovement(0)) {
                BarrierCasting.movementModifier(owner, false);
            } else {
                owner.fallDistance = 0;
                owner.motionY = Math.min(0, owner.motionY);
            }
            data.cooldown = Balance.spellCooldown;
            broadcast(EffectMessage.STOP);
        }

        void flare(int age) {
            boolean grounded=dev.lostfantasy.world.HiganWorld.inside(owner);
            double lift = grounded?0:CastMotion.flareLift(age, Balance.chargeTicks);
            // Levitation passes vanilla's airborne check and expires after interruption.
            if (!grounded && !owner.isPotionActive(MobEffects.LEVITATION)) {
                owner.addPotionEffect(new PotionEffect(MobEffects.LEVITATION, 4, 0, true, false));
            }
            owner.connection.setPlayerLocation(origin.x, origin.y + lift, origin.z, owner.rotationYaw, owner.rotationPitch);
            owner.motionX = owner.motionY = owner.motionZ = 0;
            owner.fallDistance = 0;
            if (age < Balance.chargeTicks) return;

            Vec3d center = origin.add(0, lift + CastMotion.flareSunHeight(age, Balance.chargeTicks), 0);
            AxisAlignedBB area = new AxisAlignedBB(center, center).grow(Balance.flareRadius);
            for (EntityLivingBase victim : world.getEntitiesWithinAABB(EntityLivingBase.class, area,
                    entity -> entity != null && Combat.hostile(owner, entity))) {
                if (!running()) return;
                AxisAlignedBB box = victim.getEntityBoundingBox();
                if (Rules.sphereIntersectsBox(center.x, center.y, center.z, Balance.flareRadius,
                        box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)) {
                    Combat.spellDamage(owner, victim, Balance.flareDamage, false);
                }
            }
        }

        void train(int age) {
            if (age < CastMotion.TRAIN_CHARGE) return;
            double current = CastMotion.trainTravel(age + 1);
            double previous = CastMotion.trainTravel(age);
            double back = Math.max(0, previous - CastMotion.TRAIN_LENGTH);
            Vec3d from = origin.add(direction.scale(back));
            Vec3d to = origin.add(direction.scale(current));
            AxisAlignedBB area = new AxisAlignedBB(from, to)
                    .grow(CastMotion.TRAIN_HALF_WIDTH, 0, CastMotion.TRAIN_HALF_WIDTH)
                    .expand(0, CastMotion.TRAIN_HEIGHT, 0);
            for (EntityLivingBase victim : world.getEntitiesWithinAABB(EntityLivingBase.class, area,
                    entity -> entity != null && !hit.contains(entity.getEntityId()) && Combat.hostile(owner, entity))) {
                if (!running()) return;
                AxisAlignedBB box = victim.getEntityBoundingBox();
                if (!CastMotion.trainIntersects((box.minX + box.maxX) * .5 - origin.x,
                        (box.minZ + box.maxZ) * .5 - origin.z, (box.maxX - box.minX) * .5, (box.maxZ - box.minZ) * .5,
                        direction.x, direction.z, back, current)) continue;
                if (hit.add(victim.getEntityId())) {
                    Combat.spellDamage(owner, victim, Balance.trainDamage, false);
                    if (!running()) return;
                    LOCKS.remove(victim.getUniqueID());
                    if (victim.isEntityAlive() && victim.world == world)
                        victim.addVelocity(direction.x * 1.6, .5, direction.z * 1.6);
                }
            }
            TrainBlocks.sweep(owner, origin, direction, previous, current, broken, this::running);
        }

        void spear(int age) {
            if (age < CastMotion.SPEAR_RELEASE || spearStopped) return;
            if (age == CastMotion.SPEAR_RELEASE) {
                origin = owner.getPositionEyes(1);
                broadcast(spell.networkId);
            }
            Vec3d from = origin.add(direction.scale(CastMotion.spearTravel(age)));
            Vec3d to = origin.add(direction.scale(CastMotion.spearTravel(age + 1)));
            RayTraceResult wall = world.rayTraceBlocks(from, to, false, true, false);
            if (wall != null) to = wall.hitVec;
            AxisAlignedBB area = new AxisAlignedBB(from, to).grow(.8);
            for (EntityLivingBase victim : world.getEntitiesWithinAABB(EntityLivingBase.class, area,
                    entity -> entity != null && !hit.contains(entity.getEntityId()) && Combat.hostile(owner, entity))) {
                if (!running()) return;
                double radius = .7 + victim.width * .5;
                if (Rules.distanceToSegmentSquared(victim.posX, victim.posY + victim.height * .5, victim.posZ,
                        from.x, from.y, from.z, to.x, to.y, to.z) < radius * radius) {
                    hit.add(victim.getEntityId());
                    Combat.spellDamage(owner, victim, Balance.spearDamage, false);
                    if (!running()) return;
                    if (victim.isEntityAlive() && victim.world == world) lock(victim, 20, owner);
                }
            }
            if (!running()) return;
            for (EntityArrow arrow : world.getEntitiesWithinAABB(EntityArrow.class, area)) arrow.setDead();
            if (wall != null) spearStopped = true;
        }
    }

    public static Vec3d[] flareRays(double tick) {
        Vec3d[] rays = new Vec3d[18];
        int next = 0;
        for (int ring = 0; ring < 3; ring++) {
            double tilt = ring * Math.PI / 3 + Math.sin(tick * .013) * .3;
            double sinTilt = Math.sin(tilt);
            double cosTilt = Math.cos(tilt);
            for (int ray = 0; ray < 6; ray++) {
                double angle = ray * Math.PI / 3 + tick * (ring % 2 == 0 ? .035 : -.043) + ring * .47;
                double sin = Math.sin(angle);
                rays[next++] = new Vec3d(Math.cos(angle), sin * sinTilt, sin * cosTilt);
            }
        }
        return rays;
    }
}
