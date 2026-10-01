package dev.lostfantasy.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

/** A shared dice cup with no stakes, inventory or ticking state. */
public final class MountainGamingTable extends LibraryDecoration {
    public MountainGamingTable() {super(Kind.GAMING_TABLE);}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,
            EnumHand hand,EnumFacing side,float hitX,float hitY,float hitZ) {
        if(hand!=EnumHand.MAIN_HAND)return false;
        if(!world.isRemote)player.sendStatusMessage(new TextComponentTranslation("message.lostfantasy.dice_roll",
                1+world.rand.nextInt(6),1+world.rand.nextInt(6),1+world.rand.nextInt(6)),true);
        return true;
    }
}
