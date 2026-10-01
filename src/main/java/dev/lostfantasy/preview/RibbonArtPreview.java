package dev.lostfantasy.preview;

import dev.lostfantasy.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.world.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Opt-in development capture using the actual item renderer. Excluded from release JARs. */
public final class RibbonArtPreview {
    private int ticks,elapsed;private boolean started,entered;
    public static void install(){MinecraftForge.EVENT_BUS.register(new RibbonArtPreview());}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        Minecraft mc=Minecraft.getMinecraft();ticks++;
        if(!started && ticks>40) {
            started=true;mc.gameSettings.pauseOnLostFocus=false;mc.gameSettings.renderDistanceChunks=4;
            mc.gameSettings.limitFramerate=50;mc.gameSettings.hideGUI=true;mc.gameSettings.fovSetting=70;
            mc.gameSettings.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
            try{org.lwjgl.opengl.Display.setDisplayMode(new org.lwjgl.opengl.DisplayMode(1440,900));}catch(Exception e){throw new IllegalStateException(e);}
            mc.resize(1440,900);
            mc.launchIntegratedServer("ribbon-art-"+System.currentTimeMillis(),"Ribbon 0.4.6",new WorldSettings(734736L,GameType.CREATIVE,false,false,WorldType.FLAT).enableCommands());
        }
        if(mc.player==null || mc.getIntegratedServer()==null)return;
        if(!entered) {
            entered=true;mc.getIntegratedServer().addScheduledTask(()->{
                EntityPlayerMP p=player(mc);p.world.setWorldTime(6000);p.world.getGameRules().setOrCreateGameRule("doDaylightCycle","false");
                p.world.getGameRules().setOrCreateGameRule("doMobSpawning","false");
                p.connection.setPlayerLocation(.5,4,.5,180,12);
                p.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(ModItems.MISFORTUNE_RIBBON));
            });
        }
        elapsed++;
        if(elapsed==80) {
            IBakedModel model=mc.getRenderItem().getItemModelWithOverrides(new ItemStack(ModItems.MISFORTUNE_RIBBON),mc.world,mc.player);
            int count=model.getQuads(null,null,0).size();
            if(count<3000 || !model.getParticleTexture().getIconName().equals("lostfantasy:items/misfortune_ribbon_satin"))throw new IllegalStateException("Ribbon mesh or satin atlas missing");
            System.out.println("RIBBON_ART_MESH_READY triangles="+count);mc.displayGuiScreen(new Board());
        }
        if(elapsed==115)capture(mc,"ribbon-046-inventory.png");
        if(elapsed==135){mc.displayGuiScreen(null);mc.gameSettings.hideGUI=false;}
        if(elapsed==190)capture(mc,"ribbon-046-right-hand.png");
        if(elapsed==205)mc.getIntegratedServer().addScheduledTask(()->{
            EntityPlayerMP p=player(mc);p.setHeldItem(EnumHand.MAIN_HAND,ItemStack.EMPTY);p.setHeldItem(EnumHand.OFF_HAND,new ItemStack(ModItems.MISFORTUNE_RIBBON));
        });
        if(elapsed==250)capture(mc,"ribbon-046-left-hand.png");
        if(elapsed==260) {
            mc.gameSettings.hideGUI=true;mc.gameSettings.thirdPersonView=2;
            mc.getIntegratedServer().addScheduledTask(()->{
                EntityPlayerMP p=player(mc);p.setHeldItem(EnumHand.OFF_HAND,ItemStack.EMPTY);p.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(ModItems.MISFORTUNE_RIBBON));
                p.connection.setPlayerLocation(p.posX,p.posY,p.posZ,180,0);
            });
        }
        if(elapsed==300)capture(mc,"ribbon-046-third-person.png");
        if(elapsed==320){mc.gameSettings.thirdPersonView=0;System.out.println("RIBBON_ART_COMPLETE");mc.shutdown();}
        if(ticks>1800){System.out.println("RIBBON_ART_TIMEOUT");mc.shutdown();}
    }
    private static EntityPlayerMP player(Minecraft mc){return mc.getIntegratedServer().getPlayerList().getPlayers().get(0);}
    private static void capture(Minecraft mc,String name){
        int error=org.lwjgl.opengl.GL11.glGetError();if(error!=0)throw new IllegalStateException("Ribbon GL error "+error);
        ScreenShotHelper.saveScreenshot(mc.gameDir,name,mc.displayWidth,mc.displayHeight,mc.getFramebuffer());System.out.println("RIBBON_ART_CAPTURE "+name);
    }
    private static final class Board extends GuiScreen {
        @Override public boolean doesGuiPauseGame(){return false;}
        @Override public void drawScreen(int mouseX,int mouseY,float partial){
            drawRect(0,0,width,height,0xff302d29);
            drawCenteredString(fontRenderer,"厄神的丝带",width/2,18,0xe6d8c5);
            ItemStack ribbon=new ItemStack(ModItems.MISFORTUNE_RIBBON);
            float size=Math.min(height-75,width*.60f),scale=size/16;
            GlStateManager.pushMatrix();GlStateManager.translate(width*.10f,45,0);GlStateManager.scale(scale,scale,1);
            RenderHelper.enableGUIStandardItemLighting();mc.getRenderItem().renderItemAndEffectIntoGUI(ribbon,0,0);RenderHelper.disableStandardItemLighting();GlStateManager.popMatrix();
            int x=(int)(width*.78f),y=height/2;
            drawRect(x-5,y-5,x+21,y+21,0xff59524a);
            RenderHelper.enableGUIStandardItemLighting();mc.getRenderItem().renderItemAndEffectIntoGUI(ribbon,x,y);RenderHelper.disableStandardItemLighting();
            drawCenteredString(fontRenderer,"物品栏大小",x+8,y+34,0xc1b7a8);
        }
    }
}
