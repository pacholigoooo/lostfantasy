package dev.lostfantasy.client;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.entity.EntityRiverFerry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import java.io.*;

/** Local water shading only: no full-screen pass, external shader pack or world capture. */
public final class HiganWaterSurface implements IResourceManagerReloadListener {
    public static final HiganWaterSurface INSTANCE=new HiganWaterSurface();
    private int program,timeUniform,eyeUniform,ferryUniform,lanternUniform,movingUniform;
    private boolean failed;
    private World cachedWorld;private int anchorX,anchorZ;private long rebuilt;
    private static final int RADIUS=76,FIELD_SIZE=161,MAX_TILES=153*153;
    private int[] tileX=new int[MAX_TILES],tileZ=new int[MAX_TILES],tileFlowers=new int[MAX_TILES];
    private int[] nextX=new int[MAX_TILES],nextZ=new int[MAX_TILES],nextFlowers=new int[MAX_TILES];
    private boolean rebuilding;
    private int buildX,buildZ,buildRow,nextCount;
    private long advanced=Long.MIN_VALUE;
    private final int[] flowerField=new int[FIELD_SIZE*FIELD_SIZE];
    private final BlockPos.MutableBlockPos sample=new BlockPos.MutableBlockPos();
    private int tileCount;
    private EntityRiverFerry nearbyFerry;
    private long ferryChecked=Long.MIN_VALUE;
    @Override public void onResourceManagerReload(IResourceManager manager) {
        if(program!=0)GL20.glDeleteProgram(program);program=0;failed=false;clearWorld();
    }
    private void clearWorld() {cachedWorld=null;tileCount=0;rebuilding=false;advanced=Long.MIN_VALUE;nearbyFerry=null;ferryChecked=Long.MIN_VALUE;}
    public void unload(World world) {if(cachedWorld==world)clearWorld();}
    public boolean isReady() {return program!=0 && !failed;}
    public boolean draw(double ex,double ey,double ez,float partial) {
        Minecraft mc=Minecraft.getMinecraft();
        if(HiganShaderCompatibility.externalAtmosphere() || !HiganSurfaceDetails.nearRiver(ex,ez,RADIUS)
                || ey-dev.lostfantasy.world.HiganTerrain.WATER>RADIUS)return false;
        if(ey+mc.getRenderViewEntity().getEyeHeight()<=(dev.lostfantasy.world.HiganTerrain.WATER+.90) || !OpenGlHelper.shadersSupported || failed || GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM)!=0)return false;
        if(program==0 && !load())return false;
        World world=mc.world;
        if(cachedWorld!=world) {clearWorld();cachedWorld=world;}
        int ax=(int)Math.floor(ex/8)*8,az=(int)Math.floor(ez/8)*8;
        long now=world.getTotalWorldTime();
        if(!rebuilding && (tileCount==0 || anchorX!=ax || anchorZ!=az || now-rebuilt>=15 || now<rebuilt)) {
            rebuilding=true;buildX=ax;buildZ=az;buildRow=-160;nextCount=0;
        }
        // Spread world reads over ticks; repeated render frames only consume the finished mesh.
        if(rebuilding && advanced!=now) {advanced=now;advanceRebuild(world,now);}
        if(tileCount==0)return false;
        EntityRiverFerry nearest=ferry(mc,ex,ey,ez,now);
        GL20.glUseProgram(program);
        try {
            GL20.glUniform1f(timeUniform,(float)((now%120000+partial)/20.0));
            GL20.glUniform3f(eyeUniform,(float)ex,(float)ey+mc.getRenderViewEntity().getEyeHeight(),(float)ez);
            if(nearest==null) {
                GL20.glUniform4f(ferryUniform,0,0,0,0);GL20.glUniform3f(lanternUniform,0,0,0);GL20.glUniform1f(movingUniform,0);
            } else {
                float x=(float)(nearest.lastTickPosX+(nearest.posX-nearest.lastTickPosX)*partial);
                float z=(float)(nearest.lastTickPosZ+(nearest.posZ-nearest.lastTickPosZ)*partial);
                float yaw=(float)Math.toRadians(nearest.rotationYaw),visible=1;
                float lx=x+(float)Math.cos(yaw)*.93f-(float)Math.sin(yaw)*1.48f;
                float lz=z+(float)Math.sin(yaw)*.93f+(float)Math.cos(yaw)*1.48f;
                GL20.glUniform4f(ferryUniform,x,z,yaw,visible);GL20.glUniform3f(lanternUniform,lx,lz,visible);
                double speed=Math.hypot(nearest.posX-nearest.lastTickPosX,nearest.posZ-nearest.lastTickPosZ);
                GL20.glUniform1f(movingUniform,(float)Math.min(1,speed*15));
            }
            BufferBuilder b=Tessellator.getInstance().getBuffer();b.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_COLOR);
            for(int i=0;i<tileCount;i++) {
                int divisions=(tileX[i]-ex)*(tileX[i]-ex)+(tileZ[i]-ez)*(tileZ[i]-ez)<28*28?2:1;
                double step=1.0/divisions;
                for(int z=0;z<divisions;z++)for(int x=0;x<divisions;x++) {
                    vertex(b,i,x*step,z*step);vertex(b,i,x*step,(z+1)*step);
                    vertex(b,i,(x+1)*step,(z+1)*step);vertex(b,i,(x+1)*step,z*step);
                }
            }
            Tessellator.getInstance().draw();
            return true;
        } finally {GL20.glUseProgram(0);}
    }
    private EntityRiverFerry ferry(Minecraft mc,double x,double y,double z,long now) {
        if(mc.player.getRidingEntity() instanceof EntityRiverFerry)return (EntityRiverFerry)mc.player.getRidingEntity();
        if(ferryChecked==Long.MIN_VALUE || now<ferryChecked || now-ferryChecked>=4) {
            ferryChecked=now;nearbyFerry=null;double nearestDistance=80*80;
            for(EntityRiverFerry e:mc.world.getEntitiesWithinAABB(EntityRiverFerry.class,new net.minecraft.util.math.AxisAlignedBB(x-80,y-16,z-80,x+80,y+16,z+80))) {
                double d=e.getDistanceSq(x,y,z);
                if(!e.isDead && d<nearestDistance) {nearbyFerry=e;nearestDistance=d;}
            }
        }
        return nearbyFerry!=null && !nearbyFerry.isDead && nearbyFerry.getDistanceSq(x,y,z)<80*80?nearbyFerry:null;
    }
    private void vertex(BufferBuilder b,int tile,double x,double z) {
        int f=tileFlowers[tile];
        int flowers=(int)(((f&255)*(1-z)+(f>>>8&255)*z)*(1-x)+((f>>>24&255)*(1-z)+(f>>>16&255)*z)*x);
        b.pos(tileX[tile]+x,(dev.lostfantasy.world.HiganTerrain.WATER+.889),tileZ[tile]+z).color(flowers,100,0,255).endVertex();
    }
    private void advanceRebuild(World w,long now) {
        int originX=buildX-79,originZ=buildZ-79;int[] field=flowerField;
        for(int budget=0;budget<16 && buildRow<=2*RADIUS;budget++,buildRow++) {
            if(buildRow<0) {
                int z=buildRow+160;
                // Prefix rows depend only on the previous row, including across update batches.
                for(int x=0;x<160;x++) {
                    sample.setPos(originX+x,dev.lostfantasy.world.HiganTerrain.WATER+1,originZ+z);
                    int flower=w.isBlockLoaded(sample) && w.getBlockState(sample).getBlock()==ModBlocks.FLOATING_LILY?1:0;
                    int i=(z+1)*FIELD_SIZE+x+1;
                    field[i]=flower+field[i-1]+field[i-FIELD_SIZE]-field[i-FIELD_SIZE-1];
                }
                continue;
            }
            int z=buildZ-RADIUS+buildRow;
            for(int x=buildX-RADIUS;x<=buildX+RADIUS;x++) {
                if((x-buildX)*(x-buildX)+(z-buildZ)*(z-buildZ)>RADIUS*RADIUS)continue;
                sample.setPos(x,dev.lostfantasy.world.HiganTerrain.WATER,z);if(!w.isBlockLoaded(sample))continue;
                net.minecraft.block.state.IBlockState water=w.getBlockState(sample);
                if(water.getBlock()!=ModBlocks.SANZU_WATER || water.getValue(net.minecraftforge.fluids.BlockFluidBase.LEVEL)!=0)continue;
                sample.setY(dev.lostfantasy.world.HiganTerrain.WATER+1);
                net.minecraft.block.state.IBlockState above=w.getBlockState(sample);
                if(above.getBlock()!=ModBlocks.FLOATING_LILY && !above.getBlock().isAir(above,w,sample))continue;
                int ix=x-originX,iz=z-originZ;
                nextX[nextCount]=x;nextZ[nextCount]=z;
                nextFlowers[nextCount++]=coverage(field,ix,iz) | coverage(field,ix,iz+1)<<8
                        | coverage(field,ix+1,iz+1)<<16 | coverage(field,ix+1,iz)<<24;
            }
        }
        if(buildRow<=2*RADIUS)return;
        int[] swap=tileX;tileX=nextX;nextX=swap;
        swap=tileZ;tileZ=nextZ;nextZ=swap;
        swap=tileFlowers;tileFlowers=nextFlowers;nextFlowers=swap;
        tileCount=nextCount;anchorX=buildX;anchorZ=buildZ;rebuilt=now;rebuilding=false;
    }
    private static int coverage(int[] f,int x,int z) {
        return Math.min(255,(f[(z+2)*FIELD_SIZE+x+2]-f[(z+2)*FIELD_SIZE+x-2]
                -f[(z-2)*FIELD_SIZE+x+2]+f[(z-2)*FIELD_SIZE+x-2])*16);
    }
    private boolean load() {
        int vertex=0,fragment=0;
        try {
            vertex=compile(GL20.GL_VERTEX_SHADER,"sanzu.vert");fragment=compile(GL20.GL_FRAGMENT_SHADER,"sanzu.frag");
            program=GL20.glCreateProgram();GL20.glAttachShader(program,vertex);GL20.glAttachShader(program,fragment);GL20.glLinkProgram(program);
            if(GL20.glGetProgrami(program,GL20.GL_LINK_STATUS)==0)throw new IOException(GL20.glGetProgramInfoLog(program,4096));
            timeUniform=GL20.glGetUniformLocation(program,"riverTime");eyeUniform=GL20.glGetUniformLocation(program,"riverEye");
            ferryUniform=GL20.glGetUniformLocation(program,"ferry");lanternUniform=GL20.glGetUniformLocation(program,"lantern");movingUniform=GL20.glGetUniformLocation(program,"moving");
            org.apache.logging.log4j.LogManager.getLogger("LostFantasy").info("HIGAN_WATER_SHADER_READY");return true;
        } catch(IOException | RuntimeException ex) {
            if(program!=0)GL20.glDeleteProgram(program);program=0;failed=true;
            org.apache.logging.log4j.LogManager.getLogger("LostFantasy").warn("Water shading unavailable; retaining animated river texture",ex);return false;
        } finally {if(vertex!=0)GL20.glDeleteShader(vertex);if(fragment!=0)GL20.glDeleteShader(fragment);}
    }
    private static int compile(int type,String file) throws IOException {
        StringBuilder source=new StringBuilder();char[] buffer=new char[2048];
        try(IResource resource=Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("lostfantasy:shaders/"+file));
            Reader reader=new InputStreamReader(resource.getInputStream(),java.nio.charset.StandardCharsets.UTF_8)) {
            for(int count;(count=reader.read(buffer))!=-1;)source.append(buffer,0,count);
        }
        int shader=GL20.glCreateShader(type);GL20.glShaderSource(shader,source);GL20.glCompileShader(shader);
        if(GL20.glGetShaderi(shader,GL20.GL_COMPILE_STATUS)==0) {
            String log=GL20.glGetShaderInfoLog(shader,4096);GL20.glDeleteShader(shader);throw new IOException(file+": "+log);
        }
        return shader;
    }
}
