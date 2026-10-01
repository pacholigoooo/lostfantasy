package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.ModItems;
import dev.lostfantasy.TestWorld;
import java.util.*;
import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.storage.loot.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class RainbowMineTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.RAINBOW_CAVE;
    @BeforeClass public static void bootstrap() {
        GensokyoTestBlocks.register();
        for(Item item:new Item[]{ModItems.DRAGON_GEM})if(!ForgeRegistries.ITEMS.containsKey(item.getRegistryName()))ForgeRegistries.ITEMS.register(item);
    }
    @Test public void bothDescentRoutesReachEveryWorkingAndTheSurfaceRoad() {
        Generated g=new Generated(12345);Set<BlockPos> reached=walk(g);List<String> missing=new ArrayList<>();int count=0;
        for(GensokyoBlueprint.Room r:GensokyoStructures.create().rooms())if(r.name.startsWith(SITE.title+"·")) {
            count++;if(!reached.contains(new BlockPos(r.x,r.y,r.z)))missing.add(r.name+" "+new BlockPos(r.x-SITE.x,r.y-SITE.y,r.z-SITE.z));
        }
        assertEquals(16,count);assertTrue(missing.toString(),missing.isEmpty());
        for(int z=-30;z>=-148;z--)assertTrue("rail side "+z,reached.contains(local(2,RainbowMine.railFloor(z)+1,z)) || reached.contains(local(2,RainbowMine.railFloor(z)+2,z)));
        for(int z=-68;z>=-119;z--)assertTrue("west descent "+z,reached.contains(local(-52,RainbowMine.westFloor(z)+1,z)) || reached.contains(local(-52,RainbowMine.westFloor(z)+2,z)));
        for(int z=-119;z>=-148;z--)assertTrue("lower return "+z,reached.contains(local(-16,RainbowMine.loopFloor(z)+1,z)) || reached.contains(local(-16,RainbowMine.loopFloor(z)+2,z)));
        assertTrue(SITE.y>GensokyoAtlas.FALSE_HEAVEN.y);
    }
    @Test public void trackGradientsHaveConnectedRailsPowerAndClearance() {
        Generated g=new Generated(12345);int powered=0,slopes=0;
        for(int z=-150;z<=24;z++) {
            int floor=RainbowMine.railFloor(z);BlockPos p=local(0,floor+1,z);IBlockState s=g.at(p);
            assertTrue("rail "+p,s.getBlock()==Blocks.RAIL || s.getBlock()==Blocks.GOLDEN_RAIL);
            assertTrue(g.at(p.down()).isFullCube());assertFalse(g.solid(p.up()));assertFalse(g.solid(p.up(2)));
            EnumRailDirection shape=s.getBlock()==Blocks.RAIL?s.getValue(BlockRail.SHAPE):s.getValue(BlockRailPowered.SHAPE);
            int diff=RainbowMine.railFloor(z+1)-floor;assertTrue(diff>=0 && diff<=1);
            assertEquals(diff==1?EnumRailDirection.ASCENDING_SOUTH:EnumRailDirection.NORTH_SOUTH,shape);
            if(diff==1)slopes++;
            if(s.getBlock()==Blocks.GOLDEN_RAIL) {powered++;assertTrue(s.getValue(BlockRailPowered.POWERED));assertSame(Blocks.REDSTONE_BLOCK,g.at(p.down()).getBlock());}
            if(z>-150)assertTrue(Math.abs(RainbowMine.railFloor(z-1)-floor)<=1);
        }
        assertEquals(32,slopes);assertTrue(powered>110);
    }
    @Test public void suppliesLightsAndExposedMineralsAreUsable() {
        Generated g=new Generated(12345);Set<BlockPos> reached=walk(g);int chests=0,lamps=0,ore=0;
        for(int x=-80;x<=80;x++)for(int z=-172;z<=24;z++)for(int y=-32;y<=14;y++) {
            BlockPos p=local(x,y,z);IBlockState s=g.at(p);
            if(s.getBlock()==Blocks.CHEST) {
                chests++;assertTrue("chest support "+p,g.at(p.down()).isFullCube());assertFalse("lid "+p,g.solid(p.up()));
                boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));assertTrue("chest access "+p,access);
            }else if(s.getBlock()==ModBlocks.RED_LANTERN) {lamps++;assertTrue("lamp suspension "+p,g.solid(p.up()));}
            else if(s.getBlock()==ModBlocks.LIBRARY_LAMP)assertTrue(g.at(p.down()).isFullCube());
            else if(s.getBlock()==ModBlocks.DRAGON_GEM_ORE) {
                boolean exposed=false;for(EnumFacing f:EnumFacing.values())exposed|=g.at(p.offset(f)).getBlock()==Blocks.AIR;
                if(exposed)ore++;
            }
        }
        assertEquals(11,chests);assertEquals(19,lamps);assertTrue("exposed mineral faces "+ore,ore>180);
        IBlockState state=ModBlocks.DRAGON_GEM_ORE.getDefaultState();
        assertFalse(state.getBlock().hasTileEntity(state));assertEquals(5,state.getLightValue());assertEquals(2,state.getBlock().getHarvestLevel(state));
        assertSame(ModItems.DRAGON_GEM,state.getBlock().getItemDropped(state,new Random(3),0));
        for(int fortune=0;fortune<=3;fortune++)for(int i=0;i<20;i++) {
            int amount=state.getBlock().quantityDroppedWithBonus(fortune,new Random(i));assertTrue(amount>=1 && amount<=fortune+1);
        }
    }
    @Test public void workingsStayCoveredAcrossSeedsAndSlicesRemainIndependent() {
        for(long seed:new long[]{0,17,12345,Long.MIN_VALUE}) {
            Generated g=new Generated(seed);
            for(int[] p:new int[][]{{0,-38,0},{0,-69,-8},{-43,-68,-8},{-52,-119,-22},{46,-148,-32}}) {
                int roof=-1;
                for(int y=p[2]+3;y<=42;y++)if(g.at(local(p[0],y,p[1])).isFullCube()) {roof=y;break;}
                assertTrue("roof "+Arrays.toString(p),roof>p[2]+3);
                for(int y=roof;y<=roof+2;y++)assertTrue("solid cover "+seed+" "+Arrays.toString(p),g.at(local(p[0],y,p[1])).isFullCube());
                assertTrue("terrain covers chamber "+Arrays.toString(p),new GensokyoTerrain(seed).column(SITE.x+p[0],SITE.z+p[1]).ground>=SITE.y+roof+2);
            }
        }
        Generated one=new Generated(77),two=new Generated(77);
        for(int[] p:new int[][]{{0,-40},{-45,-68},{-52,-118},{45,-148},{0,-148},{20,3}}) {
            int cx=(SITE.x+p[0])>>4,cz=(SITE.z+p[1])>>4;ChunkPrimer expected=one.chunk(cx,cz);two.chunk(cx+1,cz-1);ChunkPrimer actual=two.chunk(cx,cz);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=SITE.y-36;y<SITE.y+44;y++)assertEquals(expected.getBlockState(x,y,z),actual.getBlockState(x,y,z));
        }
    }
    @Test public void miningSuppliesParseAndSaveAsOrdinaryChestLoot() {
        BlockPos p=local(-7,1,17);Chunk chunk=new GensokyoGenerator(new TestWorld(),12345).generateChunk(p.getX()>>4,p.getZ()>>4);
        TileEntityChest chest=(TileEntityChest)chunk.getTileEntityMap().get(p);assertNotNull(chest);
        NBTTagCompound nbt=chest.writeToNBT(new NBTTagCompound());ResourceLocation key=new ResourceLocation("lostfantasy","chests/rainbow_mining");
        assertEquals(key.toString(),nbt.getString("LootTable"));TileEntityChest restored=new TileEntityChest();restored.readFromNBT(nbt);assertEquals(nbt,restored.writeToNBT(new NBTTagCompound()));
        LootTableManager manager=new LootTableManager(null);LootTable loot=manager.getLootTableFromLocation(key);assertNotSame(LootTable.EMPTY_LOOT_TABLE,loot);
        boolean gems=false;for(int seed=0;seed<30;seed++)for(net.minecraft.item.ItemStack stack:loot.generateLootForPools(new Random(seed),new LootContext(0,null,manager,null,null,null)))gems|=stack.getItem()==ModItems.DRAGON_GEM;
        assertTrue(gems);
    }
    private static Set<BlockPos> walk(Generated g) {
        Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();BlockPos start=local(3,1,22);assertTrue(g.walkable(start));reached.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(side).up(dy);
                if(n.getX()<SITE.x-83 || n.getX()>SITE.x+81 || n.getZ()<SITE.z-177 || n.getZ()>SITE.z+24 || n.getY()<SITE.y-32 || n.getY()>SITE.y+16 || reached.contains(n))continue;
                if(!g.walkable(n) || dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(n.up(2)))continue;
                reached.add(n);queue.add(n);break;
            }
        }
        return reached;
    }
    private static BlockPos local(int x,int y,int z) {return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
    private static final class Generated {
        final GensokyoGenerator generator;final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        Generated(long seed) {generator=new GensokyoGenerator(null,seed);}
        ChunkPrimer chunk(int x,int z) {return chunks.computeIfAbsent(GensokyoAtlas.key(x,z),k->generator.primer(x,z));}
        IBlockState at(BlockPos p) {return chunk(p.getX()>>4,p.getZ()>>4).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {return at(p).getMaterial().blocksMovement();}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && at(p).getMaterial()!=Material.WATER;}
    }
}
