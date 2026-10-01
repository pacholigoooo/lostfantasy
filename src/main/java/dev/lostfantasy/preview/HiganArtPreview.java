package dev.lostfantasy.preview;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.entity.EntityRiverFerry;
import dev.lostfantasy.world.HiganTerrain;
import dev.lostfantasy.world.HiganWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Development-only screenshots of the real generated field and boat. Excluded from the JAR. */
public final class HiganArtPreview {
    private int ticks,stage,elapsed;private boolean started,finished;
    public static void install() {MinecraftForge.EVENT_BUS.register(new HiganArtPreview());}
    @SubscribeEvent public void client(TickEvent.ClientTickEvent event) throws Exception {
        if(event.phase!=TickEvent.Phase.END)return;
        Minecraft mc=Minecraft.getMinecraft();ticks++;
        if(!started && ticks>40) {
            started=true;mc.gameSettings.pauseOnLostFocus=false;mc.gameSettings.renderDistanceChunks=6;mc.gameSettings.limitFramerate=50;
            mc.gameSettings.hideGUI=true;mc.gameSettings.fovSetting=70;mc.gameSettings.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
            org.lwjgl.opengl.Display.setDisplayMode(new org.lwjgl.opengl.DisplayMode(1440,900));mc.resize(1440,900);
            mc.launchIntegratedServer("higan-art-"+System.currentTimeMillis(),"Higan art 0.4.5",new WorldSettings(734736L,GameType.CREATIVE,false,false,WorldType.FLAT).enableCommands());
        }
        if(mc.player==null || mc.getIntegratedServer()==null)return;
        elapsed++;
        if(stage==0 && elapsed>80) {
            stage=1;elapsed=0;mc.getIntegratedServer().addScheduledTask(()->{
                mc.getIntegratedServer().setAllowFlight(true);
                EntityPlayerMP p=player(mc);p.connection.setPlayerLocation(.5,5,.5,0,0);HiganWorld.enter(p);
            });
        }
        if(stage==1 && elapsed==150) {
            System.out.println("HIGAN_GPU "+org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER));
            requireShader();capture(mc,"higan-045-water-before.png");
            dev.lostfantasy.client.HiganWaterSurface.INSTANCE.onResourceManagerReload(mc.getResourceManager());
            System.out.println("HIGAN_WATER_RELOAD_REQUESTED");
        }
        if(stage==1 && elapsed>200) {requireShader();capture(mc,"higan-045-boat.png");stage=2;elapsed=0;}
        if(stage==2 && elapsed==120) {requireShader();capture(mc,"sanzu-water-detail.png");}
        if(stage==2 && elapsed>200) {
            requireShader();capture(mc,"sanzu-broad-river.png");stage=3;elapsed=0;
            mc.getIntegratedServer().addScheduledTask(()->{
                EntityPlayerMP p=player(mc);
                HiganWorld.authorize(p,()->p.connection.setPlayerLocation(HiganTerrain.boatX(0)+4,HiganTerrain.WATER+2,HiganTerrain.boatZ(0),90,0));
                for(net.minecraft.entity.Entity e:p.world.loadedEntityList)if(e instanceof EntityRiverFerry && ((EntityRiverFerry)e).available()) {
                    ((EntityRiverFerry)e).processInitialInteract(p,EnumHand.MAIN_HAND);break;
                }
                if(!(p.getRidingEntity() instanceof EntityRiverFerry))throw new IllegalStateException("Could not board new ferry");
            });
        }
        if(stage==3 && elapsed==140) mc.player.rotationPitch=70;
        if(stage==3 && elapsed==165) {requireShader();capture(mc,"higan-045-ferry-floor.png");}
        if(stage==3 && elapsed==180) mc.player.rotationPitch=12;
        if(stage==3 && elapsed==240) {requireShader();capture(mc,"higan-045-voyage.png");finished=true;}
        if(finished && elapsed>260) {System.out.println("HIGAN_ART_COMPLETE");mc.shutdown();}
        if(ticks>3800) {System.out.println("HIGAN_ART_TIMEOUT");mc.shutdown();}
    }
    @SubscribeEvent public void framing(TickEvent.ServerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END || stage<1 || stage>2)return;
        Minecraft mc=Minecraft.getMinecraft();if(mc.getIntegratedServer()==null || mc.getIntegratedServer().getPlayerList().getPlayers().isEmpty())return;
        EntityPlayerMP p=player(mc);if(!HiganWorld.inside(p))return;
        // Camera framing in this development-only capture never changes production flight rules.
        HiganWorld.authorize(p,()->{
            if(stage==1)p.connection.setPlayerLocation(HiganTerrain.boatX(0)+4,HiganTerrain.WATER+4,HiganTerrain.boatZ(0)-6,90,22);
            else if(elapsed>=80 && elapsed<=130)p.connection.setPlayerLocation(HiganTerrain.boatX(0)-30,HiganTerrain.WATER+.7,HiganTerrain.boatZ(0)+24,90,20);
            else p.connection.setPlayerLocation(HiganTerrain.boatX(0)-30,HiganTerrain.WATER+8.5,HiganTerrain.boatZ(0)+24,90,25);
        });
        p.motionX=p.motionY=p.motionZ=0;p.fallDistance=0;
    }
    private static EntityPlayerMP player(Minecraft mc) {return mc.getIntegratedServer().getPlayerList().getPlayers().get(0);}
    private static void requireShader() {
        if(!dev.lostfantasy.client.HiganWaterSurface.INSTANCE.isReady())throw new IllegalStateException("River water shader did not compile and link");
        int error=org.lwjgl.opengl.GL11.glGetError();
        if(error!=org.lwjgl.opengl.GL11.GL_NO_ERROR)throw new IllegalStateException("OpenGL error during water preview: "+error);
        if(org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL20.GL_CURRENT_PROGRAM)!=0)throw new IllegalStateException("Water shader binding leaked");
        System.out.println("HIGAN_WATER_GL_PASSED");
    }
    private static void capture(Minecraft mc,String name) {
        ScreenShotHelper.saveScreenshot(mc.gameDir,name,mc.displayWidth,mc.displayHeight,mc.getFramebuffer());
        System.out.println("HIGAN_ART_CAPTURE "+name+" fps="+Minecraft.getDebugFPS());
    }
}
