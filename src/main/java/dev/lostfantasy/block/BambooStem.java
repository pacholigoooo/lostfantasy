package dev.lostfantasy.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** Four quarter-block offsets join a leaning culm, entirely through baked block geometry. */
public class BambooStem extends BlockHorizontal {
    public static final PropertyInteger STEP=PropertyInteger.create("step",0,3);
    public BambooStem() {
        super(Material.WOOD);setHardness(.7f);setResistance(2);setSoundType(SoundType.WOOD);setLightOpacity(0);
        setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.EAST).withProperty(STEP,0));
    }
    @Override protected BlockStateContainer createBlockState() {return new BlockStateContainer(this,FACING,STEP);}
    @Override public IBlockState getStateFromMeta(int meta) {return getDefaultState().withProperty(FACING,EnumFacing.byHorizontalIndex(meta&3)).withProperty(STEP,(meta>>2)&3);}
    @Override public int getMetaFromState(IBlockState state) {return state.getValue(FACING).getHorizontalIndex() | state.getValue(STEP)<<2;}
    @Override public int damageDropped(IBlockState state) {return 0;}
    @Override public boolean isFullCube(IBlockState state) {return false;}
    @Override public boolean isOpaqueCube(IBlockState state) {return false;}
    @Override public IBlockState withRotation(IBlockState state,Rotation r) {return state.withProperty(FACING,r.rotate(state.getValue(FACING)));}
    @Override public IBlockState withMirror(IBlockState state,Mirror m) {return withRotation(state,m.toRotation(state.getValue(FACING)));}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos) {
        double lo=(state.getValue(STEP)*4+.5)/16,hi=lo+7.0/16;
        switch(state.getValue(FACING)) {
            case WEST:return new AxisAlignedBB(1-hi,0,.40625,1-lo,1,.59375);
            case SOUTH:return new AxisAlignedBB(.40625,0,lo,.59375,1,hi);
            case NORTH:return new AxisAlignedBB(.40625,0,1-hi,.59375,1,1-lo);
            default:return new AxisAlignedBB(lo,0,.40625,hi,1,.59375);
        }
    }
}
