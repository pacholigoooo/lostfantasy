package dev.lostfantasy.world.gensokyo;

import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ScarletMansionTest {
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void everyRoomAndTheLibraryCanBeReachedFromTheFrontGate() {
        GensokyoAtlas site=GensokyoAtlas.SCARLET;
        final int radius=119,width=radius*2+1,bottom=site.y-51,height=116;
        final int layer=width*width,total=layer*height;
        BitSet solid=new BitSet(total),reached=new BitSet(total);
        GensokyoGenerator generator=new GensokyoGenerator(null,12345);
        for(int cx=(site.x-radius)>>4;cx<=(site.x+radius)>>4;cx++)for(int cz=(site.z-radius)>>4;cz<=(site.z+radius)>>4;cz++) {
            ChunkPrimer chunk=generator.primer(cx,cz);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
                int lx=(cx<<4)+x-site.x+radius,lz=(cz<<4)+z-site.z+radius;
                if(lx<0 || lx>=width || lz<0 || lz>=width)continue;
                for(int y=0;y<height;y++) {
                    IBlockState state=chunk.getBlockState(x,bottom+y,z);
                    if(state.getMaterial().blocksMovement() && state.getBlock()!=Blocks.CARPET)
                        solid.set(y*layer+lz*width+lx);
                }
            }
        }
        int start=(site.y+1-bottom)*layer+(radius+115)*width+radius;
        int[] queue=new int[total];int head=0,tail=0;queue[tail++]=start;reached.set(start);
        while(head<tail) {
            int p=queue[head++],y=p/layer,z=p%layer/width,x=p%width;
            for(int[] dir:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})for(int dy:new int[]{0,1,-1}) {
                int nx=x+dir[0],nz=z+dir[1],ny=y+dy;
                if(nx<0 || nx>=width || nz<0 || nz>=width || ny<1 || ny>=height-2)continue;
                int n=ny*layer+nz*width+nx;
                if(reached.get(n) || solid.get(n) || solid.get(n+layer) || !solid.get(n-layer))continue;
                if(dy>0 && solid.get(p+2*layer) || dy<0 && solid.get(n+2*layer))continue;
                reached.set(n);queue[tail++]=n;break;
            }
        }
        List<String> missing=new ArrayList<>();int rooms=0;
        for(GensokyoBlueprint.Room room:GensokyoStructures.create().rooms())if(site.contains(room.x,room.z,0)) {
            rooms++;int x=room.x-site.x,z=room.z-site.z;
            if(!reached.get((room.y-bottom)*layer+(radius+z)*width+radius+x))
                missing.add(room.name+" ("+x+","+(room.y-site.y)+","+z+")");
        }
        // Walking aisle beside the research desk, using the library's actual chunk-aligned centre.
        dev.lostfantasy.world.LibraryRuinLayout.Site library=dev.lostfantasy.world.ScarletLibrary.site(12345);
        int tableAisle=(library.baseY+4-bottom)*layer+(radius+library.centerZ()-site.z)*width+radius+library.centerX()-site.x+1;
        if(!reached.get(tableAisle))missing.add("图书馆研究桌旁座位");
        assertTrue("Rooms checked: "+rooms+"; unreachable: "+missing,missing.isEmpty());
        assertTrue(rooms>70);
    }
    @Test public void mansionChestsAndBedsHaveFloorsAndChestLidsHaveClearance() {
        GensokyoAtlas site=GensokyoAtlas.SCARLET;GensokyoBlueprint plan=GensokyoStructures.create();
        int chests=0,beds=0;
        for(long key:plan.chunks()) {
            int cx=(int)(key>>32),cz=(int)key;
            if(!site.contains((cx<<4)+8,(cz<<4)+8,16))continue;
            ChunkPrimer p=new ChunkPrimer();plan.paint(p,cx,cz);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=site.y-10;y<site.y+33;y++) {
                IBlockState state=p.getBlockState(x,y,z);
                if(state.getBlock()!=Blocks.CHEST && state.getBlock()!=Blocks.BED)continue;
                String where=((cx<<4)+x-site.x)+","+(y-site.y)+","+((cz<<4)+z-site.z);
                assertTrue("Unsupported furniture "+where,p.getBlockState(x,y-1,z).isFullCube());
                if(state.getBlock()==Blocks.CHEST) {chests++;assertFalse("Chest blocked "+where,p.getBlockState(x,y+1,z).isFullCube());}
                else {
                    beds++;
                    net.minecraft.util.EnumFacing facing=state.getValue(net.minecraft.block.BlockBed.FACING);
                    boolean head=state.getValue(net.minecraft.block.BlockBed.PART)==net.minecraft.block.BlockBed.EnumPartType.HEAD;
                    net.minecraft.util.math.BlockPos mate=new net.minecraft.util.math.BlockPos((cx<<4)+x,y,(cz<<4)+z)
                            .offset(head?facing.getOpposite():facing);
                    IBlockState other=plan.at(mate.getX(),mate.getY(),mate.getZ());
                    assertSame("Missing bed half "+where,Blocks.BED,other.getBlock());
                    assertEquals(facing,other.getValue(net.minecraft.block.BlockBed.FACING));
                    assertNotEquals(state.getValue(net.minecraft.block.BlockBed.PART),other.getValue(net.minecraft.block.BlockBed.PART));
                }
            }
        }
        assertTrue(chests>50);assertTrue(beds>40);
    }
}
