package dev.lostfantasy.world;

import org.junit.Test;

import static org.junit.Assert.*;

public class LibraryRuinLayoutTest {
    @Test public void everyQuarterTurnRoundTripsLocalCoordinates() {
        int[][] points = {
                {0, 0}, {1, 0}, {0, 1}, {-1, -1},
                {LibraryRuinLayout.HALF_WIDTH, LibraryRuinLayout.NORTH_EXTENT},
                {-LibraryRuinLayout.HALF_WIDTH, -LibraryRuinLayout.SOUTH_EXTENT}
        };
        for (int turns = 0; turns < 4; turns++) {
            for (int[] point : points) {
                int[] world = LibraryRuinLayout.toWorld(point[0], point[1], turns);
                int[] local = LibraryRuinLayout.toLocal(world[0], world[1], turns);
                assertArrayEquals("rotation " + turns + " failed for " + point[0] + "," + point[1],
                        point, local);
            }
        }
    }

    @Test public void fourQuarterTurnsReturnToOrigin() {
        int[] point = {-37, 59};
        for (int turn = 0; turn < 4; turn++) {
            point = LibraryRuinLayout.toWorld(point[0], point[1], 1);
        }
        assertArrayEquals(new int[]{-37, 59}, point);
    }

    @Test public void chunkIntersectionIncludesBoundaryChunksOnly() {
        int[][] corners = {
                {-LibraryRuinLayout.HALF_WIDTH, -LibraryRuinLayout.SOUTH_EXTENT},
                {-LibraryRuinLayout.HALF_WIDTH, LibraryRuinLayout.NORTH_EXTENT},
                {LibraryRuinLayout.HALF_WIDTH, -LibraryRuinLayout.SOUTH_EXTENT},
                {LibraryRuinLayout.HALF_WIDTH, LibraryRuinLayout.NORTH_EXTENT}
        };
        for (int turns = 0; turns < 4; turns++) {
            LibraryRuinLayout.Site site = new LibraryRuinLayout.Site(-10, -20, 24, turns, 99L);
            int minOffsetX = Integer.MAX_VALUE, maxOffsetX = Integer.MIN_VALUE;
            int minOffsetZ = Integer.MAX_VALUE, maxOffsetZ = Integer.MIN_VALUE;
            for (int[] corner : corners) {
                int[] world = LibraryRuinLayout.toWorld(corner[0], corner[1], turns);
                minOffsetX = Math.min(minOffsetX, world[0]);
                maxOffsetX = Math.max(maxOffsetX, world[0]);
                minOffsetZ = Math.min(minOffsetZ, world[1]);
                maxOffsetZ = Math.max(maxOffsetZ, world[1]);
            }
            int minChunkX = Math.floorDiv(site.centerX() + minOffsetX, 16);
            int maxChunkX = Math.floorDiv(site.centerX() + maxOffsetX, 16);
            int minChunkZ = Math.floorDiv(site.centerZ() + minOffsetZ, 16);
            int maxChunkZ = Math.floorDiv(site.centerZ() + maxOffsetZ, 16);

            assertTrue(LibraryRuinLayout.intersectsChunk(site, site.chunkX, site.chunkZ));
            assertTrue(LibraryRuinLayout.intersectsChunk(site, minChunkX, minChunkZ));
            assertTrue(LibraryRuinLayout.intersectsChunk(site, maxChunkX, maxChunkZ));
            assertFalse(LibraryRuinLayout.intersectsChunk(site, minChunkX - 1, site.chunkZ));
            assertFalse(LibraryRuinLayout.intersectsChunk(site, maxChunkX + 1, site.chunkZ));
            assertFalse(LibraryRuinLayout.intersectsChunk(site, site.chunkX, minChunkZ - 1));
            assertFalse(LibraryRuinLayout.intersectsChunk(site, site.chunkX, maxChunkZ + 1));
        }
    }

}
