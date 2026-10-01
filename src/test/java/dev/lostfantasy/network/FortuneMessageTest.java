package dev.lostfantasy.network;

import io.netty.buffer.*;
import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;

public class FortuneMessageTest {
    @Test public void packetPreservesPlayerDimensionTimeAndEitherHand() {
        for(int side:new int[]{-1,1}) {
            FortuneMessage source=new FortuneMessage();source.playerId=UUID.randomUUID();source.ribbonId=UUID.randomUUID();
            source.dimension=-23;source.started=9000000001L;source.handSide=side;
            ByteBuf bytes=Unpooled.buffer();
            try {
                source.toBytes(bytes);FortuneMessage copy=new FortuneMessage();copy.fromBytes(bytes);
                assertEquals(source.playerId,copy.playerId);assertEquals(-23,copy.dimension);
                assertEquals(source.ribbonId,copy.ribbonId);
                assertEquals(source.started,copy.started);assertEquals(side,copy.handSide);
                assertTrue(copy.valid());assertEquals(0,bytes.readableBytes());
                copy.handSide=0;assertFalse(copy.valid());copy.handSide=1;copy.started=-1;assertFalse(copy.valid());
            } finally {bytes.release();}
        }
    }
}
