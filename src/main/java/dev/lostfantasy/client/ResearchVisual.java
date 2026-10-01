package dev.lostfantasy.client;

import dev.lostfantasy.core.ResearchDemonstration;
import dev.lostfantasy.network.ResearchMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.LinkedHashMap;
import java.util.Map;

/** A slow radial demonstration above the existing desk. */
final class ResearchVisual {
    private static final Map<BlockPos,Long> demos=new LinkedHashMap<>();
    private static World world;
    private ResearchVisual() {}
    static void tick() {
        World current=Minecraft.getMinecraft().world;
        if(world!=current) { demos.clear();world=current; }
        if(world!=null) demos.values().removeIf(start->world.getTotalWorldTime()-start>=ResearchDemonstration.DURATION || world.getTotalWorldTime()-start< -40);
    }
    static void receive(ResearchMessage message) {
        tick();
        if(world==null || world.provider.getDimension()!=message.dimension) return;
        long age=world.getTotalWorldTime()-message.started;
        if(age< -40 || age>=ResearchDemonstration.DURATION)return;
        if(demos.size()>=32)demos.remove(demos.keySet().iterator().next());
        demos.put(message.pos,message.started);
    }
    static void render(float partial) {
        tick(); Minecraft mc=Minecraft.getMinecraft(); Entity view=mc.getRenderViewEntity();
        if(world==null || view==null || demos.isEmpty() || ShaderBridge.shadowPass())return;
        double vx=view.lastTickPosX+(view.posX-view.lastTickPosX)*partial;
        double vy=view.lastTickPosY+(view.posY-view.lastTickPosY)*partial;
        double vz=view.lastTickPosZ+(view.posZ-view.lastTickPosZ)*partial;
        try(RenderState state=new RenderState();RenderMatrix matrix=new RenderMatrix()) {GlStateManager.translate(-vx,-vy,-vz);
        GlStateManager.setActiveTexture(net.minecraft.client.renderer.OpenGlHelper.defaultTexUnit);
        GlStateManager.disableTexture2D();GlStateManager.disableLighting();GlStateManager.enableBlend();GlStateManager.disableCull();GlStateManager.depthMask(false);
        ShaderBridge.colored();org.lwjgl.opengl.GL11.glNormal3f(0,1,0);
            for(Map.Entry<BlockPos,Long> entry:demos.entrySet()) {
                BlockPos pos=entry.getKey();if(!world.isBlockLoaded(pos) || view.getDistanceSqToCenter(pos)>32*32)continue;
                if(world.getBlockState(pos).getBlock()!=dev.lostfantasy.ModBlocks.EMERALD_STUDY)continue;
                double age=Math.max(0,world.getTotalWorldTime()-entry.getValue()+partial);
                try(RenderMatrix local=new RenderMatrix()) {
                GlStateManager.translate(pos.getX()+.5,pos.getY()+.20,pos.getZ()+.5);
                GlStateManager.scale(.085,.085,.085);ResearchScene.draw(age);
                }
            }
        }
    }
}
