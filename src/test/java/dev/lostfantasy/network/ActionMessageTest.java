package dev.lostfantasy.network;

import org.junit.Test;
import static org.junit.Assert.assertNull;

public class ActionMessageTest {
    @Test public void removedSummonPacketsAreDiscardedBeforeAccessingPlayerOrSchedulingWork() {
        ActionMessage.Handler handler = new ActionMessage.Handler();
        // These retired IDs must never dispatch an action, even if sent by an old client.
        assertNull(handler.onMessage(new ActionMessage(5, 0, 0), null));
        assertNull(handler.onMessage(new ActionMessage(6, 0, 0), null));
    }
}
