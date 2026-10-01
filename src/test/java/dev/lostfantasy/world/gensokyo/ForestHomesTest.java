package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import java.util.*;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkPrimer;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ForestHomesTest {
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void bothHomesHaveAccessibleRoomsInTheActualGeneratedTerrain() {
        Generated generated=new Generated(12345);
        for(GensokyoAtlas site:new GensokyoAtlas[]{GensokyoAtlas.MARISA,GensokyoAtlas.ALICE}) {
            Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
            BlockPos start=new BlockPos(site.x,site.y+1,site.z+site.rz+1);queue.add(start);reached.add(start);
            while(!queue.isEmpty()) {
                BlockPos p=queue.removeFirst();
                for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                    BlockPos n=p.offset(side).up(dy);
                    if(!site.contains(n.getX(),n.getZ(),2) || n.getY()<site.y+1 || n.getY()>site.y+36 || reached.contains(n))continue;
                    if(!generated.solid(n.down()) || generated.solid(n) || generated.solid(n.up()))continue;
                    if(dy>0 && generated.solid(p.up(2)) || dy<0 && generated.solid(n.up(2)))continue;
                    reached.add(n);queue.add(n);break;
                }
            }
            List<String> missing=new ArrayList<>();int rooms=0;
            for(GensokyoBlueprint.Room room:GensokyoStructures.create().rooms())if(site.contains(room.x,room.z,0)) {
                rooms++;if(!reached.contains(new BlockPos(room.x,room.y,room.z)))missing.add(room.name+" ("+(room.x-site.x)+","+(room.y-site.y)+","+(room.z-site.z)+")");
            }
            assertTrue("Unreachable "+missing,missing.isEmpty());assertTrue(rooms>=5);
        }
    }
    @Test public void bedsChestsAndDollsAreSupportedAndUnobstructed() {
        Generated generated=new Generated(12345);int dolls=0,beds=0,chests=0;
        for(GensokyoAtlas site:new GensokyoAtlas[]{GensokyoAtlas.MARISA,GensokyoAtlas.ALICE})
            for(int x=-site.rx;x<=site.rx;x++)for(int z=-site.rz;z<=site.rz;z++)for(int y=1;y<18;y++) {
                BlockPos p=new BlockPos(site.x+x,site.y+y,site.z+z);IBlockState state=generated.at(p);
                if(state.getBlock()==Blocks.BED) {
                    beds++;boolean head=state.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD;
                    EnumFacing facing=state.getValue(BlockBed.FACING);IBlockState other=generated.at(p.offset(head?facing.getOpposite():facing));
                    assertSame("Missing bed half "+p,Blocks.BED,other.getBlock());
                    assertNotEquals(state.getValue(BlockBed.PART),other.getValue(BlockBed.PART));
                    assertTrue(generated.at(p.down()).isFullCube());
                } else if(state.getBlock()==Blocks.CHEST) {
                    chests++;assertFalse("Blocked chest "+p,generated.at(p.up()).isFullCube());assertTrue(generated.at(p.down()).isFullCube());
                } else if(state.getBlock()==ModBlocks.DOLL_DISPLAY) {
                    dolls++;IBlockState support=generated.at(p.down());
                    assertTrue("Floating doll "+p,support.isFullCube() || support.getBlock()==Blocks.WOODEN_SLAB && (support.getBlock().getMetaFromState(support)&8)!=0);
                    assertFalse(generated.at(p.up()).isFullCube());
                }
            }
        assertEquals(6,beds);assertTrue(chests>=15);assertTrue(dolls>=12);
    }
    @Test public void forestKeepsMushroomsAndStableGenerationAtChunkBoundaries() {
        Generated first=new Generated(12345),second=new Generated(12345);int mushrooms=0,logs=0;
        int ox=GensokyoAtlas.FAIRY_OLD_TREE.x+80,oz=GensokyoAtlas.FAIRY_OLD_TREE.z+80;
        for(int x=ox;x<ox+48;x++)for(int z=oz;z<oz+48;z++) {
            int ground=new GensokyoTerrain(12345).column(x,z).ground;
            for(int y=ground;y<ground+34;y++) {
                BlockPos p=new BlockPos(x,y,z);IBlockState s=first.at(p);
                if(s.getBlock()==Blocks.LOG)logs++;
                if(s.getBlock()==Blocks.BROWN_MUSHROOM || s.getBlock()==Blocks.RED_MUSHROOM) {
                    mushrooms++;assertTrue(first.at(p.down()).isFullCube());
                }
                // Query the adjacent chunk before the original in the second independent generator.
                second.at(p.add(16,0,16));assertEquals(s,second.at(p));
            }
        }
        assertTrue(logs>100);assertTrue(mushrooms>10);
    }
    @Test public void generatedWorkshopContainersCarryUsableLootAndPersistTheirSeeds() {
        dev.lostfantasy.TestWorld world=new dev.lostfantasy.TestWorld();
        GensokyoGenerator generator=new GensokyoGenerator(world,12345);
        net.minecraft.world.storage.loot.LootTableManager manager=new net.minecraft.world.storage.loot.LootTableManager(null);
        int[][] chests={{0,-27,3,6},{0,-19,3,6},{1,-15,3,-27},{1,6,3,-27}};
        String[] tables={"forest_alchemy","forest_books","doll_materials","doll_materials"};
        for(int i=0;i<chests.length;i++) {
            GensokyoAtlas site=chests[i][0]==0?GensokyoAtlas.MARISA:GensokyoAtlas.ALICE;
            BlockPos p=new BlockPos(site.x+chests[i][1],site.y+chests[i][2],site.z+chests[i][3]);
            net.minecraft.world.chunk.Chunk chunk=generator.generateChunk(p.getX()>>4,p.getZ()>>4);
            net.minecraft.tileentity.TileEntityChest tile=(net.minecraft.tileentity.TileEntityChest)chunk.getTileEntityMap().get(p);
            assertNotNull(tile);net.minecraft.nbt.NBTTagCompound data=tile.writeToNBT(new net.minecraft.nbt.NBTTagCompound());
            net.minecraft.util.ResourceLocation name=new net.minecraft.util.ResourceLocation("lostfantasy","chests/"+tables[i]);
            assertEquals(name.toString(),data.getString("LootTable"));
            net.minecraft.tileentity.TileEntityChest restored=new net.minecraft.tileentity.TileEntityChest();restored.readFromNBT(data);
            assertEquals(data,restored.writeToNBT(new net.minecraft.nbt.NBTTagCompound()));
            net.minecraft.world.storage.loot.LootTable loot=manager.getLootTableFromLocation(name);
            assertNotSame(net.minecraft.world.storage.loot.LootTable.EMPTY_LOOT_TABLE,loot);
            assertFalse(loot.generateLootForPools(new Random(data.getLong("LootTableSeed")),
                    new net.minecraft.world.storage.loot.LootContext(0,null,manager,null,null,null)).isEmpty());
        }
    }
    private static final class Generated {
        final GensokyoGenerator generator;final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        Generated(long seed) {generator=new GensokyoGenerator(null,seed);}
        IBlockState at(BlockPos p) {
            int cx=p.getX()>>4,cz=p.getZ()>>4;long key=GensokyoAtlas.key(cx,cz);
            return chunks.computeIfAbsent(key,k->generator.primer(cx,cz)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);
        }
        boolean solid(BlockPos p) {IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET;}
    }
}
