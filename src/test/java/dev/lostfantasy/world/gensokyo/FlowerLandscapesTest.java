package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import java.util.*;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.storage.loot.*;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static dev.lostfantasy.world.gensokyo.GensokyoAtlas.*;

public class FlowerLandscapesTest {
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}

    @Test public void sunflowerBowlAndTheSingleLilyHillHaveDifferentProfiles() {
        for(long seed:new long[]{0,12345,734901,Long.MIN_VALUE}) {
            GensokyoTerrain t=new GensokyoTerrain(seed);
            int middle=t.column(SUN_GARDEN.x+35,SUN_GARDEN.z).ground;
            int north=t.column(SUN_GARDEN.x+35,SUN_GARDEN.z-265).ground;
            int south=t.column(SUN_GARDEN.x+35,SUN_GARDEN.z+265).ground;
            assertTrue("bowl north "+seed,north>middle+10);
            assertTrue("bowl south "+seed,south>middle+10);
            assertTrue("southward incline "+seed,north>south+2);
            int shoulder=t.column(NAMELESS_HILL.x-40,NAMELESS_HILL.z).ground;
            int field=t.column(SUZURAN.x-40,SUZURAN.z).ground;
            assertTrue("one descending hill "+seed,shoulder>field+12);
            int previous=0;
            int steps=(int)Math.ceil(Math.hypot(SUZURAN.x-NAMELESS_HILL.x,SUZURAN.z-NAMELESS_HILL.z));
            for(int i=0;i<=steps;i++) {
                int x=(int)Math.round(NAMELESS_HILL.x+(SUZURAN.x-NAMELESS_HILL.x)*i/(double)steps)-40;
                int z=(int)Math.round(NAMELESS_HILL.z+(SUZURAN.z-NAMELESS_HILL.z)*i/(double)steps);
                GensokyoTerrain.Column c=t.column(x,z);
                assertEquals(GensokyoTerrain.Region.FLOWERS,c.region);assertFalse("dry lily slope",c.wet());
                if(i>0)assertTrue("continuous slope "+seed+" at "+x+","+z+": "+previous+" -> "+c.ground,Math.abs(c.ground-previous)<=1);
                previous=c.ground;
            }
            // The nearby fairy tower remains in its established forest.
            assertEquals(GensokyoTerrain.Region.FOREST,t.region(FAIRY_SHRINE.x,FAIRY_SHRINE.z));
        }
    }

    @Test public void generatedFlowersAreDenseSupportedAndSpeciesStaySeparate() {
        Generated g=new Generated(12345);
        int sun=0,lilies=0;
        for(int dx=-122;dx<-58;dx++)for(int dz=24;dz<88;dz++) {
            int x=SUN_GARDEN.x+dx,z=SUN_GARDEN.z+dz;
            GensokyoTerrain.Column c=g.terrain.column(x,z);IBlockState s=g.at(new BlockPos(x,c.ground+1,z));
            assertNotSame(ModBlocks.SUZURAN,s.getBlock());
            if(s.getBlock()==Blocks.DOUBLE_PLANT) {
                sun++;assertEquals(BlockDoublePlant.EnumPlantType.SUNFLOWER,s.getValue(BlockDoublePlant.VARIANT));
                assertSame(Blocks.GRASS,g.at(new BlockPos(x,c.ground,z)).getBlock());
                IBlockState upper=g.at(new BlockPos(x,c.ground+2,z));assertSame(Blocks.DOUBLE_PLANT,upper.getBlock());
                assertEquals(BlockDoublePlant.EnumBlockHalf.UPPER,upper.getValue(BlockDoublePlant.HALF));
            }
        }
        for(int step=0;step<=2;step++) {
            int cx=NAMELESS_HILL.x+(SUZURAN.x-NAMELESS_HILL.x)*step/2-80;
            int cz=NAMELESS_HILL.z+(SUZURAN.z-NAMELESS_HILL.z)*step/2;
            int patch=0;
            for(int dx=-24;dx<24;dx++)for(int dz=-24;dz<24;dz++) {
                int x=cx+dx,z=cz+dz;GensokyoTerrain.Column c=g.terrain.column(x,z);
                IBlockState s=g.at(new BlockPos(x,c.ground+1,z));
                assertNotSame(Blocks.DOUBLE_PLANT,s.getBlock());
                if(s.getBlock()==ModBlocks.SUZURAN) {
                    patch++;assertSame(Blocks.GRASS,g.at(new BlockPos(x,c.ground,z)).getBlock());
                    assertSame(Blocks.AIR,g.at(new BlockPos(x,c.ground+2,z)).getBlock());
                }
            }
            assertTrue("continuous lily patch "+step,patch>800);lilies+=patch;
        }
        assertTrue("sunflower mass",sun>1000);assertTrue(lilies>2400);
        assertEquals(0,ModBlocks.SUZURAN.getDefaultState().getLightValue());
        assertEquals(BlockRenderLayer.CUTOUT_MIPPED,ModBlocks.SUZURAN.getRenderLayer());
    }

    @Test public void flowerMeadowPathsAndViewOpeningsRemainClear() {
        Generated g=new Generated(734901);
        for(int[] route:new int[][]{{0,0,40,135},{40,135,110,135},{110,135,110,125},{0,0,-155,78},{-155,78,-255,105},{110,125,160,125},{160,125,222,-35},{222,-35,277,-25}}) {
            int steps=(int)Math.ceil(Math.hypot(route[2]-route[0],route[3]-route[1]));BlockPos previous=null;
            for(int i=0;i<=steps;i++) {
                int x=SUN_GARDEN.x+(int)Math.round(route[0]+(route[2]-route[0])*i/(double)steps);
                int z=SUN_GARDEN.z+(int)Math.round(route[1]+(route[3]-route[1])*i/(double)steps);
                int y=g.terrain.column(x,z).ground;BlockPos p=new BlockPos(x,y+1,z);
                assertSame("no stems on route "+p,Blocks.AIR,g.at(p).getBlock());assertTrue("passage "+p,g.walkable(p));
                if(previous!=null)assertTrue("no route cliff "+p,Math.abs(p.getY()-previous.getY())<=1);
                previous=p;
            }
        }
        for(int[] offset:new int[][]{{-155,78},{222,-35}}) {
            int x=SUN_GARDEN.x+offset[0],z=SUN_GARDEN.z+offset[1],y=g.terrain.column(x,z).ground;
            for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++) {
                BlockPos p=new BlockPos(x+dx,g.terrain.column(x+dx,z+dz).ground+1,z+dz);
                assertSame("open view "+p,Blocks.AIR,g.at(p).getBlock());
            }
        }
    }

    @Test public void concertStageHasConnectedEquipmentAndAudienceSpaces() {
        Generated g=new Generated(12345);Set<BlockPos> reached=walkStage(g);int rooms=0,chests=0,notes=0,lights=0;
        for(GensokyoBlueprint.Room room:GensokyoStructures.create().rooms())if(room.name.startsWith("太阳花田演奏舞台·")) {
            rooms++;assertTrue(room.name+" "+room.x+","+room.y+","+room.z,reached.contains(new BlockPos(room.x,room.y,room.z)));
        }
        for(int x=-23;x<=23;x++)for(int z=-22;z<=48;z++)for(int y=1;y<=18;y++) {
            BlockPos p=stage(x,y,z);IBlockState s=g.at(p);
            if(s.getBlock()==Blocks.CHEST || s.getBlock()==Blocks.NOTEBLOCK) {
                if(s.getBlock()==Blocks.CHEST) {
                    chests++;assertTrue(g.at(p.down()).isFullCube());assertFalse(g.solid(p.up()));
                    boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));assertTrue("chest access "+p,access);
                } else notes++;
            } else if(s.getBlock()==Blocks.GLOWSTONE)lights++;
        }
        assertEquals(7,rooms);assertEquals(2,chests);assertEquals(6,notes);assertEquals(7,lights);
    }

    @Test public void stageSuppliesUseSavedLootAndGenerationDoesNotDependOnChunkOrder() {
        ResourceLocation lootKey=new ResourceLocation("lostfantasy","chests/concert_supplies");
        LootTableManager manager=new LootTableManager(null);LootTable loot=manager.getLootTableFromLocation(lootKey);
        assertNotSame(LootTable.EMPTY_LOOT_TABLE,loot);assertFalse(loot.generateLootForPools(new Random(42),new LootContext(0,null,manager,null,null,null)).isEmpty());
        TestWorld world=new TestWorld();world.loaded=false;GensokyoGenerator gen=new GensokyoGenerator(world,12345);
        for(int x:new int[]{-11,11}) {
            BlockPos p=stage(x,3,-14);Chunk c=gen.generateChunk(p.getX()>>4,p.getZ()>>4);
            TileEntityChest chest=(TileEntityChest)c.getTileEntityMap().get(p);assertNotNull(chest);
            NBTTagCompound nbt=chest.writeToNBT(new NBTTagCompound());assertEquals(lootKey.toString(),nbt.getString("LootTable"));
            TileEntityChest restored=new TileEntityChest();restored.readFromNBT(nbt);assertEquals(nbt,restored.writeToNBT(new NBTTagCompound()));
        }
        Generated forward=new Generated(734901),reverse=new Generated(734901);
        List<int[]> chunks=new ArrayList<>();
        for(GensokyoAtlas site:new GensokyoAtlas[]{SUN_GARDEN,NAMELESS_HILL,SUZURAN})
            for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)chunks.add(new int[]{(site.x>>4)+dx,(site.z>>4)+dz});
        for(int[] p:chunks)forward.chunk(p[0],p[1]);Collections.reverse(chunks);
        for(int[] p:chunks) {
            ChunkPrimer a=forward.chunk(p[0],p[1]),b=reverse.chunk(p[0],p[1]);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=65;y<=150;y++)assertEquals(a.getBlockState(x,y,z),b.getBlockState(x,y,z));
        }
    }

    private static BlockPos stage(int x,int y,int z) {return new BlockPos(SUN_GARDEN.x+FlowerLandscapes.STAGE_DX+x,SUN_GARDEN.y+y,SUN_GARDEN.z+FlowerLandscapes.STAGE_DZ+z);}
    private static Set<BlockPos> walkStage(Generated g) {
        BlockPos start=stage(0,1,47);Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        assertTrue(g.walkable(start));reached.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing f:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(f).up(dy);int x=n.getX()-SUN_GARDEN.x-FlowerLandscapes.STAGE_DX,z=n.getZ()-SUN_GARDEN.z-FlowerLandscapes.STAGE_DZ;
                if(Math.abs(x)>23 || z<-22 || z>48 || n.getY()<SUN_GARDEN.y+1 || n.getY()>SUN_GARDEN.y+8 || reached.contains(n))continue;
                if(!g.walkable(n) || dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(n.up(2)))continue;
                reached.add(n);queue.add(n);break;
            }
        }
        return reached;
    }
    private static final class Generated {
        final GensokyoGenerator generator;final GensokyoTerrain terrain;final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        Generated(long seed) {generator=new GensokyoGenerator(null,seed);terrain=new GensokyoTerrain(seed);}
        ChunkPrimer chunk(int x,int z) {return chunks.computeIfAbsent(GensokyoAtlas.key(x,z),k->generator.primer(x,z));}
        IBlockState at(BlockPos p) {return chunk(p.getX()>>4,p.getZ()>>4).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && at(p).getMaterial()!=Material.WATER;}
    }
}
