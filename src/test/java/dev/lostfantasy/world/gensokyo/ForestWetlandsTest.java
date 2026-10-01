package dev.lostfantasy.world.gensokyo;

import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkPrimer;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ForestWetlandsTest {
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}

    @Test public void forestHollowsAreShallowClosedBasinsAcrossSeeds() {
        for(long seed:new long[]{0,12345,Long.MIN_VALUE}) {
            GensokyoTerrain t=new GensokyoTerrain(seed);
            for(int[] center:ForestWetlands.HOLLOWS) {
                int water=0,dry=0;
                assertEquals(GensokyoTerrain.Region.FOREST,t.region(center[0],center[1]));
                for(int x=center[0]-96;x<=center[0]+96;x+=2)for(int z=center[1]-96;z<=center[1]+96;z+=2) {
                    GensokyoTerrain.Column c=t.column(x,z);
                    if(!c.wetland)continue;
                    if(!c.wet()) {dry++;continue;}
                    water++;assertTrue("wading depth",c.water-c.ground<=4);
                    for(EnumFacing side:EnumFacing.HORIZONTALS) {
                        GensokyoTerrain.Column next=t.column(x+side.getXOffset(),z+side.getZOffset());
                        assertTrue("water must meet water or a sealed bank: "+seed+" "+x+","+z,
                                next.ground>=c.water || next.wet() && next.water==c.water);
                    }
                    if(c.path())assertTrue("dry bridge",c.roadY>c.water);
                }
                assertTrue("meaningful open water "+Arrays.toString(center),water>300);
                assertTrue("sloped forest bank",dry>300);
            }
        }
    }

    @Test public void generatedShorePlantsHaveRealSupportAndTreesDoNotEraseWater() {
        Generated g=new Generated(12345);int water=0,lilies=0,reeds=0,ferns=0,mushrooms=0;
        for(int x=-742;x<=-568;x++)for(int z=1558;z<=1682;z++) {
            GensokyoTerrain.Column c=g.terrain.column(x,z);if(!c.wetland || c.path())continue;
            BlockPos floor=new BlockPos(x,c.ground,z);
            if(c.wet()) {
                water++;assertSame("uncut still water "+floor,Blocks.WATER,g.at(new BlockPos(x,c.water,z)).getBlock());
                assertSame("no underwater mushrooms "+floor,Blocks.WATER,g.at(floor.up()).getBlock());
                if(g.at(new BlockPos(x,c.water+1,z)).getBlock()==Blocks.WATERLILY)lilies++;
            } else {
                IBlockState s=g.at(floor.up());
                if(s.getBlock()==Blocks.REEDS) {
                    reeds++;assertSame(Blocks.DIRT,g.at(floor).getBlock());
                    boolean neighbour=false;for(EnumFacing side:EnumFacing.HORIZONTALS)neighbour|=g.at(floor.offset(side)).getBlock()==Blocks.WATER;
                    assertTrue("reeds need adjacent actual water "+floor,neighbour);
                }
                if(s.getBlock()==Blocks.TALLGRASS && s.getBlock().getMetaFromState(s)==2)ferns++;
                if(s.getBlock()==Blocks.BROWN_MUSHROOM) {
                    mushrooms++;assertSame(Blocks.DIRT,g.at(floor).getBlock());assertEquals(2,g.at(floor).getBlock().getMetaFromState(g.at(floor)));
                }
            }
        }
        assertTrue(water>3000);assertTrue(lilies>40);assertTrue(reeds>5);assertTrue(ferns>100);assertTrue(mushrooms>30);
        System.out.println("Forest hollow: water="+water+", lily pads="+lilies+", reeds="+reeds+", ferns="+ferns+", mushrooms="+mushrooms);
    }

    @Test public void wetlandChunksAreIdenticalWhenRequestedInReverseOrder() {
        Generated a=new Generated(734901),b=new Generated(734901);
        List<int[]> positions=Arrays.asList(new int[]{-42,99},new int[]{-41,100},new int[]{-40,103},new int[]{-9,109},new int[]{31,91});
        for(int[] p:positions)a.chunk(p[0],p[1]);
        List<int[]> reversed=new ArrayList<>(positions);Collections.reverse(reversed);
        for(int[] p:reversed)b.chunk(p[0],p[1]);
        for(int[] pos:positions) {
            ChunkPrimer first=a.chunk(pos[0],pos[1]),second=b.chunk(pos[0],pos[1]);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<256;y++)assertEquals(first.getBlockState(x,y,z),second.getBlockState(x,y,z));
        }
    }

    private static final class Generated {
        final GensokyoTerrain terrain;final GensokyoGenerator generator;final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        Generated(long seed) {terrain=new GensokyoTerrain(seed);generator=new GensokyoGenerator(null,seed);}
        ChunkPrimer chunk(int x,int z) {return chunks.computeIfAbsent(GensokyoAtlas.key(x,z),k->generator.primer(x,z));}
        IBlockState at(BlockPos p) {return chunk(p.getX()>>4,p.getZ()>>4).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
    }
}
