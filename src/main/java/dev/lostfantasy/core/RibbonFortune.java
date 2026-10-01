package dev.lostfantasy.core;

import java.util.UUID;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Cosmetic mark on the used ribbon; the player's separation state lives in RiverJourney. */
public final class RibbonFortune {
    public static final String TAG="LostFantasyRibbonLight";
    public static final int DRAW_TICKS=72;
    private RibbonFortune() {}
    public static UUID mark(ItemStack stack,UUID owner,int dimension,long started) {
        UUID id=UUID.randomUUID();NBTTagCompound tag=new NBTTagCompound();
        tag.setUniqueId("id",id);tag.setUniqueId("owner",owner);tag.setInteger("dimension",dimension);tag.setLong("started",started);
        stack.setTagInfo(TAG,tag);return id;
    }
    public static NBTTagCompound data(ItemStack stack) {
        return stack.hasTagCompound()?stack.getTagCompound().getCompoundTag(TAG):new NBTTagCompound();
    }
    public static boolean marked(ItemStack stack){return data(stack).hasUniqueId("id");}
    public static boolean matches(ItemStack stack,UUID id) {NBTTagCompound n=data(stack);return n.hasUniqueId("id") && n.getUniqueId("id").equals(id);}
    public static float glow(ItemStack stack,int dimension,double now) {
        NBTTagCompound n=data(stack);if(!n.hasUniqueId("id"))return 0;
        if(n.getInteger("dimension")!=dimension)return 1;
        double t=Math.max(0,Math.min(1,(now-n.getLong("started")-DRAW_TICKS)/10));
        return (float)(t*t*(3-2*t));
    }
    public static boolean onlyLightChanged(ItemStack before,ItemStack after) {
        if(!ItemStack.areItemsEqual(before,after) || before.getCount()!=after.getCount())return false;
        NBTTagCompound a=before.hasTagCompound()?before.getTagCompound().copy():new NBTTagCompound();
        NBTTagCompound b=after.hasTagCompound()?after.getTagCompound().copy():new NBTTagCompound();
        a.removeTag(TAG);b.removeTag(TAG);return a.equals(b);
    }
}
