package dev.lostfantasy.block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public final class Clover extends BlockBush {
    public Clover() {setSoundType(SoundType.PLANT);setTickRandomly(false);}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos) {
        return new AxisAlignedBB(.0625,0,.0625,.9375,.375,.9375);
    }
}
