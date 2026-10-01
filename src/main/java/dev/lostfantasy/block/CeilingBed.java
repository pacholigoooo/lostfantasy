package dev.lostfantasy.block;

import dev.lostfantasy.LostFantasy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Inverted bed with vanilla sleep/occupancy rules and a safe exit on the floor below. */
@Mod.EventBusSubscriber(modid=LostFantasy.ID)
public final class CeilingBed extends BlockBed {
    public CeilingBed(){hasTileEntity=false;setHardness(.2f);setLightOpacity(0);}
    @Override public boolean hasTileEntity(IBlockState state){return false;}
    @Override public TileEntity createNewTileEntity(World world,int meta){return null;}
    @Override public EnumBlockRenderType getRenderType(IBlockState state){return EnumBlockRenderType.MODEL;}
    @Override public boolean hasCustomBreakingProgress(IBlockState state){return false;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(0,state.getValue(PART)==EnumPartType.HEAD?.375:.4375,0,1,1,1);}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block changed,BlockPos from) {
        if(!world.getBlockState(pos.up()).isFullCube())world.destroyBlock(pos,true);
        else super.neighborChanged(state,world,pos,changed,from);
    }
    @Override public boolean isBed(IBlockState state,IBlockAccess world,BlockPos pos,Entity entity){return true;}
    @Override public EnumFacing getBedDirection(IBlockState state,IBlockAccess world,BlockPos pos){return state.getValue(FACING);}
    @Override public boolean isBedFoot(IBlockAccess world,BlockPos pos){return world.getBlockState(pos).getValue(PART)==EnumPartType.FOOT;}
    @Override public void setBedOccupied(IBlockAccess access,BlockPos pos,EntityPlayer player,boolean occupied) {
        if(access instanceof World) {
            IBlockState state=access.getBlockState(pos);
            if(state.getValue(PART)==EnumPartType.FOOT)pos=pos.offset(state.getValue(FACING));
            IBlockState head=access.getBlockState(pos);
            if(head.getBlock()==this)((World)access).setBlockState(pos,head.withProperty(OCCUPIED,occupied),4);
        }
    }
    @Override public BlockPos getBedSpawnPosition(IBlockState state,IBlockAccess world,BlockPos pos,EntityPlayer player) {
        for(int down=1;down<=16;down++)for(int radius=0;radius<=2;radius++)
            for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++) {
                if(Math.max(Math.abs(dx),Math.abs(dz))!=radius)continue;
                BlockPos p=pos.add(dx,-down,dz);
                if(world.getBlockState(p.down()).isTopSolid() && clear(world,p) && clear(world,p.up()))return p;
            }
        // Respawn rejects a removed landing; waking may still leave through clear air below the mattress.
        if(player!=null && clear(world,pos.down()) && clear(world,pos.down(2)))return pos.down(2);
        return null;
    }
    private static boolean clear(IBlockAccess world,BlockPos pos) {
        IBlockState state=world.getBlockState(pos);return !state.getMaterial().blocksMovement() && !state.getMaterial().isLiquid();
    }
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,
                                             EnumHand hand,EnumFacing side,float x,float y,float z) {
        boolean result=super.onBlockActivated(world,pos,state,player,hand,side,x,y,z);alignSleeper(player);return result;
    }
    @SubscribeEvent public static void sleepingPosition(TickEvent.PlayerTickEvent event) {
        if(event.phase==TickEvent.Phase.END)alignSleeper(event.player);
    }
    private static void alignSleeper(EntityPlayer player) {
        if(!player.isPlayerSleeping() || player.bedLocation==null)return;
        IBlockState state=player.world.getBlockState(player.bedLocation);
        if(!(state.getBlock() instanceof CeilingBed))return;
        EnumFacing facing=state.getValue(FACING);BlockPos p=player.bedLocation;
        player.setPosition(p.getX()+.5+facing.getXOffset()*.4,p.getY()+.125,p.getZ()+.5+facing.getZOffset()*.4);
        player.motionX=player.motionY=player.motionZ=0;player.fallDistance=0;
    }
    @Override public ItemStack getItem(World world,BlockPos pos,IBlockState state){return new ItemStack(Item.getItemFromBlock(this));}
    @Override public void dropBlockAsItemWithChance(World world,BlockPos pos,IBlockState state,float chance,int fortune) {
        if(state.getValue(PART)==EnumPartType.HEAD)spawnAsEntity(world,pos,getItem(world,pos,state));
    }
}
