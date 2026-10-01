package dev.lostfantasy.world;

import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExplorationBoundsTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Test public void libraryDiscoveryRejectsSurfaceBufferAndRotatesTheHiddenRoom() {
        for (int rotation = 0; rotation < 4; rotation++) {
            LibraryRuinLayout.Site site = new LibraryRuinLayout.Site(-81, 21, 20, rotation, 91);
            BlockPos center = new BlockPos(site.centerX(), 20, site.centerZ());
            assertTrue(ScarletLibrary.contains(site, center.up(3)));
            assertFalse(ScarletLibrary.contains(site, center.up(37)));
            assertFalse(ScarletLibrary.contains(site, center.up(2)));
            int[] secret = LibraryRuinLayout.toWorld(18, 64, rotation);
            assertTrue(ScarletLibrary.contains(site, center.add(secret[0], 3, secret[1])));
            assertFalse(ScarletLibrary.contains(site, center.add(secret[0], 10, secret[1])));
            int[] buffer = LibraryRuinLayout.toWorld(59, 0, rotation);
            assertFalse(ScarletLibrary.contains(site, center.add(buffer[0], 4, buffer[1])));
        }
    }

}
