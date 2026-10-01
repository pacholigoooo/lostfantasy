package dev.lostfantasy.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** Baked logs and an animated cutout flame, using ordinary block light without fire spread. */
public final class WoodlandBonfire extends Block {
    public WoodlandBonfire() {
        super(Material.ROCK);setHardness(1);setSoundType(SoundType.WOOD);setLightLevel(1);setLightOpacity(0);
    }
    @Override public boolean isOpaqueCube(IBlockState state) {return false;}
    @Override public boolean isFullCube(IBlockState state) {return false;}
    @Override public BlockRenderLayer getRenderLayer() {return BlockRenderLayer.CUTOUT;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos) {
        return new AxisAlignedBB(0,0,0,1,.75,1);
    }
}
