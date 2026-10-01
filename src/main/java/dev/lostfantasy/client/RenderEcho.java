package dev.lostfantasy.client;
import dev.lostfantasy.entity.EntityCompanion;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
public final class RenderEcho extends RenderLiving<EntityCompanion> {
    private final ModelPlayer regular=new ModelPlayer(0,false),slim=new ModelPlayer(0,true);
    public RenderEcho(RenderManager manager) {super(manager,new ModelPlayer(0,false),.35f);addLayer(new LayerHeldItem(this));}
    @Override public void doRender(EntityCompanion e,double x,double y,double z,float yaw,float partial) {
        boolean small=e.owner() instanceof AbstractClientPlayer&&"slim".equals(((AbstractClientPlayer)e.owner()).getSkinType());
        ModelPlayer body=small?slim:regular;
        body.rightArmPose=e.getHeldItemMainhand().isEmpty()?net.minecraft.client.model.ModelBiped.ArmPose.EMPTY:e.getHeldItemMainhand().getItem() instanceof net.minecraft.item.ItemBow?net.minecraft.client.model.ModelBiped.ArmPose.BOW_AND_ARROW:net.minecraft.client.model.ModelBiped.ArmPose.ITEM;
        mainModel=body;super.doRender(e,x,y,z,yaw,partial);
    }
    @Override protected ResourceLocation getEntityTexture(EntityCompanion e) {
        return e.owner() instanceof AbstractClientPlayer?((AbstractClientPlayer)e.owner()).getLocationSkin():DefaultPlayerSkin.getDefaultSkinLegacy();
    }
    @Override protected boolean canRenderName(EntityCompanion entity) {return false;}
}
