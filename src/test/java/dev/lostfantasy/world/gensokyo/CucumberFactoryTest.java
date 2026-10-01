package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.ModItems;
import dev.lostfantasy.TestWorld;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.storage.loot.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import java.util.*;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class CucumberFactoryTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.CUCUMBER_FACTORY;
    @BeforeClass public static void bootstrap() {
        GensokyoTestBlocks.register();
        for(net.minecraft.item.Item item:new net.minecraft.item.Item[]{ModItems.CUCUMBER,ModItems.CUCUMBER_SEEDS})
            if(!ForgeRegistries.ITEMS.containsKey(item.getRegistryName()))ForgeRegistries.ITEMS.register(item);
    }
    @Test public void allThreeLevelsWorkroomsAndWaterfallStationAreReachableOnFoot() {
        Generated g=new Generated();Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        BlockPos start=local(0,1,45);queue.add(start);reached.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing f:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(f).up(dy);
                if(n.getX()<SITE.x-108 || n.getX()>SITE.x+74 || n.getZ()<SITE.z-362 || n.getZ()>SITE.z+47 || n.getY()<SITE.y || n.getY()>SITE.y+22 || reached.contains(n))continue;
                if(!g.solid(n.down()) || g.at(n.down()).getBlock()==Blocks.FARMLAND || g.solid(n) || g.solid(n.up()))continue;
                if(dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(n.up(2)))continue;
                reached.add(n);queue.add(n);break;
            }
        }
        List<String> missing=new ArrayList<>();int count=0;
        for(GensokyoBlueprint.Room r:GensokyoStructures.create().rooms())
            if(r.name.startsWith(SITE.title+"·") || r.name.endsWith("·瀑后停靠台") || r.name.endsWith("·瀑后步道入口") || r.name.endsWith("·西侧观瀑台")) {
                count++;if(!reached.contains(new BlockPos(r.x,r.y,r.z)))missing.add(r.name+" "+new BlockPos(r.x-SITE.x,r.y-SITE.y,r.z-SITE.z));
            }
        assertTrue(missing.toString(),missing.isEmpty());assertTrue(count>=20);
    }
    @Test public void everyCropHasHydratedSoilReachableLightAndUsableStorage() {
        Generated g=new Generated();List<BlockPos> crops=new ArrayList<>();Set<BlockPos> lit=new HashSet<>();ArrayDeque<BlockPos> frontier=new ArrayDeque<>();int chests=0;
        for(int x=-40;x<=36;x++)for(int z=-40;z<=38;z++)for(int y=1;y<=21;y++) {
            BlockPos p=local(x,y,z);IBlockState s=g.at(p);
            if(s.getBlock()==ModBlocks.CUCUMBER_CROP)crops.add(p);
            if(s.getBlock()==Blocks.SEA_LANTERN) {lit.add(p);frontier.add(p);}
            if(s.getBlock()==Blocks.CHEST) {
                chests++;assertTrue("chest support "+p,g.at(p.down()).isFullCube());assertFalse("chest lid "+p,g.at(p.up()).isFullCube());
            }
        }
        // Level-15 lights spread six clear cells before dropping below the crop's level-9 requirement.
        for(int distance=1;distance<=6;distance++) {
            int size=frontier.size();
            for(int i=0;i<size;i++) {
                BlockPos p=frontier.removeFirst();
                for(EnumFacing f:EnumFacing.VALUES) {
                    BlockPos n=p.offset(f);if(n.getY()<SITE.y || n.getY()>SITE.y+24 || Math.abs(n.getX()-SITE.x)>43 || Math.abs(n.getZ()-SITE.z)>42)continue;
                    if(!g.at(n).isOpaqueCube() && lit.add(n))frontier.add(n);
                }
            }
        }
        assertEquals(2160,crops.size());assertTrue(chests>=18);
        for(BlockPos p:crops) {
            IBlockState soil=g.at(p.down());assertSame(Blocks.FARMLAND,soil.getBlock());assertEquals(7,(int)soil.getValue(BlockFarmland.MOISTURE));
            boolean wet=false;for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++)if(g.at(p.add(dx,-1,dz)).getMaterial()==net.minecraft.block.material.Material.WATER)wet=true;
            assertTrue("dry farmland "+p,wet);assertTrue("crop light "+p,lit.contains(p));assertTrue("growth light "+p,lit.contains(p.up()));
        }
    }
    @Test public void railwayKeepsConnectedShapesPowerClearanceAndDryWalkingSpace() {
        Generated g=new Generated();List<BlockPos> path=CucumberRailway.route();int slopes=0,curves=0,powered=0;
        for(int i=0;i<path.size();i++) {
            BlockPos p=path.get(i).add(SITE.x,SITE.y+1,SITE.z);IBlockState s=g.at(p);
            assertTrue("missing rail "+i+" "+p,s.getBlock() instanceof BlockRailBase);assertTrue(g.at(p.down()).isFullCube());
            assertFalse("cart headroom "+p,g.solid(p.up()));assertFalse(g.solid(p.up(2)));
            BlockRailBase.EnumRailDirection shape=s.getBlock()==Blocks.GOLDEN_RAIL?s.getValue(BlockRailPowered.SHAPE):s.getValue(BlockRail.SHAPE);
            for(int j:new int[]{i-1,i+1})if(j>=0 && j<path.size()) {
                BlockPos n=path.get(j).subtract(path.get(i));
                assertEquals(1,Math.abs(n.getX())+Math.abs(n.getZ()));assertTrue(Math.abs(n.getY())<=1);
                assertTrue("rail orientation "+i+" "+shape+" "+n,connects(shape,n));
                if(n.getY()>0)assertTrue("missing ascent "+i,shape.isAscending());
            }
            if(shape.isAscending())slopes++;if(shape.getMetadata()>=6)curves++;
            if(s.getBlock()==Blocks.GOLDEN_RAIL) {powered++;assertTrue(s.getValue(BlockRailPowered.POWERED));assertSame(Blocks.REDSTONE_BLOCK,g.at(p.down()).getBlock());}
        }
        assertEquals(13,slopes);assertEquals(3,curves);assertTrue(powered>25);
        // Inspect the whole cavity for side leaks rather than assuming a centre rail is dry.
        Set<BlockPos> seen=new HashSet<>();ArrayDeque<BlockPos> q=new ArrayDeque<>();BlockPos start=local(68,4,-100);q.add(start);seen.add(start);
        while(!q.isEmpty()) {
            BlockPos p=q.removeFirst();
            for(EnumFacing f:EnumFacing.VALUES) {
                BlockPos n=p.offset(f);if(n.getX()<SITE.x-73 || n.getX()>SITE.x+74 || n.getZ()<SITE.z-363 || n.getZ()>SITE.z-51 || n.getY()<SITE.y || n.getY()>SITE.y+22)continue;
                // Stop the western branch just inside its outdoor doorway, but retain the full eastern rail bore.
                if(n.getX()<SITE.x+60 && n.getZ()>GensokyoAtlas.WATERFALL.z+14)continue;
                IBlockState s=g.at(n);assertNotEquals("water entering tunnel "+n,net.minecraft.block.material.Material.WATER,s.getMaterial());
                if(!g.solid(n) && seen.add(n))q.add(n);
            }
        }
        assertTrue(seen.size()>3000);
    }
    private static boolean connects(BlockRailBase.EnumRailDirection shape,BlockPos n) {
        switch(shape) {
            case NORTH_SOUTH:case ASCENDING_NORTH:case ASCENDING_SOUTH:return n.getX()==0;
            case EAST_WEST:case ASCENDING_EAST:case ASCENDING_WEST:return n.getZ()==0;
            case NORTH_EAST:return n.getX()>0 || n.getZ()<0;
            case NORTH_WEST:return n.getX()<0 || n.getZ()<0;
            case SOUTH_EAST:return n.getX()>0 || n.getZ()>0;
            case SOUTH_WEST:return n.getX()<0 || n.getZ()>0;
            default:return false;
        }
    }
    @Test public void cucumberGrowsHarvestsSeedsAndSuppliesSurviveContainerSaving() {
        CropWorld world=new CropWorld();BlockCrops crop=(BlockCrops)ModBlocks.CUCUMBER_CROP;BlockPos p=new BlockPos(0,70,0);
        world.blocks.put(p.down(),Blocks.FARMLAND.getDefaultState().withProperty(BlockFarmland.MOISTURE,7));world.blocks.put(p,crop.withAge(0));
        assertTrue(crop.canBlockStay(world,p,world.getBlockState(p)));
        NonNullList<ItemStack> immature=NonNullList.create();crop.getDrops(immature,world,p,world.getBlockState(p),0);
        assertEquals(1,immature.size());assertSame(ModItems.CUCUMBER_SEEDS,immature.get(0).getItem());
        for(int i=0;i<4;i++)crop.grow(world,p,world.getBlockState(p));assertTrue(crop.isMaxAge(world.getBlockState(p)));
        boolean seeds=false;for(int i=0;i<30;i++) {
            NonNullList<ItemStack> drops=NonNullList.create();crop.getDrops(drops,world,p,world.getBlockState(p),0);
            assertTrue(drops.stream().anyMatch(s->s.getItem()==ModItems.CUCUMBER));seeds|=drops.stream().anyMatch(s->s.getItem()==ModItems.CUCUMBER_SEEDS);
        }
        assertTrue(seeds);assertFalse(crop.canGrow(world,p,world.getBlockState(p),false));
        GensokyoGenerator generator=new GensokyoGenerator(new TestWorld(),12345);BlockPos chest=local(31,1,-11);
        Chunk chunk=generator.generateChunk(chest.getX()>>4,chest.getZ()>>4);TileEntityChest tile=(TileEntityChest)chunk.getTileEntityMap().get(chest);assertNotNull(tile);
        NBTTagCompound saved=tile.writeToNBT(new NBTTagCompound());assertEquals("lostfantasy:chests/cucumber_supplies",saved.getString("LootTable"));
        TileEntityChest restored=new TileEntityChest();restored.readFromNBT(saved);assertEquals(saved,restored.writeToNBT(new NBTTagCompound()));
        LootTableManager manager=new LootTableManager(null);LootTable loot=manager.getLootTableFromLocation(new ResourceLocation(saved.getString("LootTable")));
        assertNotSame(LootTable.EMPTY_LOOT_TABLE,loot);Set<net.minecraft.item.Item> items=new HashSet<>();
        for(int i=0;i<30;i++)for(ItemStack stack:loot.generateLootForPools(new Random(i),new LootContext(0,null,manager,null,null,null)))items.add(stack.getItem());
        assertTrue(items.contains(ModItems.CUCUMBER));assertTrue(items.contains(ModItems.CUCUMBER_SEEDS));assertTrue(items.contains(net.minecraft.init.Items.MINECART));
    }
    private static BlockPos local(int x,int y,int z) {return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
    private static final class CropWorld extends TestWorld {
        @Override public boolean setBlockState(BlockPos p,IBlockState state,int flags) {blocks.put(p.toImmutable(),state);return true;}
        @Override public int getLight(BlockPos p) {return 15;}
        @Override public int getLightFromNeighbors(BlockPos p) {return 15;}
    }
    private static final class Generated {
        final GensokyoGenerator generator=new GensokyoGenerator(null,12345);final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        IBlockState at(BlockPos p) {return chunks.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.primer(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET && s.getBlock()!=Blocks.WALL_SIGN;}
    }
}
