package dev.lostfantasy.client;

import dev.lostfantasy.core.CastMotion;
import dev.lostfantasy.core.FourfoldBarrier;
import dev.lostfantasy.network.EffectMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

/** All vertices lie on one horizontal plane: no walls, volume or inward contraction. */
final class BarrierVisual {
    static void draw(EffectMessage effect, double age) {
        if (age >= FourfoldBarrier.FADE_END) return;
        Entity caster = Minecraft.getMinecraft().world.getEntityByID(effect.caster);
        if (caster == null || !caster.isEntityAlive()) return;
        float partial = Minecraft.getMinecraft().getRenderPartialTicks();
        Vec3d direction = new Vec3d(effect.dx, 0, effect.dz);
        Vec3d side = new Vec3d(effect.dz, 0, -effect.dx);
        Vec3d center = new Vec3d(caster.lastTickPosX+(caster.posX-caster.lastTickPosX)*partial,
                caster.lastTickPosY+(caster.posY-caster.lastTickPosY)*partial+FourfoldBarrier.PLANE_HEIGHT,
                caster.lastTickPosZ+(caster.posZ-caster.lastTickPosZ)*partial).add(direction.scale(FourfoldBarrier.CENTER));
        double size = CastMotion.smooth(age/FourfoldBarrier.OPEN);
        double fade = age < FourfoldBarrier.FINISH_HIT ? 1 : 1-(age-FourfoldBarrier.FINISH_HIT)/4;
        double flash = age >= FourfoldBarrier.FINISH_HIT ? 1 : Math.max(0, 1-((age-6)%4)/1.6)*.35;
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int layer = 0; layer < 4; layer++) {
            Vec3d[] corners = new Vec3d[4];
            for (int i = 0; i < 4; i++) {
                double[] point = FourfoldBarrier.corner(layer, i);
                corners[i] = center.add(side.scale(point[0]*size)).add(direction.scale(point[1]*size));
                vertex(buffer, corners[i], 172, 108, 231, (int)((9+flash*9)*fade));
            }
            for (int i = 0; i < 4; i++) {
                Vec3d from = corners[i], to = corners[(i+1)%4];
                stroke(buffer, from, to, .055, 157, 82, 223, (int)(65*fade));
                stroke(buffer, from, to, .018, 211, 166, 255, (int)((155+flash*90)*fade));
                Vec3d toward = center.subtract(from).normalize().scale(.17*size);
                stroke(buffer, from.add(toward), from.add(to.subtract(from).scale(.12)), .025, 255, 222, 151, (int)(230*fade));
                stroke(buffer, from.add(toward), from.add(corners[(i+3)%4].subtract(from).scale(.12)), .025, 255, 222, 151, (int)(230*fade));
            }
        }
        // A faint footprint makes the actual rectangle (including its near dead zone) readable.
        Vec3d front = direction.scale(1.5*size), wide = side.scale(2.5*size);
        Vec3d[] edge = {center.add(front).add(wide), center.add(front).subtract(wide), center.subtract(front).subtract(wide), center.subtract(front).add(wide)};
        for (int i=0;i<4;i++) stroke(buffer, edge[i], edge[(i+1)%4], .009, 217, 193, 247, (int)(65*fade));
        Tessellator.getInstance().draw();
    }

    private static void stroke(BufferBuilder buffer, Vec3d from, Vec3d to, double width, int r, int g, int b, int a) {
        Vec3d delta=to.subtract(from), normal=new Vec3d(delta.z,0,-delta.x).normalize().scale(width);
        vertex(buffer,from.add(normal),r,g,b,a); vertex(buffer,to.add(normal),r,g,b,a);
        vertex(buffer,to.subtract(normal),r,g,b,a); vertex(buffer,from.subtract(normal),r,g,b,a);
    }
    private static void vertex(BufferBuilder buffer, Vec3d point, int r, int g, int b, int a) {
        buffer.pos(point.x,point.y,point.z).color(r,g,b,Math.max(0,Math.min(255,a))).endVertex();
    }
}
