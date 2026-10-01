package dev.lostfantasy.preview;

import dev.lostfantasy.ModItems;
import dev.lostfantasy.client.FortuneVisual;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.item.HiganItem;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Opt-in visual capture and real server item-use exercise; excluded from release JARs. */
public final class FortunePreview {
    private int ticks,elapsed;private boolean started,entered;
    public static void install(){MinecraftForge.EVENT_BUS.register(new FortunePreview());}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        Minecraft mc=Minecraft.getMinecraft();ticks++;
        if(!started && ticks>40) {
            started=true;mc.gameSettings.pauseOnLostFocus=false;mc.gameSettings.renderDistanceChunks=4;
            mc.gameSettings.limitFramerate=50;mc.gameSettings.hideGUI=true;mc.gameSettings.fovSetting=70;
            mc.gameSettings.thirdPersonView=2;mc.gameSettings.particleSetting=2;
            mc.gameSettings.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
            try{org.lwjgl.opengl.Display.setDisplayMode(new org.lwjgl.opengl.DisplayMode(1440,900));}catch(Exception e){throw new IllegalStateException(e);}
            mc.resize(1440,900);
            mc.launchIntegratedServer("fortune-048-"+System.currentTimeMillis(),"Fortune 0.4.8",new WorldSettings(734737L,GameType.CREATIVE,false,false,WorldType.FLAT).enableCommands());
        }
        if(mc.player==null || mc.getIntegratedServer()==null)return;
        if(!entered) {
            entered=true;mc.getIntegratedServer().addScheduledTask(()->{
                EntityPlayerMP p=player(mc);p.world.setWorldTime(12500);p.world.getGameRules().setOrCreateGameRule("doDaylightCycle","false");
                p.world.getGameRules().setOrCreateGameRule("doMobSpawning","false");
                p.connection.setPlayerLocation(.5,4,.5,180,0);p.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(ModItems.MISFORTUNE_RIBBON));
            });
        }
        elapsed++;
        if(elapsed==70)use(mc,EnumHand.MAIN_HAND,false);
        if(elapsed==89)capture(mc,"fortune-048-lift.png",true);
        if(elapsed==110)capture(mc,"fortune-048-draw.png",true);
        if(elapsed==152)capture(mc,"fortune-048-third-gold.png",false);
        if(elapsed==155) {
            if(FortuneVisual.isPlaying(mc.player.getUniqueID()))throw new IllegalStateException("Expired fortune effect remains");
            mc.getIntegratedServer().addScheduledTask(()->HiganItem.separateFortune(player(mc)));
        }
        if(elapsed==169 && FortuneVisual.isPlaying(mc.player.getUniqueID()))throw new IllegalStateException("Repeated use restarted the effect");
        if(elapsed==174){mc.gameSettings.thirdPersonView=0;mc.gameSettings.hideGUI=false;}
        if(elapsed==178)capture(mc,"fortune-048-right-gold.png",false);
        if(elapsed==190)use(mc,EnumHand.OFF_HAND,true);
        if(elapsed==232)capture(mc,"fortune-048-first-person.png",true);
        if(elapsed==278)capture(mc,"fortune-048-left-gold.png",false);
        if(elapsed==286)mc.displayGuiScreen(new Comparison());
        if(elapsed==294)capture(mc,"fortune-048-inventory-gold.png",false);
        if(elapsed==305) {
            if(FortuneVisual.isPlaying(mc.player.getUniqueID()))throw new IllegalStateException("Offhand effect did not expire");
            if(mc.gameSettings.fovSetting!=70)throw new IllegalStateException("Fortune effect changed FOV");
            if(dev.lostfantasy.core.RibbonFortune.glow(mc.player.getHeldItemOffhand(),0,mc.world.getTotalWorldTime())!=1)throw new IllegalStateException("Ribbon did not retain golden light");
            System.out.println("FORTUNE_PREVIEW_COMPLETE particleSetting="+mc.gameSettings.particleSetting);mc.shutdown();
        }
        if(ticks>1800){System.out.println("FORTUNE_PREVIEW_TIMEOUT");mc.shutdown();}
    }
    private static final class Comparison extends net.minecraft.client.gui.GuiScreen {
        @Override public boolean doesGuiPauseGame(){return false;}
        @Override public void drawScreen(int x,int y,float partial) {
            drawRect(0,0,width,height,0xff302d29);
            drawCenteredString(fontRenderer,"使用前",width/3,35,0xe3d6c5);drawCenteredString(fontRenderer,"抽离后",width*2/3,35,0xe3d6c5);
            drawItem(new ItemStack(ModItems.MISFORTUNE_RIBBON),width/3-32,height/2-32);
            drawItem(mc.player.getHeldItemOffhand(),width*2/3-32,height/2-32);
        }
        private void drawItem(ItemStack stack,int x,int y) {
            net.minecraft.client.renderer.GlStateManager.pushMatrix();net.minecraft.client.renderer.GlStateManager.translate(x,y,0);net.minecraft.client.renderer.GlStateManager.scale(4,4,4);
            net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();mc.getRenderItem().renderItemAndEffectIntoGUI(stack,0,0);
            net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();net.minecraft.client.renderer.GlStateManager.popMatrix();
        }
    }
    private static EntityPlayerMP player(Minecraft mc){return mc.getIntegratedServer().getPlayerList().getPlayers().get(0);}
    private static void use(Minecraft mc,EnumHand hand,boolean reset) {
        mc.getIntegratedServer().addScheduledTask(()->{
            EntityPlayerMP p=player(mc);PlayerData data=PlayerData.get(p);
            if(reset){setJourney(data.journey,"fortuneSeparated",false);p.setHeldItem(EnumHand.MAIN_HAND,ItemStack.EMPTY);p.setHeldItem(hand,new ItemStack(ModItems.MISFORTUNE_RIBBON));}
            ModItems.MISFORTUNE_RIBBON.onItemRightClick(p.world,p,hand);
            if(!data.journey.fortuneSeparated())throw new IllegalStateException("Item use did not separate fortune");
            System.out.println("FORTUNE_SERVER_USE "+hand);
        });
    }
    private static void capture(Minecraft mc,String name,boolean playing) {
        if(playing && !FortuneVisual.isPlaying(mc.player.getUniqueID()))throw new IllegalStateException("Server effect packet not displayed");
        int error=org.lwjgl.opengl.GL11.glGetError();if(error!=0)throw new IllegalStateException("Fortune GL error "+error);
        ScreenShotHelper.saveScreenshot(mc.gameDir,name,mc.displayWidth,mc.displayHeight,mc.getFramebuffer());System.out.println("FORTUNE_CAPTURE "+name);
    }
    private static void setJourney(dev.lostfantasy.core.RiverJourney journey,String key,Object value) {
        net.minecraft.nbt.NBTTagCompound state=journey.save();
        if(value instanceof Boolean)state.setBoolean(key,(Boolean)value);
        else if(value instanceof Number)state.setInteger(key,((Number)value).intValue());
        else state.setString(key,value.toString());
        journey.restore(state);
    }
}
