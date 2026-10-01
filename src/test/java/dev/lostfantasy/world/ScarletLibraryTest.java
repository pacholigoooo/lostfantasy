package dev.lostfantasy.world;

import dev.lostfantasy.Balance;
import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import dev.lostfantasy.core.ResearchCatalog;
import dev.lostfantasy.world.gensokyo.GensokyoGenerator;
import dev.lostfantasy.world.gensokyo.GensokyoTestBlocks;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ScarletLibraryTest {
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void actualMansionRetainsOneCabinetPerDocumentAndAValidStudyDesk() {
        MansionWorld world=new MansionWorld();int count=0,empty=0;int[] documents=new int[6];
        for(int x=-52;x<=52;x++)for(int z=-61;z<=66;z++)for(int y=3;y<=5;y++) {
            BlockPos p=world.at(x,y,z);
            if(world.getBlockState(p).getBlock()!=ModBlocks.ARCHIVE_CATALOG)continue;
            count++;assertTrue(LibraryResearch.intact(world,world.site,p,LibraryResearch.CATALOG));
            int doc=ResearchCatalog.document(world.site.structureSeed,LibraryResearch.catalogAt(world.site,p));
            if(doc<0)empty++;else documents[doc]++;
            assertEquals(-1,LibraryResearch.catalogAt(world.site,p.up()));
            assertEquals(-1,LibraryResearch.catalogAt(world.site,p.down()));
        }
        assertEquals(8,count);assertEquals(2,empty);assertArrayEquals(new int[]{1,1,1,1,1,1},documents);
        assertTrue(LibraryResearch.intact(world,world.site,world.at(-2,5,0),LibraryResearch.TABLE));
        assertTrue(ScarletLibrary.contains(world.site,world.at(-2,5,0)));
        assertFalse(ScarletLibrary.contains(world.site,world.at(-2,70,0)));
        assertTrue(LibrarySites.dimension(Balance.gensokyoDimensionId));assertFalse(LibrarySites.dimension(-1));
    }
    @Test public void originalRewardLootTablesSurviveContainerSerialization() {
        MansionWorld world=new MansionWorld();Map<Long,Chunk> chunks=new HashMap<>();int count=0;
        int[][] positions={{-38,-36},{38,-36},{-38,36},{38,36},{-9,13},{9,13},{14,-10},{18,63}};
        for(int i=0;i<positions.length;i++) {
            BlockPos p=world.at(positions[i][0],3,positions[i][1]);int cx=p.getX()>>4,cz=p.getZ()>>4;
            long key=((long)cx<<32)^(cz&0xffffffffL);
            Chunk chunk=chunks.get(key);
            if(chunk==null) {
                chunk=new Chunk(world,world.generator.primer(cx,cz),cx,cz);ScarletLibrary.installContainers(chunk,world.site);chunks.put(key,chunk);
            }
            assertSame(Blocks.CHEST,chunk.getBlockState(p).getBlock());
            TileEntityChest chest=(TileEntityChest)chunk.getTileEntityMap().get(p);assertNotNull(chest);count++;
            NBTTagCompound saved=chest.writeToNBT(new NBTTagCompound());
            String expected=(i<4?LibraryStructure.COMMON_LOOT:i<6?LibraryStructure.RESEARCH_LOOT:
                    i==6?LibraryStructure.CORE_LOOT:LibraryStructure.SECRET_LOOT).toString();
            assertEquals(expected,saved.getString("LootTable"));
            TileEntityChest restored=new TileEntityChest();restored.readFromNBT(saved);
            assertEquals(saved,restored.writeToNBT(new NBTTagCompound()));
            // An already opened/empty saved container stays empty when deserialized.
            saved.removeTag("LootTable");saved.removeTag("LootTableSeed");
            TileEntityChest opened=new TileEntityChest();opened.readFromNBT(saved);
            assertFalse(opened.writeToNBT(new NBTTagCompound()).hasKey("LootTable"));
            assertTrue(world.getBlockState(p.down()).isFullCube());assertFalse(world.getBlockState(p.up()).isFullCube());
        }
        assertEquals(8,count);
    }
    @Test public void intactRoofClosesEveryLibraryColumn() {
        LibraryRuinLayout.Site site=ScarletLibrary.site(12345);
        for(int x=-52;x<=52;x++)for(int z=-61;z<=66;z++) {
            int top=LibraryStructure.columnMaxY(x,z);if(top<0)continue;
            assertTrue("Roof "+x+","+z,ScarletLibrary.stateAt(site,x,top,z).isFullCube());
        }
    }
    private static final class MansionWorld extends TestWorld {
        final LibraryRuinLayout.Site site=ScarletLibrary.site(12345);
        final GensokyoGenerator generator=new GensokyoGenerator(null,12345);
        final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        MansionWorld() {provider.setDimension(Balance.gensokyoDimensionId);}
        BlockPos at(int x,int y,int z) {return new BlockPos(site.centerX()+x,site.baseY+y,site.centerZ()+z);}
        @Override public IBlockState getBlockState(BlockPos p) {
            if(p.getY()<0 || p.getY()>255)return Blocks.AIR.getDefaultState();
            int cx=p.getX()>>4,cz=p.getZ()>>4;long key=((long)cx<<32)^(cz&0xffffffffL);
            return chunks.computeIfAbsent(key,k->generator.primer(cx,cz)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);
        }
    }
}
