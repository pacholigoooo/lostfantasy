package dev.lostfantasy.network;

import dev.lostfantasy.core.Spell;
import dev.lostfantasy.data.PlayerData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import static org.junit.Assert.*;

public class SpellStateMessageTest {
    @Test public void namedLearningAndStableSlotsSurviveActualStatePacketEncoding() {
        PlayerData server = new PlayerData();
        server.learn(Spell.GUNGNIR);
        server.learn(Spell.FOUR_OF_A_KIND);
        server.selectSlot(1);
        ByteBuf buffer = Unpooled.buffer();
        try {
            new StateMessage(server.createUpdate(true)).toBytes(buffer);
            StateMessage received = new StateMessage();
            received.fromBytes(buffer);
            PlayerData client = new PlayerData();
            client.applyUpdate(received.data);
            assertArrayEquals(server.preparedSlots(), client.preparedSlots());
            assertTrue(client.knows(Spell.GUNGNIR));
            assertTrue(client.knows(Spell.FOUR_OF_A_KIND));
            assertFalse(client.knows(Spell.ROYAL_FLARE));
            assertSame(Spell.FOUR_OF_A_KIND, client.selectedSpell());
            assertEquals(0, buffer.readableBytes());
            buffer.clear();server.setPower(2);
            new StateMessage(server.createUpdate(false)).toBytes(buffer);
            received=new StateMessage();received.fromBytes(buffer);client.applyUpdate(received.data);
            assertFalse(received.data.hasKey("progress"));assertEquals(2,client.power());
            assertTrue(client.knows(Spell.GUNGNIR));assertSame(Spell.FOUR_OF_A_KIND,client.selectedSpell());
            assertEquals(0,buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test public void equipMessageKeepsSlotAndStableSpellIdSeparate() {
        ByteBuf buffer = Unpooled.buffer();
        try {
            new ActionMessage(ActionMessage.EQUIP_SPELL, 3, 64).toBytes(buffer);
            ActionMessage received = new ActionMessage();
            received.fromBytes(buffer);
            assertEquals(ActionMessage.EQUIP_SPELL, received.action);
            assertEquals(3, received.index);
            assertEquals(64, received.spellId);
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }
}
