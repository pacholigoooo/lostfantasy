package dev.lostfantasy.world.gensokyo;

import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

/** Construction primitives, in the local coordinates of one site. Room plans live in each site builder. */
final class GensokyoArchitecture {
    static final IBlockState AIR=Blocks.AIR.getDefaultState(),WOOD=Blocks.PLANKS.getStateFromMeta(1),
            DARK=Blocks.PLANKS.getStateFromMeta(5),WHITE=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(0),
            RED=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(14),STONE=Blocks.STONEBRICK.getDefaultState(),
            ROOF=Blocks.STONEBRICK.getStateFromMeta(3),PAPER=Blocks.STAINED_GLASS_PANE.getStateFromMeta(0),
            LOG=Blocks.LOG.getStateFromMeta(1),SLAB=Blocks.STONE_SLAB.getStateFromMeta(5),
            LIGHT=Blocks.GLOWSTONE.getDefaultState();
    final GensokyoBlueprint plan;final GensokyoAtlas site;private final int originX,originY,originZ;private final String title;
    private final Rotation rotation;
    GensokyoArchitecture(GensokyoBlueprint plan,GensokyoAtlas site) {this(plan,site,0,0,site.title);}
    GensokyoArchitecture(GensokyoBlueprint plan,GensokyoAtlas site,Rotation rotation) {this(plan,site,0,0,site.title,rotation);}
    GensokyoArchitecture(GensokyoBlueprint plan,GensokyoAtlas site,int dx,int dz,String title) {
        this(plan,site,dx,dz,title,Rotation.NONE);
    }
    GensokyoArchitecture(GensokyoBlueprint plan,GensokyoAtlas site,int dx,int dz,String title,Rotation rotation) {
        this(plan,site,dx,dz,title,rotation,0);
    }
    GensokyoArchitecture(GensokyoBlueprint plan,GensokyoAtlas site,int dx,int dz,String title,Rotation rotation,int elevation) {
        this.plan=plan;this.site=site;originX=site.x+dx;originY=site.y+elevation;originZ=site.z+dz;this.title=title;this.rotation=rotation;
    }
    private BlockPos position(int x,int y,int z) {
        BlockPos local=new BlockPos(x,y,z).rotate(rotation);
        return local.add(originX,originY,originZ);
    }
    void box(int x1,int y1,int z1,int x2,int y2,int z2,IBlockState state) {
        BlockPos p=position(x1,y1,z1),q=position(x2,y2,z2);
        plan.box(Math.min(p.getX(),q.getX()),p.getY(),Math.min(p.getZ(),q.getZ()),
                Math.max(p.getX(),q.getX()),q.getY(),Math.max(p.getZ(),q.getZ()),state.withRotation(rotation));
    }
    void block(int x,int y,int z,IBlockState state) {box(x,y,z,x,y,z,state);}
    void room(String name,int x,int y,int z) {BlockPos p=position(x,y,z);plan.room(title+"·"+name,p.getX(),p.getY(),p.getZ());}
    void hall(int x1,int z1,int x2,int z2,int floor,int height,IBlockState post) {
        box(x1-1,0,z1-1,x2+1,floor-1,z2+1,STONE);
        box(x1-1,floor,z1-1,x2+1,floor,z2+1,WOOD);
        box(x1,floor+1,z1,x2,floor+height,z2,WHITE);
        box(x1+1,floor+1,z1+1,x2-1,floor+height-1,z2-1,AIR);
        for(int x=x1;x<=x2;x+=6) {
            box(x,floor+1,z1,x,floor+height,z1,post);box(x,floor+1,z2,x,floor+height,z2,post);
            if(x+4<x2) {
                box(x+1,floor+2,z1,x+4,floor+height-2,z1,PAPER);
                box(x+1,floor+2,z2,x+4,floor+height-2,z2,PAPER);
            }
        }
        for(int z=z1;z<=z2;z+=6) {
            box(x1,floor+1,z,x1,floor+height,z,post);box(x2,floor+1,z,x2,floor+height,z,post);
            if(z+4<z2) {
                box(x1,floor+2,z+1,x1,floor+height-2,z+4,PAPER);
                box(x2,floor+2,z+1,x2,floor+height-2,z+4,PAPER);
            }
        }
        box(x1,floor+height,z1,x2,floor+height,z1,DARK);
        box(x1,floor+height,z2,x2,floor+height,z2,DARK);
        box(x1,floor+height,z1,x1,floor+height,z2,DARK);
        box(x2,floor+height,z1,x2,floor+height,z2,DARK);
        gable(x1-3,z1-3,x2+3,z2+3,floor+height+1);
    }
    /** Ridge along X, projecting eaves and closed gable ends. */
    void gable(int x1,int z1,int x2,int z2,int y) {
        int middle=(z1+z2)/2,depth=(z2-z1)/2;
        for(int z=z1;z<=z2;z++) {
            int rise=(depth-Math.abs(z-middle))/2;
            box(x1,y+rise,z,x2,y+rise,z,ROOF);
            if(rise>0) {
                box(x1+3,y,z,x1+3,y+rise-1,z,DARK);
                box(x2-3,y,z,x2-3,y+rise-1,z,DARK);
            }
            if(z==z1 || z==z2)box(x1-1,y+1,z,x2+1,y+1,z,SLAB);
        }
        box(x1-1,y+depth/2+1,middle,x2+1,y+depth/2+1,middle,SLAB);
    }
    void gableZ(int x1,int z1,int x2,int z2,int y) {
        int middle=(x1+x2)/2,width=(x2-x1)/2;
        for(int x=x1;x<=x2;x++) {
            int rise=(width-Math.abs(x-middle))/2;
            box(x,y+rise,z1,x,y+rise,z2,ROOF);
            if(rise>0)for(int z:new int[]{z1+2,z2-2})box(x,y,z,x,y+rise-1,z,DARK);
            if(x==x1 || x==x2)box(x,y+1,z1-1,x,y+1,z2+1,SLAB);
        }
        box(middle,y+width/2+1,z1-1,middle,y+width/2+1,z2+1,SLAB);
    }
    void openZ(int x,int z,int floor,int halfWidth,int height) {box(x-halfWidth,floor+1,z,x+halfWidth,floor+height,z,AIR);}
    void openX(int x,int z,int floor,int halfWidth,int height) {box(x,floor+1,z-halfWidth,x,floor+height,z+halfWidth,AIR);}
    void stairsSouth(int x,int z,int bottom,int top,int halfWidth) {
        IBlockState stair=Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH);
        for(int rise=1;rise<=top-bottom;rise++) {
            int at=z+(top-bottom)-rise;
            if(rise>1)box(x-halfWidth,bottom+1,at,x+halfWidth,bottom+rise-1,at,STONE);
            box(x-halfWidth,bottom+rise,at,x+halfWidth,bottom+rise,at,stair);
        }
    }
    void lamp(int x,int y,int z) {
        block(x,y,z,STONE);block(x,y+1,z,Blocks.COBBLESTONE_WALL.getDefaultState());
        block(x,y+2,z,LIGHT);block(x,y+3,z,SLAB);
    }
    void table(int x,int y,int z,int length) {
        for(int i=0;i<length;i++) {block(x+i,y+1,z,Blocks.OAK_FENCE.getDefaultState());block(x+i,y+2,z,Blocks.WOODEN_SLAB.getDefaultState());}
    }
    void chest(int x,int y,int z) {block(x,y+1,z,Blocks.CHEST.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));}
    void chest(int x,int floor,int z,String loot) {
        chest(x,floor,z);
        BlockPos p=position(x,floor+1,z);
        plan.container(p.getX(),p.getY(),p.getZ(),new net.minecraft.util.ResourceLocation("lostfantasy","chests/"+loot));
    }
    void sign(int x,int y,int z,EnumFacing facing,String first,String second) {
        block(x,y,z,Blocks.WALL_SIGN.getDefaultState().withProperty(BlockHorizontal.FACING,facing));
        plan.sign(position(x,y,z),first,second);
    }
    void bed(int x,int floor,int z) {
        IBlockState bed=Blocks.BED.getDefaultState().withProperty(BlockBed.FACING,EnumFacing.NORTH);
        block(x,floor+1,z,bed.withProperty(BlockBed.PART,BlockBed.EnumPartType.FOOT));
        block(x,floor+1,z-1,bed.withProperty(BlockBed.PART,BlockBed.EnumPartType.HEAD));
    }
}
