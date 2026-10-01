package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import java.util.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class GenbuRavineTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.GENBU;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void riverBanksContainTheWaterAndColumnsFormRealRockWalls() {
        for(long seed:new long[]{0,17,12345}) {
            GensokyoTerrain terrain=new GensokyoTerrain(seed);int cliffs=0,benches=0;
            for(int z=-1970;z<=-1650;z+=4) {
                int middle=(int)Math.round(KappaWatercourse.centreX(z));
                for(int x=middle-100;x<=middle+100;x++) {
                    GensokyoTerrain.Column c=terrain.column(x,z);
                    if(c.basalt) {
                        if(c.ground>=132)cliffs++;
                        if(c.ground>=112 && c.ground<=119)benches++;
                    }
                    if(!c.wet())continue;
                    for(EnumFacing side:EnumFacing.HORIZONTALS) {
                        GensokyoTerrain.Column bank=terrain.column(x+side.getXOffset(),z+side.getZOffset());
                        assertTrue("low river bank "+seed+" "+x+","+z+" -> "+side+" "+bank.ground+" / "+c.water,
                                bank.wet() || bank.ground>=c.water);
                    }
                }
            }
            assertTrue("column walls",cliffs>700);assertTrue("lower rock benches",benches>700);
        }
        Generated g=new Generated();int rock=0;
        for(int x=-35;x<=45;x++)for(int z=-95;z<=-60;z++) {
            GensokyoTerrain.Column c=new GensokyoTerrain(12345).column(x,SITE.z+z);
            if(c.basalt && c.ground>129) {
                assertSame(ModBlocks.COLUMNAR_BASALT,g.at(new BlockPos(x,c.ground,SITE.z+z)).getBlock());rock++;
            }
        }
        assertTrue(rock>50);
    }
    @Test public void cavesAndFullBankWalkAreDryAndConnectedToTheUpstreamWorkshop() {
        Generated g=new Generated();Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        BlockPos start=local(232,5,-312);queue.add(start);reached.add(start);assertTrue(g.walkable(start));
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing facing:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(facing).up(dy);
                if(n.getX()<-242 || n.getX()>240 || n.getZ()<SITE.z-320 || n.getZ()>SITE.z+130
                        || n.getY()<SITE.y-3 || n.getY()>SITE.y+8 || reached.contains(n))continue;
                if(!g.walkable(n) || dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(n.up(2)))continue;
                reached.add(n);queue.add(n);break;
            }
        }
        List<String> missing=new ArrayList<>();int count=0;
        for(GensokyoBlueprint.Room room:GensokyoStructures.create().rooms())if(room.name.startsWith(SITE.title+"·")) {
            count++;if(!reached.contains(new BlockPos(room.x,room.y,room.z)))missing.add(room.name+" "+new BlockPos(room.x-SITE.x,room.y-SITE.y,room.z-SITE.z));
        }
        assertTrue(missing.toString(),missing.isEmpty());assertEquals(10,count);
        // Every bank station remains available; a reachable chamber alone does not prove the long path.
        for(int z=-2118;z<=-1680;z++) {
            int x=(int)Math.round(KappaWatercourse.centreX(z))-32;
            int y=(int)Math.floor(KappaWatercourse.level(z))+4;
            if(z>=-1831 && z<=-1819 && x>=34 && x<=61)y=SITE.y+1;
            assertTrue("bank walk "+x+","+y+","+z,reached.contains(new BlockPos(x,y,z)));
        }
    }
    @Test public void usefulCaveFurnitureHasSupportHeadroomAndNoEmissiveMoss() {
        Generated g=new Generated();int chests=0,moss=0,crafting=0,lamps=0;
        for(int x=-185;x<=65;x++)for(int z=-52;z<=20;z++)for(int y=-4;y<=16;y++) {
            BlockPos p=local(x,y,z);IBlockState s=g.at(p);
            if(s.getBlock()==Blocks.CHEST) {
                chests++;assertTrue("chest support "+p,g.at(p.down()).isFullCube());
                assertFalse("chest lid "+p,g.at(p.up()).isFullCube());
                boolean accessible=false;
                for(EnumFacing facing:EnumFacing.HORIZONTALS)accessible|=g.walkable(p.offset(facing));
                assertTrue("chest access "+p,accessible);
            }
            if(s.getBlock()==ModBlocks.MOSSY_BASALT) {moss++;assertEquals(0,s.getLightValue());}
            if(s.getBlock()==Blocks.CRAFTING_TABLE) {crafting++;assertTrue(g.at(p.down()).isFullCube());}
            if(s.getBlock()==ModBlocks.LIBRARY_LAMP) {lamps++;assertTrue("lamp support "+p,g.solid(p.down()));}
        }
        assertEquals(7,chests);assertEquals(3,crafting);assertEquals("seven cave and three nearby path lamps",10,lamps);assertTrue(moss>150);
    }
    @Test public void caveSlicesAreOrderIndependentAndSuppliesSurviveSaving() {
        Generated a=new Generated(),b=new Generated();
        for(int cx=-10;cx<=3;cx+=2)for(int cz=(SITE.z-48)>>4;cz<=(SITE.z+16)>>4;cz++) {
            ChunkPrimer expected=a.chunk(cx,cz);b.chunk(cx+1,cz+1);ChunkPrimer actual=b.chunk(cx,cz);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=SITE.y-5;y<=SITE.y+17;y++)assertEquals(expected.getBlockState(x,y,z),actual.getBlockState(x,y,z));
        }
        GensokyoGenerator generator=new GensokyoGenerator(new TestWorld(),12345);
        for(int[] position:new int[][]{{-29,1,-25},{-57,1,-36}}) {
            BlockPos p=local(position[0],position[1],position[2]);Chunk chunk=generator.generateChunk(p.getX()>>4,p.getZ()>>4);
            TileEntityChest tile=(TileEntityChest)chunk.getTileEntityMap().get(p);assertNotNull(tile);
            NBTTagCompound nbt=tile.writeToNBT(new NBTTagCompound());assertTrue(nbt.hasKey("LootTable"));
            TileEntityChest restored=new TileEntityChest();restored.readFromNBT(nbt);assertEquals(nbt,restored.writeToNBT(new NBTTagCompound()));
        }
    }
    private static BlockPos local(int x,int y,int z) {return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
    private static final class Generated {
        final GensokyoGenerator generator=new GensokyoGenerator(null,12345);
        final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        ChunkPrimer chunk(int x,int z) {return chunks.computeIfAbsent(GensokyoAtlas.key(x,z),key->generator.primer(x,z));}
        IBlockState at(BlockPos p) {return chunk(p.getX()>>4,p.getZ()>>4).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && at(p).getMaterial()!=Material.WATER;}
    }
}
