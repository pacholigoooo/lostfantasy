package dev.lostfantasy.combat;

import dev.lostfantasy.Balance;
import dev.lostfantasy.core.CastMotion;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.*;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import java.util.Set;
import java.util.function.BooleanSupplier;

final class TrainBlocks {
    private TrainBlocks() {}
    static void sweep(EntityPlayerMP owner,Vec3d origin,Vec3d direction,double back,double front,Set<Long> visited,BooleanSupplier valid) {
        if(!Balance.trainBreakBlocks || !valid.getAsBoolean() || owner.isSpectator() || !owner.isAllowEdit())return;
        WorldServer w=owner.getServerWorld();
        Vec3d a=origin.add(direction.scale(back)),b=origin.add(direction.scale(front));
        int minX=MathHelper.floor(Math.min(a.x,b.x)-CastMotion.TRAIN_HALF_WIDTH),maxX=MathHelper.floor(Math.max(a.x,b.x)+CastMotion.TRAIN_HALF_WIDTH);
        int minZ=MathHelper.floor(Math.min(a.z,b.z)-CastMotion.TRAIN_HALF_WIDTH),maxZ=MathHelper.floor(Math.max(a.z,b.z)+CastMotion.TRAIN_HALF_WIDTH);
        // Keep the ground below the wheels; do not load chunks or touch the world outside its height.
        int minY=Math.max(0,MathHelper.ceil(origin.y)),maxY=Math.min(w.getHeight()-1,MathHelper.floor(origin.y+CastMotion.TRAIN_HEIGHT));
        int attempts=0;
        for(int y=minY;y<=maxY;y++)for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++) {
            if(!valid.getAsBoolean())return;
            if(!CastMotion.trainIntersects(x+.5-origin.x,z+.5-origin.z,.5,.5,direction.x,direction.z,back,front))continue;
            BlockPos pos=new BlockPos(x,y,z);
            if(!w.isBlockLoaded(pos) || !visited.add(pos.toLong()))continue;
            IBlockState state=w.getBlockState(pos);
            if(state.getBlock().isAir(state,w,pos) || state.getMaterial().isLiquid() || state.getBlockHardness(w,pos)<0)continue;
            if(!w.isBlockModifiable(owner,pos) || !w.getWorldBorder().contains(pos))continue;
            if(++attempts>Balance.trainBreakLimit)return;
            if(ForgeHooks.onBlockBreakEvent(w,owner.interactionManager.getGameType(),owner,pos)<0)continue;
            if(!valid.getAsBoolean())return;
            // Destruction, not harvesting: no tool wear, silk touch, XP or mass item drops.
            w.destroyBlock(pos,false);
        }
    }
}
