package dev.lostfantasy.combat;

import dev.lostfantasy.TestWorld;
import net.minecraft.entity.EntityCreature;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.DamageSource;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class DamageHitResultTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Test public void resultWaitsForTheDamageMethodToExit() {
        EntityCreature victim=new EntityCreature(new TestWorld()) {};
        DamageSource source=new DamageSource("hit");
        try(DamageTransactions.HitResult hit=DamageTransactions.observe(victim,source)) {
            DamageTransactions.Scope scope=DamageTransactions.enter(victim,source);
            try {
                DamageTransactions.capture(20,victim,source);
                DamageTransactions.finish(victim,source,20,(entity,damage,amount)->5);
                assertFalse(hit.confirmed());
            } finally { DamageTransactions.exit(scope); }
            assertTrue(hit.confirmed());
        }
        assertNull(DamageTransactions.enter(victim,source));
    }

    @Test public void anotherVictimOrSourceCannotSupplyTheResult() {
        TestWorld world=new TestWorld();EntityCreature victim=new EntityCreature(world) {},other=new EntityCreature(world) {};
        DamageSource source=new DamageSource("hit");DamageEventFixture events=new DamageEventFixture();
        try(DamageTransactions.HitResult hit=DamageTransactions.observe(victim,source)) {
            events.apply(other,source,20);
            events.apply(victim,new DamageSource("hit"),20);
            assertFalse(hit.confirmed());
            events.apply(victim,source,20);
            assertTrue(hit.confirmed());
        }
    }

    @Test public void nestedSameSourceHitCannotConfirmACancelledOuterHit() {
        EntityCreature victim=new EntityCreature(new TestWorld()) {};DamageSource source=new DamageSource("hit");
        DamageEventFixture outer=new DamageEventFixture(),nested=new DamageEventFixture();
        outer.hurt=event->assertEquals(7,nested.apply(victim,source,7),0);
        outer.damage=event->event.setCanceled(true);
        try(DamageTransactions.HitResult hit=DamageTransactions.observe(victim,source)) {
            assertEquals(0,outer.apply(victim,source,20),0);
            assertFalse(hit.confirmed());
        }
    }

    @Test public void nestedObservedHitRestoresTheOuterObservation() {
        TestWorld world=new TestWorld();EntityCreature victim=new EntityCreature(world) {},other=new EntityCreature(world) {};
        DamageSource source=new DamageSource("outer"),nestedSource=new DamageSource("nested");
        DamageEventFixture outer=new DamageEventFixture(),nested=new DamageEventFixture();
        outer.damage=event->{
            try(DamageTransactions.HitResult hit=DamageTransactions.observe(other,nestedSource)) {
                nested.apply(other,nestedSource,7);
                assertTrue(hit.confirmed());
            }
            event.setAmount(0);
        };
        try(DamageTransactions.HitResult hit=DamageTransactions.observe(victim,source)) {
            outer.apply(victim,source,20);
            assertFalse(hit.confirmed());
        }
        assertNull(DamageTransactions.enter(victim,source));
    }

    @Test public void originalBodySupersedesAnEarlierSameSourceHitFromAnAttackListener() {
        EntityCreature victim=new EntityCreature(new TestWorld()) {};DamageSource source=new DamageSource("hit");
        DamageEventFixture events=new DamageEventFixture();
        try(DamageTransactions.HitResult hit=DamageTransactions.observe(victim,source)) {
            events.apply(victim,source,7);
            assertTrue(hit.confirmed());
            events.hurt=event->event.setCanceled(true);
            events.apply(victim,source,20);
            assertFalse(hit.confirmed());
        }
    }

    @Test public void absentZeroNegativeOrNonfiniteSettlementCannotConfirm() {
        EntityCreature victim=new EntityCreature(new TestWorld()) {};DamageSource source=new DamageSource("hit");
        DamageEventFixture events=new DamageEventFixture();
        try(DamageTransactions.HitResult hit=DamageTransactions.observe(victim,source)) {
            // A hook outside a damageEntity scope cannot stand in for a settled attack.
            DamageTransactions.finish(victim,source,20,(entity,damage,amount)->amount);
            assertFalse(hit.confirmed());
        }
        for(float finalAmount:new float[]{0,-1,Float.NaN,Float.POSITIVE_INFINITY}) {
            events.damage=event->event.setAmount(finalAmount);
            try(DamageTransactions.HitResult hit=DamageTransactions.observe(victim,source)) {
                events.apply(victim,source,20);
                assertFalse(hit.confirmed());
            }
        }
    }
}
