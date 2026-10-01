package dev.lostfantasy.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.*;

/** Still regional liquid, baked by Forge into ordinary chunk geometry. */
public final class CursedBlood extends BlockFluidClassic {
    private static Fluid fluid() {
        Fluid f=new Fluid("lostfantasy_cursed_blood",new ResourceLocation("minecraft:blocks/lava_still"),new ResourceLocation("minecraft:blocks/lava_flow"));
        f.setColor(0xFFAD284B).setDensity(1400).setViscosity(6000).setLuminosity(5);
        FluidRegistry.registerFluid(f);return FluidRegistry.getFluid(f.getName());
    }
    public CursedBlood() {super(fluid(),Material.WATER);setHardness(100);setLightOpacity(3);setLightLevel(5/15f);disableStats();}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block block,BlockPos from) {}
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,java.util.Random random) {}
}
