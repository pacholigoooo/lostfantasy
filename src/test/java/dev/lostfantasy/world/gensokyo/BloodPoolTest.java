package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import java.util.*;
import java.util.function.BiFunction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.*;
import net.minecraft.world.storage.loot.*;
import org.junit.*;
import static org.junit.Assert.*;

public class BloodPoolTest {
    @BeforeClass public static void setup() {GensokyoTestBlocks.register();}
    @Test public void stairsServiceLoopAndAllInteriorUsesAreConnected() {
        Scene s=new Scene(new BloodPoolGenerator(null,12345)::primer);Set<BlockPos> reached=s.walk(BloodPoolWorld.arrival(),-280,280,-301,638,64,213);
        List<String> missing=new ArrayList<>();
        for(GensokyoBlueprint.Room r:BloodPoolRemains.create().rooms())if(!reached.contains(new BlockPos(r.x,r.y,r.z)))missing.add(r.name+" "+r.x+","+r.y+","+r.z);
        assertTrue(missing.toString(),missing.isEmpty());
        int chests=0,work=0;
        for(int x=-250;x<=275;x++)for(int z=210;z<=538;z++) {
            if(Math.abs(x)<198)continue;
            BlockPos p=BloodPoolWorld.local(x,BloodPoolRemains.FLOOR+1,z);IBlockState b=s.at(p);
            if(b.getBlock()!=Blocks.CHEST && b.getBlock()!=Blocks.CRAFTING_TABLE && b.getBlock()!=Blocks.ANVIL)continue;
            assertTrue("support "+p,s.at(p.down()).isFullCube());assertFalse("headroom "+p,s.solid(p.up()));
            boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));assertTrue("usable "+p,access);
            if(b.getBlock()==Blocks.CHEST)chests++;else work++;
        }
        assertEquals(17,chests);assertEquals(6,work);
        assertTrue(reached.contains(BloodPoolWorld.gate(true)));
        System.out.println("Blood Pool reachable markers: "+BloodPoolRemains.create().rooms().size()+", chests: "+chests+", work: "+work);
    }
    @Test public void upperPassageConnectsAndBothLandingsAreDryWithoutBounce() {
        for(long seed:new long[]{0,12345,Long.MIN_VALUE}) {
            Scene upper=new Scene(new OldHellGenerator(null,seed)::primer);
            Set<BlockPos> reachable=upper.walk(OldHellWorld.local(0,BlazingHell.DECK+1,1685),-9,9,1684,1819,25,65);
            assertTrue("upper gate "+seed,reachable.contains(BloodPoolWorld.gate(false)));assertTrue(reachable.contains(BloodPoolWorld.returnPoint()));
            Scene lower=new Scene(new BloodPoolGenerator(null,seed)::primer);
            assertTrue(lower.walkable(BloodPoolWorld.arrival()));assertTrue(lower.walkable(BloodPoolWorld.gate(true)));
            for(int z=1789;z<=1818;z++)for(int y=30;y<=37;y++) {
                assertTrue("east lava casing",upper.at(new BlockPos(OldHellWorld.ORIGIN.x+5,y,OldHellWorld.ORIGIN.z+z)).isFullCube());
                assertTrue("west lava casing",upper.at(new BlockPos(OldHellWorld.ORIGIN.x-5,y,OldHellWorld.ORIGIN.z+z)).isFullCube());
            }
        }
        for(boolean inside:new boolean[]{false,true}) {
            BlockPos p=BloodPoolWorld.gate(inside),a=inside?BloodPoolWorld.arrival():BloodPoolWorld.returnPoint();
            assertTrue(BloodPoolWorld.doorway(inside,p.getX()+.5,p.getY(),p.getZ()+.5));
            assertFalse(BloodPoolWorld.doorway(inside,a.getX()+.5,a.getY(),a.getZ()+.5));
            assertFalse(BloodPoolWorld.doorway(inside,p.getX()+.5,p.getY()+3,p.getZ()+.5));
        }
    }
    @Test public void seaHasRegionalScaleDeepLiquidDryIslandsAndSolidCeiling() {
        for(long seed:new long[]{0,12345,Long.MIN_VALUE}) {
            BloodPoolGenerator g=new BloodPoolGenerator(null,seed);int liquid=0,dry=0,high=0;
            for(int x=-800;x<=800;x+=160)for(int z=0;z<=2240;z+=160) {
                BloodPoolGenerator.Column c=g.column(x,z);
                if(c.floor<BloodPoolGenerator.WATER && c.roof>BloodPoolGenerator.WATER+100)liquid++;
                if(c.floor>=BloodPoolGenerator.WATER && c.floor<c.roof)dry++;
                if(c.roof-c.floor>150)high++;
            }
            assertTrue("open blood sea "+liquid,liquid>95);assertTrue("rock islands "+dry,dry>5);assertTrue("tall vault "+high,high>50);
            for(int[] xz:new int[][]{{500,1000},{0,1070},{-450,1500}}) {
                BlockPos at=BloodPoolWorld.local(xz[0],0,xz[1]);int cx=at.getX()>>4,cz=at.getZ()>>4;
                ChunkPrimer first=g.primer(cx,cz);g.primer(cx-1,cz+1);ChunkPrimer again=g.primer(cx,cz);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<256;y++)assertEquals(first.getBlockState(x,y,z),again.getBlockState(x,y,z));
                assertSame(Blocks.BEDROCK,first.getBlockState(at.getX()&15,255,at.getZ()&15).getBlock());
            }
            BlockPos open=BloodPoolWorld.local(500,0,1000);ChunkPrimer p=g.primer(open.getX()>>4,open.getZ()>>4);
            assertSame(ModBlocks.CURSED_BLOOD,p.getBlockState(open.getX()&15,56,open.getZ()&15).getBlock());
            assertTrue(p.getBlockState(open.getX()&15,250,open.getZ()&15).isFullCube());
        }
    }
    @Test public void relicLootAndOpenedStoragePersist() {
        LootTableManager manager=new LootTableManager(null);ResourceLocation name=new ResourceLocation("lostfantasy","chests/blood_pool_relics");
        LootTable table=manager.getLootTableFromLocation(name);assertNotSame(LootTable.EMPTY_LOOT_TABLE,table);
        assertFalse(table.generateLootForPools(new Random(4),new LootContext(0,null,manager,null,null,null)).isEmpty());
        BloodPoolGenerator g=new BloodPoolGenerator(new TestWorld(),12345);BlockPos p=BloodPoolWorld.local(-240,BloodPoolRemains.FLOOR+1,222);
        Chunk c=g.generateChunk(p.getX()>>4,p.getZ()>>4);TileEntityChest chest=(TileEntityChest)c.getTileEntityMap().get(p);assertNotNull(chest);
        NBTTagCompound n=chest.writeToNBT(new NBTTagCompound());assertEquals(name.toString(),n.getString("LootTable"));
        TileEntityChest copy=new TileEntityChest();copy.readFromNBT(n);assertEquals(n,copy.writeToNBT(new NBTTagCompound()));
        copy.setLootTable(null,0);copy.setInventorySlotContents(2,new ItemStack(Items.BOOK,3));
        TileEntityChest opened=new TileEntityChest();opened.readFromNBT(copy.writeToNBT(new NBTTagCompound()));assertEquals(3,opened.getStackInSlot(2).getCount());
        assertEquals(5,ModBlocks.CURSED_BLOOD.getDefaultState().getLightValue());
    }
    private static final class Scene {
        final BiFunction<Integer,Integer,ChunkPrimer> generator;
        final Map<Long,ChunkPrimer> cache=new LinkedHashMap<Long,ChunkPrimer>(256,.75f,true) {@Override protected boolean removeEldestEntry(Map.Entry<Long,ChunkPrimer> e){return size()>256;}};
        Scene(BiFunction<Integer,Integer,ChunkPrimer> generator){this.generator=generator;}
        IBlockState at(BlockPos p){return cache.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.apply(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p){return at(p).getMaterial().blocksMovement();}
        boolean walkable(BlockPos p){return solid(p.down()) && !solid(p) && !solid(p.up()) && !at(p).getMaterial().isLiquid();}
        Set<BlockPos> walk(BlockPos start,int x1,int x2,int z1,int z2,int y1,int y2) {
            assertTrue("start "+start,walkable(start));Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();reached.add(start);queue.add(start);
            while(!queue.isEmpty()) {
                BlockPos p=queue.removeFirst();
                for(EnumFacing f:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                    BlockPos n=p.offset(f).up(dy);int x=n.getX()-OldHellWorld.ORIGIN.x,z=n.getZ()-OldHellWorld.ORIGIN.z;
                    if(x<x1 || x>x2 || z<z1 || z>z2 || n.getY()<y1 || n.getY()>y2 || reached.contains(n))continue;
                    if(!walkable(n) || dy>0 && solid(p.up(2)) || dy<0 && solid(n.up(2)))continue;
                    reached.add(n);queue.add(n);break;
                }
            }
            return reached;
        }
    }
}
