package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.ChunkPrimer;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class RopewayTest {
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void bothStationsHaveWalkableRoomsUsableFurnitureAndSafeBoarding() {
        Scene scene=new Scene(12345);GensokyoBlueprint plan=new GensokyoBlueprint();Ropeway.build(plan);plan.seal();
        for(int end=0;end<2;end++) {
            GensokyoAtlas s=RopewayPath.station(end);Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
            BlockPos start=new BlockPos(s.x,s.y+1,s.approachZ());reached.add(start);queue.add(start);assertTrue(scene.walkable(start));
            while(!queue.isEmpty()) {
                BlockPos at=queue.removeFirst();
                for(EnumFacing f:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                    BlockPos p=at.offset(f).up(dy);
                    if(Math.abs(p.getX()-s.x)>35 || p.getZ()<s.z-27 || p.getZ()>s.z+33 || p.getY()<s.y || p.getY()>s.y+10 || reached.contains(p))continue;
                    if(!scene.walkable(p) || dy>0 && scene.solid(at.up(2)) || dy<0 && scene.solid(p.up(2)))continue;
                    reached.add(p);queue.add(p);break;
                }
            }
            for(GensokyoBlueprint.Room r:plan.rooms())if(r.name.startsWith(s.title))assertTrue(r.name,reached.contains(new BlockPos(r.x,r.y,r.z)));
            assertTrue(reached.contains(RopewayPath.landing(end)));assertTrue(reached.contains(RopewayPath.console(end).south()));
            int chests=0;
            for(int x=s.x-31;x<=s.x+31;x++)for(int z=s.z-23;z<=s.z+24;z++) {
                BlockPos p=new BlockPos(x,s.y+5,z);IBlockState state=scene.at(p);
                if(state.getBlock()!=Blocks.CHEST && state.getBlock()!=Blocks.FURNACE && state.getBlock()!=Blocks.CRAFTING_TABLE)continue;
                if(state.getBlock()==Blocks.CHEST)chests++;
                assertTrue(scene.at(p.down()).isFullCube());assertFalse(scene.solid(p.up()));
                boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));assertTrue("access "+p,access);
            }
            assertEquals(6,chests);
        }
    }
    @Test public void fullRunningEnvelopeClearsTerrainBuildingsAndSupportsAcrossSeeds() {
        for(long seed:new long[]{0,12345,Long.MIN_VALUE}) {
            Scene s=new Scene(seed);GensokyoTerrain terrain=new GensokyoTerrain(seed);
            for(int lane:new int[]{-4,4})for(int d=0;d<=2600;d+=5) {
                Vec3d p=RopewayPath.point(d/2600.0,lane);
                assertTrue("terrain clearance seed="+seed+" d="+d,p.y>terrain.column((int)Math.floor(p.x),(int)Math.floor(p.z)).surface()+1);
                for(int x=(int)Math.floor(p.x-2.15);x<=Math.floor(p.x+2.15);x++)for(int z=(int)Math.floor(p.z-2.65);z<=Math.floor(p.z+2.65);z++)
                    for(int y=(int)Math.floor(p.y);y<=Math.floor(p.y+3);y++)assertFalse("cabin obstruction "+d+" "+new BlockPos(x,y,z),s.solid(new BlockPos(x,y,z)));
            }
        }
    }
    @Test public void cablesConnectAcrossChunkSeamsAndDoNotRequireANeighbourWorld() {
        Scene s=new Scene(12345);
        for(int lane:new int[]{-4,4}) {
            BlockPos first=new BlockPos(RopewayPath.point(0,lane).add(0,7,0)),last=new BlockPos(RopewayPath.point(1,lane).add(0,7,0));
            Set<BlockPos> visited=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();queue.add(first);visited.add(first);
            while(!queue.isEmpty()) {
                BlockPos at=queue.removeFirst();
                for(EnumFacing f:EnumFacing.values()) {
                    BlockPos n=at.offset(f);if(s.at(n).getBlock()==ModBlocks.ROPEWAY_CABLE && visited.add(n))queue.addLast(n);
                }
            }
            assertTrue("continuous lane "+lane,visited.contains(last));assertTrue(visited.size()>2600);
        }
        BlockPos p=new BlockPos(RopewayPath.point(.5,0));ChunkPrimer a=s.generator.primer(p.getX()>>4,p.getZ()>>4);
        s.generator.primer((p.getX()>>4)+1,p.getZ()>>4);ChunkPrimer b=s.generator.primer(p.getX()>>4,p.getZ()>>4);
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=80;y<230;y++)assertEquals(a.getBlockState(x,y,z),b.getBlockState(x,y,z));
    }
    @Test public void motionIsContinuousBoundedAndReachesBothPlatformsExactly() {
        for(int start=0;start<2;start++) {
            Vec3d previous=RopewayPath.ride(start,0),velocity=Vec3d.ZERO;
            for(int age=1;age<=RopewayPath.TRAVEL_TICKS;age++) {
                Vec3d next=RopewayPath.ride(start,age),delta=next.subtract(previous);
                assertTrue(delta.length()<.9);assertTrue(delta.subtract(velocity).length()<.02);previous=next;velocity=delta;
            }
            assertTrue(velocity.length()<.01);Vec3d target=RopewayPath.point(start==0?1:0,start==0?-4:4);assertEquals(target,previous);
            assertEquals(start,RopewayPath.nearest(start,0));assertEquals(1-start,RopewayPath.nearest(start,RopewayPath.TRAVEL_TICKS));
        }
    }
    private static final class Scene {
        final GensokyoGenerator generator;final Map<Long,ChunkPrimer> chunks=new LinkedHashMap<Long,ChunkPrimer>(128,.75f,true) {
            @Override protected boolean removeEldestEntry(Map.Entry<Long,ChunkPrimer> e) {return size()>128;}
        };
        Scene(long seed) {generator=new GensokyoGenerator(null,seed);}
        IBlockState at(BlockPos p) {return chunks.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.primer(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {return at(p).getMaterial().blocksMovement() && at(p).getBlock()!=ModBlocks.ROPEWAY_CABLE;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up());}
    }
}
