package dev.lostfantasy.world.gensokyo;

import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkPrimer;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class GensokyoArchitectureTest {
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void shrineRoomsAreReachableFromTheFrontWithoutBreakingBlocks() {
        GensokyoBlueprint plan=GensokyoStructures.create();GensokyoAtlas site=GensokyoAtlas.HAKUREI;
        checkRooms(plan,site,GensokyoWorld.arrival(),site.y+7);
    }
    @Test public void bothFairyHomesHaveReachableLivingRoomsAndThreeSleepingPlaces() {
        GensokyoBlueprint plan=GensokyoStructures.create();
        for(GensokyoAtlas site:new GensokyoAtlas[]{GensokyoAtlas.FAIRY_TREE,GensokyoAtlas.FAIRY_OLD_TREE}) {
            checkRooms(plan,site,new BlockPos(site.x,site.y+1,site.z+22),site.y+23);
            int beds=0;
            for(int x=-10;x<=10;x++)for(int z=-10;z<=10;z++)for(int y=1;y<25;y++) {
                IBlockState s=plan.at(site.x+x,site.y+y,site.z+z);
                if(s.getBlock()==Blocks.BED && s.getValue(net.minecraft.block.BlockBed.PART)==net.minecraft.block.BlockBed.EnumPartType.HEAD)beds++;
            }
            assertEquals(site.name(),3,beds);
            assertSame(Blocks.CHEST,plan.at(site.x-3,site.y+2,site.z-5).getBlock());
            assertFalse(plan.at(site.x-3,site.y+3,site.z-5).isFullCube());
            assertSame(Blocks.BOOKSHELF,plan.at(site.x-6,site.y+3,site.z-3).getBlock());
            for(int x:new int[]{-4,-2}) {
                assertSame(dev.lostfantasy.ModBlocks.DOLL_DISPLAY,plan.at(site.x+x,site.y+10,site.z+4).getBlock());
                assertTrue(plan.at(site.x+x,site.y+9,site.z+4).isFullCube());
            }
        }
    }
    private static void checkRooms(GensokyoBlueprint plan,GensokyoAtlas site,BlockPos start,int ceiling) {
        Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        queue.add(start);reached.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            boolean climbing=plan.at(p.getX(),p.getY(),p.getZ()).getBlock()==Blocks.LADDER;
            if(climbing)for(int dy:new int[]{-1,1}) {
                BlockPos n=p.up(dy);
                if(n.getY()>site.y && n.getY()<ceiling && !reached.contains(n)
                        && !solid(plan,n,site) && !solid(plan,n.up(),site)) {reached.add(n);queue.add(n);}
            }
            for(int[] direction:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.add(direction[0],dy,direction[1]);
                if(!site.contains(n.getX(),n.getZ(),2) || n.getY()<site.y+1 || n.getY()>ceiling || reached.contains(n))continue;
                boolean landing=solid(plan,n.down(),site) || plan.at(n.getX(),n.getY(),n.getZ()).getBlock()==Blocks.LADDER;
                if(landing && !solid(plan,n,site) && !solid(plan,n.up(),site)
                        && (dy<=0 || !solid(plan,p.up(2),site))) {reached.add(n);queue.add(n);break;}
            }
        }
        for(GensokyoBlueprint.Room room:plan.rooms())if(site.contains(room.x,room.z,0))
            assertTrue("unreachable: "+room.name,reached.contains(new BlockPos(room.x,room.y,room.z)));
    }
    private static boolean solid(GensokyoBlueprint plan,BlockPos p,GensokyoAtlas site) {
        if(p.getY()<=site.y)return true;
        IBlockState state=plan.at(p.getX(),p.getY(),p.getZ());
        return state.getMaterial().blocksMovement() && state.getBlock()!=Blocks.CARPET && state.getBlock()!=Blocks.LADDER;
    }
    @Test public void chunkClippingMatchesPlanIncludingNegativeCoordinates() {
        GensokyoBlueprint plan=GensokyoStructures.create();
        for(long key:plan.chunks()) {
            int cx=(int)(key>>32),cz=(int)key;ChunkPrimer p=new ChunkPrimer();plan.paint(p,cx,cz);
            for(int x=0;x<16;x+=3)for(int z=0;z<16;z+=3)for(int y=115;y<153;y++)
                assertEquals(plan.at((cx<<4)+x,y,(cz<<4)+z),p.getBlockState(x,y,z));
        }
    }
    @Test public void generatorNeedsNoWorldOrNeighbourAccess() {
        GensokyoGenerator generator=new GensokyoGenerator(null,31);
        int[][] chunks={{GensokyoAtlas.HAKUREI.x>>4,GensokyoAtlas.HAKUREI.z>>4},{-11,-21},{-122,94}};
        for(int[] pos:chunks) {
            ChunkPrimer a=generator.primer(pos[0],pos[1]),b=generator.primer(pos[0],pos[1]);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<256;y++)assertEquals(a.getBlockState(x,y,z),b.getBlockState(x,y,z));
        }
    }
    @Test public void shrineChestsHaveOpenLidsAndStableFloors() {
        GensokyoBlueprint plan=GensokyoStructures.create();int count=0;
        for(long key:plan.chunks()) {
            int cx=(int)(key>>32),cz=(int)key;ChunkPrimer p=new ChunkPrimer();plan.paint(p,cx,cz);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=117;y<150;y++)if(p.getBlockState(x,y,z).getBlock()==Blocks.CHEST) {
                count++;assertTrue(p.getBlockState(x,y-1,z).isFullCube());assertFalse(p.getBlockState(x,y+1,z).isFullCube());
            }
        }
        assertTrue(count>=6);
    }
    @Test public void biomeQueriesUseTheSameGeographyAtEveryResolution() {
        GensokyoBiomes b=new GensokyoBiomes(123);
        net.minecraft.world.biome.Biome[] coarse=b.getBiomesForGeneration(null,-20,-25,13,11);
        for(int z=0;z<11;z++)for(int x=0;x<13;x++)
            assertEquals(coarse[x+z*13],b.getBiome(new BlockPos((-20+x)*4,64,(-25+z)*4)));
    }
}
