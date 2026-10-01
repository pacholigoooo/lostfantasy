package dev.lostfantasy.world;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class LibraryFloorSupportTest {
    private static final int[][] CHESTS = {{-38,-36},{38,-36},{-38,36},{38,36},
            {-9,13},{9,13},{14,-10},{18,63}};
    private static final int[][] LAMPS = {{-14,-11},{-10,-11},{-14,10},{9,12},{16,-6}};
    private final LibraryRuinLayout.Site site = new LibraryRuinLayout.Site(-3, 5, 20, 0, 0x5eedL);

    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Test public void entireMainAndSecretFloorMeetsTheFurnitureLayerForEveryPattern() {
        for (long seed : new long[]{0, 1, -1, 0x5eedL}) {
            LibraryRuinLayout.Site sample = new LibraryRuinLayout.Site(-3, 5, 20, 0, seed);
            int checked = 0;
            for (int x = -52; x <= 52; x++) for (int z = -61; z <= 66; z++) {
                IBlockState floor = LibraryStructure.stateAt(sample, x, 2, z);
                if (floor == null) continue;
                assertTrue("Floor gap below furniture at " + x + "," + z, floor.isFullCube());
                assertEquals(1.0, floor.getBoundingBox(null, BlockPos.ORIGIN).maxY, 0.0);
                assertTrue("Rug and exposed boards must both stay level",
                        floor.getBlock() == Blocks.WOOL || floor.getBlock() == Blocks.PLANKS
                                || floor.getBlock() == Blocks.STONEBRICK);
                checked++;
            }
            assertTrue("Main hall or secret extension was omitted", checked > 12000);
        }
    }

    @Test public void allEightUnmovedChestsHaveSolidFloorAtTheirFeet() {
        for (int[] chest : CHESTS) {
            IBlockState floor = LibraryStructure.stateAt(site, chest[0], 2, chest[1]);
            assertNotNull(floor);
            assertTrue(floor.isFullCube());
        }
    }

    @Test public void deskPapersAndArtifactSitOnFullHeightTabletops() {
        for (int x : new int[]{-13, -11}) {
            IBlockState paper = LibraryStructure.stateAt(site, x, 5, -10);
            IBlockState table = LibraryStructure.stateAt(site, x, 4, -10);
            assertSame(ModBlocks.RESEARCH_NOTES, paper.getBlock());
            assertEquals(1.0, table.getBoundingBox(null, BlockPos.ORIGIN).maxY, 0.0);
        }
        assertTrue(LibraryStructure.stateAt(site, -13, 4, 11).isFullCube());
        assertSame(ModBlocks.ARMILLARY,
                LibraryStructure.stateAt(site, -13, 5, 11).getBlock());
    }

    @Test public void floorLampsAndSofaBackHaveContinuousSupports() {
        for (int[] lamp : LAMPS) {
            for (int y = 2; y <= 5; y++) {
                IBlockState support = LibraryStructure.stateAt(site, lamp[0], y, lamp[1]);
                assertNotNull(support);
                assertNotSame("Floating lamp at " + lamp[0] + "," + y + "," + lamp[1], Blocks.AIR, support.getBlock());
            }
        }
        for (int x = 11; x <= 14; x++) for (int y = 3; y <= 4; y++) {
            assertSame(Blocks.WOOL.getStateFromMeta(10), LibraryStructure.stateAt(site, x, y, -6));
        }
    }

}
