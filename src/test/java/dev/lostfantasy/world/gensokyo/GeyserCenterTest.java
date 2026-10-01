package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.TestWorld;
import java.util.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class GeyserCenterTest {
    private static Generated generated;private static Set<BlockPos> reached;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    private static void walk() {
        if(generated!=null)return;generated=new Generated(12345);reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        BlockPos start=GeyserCenter.local(0,1,52);assertTrue(generated.walkable(start));reached.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(side).up(dy);int x=n.getX()-GeyserCenter.SITE.x,z=n.getZ()-GeyserCenter.SITE.z,y=n.getY()-GeyserCenter.SITE.y;
                if(Math.abs(x)>59 || z<-48 || z>54 || y<GeyserCenter.BOTTOM || y>27 || reached.contains(n))continue;
                if(!generated.walkable(n) || dy>0 && generated.solid(p.up(2)) || dy<0 && generated.solid(n.up(2)))continue;
                reached.add(n);queue.add(n);break;
            }
        }
    }
    @Test public void surfaceRoomsAndWholeShaftConnectByWalking() {
        walk();GensokyoBlueprint p=new GensokyoBlueprint();GeyserCenter.build(p);p.seal();List<String> missing=new ArrayList<>();
        for(GensokyoBlueprint.Room room:p.rooms())if(!reached.contains(new BlockPos(room.x,room.y,room.z)))missing.add(room.name+" "+new BlockPos(room.x,room.y,room.z));
        assertTrue(missing.toString(),missing.isEmpty());assertTrue(reached.contains(GeyserCenter.gate()));assertTrue(reached.contains(GeyserCenter.arrival()));
        for(int side=0;side<4;side++)for(int t=0;t<36;t++) {
            int x=side==0?-18+t:side==1?18:side==2?18-t:-18,z=side==0?-18:side==1?-18+t:side==2?18:18-t;
            int floor=GeyserCenter.BOTTOM+24*side+Math.min(t,24);
            assertTrue("stair "+side+" "+t,reached.contains(GeyserCenter.local(x,floor+1,z)));
        }
        System.out.println("Geyser centre rooms and passages: "+p.rooms().size());
    }
    @Test public void bedsChestsAndLabBenchesRemainAccessible() {
        walk();int beds=0,chests=0,work=0;
        for(int x=-56;x<=56;x++)for(int z=-29;z<=46;z++)for(int y=4;y<=23;y++) {
            BlockPos p=GeyserCenter.local(x,y,z);IBlockState s=generated.at(p);
            if(s.getBlock()!=Blocks.BED && s.getBlock()!=Blocks.CHEST && s.getBlock()!=Blocks.FURNACE && s.getBlock()!=Blocks.CRAFTING_TABLE)continue;
            assertTrue("support "+p,generated.at(p.down()).isFullCube());assertFalse("headroom "+p,generated.solid(p.up()));
            boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));assertTrue("access "+p,access);
            if(s.getBlock()==Blocks.BED)beds++;else if(s.getBlock()==Blocks.CHEST)chests++;else work++;
        }
        assertEquals(8,beds);assertEquals(19,chests);assertEquals(4,work);
    }
    @Test public void shaftHasOpenSkyContainedWaterAndStableChunkSlices() {
        for(long seed:new long[]{0,12345,Long.MIN_VALUE}) {
            Generated g=new Generated(seed);
            for(int y=GeyserCenter.SITE.y+GeyserCenter.BOTTOM+1;y<255;y++)assertSame("daylight bore "+y,Blocks.AIR,g.at(new BlockPos(GeyserCenter.SITE.x,y,GeyserCenter.SITE.z)).getBlock());
            assertTrue(g.walkable(GeyserCenter.arrival()));assertTrue(g.walkable(GeyserCenter.gate()));
            for(int[] xz:new int[][]{{-20,-18},{0,-33},{-40,-18},{40,12}}) {
                BlockPos p=GeyserCenter.local(xz[0],0,xz[1]);int cx=p.getX()>>4,cz=p.getZ()>>4;
                ChunkPrimer first=g.generator.primer(cx,cz);g.generator.primer(cx+1,cz);ChunkPrimer second=g.generator.primer(cx,cz);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=30;y<171;y++)assertEquals(first.getBlockState(x,y,z),second.getBlockState(x,y,z));
            }
        }
        walk();int water=0;
        for(int x=-56;x<=30;x++)for(int z=-29;z<=30;z++)for(int y:new int[]{GeyserCenter.BOTTOM,4}) {
            BlockPos p=GeyserCenter.local(x,y,z);if(generated.at(p).getMaterial()!=Material.WATER)continue;water++;
            assertTrue("water floor "+p,generated.at(p.down()).isFullCube());
            for(EnumFacing f:EnumFacing.HORIZONTALS)assertTrue("water side "+p,generated.at(p.offset(f)).getMaterial()==Material.WATER || generated.solid(p.offset(f)));
            assertFalse("open pool "+p,generated.solid(p.up()));
        }
        assertTrue(water>700);
    }
    @Test public void thirdEntranceUsesDistinctLandingsAndNormalContainerSaving() {
        for(boolean inside:new boolean[]{false,true}) {
            BlockPos gate=inside?GeyserCenter.lowerGate():GeyserCenter.gate(),arrival=inside?OldHellWorld.arrival(2):GeyserCenter.arrival();
            assertTrue(OldHellWorld.doorway(2,inside,gate.getX()+.5,gate.getY(),gate.getZ()+.5));
            assertFalse(OldHellWorld.doorway(2,inside,arrival.getX()+.5,arrival.getY(),arrival.getZ()+.5));
            assertFalse(OldHellWorld.doorway(2,inside,gate.getX()+.5,gate.getY()+3,gate.getZ()+.5));
        }
        OldHellGenerator deep=new OldHellGenerator(null,12345);
        for(BlockPos p:new BlockPos[]{GeyserCenter.lowerGate(),GeyserCenter.lowerArrival()}) {
            ChunkPrimer c=deep.primer(p.getX()>>4,p.getZ()>>4);assertTrue(c.getBlockState(p.getX()&15,p.getY()-1,p.getZ()&15).isFullCube());
            for(int y=0;y<2;y++)assertSame(Blocks.AIR,c.getBlockState(p.getX()&15,p.getY()+y,p.getZ()&15).getBlock());
        }
        GensokyoGenerator generator=new GensokyoGenerator(new TestWorld(),12345);BlockPos p=GeyserCenter.local(34,4,-25);
        Chunk chunk=generator.generateChunk(p.getX()>>4,p.getZ()>>4);TileEntityChest chest=(TileEntityChest)chunk.getTileEntityMap().get(p);assertNotNull(chest);
        NBTTagCompound saved=chest.writeToNBT(new NBTTagCompound());assertEquals("lostfantasy:chests/kappa_parts",saved.getString("LootTable"));
        TileEntityChest restored=new TileEntityChest();restored.readFromNBT(saved);assertEquals(saved,restored.writeToNBT(new NBTTagCompound()));
    }
    private static final class Generated {
        final GensokyoGenerator generator;final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        Generated(long seed) {generator=new GensokyoGenerator(null,seed);}
        IBlockState at(BlockPos p) {return chunks.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.primer(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && at(p).getMaterial()!=Material.WATER && at(p).getMaterial()!=Material.LAVA;}
    }
}
