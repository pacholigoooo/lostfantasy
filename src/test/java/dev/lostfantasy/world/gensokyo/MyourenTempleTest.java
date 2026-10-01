package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import java.util.*;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkPrimer;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class MyourenTempleTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.MYOUREN;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void worshipLivingAndCemeterySpacesAreReachableFromTheGate() {
        Generated g=new Generated();GensokyoBlueprint plan=GensokyoStructures.create();
        Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        BlockPos start=g.pos(0,1,96);reached.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(side).up(dy);
                if(!SITE.contains(n.getX(),n.getZ(),5) || n.getY()<SITE.y+1 || n.getY()>SITE.y+14 || reached.contains(n))continue;
                if(!g.solid(n.down()) || g.solid(n) || g.solid(n.up()))continue;
                if(dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(n.up(2)))continue;
                reached.add(n);queue.add(n);break;
            }
        }
        List<String> missing=new ArrayList<>();int count=0;
        for(GensokyoBlueprint.Room room:plan.rooms())if(room.name.startsWith(SITE.title+"·")) {
            count++;if(!reached.contains(new BlockPos(room.x,room.y,room.z)))
                missing.add(room.name+" "+new BlockPos(room.x-SITE.x,room.y-SITE.y,room.z-SITE.z));
        }
        assertTrue(missing.toString(),missing.isEmpty());assertTrue(count>=20);
    }
    @Test public void bedsStorageFiguresAndLampsHaveSupportAndClearance() {
        Generated g=new Generated();int beds=0,chests=0,figures=0,lights=0;
        for(int x=-99;x<=99;x++)for(int z=-87;z<=87;z++)for(int y=1;y<=12;y++) {
            BlockPos p=g.pos(x,y,z);IBlockState s=g.at(p);
            if(s.getBlock()==Blocks.BED) {
                beds++;assertTrue("bed support "+p,g.at(p.down()).isFullCube());assertFalse(g.solid(p.up()));
                EnumFacing direction=s.getValue(BlockBed.FACING);
                assertSame(Blocks.BED,g.at(p.offset(s.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?direction.getOpposite():direction)).getBlock());
            } else if(s.getBlock()==Blocks.CHEST) {
                chests++;assertTrue("chest support "+p,g.at(p.down()).isFullCube());assertFalse("chest lid "+p,g.at(p.up()).isFullCube());
            } else if(s.getBlock()==ModBlocks.JIZO || s.getBlock()==ModBlocks.WRITING_DESK || s.getBlock()==ModBlocks.LIBRARY_LAMP) {
                if(s.getBlock()==ModBlocks.JIZO)figures++;
                assertTrue("furniture support "+p,g.at(p.down()).isFullCube());assertFalse("furniture clearance "+p,g.at(p.up()).isFullCube());
            } else if(s.getBlock()==ModBlocks.RED_LANTERN) {
                lights++;assertTrue("hanging support "+p,g.solid(p.up()));
            } else if(s.getBlock()==Blocks.WALL_SIGN) {
                assertTrue("sign support "+p,g.at(p.offset(s.getValue(BlockHorizontal.FACING).getOpposite())).isFullCube());
            }
        }
        assertEquals("bed halves",14,beds);assertEquals("supply chests",15,chests);
        assertTrue("stone figures: "+figures,figures>=10);assertTrue("hanging lamps: "+lights,lights>=15);
    }
    @Test public void mainRoofIsClosedAndBellRemainsHollow() {
        Generated g=new Generated();
        for(int x=-33;x<=33;x++)for(int z=-36;z<=-4;z++)assertTrue("ceiling "+x+","+z,g.at(g.pos(x,12,z)).isFullCube());
        assertSame(Blocks.AIR,g.at(g.pos(-59,6,7)).getBlock());
        assertSame(Blocks.STAINED_HARDENED_CLAY,g.at(g.pos(-61,6,7)).getBlock());
        assertSame(Blocks.IRON_BARS,g.at(g.pos(-59,10,7)).getBlock());
        assertTrue(g.at(g.pos(-59,12,7)).isFullCube());
    }
    private static final class Generated {
        final GensokyoGenerator generator=new GensokyoGenerator(null,12345);final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        BlockPos pos(int x,int y,int z) {return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
        IBlockState at(BlockPos p) {
            int cx=p.getX()>>4,cz=p.getZ()>>4;
            return chunks.computeIfAbsent(GensokyoAtlas.key(cx,cz),key->generator.primer(cx,cz)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);
        }
        boolean solid(BlockPos p) {
            IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET && s.getBlock()!=Blocks.WALL_SIGN;
        }
    }
}
