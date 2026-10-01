package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.Balance;
import dev.lostfantasy.combat.SpellManager;
import dev.lostfantasy.data.PlayerData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** A walking entrance to the hermit's separate world; other travel events may still veto entry. */
public final class KasenWorld {
    public static DimensionType TYPE;
    private static final String PATH="lostfantasyHermitPath";
    private KasenWorld() {}
    static void register() {
        if(DimensionManager.isDimensionRegistered(Balance.kasenDimensionId))throw new IllegalStateException(
                "Lost Fantasy Kasen dimension ID "+Balance.kasenDimensionId+" is already used; change world.kasenDimensionId in config/lostfantasy.cfg");
        TYPE=DimensionType.register("lostfantasy_kasen","_kasen",Balance.kasenDimensionId,KasenProvider.class,false);
        DimensionManager.registerDimension(Balance.kasenDimensionId,TYPE);MinecraftForge.EVENT_BUS.register(new KasenWorld());
    }
    public static BlockPos arrival() {return local(0,1,47);}
    private static BlockPos local(int x,int y,int z) {GensokyoAtlas s=GensokyoAtlas.KASEN;return new BlockPos(s.x+x,s.y+y,s.z+z);}
    @SubscribeEvent public void tick(TickEvent.PlayerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP))return;
        EntityPlayerMP p=(EntityPlayerMP)event.player;NBTTagCompound data=p.getEntityData();
        if(p.dimension!=Balance.gensokyoDimensionId && p.dimension!=Balance.kasenDimensionId) {data.removeTag(PATH);return;}
        GensokyoAtlas site=GensokyoAtlas.KASEN;
        double x=p.posX-site.x-.5,y=p.posY-site.y,z=p.posZ-site.z-.5;
        if(Math.abs(x)>42 || z<-38 || z>70 || !p.isEntityAlive() || p.isSpectator() || p.isRiding()
                || p.timeUntilPortal>0 || PlayerData.get(p).trappedInGap || SpellManager.active(p) || SpellManager.locked(p)) {data.removeTag(PATH);return;}
        if(p.dimension==Balance.kasenDimensionId) {
            data.removeTag(PATH);
            if(p.onGround && Math.abs(x)<2.5 && y>.8 && y<2 && z>=60 && z<=62)
                RealmPassage.transfer(p,Balance.gensokyoDimensionId,local(0,1,64),0);
            return;
        }
        NBTTagCompound path=data.getCompoundTag(PATH);
        boolean ready=HermitPath.step(path,x,y,z,p.world.getTotalWorldTime(),!p.capabilities.isFlying && !p.isElytraFlying(),p.onGround);
        if(path.hasKey("next"))data.setTag(PATH,path);else data.removeTag(PATH);
        if(ready && p.world.getTotalWorldTime()>=path.getLong("retry")) {
            path.setLong("retry",p.world.getTotalWorldTime()+20);
            if(RealmPassage.transfer(p,Balance.kasenDimensionId,arrival(),180))data.removeTag(PATH);
        }
    }
}
