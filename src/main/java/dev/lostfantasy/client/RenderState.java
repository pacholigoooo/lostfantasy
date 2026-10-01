package dev.lostfantasy.client;

import java.nio.FloatBuffer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;

/** One snapshot per render pass; restore through Minecraft so its GL cache stays in sync. */
final class RenderState implements AutoCloseable {
    private static final FloatBuffer VALUES = BufferUtils.createFloatBuffer(16);
    private final boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
    private final boolean alpha = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
    private final boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
    private final boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
    private final boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
    private final boolean rescaleNormal = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);
    private final boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
    private final int shade = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
    private final float lightX = OpenGlHelper.lastBrightnessX, lightY = OpenGlHelper.lastBrightnessY;
    private final boolean depthWrite = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
    private final int srcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
    private final int dstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
    private final int srcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
    private final int dstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
    private final int alphaFunction = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
    private final float alphaReference = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);
    private final int activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
    private final int activeBinding = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
    private final boolean activeTextured = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
    private final int defaultBinding;
    private final boolean defaultTextured;
    private final float red, green, blue, opacity;
    private final float normalX,normalY,normalZ;
    private final ShaderBridge.ProgramState shaderProgram=ShaderBridge.captureProgram();

    RenderState() {
        VALUES.clear();
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, VALUES);
        red = VALUES.get(0);
        green = VALUES.get(1);
        blue = VALUES.get(2);
        opacity = VALUES.get(3);
        VALUES.clear();GL11.glGetFloat(GL11.GL_CURRENT_NORMAL,VALUES);
        normalX=VALUES.get(0);normalY=VALUES.get(1);normalZ=VALUES.get(2);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        defaultBinding = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        defaultTextured = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        GlStateManager.setActiveTexture(activeTexture);
    }

    @Override public void close() {
        // Switching programs can release/reapply OptiFine's alpha and blend locks.
        // Restore the program first, then the physical state and finally its saved logical locks.
        if(shaderProgram!=null)shaderProgram.restoreProgram();
        GlStateManager.tryBlendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
        GlStateManager.alphaFunc(alphaFunction, alphaReference);
        GlStateManager.depthMask(depthWrite);
        GlStateManager.shadeModel(shade);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightX, lightY);
        if (depthTest) GlStateManager.enableDepth(); else GlStateManager.disableDepth();
        if (blend) GlStateManager.enableBlend(); else GlStateManager.disableBlend();
        if (alpha) GlStateManager.enableAlpha(); else GlStateManager.disableAlpha();
        if (lighting) GlStateManager.enableLighting(); else GlStateManager.disableLighting();
        if (fog) GlStateManager.enableFog(); else GlStateManager.disableFog();
        if (cull) GlStateManager.enableCull(); else GlStateManager.disableCull();
        if (rescaleNormal) GlStateManager.enableRescaleNormal(); else GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.bindTexture(defaultBinding);
        if (defaultTextured) GlStateManager.enableTexture2D(); else GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(activeTexture);
        GlStateManager.bindTexture(activeBinding);
        if (activeTextured) GlStateManager.enableTexture2D(); else GlStateManager.disableTexture2D();
        // Vertex-array drawing can update GL's colour without updating Minecraft's colour cache.
        GlStateManager.resetColor();
        GlStateManager.color(red, green, blue, opacity);
        GL11.glNormal3f(normalX,normalY,normalZ);
        if(shaderProgram!=null)shaderProgram.restoreLocks();
    }
}
