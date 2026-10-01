package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import java.util.*;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.storage.loot.*;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class VillageLandmarksTest {
    private static final GensokyoAtlas[] SITES={GensokyoAtlas.SUZUNAAN,GensokyoAtlas.TERAKOYA,GensokyoAtlas.HIEDA};
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void everyLandmarkRoomIsReachableFromOutside() {
        Generated generated=new Generated();GensokyoBlueprint plan=GensokyoStructures.create();
        for(GensokyoAtlas site:SITES) {
            Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
            BlockPos start=new BlockPos(site.x,site.y+1,site.z+site.rz+1);queue.add(start);reached.add(start);
            while(!queue.isEmpty()) {
                BlockPos p=queue.removeFirst();
                for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                    BlockPos n=p.offset(side).up(dy);
                    if(!site.contains(n.getX(),n.getZ(),3) || n.getY()<site.y+1 || n.getY()>site.y+17 || reached.contains(n))continue;
                    if(!generated.solid(n.down()) || generated.solid(n) || generated.solid(n.up()))continue;
                    if(dy>0 && generated.solid(p.up(2)) || dy<0 && generated.solid(n.up(2)))continue;
                    reached.add(n);queue.add(n);break;
                }
            }
            List<String> missing=new ArrayList<>();int rooms=0;
            for(GensokyoBlueprint.Room room:plan.rooms())if(room.name.startsWith(site.title+"·")) {
                rooms++;if(!reached.contains(new BlockPos(room.x,room.y,room.z)))missing.add(room.name+" "+new BlockPos(room.x-site.x,room.y-site.y,room.z-site.z));
            }
            assertTrue(missing.toString(),missing.isEmpty());assertTrue(rooms>=6);
        }
    }
    @Test public void livingAndWorkFurnitureSurvivesRoofAndWallConstruction() {
        Generated generated=new Generated();int beds=0,chests=0,gramophones=0,desks=0;
        for(GensokyoAtlas site:SITES)for(int x=-site.rx;x<=site.rx;x++)for(int z=-site.rz;z<=site.rz;z++)for(int y=1;y<13;y++) {
            BlockPos p=new BlockPos(site.x+x,site.y+y,site.z+z);IBlockState s=generated.at(p);
            if(s.getBlock()==Blocks.BED) {
                beds++;assertTrue("Bed support "+p,generated.at(p.down()).isFullCube());
                EnumFacing direction=s.getValue(BlockBed.FACING);
                assertSame("Bed half "+p,Blocks.BED,generated.at(p.offset(s.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?direction.getOpposite():direction)).getBlock());
                assertFalse("Bed headroom "+p,generated.solid(p.up()));
            } else if(s.getBlock()==Blocks.CHEST) {
                chests++;assertTrue("Chest support "+p,generated.at(p.down()).isFullCube());assertFalse("Chest lid "+p,generated.at(p.up()).isFullCube());
            } else if(s.getBlock()==ModBlocks.GRAMOPHONE || s.getBlock()==ModBlocks.WRITING_DESK) {
                if(s.getBlock()==ModBlocks.GRAMOPHONE)gramophones++;else desks++;
                assertTrue("Furniture support "+p,generated.at(p.down()).isFullCube());assertFalse("Furniture headroom "+p,generated.at(p.up()).isFullCube());
            } else if(s.getBlock()==Blocks.WALL_SIGN) {
                assertTrue(generated.at(p.offset(s.getValue(BlockHorizontal.FACING).getOpposite())).isFullCube());
            }
        }
        assertEquals(20,beds);assertEquals(2,gramophones);assertTrue(desks>=70);assertTrue(chests>=25);
    }
    @Test public void canalPassesInFrontOfSuzunaanAndBridgesStayDry() {
        GensokyoTerrain terrain=new GensokyoTerrain(12345);Generated generated=new Generated();GensokyoAtlas v=GensokyoAtlas.VILLAGE;
        for(int x:new int[]{-180,-112,-50,0,80,160}) {
            GensokyoTerrain.Column c=terrain.column(v.x+x,v.z+190);assertTrue(c.wet());assertEquals(81,c.water);
            assertEquals(78,c.ground);assertEquals(83,c.roadY<0?83:c.roadY);
        }
        for(int x:new int[]{0,120}) {
            BlockPos deck=new BlockPos(v.x+x,83,v.z+190);
            assertSame(Blocks.PLANKS,generated.at(deck).getBlock());assertFalse(generated.solid(deck.up()));
            assertSame(Blocks.WATER,generated.at(deck.down(2)).getBlock());
        }
        GensokyoAtlas h=GensokyoAtlas.HIEDA;
        assertFalse(terrain.column(h.x,h.z+h.rz).wet());
        assertTrue(terrain.column(h.x,v.z+228).wet());
    }
    @Test public void newSupplyTablesLoadAndGenerateUsableItems() {
        LootTableManager tables=new LootTableManager(null);
        for(String name:new String[]{"bookbinding","school_supplies","hieda_records","village_pantry"}) {
            LootTable table=tables.getLootTableFromLocation(new ResourceLocation("lostfantasy","chests/"+name));
            assertNotSame(name,LootTable.EMPTY_LOOT_TABLE,table);
            assertFalse(name,table.generateLootForPools(new Random(91),new LootContext(0,null,tables,null,null,null)).isEmpty());
        }
    }
    private static final class Generated {
        final GensokyoGenerator generator=new GensokyoGenerator(null,12345);final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        IBlockState at(BlockPos p) {
            int cx=p.getX()>>4,cz=p.getZ()>>4;
            return chunks.computeIfAbsent(GensokyoAtlas.key(cx,cz),key->generator.primer(cx,cz)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);
        }
        boolean solid(BlockPos p) {
            IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET && s.getBlock()!=Blocks.WALL_SIGN;
        }
    }
}
