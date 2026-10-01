package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import java.util.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
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

public class BlazingHellTest {
    private static Generated scene;private static Set<BlockPos> reached;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    private static void route() {
        if(scene!=null)return;
        scene=new Generated(12345);reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        BlockPos start=OldHellWorld.local(23,-10,552);assertTrue("palace connection",scene.walkable(start));reached.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(side).up(dy);int x=n.getX()-OldHellWorld.ORIGIN.x,z=n.getZ()-OldHellWorld.ORIGIN.z,y=n.getY()-OldHellWorld.ORIGIN.y;
                if(Math.abs(x)>332 || z<549 || z>1820 || y<-61 || y>BlazingHell.DECK+103 || reached.contains(n))continue;
                if(!scene.walkable(n) || dy>0 && scene.solid(p.up(2)) || dy<0 && scene.solid(n.up(2)))continue;
                reached.add(n);queue.add(n);break;
            }
        }
        System.out.println("Blazing hell walking positions: "+reached.size());
    }
    @Test public void descentLoopFacilitiesAndAllThreeCoreLevelsAreReachableWithoutFlight() {
        route();List<String> missing=new ArrayList<>();
        for(GensokyoBlueprint.Room room:BlazingHell.create().rooms())if(!reached.contains(new BlockPos(room.x,room.y,room.z)))missing.add(room.name+" "+new BlockPos(room.x,room.y,room.z));
        assertTrue(missing.toString(),missing.isEmpty());assertTrue(BlazingHell.create().rooms().size()>=40);
        for(int z=780;z<=1690;z+=10) {
            if(z<1110)assertTrue("approach "+z,reached.contains(OldHellWorld.local(54,BlazingHell.DECK+1,z)));
            else for(int x:new int[]{-240,240})assertTrue("outer loop "+x+" "+z,reached.contains(OldHellWorld.local(x,BlazingHell.DECK+1,z)));
        }
    }
    @Test public void industrialFurnitureHasSupportClearanceAndWalkingAccess() {
        route();int chests=0,work=0,lights=0,beds=0;
        for(int[] center:new int[][]{{-240,1170},{240,1240},{-240,1580},{240,1620}}) {
            for(int x=center[0]-26;x<=center[0]+26;x++)for(int z=center[1]-28;z<=center[1]+28;z++)for(int y=BlazingHell.DECK+1;y<=BlazingHell.DECK+16;y++) {
                BlockPos p=OldHellWorld.local(x,y,z);IBlockState s=scene.at(p);
                if(s.getBlock()==Blocks.CHEST || s.getBlock()==Blocks.CRAFTING_TABLE || s.getBlock()==Blocks.FURNACE || s.getBlock()==Blocks.ANVIL || s.getBlock()==Blocks.BED) {
                    assertTrue("support "+p,scene.at(p.down()).isFullCube());assertFalse("clearance "+p,scene.solid(p.up()));
                    boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));assertTrue("access "+p,access);
                    if(s.getBlock()==Blocks.CHEST)chests++;
                    else if(s.getBlock()==Blocks.BED) {
                        beds++;net.minecraft.block.BlockBed.EnumPartType part=s.getValue(net.minecraft.block.BlockBed.PART);
                        EnumFacing facing=s.getValue(net.minecraft.block.BlockBed.FACING);
                        IBlockState other=scene.at(p.offset(part==net.minecraft.block.BlockBed.EnumPartType.HEAD?facing.getOpposite():facing));
                        assertSame(Blocks.BED,other.getBlock());assertNotEquals(part,other.getValue(net.minecraft.block.BlockBed.PART));
                    }else work++;
                }
                if(s.getBlock()==ModBlocks.LIBRARY_LAMP) {
                    lights++;assertSame("hung lamp "+p,Blocks.IRON_BARS,scene.at(p.up()).getBlock());
                }
            }
        }
        assertEquals(42,chests);assertTrue(work>=32);assertEquals(16,lights);assertEquals(16,beds);
        System.out.println("Blazing hell furniture: chests="+chests+", work="+work+", lamps="+lights+", bed halves="+beds);
    }
    @Test public void regionalCavernHasDepthLavaIslandsAndRockRoofAcrossSeeds() {
        for(long seed:new long[]{0,12345,Long.MIN_VALUE}) {
            Generated g=new Generated(seed);int open=0,lava=0,dry=0,high=0;
            for(int x=-600;x<=600;x+=80)for(int z=800;z<=2200;z+=80) {
                boolean columnOpen=false;
                for(int y=35;y<=220;y+=20)if(g.at(new BlockPos(OldHellWorld.ORIGIN.x+x,y,OldHellWorld.ORIGIN.z+z)).getBlock()==Blocks.AIR)columnOpen=true;
                if(columnOpen)open++;
                IBlockState floor=g.at(new BlockPos(OldHellWorld.ORIGIN.x+x,32,OldHellWorld.ORIGIN.z+z));
                if(floor.getMaterial()==Material.LAVA)lava++;else if(floor.isFullCube())dry++;
                if(g.at(new BlockPos(OldHellWorld.ORIGIN.x+x,205,OldHellWorld.ORIGIN.z+z)).getBlock()==Blocks.AIR)high++;
                assertTrue("rock cap",g.at(new BlockPos(OldHellWorld.ORIGIN.x+x,250,OldHellWorld.ORIGIN.z+z)).isFullCube());
                assertSame(Blocks.BEDROCK,g.at(new BlockPos(OldHellWorld.ORIGIN.x+x,255,OldHellWorld.ORIGIN.z+z)).getBlock());
            }
            assertTrue("regional area "+open,open>220);assertTrue("lava basins "+lava,lava>45);assertTrue("dry islands "+dry,dry>30);assertTrue("high cavern "+high,high>30);
            assertTrue(g.walkable(OldHellWorld.local(54,BlazingHell.DECK+1,850)));
            assertSame("hell beneath palace",Blocks.AIR,g.at(new BlockPos(OldHellWorld.ORIGIN.x,55,OldHellWorld.ORIGIN.z+Chireiden.Z)).getBlock());
            assertTrue("rock below palace foundation",g.at(new BlockPos(OldHellWorld.ORIGIN.x,72,OldHellWorld.ORIGIN.z+Chireiden.Z)).isFullCube());
        }
    }
    @Test public void ordinaryLightBlocksAndChunkOrderNeedNoRendererOrNeighbourLoads() {
        Generated g=new Generated(12345);
        assertSame(Blocks.GLOWSTONE,g.at(OldHellWorld.local(0,BlazingHell.DECK+72,BlazingHell.CORE_Z)).getBlock());
        for(int[] xz:new int[][]{{54,720},{-240,1170},{0,BlazingHell.CORE_Z},{240,1690},{390,1950}}) {
            BlockPos pos=OldHellWorld.local(xz[0],0,xz[1]);int cx=pos.getX()>>4,cz=pos.getZ()>>4;
            ChunkPrimer before=g.generator.primer(cx,cz);g.generator.primer(cx+1,cz-1);ChunkPrimer after=g.generator.primer(cx,cz);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<256;y++)assertEquals(before.getBlockState(x,y,z),after.getBlockState(x,y,z));
        }
        assertSame(Blocks.IRON_BARS,g.at(Chireiden.local(0,-10,72)).getBlock());
    }
    @Test public void maintenanceLootAndOpenedInventoryPersistNormally() {
        LootTableManager manager=new LootTableManager(null);ResourceLocation name=new ResourceLocation("lostfantasy","chests/hell_maintenance");
        LootTable loot=manager.getLootTableFromLocation(name);assertNotSame(LootTable.EMPTY_LOOT_TABLE,loot);
        assertFalse(loot.generateLootForPools(new Random(4),new LootContext(0,null,manager,null,null,null)).isEmpty());
        OldHellGenerator generator=new OldHellGenerator(new TestWorld(),12345);BlockPos p=OldHellWorld.local(-261,BlazingHell.DECK+1,1147);
        Chunk chunk=generator.generateChunk(p.getX()>>4,p.getZ()>>4);TileEntityChest chest=(TileEntityChest)chunk.getTileEntityMap().get(p);assertNotNull(chest);
        NBTTagCompound saved=chest.writeToNBT(new NBTTagCompound());assertEquals(name.toString(),saved.getString("LootTable"));
        TileEntityChest restored=new TileEntityChest();restored.readFromNBT(saved);assertEquals(saved,restored.writeToNBT(new NBTTagCompound()));
        restored.setLootTable(null,0);restored.setInventorySlotContents(3,new ItemStack(Items.QUARTZ,7));saved=restored.writeToNBT(new NBTTagCompound());
        TileEntityChest opened=new TileEntityChest();opened.readFromNBT(saved);assertEquals(7,opened.getStackInSlot(3).getCount());
    }
    @Test public void junctionRailingsGuardDropEdgesWithoutClosingStarBridgeEnds() {
        Generated g=new Generated(12345);
        assertSame("outer corner rail",Blocks.IRON_BARS,g.at(OldHellWorld.local(-240,BlazingHell.DECK+1,1106)).getBlock());
        assertSame("junction walking surface",Blocks.AIR,g.at(OldHellWorld.local(-240,BlazingHell.DECK+1,1110)).getBlock());
        assertSame("star to ring opening",Blocks.AIR,g.at(OldHellWorld.local(25,BlazingHell.DECK+1,BlazingHell.CORE_Z+37)).getBlock());
        assertSame("upper inner rail",Blocks.IRON_BARS,g.at(OldHellWorld.local(25,BlazingHell.DECK+49,BlazingHell.CORE_Z+37)).getBlock());
    }
    private static final class Generated {
        final OldHellGenerator generator;
        private final Map<Long,ChunkPrimer> chunks=new LinkedHashMap<Long,ChunkPrimer>(256,.75f,true) {
            @Override protected boolean removeEldestEntry(Map.Entry<Long,ChunkPrimer> entry) {return size()>256;}
        };
        Generated(long seed) {generator=new OldHellGenerator(null,seed);}
        IBlockState at(BlockPos p) {return chunks.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.primer(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && at(p).getMaterial()!=Material.WATER && at(p).getMaterial()!=Material.LAVA;}
    }
}
