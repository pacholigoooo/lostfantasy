package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import java.util.*;
import java.util.function.BiFunction;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
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

public class BoundaryWorldTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.YAKUMO;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void bothSleepingRoomsLivingSpacesAndGardenConnectToTheGate() {
        Generated g=new Generated(new BoundaryGenerator(null,12345)::primer);Set<BlockPos> reached=walk(g,BoundaryWorld.arrival());
        GensokyoBlueprint plan=BoundaryHouse.create();assertEquals(17,plan.rooms().size());List<String> missing=new ArrayList<>();
        for(GensokyoBlueprint.Room room:plan.rooms())if(!reached.contains(new BlockPos(room.x,room.y,room.z)))missing.add(room.name);
        assertTrue(missing.toString(),missing.isEmpty());
        assertTrue(reached.contains(local(0,1,61)));assertTrue(reached.contains(local(0,3,12)));
        // Every interior ceiling is closed, including the shared wing walls and veranda.
        for(int[] b:new int[][]{{-17,-18,17,9},{-29,-16,-17,5},{17,-8,29,9},{-18,10,18,13}})
            for(int x=b[0];x<=b[2];x++)for(int z=b[1];z<=b[3];z++)assertTrue("ceiling "+x+","+z,g.at(local(x,7,z)).isFullCube());
    }
    @Test public void furnishingsRemainUsableAndLampsAreSuspended() {
        Generated g=new Generated(new BoundaryGenerator(null,12345)::primer);Set<BlockPos> reached=walk(g,BoundaryWorld.arrival());int beds=0,chests=0,lamps=0,tv=0;
        for(int x=-44;x<=44;x++)for(int z=-29;z<=18;z++)for(int y=1;y<=10;y++) {
            BlockPos p=local(x,y,z);IBlockState s=g.at(p);
            if(s.getBlock()==Blocks.BED || s.getBlock()==Blocks.CHEST) {
                assertTrue("support "+p,g.at(p.down()).isFullCube());assertFalse("headroom "+p,g.solid(p.up()));
                boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));assertTrue("access "+p,access);
                if(s.getBlock()==Blocks.CHEST)chests++;
                else {beds++;EnumFacing face=s.getValue(BlockBed.FACING);IBlockState pair=g.at(p.offset(s.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?face.getOpposite():face));assertSame(Blocks.BED,pair.getBlock());assertNotEquals(s.getValue(BlockBed.PART),pair.getValue(BlockBed.PART));}
            } else if(s.getBlock()==ModBlocks.RED_LANTERN) {lamps++;assertTrue("lamp "+p,g.at(p.up()).isFullCube());}
            else if(s.getBlock()==ModBlocks.OUTSIDE_TELEVISION) {tv++;assertTrue(g.at(p.down()).isFullCube());}
        }
        assertEquals(4,beds);assertEquals(11,chests);assertEquals(13,lamps);assertEquals(1,tv);
        BlockPos study=dev.lostfantasy.world.BarrierStudy.position();
        assertSame(ModBlocks.BARRIER_STUDY,g.at(study).getBlock());
        assertTrue(g.at(study.down()).isFullCube());
    }
    @Test public void mountainEntranceAndBothArrivalPointsAreClearAndOutsideTheTrigger() {
        Generated outside=new Generated(new GensokyoGenerator(null,12345)::primer),inside=new Generated(new BoundaryGenerator(null,12345)::primer);
        assertTrue(walk(outside,BoundaryWorld.returnPoint()).contains(local(0,1,61)));
        assertTrue(inside.walkable(BoundaryWorld.arrival()));assertTrue(outside.walkable(BoundaryWorld.returnPoint()));
        for(BlockPos p:new BlockPos[]{BoundaryWorld.arrival(),BoundaryWorld.returnPoint()})assertFalse(BoundaryWorld.doorway(p.getX()-SITE.x,p.getY()-SITE.y,p.getZ()-SITE.z));
        assertTrue(BoundaryWorld.doorway(0,1,61));assertTrue(BoundaryWorld.doorway(-2,1,60));
        for(double[] p:new double[][]{{3,1,61},{0,7,61},{0,-3,61},{0,1,59},{0,1,63}})assertFalse(BoundaryWorld.doorway(p[0],p[1],p[2]));
        for(int z=58;z<=65;z++)for(int x=-2;x<=2;x++)for(int y=1;y<=3;y++) {
            assertSame(Blocks.AIR,outside.at(local(x,y,z)).getBlock());assertSame(Blocks.AIR,inside.at(local(x,y,z)).getBlock());
        }
    }
    @Test public void gardenWaterIsContainedAndBridgeHasTwoAccessibleBanks() {
        Generated g=new Generated(new BoundaryGenerator(null,12345)::primer);Set<BlockPos> reached=walk(g,BoundaryWorld.arrival());int water=0,pads=0;
        for(int x=-45;x<=-10;x++)for(int z=27;z<=53;z++) {
            BlockPos p=local(x,0,z);
            if(g.at(p).getMaterial()==Material.WATER) {
                water++;assertTrue(g.at(p.down()).isFullCube());
                for(EnumFacing f:EnumFacing.HORIZONTALS)assertTrue("pond bank "+p,g.at(p.offset(f)).getMaterial()==Material.WATER || g.at(p.offset(f)).isFullCube());
            }
            if(g.at(p.up()).getBlock()==Blocks.WATERLILY) {pads++;assertEquals(Material.WATER,g.at(p).getMaterial());}
        }
        assertTrue(water>300);assertTrue(pads>=4);
        for(int z=28;z<=52;z++)assertTrue("bridge "+z,reached.contains(local(-27,2,z)));
    }
    @Test public void terrainAndBuildingsDoNotDependOnChunkRequestOrder() {
        for(long seed:new long[]{0,12345,Long.MIN_VALUE}) {
            BoundaryGenerator g=new BoundaryGenerator(null,seed);
            for(int[] cell:new int[][]{{0,0},{-3,-2},{4,4},{-10,9}}) {
                int cx=(SITE.x>>4)+cell[0],cz=(SITE.z>>4)+cell[1];ChunkPrimer first=g.primer(cx,cz);g.primer(cx+1,cz-1);ChunkPrimer second=g.primer(cx,cz);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<256;y++)assertEquals(first.getBlockState(x,y,z),second.getBlockState(x,y,z));
            }
            for(int[] p:new int[][]{{0,50},{0,70},{-53,-39},{53,71}})assertEquals(SITE.y,g.ground(SITE.x+p[0],SITE.z+p[1]));
        }
    }
    @Test public void householdAndBookChestsKeepTheirLootAndInventoryAfterSaving() {
        LootTableManager manager=new LootTableManager(null);String[] names={"boundary_books","boundary_household","village_pantry"};int[][] points={{-4,3,1},{-4,3,-16},{18,3,-2}};
        for(int i=0;i<names.length;i++) {
            BlockPos p=local(points[i][0],points[i][1],points[i][2]);Chunk chunk=new BoundaryGenerator(new TestWorld(),12345).generateChunk(p.getX()>>4,p.getZ()>>4);
            TileEntityChest chest=(TileEntityChest)chunk.getTileEntityMap().get(p);assertNotNull(chest);
            NBTTagCompound nbt=chest.writeToNBT(new NBTTagCompound());ResourceLocation id=new ResourceLocation("lostfantasy","chests/"+names[i]);assertEquals(id.toString(),nbt.getString("LootTable"));
            TileEntityChest restored=new TileEntityChest();restored.readFromNBT(nbt);assertEquals(nbt,restored.writeToNBT(new NBTTagCompound()));
            LootTable loot=manager.getLootTableFromLocation(id);assertNotSame(LootTable.EMPTY_LOOT_TABLE,loot);
            List<ItemStack> items=loot.generateLootForPools(new Random(2),new LootContext(0,null,manager,null,null,null));assertFalse(items.isEmpty());
            TileEntityChest opened=new TileEntityChest();for(int j=0;j<items.size();j++)opened.setInventorySlotContents(j,items.get(j));
            NBTTagCompound saved=opened.writeToNBT(new NBTTagCompound());TileEntityChest loaded=new TileEntityChest();loaded.readFromNBT(saved);assertEquals(saved,loaded.writeToNBT(new NBTTagCompound()));
        }
    }
    private static BlockPos local(int x,int y,int z) {return BoundaryWorld.local(x,y,z);}
    private static Set<BlockPos> walk(Generated g,BlockPos start) {
        Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();assertTrue("start "+start,g.walkable(start));reached.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(side).up(dy);
                if(n.getX()<SITE.x-53 || n.getX()>SITE.x+53 || n.getZ()<SITE.z-39 || n.getZ()>SITE.z+84 || n.getY()<SITE.y || n.getY()>SITE.y+9 || reached.contains(n))continue;
                if(!g.walkable(n) || dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(n.up(2)))continue;
                reached.add(n);queue.add(n);break;
            }
        }
        return reached;
    }
    private static final class Generated {
        private final BiFunction<Integer,Integer,ChunkPrimer> generator;private final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        Generated(BiFunction<Integer,Integer,ChunkPrimer> generator) {this.generator=generator;}
        IBlockState at(BlockPos p) {return chunks.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.apply(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && at(p).getMaterial()!=Material.WATER;}
    }
}
