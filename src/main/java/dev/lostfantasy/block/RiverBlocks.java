package dev.lostfantasy.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public final class RiverBlocks {
    private RiverBlocks() {}
    public static Block soil() { return new Block(Material.GROUND).setHardness(.6f); }
    public static final class Water extends net.minecraftforge.fluids.BlockFluidClassic {
        private static net.minecraftforge.fluids.Fluid fluid() {
            net.minecraftforge.fluids.Fluid f=new net.minecraftforge.fluids.Fluid("lostfantasy_sanzu",new net.minecraft.util.ResourceLocation("lostfantasy:blocks/sanzu_still"),new net.minecraft.util.ResourceLocation("lostfantasy:blocks/sanzu_flow"));
            f.setDensity(1000).setViscosity(1500);
            net.minecraftforge.fluids.FluidRegistry.registerFluid(f);return net.minecraftforge.fluids.FluidRegistry.getFluid("lostfantasy_sanzu");
        }
        public Water() { super(fluid(),Material.WATER);setHardness(100);setLightOpacity(2);disableStats(); }
        @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block block,BlockPos from) {}
        @Override public void updateTick(World world,BlockPos pos,IBlockState state,java.util.Random rand) {}
    }
    public static final class Lily extends BlockBush {
        private final boolean floating;
        public Lily() { this(false); }
        public Lily(boolean floating) { super(Material.PLANTS);this.floating=floating;setSoundType(SoundType.PLANT);setLightLevel(.2f); }
        @Override protected boolean canSustainBush(IBlockState state) {
            return floating?state.getBlock()==dev.lostfantasy.ModBlocks.SANZU_WATER
                    && state.getValue(net.minecraftforge.fluids.BlockFluidBase.LEVEL)==0:state.getMaterial().isSolid();
        }
        @Override public boolean canBlockStay(World world,BlockPos pos,IBlockState state) {
            return canSustainBush(world.getBlockState(pos.down()));
        }
        @Override public EnumOffsetType getOffsetType() {return EnumOffsetType.XZ;}
        @Override public AxisAlignedBB getBoundingBox(IBlockState s,IBlockAccess w,BlockPos p) { return new AxisAlignedBB(-.03,floating?-.24:0,-.01,1.15,floating?.25:.90,1.06).offset(s.getOffset(w,p)); }
        @Override public net.minecraft.item.Item getItemDropped(IBlockState s,java.util.Random r,int fortune) { return dev.lostfantasy.ModItems.HIGAN_LILY; }
    }
    public static final class Lantern extends Block {
        public Lantern() { super(Material.ROCK);setHardness(2);setLightOpacity(0);setLightLevel(1); }
        @Override public boolean isOpaqueCube(IBlockState s) { return false; }
        @Override public boolean isFullCube(IBlockState s) { return false; }
        @Override public AxisAlignedBB getBoundingBox(IBlockState s,IBlockAccess w,BlockPos p) { return new AxisAlignedBB(.2,0,.2,.8,.8,.8); }
    }
}
