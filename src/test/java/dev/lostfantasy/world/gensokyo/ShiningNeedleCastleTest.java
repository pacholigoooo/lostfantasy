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
import dev.lostfantasy.block.CeilingChestTile;
import net.minecraft.block.BlockStairs;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.storage.loot.*;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ShiningNeedleCastleTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.SHINING_NEEDLE;
    @BeforeClass public static void bootstrap(){GensokyoTestBlocks.register();}
    @Test public void fourKeepFloorsAndBothLevelsOfEveryTowerAreReachableByFlight() {
        Generated g=new Generated(12345);FlightRoutes reached=fly(g);GensokyoBlueprint plan=new GensokyoBlueprint();
        ShiningNeedleCastle.build(plan);assertEquals(65,plan.rooms().size());List<String> missing=new ArrayList<>();
        Set<BlockPos> points=new HashSet<>();
        for(GensokyoBlueprint.Room room:plan.rooms()) {
            BlockPos p=new BlockPos(room.x,room.y,room.z);assertTrue("unique room marker "+room.name,points.add(p));
            if(!reached.contains(p))missing.add(room.name+" "+p);
        }
        assertTrue(missing.toString(),missing.isEmpty());
        for(int[] stair:new int[][]{{0,0,-31},{0,0,-14},{0,0,3},{-60,0,3},{60,0,3},{0,55,3}}) {
            for(int i=1;i<=9;i++)assertEquals(BlockStairs.EnumHalf.TOP,g.at(local(stair[0]-4,stair[2]+28-i,stair[1]+4-i)).getValue(BlockStairs.HALF));
            assertTrue("stairwell flight "+Arrays.toString(stair),reached.contains(local(stair[0],stair[2]+18,stair[1])));
        }
    }
    @Test public void allInvertedBedsChestsAndBowlDisplaysHaveCeilingSupportAndFlyingAccess() {
        Generated g=new Generated(12345);FlightRoutes reached=fly(g);int beds=0,chests=0,bowls=0,lamps=0,workbenches=0;
        List<String> faults=new ArrayList<>();
        for(int x=-80;x<=80;x++)for(int z=-37;z<=76;z++)for(int y=-31;y<=30;y++) {
            BlockPos p=local(x,y,z);IBlockState state=g.at(p);
            assertFalse("no upright fittings "+p,state.getBlock()==Blocks.CHEST || state.getBlock()==Blocks.BED || state.getBlock()==Blocks.CRAFTING_TABLE || state.getBlock()==ModBlocks.RED_LANTERN);
            if(state.getBlock()==ModBlocks.CEILING_CHEST || state.getBlock()==ModBlocks.CEILING_BED || state.getBlock()==ModBlocks.CEILING_LACQUER_BOWL || state.getBlock()==ModBlocks.CEILING_WORKBENCH) {
                if(!g.at(p.up()).isFullCube())faults.add("support "+p);
                if(g.solid(p.down()))faults.add("underside clearance "+p);
                boolean reachable=reached.contains(p.down(2));for(EnumFacing f:EnumFacing.HORIZONTALS)reachable|=reached.contains(p.down().offset(f));
                if(!reachable)faults.add("access "+p);
                if(state.getBlock()==ModBlocks.CEILING_CHEST)chests++;
                else if(state.getBlock()==ModBlocks.CEILING_LACQUER_BOWL)bowls++;
                else if(state.getBlock()==ModBlocks.CEILING_WORKBENCH)workbenches++;
                else {
                    beds++;EnumFacing facing=state.getValue(BlockBed.FACING);
                    IBlockState pair=g.at(p.offset(state.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?facing.getOpposite():facing));
                    assertSame(ModBlocks.CEILING_BED,pair.getBlock());assertNotEquals(state.getValue(BlockBed.PART),pair.getValue(BlockBed.PART));
                }
            } else if(state.getBlock()==ModBlocks.CEILING_LANTERN) {
                lamps++;if(g.at(p.up()).getBlock()!=Blocks.LOG || !g.at(p.up(2)).isFullCube())faults.add("lamp support "+p);
            }
        }
        assertTrue(faults.toString(),faults.isEmpty());assertEquals(12,beds);assertEquals(47,chests);assertEquals(3,bowls);assertEquals(14,workbenches);
        assertTrue("lit rooms",lamps>=65);System.out.println("Castle lanterns: "+lamps);
    }
    @Test public void invertedFloorsAndRoofsArePresentWithoutAffectingGravityOrRendering() {
        Generated g=new Generated(12345);
        for(int[] level:new int[][]{{20,31,40,32},{3,14,30,24},{-14,-3,22,17},{-31,-20,12,13}}) {
            int mats=0;for(int x=-level[2]+1;x<level[2];x++)for(int z=-level[3]+1;z<level[3];z++)
                if(g.at(local(x,level[1],z)).getBlock()==Blocks.WOOL)mats++;
            assertTrue("inverted tatami "+level[0],mats>70);
            assertTrue("outer ceiling",g.at(local(level[2]-2,level[1],0)).isFullCube());
            assertSame("underside tile eave",Blocks.STAINED_HARDENED_CLAY,g.at(local(level[2]+4,level[0]-1,0)).getBlock());
        }
        assertSame("upper stone base",Blocks.STONEBRICK,g.at(local(0,47,0)).getBlock());
        assertSame("lowest downward ridge",Blocks.STAINED_HARDENED_CLAY,g.at(local(0,-40,0)).getBlock());
        IBlockState bowl=ModBlocks.LACQUER_BOWL.getDefaultState();
        for(int i=0;i<4;i++) {
            assertEquals(bowl,ModBlocks.LACQUER_BOWL.getStateFromMeta(ModBlocks.LACQUER_BOWL.getMetaFromState(bowl)));
            assertFalse(ModBlocks.LACQUER_BOWL.hasTileEntity(bowl));assertEquals(0,bowl.getLightValue());
            bowl=ModBlocks.LACQUER_BOWL.withRotation(bowl,Rotation.CLOCKWISE_90);
        }
    }
    @Test public void invertedChestsKeepTheirLootAndOpenedContentsAcrossSaving() {
        TestWorld world=new TestWorld();world.loaded=false;GensokyoGenerator generator=new GensokyoGenerator(world,12345);
        LootTableManager manager=new LootTableManager(null);Set<BlockPos> found=new HashSet<>();Set<String> kinds=new HashSet<>();
        for(int cx=(SITE.x-80)>>4;cx<=(SITE.x+80)>>4;cx++)for(int cz=(SITE.z-37)>>4;cz<=(SITE.z+76)>>4;cz++) {
            Chunk chunk=generator.generateChunk(cx,cz);
            for(Map.Entry<BlockPos,TileEntity> e:chunk.getTileEntityMap().entrySet())if(e.getValue() instanceof CeilingChestTile) {
                assertTrue(found.add(e.getKey()));NBTTagCompound tag=e.getValue().writeToNBT(new NBTTagCompound());
                CeilingChestTile load=new CeilingChestTile();load.readFromNBT(tag);assertEquals(tag,load.writeToNBT(new NBTTagCompound()));
                ResourceLocation id=new ResourceLocation(tag.getString("LootTable"));kinds.add(id.getPath());
                LootTable table=manager.getLootTableFromLocation(id);assertNotSame(id.toString(),LootTable.EMPTY_LOOT_TABLE,table);
                List<ItemStack> items=table.generateLootForPools(new Random(25),new LootContext(0,null,manager,null,null,null));assertFalse(items.isEmpty());
                tag.removeTag("LootTable");tag.removeTag("LootTableSeed");CeilingChestTile opened=new CeilingChestTile();opened.readFromNBT(tag);
                for(int i=0;i<items.size();i++)opened.setInventorySlotContents(i,items.get(i));
                NBTTagCompound saved=opened.writeToNBT(new NBTTagCompound());CeilingChestTile reload=new CeilingChestTile();reload.readFromNBT(saved);
                assertEquals(saved,reload.writeToNBT(new NBTTagCompound()));assertFalse(saved.hasKey("LootTable"));
            }
        }
        assertEquals(47,found.size());assertEquals(new HashSet<>(Arrays.asList("chests/castle_records","chests/castle_household","chests/castle_crafts")),kinds);
    }
    @Test public void massiveCastleStaysWithinBuildHeightAndClearOfTheTerrain() {
        Generated original=new Generated(12345);List<BlockPos> points=new ArrayList<>();Map<BlockPos,IBlockState> expected=new HashMap<>();
        for(int x=-80;x<=80;x+=5)for(int z=-38;z<=77;z+=5)for(int y=-43;y<=49;y+=3) {
            BlockPos p=local(x,y,z);points.add(p);expected.put(p,original.at(p));
        }
        Collections.reverse(points);
        for(long seed:new long[]{0,19,Long.MIN_VALUE}) {
            Generated g=new Generated(seed);GensokyoTerrain terrain=new GensokyoTerrain(seed);
            for(BlockPos p:points)assertEquals("generation order/seed "+p,expected.get(p),g.at(p));
            for(int x=-80;x<=80;x+=4)for(int z=-38;z<=77;z+=4) {
                int lowest=256;for(int y=SITE.y-43;y<=SITE.y+49;y++)if(g.at(new BlockPos(SITE.x+x,y,SITE.z+z)).getBlock()!=Blocks.AIR){lowest=y;break;}
                if(lowest<256)assertTrue("terrain clearance "+seed+" "+x+","+z,terrain.column(SITE.x+x,SITE.z+z).ground<lowest-5);
            }
        }
        assertTrue(SITE.y+49<256);assertTrue(SITE.y-43>0);
    }
    private static BlockPos local(int x,int y,int z){return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
    private static FlightRoutes fly(Generated g) {
        FlightRoutes routes=new FlightRoutes();int[] pending=new int[FlightRoutes.NX*FlightRoutes.NY*FlightRoutes.NZ];
        int head=0,tail=0,start=routes.index(local(0,21,73));routes.reached.set(start);pending[tail++]=start;
        while(head<tail) {
            BlockPos p=routes.position(pending[head++]);
            for(EnumFacing f:EnumFacing.VALUES) {
                BlockPos q=p.offset(f);int index=routes.index(q);
                if(index<0 || routes.reached.get(index) || g.solid(q) || g.solid(q.up()))continue;
                routes.reached.set(index);pending[tail++]=index;
            }
        }
        return routes;
    }
    private static final class FlightRoutes {
        static final int NX=165,NY=64,NZ=121;
        final BitSet reached=new BitSet(NX*NY*NZ);
        int index(BlockPos p) {
            int x=p.getX()-SITE.x+82,y=p.getY()-SITE.y+31,z=p.getZ()-SITE.z+42;
            return x<0||x>=NX||y<0||y>=NY||z<0||z>=NZ?-1:(y*NZ+z)*NX+x;
        }
        BlockPos position(int i){int x=i%NX;i/=NX;int z=i%NZ;return local(x-82,i/NZ-31,z-42);}
        boolean contains(BlockPos p){int i=index(p);return i>=0 && reached.get(i);}
    }
    private static final class Generated {
        final GensokyoGenerator generator;final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        Generated(long seed){generator=new GensokyoGenerator(null,seed);}
        IBlockState at(BlockPos p){return chunks.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.primer(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p){return at(p).getMaterial().blocksMovement() && at(p).getBlock()!=Blocks.CARPET;}
        boolean walkable(BlockPos p){return solid(p.down()) && !solid(p) && !solid(p.up()) && !at(p).getMaterial().isLiquid();}
    }
}
