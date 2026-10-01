package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import java.util.*;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
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

public class PalanquinShipTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.PALANQUIN;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void allThreeDecksAndEveryRoomAreReachableFromTheForwardLanding() {
        Generated g=new Generated(12345);Set<BlockPos> reached=walk(g);
        GensokyoBlueprint plan=new GensokyoBlueprint();PalanquinShip.build(plan);assertEquals(26,plan.rooms().size());
        List<String> missing=new ArrayList<>();
        for(GensokyoBlueprint.Room r:plan.rooms())if(!reached.contains(new BlockPos(r.x,r.y,r.z)))missing.add(r.name);
        assertTrue(missing.toString(),missing.isEmpty());
        for(int x:new int[]{-6,6})for(int i=1;i<=5;i++)assertTrue("stair "+x+" step "+i,reached.contains(local(x,(x<0?-5:-10)+i+1,-8-i)));
        for(int x=-11;x<=11;x++)for(int z=15;z<=38;z++)assertTrue("cabin ceiling "+x+","+z,g.at(local(x,7,z)).isFullCube());
        for(int x=-10;x<=10;x++)for(int z=-24;z<=24;z++) {
            boolean stair=x>=5 && x<=7 && z>=-15 && z<=-6;
            if(!stair)assertTrue("middle floor "+x+","+z,g.at(local(x,-5,z)).isFullCube());
        }
        for(int side:new int[]{-1,1})for(int z=-47;z<=-36;z++) {
            int rim=Math.max(1,PalanquinShip.halfWidth(z)-2)+1;
            assertTrue("raised guard support",g.at(local(side*rim,2,z)).isFullCube());
            if(z== -47)continue;
            int previous=Math.max(1,PalanquinShip.halfWidth(z-1)-2)+1,row=rim>previous?z:z-1;
            for(int x=Math.min(previous,rim);x<=Math.max(previous,rim);x++)
                assertSame("guard bend",Blocks.SPRUCE_FENCE,g.at(local(side*x,3,row)).getBlock());
        }
    }
    @Test public void bedsContainersAndLampsHaveSupportClearanceAndReachableSides() {
        Generated g=new Generated(12345);Set<BlockPos> reached=walk(g);int beds=0,chests=0,lights=0;
        List<String> faults=new ArrayList<>();
        for(int x=-25;x<=25;x++)for(int z=-50;z<=45;z++)for(int y=-10;y<=7;y++) {
            BlockPos p=local(x,y,z);IBlockState s=g.at(p);
            if(s.getBlock()==Blocks.BED || s.getBlock()==Blocks.CHEST) {
                if(!g.at(p.down()).isFullCube())faults.add("support "+p);
                if(g.solid(p.up()))faults.add("headroom "+p);
                boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));
                if(!access)faults.add("access "+p);
                if(s.getBlock()==Blocks.CHEST)chests++;
                else {
                    beds++;EnumFacing f=s.getValue(BlockBed.FACING);IBlockState pair=g.at(p.offset(s.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?f.getOpposite():f));
                    assertSame(Blocks.BED,pair.getBlock());assertNotEquals(s.getValue(BlockBed.PART),pair.getValue(BlockBed.PART));
                }
            } else if(s.getBlock()==ModBlocks.RED_LANTERN) {
                lights++;if(!g.at(p.up()).isFullCube() && g.at(p.down()).getBlock()!=Blocks.LOG)faults.add("lamp support "+p);
            }
        }
        assertTrue(faults.toString(),faults.isEmpty());assertEquals(10,beds);assertEquals(20,chests);assertEquals(27,lights);
    }
    @Test public void enclosedHoldCannotLeakThroughTheSidesOrKeel() {
        Generated g=new Generated(12345);Set<BlockPos> visited=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        BlockPos start=local(0,-9,-20);queue.add(start);visited.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            assertTrue("open hull "+p,p.getX()>SITE.x-23 && p.getX()<SITE.x+23 && p.getZ()>SITE.z-52 && p.getZ()<SITE.z+45 && p.getY()>SITE.y-17);
            for(EnumFacing f:EnumFacing.values()) {
                BlockPos q=p.offset(f);if(q.getY()>=SITE.y || visited.contains(q) || g.solid(q))continue;
                visited.add(q);queue.add(q);
            }
        }
        assertTrue("hold reached",visited.size()>500);
        // The forward top-deck landing has five by five blocks of clear headroom.
        for(int x=-2;x<=2;x++)for(int z=-23;z<=-19;z++)assertTrue(g.walkable(local(x,1,z)));
    }
    @Test public void riggingConnectsToTheMastWithoutPiercingTheCloth() {
        Generated g=new Generated(12345);Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> pending=new ArrayDeque<>();
        BlockPos start=local(0,46,-4);pending.add(start);reached.add(start);
        while(!pending.isEmpty()) {
            BlockPos p=pending.removeFirst();for(EnumFacing f:EnumFacing.values()) {
                BlockPos q=p.offset(f);if(q.getY()<SITE.y+1 || q.getY()>SITE.y+49 || reached.contains(q))continue;
                IBlockState s=g.at(q);if(s.getBlock()!=Blocks.LOG && s.getBlock()!=Blocks.SPRUCE_FENCE)continue;
                reached.add(q);pending.add(q);
            }
        }
        for(int side:new int[]{-1,1}) {
            assertTrue("bow stay",reached.contains(local(side*8,3,-40)));
            assertTrue("aft stay",reached.contains(local(side*18,1,7)));
            assertTrue(g.at(local(side*8,2,-40)).isFullCube());
            assertTrue(g.at(local(side*18,0,7)).isFullCube());
        }
        for(int x=-17;x<=17;x++)for(int y=14;y<=43;y++) {
            int cloth=0;for(int z=-10;z<=-7;z++)if(g.at(local(x,y,z)).getBlock()==Blocks.WOOL)cloth++;
            assertEquals("cloth cross-section "+x+","+y,1,cloth);
        }
    }
    @Test public void shipSuppliesAndNavigationMaterialUseSavedChests() {
        TestWorld world=new TestWorld();world.loaded=false;GensokyoGenerator generator=new GensokyoGenerator(world,12345);
        LootTableManager manager=new LootTableManager(null);Set<BlockPos> seen=new HashSet<>();Set<String> kinds=new HashSet<>();
        for(int cx=(SITE.x-25)>>4;cx<=(SITE.x+25)>>4;cx++)for(int cz=(SITE.z-50)>>4;cz<=(SITE.z+45)>>4;cz++) {
            Chunk chunk=generator.generateChunk(cx,cz);
            for(Map.Entry<BlockPos,TileEntity> e:chunk.getTileEntityMap().entrySet())if(e.getValue() instanceof TileEntityChest) {
                assertTrue(seen.add(e.getKey()));TileEntityChest chest=(TileEntityChest)e.getValue();NBTTagCompound tag=chest.writeToNBT(new NBTTagCompound());
                TileEntityChest loaded=new TileEntityChest();loaded.readFromNBT(tag);assertEquals(tag,loaded.writeToNBT(new NBTTagCompound()));
                ResourceLocation id=new ResourceLocation(tag.getString("LootTable"));kinds.add(id.getPath());
                LootTable loot=manager.getLootTableFromLocation(id);assertNotSame(id.toString(),LootTable.EMPTY_LOOT_TABLE,loot);
                List<ItemStack> items=loot.generateLootForPools(new Random(13),new LootContext(0,null,manager,null,null,null));assertFalse(items.isEmpty());
                NBTTagCompound ordinary=tag.copy();ordinary.removeTag("LootTable");ordinary.removeTag("LootTableSeed");
                TileEntityChest openedChest=new TileEntityChest();openedChest.readFromNBT(ordinary);
                for(int i=0;i<items.size();i++)openedChest.setInventorySlotContents(i,items.get(i));
                NBTTagCompound opened=openedChest.writeToNBT(new NBTTagCompound());TileEntityChest reopened=new TileEntityChest();reopened.readFromNBT(opened);
                assertEquals(opened,reopened.writeToNBT(new NBTTagCompound()));assertFalse(opened.hasKey("LootTable"));
            }
        }
        assertEquals(20,seen.size());assertEquals(new HashSet<>(Arrays.asList("chests/ship_supplies","chests/ship_gear","chests/ship_records")),kinds);
    }
    @Test public void shipStaysAboveTerrainAndDoesNotChangeWithChunkOrderOrSeed() {
        Generated first=new Generated(12345);Map<BlockPos,IBlockState> expected=new HashMap<>();List<BlockPos> points=new ArrayList<>();
        for(int x=-25;x<=25;x+=3)for(int z=-53;z<=45;z+=3)for(int y=-18;y<=49;y+=2) {BlockPos p=local(x,y,z);points.add(p);expected.put(p,first.at(p));}
        Collections.reverse(points);
        for(long seed:new long[]{0,19,Long.MIN_VALUE}) {
            Generated other=new Generated(seed);GensokyoTerrain terrain=new GensokyoTerrain(seed);
            for(BlockPos p:points)assertEquals("seed/order "+p,expected.get(p),other.at(p));
            for(int x=-26;x<=26;x+=5)for(int z=-55;z<=48;z+=5)
                assertTrue("clear of mountain "+seed,terrain.column(SITE.x+x,SITE.z+z).ground<SITE.y-22);
        }
    }
    private static BlockPos local(int x,int y,int z) {return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
    private static Set<BlockPos> walk(Generated g) {
        Set<BlockPos> visited=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();BlockPos start=local(0,1,-20);
        assertTrue(g.walkable(start));visited.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();for(EnumFacing f:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos q=p.offset(f).up(dy);
                if(q.getX()<SITE.x-23 || q.getX()>SITE.x+23 || q.getZ()<SITE.z-51 || q.getZ()>SITE.z+44 || q.getY()<SITE.y-10 || q.getY()>SITE.y+6 || visited.contains(q))continue;
                if(!g.walkable(q) || dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(q.up(2)))continue;
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
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && !at(p).getMaterial().isLiquid();}
    }
}
