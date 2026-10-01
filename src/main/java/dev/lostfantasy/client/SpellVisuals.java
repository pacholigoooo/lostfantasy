package dev.lostfantasy.client;

import dev.lostfantasy.combat.SpellManager;
import dev.lostfantasy.core.CastMotion;
import dev.lostfantasy.core.Facing;
import dev.lostfantasy.core.GungnirShape;
import dev.lostfantasy.core.Spell;
import dev.lostfantasy.network.EffectMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderPlayerEvent;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

public final class SpellVisuals {
    private static final Map<Long,EffectMessage> effects=new HashMap<>();
    private static final java.lang.reflect.Field MODEL=net.minecraftforge.fml.relauncher.ReflectionHelper.findField(net.minecraft.client.renderer.entity.RenderLivingBase.class,"mainModel","field_77045_g");
    private static final Map<Object,PlayerRenderState> originals=new IdentityHashMap<>();
    private static final CastingPlayerModel REGULAR=new CastingPlayerModel(false),SLIM=new CastingPlayerModel(true);
    private static final Vec3d BEAM_REFERENCE=new Vec3d(.23,1,.17);
    private static final Random PARTICLE_RANDOM=new Random();
    private static boolean batch;
    private static final Map<Spell,java.util.function.BiConsumer<EffectMessage,Double>> DRAW;
    static {
        Map<Spell,java.util.function.BiConsumer<EffectMessage,Double>> draw=new java.util.EnumMap<>(Spell.class);
        draw.put(Spell.ROYAL_FLARE,SpellVisuals::flare);
        draw.put(Spell.ABANDONED_TRAIN,SpellVisuals::train);
        draw.put(Spell.GUNGNIR,SpellVisuals::spear);
        draw.put(Spell.FOUR_OF_A_KIND,(effect,age)->{}); // Entity renderers draw the echoes.
        draw.put(Spell.FOURFOLD_BARRIER,BarrierVisual::draw);
        draw.put(Spell.EMERALD_CITY,EmeraldVisual::draw);
        DRAW=Spell.completeRegistry(draw);
    }
    public static void validateCatalog() { Spell.completeRegistry(DRAW);CastingPlayerModel.validateCatalog(); }
    private SpellVisuals() {}
    private static long key(int caster,boolean ordinary) {return ((long)caster<<1)|(ordinary?1:0);}
    public static void clear() {
        effects.clear();restorePlayers();
    }
    public static void receive(EffectMessage m) {
        if(m.kind==EffectMessage.STOP)effects.remove(key(m.caster,false));
        else if(m.kind==EffectMessage.ORDINARY_BEAM || m.spell()!=null)
            effects.put(key(m.caster,m.kind==EffectMessage.ORDINARY_BEAM),m);
    }
    public static void tickParticles() {
        Minecraft mc=Minecraft.getMinecraft();Entity viewer=mc.getRenderViewEntity();
        if(mc.world==null) {clear();return;}
        long now=mc.world.getTotalWorldTime();int dimension=mc.world.provider.getDimension();
        effects.values().removeIf(m->!m.retainedAt(dimension,now));
        if(viewer==null || effects.isEmpty() || mc.gameSettings.particleSetting>=2)return;
        boolean reduced=mc.gameSettings.particleSetting==1;
        if(reduced && mc.world.getTotalWorldTime()%2!=0)return;
        int budget=reduced?20:64;
        for(EffectMessage m:effects.values()) {
            double age=mc.world.getTotalWorldTime()-m.started;
            if((m.spell()!=Spell.ROYAL_FLARE && m.spell()!=Spell.GUNGNIR) || m.dimension!=viewer.dimension || age<0 || age>=m.duration)continue;
            int count=Math.min(budget,reduced?4:12);
            if(m.spell()==Spell.ROYAL_FLARE) {
                Vec3d center=flareCenter(m,age);
                if(viewer.getDistanceSq(center.x,center.y,center.z)>80*80)continue;
                flareParticles(mc,m,age,center,count);
            }else {
                SpearPose pose=spearPose(m,age,1);
                if(viewer.getDistanceSq(pose.tip.x,pose.tip.y,pose.tip.z)>80*80)continue;
                spearParticles(mc,m,age,pose,count);
            }
            budget-=count;if(budget<=0)break;
        }
    }
    private static Vec3d flareCenter(EffectMessage m,double age) {
        return new Vec3d(m.x,m.y+CastMotion.flareLift(age,m.charge)+CastMotion.flareSunHeight(age,m.charge),m.z);
    }
    private static void flareParticles(Minecraft mc,EffectMessage m,double age,Vec3d center,int count) {
        double radius=CastMotion.flareRadius(age,m.charge);
        boolean charging=age<m.charge;
        Vec3d right=new Vec3d(m.dz,0,-m.dx).normalize();
        double handOffset=.3125+.625*Math.sin(CastMotion.flareSpread(age,m.charge));
        Vec3d shoulders=new Vec3d(m.x,m.y+CastMotion.flareLift(age,m.charge)+CastMotion.flareHandHeight(age,m.charge),m.z);
        for(int i=0;i<count;i++) {
            double angle=age*.3+i*Math.PI*2/count;
            if(charging && i<count/2) {
                Vec3d hand=shoulders.add(right.scale((i%2==0?-1:1)*handOffset));
                double t=PARTICLE_RANDOM.nextDouble();
                Vec3d point=hand.add(center.subtract(hand).scale(t));
                Vec3d curl=right.scale(Math.sin(angle)*.10*(1-t));
                mote(mc,point.add(curl),center.subtract(point).scale(.12),0xffda76,.6f,18);
            }else if(!charging && age-m.charge<8) {
                double ring=.8+(age-m.charge)*.65;
                Vec3d radial=new Vec3d(Math.cos(angle),.08*Math.sin(angle*3),Math.sin(angle));
                mote(mc,center.add(radial.scale(ring)),radial.scale(.23),0xffbc54,1.1f,24);
            }else {
                double height=PARTICLE_RANDOM.nextDouble()*2-1;
                double horizontal=Math.sqrt(1-height*height);
                Vec3d radial=new Vec3d(Math.cos(angle)*horizontal,height,Math.sin(angle)*horizontal);
                double distance=radius*(charging?2.4:1.3);
                Vec3d curl=new Vec3d(-radial.z,.03,radial.x).scale(.07);
                Vec3d velocity=radial.scale(charging?-.08:.09).add(curl);
                mote(mc,center.add(radial.scale(distance)),velocity,i%3==0?0xfff2bd:0xff6630,charging?.65f:.9f,24);
            }
        }
    }
    private static void spearParticles(Minecraft mc,EffectMessage m,double age,SpearPose pose,int count) {
        Vec3d dir=pose.direction;
        Vec3d right=dir.crossProduct(Math.abs(dir.y)>.95?new Vec3d(1,0,0):new Vec3d(0,1,0)).normalize();
        Vec3d up=right.crossProduct(dir).normalize();
        boolean charging=age<CastMotion.SPEAR_RELEASE;
        // Fill the entire travelled segment so the fast projectile leaves no four-block gaps.
        double travel=Math.min(m.radius,CastMotion.spearTravel(age));
        double previous=Math.min(m.radius,CastMotion.spearTravel(age-1));
        for(int i=0;i<count;i++) {
            double t=(i+PARTICLE_RANDOM.nextDouble())/count;
            double angle=age*.8+t*Math.PI*4;
            Vec3d radial=right.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
            double back=charging?t*GungnirShape.LENGTH*pose.growth:t*(travel-previous);
            double width=charging?.35*(1-CastMotion.charge(age,CastMotion.SPEAR_RELEASE))+.10:.14;
            Vec3d point=pose.tip.subtract(dir.scale(back)).add(radial.scale(width));
            Vec3d velocity=charging?radial.scale(-.035):radial.scale(.03).subtract(dir.scale(.02));
            if(!charging && age<CastMotion.SPEAR_RELEASE+3) velocity=radial.scale(.16);
            mote(mc,point,velocity,i%4==0?0xfff3f5:0xff2045,charging?.55f:.8f,charging?16:24);
        }
    }
    private static void mote(Minecraft mc,Vec3d point,Vec3d velocity,int color,float size,int life) {
        mc.effectRenderer.addEffect(new SpellParticle(mc.world,point.x,point.y,point.z,
                velocity.x,velocity.y,velocity.z,color,size,life));
    }
    public static void render(float partial) {
        if (effects.isEmpty() || ShaderBridge.shadowPass()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.getRenderViewEntity() == null) return;

        Entity view = mc.getRenderViewEntity();
        double x = view.lastTickPosX + (view.posX - view.lastTickPosX) * partial;
        double y = view.lastTickPosY + (view.posY - view.lastTickPosY) * partial;
        double z = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * partial;
        long now = mc.world.getTotalWorldTime();
        int dimension = mc.world.provider.getDimension();
        Frustum frustum=new Frustum();frustum.setPosition(x,y,z);
        try (RenderState state=new RenderState(); RenderMatrix matrix=new RenderMatrix()) {
        GlStateManager.translate(-x, -y, -z);
        GlStateManager.setActiveTexture(net.minecraft.client.renderer.OpenGlHelper.defaultTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        ShaderBridge.colored();org.lwjgl.opengl.GL11.glNormal3f(0,1,0);
            for (EffectMessage effect : effects.values()) {
                double age = now - effect.started + partial;
                if (!effect.retainedAt(dimension, now) || age >= effect.duration) continue;
                if(!visible(effect,Math.max(0,age),partial,frustum))continue;
                renderEffect(effect, Math.max(0, age));
            }
        }
    }

    /** Bounds cover the geometry, including remote ends and the full height of each column. */
    private static boolean visible(EffectMessage m,double age,float partial,Frustum frustum) {
        Vec3d origin=new Vec3d(m.x,m.y,m.z);
        AxisAlignedBB bounds;
        if(m.kind==EffectMessage.ORDINARY_BEAM)bounds=new AxisAlignedBB(origin,origin.add(m.dx,m.dy,m.dz)).grow(.15);
        else if(m.spell()==Spell.ROYAL_FLARE) {
            Vec3d center=flareCenter(m,age);double radius=age>=m.charge?m.radius+1:2;
            bounds=new AxisAlignedBB(center,center).grow(radius);
        } else if(m.spell()==Spell.ABANDONED_TRAIN) {
            Vec3d front=origin.add(m.dx*CastMotion.trainTravel(age),0,m.dz*CastMotion.trainTravel(age));
            bounds=new AxisAlignedBB(origin,front).grow(8);
        } else if(m.spell()==Spell.GUNGNIR) {
            SpearPose pose=spearPose(m,age,partial);
            bounds=new AxisAlignedBB(pose.tip,pose.tip.subtract(pose.direction.scale(GungnirShape.LENGTH+4))).grow(2);
        } else if(m.spell()==Spell.FOURFOLD_BARRIER) {
            Entity caster=Minecraft.getMinecraft().world.getEntityByID(m.caster);if(caster==null)return false;
            Vec3d at=new Vec3d(caster.lastTickPosX+(caster.posX-caster.lastTickPosX)*partial,
                    caster.lastTickPosY+(caster.posY-caster.lastTickPosY)*partial,
                    caster.lastTickPosZ+(caster.posZ-caster.lastTickPosZ)*partial);
            bounds=new AxisAlignedBB(at,at).grow(8,2,8);
        } else if(m.spell()==Spell.EMERALD_CITY) {
            if(m.columns.isEmpty())return false;
            double minX=Double.POSITIVE_INFINITY,minY=minX,minZ=minX,maxX=Double.NEGATIVE_INFINITY,maxY=maxX,maxZ=maxX;
            for(dev.lostfantasy.core.EmeraldCity.Column c:m.columns) {
                minX=Math.min(minX,c.x-1);minY=Math.min(minY,c.y-.1);minZ=Math.min(minZ,c.z-1);
                maxX=Math.max(maxX,c.x+1);maxY=Math.max(maxY,c.y+c.height+.1);maxZ=Math.max(maxZ,c.z+1);
            }
            bounds=new AxisAlignedBB(minX,minY,minZ,maxX,maxY,maxZ);
        } else return false;
        return frustum.isBoundingBoxInFrustum(bounds);
    }

    private static void renderEffect(EffectMessage effect, double age) {
        if (effect.kind == EffectMessage.ORDINARY_BEAM) {
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            Vec3d origin = new Vec3d(effect.x, effect.y, effect.z);
            beam(origin, origin.add(effect.dx, effect.dy, effect.dz), .08, 0xcc7eecff);
            return;
        }
        Spell spell = effect.spell();
        if (spell == null) return;
        DRAW.get(spell).accept(effect,age);
    }
    private static void flare(EffectMessage m,double age) {
        Vec3d center=flareCenter(m,age);
        double r=CastMotion.flareRadius(age,m.charge);
        GlStateManager.tryBlendFuncSeparate(770,1,1,0);try(RenderMatrix matrix=new RenderMatrix()) {GlStateManager.translate(center.x,center.y,center.z);
        NativeModels.sphere(r*1.55,0x25ff4629);NativeModels.sphere(r*1.20,0x66ff8a2b);NativeModels.sphere(r,0xffffeea2);NativeModels.sphere(r*.78,0xfffffff0);}
        if(age>=m.charge) {
            GlStateManager.tryBlendFuncSeparate(770,771,1,0);try(RenderMatrix matrix=new RenderMatrix()) {GlStateManager.translate(center.x,center.y,center.z);
            NativeModels.sphere(m.radius,0x12ff926d);}
            GlStateManager.tryBlendFuncSeparate(770,1,1,0);beginBeams();
            try {
                int seed=0;
                for(Vec3d direction:SpellManager.flareRays(age-m.charge))flameRay(center,direction,m.radius,age,seed++);
                for(int i=0;i<80;i++) {double a=i*Math.PI/40,b=(i+1)*Math.PI/40;beam(center.add(Math.cos(a)*m.radius,0,Math.sin(a)*m.radius),center.add(Math.cos(b)*m.radius,0,Math.sin(b)*m.radius),.055,0x70ff9d7c);}
            }finally {endBeams();}
        }
    }
    private static void flameRay(Vec3d center,Vec3d direction,double radius,double age,int seed) {
        Vec3d side=direction.crossProduct(new Vec3d(.13,1,.21)).normalize();
        Vec3d previous=center;
        for(int i=1;i<=10;i++) {
            double t=i/10.0;Vec3d next=center.add(direction.scale(radius*t)).add(side.scale(Math.sin(t*20-age*.3+seed)*.3*t));
            double width=.11+.07*Math.sin(age*.18+seed+i);
            beam(previous,next,width*3,0x25ff4420);beam(previous,next,width,0x99ff9c4a);beam(previous,next,.026,0xe6fff2b5);previous=next;
        }
    }
    private static void train(EffectMessage m,double age) {
        Vec3d direction=new Vec3d(m.dx,0,m.dz),origin=new Vec3d(m.x,m.y,m.z);
        GlStateManager.tryBlendFuncSeparate(770,771,1,0);
        TrainRiftModel.draw(origin,direction,age);
        if(age<CastMotion.TRAIN_CHARGE)return;
        double travel=CastMotion.trainTravel(age);Vec3d pos=origin.add(direction.scale(travel));
        try(RenderMatrix matrix=new RenderMatrix()) {GlStateManager.translate(pos.x,pos.y,pos.z);GlStateManager.rotate((float)Math.toDegrees(Math.atan2(direction.x,direction.z)),0,1,0);GlStateManager.depthMask(true);BlenderTrainModel.INSTANCE.draw(travel);}
        finally {GlStateManager.depthMask(false);}
    }
    private static SpearPose spearPose(EffectMessage m,double age,float partial) {
        Vec3d dir=new Vec3d(m.dx,m.dy,m.dz),start=new Vec3d(m.x,m.y,m.z);double travel=Math.min(m.radius,CastMotion.spearTravel(age));
        Vec3d end=start.add(dir.scale(travel));
        if(age<CastMotion.SPEAR_RELEASE) {
            Entity caster=Minecraft.getMinecraft().world.getEntityByID(m.caster);
            if(caster!=null) {
                Vec3d eye=new Vec3d(caster.lastTickPosX+(caster.posX-caster.lastTickPosX)*partial,caster.lastTickPosY+(caster.posY-caster.lastTickPosY)*partial+caster.getEyeHeight(),caster.lastTickPosZ+(caster.posZ-caster.lastTickPosZ)*partial);
                double wind=1-CastMotion.smooth((age-8)/4);
                double side=caster instanceof net.minecraft.entity.EntityLivingBase && ((net.minecraft.entity.EntityLivingBase)caster).getPrimaryHand()==net.minecraft.util.EnumHandSide.LEFT?-1:1;
                Vec3d lateral=new Vec3d(m.dz,0,-m.dx).normalize().scale(.38*side*wind);
                end=eye.add(lateral).add(0,.38*wind,0);
                dir=dir.add(0,.65*wind,0).normalize();
            }
        }
        return new SpearPose(end,dir,.35+.65*CastMotion.charge(age,CastMotion.SPEAR_RELEASE));
    }
    private static final class SpearPose {
        final Vec3d tip,direction;final double growth;
        SpearPose(Vec3d tip,Vec3d direction,double growth) {this.tip=tip;this.direction=direction;this.growth=growth;}
    }
    private static void spear(EffectMessage m,double age) {
        SpearPose pose=spearPose(m,age,Minecraft.getMinecraft().getRenderPartialTicks());
        Vec3d end=pose.tip,dir=pose.direction;
        GlStateManager.tryBlendFuncSeparate(770,1,1,0);
        Vec3d right=dir.crossProduct(Math.abs(dir.y)>.95?new Vec3d(1,0,0):new Vec3d(0,1,0)).normalize();
        Vec3d up=right.crossProduct(dir).normalize();
        beginBeams();
        try {
            double growth=pose.growth;
            lanceEdge(end.subtract(dir.scale(GungnirShape.LENGTH*growth)),end);
            for(int plane=0;plane<2;plane++)for(int sign=-1;sign<=1;sign+=2) {
                Vec3d lateral=plane==0?right:up.scale(.42);
                Vec3d[] points=new Vec3d[GungnirShape.points()];
                for(int i=0;i<points.length;i++)points[i]=end.subtract(dir.scale(GungnirShape.back(i)*growth)).add(lateral.scale(GungnirShape.width(i)*sign*growth));
                for(int i=0;i<points.length-1;i++)lanceEdge(points[i],points[i+1]);
                for(int i=0;i<points.length-3;i++)lanceEdge(points[i],points[i+3]);
            }
        }finally {endBeams();}
    }
    private static void lanceEdge(Vec3d a,Vec3d b) {beam(a,b,.19,0x28ff102f);beam(a,b,.065,0xcaff294f);beam(a,b,.018,0xfffff4f2);}
    public static void drawGapRift(Vec3d at,Vec3d normal,double progress) {
        Vec3d side=new Vec3d(normal.z,0,-normal.x);BufferBuilder bb=Tessellator.getInstance().getBuffer();
        bb.begin(GL11.GL_TRIANGLES,DefaultVertexFormats.POSITION_COLOR);
        Vec3d center=at.add(0,1.7,0);
        for(int i=0;i<48;i++) {
            double a=i*Math.PI/24,b=(i+1)*Math.PI/24;
            vertex(bb,center,0xf0080210);vertex(bb,center.add(side.scale(Math.cos(a)*1.05*progress)).add(0,Math.sin(a)*1.7,0),0xf010021c);vertex(bb,center.add(side.scale(Math.cos(b)*1.05*progress)).add(0,Math.sin(b)*1.7,0),0xf010021c);
        }Tessellator.getInstance().draw();beginBeams();
        try {
            for(int i=0;i<48;i++) {double a=i*Math.PI/24,b=(i+1)*Math.PI/24;beam(center.add(side.scale(Math.cos(a)*1.05*progress)).add(0,Math.sin(a)*1.7,0),center.add(side.scale(Math.cos(b)*1.05*progress)).add(0,Math.sin(b)*1.7,0),.065,0xd0b955e9);}
            for(int i=0;i<5;i++) {
                Vec3d eye=center.add(side.scale((i%2==0?-.3:.3)*progress)).add(0,(i-2)*.48,0);
                for(int face:new int[]{-1,1}) {Vec3d e=eye.add(normal.scale(.025*face));beam(e.subtract(side.scale(.22)),e.add(0,.10,0),.018,0xffe3b4ed);beam(e.add(0,.10,0),e.add(side.scale(.22)),.018,0xffe3b4ed);beam(e.subtract(side.scale(.22)),e.add(0,-.10,0),.018,0xffe3b4ed);beam(e.add(0,-.10,0),e.add(side.scale(.22)),.018,0xffe3b4ed);beam(e.add(0,-.075,0),e.add(0,.075,0),.035,0xffff4063);}
            }
        }finally {endBeams();}
    }
    private static void beginBeams() {Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_COLOR);batch=true;}
    private static void endBeams() {batch=false;Tessellator.getInstance().draw();}
    private static void beam(Vec3d a,Vec3d b,double width,int color) {
        Vec3d dir=b.subtract(a).normalize(),side=dir.crossProduct(BEAM_REFERENCE).normalize().scale(width);
        Vec3d other=dir.crossProduct(side).normalize().scale(width);
        BufferBuilder bb=Tessellator.getInstance().getBuffer();if(!batch)bb.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_COLOR);
        beamQuad(bb,a,b,side,color);beamQuad(bb,a,b,other,color);
        if(!batch)Tessellator.getInstance().draw();
    }
    private static void beamQuad(BufferBuilder b,Vec3d from,Vec3d to,Vec3d offset,int color) {
        // Emit positions directly instead of allocating four temporary Vec3d per quad.
        int r=color>>16&255,g=color>>8&255,blue=color&255,a=color>>>24&255;
        b.pos(from.x+offset.x,from.y+offset.y,from.z+offset.z).color(r,g,blue,a).endVertex();
        b.pos(to.x+offset.x,to.y+offset.y,to.z+offset.z).color(r,g,blue,a).endVertex();
        b.pos(to.x-offset.x,to.y-offset.y,to.z-offset.z).color(r,g,blue,a).endVertex();
        b.pos(from.x-offset.x,from.y-offset.y,from.z-offset.z).color(r,g,blue,a).endVertex();
    }
    private static void vertex(BufferBuilder b,Vec3d p,int c) {b.pos(p.x,p.y,p.z).color(c>>16&255,c>>8&255,c&255,c>>>24&255).endVertex();}
    public static void beforePlayer(RenderPlayerEvent.Pre e) {
        // A later mod may cancel Pre, in which case Forge never emits Post.
        restoreModel(e.getRenderer());
        EffectMessage effect=effects.get(key(e.getEntityPlayer().getEntityId(),false));if(effect==null||!effect.hasCastingPose()||effect.dimension!=e.getEntityPlayer().dimension)return;
        double age=e.getEntityPlayer().world.getTotalWorldTime()-effect.started+e.getPartialRenderTick();
        if(!effect.poseActive(age))return;
        try {
            net.minecraft.client.model.ModelBase original=e.getRenderer().getMainModel();originals.put(e.getRenderer(),new PlayerRenderState(original,e.getEntityPlayer()));
            CastingPlayerModel model=e.getEntityPlayer() instanceof AbstractClientPlayer && "slim".equals(((AbstractClientPlayer)e.getEntityPlayer()).getSkinType())?SLIM:REGULAR;
            model.setModelAttributes(original);model.effect=effect;model.age=age;MODEL.set(e.getRenderer(),model);
            float yaw=(float)Math.toDegrees(Math.atan2(-effect.dx,effect.dz));
            float body=yaw;
            if(effect.spell()==Spell.ABANDONED_TRAIN) {
                net.minecraft.entity.player.EntityPlayer player=e.getEntityPlayer();
                float partial=e.getPartialRenderTick();
                float restingBody=Facing.interpolate(player.prevRenderYawOffset,player.renderYawOffset,partial);
                float restingHead=Facing.interpolate(player.prevRotationYawHead,player.rotationYawHead,partial);
                double recovery=CastMotion.trainRecovery(age);
                body=Facing.interpolate(restingBody,CastMotion.trainBodyYaw(effect.castYaw,yaw,age),recovery);
                yaw=CastMotion.trainHeadYaw(restingHead,yaw,age);
            }
            e.getEntityPlayer().prevRenderYawOffset=e.getEntityPlayer().renderYawOffset=body;
            if (effect.spell()==Spell.FOURFOLD_BARRIER || effect.spell()==Spell.EMERALD_CITY) yaw=e.getEntityPlayer().rotationYawHead;
            e.getEntityPlayer().prevRotationYawHead=e.getEntityPlayer().rotationYawHead=yaw;
        }catch(IllegalAccessException ex) {restoreModel(e.getRenderer());throw new IllegalStateException("Cannot apply spell pose",ex);}
    }
    public static void afterPlayer(RenderPlayerEvent.Post e) {
        restoreModel(e.getRenderer());
    }
    private static void restoreModel(Object renderer) {
        PlayerRenderState original=originals.remove(renderer);
        if(original!=null) {
            original.restore();
            try {MODEL.set(renderer,original.model);}catch(IllegalAccessException ex){throw new IllegalStateException(ex);}
        }
    }
    public static void restorePlayers() {if(!originals.isEmpty())for(Object renderer:new ArrayList<>(originals.keySet()))restoreModel(renderer);}
    static double poseAge(Entity entity,EffectMessage effect) {return entity.world.getTotalWorldTime()-effect.started+Minecraft.getMinecraft().getRenderPartialTicks();}
    static EffectMessage poseEffect(Entity entity) {
        EffectMessage effect=effects.get(key(entity.getEntityId(),false));
        if(effect==null || !effect.hasCastingPose() || effect.dimension!=entity.dimension)return null;
        double age=poseAge(entity,effect);return effect.poseActive(age)?effect:null;
    }
    public static void installArmorPoses() {
        for(net.minecraft.client.renderer.entity.RenderPlayer renderer:Minecraft.getMinecraft().getRenderManager().getSkinMap().values()) {
            java.util.List<net.minecraft.client.renderer.entity.layers.LayerRenderer<?>> layers=net.minecraftforge.fml.relauncher.ReflectionHelper.getPrivateValue(net.minecraft.client.renderer.entity.RenderLivingBase.class,renderer,"layerRenderers","field_177097_h");
            ListIterator<net.minecraft.client.renderer.entity.layers.LayerRenderer<?>> it=layers.listIterator();
            while(it.hasNext())if(it.next().getClass()==net.minecraft.client.renderer.entity.layers.LayerBipedArmor.class)it.set(new CastingArmorLayer(renderer));
        }
    }
    private static final class PlayerRenderState {
        final net.minecraft.client.model.ModelBase model;
        final net.minecraft.entity.player.EntityPlayer player;
        final float body,previousBody,head,previousHead;
        PlayerRenderState(net.minecraft.client.model.ModelBase model,net.minecraft.entity.player.EntityPlayer p) {
            this.model=model;player=p;body=p.renderYawOffset;previousBody=p.prevRenderYawOffset;head=p.rotationYawHead;previousHead=p.prevRotationYawHead;
        }
        void restore() {player.renderYawOffset=body;player.prevRenderYawOffset=previousBody;player.rotationYawHead=head;player.prevRotationYawHead=previousHead;}
    }
}
