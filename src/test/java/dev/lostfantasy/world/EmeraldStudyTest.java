package dev.lostfantasy.world;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import dev.lostfantasy.block.LibraryDecoration;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class EmeraldStudyTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }
    @Test public void generatedPageAndSupportMatchLearningCoordinatesAtEveryRotation() {
        for(int turns=0;turns<4;turns++) {
            LibraryRuinLayout.Site site=new LibraryRuinLayout.Site(-7,9,20,turns,123);
            BlockPos pos=EmeraldStudy.position(site);
            int[] local=LibraryRuinLayout.toLocal(pos.getX()-site.centerX(),pos.getZ()-site.centerZ(),turns);
            assertArrayEquals(new int[]{-2,0},local);assertEquals(25,pos.getY());
            IBlockState planned=LibraryStructure.stateAt(site,LibraryStructure.columnAt(-2,0),5);
            assertSame(ModBlocks.EMERALD_STUDY,planned.getBlock());
            assertSame(Blocks.STONE_SLAB.getStateFromMeta(15),LibraryStructure.stateAt(site,LibraryStructure.columnAt(-2,0),4));
        }
    }
}
