package dev.lostfantasy.world.gensokyo;

import com.mojang.authlib.GameProfile;
import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import dev.lostfantasy.block.CeilingBed;
import dev.lostfantasy.block.CeilingChestTile;
import dev.lostfantasy.item.CeilingBedItem;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldProvider.WorldSleepResult;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class CeilingFurnitureTest {
    @BeforeClass public static void bootstrap(){GensokyoTestBlocks.register();}
    @Test public void chestOpensBelowCeilingAndKeepsItemsAcrossRegisteredTileLoading() {
        Room world=new Room();BlockPos pos=new BlockPos(0,75,0);Visitor p=new Visitor(world);p.setPosition(.5,73,.5);
        IBlockState state=ModBlocks.CEILING_CHEST.getDefaultState();world.blocks.put(pos,state);world.blocks.put(pos.up(),Blocks.PLANKS.getDefaultState());
        CeilingChestTile chest=new CeilingChestTile();chest.setWorld(world);chest.setPos(pos);world.tiles.put(pos,chest);
        chest.setInventorySlotContents(0,new ItemStack(Items.BOOK,5));activate(world,pos,p);
        assertSame(chest,p.opened);Container container=chest.createContainer(p.inventory,p);assertTrue(container.canInteractWith(p));
        NBTTagCompound saved=chest.writeToNBT(new NBTTagCompound());TileEntity loaded=TileEntity.create(world,saved);
        assertTrue(loaded instanceof CeilingChestTile);assertEquals(5,((CeilingChestTile)loaded).getStackInSlot(0).getCount());
        p.opened=null;world.blocks.put(pos.down(),Blocks.STONE.getDefaultState());activate(world,pos,p);assertNull("underside lid blocked",p.opened);
        world.blocks.remove(pos.down());p.setPosition(20,75,0);assertFalse(container.canInteractWith(p));
        assertFalse((Object)chest instanceof net.minecraft.util.ITickable);
    }
    @Test public void workbenchKeepsTheVanillaCraftingSlotsAndChecksTheActualBlock() {
        Room world=new Room();BlockPos pos=new BlockPos(0,75,0);Visitor p=new Visitor(world);p.setPosition(.5,73,.5);
        world.blocks.put(pos,ModBlocks.CEILING_WORKBENCH.getDefaultState());activate(world,pos,p);
        assertEquals("minecraft:crafting_table",p.gui.getGuiID());Container c=p.gui.createContainer(p.inventory,p);
        assertTrue(c instanceof ContainerWorkbench);assertEquals(9,((ContainerWorkbench)c).craftMatrix.getSizeInventory());assertTrue(c.canInteractWith(p));
        p.setPosition(20,75,0);assertFalse(c.canInteractWith(p));p.setPosition(.5,73,.5);
        world.blocks.put(pos,Blocks.CRAFTING_TABLE.getDefaultState());assertFalse("a replaced block closes the original container",c.canInteractWith(p));
    }
    @Test public void ceilingBedUsesRealSleepOccupancyAndWakesBelowTheCeiling() {
        Room world=new Room();BlockPos foot=new BlockPos(0,75,0),head=foot.north();putBed(world,foot,EnumFacing.NORTH);
        for(int x=-2;x<=2;x++)for(int z=-3;z<=2;z++)world.blocks.put(new BlockPos(x,65,z),Blocks.PLANKS.getDefaultState());
        Visitor p=new Visitor(world);p.setPosition(.5,74,-.5);world.playerEntities.add(p);activate(world,foot,p);
        assertTrue(p.isPlayerSleeping());assertEquals(head,p.bedLocation);assertTrue(world.getBlockState(head).getValue(BlockBed.OCCUPIED));
        assertTrue("sleeping body below mattress",p.getEntityBoundingBox().maxY<head.getY()+.375);
        Visitor other=new Visitor(world);other.setPosition(.5,74,-.5);activate(world,head,other);assertFalse(other.isPlayerSleeping());
        p.wakeUpPlayer(true,true,true);assertFalse(p.isPlayerSleeping());assertFalse(world.getBlockState(head).getValue(BlockBed.OCCUPIED));
        assertEquals(66.1,p.posY,.001);assertEquals(head,p.getBedLocation(0));
        assertEquals(new BlockPos(0,66,-1),EntityPlayer.getBedSpawnLocation(world,head,false));
        for(int x=-2;x<=2;x++)for(int z=-3;z<=2;z++)world.blocks.remove(new BlockPos(x,65,z));
        assertNull("missing floor cannot become a respawn point",EntityPlayer.getBedSpawnLocation(world,head,false));
    }
    @Test public void bedPlacementNeedsBothCeilingBlocksAndKeepsTheTwoHalvesTogether() {
        Room world=new Room();Visitor player=new Visitor(world);CeilingBedItem item=new CeilingBedItem(ModBlocks.CEILING_BED);
        BlockPos ceiling=new BlockPos(0,76,0);world.blocks.put(ceiling,Blocks.PLANKS.getDefaultState());
        ItemStack stack=new ItemStack(item,1);player.setHeldItem(EnumHand.MAIN_HAND,stack);
        assertEquals(EnumActionResult.FAIL,item.onItemUse(player,world,ceiling,EnumHand.MAIN_HAND,EnumFacing.DOWN,.5f,0,.5f));
        world.blocks.put(ceiling.offset(player.getHorizontalFacing()),Blocks.PLANKS.getDefaultState());
        assertEquals(EnumActionResult.SUCCESS,item.onItemUse(player,world,ceiling,EnumHand.MAIN_HAND,EnumFacing.DOWN,.5f,0,.5f));
        IBlockState foot=world.getBlockState(ceiling.down()),head=world.getBlockState(ceiling.down().offset(player.getHorizontalFacing()));
        assertEquals(BlockBed.EnumPartType.FOOT,foot.getValue(BlockBed.PART));assertEquals(BlockBed.EnumPartType.HEAD,head.getValue(BlockBed.PART));assertTrue(stack.isEmpty());
        world.blocks.remove(ceiling.down().offset(player.getHorizontalFacing()));
        ModBlocks.CEILING_BED.neighborChanged(foot,world,ceiling.down(),Blocks.AIR,ceiling.down().offset(player.getHorizontalFacing()));
        assertSame(Blocks.AIR,world.getBlockState(ceiling.down()).getBlock());
    }
    @Test public void allFacingStatesUseBakedModelsAndReflectedBounds() {
        Room world=new Room();
        for(Block block:new Block[]{ModBlocks.CEILING_CHEST,ModBlocks.CEILING_WORKBENCH,ModBlocks.CEILING_WRITING_DESK,ModBlocks.CEILING_LANTERN,ModBlocks.CEILING_LACQUER_BOWL,ModBlocks.CEILING_BOOKSHELF,ModBlocks.CEILING_BED})
            for(IBlockState state:block.getBlockState().getValidStates()) {
                if(block==ModBlocks.CEILING_BED && state.getValue(BlockBed.PART)==BlockBed.EnumPartType.FOOT && state.getValue(BlockBed.OCCUPIED))continue;
                assertEquals(state,block.getStateFromMeta(block.getMetaFromState(state)));
                assertEquals(EnumBlockRenderType.MODEL,block.getRenderType(state));
                assertEquals(state,block.withRotation(block.withRotation(state,Rotation.CLOCKWISE_180),Rotation.CLOCKWISE_180));
            }
        assertEquals(.5,ModBlocks.CEILING_WRITING_DESK.getDefaultState().getBoundingBox(world,BlockPos.ORIGIN).minY,0);
        assertFalse(ModBlocks.CEILING_BED.hasTileEntity(ModBlocks.CEILING_BED.getDefaultState()));
        assertFalse(ModBlocks.CEILING_BED.hasCustomBreakingProgress(ModBlocks.CEILING_BED.getDefaultState()));
    }
    private static void activate(Room world,BlockPos pos,Visitor p){IBlockState s=world.getBlockState(pos);s.getBlock().onBlockActivated(world,pos,s,p,EnumHand.MAIN_HAND,EnumFacing.DOWN,.5f,0,.5f);}
    private static void putBed(Room world,BlockPos foot,EnumFacing facing) {
        IBlockState state=ModBlocks.CEILING_BED.getDefaultState().withProperty(BlockBed.FACING,facing);
        world.blocks.put(foot,state.withProperty(BlockBed.PART,BlockBed.EnumPartType.FOOT));world.blocks.put(foot.offset(facing),state.withProperty(BlockBed.PART,BlockBed.EnumPartType.HEAD));
        world.blocks.put(foot.up(),Blocks.PLANKS.getDefaultState());world.blocks.put(foot.offset(facing).up(),Blocks.PLANKS.getDefaultState());
    }
    private static final class Visitor extends EntityPlayer {
        IInventory opened;IInteractionObject gui;
        Visitor(Room world){super(world,new GameProfile(UUID.randomUUID(),"CastleVisitor"));}
        @Override public boolean isSpectator(){return false;}
        @Override public boolean isCreative(){return false;}
        @Override public void displayGUIChest(IInventory inventory){opened=inventory;}
        @Override public void displayGui(IInteractionObject interaction){gui=interaction;}
        @Override public void sendStatusMessage(net.minecraft.util.text.ITextComponent text,boolean actionBar){}
        @Override public boolean canPlayerEdit(BlockPos pos,EnumFacing side,ItemStack stack){return true;}
    }
    private static final class Room extends TestWorld {
        final Map<BlockPos,TileEntity> tiles=new HashMap<>();
        Room(){super(new WorldProviderSurface(){@Override public WorldSleepResult canSleepAt(EntityPlayer player,BlockPos pos){return WorldSleepResult.ALLOW;}});}
        @Override public boolean setBlockState(BlockPos pos,IBlockState state,int flags){blocks.put(pos,state);return true;}
        @Override public boolean setBlockToAir(BlockPos pos){blocks.remove(pos);return true;}
        @Override public TileEntity getTileEntity(BlockPos pos){return tiles.get(pos);}
        @Override public void markChunkDirty(BlockPos pos,TileEntity tile){}
        @Override public boolean isDaytime(){return false;}
        @Override public void updateAllPlayersSleepingFlag(){}
        @Override public void notifyNeighborsOfStateChange(BlockPos pos,Block block,boolean observers){}
        @Override public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> type,AxisAlignedBB box,com.google.common.base.Predicate<? super T> predicate){return Collections.emptyList();}
    }
}
