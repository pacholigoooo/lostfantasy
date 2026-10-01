package dev.lostfantasy.block;

import java.util.Random;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** Small, static petal clusters follow ordinary cutout rendering and plant support rules. */
public final class FallenPetals extends BlockBush {
    public FallenPetals() {setSoundType(SoundType.PLANT);}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos) {
        return new AxisAlignedBB(0,0,0,1,.0625,1);
    }
    @Override public int quantityDropped(Random random) {return 0;}
}
