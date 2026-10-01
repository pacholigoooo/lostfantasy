package dev.lostfantasy.world.gensokyo;

import java.util.*;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkPrimer;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class MountainCascadeTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.WATERFALL;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void lakeOutletAndCataractFeedTheClosedPool() {
        for(long seed:new long[]{0,17,12345,Long.MIN_VALUE}) {
            GensokyoTerrain t=new GensokyoTerrain(seed);int previous=173,drops=0;
            // Follow the incoming river into the basin, ending before its closed southern rim.
            for(int z=-160;z<=45;z++) {
                int x=(int)Math.round(MountainCascade.centreX(z));
                GensokyoTerrain.Column c=t.column(SITE.x+x,SITE.z+z);
                assertTrue("dry flow "+seed+" at "+x+","+z,c.wet());
                assertTrue("uphill flow "+z+": "+previous+" -> "+c.water,c.water<=previous);
                if(previous-c.water>10)drops++;previous=c.water;
            }
            assertEquals("one primary cataract",1,drops);
            assertEquals(MountainCascade.POOL_WATER,previous);
            GensokyoTerrain.Column rim=t.column(SITE.x-8,SITE.z+73);
            assertFalse("pool must have no southern outlet",rim.wet());
            assertTrue("closed rim must hold the water",rim.ground>=MountainCascade.POOL_WATER);
        }
    }
    @Test public void generatedCurtainUsesVanillaFallingWaterAcrossChunkSeams() {
        GensokyoGenerator generator=new GensokyoGenerator(null,12345);GensokyoTerrain terrain=new GensokyoTerrain(12345);
        int tallColumns=0,fallingBlocks=0;
        for(int cx=(SITE.x-37)>>4;cx<=(SITE.x+37)>>4;cx++)for(int cz=(SITE.z-51)>>4;cz<=(SITE.z-18)>>4;cz++) {
            ChunkPrimer first=generator.primer(cx,cz);generator.primer(cx+1,cz-1);ChunkPrimer again=generator.primer(cx,cz);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
                GensokyoTerrain.Column c=terrain.column((cx<<4)+x,(cz<<4)+z);
                if(!c.falling)continue;
                if(c.water-c.ground>=25)tallColumns++;
                for(int y=c.ground+1;y<c.water;y++) {
                    IBlockState s=first.getBlockState(x,y,z);assertSame(Blocks.WATER,s.getBlock());
                    assertEquals(8,(int)s.getValue(BlockLiquid.LEVEL));assertEquals(s,again.getBlockState(x,y,z));fallingBlocks++;
                }
            }
        }
        assertTrue("cataract too narrow",tallColumns>=80);assertTrue(fallingBlocks>=3500);
    }
    @Test public void mountainRouteAndViewpointRemainWalkableOnGeneratedTerrain() {
        Generated g=new Generated();Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        BlockPos start=new BlockPos(SITE.x-180,140,SITE.z+100);queue.add(start);reached.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing f:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(f).up(dy);
                if(n.getX()<SITE.x-194 || n.getX()>SITE.x-64 || n.getZ()<SITE.z-144 || n.getZ()>SITE.z+108 || n.getY()<135 || n.getY()>184 || reached.contains(n))continue;
                if(!g.solid(n.down()) || g.solid(n) || g.solid(n.up()))continue;
                if(dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(n.up(2)))continue;
                reached.add(n);queue.add(n);break;
            }
        }
        assertTrue("upper path",reached.contains(new BlockPos(SITE.x-170,178,SITE.z-140)));
        assertTrue("viewpoint",reached.contains(new BlockPos(SITE.x-76,152,SITE.z+20)));
        // A clear line of sight to the middle of the water curtain, not just an accessible balcony.
        for(int n=0;n<30;n++) {
            BlockPos p=new BlockPos(SITE.x-61+n*1.8,154+n*.3,SITE.z+20-n*1.5);
            assertFalse("blocked view "+p,g.solid(p));
        }
    }
    @Test public void upperSourcesHaveLateralRockBanksInsteadOfDryShelves() {
        GensokyoTerrain terrain=new GensokyoTerrain(12345);
        for(int z=-100;z<-25;z++)for(int x=-65;x<=65;x++) {
            GensokyoTerrain.Column c=terrain.column(SITE.x+x,SITE.z+z);
            if(!c.wet() || c.water<170)continue;
            for(int dx:new int[]{-1,1}) {
                GensokyoTerrain.Column neighbor=terrain.column(SITE.x+x+dx,SITE.z+z);
                if(!neighbor.wet())assertTrue("low lateral bank "+x+","+z,neighbor.ground>=c.water);
            }
        }
    }
    private static final class Generated {
        final GensokyoGenerator generator=new GensokyoGenerator(null,12345);final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        IBlockState at(BlockPos p) {return chunks.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.primer(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {return at(p).getMaterial().blocksMovement();}
    }
}
