package dev.lostfantasy.combat;

import com.google.common.base.Predicate;
import com.mojang.authlib.GameProfile;
import dev.lostfantasy.Balance;
import dev.lostfantasy.TestWorld;
import dev.lostfantasy.core.EmeraldCity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.entity.projectile.EntitySpectralArrow;
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
import java.util.List;
import java.util.UUID;
import static org.junit.Assert.*;

public class EmeraldCombatTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }
    @Test public void flatGroundProducesFourCompleteRingsAtAllYawAngles() {
        Arena world=new Arena();
        for(int angle=0;angle<360;angle+=30) {
            double heading=Math.toRadians(angle);
            Vec3d forward=new Vec3d(Math.sin(heading),0,Math.cos(heading));
            List<EmeraldCity.Column> columns=EmeraldGround.plan(world,new Vec3d(.5,64,.5),forward);
            assertEquals(80,columns.size());int offset=0;
            for(int ring=0;ring<4;ring++) {
                int count=8*(ring+1);double radius=2+2*ring;
                for(int i=0;i<count;i++) {
                    EmeraldCity.Column c=columns.get(offset++);double a=heading+Math.PI*2*i/count;
                    assertEquals(ring,c.round);assertEquals(64,c.y,0);assertEquals(4,c.height,0);
                    assertEquals(.5+Math.sin(a)*radius,c.x,1e-8);
                    assertEquals(.5+Math.cos(a)*radius,c.z,1e-8);
                }
            }
        }
    }
    @Test public void wallAndCliffStopOnlyTheirDirectionWithoutGeneratingBeyondThem() {
        Arena world=new Arena();
        for(int x=-1;x<=1;x++)for(int y=64;y<=66;y++)world.blocks.put(new BlockPos(x,y,4),Blocks.STONE.getDefaultState());
        List<EmeraldCity.Column> columns=plan(world);
        assertTrue(columns.size()<80);assertTrue(columns.stream().anyMatch(c->c.x>7));
        assertTrue(columns.stream().anyMatch(c->c.z< -7));
        assertFalse(columns.stream().anyMatch(c->Math.abs(c.x-.5)<1e-6 && c.z>4));
        world.blocks.clear();world.cliffAt=4;columns=plan(world);
        assertTrue(columns.size()>0);for(EmeraldCity.Column c:columns)assertTrue(c.z+EmeraldCity.RADIUS<=4);
    }
    @Test public void oneBlockStepIsAllowedButTwoBlockRiseStopsItsRadialRoute() {
        Arena world=new Arena();world.stepAt=3;world.stepHeight=1;
        List<EmeraldCity.Column> columns=EmeraldGround.plan(world,new Vec3d(.5,64,0),new Vec3d(0,0,1));
        assertTrue(columns.stream().anyMatch(c->Math.abs(c.x-.5)<1e-6 && c.z>7));
        for(EmeraldCity.Column c:columns)assertEquals(c.z>=3?65:64,c.y,0);
        world.stepHeight=2;columns=EmeraldGround.plan(world,new Vec3d(.5,64,0),new Vec3d(0,0,1));
        assertFalse(columns.stream().anyMatch(c->c.z>=3));
        assertTrue(columns.stream().anyMatch(c->c.z< -7));
    }
    @Test public void raisedCornerLoweredCornerAndAnUnsampledEdgeHoleCannotLeaveAFloatingBase() {
        for(int variant=0;variant<3;variant++) {
            Arena world=new Arena();
            if(variant==0)world.blocks.put(new BlockPos(1,64,3),Blocks.STONE.getDefaultState());
            else world.blocks.put(new BlockPos(variant==1?1:0,63,3),Blocks.AIR.getDefaultState());
            List<EmeraldCity.Column> columns=plan(world);
            assertFalse(columns.stream().anyMatch(c->Math.abs(c.x-.5)<1e-6 && c.z>1));
            assertTrue(columns.stream().anyMatch(c->c.z< -7));
        }
    }
    @Test public void aStepThroughTheBaseStopsThatRouteButUniformHalfSlabsSupportAllRings() {
        Arena world=new Arena();world.stepAt=4;world.stepHeight=1;
        assertFalse(plan(world).stream().anyMatch(c->Math.abs(c.x-.5)<1e-6 && c.z>4));
        world=new Arena();
        for(int x=-10;x<=10;x++)for(int z=-10;z<=10;z++)world.blocks.put(new BlockPos(x,63,z),Blocks.STONE_SLAB.getDefaultState());
        List<EmeraldCity.Column> columns=EmeraldGround.plan(world,new Vec3d(.5,63.5,.5),new Vec3d(0,0,1));
        assertEquals(80,columns.size());for(EmeraldCity.Column c:columns)assertEquals(63.5,c.y,0);
    }
    @Test public void enemiesAllAroundTheCasterAreReached() {
        Arena world=new Arena();Player owner=new Player(world);List<Victim> targets=new ArrayList<>();
        for(int i=0;i<8;i++) {
            double a=i*Math.PI/4;Victim target=new Victim(world);target.setPosition(.5+2*Math.sin(a),64,.5+2*Math.cos(a));
            world.entities.add(target);targets.add(target);
        }
        new EmeraldCombat(plan(world)).tick(owner,8,()->true);
        for(Victim target:targets) {assertEquals(1,target.hits);assertTrue(target.motionY>0);}
    }
    @Test public void ceilingCapsGeometryAndAirborneOrUnloadedOriginsAreRejected() {
        Arena world=new Arena();
        for(int x=-10;x<=10;x++) for(int z=-10;z<=10;z++) world.blocks.put(new BlockPos(x,66,z),Blocks.STONE.getDefaultState());
        List<EmeraldCity.Column> columns=plan(world); assertEquals(80,columns.size());
        for(EmeraldCity.Column c:columns) assertEquals(2,c.height,0);
        assertTrue(EmeraldGround.plan(world,new Vec3d(.5,64.5,.5),new Vec3d(0,0,1)).isEmpty());
        world.loaded=false; assertTrue(plan(world).isEmpty());
    }
    @Test public void onePillarHitsOnlyOnceAndRetainedOrSinkingPillarsDoNoDamage() {
        Arena world=new Arena(); Player owner=new Player(world); Victim target=new Victim(world);
        target.setPosition(.5,64,2.5); world.entities.add(target);
        EmeraldCombat combat=new EmeraldCombat(plan(world));
        for(int age=0;age<=64;age++) combat.tick(owner,age,()->true);
        assertEquals(1,target.hits); assertEquals(20,target.damage,0); assertEquals(Balance.emeraldLift,target.motionY,1e-6);
        assertEquals(0,target.motionX,0); assertEquals(0,target.motionZ,0); assertFalse(Combat.suppressesKnockback(target));
    }
    @Test public void movingTargetGetsAtMostThreeSuccessfulHitsAcrossFourDifferentPillars() {
        Arena world=new Arena(); Player owner=new Player(world); Victim target=new Victim(world); world.entities.add(target);
        EmeraldCombat combat=new EmeraldCombat(plan(world));
        for(int age=0;age<=64;age++) {
            int round=Math.max(0,Math.min(3,(age-8)/8)); target.setPosition(.5,64,2.5+2*round);
            combat.tick(owner,age,()->true);
        }
        assertEquals(3,target.hits); assertEquals(60,target.damage,0);
        assertEquals(0,target.fallDistance,0);
    }
    @Test public void canceledDamageDoesNotLaunchAndLateEntryOrUpperFloorReceivesNothing() {
        Arena world=new Arena(); Player owner=new Player(world); Victim canceled=new Victim(world);
        canceled.cancel=true; canceled.setPosition(.5,64,2.5); world.entities.add(canceled);
        EmeraldCombat combat=new EmeraldCombat(plan(world)); combat.tick(owner,8,()->true); combat.tick(owner,9,()->true);
        assertEquals(1,canceled.attempts); assertEquals(0,canceled.motionY,0);
        Victim late=new Victim(world); late.setPosition(.5,64,2.5); world.entities.add(late);
        combat.tick(owner,12,()->true); assertEquals(0,late.hits);
        Victim upstairs=new Victim(world); upstairs.setPosition(.5,67,2.5); world.entities.add(upstairs);
        world.blocks.put(new BlockPos(0,66,2),Blocks.STONE.getDefaultState());
        new EmeraldCombat(plan(world)).tick(owner,11,()->true); assertEquals(0,upstairs.hits);
    }
    @Test public void trueAttackReturnAfterHurtOrDamageCancellationOrZeroCannotLaunchOrSpendTheThreeHitCap() {
        for(int phase=0;phase<4;phase++) {
            Arena world=new Arena();Player owner=new Player(world);Victim target=new Victim(world);world.entities.add(target);
            final boolean cancel=phase%2==0;
            if(phase<2)world.events.hurt=event->{if(cancel)event.setCanceled(true);else event.setAmount(0);};
            else world.events.damage=event->{if(cancel)event.setCanceled(true);else event.setAmount(0);};
            EmeraldCombat combat=new EmeraldCombat(plan(world));target.setPosition(.5,64,2.5);combat.tick(owner,8,()->true);
            assertEquals(1,target.attempts);assertEquals(0,target.hits);assertEquals("phase "+phase,0,target.motionY,0);
            if(phase>=2)assertEquals(20,world.events.beforeLateDamage,0);
            world.events.hurt=event->{};world.events.damage=event->{};
            for(int round=1;round<4;round++) {target.setPosition(.5,64,2.5+round*2);combat.tick(owner,8+round*8,()->true);}
            assertEquals(3,target.hits);assertEquals(60,target.damage,0);
        }
    }
    @Test public void bossResistanceTeamAndCallbackCancellationAreRespected() {
        Arena world=new Arena(); Player owner=new Player(world); Victim boss=new Victim(world); boss.boss=true;
        boss.setPosition(.5,64,2.5); world.entities.add(boss);
        Victim resistant=new Victim(world); resistant.setPosition(.5,64,-1.5);
        resistant.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1); world.entities.add(resistant);
        Victim ally=new Victim(world); ally.ally=true; ally.setPosition(.5,64,2.5); world.entities.add(ally);
        EmeraldCombat combat=new EmeraldCombat(plan(world)); combat.tick(owner,8,()->true);
        assertEquals(1,boss.hits); assertEquals(0,boss.motionY,0); assertEquals(1,resistant.hits); assertEquals(0,resistant.motionY,0);
        assertEquals(0,ally.hits);
        new EmeraldCombat(plan(world)).tick(owner,8,()->false); assertEquals(1,boss.hits);
        boss.boss=false; boss.motionY=.9; EmeraldCombat.launch(boss); assertEquals(.9,boss.motionY,0);
    }
    @Test public void acceptedAttackWithoutASettlementDoesNotLaunchAndLatePositiveDamageDoes() {
        Arena world=new Arena();Player owner=new Player(world);Victim target=new Victim(world);world.entities.add(target);
        target.acceptWithoutSettlement=true;target.setPosition(.5,64,2.5);
        EmeraldCombat combat=new EmeraldCombat(plan(world));combat.tick(owner,8,()->true);
        assertEquals(1,target.attempts);assertEquals(0,target.hits);assertEquals(0,target.motionY,0);
        target.acceptWithoutSettlement=false;world.events.damage=event->event.setAmount(1);
        target.setPosition(.5,64,4.5);combat.tick(owner,16,()->true);
        assertEquals(1,target.hits);assertEquals(1,target.damage,0);assertTrue(target.motionY>0);
    }
    @Test public void listenerExceptionsRestoreHitObservationKnockbackAndInvulnerabilityState() {
        Arena world=new Arena();Player owner=new Player(world);Victim target=new Victim(world);
        target.hurtResistantTime=17;RuntimeException failure=new RuntimeException("listener failure");
        world.events.damage=event->{throw failure;};
        try { Combat.confirmedSpellDamageWithoutKnockback(owner,target,10);fail("Expected listener failure"); }
        catch(RuntimeException caught) { assertSame(failure,caught); }
        assertEquals(17,target.hurtResistantTime);assertFalse(Combat.suppressesKnockback(target));
        assertNull(DamageTransactions.enter(target,new DamageSource("probe")));
        world.events.damage=event->{};
        assertTrue(Combat.confirmedSpellDamageWithoutKnockback(owner,target,10));
        assertFalse(Combat.suppressesKnockback(target));assertEquals(17,target.hurtResistantTime);
    }
    @Test public void aCancelledAttackCannotUseDamageTriggeredByItsAttackListenerToLaunch() {
        Arena world=new Arena();Player owner=new Player(world);
        Victim target=new Victim(world) {
            @Override public boolean attackEntityFrom(DamageSource source,float amount) {
                // A LivingAttack listener can cause a separate settlement, then cancel the outer attack.
                ((Arena)world).events.apply(this,source,7);
                return false;
            }
        };
        world.entities.add(target);target.setPosition(.5,64,2.5);
        new EmeraldCombat(plan(world)).tick(owner,8,()->true);
        assertEquals(0,target.motionY,0);
    }
    @Test public void ordinaryArrowsCollideWithShaftButPassBetweenColumnsAndAboveSinkingTop() {
        Arena world=new Arena(); Player owner=new Player(world); EmeraldCombat combat=new EmeraldCombat(java.util.Collections.singletonList(plan(world).get(0)));
        Victim enemy=new Victim(world);
        EntityTippedArrow arrow=arrow(world,enemy);
        assertTrue(combat.intercept(owner,arrow,new Vec3d(-3,65,2.5),new Vec3d(3,65,2.5),12)); assertTrue(arrow.isDead);
        arrow=arrow(world,enemy);
        assertFalse(combat.intercept(owner,arrow,new Vec3d(-3,65,3.5),new Vec3d(3,65,3.5),20)); assertFalse(arrow.isDead);
        assertFalse(combat.intercept(owner,arrow,new Vec3d(-3,67,2.5),new Vec3d(3,67,2.5),36));
        assertFalse(combat.intercept(owner,arrow,new Vec3d(-3,65,2.5),new Vec3d(3,65,2.5),11));
        assertFalse(combat.intercept(owner,arrow,new Vec3d(-3,65,2.5),new Vec3d(3,65,2.5),40));
    }
    @Test public void postTickSweepFindsAnArrowThatCrossedTheWholePillarInOneTick() {
        Arena world=new Arena(); Player owner=new Player(world); EntityTippedArrow arrow=arrow(world,new Victim(world));
        arrow.setPosition(3,65,2.5); arrow.lastTickPosX=-3;arrow.lastTickPosY=65;arrow.lastTickPosZ=2.5;
        world.entities.add(arrow); new EmeraldCombat(plan(world)).tick(owner,12,()->true); assertTrue(arrow.isDead);
    }
    @Test public void unknownFriendlyAndPvpProtectedArrowsRemainUntouchedAndWallsWin() {
        Arena world=new Arena(); Player owner=new Player(world); EmeraldCombat combat=new EmeraldCombat(plan(world));
        Vec3d from=new Vec3d(-3,65,2.5),to=new Vec3d(3,65,2.5);
        assertFalse(combat.intercept(owner,arrow(world,null),from,to,12));
        assertFalse(combat.intercept(owner,arrow(world,owner),from,to,12));
        Victim ally=new Victim(world);ally.ally=true;assertFalse(combat.intercept(owner,arrow(world,ally),from,to,12));
        assertFalse(combat.intercept(owner,arrow(world,new Player(world)),from,to,12));
        EntityTippedArrow custom=new EntityTippedArrow(world) {};custom.shootingEntity=new Victim(world);
        assertFalse(combat.intercept(owner,custom,from,to,12));
        EntitySpectralArrow spectral=new EntitySpectralArrow(world);spectral.shootingEntity=new Victim(world);
        assertTrue(combat.intercept(owner,spectral,from,to,12));
        world.blocks.put(new BlockPos(-2,65,2),Blocks.STONE.getDefaultState());
        EntityTippedArrow enemy=arrow(world,new Victim(world));
        assertFalse(combat.intercept(owner,enemy,from,to,12));assertFalse(enemy.isDead);
        world.loaded=false; assertFalse(combat.intercept(owner,enemy,from,to,12));
    }
    @Test public void cancellationDuringDamageStopsLaunchAndLaterTargets() {
        Arena world=new Arena();Player owner=new Player(world);boolean[] active={true};
        Victim first=new Victim(world),second=new Victim(world);
        first.setPosition(.5,64,2.5);second.setPosition(.5,64,2.5);
        world.entities.add(first);world.entities.add(second);
        world.events.damage=event->active[0]=false;
        new EmeraldCombat(plan(world)).tick(owner,8,()->active[0]);
        assertEquals(1,first.hits);assertEquals(0,first.motionY,0);
        assertEquals(0,second.attempts);assertEquals(0,second.motionY,0);
    }
    private static EntityTippedArrow arrow(Arena world,Entity shooter) { EntityTippedArrow arrow=new EntityTippedArrow(world); arrow.shootingEntity=shooter; return arrow; }
    private static List<EmeraldCity.Column> plan(Arena world) { return EmeraldGround.plan(world,new Vec3d(.5,64,.5),new Vec3d(0,0,1)); }
    private static class Player extends EntityPlayer {
        Player(Arena world) { super(world,new GameProfile(UUID.randomUUID(),"EmeraldTester"));setPosition(.5,64,.5); }
        @Override public boolean isSpectator() { return false; }
        @Override public boolean isCreative() { return false; }
    }
    private static class Victim extends EntityCreature {
        int hits,attempts;float damage;boolean boss,ally,cancel,acceptWithoutSettlement;
        Victim(Arena world) { super(world); }
        @Override public boolean isNonBoss() { return !boss; }
        @Override public boolean isOnSameTeam(Entity entity) { return ally; }
        @Override public boolean attackEntityFrom(DamageSource source,float amount) {
            assertTrue(Combat.suppressesKnockback(this)); attempts++;if(cancel)return false;
            if(acceptWithoutSettlement)return true;
            float settled=((Arena)world).events.apply(this,source,amount);
            if(settled>0) {hits++;damage+=settled;}
            // Vanilla can accept the attack even if a later Hurt/Damage event cancels settlement.
            return true;
        }
    }
    private static class Arena extends TestWorld {
        final List<Entity> entities=new ArrayList<>(); int cliffAt=100,stepAt=100,stepHeight;
        final DamageEventFixture events=new DamageEventFixture();
        @Override public IBlockState getBlockState(BlockPos pos) {
            if(!loaded) throw new AssertionError("Unloaded chunk read");
            if(blocks.containsKey(pos))return blocks.get(pos);
            int top=64+(pos.getZ()>=stepAt?stepHeight:0);
            return pos.getZ()<cliffAt && pos.getY()<top?Blocks.STONE.getDefaultState():Blocks.AIR.getDefaultState();
        }
        @Override public List<AxisAlignedBB> getCollisionBoxes(Entity entity,AxisAlignedBB area) {
            List<AxisAlignedBB> result=new ArrayList<>();
            for(BlockPos pos:BlockPos.getAllInBox(new BlockPos(area.minX-1,area.minY-1,area.minZ-1),new BlockPos(area.maxX+1,area.maxY+1,area.maxZ+1))) {
                IBlockState state=getBlockState(pos); AxisAlignedBB box=state.getCollisionBoundingBox(this,pos);
                if(box!=null && box.offset(pos).intersects(area))result.add(box.offset(pos));
            }
            return result;
        }
        @Override public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> type,AxisAlignedBB area,Predicate<? super T> predicate) {
            List<T> result=new ArrayList<>();
            for(Entity entity:entities) if(type.isInstance(entity) && area.intersects(entity.getEntityBoundingBox())) {
                T value=type.cast(entity);if(predicate==null || predicate.apply(value))result.add(value);
            }
            return result;
        }
        @Override public void playSound(EntityPlayer player,double x,double y,double z,SoundEvent sound,SoundCategory category,float volume,float pitch) {}
    }
}
