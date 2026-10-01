package dev.lostfantasy.world.gensokyo;

import java.util.*;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkPrimer;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class LarvaHollowTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.LARVA;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void descendingEntranceConnectsEveryClearingWithoutBreakingTheEarthRoof() {
        Generated g=new Generated(12345);Set<BlockPos> reached=walk(g);
        GensokyoBlueprint plan=new GensokyoBlueprint();LarvaHollow.build(plan);
        assertEquals(7,plan.rooms().size());List<String> missing=new ArrayList<>();
        for(GensokyoBlueprint.Room r:plan.rooms())if(!reached.contains(new BlockPos(r.x,r.y,r.z)))missing.add(r.name);
        assertTrue(missing.toString(),missing.isEmpty());
        for(int z=10;z<=26;z++) {
            int floor=Math.max(-14,Math.min(0,z-24));
            for(int x=-1;x<=1;x++)assertTrue("entrance "+x+","+z,reached.contains(local(x,floor+1,z)));
        }
        for(int x=-12;x<=12;x+=3)for(int z=-14;z<=8;z+=3)assertTrue("roof "+x+","+z,g.at(local(x,-1,z)).isFullCube());
        assertTrue("substantial meadow to explore",reached.stream().filter(p->p.getY()<SITE.y-5).count()>450);
    }
    @Test public void undergroundPlantsHaveSoilCompleteHalvesAndSupportedRoots() {
        Generated g=new Generated(12345);int sunflowers=0,flowers=0,lights=0,leaves=0;
        for(int x=-16;x<=16;x++)for(int z=-19;z<=13;z++)for(int y=-15;y<=-3;y++) {
            BlockPos p=local(x,y,z);IBlockState s=g.at(p);
            if(s.getBlock()==Blocks.DOUBLE_PLANT) {
                if(s.getValue(BlockDoublePlant.HALF)==BlockDoublePlant.EnumBlockHalf.LOWER) {
                    sunflowers++;assertSame("flower soil "+p,Blocks.GRASS,g.at(p.down()).getBlock());
                    assertSame("upper half "+p,Blocks.DOUBLE_PLANT,g.at(p.up()).getBlock());
                    assertEquals(BlockDoublePlant.EnumBlockHalf.UPPER,g.at(p.up()).getValue(BlockDoublePlant.HALF));
                } else {
                    assertSame("lower half "+p,Blocks.DOUBLE_PLANT,g.at(p.down()).getBlock());
                    assertEquals(BlockDoublePlant.EnumBlockHalf.LOWER,g.at(p.down()).getValue(BlockDoublePlant.HALF));
                }
            } else if(s.getBlock()==Blocks.RED_FLOWER || s.getBlock()==Blocks.TALLGRASS) {
                flowers++;assertSame("plant soil "+p,Blocks.GRASS,g.at(p.down()).getBlock());
            } else if(s.getBlock()==Blocks.VINE) {
                boolean backing=false;for(EnumFacing f:EnumFacing.HORIZONTALS)
                    backing|=s.getValue(BlockVine.getPropertyFor(f)) && g.at(p.offset(f)).isFullCube();
                assertTrue("vine "+p,backing);
            } else if(s.getBlock()==Blocks.GLOWSTONE) {lights++;assertEquals(15,s.getLightValue());}
            else if(s.getBlock()==Blocks.LEAVES)leaves++;
            assertNotSame("a hollow, not a furnished house",Blocks.CHEST,s.getBlock());
        }
        assertTrue("flower patches",sunflowers>35);assertTrue(flowers>70);assertTrue(lights>10);assertTrue(leaves>60);
    }
    @Test public void ordinaryBlockLightKeepsTheCoveredFlowerBedsAlive() {
        Generated g=new Generated(12345);Map<BlockPos,Integer> light=new HashMap<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        List<BlockPos> flowers=new ArrayList<>();
        for(int x=-17;x<=17;x++)for(int z=-20;z<=14;z++)for(int y=-17;y<=-2;y++) {
            BlockPos p=local(x,y,z);IBlockState s=g.at(p);int emitted=s.getLightValue();
            if(emitted>0) {light.put(p,emitted);queue.add(p);}
            if(s.getBlock()==Blocks.RED_FLOWER || s.getBlock()==Blocks.DOUBLE_PLANT && s.getValue(BlockDoublePlant.HALF)==BlockDoublePlant.EnumBlockHalf.LOWER)flowers.add(p);
        }
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();int emitted=light.get(p);
            for(EnumFacing f:EnumFacing.values()) {
                BlockPos q=p.offset(f);if(!inside(q) || q.getY()>=SITE.y-1)continue;
                int propagated=emitted-Math.max(1,g.at(q).getLightOpacity());
                if(propagated>light.getOrDefault(q,0)) {light.put(q,propagated);queue.add(q);}
            }
        }
        assertTrue(flowers.size()>70);
        List<String> dark=new ArrayList<>();for(BlockPos p:flowers)if(light.getOrDefault(p,0)<8)dark.add(p+" = "+light.getOrDefault(p,0));
        assertTrue("flowers need block light 8: "+dark,dark.isEmpty());
    }
    @Test public void BothSeedsAndReverseChunkRequestsKeepTheHollowWalkableAndIdentical() {
        Generated forward=new Generated(12345),reverse=new Generated(734901);
        List<BlockPos> points=new ArrayList<>();
        for(int x=-16;x<=16;x+=2)for(int z=-18;z<=18;z+=2)for(int y=-16;y<=-1;y++)points.add(local(x,y,z));
        for(int z=20;z<=26;z++)for(int x=-1;x<=1;x++)points.add(local(x,Math.max(-14,Math.min(0,z-24))+1,z));
        Map<BlockPos,IBlockState> blocks=new HashMap<>();for(BlockPos p:points)blocks.put(p,forward.at(p));
        Collections.reverse(points);for(BlockPos p:points)assertEquals("seed/order "+p,blocks.get(p),reverse.at(p));
        assertTrue(walk(reverse).contains(local(0,-13,0)));
    }
    private static BlockPos local(int x,int y,int z) {return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
    private static Set<BlockPos> walk(Generated g) {
        Set<BlockPos> visited=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();BlockPos start=local(0,1,26);
        assertTrue("surface arrival",g.stand(start));visited.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing f:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos q=p.offset(f).up(dy);if(!inside(q) || visited.contains(q) || !g.stand(q))continue;
                if(dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(q.up(2)))continue;
                visited.add(q);queue.add(q);break;
            }
        }
        return visited;
    }
    private static boolean inside(BlockPos p) {return p.getX()>=SITE.x-17 && p.getX()<=SITE.x+17 && p.getZ()>=SITE.z-20 && p.getZ()<=SITE.z+27 && p.getY()>=SITE.y-16 && p.getY()<=SITE.y+6;}
    private static final class Generated {
        private final GensokyoGenerator generator;private final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        Generated(long seed) {generator=new GensokyoGenerator(null,seed);}
        IBlockState at(BlockPos p) {return chunks.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.primer(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {return at(p).getMaterial().blocksMovement() && at(p).getBlock()!=Blocks.CARPET;}
        boolean stand(BlockPos p) {return !solid(p) && !solid(p.up()) && solid(p.down());}
    }
}
