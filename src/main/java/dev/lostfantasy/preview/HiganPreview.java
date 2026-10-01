package dev.lostfantasy.preview;

import dev.lostfantasy.Balance;
import dev.lostfantasy.core.Growth;
import dev.lostfantasy.core.RiverJourney;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.entity.EntityKomachi;
import dev.lostfantasy.entity.EntityRiverFerry;
import dev.lostfantasy.world.HiganTerrain;
import dev.lostfantasy.world.HiganWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Opt-in development scene capture; excluded from the release JAR. */
public final class HiganPreview {
    private int ticks,stage,stageTicks;private boolean started;
    public static void install() {MinecraftForge.EVENT_BUS.register(new HiganPreview());}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        Minecraft mc=Minecraft.getMinecraft();ticks++;
        if(!started && ticks>40) {
            started=true;mc.gameSettings.pauseOnLostFocus=false;mc.gameSettings.renderDistanceChunks=7;mc.gameSettings.limitFramerate=40;
            mc.gameSettings.guiScale=2;mc.gameSettings.fovSetting=75;mc.gameSettings.hideGUI=false;
            mc.gameSettings.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
            mc.launchIntegratedServer("higan-preview-"+System.currentTimeMillis(),"Higan preview",new WorldSettings(734736L,GameType.CREATIVE,false,false,WorldType.FLAT).enableCommands());
        }
        if(mc.player==null || mc.getIntegratedServer()==null)return;
        stageTicks++;
        if(stage==0 && stageTicks>100) {
            stage=1;stageTicks=0;
            mc.getIntegratedServer().addScheduledTask(()->{
                EntityPlayerMP p=mc.getIntegratedServer().getPlayerList().getPlayers().get(0);
                PlayerData d=PlayerData.get(p);d.grantStage(Growth.Route.MAGIC,3);d.setAbsorbedXp(1234);d.setPower(3);d.setSpirit(1.5f);
                d.learn(dev.lostfantasy.core.Spell.EMERALD_CITY);d.research.restore(13);d.journey.separateFortune();
                p.inventory.mainInventory.set(0,new ItemStack(Items.DIAMOND));
                p.connection.setPlayerLocation(0.5,5,0.5,-90,0);
                HiganWorld.enter(p);
                HiganWorld.authorize(p,()->p.connection.setPlayerLocation(HiganTerrain.boatX(0)+4,HiganTerrain.WATER+2,HiganTerrain.boatZ(0),90,18));
                double x=p.posX,y=p.posY,z=p.posZ;
                p.connection.setPlayerLocation(x+30,y+10,z,0,0);
                if(p.posX!=x || p.posY!=y)throw new IllegalStateException("Higan /tp guard failed");
                p.setPositionAndUpdate(x+20,y,z);
                if(p.posX!=x)throw new IllegalStateException("Higan Entity teleport guard failed");
                if(p.attemptTeleport(x+4,y,z))throw new IllegalStateException("Higan chorus guard failed");
                net.minecraftforge.event.entity.living.EnderTeleportEvent pearl=new net.minecraftforge.event.entity.living.EnderTeleportEvent(p,x+4,y,z,5);
                MinecraftForge.EVENT_BUS.post(pearl);
                if(!pearl.isCanceled())throw new IllegalStateException("Higan pearl guard failed");
                p.getServer().getPlayerList().transferPlayerToDimension(p,0,new Teleporter(p.getServer().getWorld(0)));
                if(!HiganWorld.inside(p))throw new IllegalStateException("Higan dimension guard failed");
                System.out.println("HIGAN_PREVIEW_TRAVEL_GUARDS_PASSED");
            });
        }
        if(stage==1 && stageTicks>240 && HiganWorld.inside(mc.player)) {
            capture(mc,"higan-shore.png");stage=2;stageTicks=0;
            mc.getIntegratedServer().addScheduledTask(()->{
                EntityPlayerMP p=mc.getIntegratedServer().getPlayerList().getPlayers().get(0);
                if(p.capabilities.allowFlying || p.capabilities.isFlying)throw new IllegalStateException("Creative flight remained enabled");
                p.setGameType(GameType.SPECTATOR);
            });
        }
        if(stage==2 && stageTicks>30) {
            stage=3;stageTicks=0;
            mc.getIntegratedServer().addScheduledTask(()->{
                EntityPlayerMP p=mc.getIntegratedServer().getPlayerList().getPlayers().get(0);
                if(p.capabilities.allowFlying || p.capabilities.isFlying)throw new IllegalStateException("Spectator flight remained enabled");
                p.setGameType(GameType.SURVIVAL);
                for(Entity e:p.world.loadedEntityList)if(e instanceof EntityRiverFerry && ((EntityRiverFerry)e).available()) {
                    ((EntityRiverFerry)e).processInitialInteract(p,EnumHand.MAIN_HAND);break;
                }
                p.rotationYaw=-45;p.rotationPitch=5;
                System.out.println("HIGAN_PREVIEW_FLIGHT_GUARDS_PASSED");
            });
        }
        if(stage==3 && stageTicks==150)capture(mc,"higan-voyage.png");
        if(stage==3 && stageTicks==2960)capture(mc,"higan-far-shore.png");
        if(stage==3 && stageTicks>3020) {
            stage=4;stageTicks=0;
            mc.getIntegratedServer().addScheduledTask(()->{
                EntityPlayerMP p=mc.getIntegratedServer().getPlayerList().getPlayers().get(0);PlayerData d=PlayerData.get(p);
                BlockPos landing=HiganTerrain.landing(true);
                if(!HiganWorld.inside(p) || p.getDistanceSqToCenter(landing)>36
                        || d.journey.phase()!=RiverJourney.Phase.SHORE || d.journey.elapsed()!=0
                        || !d.journey.fortuneSeparated() || d.magic()!=3 || d.absorbedXp()!=1234
                        || d.power()!=3 || !d.knows(dev.lostfantasy.core.Spell.EMERALD_CITY)
                        || d.research.flags()!=13 || !p.inventory.hasItemStack(new ItemStack(Items.DIAMOND)))
                    throw new IllegalStateException("Higan crossing preservation failed");
                boolean docked=false,pilotAlive=false;
                for(Entity e:p.world.getEntitiesWithinAABB(EntityRiverFerry.class,new AxisAlignedBB(landing).grow(16))) {
                    EntityRiverFerry ferry=(EntityRiverFerry)e;
                    if(!ferry.available())continue;
                    docked=true;
                    for(Entity passenger:ferry.getPassengers())if(passenger instanceof EntityKomachi && !passenger.isDead && passenger.getRidingEntity()==ferry)pilotAlive=true;
                }
                if(!docked || !pilotAlive)throw new IllegalStateException("Docked ferry or ferryman missing");
                System.out.println("HIGAN_PREVIEW_CROSSING_PASSED");
            });
        }
        if(stage==4 && stageTicks>40) {System.out.println("HIGAN_PREVIEW_COMPLETE");mc.shutdown();}
        if(ticks>5000) {System.out.println("HIGAN_PREVIEW_TIMEOUT");mc.shutdown();}
    }
    @SubscribeEvent public void ferryPosition(TickEvent.ServerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END || stage!=2 || stageTicks<10)return;
        Minecraft mc=Minecraft.getMinecraft();if(mc.getIntegratedServer()==null)return;
        EntityPlayerMP p=mc.getIntegratedServer().getPlayerList().getPlayers().get(0);if(!HiganWorld.inside(p))return;
        HiganWorld.authorize(p,()->p.connection.setPlayerLocation(HiganTerrain.boatX(0)+4,HiganTerrain.WATER+2,HiganTerrain.boatZ(0),90,13));
        p.motionX=p.motionY=p.motionZ=0;
    }
    private static void capture(Minecraft mc,String file) {
        ScreenShotHelper.saveScreenshot(mc.gameDir,file,mc.displayWidth,mc.displayHeight,mc.getFramebuffer());
        System.out.println("HIGAN_CAPTURE "+file);
    }
}
