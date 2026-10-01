package dev.lostfantasy.item;

import dev.lostfantasy.LostFantasy;
import dev.lostfantasy.ModItems;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.network.Network;
import dev.lostfantasy.world.HiganWorld;
import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.EnumAction;
import net.minecraft.util.*;
import net.minecraft.world.World;

public final class HiganItem extends Item {
    private final boolean ribbon;
    public HiganItem(boolean ribbon) {this.ribbon=ribbon;setMaxStackSize(ribbon?1:16);}
    @Override public boolean shouldCauseReequipAnimation(ItemStack oldStack,ItemStack newStack,boolean slotChanged) {
        if(ribbon && !slotChanged && dev.lostfantasy.core.RibbonFortune.onlyLightChanged(oldStack,newStack))return false;
        return super.shouldCauseReequipAnimation(oldStack,newStack,slotChanged);
    }
    @Override public ActionResult<ItemStack> onItemRightClick(World world,EntityPlayer player,EnumHand hand) {
        if(ribbon) {
            if(player.dimension!=0) {if(!world.isRemote)FantasyItem.message(player,"ribbon_overworld");}
            else if(!world.isRemote && player instanceof EntityPlayerMP)separateFortune((EntityPlayerMP)player,hand);
        } else if(!HiganWorld.inside(player))player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS,player.getHeldItem(hand));
    }
    @Override public int getMaxItemUseDuration(ItemStack stack) {return ribbon?0:40;}
    @Override public EnumAction getItemUseAction(ItemStack stack) {return EnumAction.BOW;}
    @Override public ItemStack onItemUseFinish(ItemStack stack,World world,EntityLivingBase user) {
        if(!ribbon && !world.isRemote && user instanceof EntityPlayerMP)HiganWorld.enter((EntityPlayerMP)user);
        return stack;
    }
    @Override public void addInformation(ItemStack stack,World world,List<String> lines,ITooltipFlag flag) {
        if(ribbon) {lines.add("在主世界使用，抽离自身运势。");lines.add("抽离的运势汇入丝带，泛起金光。");}
        else {lines.add("长按使用，前往三途河此岸。");lines.add("可经赛之河原、中有之道步行离开。");}
    }
    public static void separateFortune(EntityPlayerMP player) {
        separateFortune(player,player.getHeldItemMainhand().getItem()==ModItems.MISFORTUNE_RIBBON?EnumHand.MAIN_HAND:EnumHand.OFF_HAND);
    }
    public static void separateFortune(EntityPlayerMP player,EnumHand hand) {
        if(player.dimension!=0 || !player.isEntityAlive() || player.isSpectator())return;
        if(player.getHeldItem(hand).getItem()!=ModItems.MISFORTUNE_RIBBON)return;
        PlayerData d=PlayerData.get(player);
        if(!d.journey.separateFortune())return;
        java.util.UUID ribbonId=dev.lostfantasy.core.RibbonFortune.mark(player.getHeldItem(hand),player.getUniqueID(),player.dimension,player.world.getTotalWorldTime());
        d.dirty=true;
        player.inventory.markDirty();player.inventoryContainer.detectAndSendChanges();
        Network.sync(player);
        Network.CHANNEL.sendToAllAround(new dev.lostfantasy.network.FortuneMessage(player,hand,ribbonId),
                new net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint(player.dimension,player.posX,player.posY,player.posZ,48));
    }
}
