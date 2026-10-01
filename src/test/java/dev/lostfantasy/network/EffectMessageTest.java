package dev.lostfantasy.network;

import io.netty.buffer.ByteBuf;
import dev.lostfantasy.core.Spell;
import io.netty.buffer.Unpooled;
import net.minecraft.util.math.Vec3d;
import org.junit.Test;
import static org.junit.Assert.*;

public class EffectMessageTest {
    @Test public void effectsExpireAtTheirEndAndCannotLeakIntoAnotherDimension() {
        EffectMessage effect = new EffectMessage(0, 1, 734, 100, 90, 25, Vec3d.ZERO, Vec3d.ZERO, 10);
        assertTrue(effect.retainedAt(734, 100));
        assertTrue(effect.retainedAt(734, 189));
        assertFalse(effect.retainedAt(734, 190));
        assertFalse(effect.retainedAt(735, 110));
        assertTrue(effect.retainedAt(734, 60));
        assertFalse(effect.retainedAt(734, 59));
        effect.duration = 0;
        assertFalse(effect.retainedAt(734, 100));
    }

    @Test public void castYawAndExistingEffectFieldsSurviveThePacket() {
        for (int kind : new int[]{EffectMessage.STOP, EffectMessage.ORDINARY_BEAM, 0, 1, 2, 4, 64}) {
            EffectMessage sent = new EffectMessage(kind, 17, 735, 12345, 90, 25,
                    new Vec3d(3.2, 65, -7), new Vec3d(.6, 0, .8), 12);
            sent.castYaw = 179.5f;
            ByteBuf buffer = Unpooled.buffer();
            try {
                sent.toBytes(buffer);
                EffectMessage received = new EffectMessage();
                received.fromBytes(buffer);
                assertEquals(kind, received.kind);
                assertEquals(sent.caster, received.caster);
                assertEquals(sent.dimension, received.dimension);
                assertEquals(sent.started, received.started);
                assertEquals(sent.duration, received.duration);
                assertEquals(sent.charge, received.charge);
                assertEquals(sent.x, received.x, 0);
                assertEquals(sent.y, received.y, 0);
                assertEquals(sent.z, received.z, 0);
                assertEquals(sent.dx, received.dx, 0);
                assertEquals(sent.dy, received.dy, 0);
                assertEquals(sent.dz, received.dz, 0);
                assertEquals(sent.radius, received.radius, 0);
                assertEquals(sent.castYaw, received.castYaw, 0);
                assertEquals(0, buffer.readableBytes());
            } finally {
                buffer.release();
            }
        }
    }

    @Test public void ordinaryBeamCannotCollideWithAFifthSpellOrBecomeAStopMessage() {
        assertTrue(EffectMessage.ORDINARY_BEAM < 0);
        assertNotEquals(EffectMessage.STOP, EffectMessage.ORDINARY_BEAM);
        EffectMessage beam = new EffectMessage();
        beam.kind = EffectMessage.ORDINARY_BEAM;
        assertNull(beam.spell());
        assertFalse(beam.hasCastingPose());
        EffectMessage flare = new EffectMessage();
        flare.kind = Spell.ROYAL_FLARE.networkId;
        assertSame(Spell.ROYAL_FLARE, flare.spell());
        assertTrue(flare.hasCastingPose());
    }
}
