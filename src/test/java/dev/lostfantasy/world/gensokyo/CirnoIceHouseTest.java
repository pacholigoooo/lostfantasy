package dev.lostfantasy.world.gensokyo;

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

public class CirnoIceHouseTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.CIRNO;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void houseAndShoreWalkConnectUsableFurnitureUnderAClosedDome() {
        Generated g=new Generated(12345);Set<BlockPos> reached=walk(g);
        GensokyoBlueprint plan=new GensokyoBlueprint();CirnoIceHouse.build(plan);assertEquals(6,plan.rooms().size());
        for(GensokyoBlueprint.Room r:plan.rooms())assertTrue(r.name,reached.contains(new BlockPos(r.x,r.y,r.z)));
        int beds=0,chests=0,lights=0;
        for(int x=-9;x<=9;x++)for(int z=-11;z<=13;z++)for(int y=0;y<=9;y++) {
            BlockPos p=local(x,y,z);IBlockState s=g.at(p);
            if(s.getBlock()==Blocks.BED || s.getBlock()==Blocks.CHEST) {
                assertTrue("support "+p,g.at(p.down()).isFullCube());assertFalse("headroom "+p,g.solid(p.up()));
                boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));
                assertTrue("access "+p,access);
                if(s.getBlock()==Blocks.CHEST)chests++;
                else {beds++;BlockPos other=p.offset(s.getValue(BlockBed.FACING),s.getValue(BlockBed.PART)==BlockBed.EnumPartType.FOOT?1:-1);assertSame(Blocks.BED,g.at(other).getBlock());}
            } else if(s.getBlock()==Blocks.SEA_LANTERN)lights++;
        }
        assertEquals(2,beds);assertEquals(2,chests);assertEquals(4,lights);
        for(int x=-5;x<=5;x++)for(int z=-7;z<=3;z++)if(x*x+(z+2)*(z+2)<36) {
            boolean roof=false;for(int y=3;y<=9;y++)roof|=g.at(local(x,y,z)).getMaterial().blocksMovement();
            assertTrue("dome roof "+x+","+z,roof);
        }
        for(int z=5;z<=13;z++)for(int x=-1;x<=1;x++)assertTrue("entrance headroom",g.stand(local(x,1,z)));
    }
    @Test public void lightingDoesNotMeltWindowsOrSnowAndTheFloorIsLit() {
        Generated g=new Generated(12345);Map<BlockPos,Integer> light=new HashMap<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        List<BlockPos> melting=new ArrayList<>();
        for(int x=-21;x<=20;x++)for(int z=-17;z<=26;z++)for(int y=-1;y<=12;y++) {
            BlockPos p=local(x,y,z);IBlockState s=g.at(p);int emitted=s.getLightValue();
            if(emitted>0) {light.put(p,emitted);queue.add(p);}
            if(s.getBlock()==Blocks.ICE || s.getBlock()==Blocks.SNOW_LAYER)melting.add(p);
        }
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();int emitted=light.get(p);
            for(EnumFacing f:EnumFacing.values()) {
                BlockPos q=p.offset(f);if(!inside(q) || q.getY()<SITE.y-1 || q.getY()>SITE.y+12)continue;
                int propagated=emitted-Math.max(1,g.at(q).getLightOpacity());
                if(propagated>light.getOrDefault(q,0)) {light.put(q,propagated);queue.add(q);}
            }
        }
        assertTrue("ice windows and a snowy fringe",melting.size()>40);
        TestWorld lightWorld=new TestWorld() {
            @Override public int getLightFor(net.minecraft.world.EnumSkyBlock type,BlockPos pos) {return light.getOrDefault(pos,0);}
        };
        for(BlockPos p:melting) {
            IBlockState s=g.at(p);int threshold=s.getBlock()==Blocks.ICE?11-s.getLightOpacity():11;
            assertTrue("block light at meltable surface "+p+" = "+light.getOrDefault(p,0),light.getOrDefault(p,0)<=threshold);
            // A melt attempts a world write, which this no-chunk world rejects.
            s.getBlock().updateTick(lightWorld,p,s,new Random(1));
        }
        assertTrue("central living space",light.getOrDefault(local(0,1,0),0)>=8);
        assertTrue("sleeping space",light.getOrDefault(local(-3,1,-5),0)>=8);
    }
    @Test public void houseFloatsOnPermanentLocalIceWhileTheRestOfTheLakeStaysOpen() {
        for(long seed:new long[]{0,12345,734901}) {
            GensokyoTerrain terrain=new GensokyoTerrain(seed);Generated g=new Generated(seed);
            for(int x=-16;x<=16;x+=4)for(int z=-14;z<=20;z+=4) {
                GensokyoTerrain.Column c=terrain.column(SITE.x+x,SITE.z+z);
                assertEquals("lake beneath floe",72,c.water);assertTrue("submerged lake bed",c.ground<71);
                assertTrue("ice supports the house and its paths",g.at(local(x,0,z)).getMaterial().blocksMovement());
            }
            assertSame("water beneath the floating floor",Blocks.WATER,g.at(local(0,-2,0)).getBlock());
            assertSame("solid ice beneath the floor",Blocks.PACKED_ICE,g.at(local(0,-1,0)).getBlock());
            for(int x:new int[]{-48,-100,-180})assertSame("open lake outside the floe",Blocks.WATER,g.at(local(x,0,4)).getBlock());
            Set<BlockPos> visited=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
            BlockPos start=local(-48,0,4);assertSame("nearby open water",Blocks.WATER,g.at(start).getBlock());
            queue.add(start);visited.add(start);boolean lake=false;
            while(!queue.isEmpty()) {
                BlockPos p=queue.removeFirst();if(p.getX()<SITE.x-235) {lake=true;break;}
                for(EnumFacing f:EnumFacing.HORIZONTALS) {
                    BlockPos q=p.offset(f);if(q.getX()<SITE.x-245 || q.getX()>SITE.x-23 || Math.abs(q.getZ()-SITE.z)>95 || visited.contains(q))continue;
                    GensokyoTerrain.Column c=terrain.column(q.getX(),q.getZ());
                    if(c.water==72 && c.ground<72) {visited.add(q);queue.add(q);}
                }
            }
            assertTrue("inlet joins the existing lake for seed "+seed,lake);
            for(BlockPos p:walk(g))if(p.getX()>SITE.x-21 && p.getZ()<SITE.z+14)
                assertNotSame("no flooded living area",Blocks.WATER,g.at(p).getBlock());
        }
    }
    @Test public void keepsakesSurviveSavingAndChunkRequestsDoNotChangeTheHouse() {
        LootTableManager manager=new LootTableManager(null);ResourceLocation id=new ResourceLocation("lostfantasy","chests/fairy_keepsakes");
        LootTable table=manager.getLootTableFromLocation(id);assertNotSame(LootTable.EMPTY_LOOT_TABLE,table);
        assertFalse(table.generateLootForPools(new Random(14),new LootContext(0,null,manager,null,null,null)).isEmpty());
        for(int z:new int[]{-4,0}) {
            TestWorld w=new TestWorld();w.loaded=false;BlockPos p=local(4,1,z);
            Chunk chunk=new GensokyoGenerator(w,12345).generateChunk(p.getX()>>4,p.getZ()>>4);
            TileEntityChest chest=(TileEntityChest)chunk.getTileEntity(p,Chunk.EnumCreateEntityType.CHECK);assertNotNull(chest);
            NBTTagCompound before=chest.writeToNBT(new NBTTagCompound());assertEquals(id.toString(),before.getString("LootTable"));
            TileEntityChest restored=new TileEntityChest();restored.readFromNBT(before);assertEquals(before,restored.writeToNBT(new NBTTagCompound()));
        }
        Generated forward=new Generated(12345),reverse=new Generated(734901);List<BlockPos> points=new ArrayList<>();
        for(int x=-9;x<=9;x++)for(int z=-11;z<=13;z++)for(int y=-1;y<=9;y++)points.add(local(x,y,z));
        Map<BlockPos,IBlockState> expected=new HashMap<>();for(BlockPos p:points)expected.put(p,forward.at(p));
        Collections.reverse(points);for(BlockPos p:points)assertEquals("seed/order "+p,expected.get(p),reverse.at(p));
    }
    private static BlockPos local(int x,int y,int z) {return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
    private static boolean inside(BlockPos p) {return p.getX()>=SITE.x-21 && p.getX()<=SITE.x+20 && p.getZ()>=SITE.z-17 && p.getZ()<=SITE.z+26;}
    private static Set<BlockPos> walk(Generated g) {
        Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();BlockPos start=local(125,11,115);
        assertTrue("arrival",g.stand(start));queue.add(start);reached.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();for(EnumFacing f:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos q=p.offset(f).up(dy);if(!walkInside(q) || q.getY()<SITE.y || q.getY()>SITE.y+16 || reached.contains(q) || !g.stand(q))continue;
                if(dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(q.up(2)))continue;
                reached.add(q);queue.add(q);break;
            }
        }
        assertTrue("shore boardwalk reaches the floe",reached.contains(local(0,1,25)));
        return reached;
    }
    private static boolean walkInside(BlockPos p) {
        int x=p.getX()-SITE.x,z=p.getZ()-SITE.z;
        return inside(p) || Math.abs(x)<=3 && z>=27 && z<=118 || x>=0 && x<=128 && z>=112 && z<=118;
    }
    private static final class Generated {
        final GensokyoGenerator generator;final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        Generated(long seed) {generator=new GensokyoGenerator(null,seed);}
        IBlockState at(BlockPos p) {return chunks.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.primer(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {return at(p).getMaterial().blocksMovement() && at(p).getBlock()!=Blocks.CARPET;}
        boolean stand(BlockPos p) {return !at(p).getMaterial().isLiquid() && !solid(p) && !solid(p.up()) && solid(p.down());}
    }
}
