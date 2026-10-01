package dev.lostfantasy.preview;

import dev.lostfantasy.*;
import dev.lostfantasy.client.SpellVisuals;
import dev.lostfantasy.core.*;
import dev.lostfantasy.entity.EntityGapRift;
import dev.lostfantasy.network.EffectMessage;
import dev.lostfantasy.world.HiganWorld;
import java.lang.management.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Only present in the diagnostic JAR; runs in an isolated production OptiFine instance. */
@Mod(modid="lostfantasy_shader_check",version="1",clientSideOnly=true,dependencies="required-after:lostfantasy")
public final class ShaderRuntimeCheck {
    private int ticks,age,stage=-1,frames;private boolean started;private volatile boolean ready;private volatile Throwable failure;
    private String capture;private long lastFrame;private double millis;
    private final int[] dimensions={0,Balance.gensokyoDimensionId,Balance.boundaryDimensionId,Balance.dimensionId,0};
    @Mod.EventHandler public void init(FMLInitializationEvent event) {if(Boolean.getBoolean("lostfantasy.shaderCheck"))MinecraftForge.EVENT_BUS.register(this);}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        Minecraft mc=Minecraft.getMinecraft();ticks++;
        if(failure!=null){System.out.println("SHADER_CHECK_FAILED "+failure);failure.printStackTrace();mc.shutdown();return;}
        if(!started && ticks>40) {
            started=true;mc.gameSettings.pauseOnLostFocus=false;mc.gameSettings.renderDistanceChunks=Integer.getInteger("lostfantasy.renderDistance",16);
            mc.gameSettings.limitFramerate=120;mc.gameSettings.enableVsync=false;mc.gameSettings.hideGUI=false;
            mc.gameSettings.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
            mc.launchIntegratedServer("shader-check-"+System.currentTimeMillis(),"Shader check",new WorldSettings(736734L,GameType.CREATIVE,false,false,WorldType.FLAT).enableCommands());
        }
        if(mc.player==null || mc.getIntegratedServer()==null)return;
        if(stage<0){next(mc);return;}
        if(!ready || mc.player.dimension!=dimensions[stage])return;
        age++;
        if(stage==0 && (age<65 || age>100)) {
            SpellVisuals.receive(new EffectMessage(Spell.ABANDONED_TRAIN.networkId,mc.player.getEntityId(),0,mc.world.getTotalWorldTime()-32,
                    CastMotion.TRAIN_DURATION,0,new Vec3d(3,4,10),new Vec3d(0,0,-1),0));
        }
        if(stage==0 && age==65) {
            SpellVisuals.clear();mc.getIntegratedServer().addScheduledTask(()->{
                EntityPlayerMP p=mc.getIntegratedServer().getPlayerList().getPlayers().get(0);
                p.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(ModItems.MISFORTUNE_RIBBON));
            });
        }
        if(stage==0 && age==80)capture="ribbon-plain-rift.png";
        if(stage==0 && age==85)mc.getIntegratedServer().addScheduledTask(()->{
            EntityPlayerMP p=mc.getIntegratedServer().getPlayerList().getPlayers().get(0);
            ItemStack ribbon=new ItemStack(ModItems.MISFORTUNE_RIBBON);RibbonFortune.mark(ribbon,p.getUniqueID(),0,p.world.getTotalWorldTime()-120);p.setHeldItem(EnumHand.MAIN_HAND,ribbon);
        });
        if(stage==0 && age==98)capture="ribbon-gold-rift.png";
        if(age==140)capture="shader-"+stage+"-dim"+mc.player.dimension+".png";
        if(age>=160 && capture==null) {
            try {report(mc);}catch(Throwable ex){failure=ex;return;}
            if(stage==dimensions.length-1){System.out.println("SHADER_RUNTIME_PASSED");mc.shutdown();}else next(mc);
        }
    }
    private void next(Minecraft mc) {
        SpellVisuals.clear();stage++;age=0;ready=false;frames=0;millis=0;lastFrame=0;final int target=dimensions[stage];
        mc.getIntegratedServer().addScheduledTask(()->{
            try {
                EntityPlayerMP p=mc.getIntegratedServer().getPlayerList().getPlayers().get(0);
                if(target==Balance.gensokyoDimensionId)HiganWorld.enter(p);
                else if(p.dimension!=target) {
                    WorldServer destination=p.getServer().getWorld(target);
                    HiganWorld.authorize(p,()->p.getServer().getPlayerList().transferPlayerToDimension(p,target,new Teleporter(destination) {
                        @Override public void placeInPortal(Entity entity,float yaw){entity.setPosition(0,70,0);}
                        @Override public boolean makePortal(Entity entity){return false;}
                    }));
                }
                p.world.getGameRules().setOrCreateGameRule("doMobSpawning","false");
                p.world.getGameRules().setOrCreateGameRule("doDaylightCycle","false");p.world.setWorldTime(6000);
                // These void dimensions normally place a player through their own entry structures.
                // A direct shader check needs an explicit landing surface in its disposable world.
                if(target!=0 && target!=Balance.gensokyoDimensionId) {
                    for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++) {
                        net.minecraft.util.math.BlockPos at=new net.minecraft.util.math.BlockPos(x,70,z);
                        p.world.getChunk(at);p.world.setBlockState(at,net.minecraft.init.Blocks.STONEBRICK.getDefaultState(),2);
                    }
                    for(int x:new int[]{-4,4})for(int y=71;y<=73;y++)p.world.setBlockState(new net.minecraft.util.math.BlockPos(x,y,3),
                            (y==73?net.minecraft.init.Blocks.GLOWSTONE:net.minecraft.init.Blocks.PLANKS).getDefaultState(),2);
                }
                HiganWorld.authorize(p,()->{
                    if(target==0)p.connection.setPlayerLocation(-5,4,-9,-20,4);
                    else if(target==Balance.gensokyoDimensionId)p.connection.setPlayerLocation(dev.lostfantasy.world.HiganTerrain.boatX(0)+4,dev.lostfantasy.world.HiganTerrain.WATER+2,dev.lostfantasy.world.HiganTerrain.boatZ(0)-2,90,8);
                    else p.connection.setPlayerLocation(.5,71,-6,0,10);
                });
                ItemStack ribbon=new ItemStack(ModItems.MISFORTUNE_RIBBON);RibbonFortune.mark(ribbon,p.getUniqueID(),target,p.world.getTotalWorldTime()-120);p.setHeldItem(EnumHand.MAIN_HAND,ribbon);
                if(stage==0){EntityGapRift rift=new EntityGapRift(p.world);rift.setLocationAndAngles(-2,4,3,0,0);p.world.getChunk(rift.getPosition());if(!p.world.spawnEntity(rift))throw new IllegalStateException("rift spawn failed");}
                ready=true;
            }catch(Throwable ex){failure=ex;}
        });
    }
    @SubscribeEvent public void render(TickEvent.RenderTickEvent event) {
        if(event.phase!=TickEvent.Phase.END || !ready || age<60)return;
        long now=System.nanoTime();if(lastFrame!=0){millis+=(now-lastFrame)/1e6;frames++;}lastFrame=now;
        if(capture!=null) {
            Minecraft mc=Minecraft.getMinecraft();ScreenShotHelper.saveScreenshot(mc.gameDir,capture,mc.displayWidth,mc.displayHeight,mc.getFramebuffer());
            System.out.println("RIBBON_STATE glow="+RibbonFortune.glow(mc.player.getHeldItemMainhand(),mc.player.dimension,mc.world.getTotalWorldTime())+" rifts="+mc.world.loadedEntityList.stream().filter(e->e instanceof EntityGapRift).count());
            System.out.println("SHADER_CAPTURE "+capture);capture=null;
        }
    }
    private void report(Minecraft mc) throws Exception {
        if(!mc.player.isEntityAlive())throw new IllegalStateException("Invalid capture: player died in dimension "+mc.player.dimension);
        Class<?> shaders=Class.forName("net.optifine.shaders.Shaders");
        int terrain=program(shaders,"ProgramTerrain"),water=program(shaders,"ProgramWater");
        if(!Boolean.getBoolean("lostfantasy.shaderOff") && (terrain==0 || water==0))throw new IllegalStateException("Missing dimension programs: "+mc.player.dimension+" terrain="+terrain+" water="+water);
        long direct=0;for(BufferPoolMXBean pool:ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class))if(pool.getName().equals("direct"))direct=pool.getMemoryUsed();
        int error=org.lwjgl.opengl.GL11.glGetError();if(error!=0)throw new IllegalStateException("GL error "+error);
        if(mc.player.dimension==Balance.gensokyoDimensionId) {
            int bank=mc.getBlockRendererDispatcher().getModelForState(ModBlocks.SPIDER_LILY.getDefaultState()).getQuads(null,null,0).size();
            int river=mc.getBlockRendererDispatcher().getModelForState(ModBlocks.FLOATING_LILY.getDefaultState()).getQuads(null,null,0).size();
            if(bank!=56 || river!=48)throw new IllegalStateException("Field geometry budget "+bank+" / "+river);
            System.out.println("LILY_FIELD_PASSED bank_quads="+bank+" river_quads="+river);
        }
        for(java.lang.reflect.Field f:mc.renderGlobal.getClass().getDeclaredFields())if(f.getType()==net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.class) {
            f.setAccessible(true);Object dispatcher=f.get(mc.renderGlobal);java.lang.reflect.Field count;
            try{count=dispatcher.getClass().getDeclaredField("field_188249_c");}catch(NoSuchFieldException ex){count=dispatcher.getClass().getDeclaredField("countRenderBuilders");}
            count.setAccessible(true);int builders=count.getInt(dispatcher);if(builders>8)throw new IllegalStateException("Unbounded chunk builders "+builders);
            System.out.println("CHUNK_BUFFER_POOL_PASSED builders="+builders);
        }
        System.out.println("SHADER_DIMENSION_PASSED actual="+mc.player.dimension+" terrain="+terrain+" water="+water+" avg_frame_ms="+(frames==0?0:millis/frames)+" direct_bytes="+direct);
    }
    private int program(Class<?> shaders,String field) throws Exception {
        Object p=shaders.getField(field).get(null);return ((Number)p.getClass().getMethod("getId").invoke(p)).intValue();
    }
}
