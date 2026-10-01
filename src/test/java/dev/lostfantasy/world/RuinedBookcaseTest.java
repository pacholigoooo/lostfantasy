package dev.lostfantasy.world;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.ModItems;
import dev.lostfantasy.block.RuinedBookcase;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class RuinedBookcaseTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Test public void allMetadataStatesRoundTripAndRotate() {
        for (int meta = 0; meta < 16; meta++) {
            IBlockState state = ModBlocks.RUINED_BOOKCASE.getStateFromMeta(meta);
            assertEquals(meta, ModBlocks.RUINED_BOOKCASE.getMetaFromState(state));
            IBlockState rotated = state;
            for (int turn = 0; turn < 4; turn++) rotated = rotated.withRotation(Rotation.CLOCKWISE_90);
            assertSame(state, rotated);
        }
    }

    @Test public void intactSidesOccludeNeighboursButOpenFrontAndBrokenSidesDoNot() {
        for (int meta = 0; meta < 16; meta++) {
            IBlockState state = ModBlocks.RUINED_BOOKCASE.getStateFromMeta(meta);
            for (EnumFacing face : EnumFacing.values()) {
                boolean expected = state.getValue(RuinedBookcase.DAMAGE) != RuinedBookcase.Damage.COLLAPSED
                        && face != state.getValue(BlockHorizontal.FACING)
                        && (state.getValue(RuinedBookcase.DAMAGE) != RuinedBookcase.Damage.DOUBLE_SIDED
                        || face != state.getValue(BlockHorizontal.FACING).getOpposite());
                assertEquals(expected, ModBlocks.RUINED_BOOKCASE.doesSideBlockRendering(state, null, BlockPos.ORIGIN, face));
            }
        }
    }

    @Test public void ruinBlocksHaveTheirOwnCreativeTab() {
        assertNotSame(ModItems.TAB, ModBlocks.TAB);
        assertEquals("lostfantasy.ruins", ModBlocks.TAB.getTabLabel());
        for (net.minecraft.block.Block block : ModBlocks.ALL.values()) assertSame(ModBlocks.TAB, block.getCreativeTab());
    }
}
