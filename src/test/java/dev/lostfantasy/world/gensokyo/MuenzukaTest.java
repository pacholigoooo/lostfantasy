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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.storage.loot.*;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class MuenzukaTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.MUENZUKA;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void clearingShelterAndSalvageAreReachableWithSupportedFurniture() {
        Generated g=new Generated(12345);Set<BlockPos> visited=walk(g);GensokyoBlueprint plan=new GensokyoBlueprint();Muenzuka.build(plan);
        assertEquals(7,plan.rooms().size());
        for(GensokyoBlueprint.Room r:plan.rooms())assertTrue(r.name,visited.contains(new BlockPos(r.x,r.y,r.z)));
        int beds=0,chests=0,lamps=0,flowers=0,bloom=0;
        for(int x=-39;x<=39;x++)for(int z=-36;z<=37;z++)for(int y=1;y<=30;y++) {
            BlockPos p=local(x,y,z);IBlockState s=g.at(p);
            if(s.getBlock()==Blocks.CHEST || s.getBlock()==Blocks.BED) {
                assertTrue("support "+p,g.at(p.down()).isFullCube());assertFalse("headroom "+p,g.solid(p.up()));
                boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=visited.contains(p.offset(f));assertTrue("access "+p,access);
                if(s.getBlock()==Blocks.CHEST)chests++;
                else {beds++;BlockPos other=p.offset(s.getValue(BlockBed.FACING),s.getValue(BlockBed.PART)==BlockBed.EnumPartType.FOOT?1:-1);assertSame(Blocks.BED,g.at(other).getBlock());}
            } else if(s.getBlock()==ModBlocks.RED_LANTERN) {lamps++;assertTrue("hanging lamp "+p,g.at(p.up()).isFullCube());}
            else if(s.getBlock()==ModBlocks.SPIDER_LILY) {flowers++;assertTrue(g.at(p.down()).getMaterial().isSolid());}
            else if(s.getBlock()==ModBlocks.PURPLE_CHERRY_LEAVES)bloom++;
        }
        assertEquals(2,beds);assertEquals(4,chests);assertEquals(1,lamps);assertTrue(flowers>900);assertTrue(bloom>1500);
        for(int x=20;x<=30;x++)for(int z=-9;z<=6;z++)assertTrue("continuous ceiling",g.at(local(x,6,z)).isFullCube());
    }
    @Test public void narrowForestRoadIsWalkableAcrossSeedsAndCarriesSupportedFlowerMargins() {
        for(long seed:new long[]{0,12345,734901}) {
            Generated g=new Generated(seed);GensokyoTerrain terrain=new GensokyoTerrain(seed);
            for(int i=1;i<Muenzuka.PATH.length;i++) {
                int[] a=Muenzuka.PATH[i-1],b=Muenzuka.PATH[i];int count=Math.max(Math.abs(b[0]-a[0]),Math.abs(b[1]-a[1]));int previous=-1;
                for(int n=0;n<=count;n++) {
                    int x=(int)Math.round(a[0]+(b[0]-a[0])*n/(double)count),z=(int)Math.round(a[1]+(b[1]-a[1])*n/(double)count);
                    GensokyoTerrain.Column c=terrain.column(x,z);assertFalse(c.wet());
                    assertTrue("road step",previous<0 || Math.abs(previous-c.ground)<=1);previous=c.ground;
                    assertTrue("walkable road "+x+","+z,g.stand(new BlockPos(x,c.ground+1,z)));
                }
            }
            int flowers=0;GensokyoAtlas at=GensokyoAtlas.RECONSIDERATION;
            for(int x=at.x-32;x<=at.x+32;x++)for(int z=at.z-32;z<=at.z+32;z++) {
                GensokyoTerrain.Column c=terrain.column(x,z);BlockPos p=new BlockPos(x,c.ground+1,z);
                if(g.at(p).getBlock()==ModBlocks.SPIDER_LILY) {flowers++;assertTrue(g.at(p.down()).getMaterial().isSolid());assertFalse(c.path());}
            }
            assertTrue("flower-lined margins",flowers>100);
        }
    }
    @Test public void containersPreserveTheirSalvageTableAndDoNotQueryNeighbourChunks() {
        LootTableManager manager=new LootTableManager(null);ResourceLocation id=new ResourceLocation("lostfantasy","chests/nazrin_salvage");
        LootTable table=manager.getLootTableFromLocation(id);assertNotSame(LootTable.EMPTY_LOOT_TABLE,table);
        assertFalse(table.generateLootForPools(new Random(14),new LootContext(0,null,manager,null,null,null)).isEmpty());
        for(int[] at:new int[][]{{29,2,-8},{10,1,-3},{16,1,-3}}) {
            TestWorld w=new TestWorld();w.loaded=false;BlockPos p=local(at[0],at[1],at[2]);
            Chunk chunk=new GensokyoGenerator(w,12345).generateChunk(p.getX()>>4,p.getZ()>>4);
            TileEntityChest chest=(TileEntityChest)chunk.getTileEntity(p,Chunk.EnumCreateEntityType.CHECK);
            assertNotNull(chest);NBTTagCompound before=chest.writeToNBT(new NBTTagCompound());assertEquals(id.toString(),before.getString("LootTable"));
            TileEntityChest restored=new TileEntityChest();restored.readFromNBT(before);
            assertEquals(before,restored.writeToNBT(new NBTTagCompound()));
        }
    }
    @Test public void clearingGeometryIsStableAcrossChunkOrderAndWorldSeed() {
        Generated forward=new Generated(12345),reverse=new Generated(734901);List<BlockPos> points=new ArrayList<>();
        for(int x=-37;x<=37;x+=3)for(int z=-34;z<=35;z+=3)for(int y=1;y<=30;y+=2)points.add(local(x,y,z));
        Map<BlockPos,IBlockState> expected=new HashMap<>();for(BlockPos p:points)expected.put(p,forward.at(p));
        Collections.reverse(points);for(BlockPos p:points)assertEquals("seed/order "+p,expected.get(p),reverse.at(p));
    }
    private static BlockPos local(int x,int y,int z) {return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
    private static Set<BlockPos> walk(Generated g) {
        Set<BlockPos> visited=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();BlockPos start=local(0,1,43);
        assertTrue(g.stand(start));visited.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing f:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos q=p.offset(f).up(dy);
                if(Math.abs(q.getX()-SITE.x)>39 || q.getZ()<SITE.z-36 || q.getZ()>SITE.z+43 || q.getY()<SITE.y || q.getY()>SITE.y+10 || visited.contains(q) || !g.stand(q))continue;
                if(dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(q.up(2)))continue;
                visited.add(q);queue.add(q);break;
            }
        }
        return visited;
    }
    private static final class Generated {
        final GensokyoGenerator generator;final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        Generated(long seed) {generator=new GensokyoGenerator(null,seed);}
        IBlockState at(BlockPos p) {return chunks.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.primer(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {return at(p).getMaterial().blocksMovement() && at(p).getBlock()!=Blocks.CARPET;}
        boolean stand(BlockPos p) {return !solid(p) && !solid(p.up()) && solid(p.down());}
    }
}
