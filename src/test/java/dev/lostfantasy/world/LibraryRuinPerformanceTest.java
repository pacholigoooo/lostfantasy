package dev.lostfantasy.world;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Blocks;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Deterministic operation-count guards for the library world-generation hot path. */
public class LibraryRuinPerformanceTest {
    private static final int CHUNK_SIZE = 16;

    @Test public void chunkClipLimitsWorkToTheStructureFootprint() {
        for (int turns = 0; turns < 4; turns++) {
            LibraryRuinLayout.Site site = new LibraryRuinLayout.Site(0, 0, 24, turns, 0x5eedL);
            int intersectingChunks = 0;
            for (int chunkX = -32; chunkX <= 32; chunkX++) {
                for (int chunkZ = -32; chunkZ <= 32; chunkZ++) {
                    if (LibraryRuinLayout.intersectsChunk(site, chunkX, chunkZ)) intersectingChunks++;
                }
            }

            assertEquals("The rotated structure AABB should touch only chunks containing planned cells",
                    63, intersectingChunks);
            assertTrue("Chunk clipping must reject at least 98% of a 65x65 candidate window",
                    intersectingChunks * 50 < 65 * 65);
        }
    }

    @Test public void nonAirPlanRemainsSparseInsideClippedChunks() {
        Bootstrap.register();
        LibraryRuinLayout.Site site = new LibraryRuinLayout.Site(0, 0, 24, 0, 0x5eedL);
        long clippedVolumeCells = 0;
        long plannedCells = 0;
        long nonAirCells = 0;
        int chunksWithPlannedCells = 0;

        for (int chunkX = -8; chunkX <= 8; chunkX++) {
            for (int chunkZ = -8; chunkZ <= 8; chunkZ++) {
                if (!LibraryRuinLayout.intersectsChunk(site, chunkX, chunkZ)) continue;
                long beforeChunk = plannedCells;
                int minX = chunkX << 4;
                int minZ = chunkZ << 4;
                for (int worldX = minX; worldX < minX + CHUNK_SIZE; worldX++) {
                    for (int worldZ = minZ; worldZ < minZ + CHUNK_SIZE; worldZ++) {
                        int[] local = LibraryRuinLayout.toLocal(
                                worldX - site.centerX(), worldZ - site.centerZ(), site.turns);
                        for (int y = 0; y <= LibraryRuinLayout.HEIGHT; y++) {
                            clippedVolumeCells++;
                            IBlockState state = LibraryStructure.stateAt(site, local[0], y, local[1]);
                            if (state == null) continue;
                            plannedCells++;
                            if (state.getBlock() != Blocks.AIR) nonAirCells++;
                        }
                    }
                }
                if (plannedCells > beforeChunk) chunksWithPlannedCells++;
            }
        }

        assertEquals("The clipped volume budget changed; review world-generation cost deliberately",
                63L * CHUNK_SIZE * CHUNK_SIZE * (LibraryRuinLayout.HEIGHT + 1), clippedVolumeCells);
        assertEquals("Every intersecting chunk should contain planned cells",
                63, chunksWithPlannedCells);
        assertEquals("The deterministic plan-write budget changed",
                471697L, plannedCells);
        assertTrue("Solid writes should stay below the sparse 142k-cell budget",
                nonAirCells < 142000L);
    }

    @Test public void shelfPlanPreservesTheDenseBookWall() {
        Bootstrap.register();
        LibraryRuinLayout.Site site = new LibraryRuinLayout.Site(0, 0, 24, 0, 0x5eedL);
        int shelfWrites = 0;
        for (int x = -LibraryRuinLayout.HALF_WIDTH; x <= LibraryRuinLayout.HALF_WIDTH; x++) {
            for (int z = -LibraryRuinLayout.SOUTH_EXTENT; z <= LibraryRuinLayout.NORTH_EXTENT; z++) {
                for (int y = 0; y <= LibraryRuinLayout.HEIGHT; y++) {
                    IBlockState state = LibraryStructure.stateAt(site, x, y, z);
                    if (state != null && (state.getBlock() == ModBlocks.RUINED_BOOKCASE
                            || state.getBlock() == Blocks.BOOKSHELF)) shelfWrites++;
                }
            }
        }

        System.out.println("Dense library shelves: " + shelfWrites);
        assertTrue("Performance work must preserve the dense, three-storey book wall",
                shelfWrites >= 25000);
        assertTrue("Damage and gaps should still be present in the full book wall",
                shelfWrites <= 38304);
    }

    @Test public void outerMasonryShellIsOneBlockThickOnStraightSides() {
        Bootstrap.register();
        LibraryRuinLayout.Site site = new LibraryRuinLayout.Site(0, 0, 24, 0, 0x5eedL);
        for (int y : new int[]{3, 10, 20, 30}) {
            IBlockState edge = LibraryStructure.stateAt(site, 52, y, 0);
            IBlockState inward = LibraryStructure.stateAt(site, 51, y, 0);
            assertTrue("The outer edge must remain a protective masonry shell at y=" + y,
                    edge != null && isMasonry(edge));
            assertFalse("The wall regressed to multiple solid layers at y=" + y,
                    inward != null && isMasonry(inward));
        }
    }

    private static boolean isMasonry(IBlockState state) {
        return state.getBlock() == Blocks.STONEBRICK
                || state.getBlock() == Blocks.COBBLESTONE
                || state.getBlock() == Blocks.MONSTER_EGG;
    }
}
