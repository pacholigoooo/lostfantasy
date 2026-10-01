package dev.lostfantasy.network;

import dev.lostfantasy.data.PlayerData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.math.BlockPos;
import org.junit.Test;
import static org.junit.Assert.*;

public class ResearchMessagesTest {
    @Test public void requestRetainsFullPositionActionAndChoiceAndRejectsUnknownActions() {
        ByteBuf buffer=Unpooled.buffer();
        try {
            ResearchRequest source=new ResearchRequest(new BlockPos(-123456,25,87654),ResearchRequest.SAMPLE,2);
            source.toBytes(buffer);ResearchRequest read=new ResearchRequest();read.fromBytes(buffer);
            assertEquals(source.pos,read.pos);assertEquals(2,read.action);assertEquals(2,read.choice);assertTrue(read.valid());
            assertEquals(0,buffer.readableBytes());read.action=255;assertFalse(read.valid());read.action=0;read.choice=255;assertFalse(read.valid());
        } finally { buffer.release(); }
    }
    @Test public void replyPreservesProgressFurnitureOrientationAndSeparateDemonstrationKind() {
        for(int kind:new int[]{ResearchMessage.VIEW,ResearchMessage.UPDATE,ResearchMessage.DEMO,ResearchMessage.CLOSE}) {
            ResearchMessage source=new ResearchMessage();source.kind=kind;source.role=1;source.dimension=0;
            source.catalog=5;source.document=2;
            source.pos=new BlockPos(-32,25,123);source.started=999999;source.flags=15;source.turns=3;source.notice=6;source.hasCopy=true;
            ByteBuf buffer=Unpooled.buffer();
            try {
                source.toBytes(buffer);ResearchMessage read=new ResearchMessage();read.fromBytes(buffer);
                assertEquals(kind,read.kind);assertEquals(source.pos,read.pos);assertEquals(source.started,read.started);
                assertEquals(15,read.flags);assertEquals(3,read.turns);assertEquals(6,read.notice);assertEquals(1,read.role);
                assertTrue(read.hasCopy);assertTrue(read.valid());assertEquals(0,buffer.readableBytes());
                assertEquals(5,read.catalog);assertEquals(2,read.document);
                read.document=-1;assertTrue(read.valid());read.document=6;assertFalse(read.valid());read.document=2;
                read.role=9;assertFalse(read.valid());
            } finally { buffer.release(); }
        }
    }
    @Test public void actualPlayerStatePacketIncludesEvidenceAlongsideExistingLearning() {
        PlayerData source=new PlayerData();source.research.discover();source.research.compare(4,1);
        source.learn(dev.lostfantasy.core.Spell.EMERALD_CITY,false);
        ByteBuf buffer=Unpooled.buffer();
        try {
            new StateMessage(source.createUpdate(true)).toBytes(buffer);StateMessage read=new StateMessage();read.fromBytes(buffer);
            PlayerData after=new PlayerData();after.applyUpdate(read.data);
            assertEquals(5,after.research.flags());assertTrue(after.knows(dev.lostfantasy.core.Spell.EMERALD_CITY));
            assertArrayEquals(source.preparedSlots(),after.preparedSlots());
        } finally { buffer.release(); }
    }
}
