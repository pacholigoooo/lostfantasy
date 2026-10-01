package dev.lostfantasy.client;

import java.util.*;
import javax.vecmath.Matrix4f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

/** Retains the ordinary mesh transforms while drawing effects in the very same item space. */
final class RibbonItemModel implements IBakedModel {
    final MeshItemModel mesh;
    final RibbonItemRenderer.Geometry geometry;
    final EntityLivingBase holder;
    ItemCameraTransforms.TransformType transform=ItemCameraTransforms.TransformType.NONE;
    private final ItemOverrideList overrides=new ItemOverrideList(Collections.emptyList()) {
        @Override public IBakedModel handleItemState(IBakedModel original,ItemStack stack,World world,EntityLivingBase entity) {
            return new RibbonItemModel(mesh,geometry,entity);
        }
    };
    RibbonItemModel(MeshItemModel mesh){this(mesh,new RibbonItemRenderer.Geometry(mesh),null);}
    private RibbonItemModel(MeshItemModel mesh,RibbonItemRenderer.Geometry geometry,EntityLivingBase holder){this.mesh=mesh;this.geometry=geometry;this.holder=holder;}
    @Override public List<BakedQuad> getQuads(IBlockState state,EnumFacing side,long seed){return mesh.getQuads(state,side,seed);}
    @Override public boolean isAmbientOcclusion(){return false;}
    @Override public boolean isGui3d(){return true;}
    @Override public boolean isBuiltInRenderer(){return true;}
    @Override public TextureAtlasSprite getParticleTexture(){return mesh.getParticleTexture();}
    @Override public ItemCameraTransforms getItemCameraTransforms(){return mesh.getItemCameraTransforms();}
    @Override public ItemOverrideList getOverrides(){return overrides;}
    @Override public Pair<? extends IBakedModel,Matrix4f> handlePerspective(ItemCameraTransforms.TransformType type) {
        transform=type;RibbonItemRenderer.INSTANCE.context=this;
        return Pair.of(this,mesh.handlePerspective(type).getRight());
    }
}
