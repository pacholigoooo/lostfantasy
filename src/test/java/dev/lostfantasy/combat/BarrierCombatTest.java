package dev.lostfantasy.combat;

import com.google.common.base.Predicate;
import com.mojang.authlib.GameProfile;
import dev.lostfantasy.Balance;
import dev.lostfantasy.TestWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import static org.junit.Assert.*;

public class BarrierCombatTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }
    @Test public void stanceDoesNotNarrowFovOrRemoveOtherSpeedEffects() {
        Player owner=new Player(new Arena());
        net.minecraft.entity.ai.attributes.IAttributeInstance speed=owner.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        speed.setBaseValue(.1);
        speed.applyModifier(new net.minecraft.entity.ai.attributes.AttributeModifier(UUID.randomUUID(),"Other speed effect",.2,2));
        double before=(speed.getAttributeValue()/owner.capabilities.getWalkSpeed()+1)/2;
        assertEquals(1,BarrierCasting.fieldOfViewCorrection(owner),0);
        BarrierCasting.movementModifier(owner,true);
        assertEquals(0,speed.getAttributeValue(),0);
        assertEquals(before,.5*BarrierCasting.fieldOfViewCorrection(owner),1e-6);
        assertEquals(0,speed.getAttributeValue(),0);
        BarrierCasting.movementModifier(owner,false);
        assertEquals(.12,speed.getAttributeValue(),1e-8);
        assertEquals(1,BarrierCasting.fieldOfViewCorrection(owner),0);
    }
    @Test public void actualCombatLoopDealsEightPulsesAndOneFinalHitWithOneMultiplier() {
        Arena world=new Arena(); Player owner=new Player(world); Victim victim=new Victim(world);
        victim.setPosition(0,64,3); world.entities.add(victim);
        for(int age=0;age<53;age++) BarrierCombat.tick(owner,new Vec3d(0,0,1),age,()->true);
        assertEquals(9,victim.hits); assertEquals(56,victim.damage,.0001);
        assertEquals(24,victim.lastDamage,0); assertFalse(Combat.suppressesKnockback(victim));
        assertEquals(0,victim.motionX,0); assertEquals(0,victim.motionZ,0);
    }
    @Test public void lateArrivalAndEarlyDepartureOnlyReceivePresentPulses() {
        Arena world=new Arena(); Player owner=new Player(world); Victim victim=new Victim(world); world.entities.add(victim);
        for(int age=0;age<53;age++) {
            victim.setPosition(0,64,age>=14 && age<30?3:9);
            BarrierCombat.tick(owner,new Vec3d(0,0,1),age,()->true);
        }
        assertEquals(4,victim.hits); assertEquals(16,victim.damage,0);
    }
    @Test public void wallsBlockBothDamageAndAttractionAndUnloadedAreasAreNeverRead() {
        Arena world=new Arena(); Player owner=new Player(world); Victim victim=new Victim(world);
        victim.setPosition(0,64,4); world.entities.add(victim);
        world.blocks.put(new BlockPos(0,65,2),Blocks.STONE.getDefaultState());
        assertFalse(BarrierCombat.visible(owner,victim));
        BarrierCombat.tick(owner,new Vec3d(0,0,1),6,()->true);
        assertEquals(0,victim.hits); assertEquals(0,victim.motionZ,0);
        world.loaded=false;
        assertFalse(BarrierCombat.visible(owner,victim));
    }
    @Test public void pullRespectsCollisionsResistanceBossesAndSpeedCeiling() {
        Arena world=new Arena(); Victim victim=new Victim(world); victim.setPosition(0,64,6);
        Vec3d center=new Vec3d(0,64,3);
        for(int i=0;i<20;i++) BarrierCombat.pull(victim,center,.12);
        assertEquals(-.12,victim.motionZ,1e-9);
        victim.motionZ=0; victim.boss=true; BarrierCombat.pull(victim,center,.12); assertEquals(0,victim.motionZ,0);
        victim.boss=false; victim.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        BarrierCombat.pull(victim,center,.12); assertEquals(0,victim.motionZ,0);
        victim.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0);
        world.blocked=true; BarrierCombat.pull(victim,center,.12); assertEquals(0,victim.motionZ,0);
        world.blocked=false; victim.motionZ=-.4; BarrierCombat.pull(victim,center,.12);
        assertEquals(-.4,victim.motionZ,0); // Never overwrite another source of movement.
    }
    @Test public void teamAndPvpProtectedTargetsCannotBePulledOrHit() {
        Arena world=new Arena(); Player owner=new Player(world); Victim ally=new Victim(world); ally.ally=true;
        ally.setPosition(0,64,4); world.entities.add(ally);
        Player other=new Player(world); other.setPosition(0,64,4); world.entities.add(other);
        boolean pvp=Balance.pvp; Balance.pvp=false;
        try { BarrierCombat.tick(owner,new Vec3d(0,0,1),6,()->true); }
        finally { Balance.pvp=pvp; }
        assertEquals(0,ally.hits); assertEquals(0,ally.motionZ,0); assertEquals(0,other.motionZ,0);
    }
    @Test public void knockbackScopeIsClearedAfterAnExceptionAndOtherAttacksRemainUnaffected() {
        Arena world=new Arena(); Player owner=new Player(world); Victim target=new Victim(world); target.fail=true;
        try { Combat.spellDamageWithoutKnockback(owner,target,2); fail(); } catch(IllegalStateException expected) {}
        assertFalse(Combat.suppressesKnockback(target));
        assertEquals(0,target.hurtResistantTime);
    }
    @Test public void movementModifierHasNoResidualEffectAndDoesNotZeroExternalVelocity() {
        Player owner=new Player(new Arena()); double speed=owner.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
        owner.motionX=.3; owner.motionY=.2;
        BarrierCasting.movementModifier(owner,true); BarrierCasting.movementModifier(owner,true);
        assertEquals(0,owner.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue(),0);
        assertEquals(.3,owner.motionX,0); assertEquals(.2,owner.motionY,0);
        BarrierCasting.movementModifier(owner,false);
        assertEquals(speed,owner.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue(),0);
    }
    @Test public void cancellationDuringOneHitStopsTheOtherTargetsAndClosingSound() {
        Arena world=new Arena(); Player owner=new Player(world); boolean[] active={true};
        Victim first=new Victim(world) {
            @Override public boolean attackEntityFrom(DamageSource source,float amount) {
                boolean result=super.attackEntityFrom(source,amount);active[0]=false;return result;
            }
        };
        Victim second=new Victim(world);
        first.setPosition(0,64,3);second.setPosition(0,64,4);
        world.entities.add(first);world.entities.add(second);
        BarrierCombat.tick(owner,new Vec3d(0,0,1),6,()->active[0]);
        assertEquals(1,first.hits);assertEquals(0,second.hits);
        assertEquals(0,second.motionZ,0);assertEquals(0,world.sounds);
    }
    private static class Player extends EntityPlayer {
        Player(Arena world) { super(world,new GameProfile(UUID.randomUUID(),"BarrierTester")); setPosition(0,64,0); }
        @Override public boolean isSpectator() { return false; }
        @Override public boolean isCreative() { return false; }
    }
    private static class Victim extends EntityCreature {
        int hits; float damage,lastDamage; boolean boss,ally,fail;
        Victim(Arena world) { super(world); }
        @Override public boolean isNonBoss() { return !boss; }
        @Override public boolean isOnSameTeam(Entity entity) { return ally; }
        @Override public boolean attackEntityFrom(DamageSource source,float amount) {
            assertTrue(Combat.suppressesKnockback(this)); assertEquals(0,hurtResistantTime);
            if(fail) throw new IllegalStateException("test hit failure");
            hits++; damage+=amount; lastDamage=amount; return true;
        }
    }
    private static class Arena extends TestWorld {
        final List<Entity> entities=new ArrayList<>(); boolean blocked;int sounds;
        @Override public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> type, AxisAlignedBB area, Predicate<? super T> predicate) {
            List<T> result=new ArrayList<>();
            for(Entity entity:entities) if(type.isInstance(entity) && area.intersects(entity.getEntityBoundingBox())) {
                T value=type.cast(entity); if(predicate==null || predicate.apply(value)) result.add(value);
            }
            return result;
        }
        @Override public List<AxisAlignedBB> getCollisionBoxes(Entity entity,AxisAlignedBB box) {
            return blocked?Collections.singletonList(box):Collections.emptyList();
        }
        @Override public void playSound(EntityPlayer player,double x,double y,double z,SoundEvent sound,SoundCategory category,float volume,float pitch) {sounds++;}
    }
}
