package dev.lostfantasy.block;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public final class RopewayCable extends Block {
    public static final PropertyBool[] CONNECTIONS={PropertyBool.create("down"),PropertyBool.create("up"),PropertyBool.create("north"),PropertyBool.create("south"),PropertyBool.create("west"),PropertyBool.create("east")};
    public RopewayCable() {super(Material.IRON);setHardness(2);setResistance(6);setLightOpacity(0);}
    @Override protected BlockStateContainer createBlockState() {return new BlockStateContainer(this,CONNECTIONS);}
    @Override public int getMetaFromState(IBlockState s) {return 0;}
    @Override public IBlockState getStateFromMeta(int meta) {return getDefaultState();}
    @Override public IBlockState getActualState(IBlockState s,IBlockAccess w,BlockPos p) {
        for(EnumFacing f:EnumFacing.values())s=s.withProperty(CONNECTIONS[f.getIndex()],w.getBlockState(p.offset(f)).getBlock()==this);
        return s;
    }
    @Override public boolean isFullCube(IBlockState s) {return false;}
    @Override public boolean isOpaqueCube(IBlockState s) {return false;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState s,IBlockAccess w,BlockPos p) {
        return FULL_BLOCK_AABB;
    }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState s,IBlockAccess w,BlockPos p) {return NULL_AABB;}
}
