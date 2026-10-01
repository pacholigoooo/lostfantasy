package dev.lostfantasy.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** Persistent garden foliage with baked cutout faces and ordinary world lighting. */
public final class GardenLeaves extends Block {
    public GardenLeaves() {
        super(Material.LEAVES);setHardness(.2f);setLightOpacity(1);setSoundType(SoundType.PLANT);
    }
    @Override public boolean isOpaqueCube(IBlockState state) {return false;}
    @Override public BlockRenderLayer getRenderLayer() {return BlockRenderLayer.CUTOUT_MIPPED;}
    @Override public boolean shouldSideBeRendered(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side) {
        return world.getBlockState(pos.offset(side)).getMaterial()!=Material.LEAVES
                && super.shouldSideBeRendered(state,world,pos,side);
    }
}
