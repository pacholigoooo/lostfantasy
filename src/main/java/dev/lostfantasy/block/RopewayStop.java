package dev.lostfantasy.block;

import dev.lostfantasy.world.gensokyo.RopewayRide;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class RopewayStop extends LibraryDecoration {
    public RopewayStop() {super(Kind.CATALOG);}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z) {
        if(hand!=EnumHand.MAIN_HAND)return false;
        if(!world.isRemote)RopewayRide.board(player,pos);
        return true;
    }
}
