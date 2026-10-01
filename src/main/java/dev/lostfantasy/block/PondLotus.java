package dev.lostfantasy.block;

import net.minecraft.block.BlockLilyPad;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Ordinary chunk-baked flower above still water; no renderer or dynamic lighting. */
public final class PondLotus extends BlockLilyPad {
    private static final AxisAlignedBB FLOWER=new AxisAlignedBB(.0625,0,.0625,.9375,.75,.9375);
    public PondLotus() {setHardness(0);setSoundType(SoundType.PLANT);setTickRandomly(false);}
    @Override public BlockRenderLayer getRenderLayer() {return BlockRenderLayer.CUTOUT_MIPPED;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos) {return FLOWER;}
    @Override public boolean canBlockStay(World world,BlockPos pos,IBlockState state) {
        IBlockState below=world.getBlockState(pos.down());
        return below.getMaterial()==Material.WATER && below.getBlock() instanceof BlockLiquid && below.getValue(BlockLiquid.LEVEL)==0;
    }
}
