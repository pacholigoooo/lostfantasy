package dev.lostfantasy.block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** Low bells and broad leaves, baked into the ordinary cutout chunk mesh. */
public final class SuzuranFlower extends BlockBush {
    private static final AxisAlignedBB BOUNDS=new AxisAlignedBB(.12,0,.12,.88,.78,.88);
    public SuzuranFlower() {setHardness(0);setSoundType(SoundType.PLANT);setTickRandomly(false);}
    @Override public BlockRenderLayer getRenderLayer() {return BlockRenderLayer.CUTOUT_MIPPED;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos) {return BOUNDS;}
    @Override public EnumOffsetType getOffsetType() {return EnumOffsetType.XZ;}
}
