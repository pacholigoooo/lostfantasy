package dev.lostfantasy.client;

import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import org.apache.commons.lang3.tuple.Pair;
import javax.vecmath.Matrix4f;

/** Radial cutout petals and curved stamens: 48 water / 56 bank quads, cached at resource load. */
final class LilyFieldModel implements IBakedModel {
    static final ResourceLocation PETAL=new ResourceLocation("lostfantasy:blocks/lily_petal");
    static final ResourceLocation STAMEN=new ResourceLocation("lostfantasy:blocks/lily_stamen");
    private final IBakedModel base;
    private final List<BakedQuad> quads;
    private final TextureAtlasSprite petal;
    LilyFieldModel(IBakedModel base,boolean floating) {
        this.base=base;petal=sprite(PETAL);TextureAtlasSprite stamen=sprite(STAMEN),solid=sprite(MeshItemModel.TEXTURE);
        List<BakedQuad> faces=new ArrayList<>();
        double[][] heads={{-.17,.10,.36,.78,.1},{.13,-.08,.52,1,.7}};
        int[] reds={0xffd60913,0xfffc262b,0xfffa514f};
        for(double[] head:heads) {
            double cx=.5+head[0],cz=.5-head[1],height=floating?-.11:head[2],size=head[3];
            for(int i=0;i<6;i++) {
                double angle=head[4]+i*Math.PI/3;
                radial(faces,petal,cx,height,cz,size,angle+.16,.35,reds[i%3]);
                radial(faces,stamen,cx,height,cz,size,angle+.4*Math.PI/3,.10,0xfffa514f);
            }
            if(!floating)for(int i=0;i<2;i++) {
                double angle=i*Math.PI/2,dx=Math.cos(angle)*.012,dz=Math.sin(angle)*.012;
                double[][] v={{cx-.045-dx,0,cz-.025-dz},{cx-.045+dx,0,cz-.025+dz},{cx+dx,height,cz+dz},{cx-dx,height,cz-dz}};
                doubleSided(faces,solid,v,0xff183329);
            }
        }
        quads=Collections.unmodifiableList(faces);
    }
    private static TextureAtlasSprite sprite(ResourceLocation id) {return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(id.toString());}
    private static void radial(List<BakedQuad> faces,TextureAtlasSprite sprite,double cx,double cy,double cz,double size,double angle,double tilt,int color) {
        double[][] corners={{-.03,-.15},{.61,-.15},{.61,.49},{-.03,.49}},v=new double[4][3];
        for(int i=0;i<4;i++) {
            double radial=corners[i][0]*size,y=corners[i][1]*size,tangent=y*tilt;
            v[i]=new double[]{cx+Math.cos(angle)*radial-Math.sin(angle)*tangent,cy+y,cz-Math.sin(angle)*radial-Math.cos(angle)*tangent};
        }
        doubleSided(faces,sprite,v,color);
    }
    private static Vec3d point(double[] v){return new Vec3d(v[0],v[1],v[2]);}
    private static void doubleSided(List<BakedQuad> faces,TextureAtlasSprite sprite,double[][] v,int color) {
        for(int side=0;side<2;side++) {
            UnpackedBakedQuad.Builder b=new UnpackedBakedQuad.Builder(DefaultVertexFormats.ITEM);
            b.setTexture(sprite);b.setApplyDiffuseLighting(true);b.setQuadTint(-1);b.setContractUVs(false);
            Vec3d normal=point(v[1]).subtract(point(v[0])).crossProduct(point(v[2]).subtract(point(v[0]))).normalize().scale(side==0?1:-1);
            b.setQuadOrientation(EnumFacing.getFacingFromVector((float)normal.x,(float)normal.y,(float)normal.z));
            for(int n=0;n<4;n++) {
                int i=side==0?n:3-n;
                for(int e=0;e<DefaultVertexFormats.ITEM.getElementCount();e++)switch(DefaultVertexFormats.ITEM.getElement(e).getUsage()) {
                    case POSITION:b.put(e,(float)v[i][0],(float)v[i][1],(float)v[i][2],1);break;
                    case COLOR:b.put(e,(color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f,1);break;
                    case UV:b.put(e,sprite.getInterpolatedU(i==0||i==3?0:16),sprite.getInterpolatedV(i<2?16:0),0,1);break;
                    case NORMAL:b.put(e,(float)normal.x,(float)normal.y,(float)normal.z,0);break;
                    default:b.put(e);break;
                }
            }
            faces.add(b.build());
        }
    }
    @Override public List<BakedQuad> getQuads(IBlockState state,EnumFacing side,long seed){return side==null?quads:Collections.emptyList();}
    @Override public boolean isAmbientOcclusion(){return false;}
    @Override public boolean isGui3d(){return true;}
    @Override public boolean isBuiltInRenderer(){return false;}
    @Override public TextureAtlasSprite getParticleTexture(){return petal;}
    @Override public ItemOverrideList getOverrides(){return ItemOverrideList.NONE;}
    @Override public ItemCameraTransforms getItemCameraTransforms(){return base.getItemCameraTransforms();}
    @Override public Pair<? extends IBakedModel,Matrix4f> handlePerspective(ItemCameraTransforms.TransformType type){return Pair.of(this,base.handlePerspective(type).getRight());}
}
