package dev.lostfantasy.block;

import dev.lostfantasy.ModItems;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** A normal farmland crop: the vanilla light, hydration, growth and harvest rules apply. */
public final class CucumberCrop extends BlockCrops {
    @Override protected Item getSeed() {return ModItems.CUCUMBER_SEEDS;}
    @Override protected Item getCrop() {return ModItems.CUCUMBER;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos) {
        return new AxisAlignedBB(.0625,0,.0625,.9375,.25+.1*getAge(state),.9375);
    }
}
