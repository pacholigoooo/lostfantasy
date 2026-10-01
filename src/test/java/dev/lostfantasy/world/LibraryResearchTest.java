package dev.lostfantasy.world;

import com.mojang.authlib.GameProfile;
import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.ModItems;
import dev.lostfantasy.TestWorld;
import dev.lostfantasy.network.ResearchRequest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.UUID;
import static org.junit.Assert.*;

public class LibraryResearchTest {
    @BeforeClass public static void bootstrap() {
        Bootstrap.register();for(Item item:ModItems.ALL.values())if(!ForgeRegistries.ITEMS.containsKey(item.getRegistryName()))ForgeRegistries.ITEMS.register(item);
    }
    @Test public void allFourRotationsIdentifyOnlyTheSingleCabinetBlock() {
        for(int turn=0;turn<4;turn++) {
            PlanWorld world=new PlanWorld(turn);
            BlockPos table=world.at(-2,5,0);assertEquals(0,LibraryResearch.roleAt(world.site,table));
            assertTrue(LibraryResearch.intact(world,world.site,table,0));
            for(int x=10;x<=13;x++)for(int y=3;y<=5;y++) {
                BlockPos pos=world.at(x,y,14);assertEquals(y==4?1:-1,LibraryResearch.roleAt(world.site,pos));
                assertEquals(y==4,LibraryResearch.intact(world,world.site,pos,1));
            }
            assertEquals(-1,LibraryResearch.roleAt(world.site,world.at(12,4,13)));
            assertEquals(-1,LibraryResearch.roleAt(world.site,world.at(-11,5,-10)));
            assertEquals(-1,LibraryResearch.roleAt(world.site,world.at(12,6,14)));
        }
    }
    @Test public void tableCanBeReadFromItsExistingChairAndCatalogFromClearAisleInEveryRotation() {
        for(int turn=0;turn<4;turn++) {
            PlanWorld world=new PlanWorld(turn);
            BlockPos chair=world.at(1,3,0),table=world.at(-2,5,0);
            assertSame(Blocks.WOOL,world.getBlockState(chair).getBlock());
            assertTrue(world.isAirBlock(chair.up()));assertTrue(world.isAirBlock(chair.up(2)));
            Vec3d eye=new Vec3d(chair).add(.5,2.62,.5);
            assertEquals(table,world.rayTraceBlocks(eye,new Vec3d(table).add(.5,.15,.5),false,true,false).getBlockPos());
            BlockPos aisle=world.at(12,3,13),cabinet=world.at(12,4,14);
            assertTrue(world.isAirBlock(aisle));assertTrue(world.isAirBlock(aisle.up()));
            RayTraceResult hit=world.rayTraceBlocks(new Vec3d(aisle).add(.5,1.62,.5),new Vec3d(cabinet).add(.5,.5,.5),false,true,false);
            assertNotNull(hit);assertEquals(cabinet,hit.getBlockPos());
        }
    }
    @Test public void recordsBelongToTheirActualCabinetsInEveryRotation() {
        for(int turn=0;turn<4;turn++) {
            PlanWorld world=new PlanWorld(turn);
            int[][] locations={{10,14},{11,14},{12,14},{13,14},{-6,-5},{-6,5},{6,-5},{6,5}};
            for(int index=0;index<locations.length;index++) {
                BlockPos pos=world.at(locations[index][0],4,locations[index][1]);
                assertEquals(index,LibraryResearch.catalogAt(world.site,pos));
                assertTrue(LibraryResearch.intact(world,world.site,pos,LibraryResearch.CATALOG));
                int document=dev.lostfantasy.core.ResearchCatalog.document(world.site.structureSeed,index);
                for(int candidate=0;candidate<6;candidate++) {
                    int action=candidate<3?ResearchRequest.SAMPLE:ResearchRequest.GROWTH;
                    ResearchRequest request=new ResearchRequest(pos,action,candidate%3);
                    assertEquals(candidate==document,LibraryResearch.holds(world.site,pos,request));
                    assertFalse(LibraryResearch.holds(world.site,pos.up(),new ResearchRequest(pos.up(),action,candidate%3)));
                    assertFalse(LibraryResearch.holds(world.site,pos.down(),new ResearchRequest(pos.down(),action,candidate%3)));
                }
            }
        }
    }
    @Test public void generatedLibraryHasOnlyOnePhysicalCabinetForEachRecord() {
        for(int turn=0;turn<4;turn++) {
            PlanWorld world=new PlanWorld(turn);int cabinets=0,empty=0;int[] occurrences=new int[6];
            for(int x=-52;x<=52;x++)for(int z=-61;z<=66;z++)for(int y=3;y<=5;y++) {
                BlockPos pos=world.at(x,y,z);
                if(world.getBlockState(pos).getBlock()!=ModBlocks.ARCHIVE_CATALOG)continue;
                cabinets++;assertEquals(1,LibraryResearch.roleAt(world.site,pos));
                assertTrue(LibraryResearch.intact(world,world.site,pos,1));
                int index=LibraryResearch.catalogAt(world.site,pos);
                int document=dev.lostfantasy.core.ResearchCatalog.document(world.site.structureSeed,index);
                if(document<0)empty++;else occurrences[document]++;
            }
            assertEquals(8,cabinets);assertEquals(2,empty);assertArrayEquals(new int[]{1,1,1,1,1,1},occurrences);
        }
    }
    @Test public void currentStudyPageWorksButMissingOrReplacedFurnitureIsNeverReconstructed() {
        PlanWorld world=new PlanWorld(0);BlockPos table=world.at(-2,5,0),cabinet=world.at(12,4,14);
        world.blocks.put(table,ModBlocks.EMERALD_STUDY.getDefaultState());assertTrue(LibraryResearch.intact(world,world.site,table,0));
        world.blocks.put(table,Blocks.CHEST.getDefaultState());assertFalse(LibraryResearch.intact(world,world.site,table,0));
        world.blocks.put(cabinet,Blocks.AIR.getDefaultState());assertFalse(LibraryResearch.intact(world,world.site,cabinet,1));
        world.blocks.put(table,ModBlocks.EMERALD_STUDY.getDefaultState());world.blocks.put(table.down(),Blocks.STONE.getDefaultState());
        assertFalse(LibraryResearch.intact(world,world.site,table,0));
        assertEquals(3,world.blocks.size());assertSame(Blocks.AIR,world.blocks.get(cabinet).getBlock());
        world.loaded=false;assertFalse(LibraryResearch.intact(world,world.site,table,0));
    }
    @Test public void forgedOperationsCannotUseTheWrongFurnitureOrUnknownRole() {
        assertTrue(LibraryResearch.permits(0,ResearchRequest.DISCOVER));assertTrue(LibraryResearch.permits(0,ResearchRequest.COMPLETE));
        assertFalse(LibraryResearch.permits(1,ResearchRequest.COMPLETE));assertFalse(LibraryResearch.permits(1,ResearchRequest.COPY));
        assertFalse(LibraryResearch.permits(0,ResearchRequest.SAMPLE));assertFalse(LibraryResearch.permits(0,ResearchRequest.GROWTH));
        for(int action=0;action<=6;action++)assertFalse(LibraryResearch.permits(-1,action));
        assertFalse(LibraryResearch.permits(0,255));assertFalse(LibraryResearch.permits(1,-1));
    }
    @Test public void copyDeliveryIsDuplicateSafeAndFullInventoryCanBeRetried() {
        Player player=new Player();
        for(int slot=0;slot<36;slot++)player.inventory.setInventorySlotContents(slot,new ItemStack(Items.STICK,64));
        assertEquals(6,LibraryResearch.giveCopy(player));assertFalse(LibraryResearch.hasCopy(player));
        player.inventory.setInventorySlotContents(4,ItemStack.EMPTY);
        assertEquals(5,LibraryResearch.giveCopy(player));assertTrue(LibraryResearch.hasCopy(player));
        assertEquals(7,LibraryResearch.giveCopy(player));assertEquals(1,copies(player));
        player.inventory.setInventorySlotContents(4,ItemStack.EMPTY);
        assertEquals(5,LibraryResearch.giveCopy(player));assertEquals(1,copies(player));
    }
    @Test public void offhandAndCursorCopiesAlsoPreventDuplicateDeliveryAndOtherPlayersCanClaim() {
        Player first=new Player(),second=new Player();
        first.inventory.offHandInventory.set(0,new ItemStack(ModItems.RESEARCH_COPY));
        assertEquals(7,LibraryResearch.giveCopy(first));assertEquals(5,LibraryResearch.giveCopy(second));
        first.inventory.offHandInventory.set(0,ItemStack.EMPTY);first.inventory.setItemStack(new ItemStack(ModItems.RESEARCH_COPY));
        assertEquals(7,LibraryResearch.giveCopy(first));assertEquals(0,copies(first));assertEquals(1,copies(second));
    }
    private static int copies(EntityPlayer player) {
        int count=0;for(int slot=0;slot<player.inventory.getSizeInventory();slot++)
            if(player.inventory.getStackInSlot(slot).getItem()==ModItems.RESEARCH_COPY)count+=player.inventory.getStackInSlot(slot).getCount();
        return count;
    }
    private static class Player extends EntityPlayer {
        Player() { super(new QuietWorld(),new GameProfile(UUID.randomUUID(),"ResearchTester")); }
        @Override public boolean isSpectator() { return false; }
        @Override public boolean isCreative() { return false; }
    }
    private static class QuietWorld extends TestWorld {
        @Override public void playSound(EntityPlayer player,double x,double y,double z,SoundEvent sound,SoundCategory category,float volume,float pitch) {}
    }
    private static class PlanWorld extends TestWorld {
        final LibraryRuinLayout.Site site;
        PlanWorld(int turn) { site=new LibraryRuinLayout.Site(-3,5,20,turn,1234); }
        BlockPos at(int x,int y,int z) { int[] p=LibraryRuinLayout.toWorld(x,z,site.turns);return new BlockPos(site.centerX()+p[0],site.baseY+y,site.centerZ()+p[1]); }
        @Override public IBlockState getBlockState(BlockPos pos) {
            if(!loaded)throw new AssertionError("Unloaded chunk accessed");
            if(blocks.containsKey(pos))return blocks.get(pos);
            int[] local=LibraryRuinLayout.toLocal(pos.getX()-site.centerX(),pos.getZ()-site.centerZ(),site.turns);
            IBlockState planned=LibraryStructure.stateAt(site,LibraryStructure.columnAt(local[0],local[1]),pos.getY()-site.baseY);
            return planned==null?Blocks.AIR.getDefaultState():planned.withRotation(LibraryStructure.rotation(site.turns));
        }
    }
}
