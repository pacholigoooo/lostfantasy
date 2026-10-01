package dev.lostfantasy.world;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import net.minecraft.block.BlockBush;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.BlockFluidBase;
import org.junit.Test;
import static org.junit.Assert.*;

public class HiganFlowerTest {
    @Test public void floatingFlowersNeedStillSanzuWaterAndDoNotBlockPassage() {
        Bootstrap.register();TestWorld w=new TestWorld() {
            @Override public int getLightFromNeighbors(BlockPos p) {return 15;}
        };
        BlockPos pos=new BlockPos(2,63,2);BlockBush lily=(BlockBush)ModBlocks.FLOATING_LILY;
        w.blocks.put(pos.down(),ModBlocks.SANZU_WATER.getDefaultState());
        assertTrue(lily.canBlockStay(w,pos,lily.getDefaultState()));
        assertNull(lily.getCollisionBoundingBox(lily.getDefaultState(),w,pos));
        w.blocks.put(pos.down(),ModBlocks.SANZU_WATER.getDefaultState().withProperty(BlockFluidBase.LEVEL,3));
        assertFalse(lily.canBlockStay(w,pos,lily.getDefaultState()));
        for(net.minecraft.block.Block other:new net.minecraft.block.Block[]{Blocks.AIR,Blocks.WATER,Blocks.DIRT}) {
            w.blocks.put(pos.down(),other.getDefaultState());assertFalse(lily.canBlockStay(w,pos,lily.getDefaultState()));
        }
    }
}
