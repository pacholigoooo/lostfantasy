package dev.lostfantasy.item;

import dev.lostfantasy.world.gensokyo.GensokyoTravel;
import java.util.List;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class HakureiCharm extends Item {
    public HakureiCharm() {setMaxStackSize(1);}
    @Override public ActionResult<ItemStack> onItemRightClick(World world,EntityPlayer player,EnumHand hand) {
        if(player.dimension!=0) {
            if(!world.isRemote)FantasyItem.message(player,"gensokyo_overworld");
            return new ActionResult<>(EnumActionResult.FAIL,player.getHeldItem(hand));
        }
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS,player.getHeldItem(hand));
    }
    @Override public int getMaxItemUseDuration(ItemStack stack) {return 40;}
    @Override public ItemStack onItemUseFinish(ItemStack stack,World world,EntityLivingBase user) {
        if(!world.isRemote && user instanceof EntityPlayerMP)GensokyoTravel.enter((EntityPlayerMP)user);
        return stack;
    }
    @Override @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack,World world,List<String> lines,ITooltipFlag flag) {
        lines.add(I18n.format("tooltip.lostfantasy.hakurei_charm.enter"));
        lines.add(I18n.format("tooltip.lostfantasy.hakurei_charm.return"));
    }
}
