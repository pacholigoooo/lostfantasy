package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import java.util.*;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityOcelot;
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

public class MountainGardensTest {
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void allHousesFieldAislesAndPondShrineAreReachable() {
        Generated g=new Generated(12345);GensokyoBlueprint plan=GensokyoStructures.create();
        for(GensokyoAtlas site:new GensokyoAtlas[]{GensokyoAtlas.PEONY_FIELD,GensokyoAtlas.MAYOHIGA,GensokyoAtlas.TOAD_POND}) {
            boolean pond=site==GensokyoAtlas.TOAD_POND;
            Set<BlockPos> reached=walk(g,site,pond?local(site,104,5,24):local(site,0,1,site.rz+4));
            int count=0;List<String> missing=new ArrayList<>();
            for(GensokyoBlueprint.Room room:plan.rooms())if(room.name.startsWith(site.title+"·")) {
                count++;if(!reached.contains(new BlockPos(room.x,room.y,room.z)))missing.add(room.name+" "+new BlockPos(room.x-site.x,room.y-site.y,room.z-site.z));
            }
            assertEquals(site.title,pond?3:site==GensokyoAtlas.PEONY_FIELD?7:50,count);
            assertTrue(missing.toString(),missing.isEmpty());
        }
    }
    @Test public void furnitureFlowersAndWholeRoofsHaveSupport() {
        Generated g=new Generated(12345);int peonies=0,beds=0,chests=0,lamps=0;
        for(GensokyoAtlas site:new GensokyoAtlas[]{GensokyoAtlas.PEONY_FIELD,GensokyoAtlas.MAYOHIGA}) {
            Set<BlockPos> reached=walk(g,site,local(site,0,1,site.rz+4));
            for(int x=-site.rx;x<=site.rx;x++)for(int z=-site.rz;z<=site.rz;z++)for(int y=1;y<=13;y++) {
                BlockPos p=local(site,x,y,z);IBlockState state=g.at(p);
                if(state.getBlock()==Blocks.BED || state.getBlock()==Blocks.CHEST) {
                    assertTrue("floor "+p,g.at(p.down()).isFullCube());assertFalse("ceiling "+p,g.solid(p.up()));
                    boolean access=false;for(EnumFacing facing:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(facing));assertTrue("access "+p,access);
                    if(state.getBlock()==Blocks.CHEST)chests++;else {
                        beds++;EnumFacing facing=state.getValue(BlockBed.FACING);
                        assertSame(Blocks.BED,g.at(p.offset(state.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?facing.getOpposite():facing)).getBlock());
                    }
                } else if(state.getBlock()==ModBlocks.RED_LANTERN) {lamps++;assertTrue(g.solid(p.up()));}
                else if(state.getBlock()==Blocks.DOUBLE_PLANT && state.getValue(BlockDoublePlant.HALF)==BlockDoublePlant.EnumBlockHalf.LOWER) {
                    peonies++;assertEquals(BlockDoublePlant.EnumPlantType.PAEONIA,state.getValue(BlockDoublePlant.VARIANT));
                    assertSame(Blocks.DIRT,g.at(p.down()).getBlock());
                    assertSame(Blocks.DOUBLE_PLANT,g.at(p.up()).getBlock());
                    assertEquals(BlockDoublePlant.EnumBlockHalf.UPPER,g.at(p.up()).getValue(BlockDoublePlant.HALF));
                }
            }
        }
        assertTrue(peonies>350);assertEquals(32,beds);assertEquals(24,chests);assertEquals(12,lamps);
        for(MayohigaHomes.House house:MayohigaHomes.HOUSES)
            for(int x=-house.h;x<=house.h;x++)for(int z=-house.d;z<=house.d;z++)
                assertTrue("roof",g.at(house.world(x,1+5*house.storeys,z)).isFullCube());
    }
    @Test public void pondWaterIsEnclosedAndTheRoadStaysOnLand() {
        GensokyoAtlas site=GensokyoAtlas.TOAD_POND;
        for(long seed:new long[]{0,12345,Long.MIN_VALUE}) {
            GensokyoTerrain terrain=new GensokyoTerrain(seed);Generated g=new Generated(seed);int flowers=0;
            for(int x=-72;x<=72;x++)for(int z=-55;z<=55;z++) {
                GensokyoTerrain.Column c=terrain.column(site.x+x,site.z+z);
                if(c.wet()) {
                    assertEquals(site.y,c.water);assertFalse("bridge through pond",c.path());
                    for(EnumFacing face:EnumFacing.HORIZONTALS) {
                        GensokyoTerrain.Column n=terrain.column(site.x+x+face.getXOffset(),site.z+z+face.getZOffset());
                        assertTrue("open bank "+x+","+z,n.water>=c.water || n.ground>=c.water);
                    }
                }
                BlockPos p=local(site,x,1,z);
                if(g.at(p).getBlock()==ModBlocks.POND_LOTUS) {flowers++;assertSame(Blocks.WATER,g.at(p.down()).getBlock());}
            }
            assertTrue("lotus patches",flowers>20);
            assertTrue(g.at(local(site,0,1,0)).getBlock()!=Blocks.PLANKS);
        }
        TestWorld world=new TestWorld();BlockPos p=new BlockPos(0,65,0);
        world.blocks.put(p.down(),Blocks.WATER.getDefaultState());assertTrue(((dev.lostfantasy.block.PondLotus)ModBlocks.POND_LOTUS).canBlockStay(world,p,ModBlocks.POND_LOTUS.getDefaultState()));
        world.blocks.put(p.down(),Blocks.WATER.getStateFromMeta(2));assertFalse(((dev.lostfantasy.block.PondLotus)ModBlocks.POND_LOTUS).canBlockStay(world,p,ModBlocks.POND_LOTUS.getDefaultState()));
        world.blocks.put(p.down(),Blocks.DIRT.getDefaultState());assertFalse(((dev.lostfantasy.block.PondLotus)ModBlocks.POND_LOTUS).canBlockStay(world,p,ModBlocks.POND_LOTUS.getDefaultState()));
        assertEquals(0,ModBlocks.POND_LOTUS.getDefaultState().getLightValue());
    }
    @Test public void catsAreChunkLocalUntamedAndSaveTheirAppearance() {
        TestWorld world=new TestWorld();GensokyoGenerator generator=new GensokyoGenerator(world,12345);
        Set<Long> keys=new HashSet<>();GensokyoAtlas site=GensokyoAtlas.MAYOHIGA;int cats=0;Set<UUID> ids=new HashSet<>();
        for(int[] p:MayohigaVillage.CATS)keys.add(GensokyoAtlas.key((site.x+p[0])>>4,(site.z+p[2])>>4));
        for(long key:keys) {
            Chunk chunk=generator.generateChunk((int)(key>>32),(int)key);
            for(Iterable<Entity> list:chunk.getEntityLists())for(Entity entity:list)if(entity instanceof EntityOcelot) {
                cats++;EntityOcelot cat=(EntityOcelot)entity;assertFalse(cat.isTamed());assertTrue(cat.isNoDespawnRequired());
                assertTrue(cat.getTameSkin()>=1 && cat.getTameSkin()<=3);assertTrue(ids.add(cat.getUniqueID()));
                assertTrue(chunk.getBlockState(cat.getPosition().down()).isFullCube());
                NBTTagCompound nbt=cat.writeToNBT(new NBTTagCompound());EntityOcelot restored=new EntityOcelot(world);restored.readFromNBT(nbt);
                assertEquals(cat.getTameSkin(),restored.getTameSkin());assertEquals(cat.getUniqueID(),restored.getUniqueID());assertTrue(restored.isNoDespawnRequired());assertFalse(restored.isTamed());
            }
        }
        assertEquals(12,cats);
    }
    @Test public void suppliesAndFieldNoticeUseNormalSavedContainers() {
        for(Object[] entry:new Object[][]{{GensokyoAtlas.PEONY_FIELD,23,2,14,"peony_tools"},{GensokyoAtlas.MAYOHIGA,-90,2,-79,"mayohiga_household"}}) {
            GensokyoAtlas site=(GensokyoAtlas)entry[0];BlockPos p=local(site,(int)entry[1],(int)entry[2],(int)entry[3]);
            Chunk chunk=new GensokyoGenerator(new TestWorld(),12345).generateChunk(p.getX()>>4,p.getZ()>>4);
            TileEntityChest chest=(TileEntityChest)chunk.getTileEntityMap().get(p);assertNotNull(chest);
            NBTTagCompound nbt=chest.writeToNBT(new NBTTagCompound());ResourceLocation key=new ResourceLocation("lostfantasy","chests/"+entry[4]);
            assertEquals(key.toString(),nbt.getString("LootTable"));TileEntityChest restored=new TileEntityChest();restored.readFromNBT(nbt);assertEquals(nbt,restored.writeToNBT(new NBTTagCompound()));
            LootTableManager manager=new LootTableManager(null);LootTable loot=manager.getLootTableFromLocation(key);assertNotSame(LootTable.EMPTY_LOOT_TABLE,loot);
            assertFalse(loot.generateLootForPools(new Random(18),new LootContext(0,null,manager,null,null,null)).isEmpty());
        }
        BlockPos p=local(GensokyoAtlas.PEONY_FIELD,4,3,32);Chunk chunk=new GensokyoGenerator(new TestWorld(),12345).generateChunk(p.getX()>>4,p.getZ()>>4);
        TileEntitySign sign=(TileEntitySign)chunk.getTileEntityMap().get(p);assertNotNull(sign);assertEquals("永远亭管理",sign.signText[2].getUnformattedText());
    }
    @Test public void gardensAndPondCanGenerateInEitherChunkOrder() {
        Generated one=new Generated(12345),two=new Generated(12345);
        for(GensokyoAtlas site:new GensokyoAtlas[]{GensokyoAtlas.PEONY_FIELD,GensokyoAtlas.MAYOHIGA,GensokyoAtlas.TOAD_POND})
            for(int cx=(site.x-32)>>4;cx<=(site.x+96)>>4;cx++)for(int cz=(site.z-32)>>4;cz<=(site.z+32)>>4;cz++) {
                ChunkPrimer a=one.chunk(cx,cz);two.chunk(cx+1,cz-1);ChunkPrimer b=two.chunk(cx,cz);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=site.y-8;y<site.y+40;y++)assertEquals(a.getBlockState(x,y,z),b.getBlockState(x,y,z));
            }
    }
    private static BlockPos local(GensokyoAtlas site,int x,int y,int z) {return new BlockPos(site.x+x,site.y+y,site.z+z);}
    private static Set<BlockPos> walk(Generated g,GensokyoAtlas site,BlockPos start) {
        Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();assertTrue("start "+start,g.walkable(start));reached.add(start);queue.add(start);
        boolean pond=site==GensokyoAtlas.TOAD_POND;
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing face:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(face).up(dy);
                if(n.getX()<site.x+(pond?55:-site.rx-2) || n.getX()>site.x+(pond?108:site.rx+2) || n.getZ()<site.z-(pond?51:site.rz+2) || n.getZ()>site.z+(pond?56:site.rz+6) || n.getY()<site.y+1 || n.getY()>site.y+13 || reached.contains(n))continue;
                if(!g.walkable(n) || dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(n.up(2)))continue;
                reached.add(n);queue.add(n);break;
            }
        }
        return reached;
    }
    private static final class Generated {
        final GensokyoGenerator generator;final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        Generated(long seed) {generator=new GensokyoGenerator(null,seed);}
        ChunkPrimer chunk(int x,int z) {return chunks.computeIfAbsent(GensokyoAtlas.key(x,z),k->generator.primer(x,z));}
        IBlockState at(BlockPos p) {return chunk(p.getX()>>4,p.getZ()>>4).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState state=at(p);return state.getMaterial().blocksMovement() && state.getBlock()!=Blocks.CARPET;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && at(p).getMaterial()!=Material.WATER;}
    }
}
