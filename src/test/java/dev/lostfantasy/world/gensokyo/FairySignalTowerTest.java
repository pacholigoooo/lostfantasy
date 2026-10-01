package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.TestWorld;
import java.util.*;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.storage.loot.*;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class FairySignalTowerTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.FAIRY_SHRINE;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void oldLadderReachesAllThreePlatformsAndTheShrineGrounds() {
        Generated g=new Generated(12345);Set<BlockPos> reached=walk(g);GensokyoBlueprint p=new GensokyoBlueprint();FairySignalTower.build(p);
        assertEquals(8,p.rooms().size());List<String> missing=new ArrayList<>();
        for(GensokyoBlueprint.Room r:p.rooms())if(!reached.contains(new BlockPos(r.x,r.y,r.z)))missing.add(r.name);
        assertTrue(missing.toString(),missing.isEmpty());
        for(int y=1;y<=43;y++) {
            BlockPos at=local(0,y,0);IBlockState ladder=g.at(at);
            assertSame(Blocks.LADDER,ladder.getBlock());assertEquals(EnumFacing.SOUTH,ladder.getValue(BlockLadder.FACING));
            assertTrue(g.at(at.north()).isFullCube());assertFalse(g.solid(at.up()));assertTrue("ladder "+y,reached.contains(at));
        }
        for(int y:new int[]{14,28,42}) {
            int r=10-y/7;
            for(int n=-r+1;n<r;n++)for(int side:new int[]{-1,1}) {
                assertTrue("ring x "+y+","+n,reached.contains(local(n,y+1,side*(r-1))));
                assertTrue("ring z "+y+","+n,reached.contains(local(side*(r-1),y+1,n)));
            }
        }
    }
    @Test public void cratesAndClimbingPlantsRetainSupportAfterTheClearancesAreCut() {
        Generated g=new Generated(12345);Set<BlockPos> reached=walk(g);int chests=0,vines=0,wood=0,leaves=0;
        for(int x=-19;x<=19;x++)for(int z=-20;z<=25;z++)for(int y=1;y<=61;y++) {
            BlockPos p=local(x,y,z);IBlockState s=g.at(p);
            if(s.getBlock()==Blocks.CHEST) {
                chests++;assertTrue(g.at(p.down()).isFullCube());assertFalse(g.solid(p.up()));
                boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));assertTrue("crate "+p,access);
            } else if(s.getBlock()==Blocks.VINE) {
                vines++;boolean backing=false;for(EnumFacing f:EnumFacing.HORIZONTALS)
                    backing|=s.getValue(BlockVine.getPropertyFor(f)) && g.at(p.offset(f)).isFullCube();
                assertTrue("vine backing "+p,backing);
            } else if(s.getBlock()==Blocks.LOG)wood++;
            else if(s.getBlock()==Blocks.LEAVES)leaves++;
        }
        assertEquals(4,chests);assertTrue(vines>25);assertTrue(wood>150);assertTrue(leaves>1000);
        for(int x:new int[]{-9,9})for(int z:new int[]{-9,9})assertTrue(g.at(local(x,-3,z)).isFullCube());
    }
    @Test public void salvageIsInitializedOnlyOnNewChunksAndSurvivesContainerReload() {
        LootTableManager manager=new LootTableManager(null);ResourceLocation id=new ResourceLocation("lostfantasy","chests/signal_salvage");
        LootTable loot=manager.getLootTableFromLocation(id);assertNotSame(LootTable.EMPTY_LOOT_TABLE,loot);
        assertFalse(loot.generateLootForPools(new Random(7),new LootContext(0,null,manager,null,null,null)).isEmpty());
        for(int[] at:new int[][]{{3,15,1},{-3,29,1},{13,1,-4},{17,1,-4}}) {
            TestWorld w=new TestWorld();w.loaded=false;BlockPos p=local(at[0],at[1],at[2]);
            Chunk c=new GensokyoGenerator(w,12345).generateChunk(p.getX()>>4,p.getZ()>>4);
            TileEntityChest chest=(TileEntityChest)c.getTileEntityMap().get(p);assertNotNull(chest);
            NBTTagCompound data=chest.writeToNBT(new NBTTagCompound());assertEquals(id.toString(),data.getString("LootTable"));
            TileEntityChest loaded=new TileEntityChest();loaded.readFromNBT(data);assertEquals(data,loaded.writeToNBT(new NBTTagCompound()));
        }
    }
    @Test public void seedAndChunkOrderDoNotChangeTheTowerOrItsWalkways() {
        Generated forward=new Generated(12345),reverse=new Generated(734901);
        List<BlockPos> spots=new ArrayList<>();
        for(int x=-12;x<=18;x+=3)for(int z=-12;z<=12;z+=3)for(int y=0;y<=61;y++)spots.add(local(x,y,z));
        Map<BlockPos,IBlockState> states=new HashMap<>();for(BlockPos p:spots)states.put(p,forward.at(p));
        Collections.reverse(spots);for(BlockPos p:spots)assertEquals("seed/order "+p,states.get(p),reverse.at(p));
    }
    @Test public void towerClearingBelongsToForestWhileTheSunGardenStaysInTheFlowerRegion() {
        for(long seed:new long[]{0,12345,734901,Long.MIN_VALUE}) {
            GensokyoTerrain terrain=new GensokyoTerrain(seed);
            for(int x:new int[]{-30,0,30})for(int z:new int[]{-33,0,33})
                assertEquals(GensokyoTerrain.Region.FOREST,terrain.region(SITE.x+x,SITE.z+z));
            GensokyoAtlas flowers=GensokyoAtlas.SUN_GARDEN;
            assertEquals(GensokyoTerrain.Region.FLOWERS,terrain.region(flowers.x,flowers.z));
        }
    }
    private static BlockPos local(int x,int y,int z) {return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
    private static Set<BlockPos> walk(Generated g) {
        Set<BlockPos> visited=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();BlockPos start=local(0,1,38);
        assertTrue(g.stand(start));visited.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing f:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos q=p.offset(f).up(dy);if(!inside(q) || visited.contains(q) || !g.stand(q))continue;
                if(dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(q.up(2)))continue;
                visited.add(q);queue.add(q);break;
            }
            if(g.at(p).getBlock()==Blocks.LADDER)for(int dy:new int[]{-1,1}) {
                BlockPos q=p.up(dy);if(inside(q) && !visited.contains(q) && g.clear(q) && (g.at(q).getBlock()==Blocks.LADDER || g.stand(q))) {visited.add(q);queue.add(q);}
            }
        }
        return visited;
    }
    private static boolean inside(BlockPos p) {return p.getX()>=SITE.x-20 && p.getX()<=SITE.x+21 && p.getZ()>=SITE.z-20 && p.getZ()<=SITE.z+39 && p.getY()>=SITE.y && p.getY()<=SITE.y+45;}
    private static final class Generated {
        private final GensokyoGenerator generator;private final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        Generated(long seed) {generator=new GensokyoGenerator(null,seed);}
        IBlockState at(BlockPos p) {return chunks.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.primer(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {return at(p).getMaterial().blocksMovement() && at(p).getBlock()!=Blocks.LADDER;}
        boolean clear(BlockPos p) {return !solid(p) && !solid(p.up());}
        boolean stand(BlockPos p) {return clear(p) && (solid(p.down()) || at(p).getBlock()==Blocks.LADDER);}
    }
}
