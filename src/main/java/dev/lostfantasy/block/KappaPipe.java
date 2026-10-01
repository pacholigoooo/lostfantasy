package dev.lostfantasy.block;

import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** An ordinary baked conduit, placed along the clicked face's axis. */
public final class KappaPipe extends BlockRotatedPillar {
    public KappaPipe() {super(Material.IRON);setHardness(2);setResistance(6);setSoundType(SoundType.METAL);setLightOpacity(0);}
    @Override public boolean isFullCube(IBlockState state) {return false;}
    @Override public boolean isOpaqueCube(IBlockState state) {return false;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos) {
        EnumFacing.Axis axis=state.getValue(AXIS);
        return axis==EnumFacing.Axis.X?new AxisAlignedBB(0,.1875,.1875,1,.8125,.8125):
                axis==EnumFacing.Axis.Z?new AxisAlignedBB(.1875,.1875,0,.8125,.8125,1):new AxisAlignedBB(.1875,0,.1875,.8125,1,.8125);
    }
}
