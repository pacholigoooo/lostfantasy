package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import java.util.*;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
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

public class NemunoHomeTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.NEMUNO;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void earthenEntranceRaisedLivingFloorAndYardsAreConnected() {
        Generated g=new Generated();Set<BlockPos> reached=walk(g);List<String> missing=new ArrayList<>();int count=0;
        for(GensokyoBlueprint.Room r:GensokyoStructures.create().rooms())if(r.name.startsWith(SITE.title+"·")) {
            count++;if(!reached.contains(new BlockPos(r.x,r.y,r.z)))missing.add(r.name+" "+new BlockPos(r.x-SITE.x,r.y-SITE.y,r.z-SITE.z));
        }
        assertEquals(9,count);assertTrue(missing.toString(),missing.isEmpty());
        assertTrue(reached.contains(local(11,1,6)));assertTrue(reached.contains(local(-9,2,12)));
    }
    @Test public void sleepingStorageAndGardenHaveSupportAndClearance() {
        Generated g=new Generated();Set<BlockPos> reached=walk(g);int beds=0,chests=0,lamps=0,carrots=0,thatch=0;
        for(int x=-35;x<=35;x++)for(int z=-32;z<=33;z++)for(int y=1;y<=23;y++) {
            BlockPos p=local(x,y,z);IBlockState state=g.at(p);
            if(state.getBlock()==Blocks.BED || state.getBlock()==Blocks.CHEST) {
                assertTrue("support "+p,g.at(p.down()).isFullCube());assertFalse("overhead "+p,g.solid(p.up()));
                boolean accessible=false;for(EnumFacing side:EnumFacing.HORIZONTALS)accessible|=reached.contains(p.offset(side));assertTrue("access "+p,accessible);
                if(state.getBlock()==Blocks.BED) {
                    beds++;EnumFacing facing=state.getValue(BlockBed.FACING);IBlockState pair=g.at(p.offset(state.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?facing.getOpposite():facing));
                    assertSame(Blocks.BED,pair.getBlock());assertNotEquals(state.getValue(BlockBed.PART),pair.getValue(BlockBed.PART));
                }else chests++;
            } else if(state.getBlock()==ModBlocks.RED_LANTERN) {lamps++;assertTrue(g.solid(p.up()));}
            else if(state.getBlock()==ModBlocks.THATCH)thatch++;
            else if(state.getBlock()==Blocks.CARROTS) {
                carrots++;assertSame(Blocks.FARMLAND,g.at(p.down()).getBlock());boolean water=false;
                for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++)water|=g.at(p.add(dx,-1,dz)).getMaterial()==Material.WATER;
                assertTrue(water);
            }
        }
        assertEquals(4,beds);assertEquals(5,chests);assertEquals(6,lamps);assertEquals(88,carrots);assertTrue(thatch>900);
        for(int x=-16;x<=10;x++)for(int z=-12;z<=10;z++)assertTrue("house ceiling "+x+","+z,g.at(local(x,6,z)).isFullCube());
    }
    @Test public void homeSlicesAndHouseholdSuppliesSurviveSaving() {
        Generated one=new Generated(),two=new Generated();
        for(int cx=(SITE.x-20)>>4;cx<=(SITE.x+30)>>4;cx++)for(int cz=(SITE.z-16)>>4;cz<=(SITE.z+18)>>4;cz++) {
            ChunkPrimer expected=one.chunk(cx,cz);two.chunk(cx-1,cz+1);ChunkPrimer actual=two.chunk(cx,cz);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=SITE.y;y<SITE.y+24;y++)assertEquals(expected.getBlockState(x,y,z),actual.getBlockState(x,y,z));
        }
        BlockPos p=local(19,2,-6);Chunk chunk=new GensokyoGenerator(new TestWorld(),12345).generateChunk(p.getX()>>4,p.getZ()>>4);
        TileEntityChest tile=(TileEntityChest)chunk.getTileEntityMap().get(p);assertNotNull(tile);NBTTagCompound nbt=tile.writeToNBT(new NBTTagCompound());
        ResourceLocation key=new ResourceLocation("lostfantasy","chests/nemuno_tools");assertEquals(key.toString(),nbt.getString("LootTable"));
        TileEntityChest restored=new TileEntityChest();restored.readFromNBT(nbt);assertEquals(nbt,restored.writeToNBT(new NBTTagCompound()));
        LootTableManager manager=new LootTableManager(null);LootTable loot=manager.getLootTableFromLocation(key);assertNotSame(LootTable.EMPTY_LOOT_TABLE,loot);
        assertFalse(loot.generateLootForPools(new Random(17),new LootContext(0,null,manager,null,null,null)).isEmpty());
    }
    private static Set<BlockPos> walk(Generated g) {
        Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();BlockPos start=local(0,1,39);assertTrue(g.walkable(start));reached.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(side).up(dy);
                if(n.getX()<SITE.x-36 || n.getX()>SITE.x+36 || n.getZ()<SITE.z-33 || n.getZ()>SITE.z+40 || n.getY()<SITE.y+1 || n.getY()>SITE.y+24 || reached.contains(n))continue;
                if(!g.walkable(n) || dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(n.up(2)))continue;
                reached.add(n);queue.add(n);break;
            }
        }
        return reached;
    }
    private static BlockPos local(int x,int y,int z) {return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
    private static final class Generated {
        final GensokyoGenerator generator=new GensokyoGenerator(null,12345);final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        ChunkPrimer chunk(int x,int z) {return chunks.computeIfAbsent(GensokyoAtlas.key(x,z),k->generator.primer(x,z));}
        IBlockState at(BlockPos p) {return chunk(p.getX()>>4,p.getZ()>>4).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && at(p).getMaterial()!=Material.WATER;}
    }
}
