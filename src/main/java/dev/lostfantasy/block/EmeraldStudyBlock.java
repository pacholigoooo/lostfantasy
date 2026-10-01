package dev.lostfantasy.block;

import dev.lostfantasy.LostFantasy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class EmeraldStudyBlock extends LibraryDecoration {
    public EmeraldStudyBlock() { super(Kind.NOTES); setBlockUnbreakable(); setResistance(6000000); }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                              EnumHand hand, EnumFacing side, float x, float y, float z) {
        if (world.isRemote && hand == EnumHand.MAIN_HAND) LostFantasy.PROXY.openEmeraldStudy(pos);
        return true;
    }
}
