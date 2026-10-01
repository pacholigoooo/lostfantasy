package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkPrimer;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class BambooGroveTest {
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void stateMetadataAndThinCollisionBoxesSurviveRoundTrip() {
        for(int meta=0;meta<16;meta++) {
            IBlockState s=ModBlocks.BAMBOO_STEM.getStateFromMeta(meta);
            assertEquals(meta,ModBlocks.BAMBOO_STEM.getMetaFromState(s));
            assertFalse(s.isFullCube());assertFalse(s.isOpaqueCube());
            net.minecraft.util.math.AxisAlignedBB box=s.getBoundingBox(null,BlockPos.ORIGIN);
            assertTrue(box.maxX-box.minX<.5);assertTrue(box.maxZ-box.minZ<.5);
            IBlockState leafy=ModBlocks.BAMBOO_FOLIAGE.getStateFromMeta(meta);
            assertEquals(meta,ModBlocks.BAMBOO_FOLIAGE.getMetaFromState(leafy));
            assertEquals(box,leafy.getCollisionBoundingBox(null,BlockPos.ORIGIN));
        }
    }
    @Test public void realGroveIsTallLeafyAndIndependentOfChunkOrder() {
        GensokyoGenerator generator=new GensokyoGenerator(null,12345);int stems=0,leaves=0,tallest=0;
        int cx=1560>>4,cz=1494>>4;
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++) {
            ChunkPrimer first=generator.primer(cx+dx,cz+dz);
            generator.primer(cx+dx+1,cz+dz-1);ChunkPrimer second=generator.primer(cx+dx,cz+dz);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=75;y<150;y++) {
                IBlockState s=first.getBlockState(x,y,z);assertEquals("chunk order",s,second.getBlockState(x,y,z));
                if(s.getBlock()==ModBlocks.BAMBOO_STEM || s.getBlock()==ModBlocks.BAMBOO_FOLIAGE) {stems++;tallest=Math.max(tallest,y);}
                if(s.getBlock()==ModBlocks.BAMBOO_FOLIAGE)leaves++;
                assertNotSame("broadleaf placeholder in bamboo interior",Blocks.LOG,s.getBlock());
            }
        }
        assertTrue("stems "+stems,stems>2000);assertTrue("leafy culms "+leaves,leaves>100);assertTrue(tallest>115);
    }
    @Test public void pathsAndBuildingApproachesHavePlayerHeadroom() {
        GensokyoTerrain terrain=new GensokyoTerrain(12345);GensokyoGenerator generator=new GensokyoGenerator(null,12345);
        for(GensokyoAtlas site:new GensokyoAtlas[]{GensokyoAtlas.MOKOU,GensokyoAtlas.EIENTEI}) {
            int wx=site.x,wz=site.approachZ();ChunkPrimer p=generator.primer(wx>>4,wz>>4);
            int ground=terrain.column(wx,wz).ground;
            for(int y=ground+1;y<=ground+3;y++)assertSame(Blocks.AIR,p.getBlockState(wx&15,y,wz&15).getBlock());
        }
    }
}
