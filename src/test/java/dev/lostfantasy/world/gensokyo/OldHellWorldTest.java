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

public class OldHellWorldTest {
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void bothSurfaceCavesHaveWalkableDescentsAndSeparateReturnLandings() {
        Generated g=new Generated(new GensokyoGenerator(null,12345)::primer);
        GensokyoBlueprint plan=new GensokyoBlueprint();UndergroundEntrances.build(plan);plan.seal();
        for(int route=0;route<2;route++) {
            GensokyoAtlas site=UndergroundEntrances.SITES[route];
            Set<BlockPos> reached=walk(g,new BlockPos(site.x,site.y+1,site.z-28),site,-29,38,-34,163,-53,24);
            assertTrue("gate "+route,reached.contains(UndergroundEntrances.gate(route)));
            assertTrue("return "+route,reached.contains(UndergroundEntrances.returnPoint(route)));
            for(GensokyoBlueprint.Room room:plan.rooms())if(room.name.startsWith(site.title+"·"))assertTrue(room.name,reached.contains(new BlockPos(room.x,room.y,room.z)));
            BlockPos gate=UndergroundEntrances.gate(route),back=UndergroundEntrances.returnPoint(route);
            assertTrue(OldHellWorld.doorway(route,false,gate.getX()+.5,gate.getY(),gate.getZ()+.5));
            assertFalse(OldHellWorld.doorway(route,false,back.getX()+.5,back.getY(),back.getZ()+.5));
            assertFalse(OldHellWorld.doorway(route,false,gate.getX()+.5,gate.getY()+3,gate.getZ()+.5));
        }
    }
    @Test public void streetsUpperRoomsBathsAndBothExitsConnectWithoutFlight() {
        Generated g=new Generated(new OldHellGenerator(null,12345)::primer);
        Set<BlockPos> reached=walk(g,OldHellWorld.arrival(0),OldHellWorld.ORIGIN,-390,495,-909,OldHellCity.EXIT_Z,-25,129);
        List<String> missing=new ArrayList<>();
        for(GensokyoBlueprint.Room room:OldHellCity.create().rooms())if(!reached.contains(new BlockPos(room.x,room.y,room.z)))missing.add(room.name+" "+new BlockPos(room.x,room.y,room.z));
        for(GensokyoBlueprint.Room room:HellDeepRoad.create().rooms())if(!reached.contains(new BlockPos(room.x,room.y,room.z)))missing.add(room.name+" "+new BlockPos(room.x,room.y,room.z));
        assertTrue(missing.toString(),missing.isEmpty());assertEquals(322,OldHellCity.create().rooms().size());
        for(int route=0;route<2;route++) {
            assertTrue(reached.contains(OldHellWorld.gate(route)));assertTrue(reached.contains(OldHellWorld.arrival(route)));
            BlockPos p=OldHellWorld.arrival(route);assertFalse(OldHellWorld.doorway(route,true,p.getX()+.5,p.getY(),p.getZ()+.5));
        }
    }
    @Test public void bedsStorageWorkplacesAndLightsHaveSupportAndAccess() {
        Generated g=new Generated(new OldHellGenerator(null,12345)::primer);Set<BlockPos> reached=cityWalk(g);int bedHalves=0,chests=0,workplaces=0;
        for(int x=-OldHellCity.HALF_WIDTH;x<=OldHellCity.HALF_WIDTH;x++)for(int z=-5;z<=OldHellCity.SOUTH;z++)for(int y=1;y<=16;y++) {
            BlockPos p=OldHellWorld.local(x,y,z);IBlockState state=g.at(p);
            if(state.getBlock()==Blocks.BED || state.getBlock()==Blocks.CHEST || state.getBlock()==Blocks.CRAFTING_TABLE || state.getBlock()==Blocks.FURNACE) {
                assertTrue("support "+p,g.at(p.down()).isFullCube());assertFalse("headroom "+p,g.solid(p.up()));
                boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));assertTrue("access "+p+" "+state,access);
                if(state.getBlock()==Blocks.BED) {
                    bedHalves++;EnumFacing facing=state.getValue(BlockBed.FACING);IBlockState other=g.at(p.offset(state.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?facing.getOpposite():facing));
                    assertSame(Blocks.BED,other.getBlock());assertNotEquals(state.getValue(BlockBed.PART),other.getValue(BlockBed.PART));
                } else if(state.getBlock()==Blocks.CHEST)chests++;else workplaces++;
            }
            if(state.getBlock()==ModBlocks.RED_LANTERN)assertTrue("lantern support "+p,g.at(p.up()).isFullCube());
        }
        assertEquals(304,bedHalves);assertEquals(224,chests);assertEquals(204,workplaces);
    }
    @Test public void bathingWaterIsExposedContainedAndHasShallowExits() {
        Generated g=new Generated(new OldHellGenerator(null,12345)::primer);int water=0;
        for(int x=-45;x<=45;x++)for(int z=OldHellCity.BATH_Z-3;z<=OldHellCity.BATH_Z+24;z++)for(int y=0;y<=1;y++) {
            BlockPos p=OldHellWorld.local(x,y,z);if(g.at(p).getMaterial()!=Material.WATER)continue;water++;
            assertTrue("pool floor "+p,y!=0 || g.at(p.down()).isFullCube());
            for(EnumFacing f:EnumFacing.HORIZONTALS)assertTrue("pool wall "+p,g.at(p.offset(f)).getMaterial()==Material.WATER || g.at(p.offset(f)).isFullCube());
            if(y==1)assertSame("open water "+p,Blocks.AIR,g.at(p.up()).getBlock());
        }
        assertEquals(1720,water);
        for(int x:new int[]{-26,26}) {
            assertTrue(g.at(OldHellWorld.local(x,1,OldHellCity.BATH_Z+1)).isFullCube());
            assertTrue(g.at(OldHellWorld.local(x,2,OldHellCity.BATH_Z)).getBlock() instanceof net.minecraft.block.BlockStairs);
        }
    }
    @Test public void roofGenerationAndLootRemainStableAcrossSeedsAndReloads() {
        for(long seed:new long[]{0,12345,Long.MIN_VALUE}) {
            OldHellGenerator generator=new OldHellGenerator(null,seed);Generated g=new Generated(generator::primer);
            for(BlockPos p:new BlockPos[]{OldHellWorld.arrival(0),OldHellWorld.arrival(1),OldHellWorld.local(0,1,100)})assertTrue("landing "+seed,g.walkable(p));
            int cx=OldHellWorld.ORIGIN.x>>4,cz=OldHellWorld.ORIGIN.z>>4;ChunkPrimer first=generator.primer(cx,cz);generator.primer(cx+1,cz-1);ChunkPrimer second=generator.primer(cx,cz);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<256;y++)assertEquals(first.getBlockState(x,y,z),second.getBlockState(x,y,z));
            for(int x:new int[]{-370,0,370})for(int z:new int[]{0,168,366}) {
                assertSame(Blocks.BEDROCK,g.at(OldHellWorld.local(x,255-OldHellWorld.ORIGIN.y,z)).getBlock());
                assertTrue("roof "+x+","+z,g.at(OldHellWorld.local(x,153,z)).isFullCube());
            }
        }
        TestWorld world=new TestWorld();OldHellGenerator generator=new OldHellGenerator(world,12345);LootTableManager manager=new LootTableManager(null);
        for(String name:new String[]{"old_hell_household","old_hell_pantry","old_hell_trade"}) {
            LootTable table=manager.getLootTableFromLocation(new ResourceLocation("lostfantasy","chests/"+name));assertNotSame(LootTable.EMPTY_LOOT_TABLE,table);
            List<ItemStack> items=table.generateLootForPools(new Random(19),new LootContext(0,null,manager,null,null,null));assertFalse(items.isEmpty());
        }
        OldCapitalHomes.Lot first=OldCapitalHomes.LOTS.get(0);
        BlockPos p=first.world(-first.h+2,9,-first.d+2);
        Chunk c=generator.generateChunk(p.getX()>>4,p.getZ()>>4);
        TileEntityChest chest=(TileEntityChest)c.getTileEntity(p,Chunk.EnumCreateEntityType.CHECK);assertNotNull(chest);
        NBTTagCompound saved=chest.writeToNBT(new NBTTagCompound());assertEquals("lostfantasy:chests/old_hell_household",saved.getString("LootTable"));
        TileEntityChest loaded=new TileEntityChest();loaded.readFromNBT(saved);assertEquals(saved,loaded.writeToNBT(new NBTTagCompound()));
        loaded.setLootTable(null,0);loaded.setInventorySlotContents(3,new ItemStack(net.minecraft.init.Items.BREAD,4));saved=loaded.writeToNBT(new NBTTagCompound());
        TileEntityChest reopened=new TileEntityChest();reopened.readFromNBT(saved);assertEquals(4,reopened.getStackInSlot(3).getCount());
    }
    private static Set<BlockPos> cityWalk(Generated g) {return walk(g,OldHellWorld.local(-52,1,-163),OldHellWorld.ORIGIN,-390,495,-179,OldHellCity.EXIT_Z,-25,28);}
    private static Set<BlockPos> walk(Generated g,BlockPos start,GensokyoAtlas site,int x1,int x2,int z1,int z2,int y1,int y2) {
        Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();assertTrue("start "+start,g.walkable(start));reached.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(side).up(dy);
                if(n.getX()<site.x+x1 || n.getX()>site.x+x2 || n.getZ()<site.z+z1 || n.getZ()>site.z+z2 || n.getY()<site.y+y1 || n.getY()>site.y+y2 || reached.contains(n))continue;
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
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && at(p).getMaterial()!=Material.WATER && at(p).getBlock()!=Blocks.WEB;}
    }
}
