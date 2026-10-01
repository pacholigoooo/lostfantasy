package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import java.util.*;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.storage.loot.*;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class RoadsideBuildingsTest {
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void shopWarehouseHomeAndStallCanBeReachedFromTheirApproaches() {
        Generated generated=new Generated();GensokyoBlueprint plan=GensokyoStructures.create();
        for(GensokyoAtlas site:new GensokyoAtlas[]{GensokyoAtlas.KOURINDOU,GensokyoAtlas.MYSTIA}) {
            Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
            BlockPos start=new BlockPos(site.x,site.y+1,site.z+site.rz+1);queue.add(start);reached.add(start);
            while(!queue.isEmpty()) {
                BlockPos p=queue.removeFirst();
                for(EnumFacing facing:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                    BlockPos n=p.offset(facing).up(dy);
                    if(!site.contains(n.getX(),n.getZ(),3) || n.getY()<site.y+1 || n.getY()>site.y+23 || reached.contains(n))continue;
                    if(!generated.solid(n.down()) || generated.solid(n) || generated.solid(n.up()))continue;
                    if(dy>0 && generated.solid(p.up(2)) || dy<0 && generated.solid(n.up(2)))continue;
                    reached.add(n);queue.add(n);break;
                }
            }
            int rooms=0;List<String> missing=new ArrayList<>();
            for(GensokyoBlueprint.Room room:plan.rooms())if(site.contains(room.x,room.z,0)) {
                rooms++;if(!reached.contains(new BlockPos(room.x,room.y,room.z)))missing.add(room.name+" "+new BlockPos(room.x-site.x,room.y-site.y,room.z-site.z));
            }
            assertTrue(missing.toString(),missing.isEmpty());assertEquals(site==GensokyoAtlas.KOURINDOU?11:3,rooms);
        }
    }
    @Test public void furnitureHasSupportChestLidsAreClearAndSignsHaveBacking() {
        Generated generated=new Generated();int beds=0,signs=0,boxes=0;
        for(GensokyoAtlas site:new GensokyoAtlas[]{GensokyoAtlas.KOURINDOU,GensokyoAtlas.MYSTIA})
            for(int x=-site.rx;x<=site.rx;x++)for(int z=-site.rz;z<=site.rz;z++)for(int y=1;y<17;y++) {
                BlockPos p=new BlockPos(site.x+x,site.y+y,site.z+z);IBlockState s=generated.at(p);
                if(s.getBlock()==Blocks.CHEST) {
                    boxes++;assertTrue("Floating chest "+p,generated.at(p.down()).isFullCube());assertFalse("Blocked lid "+p,generated.at(p.up()).isFullCube());
                } else if(s.getBlock()==Blocks.BED) {
                    beds++;assertTrue(generated.at(p.down()).isFullCube());
                    EnumFacing direction=s.getValue(BlockBed.FACING);
                    assertSame(Blocks.BED,generated.at(p.offset(s.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?direction.getOpposite():direction)).getBlock());
                } else if(s.getBlock()==Blocks.WALL_SIGN) {
                    signs++;assertTrue("Floating sign "+p,generated.at(p.offset(s.getValue(BlockHorizontal.FACING).getOpposite())).isFullCube());
                } else if(s.getBlock()==ModBlocks.OUTSIDE_TELEVISION) {
                    assertTrue("Floating television "+p,generated.at(p.down()).isFullCube());
                }
            }
        assertEquals(2,beds);assertEquals(3,signs);assertTrue(boxes>=20);
    }
    @Test public void newlyGeneratedSignsAndSuppliesPersistWithoutNeighbourAccess() {
        GensokyoGenerator generator=new GensokyoGenerator(new TestWorld(),12345);
        GensokyoAtlas shop=GensokyoAtlas.KOURINDOU,stall=GensokyoAtlas.MYSTIA;
        for(BlockPos p:new BlockPos[]{new BlockPos(shop.x-3,shop.y+7,shop.z+20),new BlockPos(stall.x,stall.y+5,stall.z+5)}) {
            Chunk chunk=generator.generateChunk(p.getX()>>4,p.getZ()>>4);
            TileEntitySign sign=(TileEntitySign)chunk.getTileEntityMap().get(p);assertNotNull(sign);
            NBTTagCompound saved=sign.writeToNBT(new NBTTagCompound());
            TileEntitySign restored=new TileEntitySign();restored.readFromNBT(saved);
            assertEquals(sign.signText[1].getUnformattedText(),restored.signText[1].getUnformattedText());
            assertFalse(restored.signText[1].getUnformattedText().isEmpty());
        }
        LootTableManager tables=new LootTableManager(null);
        BlockPos[] containers={new BlockPos(shop.x-6,shop.y+2,shop.z+6),new BlockPos(stall.x+2,stall.y+2,stall.z-1)};
        String[] names={"kourindou_tools","night_stall"};
        for(int i=0;i<containers.length;i++) {
            BlockPos p=containers[i];Chunk chunk=generator.generateChunk(p.getX()>>4,p.getZ()>>4);
            TileEntityChest chest=(TileEntityChest)chunk.getTileEntityMap().get(p);assertNotNull(chest);
            NBTTagCompound data=chest.writeToNBT(new NBTTagCompound());
            ResourceLocation name=new ResourceLocation("lostfantasy","chests/"+names[i]);assertEquals(name.toString(),data.getString("LootTable"));
            LootTable loot=tables.getLootTableFromLocation(name);assertNotSame(LootTable.EMPTY_LOOT_TABLE,loot);
            assertFalse(loot.generateLootForPools(new Random(12),new LootContext(0,null,tables,null,null,null)).isEmpty());
        }
        assertEquals(12,ModBlocks.RED_LANTERN.getDefaultState().getLightValue());
        assertEquals(0,ModBlocks.OUTSIDE_TELEVISION.getDefaultState().getLightValue());
    }
    private static final class Generated {
        final GensokyoGenerator generator=new GensokyoGenerator(null,12345);final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        IBlockState at(BlockPos p) {
            int cx=p.getX()>>4,cz=p.getZ()>>4;
            return chunks.computeIfAbsent(GensokyoAtlas.key(cx,cz),k->generator.primer(cx,cz)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);
        }
        boolean solid(BlockPos p) {
            IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET && s.getBlock()!=Blocks.WALL_SIGN;
        }
    }
}
