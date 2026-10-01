package dev.lostfantasy.combat;

import com.mojang.authlib.GameProfile;
import dev.lostfantasy.TestWorld;
import dev.lostfantasy.entity.EntityCompanion;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class CombatValidityTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Test public void deadOrSpectatorOwnersAndCrossWorldTargetsCannotBeHarmed() {
        TestWorld world = new TestWorld();
        Player owner = new Player(world);
        EntityCompanion target = new EntityCompanion(world);
        assertTrue(Combat.canHarm(owner, target));
        owner.spectator = true;
        assertFalse(Combat.canHarm(owner, target));
        owner.spectator = false;
        owner.isDead = true;
        assertFalse(Combat.canHarm(owner, target));
        owner.isDead = false;
        target.world = new TestWorld();
        assertFalse(Combat.canHarm(owner, target));
        assertFalse(Combat.canHarm(owner, null));
        assertFalse(Combat.canHarm(null, target));
    }

    @Test public void invalidDamageNeverReachesTheEntityOrChangesInvulnerability() {
        TestWorld world = new TestWorld();
        Player owner = new Player(world);
        EntityCompanion target = new EntityCompanion(world);
        target.hurtResistantTime = 8;
        for (float damage : new float[]{0, -1, Float.NaN, Float.POSITIVE_INFINITY, Float.MAX_VALUE}) {
            assertFalse(Combat.spellDamage(owner, target, damage, false));
            assertFalse(EchoCombat.damage(owner, target, damage));
            assertEquals(8, target.hurtResistantTime);
            assertEquals(20, target.getHealth(), 0);
        }
    }

    private static final class Player extends EntityPlayer {
        boolean spectator;
        Player(TestWorld world) { super(world, new GameProfile(new UUID(0, 1), "Tester")); }
        @Override public boolean isSpectator() { return spectator; }
        @Override public boolean isCreative() { return false; }
    }
}
