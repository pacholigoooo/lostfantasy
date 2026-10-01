package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import java.util.*;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class MoriyaShrineTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.MORIYA;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void allRoomsStoreyAndLakePierCanBeReachedFromMountainPath() {
        Generated g=new Generated();GensokyoBlueprint plan=GensokyoStructures.create();
        Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        BlockPos start=new BlockPos(SITE.x,SITE.y+1,SITE.approachZ());reached.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(side).up(dy);
                if(n.getX()<SITE.x-111 || n.getX()>SITE.x+210 || Math.abs(n.getZ()-SITE.z)>126 || n.getY()<SITE.y-5 || n.getY()>SITE.y+26 || reached.contains(n))continue;
                if(!g.solid(n.down()) || g.solid(n) || g.solid(n.up()))continue;
                if(dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(n.up(2)))continue;
                reached.add(n);queue.add(n);break;
            }
        }
        List<String> missing=new ArrayList<>();int count=0;
        for(GensokyoBlueprint.Room room:plan.rooms())if(room.name.startsWith(SITE.title+"·")) {
            count++;if(!reached.contains(new BlockPos(room.x,room.y,room.z)))missing.add(room.name+" "+new BlockPos(room.x-SITE.x,room.y-SITE.y,room.z-SITE.z));
        }
        assertTrue(missing.toString(),missing.isEmpty());assertTrue(count>=18);
    }
    @Test public void turnedBedsStorageSignsAndFurnitureAreSupported() {
        Generated g=new Generated();int beds=0,chests=0,ropes=0,signs=0;
        for(int x=-85;x<=85;x++)for(int z=-85;z<=85;z++)for(int y=1;y<=27;y++) {
            BlockPos p=new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);IBlockState s=g.at(p);
            if(s.getBlock()==Blocks.BED) {
                beds++;assertTrue("bed support "+p,g.at(p.down()).isFullCube());assertFalse(g.solid(p.up()));
                EnumFacing f=s.getValue(BlockBed.FACING);assertEquals(EnumFacing.WEST,f);
                IBlockState pair=g.at(p.offset(s.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?f.getOpposite():f));
                assertSame(Blocks.BED,pair.getBlock());assertNotEquals(s.getValue(BlockBed.PART),pair.getValue(BlockBed.PART));
            } else if(s.getBlock()==Blocks.CHEST) {
                chests++;assertTrue("chest support "+p,g.at(p.down()).isFullCube());assertFalse("chest lid "+p,g.at(p.up()).isFullCube());
                assertEquals(EnumFacing.EAST,s.getValue(BlockHorizontal.FACING));
            } else if(s.getBlock()==ModBlocks.WRITING_DESK || s.getBlock()==ModBlocks.LIBRARY_LAMP || s.getBlock()==ModBlocks.OUTSIDE_TELEVISION) {
                assertTrue("furniture support "+p,g.at(p.down()).isFullCube());assertFalse("furniture clearance "+p,g.at(p.up()).isFullCube());
            } else if(s.getBlock()==ModBlocks.RED_LANTERN)assertTrue("lantern support "+p,g.solid(p.up()));
            else if(s.getBlock()==Blocks.WALL_SIGN) {
                signs++;assertTrue("sign backing "+p,g.at(p.offset(s.getValue(BlockHorizontal.FACING).getOpposite())).isFullCube());
            } else if(s.getBlock()==ModBlocks.SHRINE_ROPE)ropes++;
        }
        assertEquals("bed halves",6,beds);assertTrue("chests "+chests,chests>=18);assertEquals(1,signs);assertTrue(ropes>=90);
    }
    @Test public void rotatedContainerAndSignMetadataInstallInTheirGeneratedChunks() {
        GensokyoBlueprint plan=GensokyoStructures.create();Generated g=new Generated();TestWorld world=new TestWorld();int boxes=0,labels=0;
        for(int cx=(SITE.x-85)>>4;cx<=(SITE.x+85)>>4;cx++)for(int cz=(SITE.z-85)>>4;cz<=(SITE.z+85)>>4;cz++) {
            ChunkPrimer primer=g.chunk(cx,cz);Chunk chunk=new Chunk(world,primer,cx,cz);plan.installContainers(chunk,12345);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=SITE.y+1;y<=SITE.y+27;y++) {
                IBlockState s=primer.getBlockState(x,y,z);BlockPos p=new BlockPos((cx<<4)+x,y,(cz<<4)+z);
                if(s.getBlock()==Blocks.CHEST) {
                    boxes++;assertTrue("missing chest metadata "+p,chunk.getTileEntityMap().get(p) instanceof TileEntityChest);
                    NBTTagCompound nbt=chunk.getTileEntityMap().get(p).writeToNBT(new NBTTagCompound());assertTrue(nbt.hasKey("LootTable"));
                } else if(s.getBlock()==Blocks.WALL_SIGN) {
                    labels++;assertEquals("守矢神社",((TileEntitySign)chunk.getTileEntityMap().get(p)).signText[1].getUnformattedText());
                }
            }
        }
        assertTrue(boxes>=18);assertEquals(1,labels);
        // Ordinary water remains in the lake, under the raised walking deck.
        assertSame(Blocks.WATER,g.at(new BlockPos(SITE.x+195,173,SITE.z)).getBlock());
        assertTrue(g.at(new BlockPos(SITE.x+195,175,SITE.z)).isFullCube());
    }
    private static final class Generated {
        final GensokyoGenerator generator=new GensokyoGenerator(null,12345);final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        ChunkPrimer chunk(int cx,int cz) {return chunks.computeIfAbsent(GensokyoAtlas.key(cx,cz),key->generator.primer(cx,cz));}
        IBlockState at(BlockPos p) {return chunk(p.getX()>>4,p.getZ()>>4).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {
            IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET && s.getBlock()!=Blocks.WALL_SIGN;
        }
    }
}
