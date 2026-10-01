package dev.lostfantasy.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import org.apache.commons.lang3.tuple.Pair;
import javax.vecmath.Matrix4f;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Bakes coloured Blender triangles during resource reload, using ordinary item rendering. */
final class MeshItemModel implements IBakedModel {
    static final ResourceLocation TEXTURE = new ResourceLocation("lostfantasy", "blocks/gohei_white");
    static final ResourceLocation SILK = new ResourceLocation("lostfantasy", "items/misfortune_ribbon_satin");
    private static final float[][] UV = {{1, 1}, {15, 1}, {8, 8}, {1, 15}};

    private final IBakedModel base;
    private final List<BakedQuad> quads;
    private final TextureAtlasSprite sprite;

    MeshItemModel(IBakedModel base, String mesh, boolean diffuseLighting) throws IOException {
        this(base, mesh, diffuseLighting, TEXTURE);
    }

    MeshItemModel(IBakedModel base, String mesh, boolean diffuseLighting, ResourceLocation texture) throws IOException {
        this(base,mesh,diffuseLighting,texture,0);
    }

    MeshItemModel(IBakedModel base,String mesh,boolean diffuseLighting,ResourceLocation texture,int quarterTurns) throws IOException {
        this.base = base;
        sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString());
        ResourceLocation location = new ResourceLocation("lostfantasy", "meshes/" + mesh + ".lfm");
        try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(location);
             DataInputStream input = new DataInputStream(new BufferedInputStream(resource.getInputStream()))) {
            quads = bake(input, sprite, diffuseLighting,quarterTurns,mesh.startsWith("bamboo_")
                    || texture.equals(new ResourceLocation("lostfantasy:blocks/interior_furniture")));
        }
    }

    static List<BakedQuad> bake(DataInputStream input, TextureAtlasSprite sprite, boolean diffuseLighting) throws IOException {
        return bake(input,sprite,diffuseLighting,0,false);
    }

    private static List<BakedQuad> bake(DataInputStream input,TextureAtlasSprite sprite,boolean diffuseLighting,int quarterTurns,boolean mergeFaces) throws IOException {
        int header = input.readInt();
        boolean textured = header == 0x4c464d32;
        if (!textured && header != 0x4c464d31) throw new IOException("Bad item mesh header");
        int count = input.readInt();
        if (count <= 0 || count % 3 != 0 || count > 12000) throw new IOException("Invalid item vertex count");
        List<BakedQuad> output = new ArrayList<>(count / 3);
        Face pending=null;
        for (int triangle = 0; triangle < count / 3; triangle++) {
            float[][] positions = new float[3][3];
            int[] colors = new int[3];
            float[][] textureUv = new float[3][2];
            Vec3d[] smoothNormals = new Vec3d[3];
            for (int vertex = 0; vertex < 3; vertex++) {
                for (int axis = 0; axis < 3; axis++) {
                    positions[vertex][axis] = input.readFloat();
                    if (!Float.isFinite(positions[vertex][axis])) throw new IOException("Non-finite item vertex");
                }
                colors[vertex] = input.readInt();
                if (textured) {
                    for (int axis = 0; axis < 2; axis++) {
                        float value = input.readFloat();
                        if (!Float.isFinite(value) || value < 0 || value > 1) throw new IOException("Invalid item UV");
                        textureUv[vertex][axis] = value;
                    }
                    float nx = input.readFloat(), ny = input.readFloat(), nz = input.readFloat();
                    if (!Float.isFinite(nx) || !Float.isFinite(ny) || !Float.isFinite(nz)) throw new IOException("Invalid item normal");
                    Vec3d n = new Vec3d(nx, ny, nz);
                    double norm = Math.sqrt(n.lengthSquared());
                    if (norm < 1e-6) throw new IOException("Zero item normal");
                    smoothNormals[vertex] = n.scale(1 / norm);
                }
                for(int turn=0;turn<Math.floorMod(quarterTurns,4);turn++) {
                    float x=positions[vertex][0];
                    positions[vertex][0]=1-positions[vertex][2];positions[vertex][2]=x;
                    if(textured) {
                        Vec3d n=smoothNormals[vertex];smoothNormals[vertex]=new Vec3d(-n.z,n.y,n.x);
                    }
                }
            }
            Vec3d a = new Vec3d(positions[0][0], positions[0][1], positions[0][2]);
            Vec3d b = new Vec3d(positions[1][0], positions[1][1], positions[1][2]);
            Vec3d c = new Vec3d(positions[2][0], positions[2][1], positions[2][2]);
            Vec3d cross = b.subtract(a).crossProduct(c.subtract(a));
            double length = Math.sqrt(cross.lengthSquared());
            if (length < 1e-12) throw new IOException("Degenerate item face");
            // Vec3d.normalize() rounds small, valid faces to zero.
            Vec3d normal = cross.scale(1 / length);
            Face face=new Face(positions,colors,textureUv,smoothNormals,normal);
            if(mergeFaces && textured) {
                if(pending==null) {pending=face;continue;}
                Face merged=pending.merge(face);
                if(merged!=null) {output.add(bakeFace(merged,sprite,diffuseLighting,true));pending=null;}
                else {output.add(bakeFace(pending,sprite,diffuseLighting,true));pending=face;}
            } else output.add(bakeFace(face,sprite,diffuseLighting,textured));
        }
        if(pending!=null)output.add(bakeFace(pending,sprite,diffuseLighting,textured));
        if (input.read() != -1) throw new IOException("Trailing item mesh data");
        return Collections.unmodifiableList(output);
    }

    /** Only adjacent coplanar triangles with identical shared-edge attributes are joined. */
    private static final class Face {
        final float[][] positions,uv;final int[] colors;final Vec3d[] normals;final Vec3d normal;
        Face(float[][] positions,int[] colors,float[][] uv,Vec3d[] normals,Vec3d normal) {
            this.positions=positions;this.colors=colors;this.uv=uv;this.normals=normals;this.normal=normal;
        }
        boolean same(int i,Face b,int j) {
            if(colors[i]!=b.colors[j])return false;
            for(int k=0;k<3;k++)if(Math.abs(positions[i][k]-b.positions[j][k])>1e-6)return false;
            for(int k=0;k<2;k++)if(Math.abs(uv[i][k]-b.uv[j][k])>1e-6)return false;
            return normals[i].squareDistanceTo(b.normals[j])<1e-12;
        }
        Vec3d point(int i) {return new Vec3d(positions[i][0],positions[i][1],positions[i][2]);}
        Face merge(Face b) {
            if(normal.dotProduct(b.normal)<.999999)return null;
            for(int i=0;i<3;i++)for(int j=0;j<3;j++) {
                int next=(i+1)%3,back=(j+1)%3;
                if(!same(i,b,back) || !same(next,b,j))continue;
                int extra=(j+2)%3,last=(i+2)%3;
                if(Math.abs(b.point(extra).subtract(point(i)).dotProduct(normal))>1e-6)return null;
                // Original shared edge remains the quad's 0-2 diagonal, preserving interpolation.
                Face q=new Face(new float[][]{positions[i],b.positions[extra],positions[next],positions[last]},
                        new int[]{colors[i],b.colors[extra],colors[next],colors[last]},
                        new float[][]{uv[i],b.uv[extra],uv[next],uv[last]},
                        new Vec3d[]{normals[i],b.normals[extra],normals[next],normals[last]},normal);
                for(int k=0;k<4;k++)if(q.point((k+1)%4).subtract(q.point(k))
                        .crossProduct(q.point((k+2)%4).subtract(q.point((k+1)%4))).dotProduct(normal)<=1e-12)return null;
                return q;
            }
            return null;
        }
    }
    private static BakedQuad bakeFace(Face face,TextureAtlasSprite sprite,boolean diffuseLighting,boolean textured) {
        boolean quad=face.positions.length==4;
        UnpackedBakedQuad.Builder builder=new UnpackedBakedQuad.Builder(DefaultVertexFormats.ITEM);
        builder.setTexture(sprite);builder.setContractUVs(false);builder.setQuadTint(-1);
        builder.setApplyDiffuseLighting(diffuseLighting);
        builder.setQuadOrientation(EnumFacing.getFacingFromVector((float)face.normal.x,(float)face.normal.y,(float)face.normal.z));
        for(int vertex=0;vertex<4;vertex++) {
            boolean midpoint=!quad && vertex==2;
            int source=quad?vertex:vertex==0?0:vertex==1?1:2;
            float[] position=face.positions[source];
            if(midpoint)position=new float[]{(face.positions[1][0]+face.positions[2][0])/2,
                    (face.positions[1][1]+face.positions[2][1])/2,(face.positions[1][2]+face.positions[2][2])/2};
            int color=face.colors[source];Vec3d shadingNormal=textured?face.normals[source]:face.normal;
            if(textured && midpoint) {
                Vec3d sum=face.normals[1].add(face.normals[2]);
                shadingNormal=sum.lengthSquared()>1e-12?sum.normalize():face.normal;
            }
            for(int element=0;element<DefaultVertexFormats.ITEM.getElementCount();element++) {
                switch(DefaultVertexFormats.ITEM.getElement(element).getUsage()) {
                    case POSITION:builder.put(element,position[0],position[1],position[2],1);break;
                    case COLOR:builder.put(element,(color>>>24&255)/255f,(color>>>16&255)/255f,(color>>>8&255)/255f,1);break;
                    case UV:
                        if(DefaultVertexFormats.ITEM.getElement(element).getIndex()==0) {
                            float u=UV[vertex][0],v=UV[vertex][1];
                            if(textured) {
                                u=16*(midpoint?(face.uv[1][0]+face.uv[2][0])*.5f:face.uv[source][0]);
                                v=16*(midpoint?(face.uv[1][1]+face.uv[2][1])*.5f:face.uv[source][1]);
                            }
                            builder.put(element,sprite.getInterpolatedU(u),sprite.getInterpolatedV(v),0,1);
                        } else builder.put(element,0,0,0,1);
                        break;
                    case NORMAL:builder.put(element,(float)shadingNormal.x,(float)shadingNormal.y,(float)shadingNormal.z,0);break;
                    default:builder.put(element);
                }
            }
        }
        return builder.build();
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) {
        return side == null ? quads : Collections.emptyList();
    }

    @Override public boolean isAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return sprite; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return base.getItemCameraTransforms(); }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType type) {
        return Pair.of(this, base.handlePerspective(type).getRight());
    }
}
