package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import java.util.*;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.*;
import org.junit.*;
import static org.junit.Assert.*;

public class SenkaiTest {
    @BeforeClass public static void setup() {GensokyoTestBlocks.register();}
    @Test public void caveAndAllThreeFloorsOfEveryTowerAreWalkable() {
        for(boolean inside:new boolean[]{false,true}) {
            Scene s=new Scene(inside,12345);Set<BlockPos> reached=s.walk();List<String> missing=new ArrayList<>();
            GensokyoBlueprint plan=inside?SenkaiPalace.create():new GensokyoBlueprint();if(!inside)MausoleumEntrance.build(plan);
            for(GensokyoBlueprint.Room room:plan.rooms())if(!reached.contains(new BlockPos(room.x,room.y,room.z)))missing.add(room.name+" "+new BlockPos(room.x-GensokyoAtlas.MYOUREN.x,room.y-GensokyoAtlas.MYOUREN.y,room.z-GensokyoAtlas.MYOUREN.z));
            assertTrue(missing.toString(),missing.isEmpty());assertTrue(reached.contains(SenkaiWorld.gate(inside)));
            assertTrue(reached.contains(inside?SenkaiWorld.arrival():SenkaiWorld.returnPoint()));
            System.out.println((inside?"Senkai":"Old cave")+" reachable spaces: "+plan.rooms().size());
        }
    }
    @Test public void furnishingsHaveSupportHeadroomAndAnApproach() {
        Scene s=new Scene(true,12345);Set<BlockPos> reached=s.walk();int chests=0,beds=0,work=0,lights=0;
        for(int x=-111;x<=111;x++)for(int z=-111;z<=104;z++)for(int y=1;y<=40;y++) {
            BlockPos p=SenkaiWorld.local(x,y,z);IBlockState b=s.at(p);
            if(b.getBlock()==ModBlocks.RED_LANTERN) {lights++;assertTrue("lamp support "+p,s.solid(p.up()));}
            if(b.getBlock()!=Blocks.CHEST && b.getBlock()!=Blocks.BED && b.getBlock()!=Blocks.CRAFTING_TABLE && b.getBlock()!=Blocks.FURNACE)continue;
            assertTrue("support "+p,s.at(p.down()).isFullCube());assertFalse("headroom "+p,s.solid(p.up()));
            boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));assertTrue("access "+p,access);
            if(b.getBlock()==Blocks.CHEST)chests++;else if(b.getBlock()==Blocks.BED) {
                beds++;EnumFacing f=b.getValue(BlockBed.FACING);assertSame(Blocks.BED,s.at(p.offset(b.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?f.getOpposite():f)).getBlock());
            } else work++;
        }
        assertEquals(33,chests);assertEquals(16,beds);assertEquals(4,work);assertTrue(lights>=20);
    }
    @Test public void newChunksRemainDeterministicAndOldCaveHasARockCeiling() {
        for(long seed:new long[]{0,12345,Long.MIN_VALUE})for(boolean inside:new boolean[]{false,true}) {
            Scene s=new Scene(inside,seed);BlockPos gate=SenkaiWorld.gate(inside),arrival=inside?SenkaiWorld.arrival():SenkaiWorld.returnPoint();
            assertTrue(s.walkable(gate));assertTrue(s.walkable(arrival));
            for(int[] at:new int[][]{{-77,-79},{0,-23},{12,-84},{62,35}}) {
                BlockPos p=SenkaiWorld.local(at[0],0,at[1]);int cx=p.getX()>>4,cz=p.getZ()>>4;
                ChunkPrimer first=s.primer(cx,cz);s.primer(cx+1,cz-1);ChunkPrimer second=s.primer(cx,cz);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=20;y<160;y++)assertEquals(first.getBlockState(x,y,z),second.getBlockState(x,y,z));
            }
            if(!inside)for(int x=-30;x<=30;x+=5)for(int z=-50;z<=0;z+=5)assertTrue("covered old cave",s.solid(SenkaiWorld.local(x,-10,z)));
        }
    }
    @Test public void returnLandingsDoNotRetriggerAndLootSurvivesNormalSave() {
        for(boolean inside:new boolean[]{false,true}) {
            BlockPos g=SenkaiWorld.gate(inside),a=inside?SenkaiWorld.arrival():SenkaiWorld.returnPoint();
            assertTrue(SenkaiWorld.doorway(inside,g.getX()+.5,g.getY(),g.getZ()+.5));
            assertFalse(SenkaiWorld.doorway(inside,a.getX()+.5,a.getY(),a.getZ()+.5));
            assertFalse(SenkaiWorld.doorway(inside,g.getX()+.5,g.getY()+3,g.getZ()+.5));
        }
        SenkaiGenerator g=new SenkaiGenerator(new TestWorld(),12345);BlockPos p=SenkaiWorld.local(-60,5,32);
        Chunk c=g.generateChunk(p.getX()>>4,p.getZ()>>4);TileEntityChest chest=(TileEntityChest)c.getTileEntityMap().get(p);assertNotNull(chest);
        NBTTagCompound n=chest.writeToNBT(new NBTTagCompound());assertEquals("lostfantasy:chests/school_supplies",n.getString("LootTable"));
        TileEntityChest copy=new TileEntityChest();copy.readFromNBT(n);assertEquals(n,copy.writeToNBT(new NBTTagCompound()));
    }
    private static final class Scene {
        final boolean inside;final java.util.function.BiFunction<Integer,Integer,ChunkPrimer> generator;
        final Map<Long,ChunkPrimer> cache=new HashMap<>();
        Scene(boolean inside,long seed) {this.inside=inside;generator=inside?new SenkaiGenerator(null,seed)::primer:new GensokyoGenerator(null,seed)::primer;}
        ChunkPrimer primer(int cx,int cz) {return generator.apply(cx,cz);}
        IBlockState at(BlockPos p) {return cache.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->primer(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState b=at(p);return b.getMaterial().blocksMovement() && b.getBlock()!=Blocks.CARPET && b.getBlock()!=Blocks.WALL_SIGN;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && !at(p).getMaterial().isLiquid();}
        Set<BlockPos> walk() {
            Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();BlockPos start=inside?SenkaiWorld.arrival():SenkaiWorld.local(0,1,-78);
            assertTrue(walkable(start));reached.add(start);queue.add(start);
            while(!queue.isEmpty()) {
                BlockPos p=queue.removeFirst();
                for(EnumFacing f:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                    BlockPos n=p.offset(f).up(dy);int x=n.getX()-GensokyoAtlas.MYOUREN.x,y=n.getY()-GensokyoAtlas.MYOUREN.y,z=n.getZ()-GensokyoAtlas.MYOUREN.z;
                    if(Math.abs(x)>(inside?118:47) || z<-119 || z>(inside?108:25) || y<(inside?1:-59) || y>(inside?41:9) || reached.contains(n))continue;
                    if(!walkable(n) || dy>0 && solid(p.up(2)) || dy<0 && solid(n.up(2)))continue;
                    reached.add(n);queue.add(n);break;
                }
            }
            return reached;
        }
    }
}
