package dev.lostfantasy.block;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;

/** Ordinary baked stairs, sharing the blue tile's lighting and physical properties. */
public final class BlueRoofStairs extends BlockStairs {
    public BlueRoofStairs(IBlockState tile) {super(tile);}
}
