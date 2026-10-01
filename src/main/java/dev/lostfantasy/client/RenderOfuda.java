package dev.lostfantasy.client;
import dev.lostfantasy.ModItems;
import dev.lostfantasy.entity.EntityOfuda;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
public final class RenderOfuda extends Render<EntityOfuda> {
    private final ItemStack paper=new ItemStack(ModItems.OFUDA);
    public RenderOfuda(RenderManager manager) {super(manager);}
    @Override public void doRender(EntityOfuda e,double x,double y,double z,float yaw,float partial) {
        try(RenderMatrix matrix=new RenderMatrix()) {
            GlStateManager.translate(x,y,z);
            GlStateManager.rotate(e.prevRotationYaw+net.minecraft.util.math.MathHelper.wrapDegrees(e.rotationYaw-e.prevRotationYaw)*partial,0,1,0);
            GlStateManager.rotate(e.prevRotationPitch+(e.rotationPitch-e.prevRotationPitch)*partial,1,0,0);
            // The item lies in XY: +90 about X lays it flat, with its long edge along flight +Z.
            GlStateManager.rotate(90,1,0,0);
            GlStateManager.rotate((float)Math.sin((e.ticksExisted+partial)*.5)*4,0,1,0);GlStateManager.scale(.6,.6,.6);
            bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);Minecraft.getMinecraft().getRenderItem().renderItem(paper,ItemCameraTransforms.TransformType.NONE);
        }
    }
    @Override protected ResourceLocation getEntityTexture(EntityOfuda e) {return TextureMap.LOCATION_BLOCKS_TEXTURE;}
}
