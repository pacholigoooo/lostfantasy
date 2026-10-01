package dev.lostfantasy.preview;

import dev.lostfantasy.ModItems;
import dev.lostfantasy.core.RiverJourney;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.entity.*;
import dev.lostfantasy.world.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Opt-in new-world interaction checks and real renderer captures; excluded from releases. */
public final class KomachiPreview {
    private int ticks;private volatile int stage,age;private boolean started;private volatile Throwable failure;
    private EntityKomachi shore,pilot;private EntityRiverFerry boat;private EntityOtherPlayerMP camera;
    public static void install() {MinecraftForge.EVENT_BUS.register(new KomachiPreview());}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        Minecraft mc=Minecraft.getMinecraft();ticks++;
        if(failure!=null) {System.out.println("KOMACHI_PREVIEW_FAILED "+failure);mc.shutdown();return;}
        if(!started && ticks>40) {
            started=true;mc.gameSettings.pauseOnLostFocus=false;mc.gameSettings.renderDistanceChunks=6;mc.gameSettings.limitFramerate=50;
            mc.gameSettings.hideGUI=true;mc.gameSettings.fovSetting=70;mc.gameSettings.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
            try {org.lwjgl.opengl.Display.setDisplayMode(new org.lwjgl.opengl.DisplayMode(1440,900));mc.resize(1440,900);}catch(Exception ex){throw new RuntimeException(ex);}
            mc.launchIntegratedServer("komachi-049-"+System.currentTimeMillis(),"Komachi Alex",new WorldSettings(734736L,GameType.CREATIVE,false,false,WorldType.FLAT).enableCommands());
        }
        if(mc.player==null || mc.getIntegratedServer()==null)return;
        age++;
        if(stage==0 && age>80) {
            stage=1;age=0;server(mc,()->{
                EntityPlayerMP p=player(mc);p.getServerWorld().setWorldTime(6000);p.inventory.clear();
                p.getServerWorld().getChunk(new BlockPos(0,4,0));p.connection.setPlayerLocation(1.6,4,-2.4,21,10);
                KomachiEncounter.arrive(p.world,new BlockPos(0,4,0));KomachiEncounter.arrive(p.world,new BlockPos(8,4,0));
                java.util.List<EntityKomachi> found=p.world.getEntitiesWithinAABB(EntityKomachi.class,new net.minecraft.util.math.AxisAlignedBB(-20,0,-20,20,20,20));
                check(found.size()==1,"single riverside encounter: "+found.size());shore=found.get(0);
                KomachiEncounter restored=new KomachiEncounter();restored.readFromNBT(KomachiEncounter.get(p.world).writeToNBT(new NBTTagCompound()));
                check(restored.shore().equals(new BlockPos(0,4,0)),"encounter persistence");
                p.connection.setPlayerLocation(1.6,4,-2.4,21,10);System.out.println("KOMACHI_ENCOUNTER_PASSED");
            });
        }
        if(stage==1 && age==100) {
            capture(mc,"komachi-game-shore.png");stage=2;age=0;
        }
        if(stage==2 && age==60) {
            stage=3;age=0;server(mc,()->{
                EntityPlayerMP p=player(mc);p.inventory.clear();p.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(ModItems.HIGAN_LILY));
                ModItems.HIGAN_LILY.onItemUseFinish(p.getHeldItemMainhand(),p.world,p);check(HiganWorld.inside(p),"flower entry");
                PlayerData d=PlayerData.get(p);d.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,3);d.setAbsorbedXp(1234);
                d.research.restore(13);d.learn(dev.lostfantasy.core.Spell.EMERALD_CITY);
                p.inventory.mainInventory.set(0,new ItemStack(Items.DIAMOND));
            });
        }
        if(stage==3 && age==150) {
            if(Boolean.getBoolean("lostfantasy.komachiStateAudit"))KomachiRenderAudit.water(mc);
            check(dev.lostfantasy.client.ClientProxy.state.journey.phase()==RiverJourney.Phase.SHORE,"client shore state");
            capture(mc,"komachi-game-dock.png");stage=4;age=0;server(mc,()->{
                EntityPlayerMP p=player(mc);double x=HiganTerrain.boatX(0),z=HiganTerrain.boatZ(0);
                boat=p.world.getEntitiesWithinAABB(EntityRiverFerry.class,new net.minecraft.util.math.AxisAlignedBB(x-20,HiganTerrain.WATER-8,z-20,x+20,HiganTerrain.WATER+16,z+20)).get(0);
                check(boat.available(),"pilot does not occupy player berth");boat.ensureFerryman();boat.ensureFerryman();
                check(boat.getPassengers().size()==1,"one pilot per boat");pilot=(EntityKomachi)boat.getPassengers().get(0);
                HiganWorld.authorize(p,()->p.connection.setPlayerLocation(dev.lostfantasy.world.HiganTerrain.boatX(0)+4,dev.lostfantasy.world.HiganTerrain.WATER+1.1,dev.lostfantasy.world.HiganTerrain.boatZ(0)-1.5,90,0));
                check(pilot.processInteract(p,EnumHand.MAIN_HAND),"pilot boards player");
                check(p.getRidingEntity()==boat && PlayerData.get(p).journey.phase()==RiverJourney.Phase.SAILING,"boarding starts voyage");
                check(boat.getPassengers().size()==2,"separate pilot and player seats");
                Entity restored=EntityList.createEntityFromNBT(boat.serializeNBT(),p.world);
                check(restored instanceof EntityRiverFerry && ((EntityRiverFerry)restored).belongsTo(p),"boat ownership persistence");
                check(boat.serializeNBT().getTagList("Passengers",10).tagCount()==1,"pilot included in saved boat passengers");
                System.out.println("KOMACHI_BOARDING_PASSED");
            });
        }
        if(stage==4 && age>15 && age<=85 && mc.player.isRiding()) {
            mc.setRenderViewEntity(mc.player);
            mc.player.rotationYaw=mc.player.prevRotationYaw=mc.player.getRidingEntity().rotationYaw;
            mc.player.rotationPitch=mc.player.prevRotationPitch=8;
            if(age==70)capture(mc,"komachi-game-passenger.png");
        }
        if(stage==4 && age>85 && age<=160 && mc.player.isRiding()) {
            Entity vessel=mc.player.getRidingEntity();
            if(camera==null)camera=new EntityOtherPlayerMP(mc.world,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"KomachiCamera"));
            double angle=Math.toRadians(vessel.rotationYaw),fx=-Math.sin(angle),fz=Math.cos(angle);
            double tx=vessel.posX+EntityRiverFerry.FERRYMAN_OFFSET*fx,tz=vessel.posZ+EntityRiverFerry.FERRYMAN_OFFSET*fz;
            double cx=tx+2.8*fx+2.6*fz,cy=vessel.posY+.72,cz=tz+2.8*fz-2.6*fx;
            double dx=tx-cx,dz=tz-cz;
            camera.setLocationAndAngles(cx,cy,cz,(float)Math.toDegrees(Math.atan2(-dx,dz)),18);camera.prevPosX=cx;camera.prevPosY=cy;camera.prevPosZ=cz;
            camera.prevRotationYaw=camera.rotationYaw;camera.prevRotationPitch=camera.rotationPitch;mc.setRenderViewEntity(camera);
            if(age>=89 && age<=158 && (age-89)%3==0)capture(mc,String.format("komachi-rowing-%02d.png",(age-89)/3));
        }
        if(stage==4 && age==130) {
            check(dev.lostfantasy.client.ClientProxy.state.journey.phase()==RiverJourney.Phase.SAILING
                    && dev.lostfantasy.client.ClientProxy.state.journey.elapsed()>50,"client runtime voyage updates");
            capture(mc,"komachi-game-voyage.png");
        }
        if(stage==4 && age>160 && mc.player.isRiding()) {
            mc.setRenderViewEntity(mc.player);
            mc.gameSettings.thirdPersonView=age<=200?1:2;
            mc.player.rotationYaw=mc.player.prevRotationYaw=mc.player.getRidingEntity().rotationYaw+(age<=200?45:90);
            mc.player.rotationPitch=mc.player.prevRotationPitch=age<=200?15:0;
            if(age==195)capture(mc,"komachi-game-seat.png");
            if(age==230)capture(mc,"komachi-game-seat-side.png");
        }
        if(stage==4 && age==235) {
            stage=5;age=0;mc.gameSettings.thirdPersonView=0;mc.setRenderViewEntity(mc.player);server(mc,()->{
                EntityPlayerMP p=player(mc);check(PlayerData.get(p).journey.elapsed()>100,"automatic sailing advances");
                check(pilot.getRidingEntity()==boat && pilot.getDistanceSq(boat)<4,"pilot remains aboard");
                double angle=Math.toRadians(boat.rotationYaw);
                double ahead=(pilot.posX-p.posX)*-Math.sin(angle)+(pilot.posZ-p.posZ)*Math.cos(angle);
                check(ahead>2,"Komachi rows at the bow ahead of the passenger");
                check(Math.abs(dev.lostfantasy.core.Facing.wrap(pilot.rotationYaw-boat.rotationYaw))<1,"Komachi faces the voyage direction");
                System.out.println("KOMACHI_BOW_POSITION_PASSED");
                setJourney(PlayerData.get(p).journey,"elapsed",RiverJourney.DURATION-25);
            });
        }
        if(stage==5 && age==100) {
            stage=6;age=0;capture(mc,"komachi-game-far-shore.png");server(mc,()->{
                EntityPlayerMP p=player(mc);PlayerData d=PlayerData.get(p);BlockPos landing=HiganTerrain.landing(true);
                check(HiganWorld.inside(p) && p.getDistanceSqToCenter(landing)<=36,"arrived at far bank");
                check(d.journey.phase()==RiverJourney.Phase.SHORE && d.journey.elapsed()==0,"voyage ended at shore");
                check(d.magic()==3 && d.absorbedXp()==1234 && d.research.flags()==13 && d.knows(dev.lostfantasy.core.Spell.EMERALD_CITY),"personal progress preserved");
                check(p.inventory.hasItemStack(new ItemStack(Items.DIAMOND)),"carried inventory preserved");
                check(!boat.isDead && boat.available() && pilot.getRidingEntity()==boat && !pilot.isDead,"ferry and pilot remain available");
                check(!shore.isDead,"overworld encounter preserved");
                System.out.println("KOMACHI_VOYAGE_AND_PRESERVATION_PASSED");
            });
        }
        if(stage==6 && age>30) {System.out.println("KOMACHI_PREVIEW_COMPLETE");mc.shutdown();}
        if(ticks>3000) {System.out.println("KOMACHI_PREVIEW_TIMEOUT");mc.shutdown();}
    }
    @SubscribeEvent public void frame(TickEvent.ServerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END || stage!=3 || age<40)return;
        Minecraft mc=Minecraft.getMinecraft();if(mc.getIntegratedServer()==null)return;
        EntityPlayerMP p=player(mc);if(!HiganWorld.inside(p))return;
        HiganWorld.authorize(p,()->p.connection.setPlayerLocation(dev.lostfantasy.world.HiganTerrain.boatX(0)+4,dev.lostfantasy.world.HiganTerrain.WATER+2,dev.lostfantasy.world.HiganTerrain.boatZ(0),90,13));p.motionX=p.motionY=p.motionZ=0;
    }
    private void server(Minecraft mc,Runnable action) {mc.getIntegratedServer().addScheduledTask(()->{try {action.run();}catch(Throwable ex){failure=ex;ex.printStackTrace();}});}
    private static EntityPlayerMP player(Minecraft mc) {return mc.getIntegratedServer().getPlayerList().getPlayers().get(0);}
    private static void check(boolean value,String message) {if(!value)throw new IllegalStateException(message);}
    private static void capture(Minecraft mc,String name) {
        check(org.lwjgl.opengl.GL11.glGetError()==org.lwjgl.opengl.GL11.GL_NO_ERROR,"OpenGL state");
        check(mc.gameSettings.fovSetting==70,"unchanged field of view");
        ScreenShotHelper.saveScreenshot(mc.gameDir,name,mc.displayWidth,mc.displayHeight,mc.getFramebuffer());System.out.println("KOMACHI_CAPTURE "+name);
    }
    private static void setJourney(dev.lostfantasy.core.RiverJourney journey,String key,Object value) {
        net.minecraft.nbt.NBTTagCompound state=journey.save();
        if(value instanceof Boolean)state.setBoolean(key,(Boolean)value);
        else if(value instanceof Number)state.setInteger(key,((Number)value).intValue());
        else state.setString(key,value.toString());
        journey.restore(state);
    }
}
