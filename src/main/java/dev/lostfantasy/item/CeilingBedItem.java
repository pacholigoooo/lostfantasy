package dev.lostfantasy.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class CeilingBedItem extends ItemBlock {
    public CeilingBedItem(Block block){super(block);setMaxStackSize(1);}
    @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos pos,EnumHand hand,
                                               EnumFacing side,float x,float y,float z) {
        if(side!=EnumFacing.DOWN)return EnumActionResult.FAIL;
        BlockPos foot=pos.down();EnumFacing facing=player.getHorizontalFacing();BlockPos head=foot.offset(facing);
        ItemStack stack=player.getHeldItem(hand);
        if(!player.canPlayerEdit(foot,side,stack) || !player.canPlayerEdit(head,side,stack)
                || !world.getBlockState(foot).getBlock().isReplaceable(world,foot)
                || !world.getBlockState(head).getBlock().isReplaceable(world,head)
                || !world.getBlockState(foot.up()).isFullCube() || !world.getBlockState(head.up()).isFullCube())return EnumActionResult.FAIL;
        if(!world.isRemote) {
            IBlockState state=block.getDefaultState().withProperty(BlockBed.FACING,facing);
            world.setBlockState(foot,state.withProperty(BlockBed.PART,BlockBed.EnumPartType.FOOT),2);
            world.setBlockState(head,state.withProperty(BlockBed.PART,BlockBed.EnumPartType.HEAD),2);
            world.notifyNeighborsOfStateChange(foot,block,false);world.notifyNeighborsOfStateChange(head,block,false);
            stack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }
}
