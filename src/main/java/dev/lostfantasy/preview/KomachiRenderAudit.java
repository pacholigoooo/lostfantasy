package dev.lostfantasy.preview;

import dev.lostfantasy.client.RenderKomachi;
import dev.lostfantasy.entity.EntityKomachi;
import java.lang.reflect.*;
import java.nio.FloatBuffer;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

/** Runs only from the opt-in preview, against the actual renderer in a real OpenGL context. */
public final class KomachiRenderAudit {
    private static final RuntimeException EXPECTED = new RuntimeException("Intentional render failure");

    public static void run(Minecraft mc, EntityKomachi entity) {
        Snapshot original = new Snapshot();
        try {
            RenderKomachi renderer = new RenderKomachi(mc.getRenderManager());
            Method model = RenderKomachi.class.getDeclaredMethod("renderModel", EntityKomachi.class,
                    float.class, float.class, float.class, float.class, float.class, float.class);
            model.setAccessible(true);
            Runnable drawModel = () -> invoke(model, renderer, entity, 0f, 0f, (float)entity.ticksExisted, 0f, 0f, .0625f);
            Field layersField = RenderLivingBase.class.getDeclaredField("layerRenderers");
            layersField.setAccessible(true);
            @SuppressWarnings("unchecked") LayerRenderer<EntityKomachi> layer =
                    ((List<LayerRenderer<EntityKomachi>>)layersField.get(renderer)).get(0);
            Runnable drawLayer = () -> layer.doRenderLayer(entity, 0, 0, 0, entity.ticksExisted, 0, 0, .0625f);

            if (Boolean.getBoolean("lostfantasy.komachiStateBaseline")) {
                seed(mc, true);
                Snapshot before = new Snapshot();
                drawModel.run();
                List<String> leaked = before.differences(new Snapshot());
                require(!leaked.isEmpty(), "Baseline unexpectedly preserved every state");
                System.out.println("KOMACHI_STATE_BASELINE_LEAK " + leaked);
                return;
            }
            for (boolean enabled : new boolean[]{true, false}) {
                check(mc, drawModel, enabled, false, "model");
                check(mc, drawLayer, enabled, false, "held items");
            }
            Field modelField = RenderLivingBase.class.getDeclaredField("mainModel");
            modelField.setAccessible(true);
            ModelBase realModel = renderer.getMainModel();
            modelField.set(renderer, new ModelBase() {
                @Override public void render(Entity e, float a, float b, float c, float d, float f, float scale) {
                    throw EXPECTED;
                }
            });
            try { check(mc, drawModel, true, true, "failed model"); }
            finally { modelField.set(renderer, realModel); }

            ModelBiped biped = (ModelBiped)realModel;
            ModelRenderer right = biped.bipedRightArm;
            biped.bipedRightArm = new ModelRenderer(realModel) {
                @Override public void postRender(float scale) { throw EXPECTED; }
            };
            try { check(mc, drawLayer, false, true, "failed hand"); }
            finally { biped.bipedRightArm = right; }
            require(GL11.glGetError() == GL11.GL_NO_ERROR, "OpenGL error during renderer audit");
            System.out.println("KOMACHI_STATE_AUDIT_PASSED 6 cases: model/items, enabled/disabled, injected failures");
            effects(mc);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        } finally { original.restore(); }
    }

    private static void effects(Minecraft mc) throws ReflectiveOperationException {
        int cases=0;
        for(dev.lostfantasy.core.Spell spell:dev.lostfantasy.core.Spell.catalog()) {
            dev.lostfantasy.network.EffectMessage effect=new dev.lostfantasy.network.EffectMessage(
                    spell.networkId,mc.player.getEntityId(),mc.player.dimension,mc.world.getTotalWorldTime()-16,
                    120,40,mc.player.getPositionVector(),new net.minecraft.util.math.Vec3d(0,0,1),8);
            effect.columns=Collections.singletonList(new dev.lostfantasy.core.EmeraldCity.Column(mc.player.posX,mc.player.posY,mc.player.posZ,4,0));
            for(boolean enabled:new boolean[]{false,true}) {
                dev.lostfantasy.client.SpellVisuals.receive(effect);
                try {check(mc,()->dev.lostfantasy.client.SpellVisuals.render(0),enabled,false,"spell "+spell);cases++;}
                finally {dev.lostfantasy.client.SpellVisuals.clear();}
            }
        }
        dev.lostfantasy.client.RenderRiverFerry ferry=new dev.lostfantasy.client.RenderRiverFerry(mc.getRenderManager());
        dev.lostfantasy.entity.EntityRiverFerry boat=new dev.lostfantasy.entity.EntityRiverFerry(mc.world);
        Method inset=Class.forName("dev.lostfantasy.client.ResearchScene").getDeclaredMethod("sampleInset",int.class,int.class,float.class);
        inset.setAccessible(true);
        Class<?> research=Class.forName("dev.lostfantasy.client.ResearchVisual");
        Method receive=research.getDeclaredMethod("receive",dev.lostfantasy.network.ResearchMessage.class);
        Method render=research.getDeclaredMethod("render",float.class);receive.setAccessible(true);render.setAccessible(true);
        net.minecraft.util.math.BlockPos desk=mc.player.getPosition().add(0,1,2);
        net.minecraft.block.state.IBlockState previous=mc.world.getBlockState(desk);
        mc.world.setBlockState(desk,dev.lostfantasy.ModBlocks.EMERALD_STUDY.getDefaultState(),2);
        try {
            dev.lostfantasy.network.ResearchMessage demo=new dev.lostfantasy.network.ResearchMessage();
            demo.dimension=mc.player.dimension;demo.pos=desk;demo.started=mc.world.getTotalWorldTime()-40;invoke(receive,null,demo);
            for(boolean enabled:new boolean[]{false,true}) {
                check(mc,()->ferry.doRender(boat,0,0,0,0,0),enabled,false,"ferry and lantern");cases++;
                check(mc,()->invoke(inset,null,0,0,1f),enabled,false,"research specimen");cases++;
                check(mc,()->invoke(render,null,0f),enabled,false,"research desk");cases++;
            }
        } finally {mc.world.setBlockState(desk,previous,2);}
        net.minecraft.item.ItemStack ribbon=new net.minecraft.item.ItemStack(dev.lostfantasy.ModItems.MISFORTUNE_RIBBON);
        net.minecraft.client.renderer.block.model.IBakedModel model=mc.getRenderItem().getItemModelWithOverrides(ribbon,mc.world,mc.player);
        Field context=dev.lostfantasy.client.RibbonItemRenderer.class.getDeclaredField("context");context.setAccessible(true);
        for(boolean glowing:new boolean[]{false,true}) {
            if(glowing)dev.lostfantasy.core.RibbonFortune.mark(ribbon,mc.player.getUniqueID(),mc.player.dimension,mc.world.getTotalWorldTime()-100);
            for(boolean enabled:new boolean[]{false,true}) {
                check(mc,()->{
                    model.handlePerspective(net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND);
                    dev.lostfantasy.client.RibbonItemRenderer.INSTANCE.renderByItem(ribbon);
                },enabled,false,"ribbon glow="+glowing);cases++;
                require(context.get(dev.lostfantasy.client.RibbonItemRenderer.INSTANCE)==null,"ribbon retained holder after draw");
            }
        }
        model.handlePerspective(net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.GUI);
        new dev.lostfantasy.client.ClientEvents().unload(new net.minecraftforge.event.world.WorldEvent.Unload(mc.world));
        require(context.get(dev.lostfantasy.client.RibbonItemRenderer.INSTANCE)==null,"ribbon retained holder at world unload");
        require(GL11.glGetError()==GL11.GL_NO_ERROR,"effect GL error");
        System.out.println("EFFECT_STATE_AUDIT_PASSED "+cases+" cases; ribbon consumption and unload released context");
    }

    public static void water(Minecraft mc) {
        Snapshot original=new Snapshot();
        try {
            for(boolean enabled:new boolean[]{false,true})check(mc,()->dev.lostfantasy.client.HiganAtmosphere.render(0),enabled,false,"Higan water");
            require(GL11.glGetError()==GL11.GL_NO_ERROR,"water GL error");
            System.out.println("WATER_STATE_AUDIT_PASSED 2 cases");
        } finally {original.restore();}
    }

    private static void invoke(Method method, Object receiver, Object... args) {
        try { method.invoke(receiver, args); }
        catch (InvocationTargetException ex) {
            if (ex.getCause() == EXPECTED) throw EXPECTED;
            throw new IllegalStateException(ex.getCause());
        } catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
    }
    private static void check(Minecraft mc, Runnable draw, boolean enabled, boolean failure, String label) {
        seed(mc, enabled);
        Snapshot before = new Snapshot();
        boolean failed = false;
        try { draw.run(); }
        catch (RuntimeException ex) { if (ex != EXPECTED) throw ex; failed = true; }
        require(failed == failure, label + " failure path");
        List<String> changes = before.differences(new Snapshot());
        require(changes.isEmpty(), label + " leaked " + changes);
        // Verify the Minecraft state cache agrees after restoration as well.
        GlStateManager.color(.9f, .8f, .7f, .6f);
        before.restore();
        require(before.differences(new Snapshot()).isEmpty(), label + " cache mismatch");
    }
    private static void seed(Minecraft mc, boolean enabled) {
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        if (enabled) {
            GlStateManager.enableBlend(); GlStateManager.enableAlpha(); GlStateManager.enableLighting();
            GlStateManager.enableCull(); GlStateManager.enableRescaleNormal();
        } else {
            GlStateManager.disableBlend(); GlStateManager.disableAlpha(); GlStateManager.disableLighting();
            GlStateManager.disableCull(); GlStateManager.disableRescaleNormal();
        }
        GlStateManager.depthMask(enabled);
        if(enabled)GlStateManager.enableDepth();else GlStateManager.disableDepth();
        GlStateManager.shadeModel(enabled?GL11.GL_FLAT:GL11.GL_SMOOTH);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,43,91);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GEQUAL, .37f);
        GlStateManager.color(.2f, .4f, .6f, .35f);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private static final class Snapshot {
        final int[] keys = {GL14.GL_BLEND_SRC_RGB, GL14.GL_BLEND_DST_RGB, GL14.GL_BLEND_SRC_ALPHA,
                GL14.GL_BLEND_DST_ALPHA, GL11.GL_ALPHA_TEST_FUNC, GL13.GL_ACTIVE_TEXTURE,
                GL11.GL_TEXTURE_BINDING_2D, GL11.GL_MODELVIEW_STACK_DEPTH,GL11.GL_SHADE_MODEL};
        final String[] names = {"srcRGB", "dstRGB", "srcAlpha", "dstAlpha", "alphaFunc", "activeTexture", "texture", "matrixDepth","shade"};
        final int[] caps = {GL11.GL_BLEND, GL11.GL_ALPHA_TEST, GL11.GL_LIGHTING, GL11.GL_CULL_FACE, GL12.GL_RESCALE_NORMAL, GL11.GL_TEXTURE_2D,GL11.GL_DEPTH_TEST};
        final int[] integers = new int[keys.length];
        final boolean[] enabled = new boolean[caps.length];
        final boolean depth = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        final float threshold = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);
        final float lightX=OpenGlHelper.lastBrightnessX,lightY=OpenGlHelper.lastBrightnessY;
        final float[] color = new float[4];
        Snapshot() {
            for (int i=0;i<keys.length;i++) integers[i]=GL11.glGetInteger(keys[i]);
            for (int i=0;i<caps.length;i++) enabled[i]=GL11.glIsEnabled(caps[i]);
            FloatBuffer buffer=BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_CURRENT_COLOR,buffer);
            for (int i=0;i<4;i++) color[i]=buffer.get(i);
        }
        List<String> differences(Snapshot other) {
            List<String> changes=new ArrayList<>();
            for (int i=0;i<keys.length;i++) if(integers[i]!=other.integers[i])changes.add(names[i]);
            for (int i=0;i<caps.length;i++) if(enabled[i]!=other.enabled[i])changes.add("enable:"+caps[i]);
            if(depth!=other.depth)changes.add("depthWrite");
            if(lightX!=other.lightX || lightY!=other.lightY)changes.add("lightmap");
            if(Math.abs(threshold-other.threshold)>.00001)changes.add("alphaReference");
            for(int i=0;i<4;i++)if(Math.abs(color[i]-other.color[i])>.00001)changes.add("color:"+i);
            return changes;
        }
        void restore() {
            GlStateManager.tryBlendFuncSeparate(integers[0],integers[1],integers[2],integers[3]);
            GlStateManager.alphaFunc(integers[4],threshold);GlStateManager.depthMask(depth);
            GlStateManager.setActiveTexture(integers[5]);GlStateManager.bindTexture(integers[6]);
            if(enabled[0])GlStateManager.enableBlend();else GlStateManager.disableBlend();
            if(enabled[1])GlStateManager.enableAlpha();else GlStateManager.disableAlpha();
            if(enabled[2])GlStateManager.enableLighting();else GlStateManager.disableLighting();
            if(enabled[3])GlStateManager.enableCull();else GlStateManager.disableCull();
            if(enabled[4])GlStateManager.enableRescaleNormal();else GlStateManager.disableRescaleNormal();
            if(enabled[5])GlStateManager.enableTexture2D();else GlStateManager.disableTexture2D();
            if(enabled[6])GlStateManager.enableDepth();else GlStateManager.disableDepth();
            GlStateManager.shadeModel(integers[8]);OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,lightX,lightY);
            GlStateManager.resetColor();GlStateManager.color(color[0],color[1],color[2],color[3]);
        }
    }
}
