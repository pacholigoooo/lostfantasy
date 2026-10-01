package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.Balance;
import dev.lostfantasy.combat.SpellManager;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.item.FantasyItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** The charm remembers a visitor's overworld departure; the shrine supplies the way home. */
public final class GensokyoTravel {
    private static final String KEY="lostfantasyGensokyoReturn";

    public static boolean enter(EntityPlayerMP player) {
        if(player.dimension!=0) {FantasyItem.message(player,"gensokyo_overworld");return false;}
        if(!ready(player))return false;
        NBTTagCompound departure=new NBTTagCompound();
        departure.setLong("position",player.getPosition().toLong());
        departure.setFloat("yaw",player.rotationYaw);
        if(!RealmPassage.transfer(player,Balance.gensokyoDimensionId,GensokyoWorld.arrival(),180)) {
            FantasyItem.message(player,"gensokyo_unavailable");return false;
        }
        persisted(player).setTag(KEY,departure);
        return true;
    }

    private static boolean ready(EntityPlayerMP player) {
        if(!player.isEntityAlive() || player.isSpectator() || player.timeUntilPortal>0)return false;
        if(player.isRiding() || player.isBeingRidden() || !player.onGround || player.capabilities.isFlying || player.isElytraFlying()) {
            FantasyItem.message(player,"gensokyo_stand");return false;
        }
        if(PlayerData.get(player).trappedInGap || SpellManager.active(player) || SpellManager.locked(player)) {
            FantasyItem.message(player,"gensokyo_bound");return false;
        }
        return true;
    }

    private static boolean leave(EntityPlayerMP player) {
        if(player.dimension!=Balance.gensokyoDimensionId || !ready(player))return false;
        WorldServer overworld=player.getServer().getWorld(0);
        if(overworld==null)return false;
        NBTTagCompound departure=persisted(player).getCompoundTag(KEY);
        BlockPos safe=null;
        if(departure.hasKey("position",4))safe=RealmPassage.findLanding(overworld,BlockPos.fromLong(departure.getLong("position")));
        if(safe==null) {
            BlockPos spawn=overworld.getSpawnPoint();
            if(overworld.getWorldBorder().contains(spawn)) {
                overworld.getChunk(spawn);
                safe=RealmPassage.findLanding(overworld,overworld.getTopSolidOrLiquidBlock(spawn));
            }
        }
        float yaw=departure.getFloat("yaw");if(!Float.isFinite(yaw))yaw=0;
        if(safe==null || !RealmPassage.transfer(player,overworld,safe,yaw)) {
            FantasyItem.message(player,"gensokyo_unavailable");return false;
        }
        persisted(player).removeTag(KEY);
        return true;
    }

    static NBTTagCompound persisted(EntityPlayer player) {
        NBTTagCompound root=player.getEntityData();
        if(!root.hasKey(EntityPlayer.PERSISTED_NBT_TAG,10))root.setTag(EntityPlayer.PERSISTED_NBT_TAG,new NBTTagCompound());
        return root.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
    }

    @SubscribeEvent public void clone(PlayerEvent.Clone event) {
        // Copy just this travel record; do not share mutable NBT with the old player entity.
        NBTTagCompound origin=persisted(event.getOriginal());
        if(origin.hasKey(KEY,10))persisted(event.getEntityPlayer()).setTag(KEY,origin.getCompoundTag(KEY).copy());
    }

    static boolean returnPlinth(BlockPos pos) {
        GensokyoAtlas shrine=GensokyoAtlas.HAKUREI;
        int x=pos.getX()-shrine.x,y=pos.getY()-shrine.y,z=pos.getZ()-shrine.z;
        return Math.abs(x)>=10 && Math.abs(x)<=12 && y>=1 && y<=2 && z>=70 && z<=72;
    }

    @SubscribeEvent public void interact(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player=event.getEntityPlayer();
        if(player.dimension!=Balance.gensokyoDimensionId || !returnPlinth(event.getPos())
                || !player.isSneaking() || !player.getHeldItemMainhand().isEmpty()
                || player.getDistanceSqToCenter(event.getPos())>36
                || event.getWorld().getBlockState(event.getPos()).getBlock()!=Blocks.STONEBRICK)return;
        event.setCanceled(true);event.setCancellationResult(EnumActionResult.SUCCESS);
        if(event.getHand()==EnumHand.MAIN_HAND && !event.getWorld().isRemote && player instanceof EntityPlayerMP)
            leave((EntityPlayerMP)player);
    }
}
